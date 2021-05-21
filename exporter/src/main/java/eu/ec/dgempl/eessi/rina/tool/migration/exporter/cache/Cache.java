package eu.ec.dgempl.eessi.rina.tool.migration.exporter.cache;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;

/**
 * Class that provides means of basic caching
 */
public class Cache {
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /**
     * Wrapper over {@link ConcurrentHashMap#get(Object)}
     *
     * @param key
     * @return
     */
    public CacheEntry get(String key) {
        PreconditionsHelper.notEmpty(key, "key");

        return cache.get(key);
    }

    /**
     * Wrapper over {@link ConcurrentHashMap#putIfAbsent(Object, Object)}
     *
     * @param key
     * @param value
     */
    public void put(String key, CacheEntry value) {
        PreconditionsHelper.notEmpty(key, "key");
        PreconditionsHelper.notNull(value, "value");

        cache.putIfAbsent(key, value);
    }

    /**
     * Method for deleting entries from cache according to specific rules injected through the {@code predicate}
     *
     * @param predicate
     */
    public void deleteEntries(Predicate<CacheEntry> predicate) {
        cache.entrySet().removeIf(entry -> predicate.test(entry.getValue()));
    }

    /**
     * Method for deleting entries from cache by a given set of keys
     *
     * @param keys
     */
    public void deleteEntries(String... keys) {
        PreconditionsHelper.notNull(keys, "keys");

        if (keys.length == 0) {
            return;
        } else if (keys.length == 1) {
            cache.remove(keys[0]);
        } else {
            Arrays.stream(keys).forEach(cache::remove);
        }
    }
}

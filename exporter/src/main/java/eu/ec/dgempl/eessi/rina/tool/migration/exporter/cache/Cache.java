package eu.ec.dgempl.eessi.rina.tool.migration.exporter.cache;

import java.util.Iterator;
import java.util.Map;
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
        Iterator<Map.Entry<String, CacheEntry>> iterator = cache.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, CacheEntry> entry = iterator.next();
            if (predicate.test(entry.getValue())) {
                iterator.remove();
            }
        }
    }
}

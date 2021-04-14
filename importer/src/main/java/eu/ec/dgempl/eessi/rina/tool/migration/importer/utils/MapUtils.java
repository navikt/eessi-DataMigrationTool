package eu.ec.dgempl.eessi.rina.tool.migration.importer.utils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class MapUtils {

    public static void visitField(final Map<String, Boolean> properties, String path, String field) {
        String fieldPath = path + "." + field;

        if (properties.containsKey(fieldPath) && !properties.get(fieldPath)) {
            properties.put(fieldPath, true);
        }
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}

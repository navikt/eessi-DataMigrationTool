package eu.ec.dgempl.eessi.rina.tool.migration.importer.utils;

import java.util.Map;

public class MapUtils {

    public static void visitField(final Map<String, Boolean> properties, String path, String field) {
        String fieldPath = path + "." + field;

        if (properties.containsKey(fieldPath) && !properties.get(fieldPath)) {
            properties.put(fieldPath, true);
        }
    }
}

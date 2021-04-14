package eu.ec.dgempl.eessi.rina.tool.migration.exporter.util;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.util.StringUtils;

import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;

/**
 * Util class for handling json paths
 */
public class JsonPathHelper {

    /**
     * Method for getting the value of the field found by traversing a specific {@code path} in the {@code doc}
     * 
     * @param doc
     *            the map object to be traversed
     * @param path
     *            the path in the object
     * @return the object found at the specific {@code path}
     */
    public static Object getObjectInMapByPath(Map<String, Object> doc, String path) {
        PreconditionsHelper.notNull(doc, "doc");

        if (StringUtils.isEmpty(path)) {
            return null;
        }
        int index = path.indexOf(".");
        if (index == -1) {
            return doc.getOrDefault(path, null);
        } else {
            String key = path.substring(0, index);
            String newPath = path.substring(index + 1);
            Object value = doc.getOrDefault(key, null);
            if (value == null) {
                return null;
            } else if (value instanceof Map) {
                return getObjectInMapByPath((Map<String, Object>) value, newPath);
            } else if (value instanceof ArrayList) {
                return getObjectInMapByPath(((ArrayList<Map<String, Object>>) value).get(0), newPath);
            } else {
                return null;
            }
        }
    }

    /**
     * Method that normalizes the json path by removing array indices from the path. For example, the path
     * `conversations.userMessages[0].receiver` will be normalised as `conversations.userMessages.receiver`
     *
     * @param path
     *            the json path
     * @return
     */
    public static String normalisePath(String path) {
        PreconditionsHelper.notNull(path, "path");

        return path.replaceAll("\\[\\d+\\]", "");
    }
}

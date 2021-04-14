package eu.ec.dgempl.eessi.rina.tool.migration.exporter.util;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility class, which help with the navigation inside a map of maps.
 */
public class ContentNavigator {

    /**
     * Returns the object identified by the path from the given {@code map}. The {@code path} array defines the object hierarchy within the
     * map. Returns the entry from the map hierarchy represented by the given {@code path}. Returns {@code null} if:
     * <ol>
     * <li>The {@code map} is {@code null}</li>
     * <li>The {@code path} is {@code null}</li>
     * <li>The {@code path.length} is 0</li>
     * </ol>
     *
     *
     * @param map
     *            Map to traverse
     * @param path
     *            array of String keys representing the path to the field.
     * @return
     * @throws IllegalArgumentException
     *             If any of the entries on the index {@code 1} - {@code path.length - 1} is not a map.
     */
    public static Object getField(Map<String, Object> map, String... path) throws IllegalArgumentException {

        // if the path is empty return null
        if (map == null || path == null || path.length == 0) {
            return null;
        }

        // get the first entry from the path
        Object value = map.get(path[0]);

        // check value (value may be null if not required by the schema)
        if (value == null) {
            return null;
        }

        // if there is only one field in path, return the entry
        if (path.length == 1) {
            return value;
        }

        // otherwise go deeper
        if (value != null && Map.class.isAssignableFrom(value.getClass())) {
            return getField((Map<String, Object>) value, Arrays.copyOfRange(path, 1, path.length));
        } else {
            throw new IllegalArgumentException("Invalid path: " + Arrays.stream(path).collect(Collectors.joining("->")));
        }

    }

    /**
     * Tests the field from the given {@code map} identified by the {@code path} against the {@code predicate}. Uses the
     * {@link ContentNavigator#getField(Map, String...)} to retrieve the value
     *
     * @param map
     * @param predicate
     * @param path
     * @return
     */
    public static boolean checkValue(Map<String, Object> map, Predicate<Object> predicate, String... path) {

        // if the path is empty return null
        if (map == null || path == null || path.length == 0) {
            return false;
        }

        // get the value and test
        Object value = getField(map, path);
        return predicate.test(value);

    }

}

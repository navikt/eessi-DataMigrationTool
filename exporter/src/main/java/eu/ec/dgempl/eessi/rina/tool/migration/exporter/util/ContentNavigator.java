package eu.ec.dgempl.eessi.rina.tool.migration.exporter.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;

/**
 * Utility class, which help with the navigation inside a map of maps.
 */
public class ContentNavigator {

    private static final Pattern pattern = Pattern.compile("([^\\[]+)(\\[(\\d+)\\])?");

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
     * Returns the object identified by the path from the given {@code map}. The map can contain arrays and the indices of this arrays are
     * specified in the {@code path} parameter. The {@code path} LinkedHashMap defines the object hierarchy as the keySet and the indices in
     * case the field is an array or null otherwise as the valueSet.
     * View the {@code constructNavigationPath} method to construct a valid LinkedHashMap {@code path} argument.
     * Returns the entry from the map hierarchy represented by the given {@code path}. Returns
     * {@code null} if:
     * <ol>
     * <li>The {@code map} is {@code null}</li>
     * <li>The {@code path} is {@code null}</li>
     * <li>The {@code path.length} is 0</li>
     * </ol>
     *
     * @param map
     *            Map to traverse
     * @param path
     *            LinkedHashMap containing the field names of the path and the indices in case the fields are arrays.
     *            View the {@code constructNavigationPath} method to construct a valid LinkedHashMap {@code path} argument.
     * @return
     * @throws IllegalArgumentException
     *             If any of the entries on the index {@code 1} - {@code path.length - 1} is not a map.
     */
    public static Object getFieldWithArrays(Map<String, Object> map, LinkedHashMap<String, Integer> path) throws IllegalArgumentException {

        // if the path or the map is empty return null
        if (map == null || path == null || path.size() == 0) {
            return null;
        }

        // get the first entry from the path
        Map.Entry pathValue = path.entrySet().iterator().next();
        // get the value from the map
        Object value = map.get(pathValue.getKey());

        // check value (value may be null if not required by the schema)
        if (value == null) {
            return null;
        }

        // if there is only one field in path, return the entry
        if (path.size() == 1) {
            return value;
        }

        if (pathValue.getValue() == null) {
            // if the value is not an array, go deeper in the Map object
            path.entrySet().remove(pathValue);
            return getFieldWithArrays((Map) value, path);
        } else if (pathValue.getValue() instanceof Integer) {
            // if the value is an array, go deeper in array of the Map object in the index from path value
            Integer index = (Integer) pathValue.getValue();
            if (value instanceof ArrayList) {
                Map v = (Map) ((ArrayList) value).get(index);
                path.entrySet().remove(pathValue);
                return getFieldWithArrays((Map<String, Object>) v, path);
            } else {
                throw new IllegalArgumentException(
                        "Invalid path: " + Arrays.stream(path.keySet().toArray()).map(String::valueOf).collect(Collectors.joining("->")));
            }
        } else {
            throw new IllegalArgumentException(
                    "Invalid path: " + Arrays.stream(path.keySet().toArray()).map(String::valueOf).collect(Collectors.joining("->")));
        }
    }

    /**
     * Returns the LinkedHashMap<String, Integer> constructed from the {@code path} parameter. The {@code path} must have a format like in
     * the given example "fieldName1[1].fieldName2[0].fieldName3.fieldName4" where there could be array fields and non-array fields. The
     * method will return a LinkedHashMap<String, Integer> containing the field name as the key and, in case the field is an array, the
     * index in the value or null otherwise.
     * Result for previous given example:
     * <ol>
     * <li>fieldName1 -> 1,</li>
     * <li>fieldName2 -> 0,</li>
     * <li>fieldName3 -> null,</li>
     * <li>fieldName4 -> null</li>
     * </ol>
     * The entries in the LinkedHashMap will be ordered.
     * IMPORTANT: this method is not valid for {@code path} with field names containing the character '.'.
     * Returns the LinkedHashMap<String, Integer> from the hierarchy represented by the given {@code path}.
     *
     * @param path
     *            the structured input path
     * @return LinkedHashMap<String, Object>
     * @throws IllegalArgumentException
     *             If the structure does not match the expected pattern.
     */
    public static LinkedHashMap<String, Integer> constructNavigationPath(String path) {
        PreconditionsHelper.notNull(path, "path");
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        String[] tokens = path.split("\\.");
        for (String token : tokens) {
            Matcher matcher = pattern.matcher(token);
            if (matcher.matches()) {
                String key = matcher.group(1);
                Integer value = matcher.group(3) != null ? Integer.valueOf(matcher.group(3)) : null;
                map.put(key, value);
            } else {
                throw new IllegalArgumentException(String.format("Invalid navigation path [path=%s]", path));
            }
        }
        return map;
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

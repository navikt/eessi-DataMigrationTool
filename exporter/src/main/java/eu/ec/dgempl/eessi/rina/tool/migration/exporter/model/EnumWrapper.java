package eu.ec.dgempl.eessi.rina.tool.migration.exporter.model;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;

/**
 * Helper class for storing relevant enum information
 */
public class EnumWrapper {
    private final Map<String, List<String>> entries;

    public EnumWrapper(Class clazz) throws IllegalAccessException {
        entries = new HashMap<>();
        init(clazz);
    }

    /**
     * Method that initializes the entries of the EnumWrapper. The method gets through reflection the list of constants and maps each of
     * them to their associated values, all lower cased. For instance, an enum entry like `PENSION("Pension", "P")` will generate the map
     * entry `pension -> ("pension", "p")`
     * 
     * @param clazz
     *            the enum class
     * @throws IllegalAccessException
     */
    private void init(Class clazz) throws IllegalAccessException {
        PreconditionsHelper.notNull(clazz, "clazz");

        Object[] names = clazz.getEnumConstants();

        List<Field> fields = Arrays.stream(clazz.getDeclaredFields())
                .filter(x -> !x.isEnumConstant() && !x.isSynthetic() && x.getType().equals(String.class)).collect(Collectors.toList());

        for (Object name : names) {
            String stringifiedName = name.toString().toLowerCase();

            List<String> values = entries.get(stringifiedName);
            if (values == null) {
                values = new LinkedList<>();
            }

            for (Field field : fields) {
                field.setAccessible(true);
                String value = (String) field.get(name);
                values.add(value.toLowerCase());
            }

            entries.put(stringifiedName, values);
        }
    }

    /**
     * Method for looking up values in the enum name and values. The method returns true if {@code value} is found in the list of name and
     * values, false otherwise
     * 
     * @param value
     *            the value that is searched in the enum
     * @return
     */
    public boolean lookup(String value) {
        PreconditionsHelper.notNull(value, "value");

        for (Map.Entry<String, List<String>> entry : entries.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(value)) {
                return true;
            }
            for (String val : entry.getValue()) {
                if (val.equalsIgnoreCase(value)) {
                    return true;
                }
            }
        }

        return false;
    }
}

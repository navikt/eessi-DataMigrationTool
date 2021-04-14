package eu.ec.dgempl.eessi.rina.tool.migration.exporter.util;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;

/**
 * Util class for parsing field info properties
 */
public class FieldInfoProperty {
    private static final Pattern PARAMS_REGEX = Pattern.compile("([^\\(]+)\\((.+)\\)");
    private final String propertyName;
    private final String propertyValue;
    private final String type;
    private final String[] params;

    /**
     * Private constructor
     * 
     * @param propertyName
     *            the name of the field info property
     * @param propertyValue
     *            the raw value of the field info property
     * @param type
     *            the type extracted from the propertyValue
     * @param params
     *            the params extracted from the propertyValue
     */
    private FieldInfoProperty(String propertyName, String propertyValue, String type, String... params) {
        PreconditionsHelper.notEmpty(propertyName, "propertyName");
        PreconditionsHelper.notNull(propertyValue, "propertyValue");// can be empty

        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.type = type;
        this.params = params;
    }

    /**
     * Static factory method for creating {@link FieldInfoProperty} instances based on the field info properties.
     *
     * @param name
     *            the name of the field info property
     * @param value
     *            the raw value of the field info property
     * @return an {@link FieldInfoProperty} instance containing detailed information on the field info property
     */
    public static FieldInfoProperty fromProperty(String name, String value) {
        PreconditionsHelper.notEmpty(name, "name");
        PreconditionsHelper.notNull(value, "value");// can be empty

        // remove leading and trailing whitespace
        String trimmedValue = value.trim();

        Matcher matcher = PARAMS_REGEX.matcher(trimmedValue);

        // if it matches, parse the parameters
        if (matcher.matches()) {
            String type = matcher.group(1);
            String rawParams = matcher.group(2);

            String[] params;
            if (type.equals("regex")) {
                params = new String[] { rawParams };
            } else {
                // @formatter:off
                params = Arrays.stream(rawParams.split(","))
                        .map(String::trim)
                        .toArray(size -> new String[size]);
                // @formatter:on
            }

            return new FieldInfoProperty(name, value, type, params);
        } else {
            return new FieldInfoProperty(name, value, trimmedValue, new String[] {});
        }
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public String getType() {
        return type;
    }

    public String[] getParams() {
        return params;
    }
}

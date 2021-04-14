package eu.ec.dgempl.eessi.rina.tool.migration.exporter.model;

/**
 * Enum that defines the possible validation results
 */
public enum EValidationResult {
    // @formatter:off
    // valid results
    VALID("valid"),

    // not valid results
    INVALID_BOOLEAN("invalidBoolean"),
    INVALID_DATE("invalidDate"),
    INVALID_DUPLICATE("invalidDuplicate"),
    INVALID_ENUM("invalidEnum"),
    INVALID_INTEGER("invalidInteger"),
    INVALID_LENGTH("invalidLength"),
    INVALID_LONG("invalidLong"),
    INVALID_NULL("invalidNull"),
    INVALID_REFERENCE("invalidReference"),
    INVALID_STRING("invalidString"),
    UNKNOWN_FIELD("unknownField");
    // @formatter:on

    private final String value;

    EValidationResult(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EValidationResult fromValue(String v) {
        for (EValidationResult c : EValidationResult.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}

package eu.ec.dgempl.eessi.rina.tool.migration.common.model;

/**
 * Enum that defines the list of elasticsearch indices
 */
public enum EEsIndex {
    // @formatter:off
    ADMIN("admin"),
    APCLIENTLOG("apclientlog"),
    AUDIT("audit"),
    BONITALOG("bonitalog"),
    BUSINESSEXCEPTIONS("businessexceptions"),
    CASES("cases"),
    CHECKS("checks"),
    CONFIGURATIONS("configurations"),
    TOKEN("token"),
    ENTITIES("entities"),
    GLOBALCONFIGURATIONS("globalconfigurations"),
    IDENTITY_V1("identity_v1"),
    LETTERTEMPLATES("lettertemplates"),
    NOTIFICATIONS("notifications"),
    RESOURCES("resources"),
    RESTLOG("restlog"),
    SEQUENCES("sequences"),
    SYNCHRONIZATIONS("synchronizations"),
    VOCABULARIES("vocabularies");
    // @formatter:on

    private final String value;

    EEsIndex(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EEsIndex fromValue(String v) {
        for (EEsIndex c : EEsIndex.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}

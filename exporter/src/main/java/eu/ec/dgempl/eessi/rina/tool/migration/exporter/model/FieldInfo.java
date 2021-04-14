package eu.ec.dgempl.eessi.rina.tool.migration.exporter.model;

/**
 * Helper class used when parsing jsons
 */
public class FieldInfo {
    private String esPath;
    private String esType;
    private String sqlPath;
    private String sqlType;
    private boolean required;
    private boolean ignorePath;
    private String value;

    public String getEsPath() {
        return esPath;
    }

    public String getEsType() {
        return esType;
    }

    public String getSqlPath() {
        return sqlPath;
    }

    public String getSqlType() {
        return sqlType;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isIgnorePath() {
        return ignorePath;
    }

    public String getValue() {
        return value;
    }

}
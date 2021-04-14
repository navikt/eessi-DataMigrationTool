package eu.ec.dgempl.eessi.rina.tool.migration.common.model;

import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;

/**
 * Class that defines a search query term
 */
public class EsSearchQueryTerm {
    private final String field;
    private final String value;

    public EsSearchQueryTerm(String field, String value) {
        PreconditionsHelper.notEmpty(field, "field");
        PreconditionsHelper.notEmpty(value, "value");

        this.field = field;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }
}

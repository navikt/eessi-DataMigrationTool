package eu.ec.dgempl.eessi.rina.tool.migration.exporter.model;

import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.schema.Schema;

/**
 * Placeholder for contextual information needed during document validation
 */
public class ValidationContext {
    private final EsDocument document;
    private final EsDocument parent;
    private final Schema schema;

    public ValidationContext(EsDocument document, EsDocument parent, Schema schema) {
        PreconditionsHelper.notNull(document, "document");
        PreconditionsHelper.notNull(parent, "parent");
        PreconditionsHelper.notNull(schema, "schema");

        this.document = document;
        this.parent = parent;
        this.schema = schema;
    }

    public EsDocument getDocument() {
        return document;
    }

    public EsDocument getParent() {
        return parent;
    }

    public Schema getSchema() {
        return schema;
    }

}

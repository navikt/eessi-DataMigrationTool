package eu.ec.dgempl.eessi.rina.tool.migration.exporter.cache;

import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;

public class CacheEntry {
    private final boolean exists;
    private final String index;
    private final String type;
    private final String documentId;
    private final String context;

    public CacheEntry(boolean exists, String index, String type, String documentId, String context) {
        PreconditionsHelper.notEmpty(index, "index");
        PreconditionsHelper.notEmpty(type, "type");
        PreconditionsHelper.notEmpty(documentId, "documentId");
        PreconditionsHelper.notEmpty(context, "context");

        this.exists = exists;
        this.index = index;
        this.type = type;
        this.documentId = documentId;
        this.context = context;
    }

    public boolean isExists() {
        return exists;
    }

    public String getIndex() {
        return index;
    }

    public String getType() {
        return type;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getContext() {
        return context;
    }
}

package eu.ec.dgempl.eessi.rina.tool.migration.exporter.model;

import java.util.HashMap;
import java.util.Map;

import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;

/**
 * Class that defines the context of an elasticsearch document
 */
public class EsDocument {
    private final String index;
    private final String type;
    private final String objectId;
    private Map<String, Object> object;

    public EsDocument(String index, String type, String objectId) {
        PreconditionsHelper.notEmpty(index, "index");
        PreconditionsHelper.notEmpty(type, "type");
        PreconditionsHelper.notEmpty(objectId, "objectId");

        if (index.matches("identity_v.*")) {
            index = "identity";
        }

        this.index = index;
        this.type = type;
        this.objectId = objectId;
        object = new HashMap<>();
    }

    public String getIndex() {
        return index;
    }

    public String getType() {
        return type;
    }

    public String getObjectId() {
        return objectId;
    }

    public Map<String, Object> getObject() {
        return object;
    }

    public void setObject(Map<String, Object> object) {
        this.object = object;
    }

}

package eu.ec.dgempl.eessi.rina.tool.migration.exporter.util;

import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;

/**
 * Util class for handling ids
 */
public class IdHelper {

    /**
     * Method that creates a reference id based on the {@code index}, {@code type} and {@code documentId}
     * 
     * @param index
     * @param type
     * @param documentId
     * @return
     */
    public static String getDocumentReference(String index, String type, String documentId) {
        PreconditionsHelper.notEmpty(index, "index");
        PreconditionsHelper.notEmpty(type, "type");
        PreconditionsHelper.notEmpty(documentId, "index");

        return index + "_" + type + "_" + documentId;
    }
}

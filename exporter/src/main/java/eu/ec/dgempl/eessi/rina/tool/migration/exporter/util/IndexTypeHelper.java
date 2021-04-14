package eu.ec.dgempl.eessi.rina.tool.migration.exporter.util;

import java.util.LinkedList;
import java.util.List;

import eu.ec.dgempl.eessi.rina.tool.migration.common.model.EEsIndexType;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;

/**
 * Util class for handling Elasticsearch indices and types
 */
public class IndexTypeHelper {

    /**
     * Method that finds all the types from a specific {@code index}
     * 
     * @param index
     *            the elasticsearch index
     * @return
     */
    public static List<String> getTypesByIndex(String index) {
        PreconditionsHelper.notEmpty(index, "index");

        List<String> results = new LinkedList<>();
        String indexPrefix = index.toLowerCase() + "_";

        for (EEsIndexType indexType : EEsIndexType.values()) {
            if (indexType.value().toLowerCase().startsWith(indexPrefix)) {
                results.add(indexType.value().substring(indexPrefix.length()));
            }
        }

        return results;
    }

}

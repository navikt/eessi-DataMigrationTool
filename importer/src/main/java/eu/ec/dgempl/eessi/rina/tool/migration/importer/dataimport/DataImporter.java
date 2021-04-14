package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport;

import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;

/**
 * Common contract for a data importer from a datasource
 *
 * @author Silviu Stefan
 */
public interface DataImporter {

    default void importData() {
    }

    default void importData(String caseId) {
    }

    EElasticType inferElasticType();
}

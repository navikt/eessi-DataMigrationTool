package eu.ec.dgempl.eessi.rina.tool.migration.importer.datareport;

import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;

public interface DataReporter {

    void reportProblem(EElasticType elasticType, String id, String message, Object... e);

}

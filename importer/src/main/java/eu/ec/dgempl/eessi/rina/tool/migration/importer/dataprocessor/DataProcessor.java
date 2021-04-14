package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataprocessor;

import java.util.function.Consumer;

import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

public interface DataProcessor {

    DocumentsReport process(EElasticType eElasticType, Consumer<MapHolder> docProcessor, boolean transactional) throws Exception;

    DocumentsReport process(EElasticType eElasticType, Consumer<MapHolder> docProcessor, String caseId, boolean transactional)
            throws Exception;

    long countDocsByCaseId(String caseId, EElasticType eElasticType) throws Exception;
}

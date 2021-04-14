package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataprocessor;

import java.util.function.Consumer;

import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

public interface DataProcessor {

    void process(EElasticType eElasticType, Consumer<MapHolder> docProcessor, boolean transactional) throws Exception;

    void process(EElasticType eElasticType, Consumer<MapHolder> docProcessor, String caseId, boolean transactional) throws Exception;
}

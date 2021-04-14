package eu.ec.dgempl.eessi.rina.tool.migration.importer.datareport.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.tool.migration.importer.datareport.DataReporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;

@Component
public class ConsoleDataReporter implements DataReporter {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleDataReporter.class);
    private static final String MESSAGE_FORMAT = "Error during import of %s, with id %s: %s";

    @Override
    public void reportProblem(EElasticType elasticType, String id, String message, Object... e) {
        logger.error(String.format(MESSAGE_FORMAT, elasticType, id, message));
    }
}

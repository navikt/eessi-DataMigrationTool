package eu.ec.dgempl.eessi.rina.tool.migration.exporter.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.ec.dgempl.eessi.rina.tool.migration.common.model.EEsIndexType;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.schema.Schema;

/**
 * Service that defines methods for retrieving instances of {@link Schema}
 */
@Service
public class SchemaProviderService {
    private static final Logger logger = LoggerFactory.getLogger(SchemaProviderService.class);

    // this is private, must be populated in the constructor
    // the class should not provide means of altering the schemaMap
    private Map<String, Schema> schemaMap = new HashMap<>();

    @Autowired
    public SchemaProviderService(ValidatorProviderService validatorProvider, @Value("${schema.path}") String fieldMappingsFolder)
            throws IOException {
        // initialize all schemas for the existing field mapping jsons
        for (EEsIndexType indexType : EEsIndexType.values()) {
            String schemaId = indexType.value();
            String schemaPath = fieldMappingsFolder + "/" + schemaId + ".json";
            Schema schema = Schema.from(schemaPath, validatorProvider);
            if (schema != null) {
                schemaMap.put(schemaId, schema);
            }
        }
    }

    /**
     * Method for retrieving the {@link Schema} corresponding to the combination of {@code index} and {@code type} from the private schema
     * map
     * 
     * @param index
     *            the elasticsearch index
     * @param type
     *            the elasticsearch document type
     * @return the {@link Schema} instance
     */
    public Schema getSchema(String index, String type) {
        PreconditionsHelper.notEmpty(index, "index");
        PreconditionsHelper.notEmpty(type, "type");

        EEsIndexType indexType = EEsIndexType.fromValue(index + "_" + type);
        return schemaMap.getOrDefault(indexType.value(), null);
    }
}

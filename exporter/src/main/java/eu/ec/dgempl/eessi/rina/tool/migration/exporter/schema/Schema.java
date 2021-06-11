package eu.ec.dgempl.eessi.rina.tool.migration.exporter.schema;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.FieldInfo;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.service.ValidatorProviderService;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.GsonWrapper;

/**
 * Class for creating schemas used in the process of validation
 */
public class Schema {
    private final String jsonMappingFile;
    private final Map<String, SchemaEntry> mapping;
    private final List<String> requiredPaths;

    /**
     * Private constructor
     */
    private Schema(String jsonMappingFile, Map<String, SchemaEntry> mapping, List<String> requiredPaths) {
        PreconditionsHelper.notEmpty(jsonMappingFile, "jsonMappingFile");
        PreconditionsHelper.notNull(mapping, "mapping");
        PreconditionsHelper.notNull(requiredPaths, "requiredPaths");

        this.jsonMappingFile = jsonMappingFile;
        this.mapping = mapping;
        this.requiredPaths = requiredPaths;
    }

    /**
     * Static factory method for creating {@link Schema} instances by parsing json files that describe the fields and how to validate them.
     * The {@code jsonMappingFile} needs to be in the classpath.
     * 
     * @param jsonMappingFile
     *            json file containing the list of field information
     * @param validatorProvider
     *            instance of a service that provides validators
     * @return instance of {@link Schema}
     * @throws FileNotFoundException
     */
    public static Schema from(String jsonMappingFile, ValidatorProviderService validatorProvider) throws IOException {
        PreconditionsHelper.notEmpty(jsonMappingFile, "jsonMappingFile");
        PreconditionsHelper.notNull(validatorProvider, "validatorProvider");

        Map<String, SchemaEntry> mapping = new HashMap<>();
        List<String> requiredPaths = new LinkedList<>();

        // load the field info details from the json mapping file
        FieldInfo[] fieldInfos = GsonWrapper.loadFromDisk(jsonMappingFile, FieldInfo[].class);

        if (fieldInfos != null) {
            // for each FieldInfo, get the proper validators and add an entry in the field-validators map
            for (FieldInfo fieldInfo : fieldInfos) {
                // check if esPath is null or empty
                if (StringUtils.isEmpty(fieldInfo.getEsPath())) {
                    throw new IllegalStateException(
                            String.format("Invalid field path in schema [path=%s,mappingFile=%s]", fieldInfo.getEsPath(), jsonMappingFile));
                }

                // check for duplicate fields
                if (mapping.get(fieldInfo.getEsPath()) != null) {
                    throw new IllegalStateException(String.format("Duplicate definition in schema [path=%s,mappingFile=%s]",
                            fieldInfo.getEsPath(), jsonMappingFile));
                }

                // create a schema entry
                SchemaEntry entry = SchemaEntry.from(fieldInfo.getEsPath(), fieldInfo.isIgnorePath(),
                        validatorProvider.getValidators(fieldInfo));

                mapping.put(fieldInfo.getEsPath(), entry);

                if (fieldInfo.isRequired()) {
                    requiredPaths.add(fieldInfo.getEsPath());
                }
            }
        }

        // initialize the schema
        return new Schema(jsonMappingFile, mapping, requiredPaths);
    }

    /**
     * Method for retrieving the entry in the schema that corresponds to {@code esFieldPath}
     *
     * @param esFieldPath
     *            the path to the object that is validated
     * @return
     */
    public SchemaEntry getSchemaEntry(String esFieldPath) {
        PreconditionsHelper.notNull(esFieldPath, "esFieldPath"); // can be ""

        return mapping.getOrDefault(esFieldPath, null);
    }

    /**
     * Method that returns the list of fields contained in the object referenced by {@code esFieldPath}. The method returns the direct
     * children, from the first level down in the hierarchy
     * 
     * @param esFieldPath
     *            the path to the object that is validated
     * @return
     */
    public List<String> getRequiredFields(String esFieldPath) {
        PreconditionsHelper.notNull(esFieldPath, "esFieldPath"); // can be ""

        List<String> fields = new LinkedList<>();

        for (String requiredPath : requiredPaths) {
            if (requiredPath.startsWith(esFieldPath)) {
                String field = null;

                if (esFieldPath.equals("")) {
                    field = requiredPath;
                } else if (requiredPath.length() > esFieldPath.length() + 1){
                    field = requiredPath.substring(esFieldPath.length() + 1);
                }

                if (field != null && !field.contains(".")) {
                    fields.add(field);
                }
            }
        }

        return fields;
    }

    /**
     * Getter for jsonMapping
     * 
     * @return
     */
    public String getJsonMapping() {
        return jsonMappingFile;
    }
}

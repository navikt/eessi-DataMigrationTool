package eu.ec.dgempl.eessi.rina.tool.migration.importer.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.ec.dgempl.eessi.rina.buc.precondition.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.JsonFileUtils;

/**
 * Service that defines methods related with field mappings
 */
@Service
public class FieldMappingService {
    private static final Logger logger = LoggerFactory.getLogger(FieldMappingService.class);

    private String schemaPath;
    private String reportsPath;
    private RinaJsonMapper rinaJsonMapper;
    private Map<String, List<String>> ignoredFields;
    private String outputDirectoryName = "unreviewed_fields";
    private String outputFilesExtension = ".json";

    @Autowired
    public FieldMappingService() {
    }

    /**
     * Method executed on application start that iterates through {@link EElasticType} enum to load respective field mappings and store the
     * fields marked as ignored to import.
     * <p>
     * This method also created a directory to store all .txt containing the unreviewed fields found in the documents. If the directory
     * already exists, the method deletes it and its previous content.
     */
    // @PostConstruct
    public void loadIgnoredFields() {
        if (MapUtils.isEmpty(ignoredFields)) {
            ignoredFields = new HashMap<>();
            for (EElasticType eElasticType : EElasticType.values()) {
                if (eElasticType != EElasticType.NONE) {
                    try {
                        String schemaJson = JsonFileUtils.loadDocument(schemaPath + "/", eElasticType);
                        List<String> values = rinaJsonMapper.jsonToListOfObjects(schemaJson, Map.class).stream()
                                .filter(map -> (map.containsKey("importIgnore") && ((Boolean) map.get("importIgnore")))
                                        || (map.containsKey("ignorePath") && (Boolean) map.get("ignorePath")))
                                .map(entry -> (String) entry.get("esPath")).distinct().collect(Collectors.toList());

                        if (!values.isEmpty()) {
                            ignoredFields.put(eElasticType.toString().toLowerCase(), values);
                        }

                    } catch (Exception e) {
                        throw new RuntimeException("Could not load schema for " + eElasticType.name(), e);
                    }
                }
            }
        }

        File file = new File(reportsPath, "/" + outputDirectoryName + "/");
        try {
            FileUtils.deleteDirectory(file);
            Files.createDirectories(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that iterates through the fields of a document provided and generates a file containing all fields in the document that were
     * not ignored or visited previously
     *
     * @param document         the document to be evaluated
     * @param elasticType      the elastic type associated to the document
     * @param visitedFieldsMap the previously already visited fields of the document
     */
    public List<String> checkUnreviewedFields(MapHolder document, EElasticType elasticType, Map visitedFieldsMap) {
        PreconditionsHelper.notNull(document, "document");
        PreconditionsHelper.notNull(elasticType, "elasticType");
        PreconditionsHelper.notNull(visitedFieldsMap, "visitedFields");

        List<String> ignoredFields = getIgnoredFieldsByFieldMapping(elasticType);
        List<String> visitedFields = new ArrayList(visitedFieldsMap.keySet());

        List<List<String>> results = document.getHolding().entrySet().stream()
                .map(field -> walk(field.getValue(), field.getKey(), visitedFields, ignoredFields)).filter(result -> !(result).isEmpty())
                .collect(Collectors.toList());

        if (!results.isEmpty()) {
            return results.stream().flatMap(List::stream).collect(Collectors.toList());
        } else
            return new ArrayList<>();
    }

    private List<String> getIgnoredFieldsByFieldMapping(EElasticType elasticType) {
        List<String> result = ignoredFields.get(elasticType.toString().toLowerCase());
        return result != null ? result : Collections.emptyList();
    }

    public void writeOutputFiles(EElasticType elasticType, List<String> flat) {
        try {
            String fileName = "/" + outputDirectoryName + "/" + elasticType.getIndex() + "_" + elasticType.getType() + outputFilesExtension;
            File file = new File(reportsPath, fileName);

            if (file.exists()) {
                try {
                    List<String> existingLines = Files.lines(Paths.get(file.toPath().toString())).collect(Collectors.toList());
                    flat.addAll(existingLines);
                    flat = flat.stream().distinct().collect(Collectors.toList());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            FileOutputStream writer = new FileOutputStream(file, false);
            for (String str : flat) {
                writer.write(str.getBytes());
                writer.write(System.lineSeparator().getBytes());
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method used to iterate through a field of a document. This field can be a Map, an Array or a final leaf.
     *
     * @param field         the field to be evaluated
     * @param path          the relation of parents of the field
     * @param visitedFields the previously already visited fields of the document
     * @param ignoredFields the ignored fields of the document
     */
    private <T> List<String> walk(Object field, String path, List<String> visitedFields, List<String> ignoredFields) {

        if (field == null || ignoredFields.contains(removeIndex(path))) {
            return new ArrayList<>();
        }

        if (field instanceof Map) {

            String updatedPath = "".equals(path) ? path : path + ".";

            return ((Map<String, Object>) field).entrySet().stream()
                    .map(entry -> walk(entry.getValue(), updatedPath + entry.getKey(), visitedFields, ignoredFields))
                    .filter(result -> !(result).isEmpty())
                    .reduce(Collections.emptyList(), (a, b) -> Stream.concat(a.stream(), b.stream()).collect(Collectors.toList()));

        } else if (field instanceof ArrayList) {

            ArrayList<Map<String, Object>> array = (ArrayList<Map<String, Object>>) field;

            if (array.size() == 0) {
                return new ArrayList<>();
            }

            if (!(array.get(0) instanceof HashMap)) {
                return checkLeaf(path, visitedFields, ignoredFields);
            }

            return IntStream.range(0, array.size()).mapToObj(i -> walk(array.get(i), path + "[" + i + "]", visitedFields, ignoredFields))
                    .filter(result -> !(result).isEmpty())
                    .reduce(Collections.emptyList(), (a, b) -> Stream.concat(a.stream(), b.stream()).collect(Collectors.toList()));

        } else {
            return checkLeaf(path, visitedFields, ignoredFields);
        }
    }

    private String removeIndex(String path) {
        String pattern = "\\[\\d+\\]";
        return path.replaceAll(pattern, "");
    }

    private List<String> checkLeaf(String path, List<String> visitedFields, List<String> ignoredFields) {
        List<String> results = new ArrayList<>();
        if (!visitedFields.contains(path) && !ignoredFields.contains(path)) {
            results.add(path);
        }
        return results;
    }

    @Autowired
    public void setRinaJsonMapper(RinaJsonMapper rinaJsonMapper) {
        this.rinaJsonMapper = rinaJsonMapper;
    }

    @Autowired
    public void setSchemaPath(@Value("${schema.path}") String schemaPath) {
        this.schemaPath = schemaPath;
    }

    @Autowired
    public void setReportsPath(@Value("${reporting.folder}") String reportsPath) {
        this.reportsPath = reportsPath + "/importer";
    }
}

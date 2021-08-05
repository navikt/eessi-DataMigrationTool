package eu.ec.dgempl.eessi.rina.tool.migration.exporter.report;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.lang.Nullable;

import eu.ec.dgempl.eessi.rina.tool.migration.common.model.EEsIndexType;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.util.JsonPathHelper;

/**
 * Class that defines the validation result at the context level
 */
public class ContextValidationReport {
    private final Map<EEsIndexType, Integer> documents;
    private final Map<String, Object> errorMap;
    private final static String ERROR_TYPE_INVALID_ENUM = "invalidEnum";
    private final static String VALUE_FOUND_FIELD = "valueFound";
    private final static String VALUE_EXPECTED_FIELD = "valueExpected";

    /**
     * Class constructor
     */
    public ContextValidationReport() {
        documents = new HashMap<>();
        errorMap = new HashMap<>();
    }

    /**
     * Method that defines the way {@link CaseValidationReport}s are aggregated at the context level
     * 
     * @param caseReport
     *            the case-level report
     */
    public synchronized void swallow(CaseValidationReport caseReport) {
        PreconditionsHelper.notNull(caseReport, "caseReport");

        // handle documents stats
        caseReport.getDocuments().keySet().stream().forEach(indexType -> {
            // increment the number of documents of a specific type handled
            Integer no = documents.get(indexType);
            if (no == null) {
                documents.put(indexType, caseReport.getDocuments().get(indexType));
            } else {
                documents.put(indexType, no + caseReport.getDocuments().get(indexType));
            }
        });

        // handle errors
        caseReport.getErrors().stream().forEach(error -> {
            if (error.getErrors().size() == 0) {
                return;
            }
            String indexType = error.getIndex() + "_" + error.getType();
            error.getErrors().stream().forEach(err -> {
                addEntryInErrorMap(indexType, err.getResult().value(), JsonPathHelper.normalisePath(err.getField()), err.getValue(),
                        err.getDetails());
            });
        });
    }

    /**
     * Method that defines the way {@link DocumentValidationReport}s are aggregated at the context level
     *
     * @param documentReport
     *            the document-level report
     */
    public synchronized void swallow(DocumentValidationReport documentReport) {
        PreconditionsHelper.notNull(documentReport, "documentReport");

        EEsIndexType indexType = EEsIndexType.fromValue(documentReport.getIndex() + "_" + documentReport.getType());

        // handle documents stats
        Integer no = documents.get(indexType);
        if (no == null) {
            documents.put(indexType, 1);
        } else {
            documents.put(indexType, no + 1);
        }

        // handle errors
        documentReport.getErrors().stream().forEach(error -> {
            addEntryInErrorMap(indexType.value(), error.getResult().value(), JsonPathHelper.normalisePath(error.getField()),
                    error.getValue(), error.getDetails());
        });
    }

    public Map<EEsIndexType, Integer> getDocuments() {
        return documents;
    }

    @Override
    public String toString() {
        return "ContextValidationReport{" + "documents=" + documents + ", errorMap=" + errorMap + '}';
    }

    /**
     * Method for traversing the {@link #errorMap} and updating the stats. If the path does not exist, it is created
     * 
     * @param indexType
     *            compound string in the format index_type
     * @param errType
     *            the type of validation error (see {@link eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.EValidationResult})
     * @param path
     *            the path in the elasticsearch object where the error was found
     * @param value
     *            the value that failed the validation
     */
    private void addEntryInErrorMap(String indexType, String errType, String path, @Nullable Object value, Object details) {
        PreconditionsHelper.notEmpty(indexType, "indexType");
        PreconditionsHelper.notEmpty(errType, "errType");
        PreconditionsHelper.notEmpty(path, "path");

        Map<String, Object> errLevel = (Map<String, Object>) errorMap.get(indexType);
        if (errLevel == null) {
            errLevel = new HashMap<>();
            errorMap.put(indexType, errLevel);
        }

        Map<String, Object> pathLevel = (Map<String, Object>) errLevel.get(errType);
        if (pathLevel == null) {
            pathLevel = new HashMap<>();
            errLevel.put(errType, pathLevel);
        }

        LinkedHashMap<Object, Object> valueLevel = (LinkedHashMap<Object, Object>) pathLevel.get(path);
        if (valueLevel == null) {
            valueLevel = new LinkedHashMap<>();
            pathLevel.put(path, valueLevel);
        }

        valueLevel.putIfAbsent(VALUE_FOUND_FIELD, new HashMap<>());
        Map<Object, Object> valueFoundLevel = (Map<Object, Object>) valueLevel.get(VALUE_FOUND_FIELD);

        // increment the number of occurrences of a specific value found
        Integer no = (Integer) valueFoundLevel.get(value);
        if (no == null) {
            valueFoundLevel.put(value, 1);
        } else {
            valueFoundLevel.put(value, no + 1);
        }

        if (ERROR_TYPE_INVALID_ENUM.equals(errType) && details != null) {
            if (!valueLevel.containsKey(VALUE_EXPECTED_FIELD)) {
                if (details instanceof String) {
                    valueLevel.put(VALUE_EXPECTED_FIELD, details);
                }
            }
        }
    }
}

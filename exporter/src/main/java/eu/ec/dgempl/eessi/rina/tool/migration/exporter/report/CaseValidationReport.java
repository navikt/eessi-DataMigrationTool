package eu.ec.dgempl.eessi.rina.tool.migration.exporter.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.ec.dgempl.eessi.rina.tool.migration.common.model.EEsIndexType;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;

/**
 * Class that defines the validation result at the case level
 */
public class CaseValidationReport {
    private final String caseId;
    private final String tenantId;
    private final Map<EEsIndexType, Integer> documents;
    private final List<DocumentValidationReport> errors;

    /**
     * Class constructor
     * 
     * @param caseId
     *            the case id
     * @param tenantId
     *            the tenant id
     */
    public CaseValidationReport(String caseId, String tenantId) {
        PreconditionsHelper.notEmpty(caseId, "caseId");
        PreconditionsHelper.notEmpty(tenantId, "tenantId");

        this.caseId = caseId;
        this.tenantId = tenantId;
        documents = new HashMap<>();
        errors = new ArrayList<>();
    }

    /**
     * Method that defines the way {@link DocumentValidationReport}s are aggregated at the case level
     *
     * @param documentReport
     *            the document-level report
     */
    public void swallow(DocumentValidationReport documentReport) {
        PreconditionsHelper.notNull(documentReport, "documentReport");

        EEsIndexType indexType = EEsIndexType.fromValue(documentReport.getIndex() + "_" + documentReport.getType());

        // increment the number of documents of a specific type handled
        Integer no = documents.get(indexType);
        if (no == null) {
            documents.put(indexType, 1);
        } else {
            documents.put(indexType, no + 1);
        }

        // add eventual errors in the case report
        if (documentReport.getErrors().size() > 0) {
            errors.add(documentReport);
        }
    }

    public String getCaseId() {
        return caseId;
    }

    public Map<EEsIndexType, Integer> getDocuments() {
        return documents;
    }

    public List<DocumentValidationReport> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        return "CaseValidationReport{" + "caseId='" + caseId + '\'' + ", tenantId='" + tenantId + '\'' + ", documents=" + documents
                + ", errors=" + errors.size() + '}';
    }
}

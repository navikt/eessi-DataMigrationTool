package eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;

public class CaseReport {

    private String caseId;
    private final Map<String, DocumentsReport> report;
    private List<ReportError> errors;

    public CaseReport() {
        report = new HashMap<>();
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(final String caseId) {
        this.caseId = caseId;
    }

    public Map<String, DocumentsReport> getReport() {
        return report;
    }

    public List<ReportError> getErrors() {
        return errors;
    }

    public void setErrors(final List<ReportError> errors) {
        this.errors = errors;
    }

    public void swallow(DocumentsReport documentsReport) {
        String key = getKey(documentsReport.getIndex(), documentsReport.getType());

        if (report.containsKey(key)) {
            DocumentsReport currentReport = report.get(key);
            currentReport.getProcessed().set(documentsReport.getProcessed().longValue());
        } else {
            report.put(key, documentsReport);
        }

        addErrors(documentsReport);
    }

    private void addErrors(final DocumentsReport documentsReport) {
        List<ReportError> documentReportErrors = documentsReport.getErrors();
        if (CollectionUtils.isNotEmpty(documentReportErrors)) {
            if (this.errors == null) {
                this.errors = new ArrayList<>();
            }

            this.errors.addAll(documentReportErrors);
            documentsReport.setErrors(null);
        }
    }

    public void setupReport(EElasticType eElasticType, long total) {
        DocumentsReport documentsReport = new DocumentsReport(eElasticType);
        documentsReport.getTotal().set(total);

        String key = getKey(eElasticType.getIndex(), eElasticType.getType());
        report.putIfAbsent(key, documentsReport);
    }

    @NotNull
    private String getKey(final String index, final String type) {
        return index + "_" + type;
    }
}

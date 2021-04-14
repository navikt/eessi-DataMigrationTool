package eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CasesSummaryReportError {

    private List<CaseDocumentsReport> es_data;
    private List<CaseDocumentsReport> non_es_data;

    public CasesSummaryReportError() {
        es_data = new ArrayList<>();
        non_es_data = new ArrayList<>();
    }

    public List<CaseDocumentsReport> getEs_data() {
        return es_data;
    }

    public List<CaseDocumentsReport> getNon_es_data() {
        return non_es_data;
    }

    public void swallowEsData(ReportError reportError, List<CaseDocumentsReport> data) {
        if (reportError != null && data != null) {
            Optional<CaseDocumentsReport> caseDocumentsReport = data.stream()
                    .filter(x -> reportError.getElasticIndex().equals(x.getIndex())
                            && reportError.getElasticType().equals(x.getType()))
                    .findFirst();
            if (caseDocumentsReport.isPresent()) {
                addError(reportError, caseDocumentsReport.get());
            } else {
                data.add(new CaseDocumentsReport(reportError.getElasticIndex(), reportError.getElasticType(), reportError));
            }
        }
    }

    public void swallowNonEsData(ReportError reportError, List<CaseDocumentsReport> data) {
        if (reportError != null && data != null) {
            Optional<CaseDocumentsReport> caseDocumentsReport = data.stream()
                    .filter(x -> reportError.getImporterName().equals(x.getImporterName()))
                    .findFirst();
            if (caseDocumentsReport.isPresent()) {
                addError(reportError, caseDocumentsReport.get());
            } else {
                data.add(new CaseDocumentsReport(reportError.getImporterName(), reportError));
            }
        }
    }

    private void addError(final ReportError reportError, final CaseDocumentsReport caseDocumentsReport) {
        caseDocumentsReport.getFailed().incrementAndGet();
        List<ReportError> errors = new ArrayList<>(caseDocumentsReport.getErrors());
        errors.add(reportError);
        caseDocumentsReport.setErrors(errors);
    }
}
package eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;

public class SummaryReport {

    private CasesSummaryReport casesSummaryReport;
    private List<DocumentsReport> indicesReports;

    public SummaryReport() {
        indicesReports = new ArrayList<>();
    }

    public CasesSummaryReport getCasesSummaryReport() {
        return casesSummaryReport;
    }

    public List<DocumentsReport> getIndicessReports() {
        return indicesReports;
    }

    public void setCasesSummaryReport(final CasesSummaryReport casesSummaryReport) {
        this.casesSummaryReport = casesSummaryReport;
    }

    public void swallow(List<IndexReport> indexReports) {
        if (!CollectionUtils.isEmpty(indexReports)) {
            indexReports.forEach(indexReport -> {
                Map<String, DocumentsReport> report = indexReport.getReport();
                report.forEach((k, v) -> {
                    v.setErrors(indexReport.getErrors());
                    indicesReports.add(v);
                });
            });
        }
    }

    public void swallow(SummaryReport summaryReport) {
        if (summaryReport != null) {
            casesSummaryReport = summaryReport.casesSummaryReport;
            indicesReports.addAll(summaryReport.indicesReports);
        }
    }

    public void swallow(CasesSummaryReport casesSummaryReport) {
        if (casesSummaryReport != null) {
            this.casesSummaryReport = casesSummaryReport;
        }
    }
}
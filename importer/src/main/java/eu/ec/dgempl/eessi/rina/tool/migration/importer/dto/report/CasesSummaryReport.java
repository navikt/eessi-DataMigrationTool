package eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.util.CollectionUtils;

public class CasesSummaryReport {

    private AtomicLong casesSuccess = new AtomicLong();
    private AtomicLong casesWithError = new AtomicLong();
    private CasesSummaryReportError errors;

    public CasesSummaryReport() {
        errors = new CasesSummaryReportError();
    }

    public AtomicLong getCasesSuccess() {
        return casesSuccess;
    }

    public AtomicLong getCasesWithError() {
        return casesWithError;
    }

    public CasesSummaryReportError getErrors() {
        return errors;
    }

    public void swallow(CaseReport caseReport) {
        List<ReportError> caseReportErrors = caseReport.getErrors();
        if (CollectionUtils.isEmpty(caseReportErrors)) {
            casesSuccess.incrementAndGet();
        } else {
            casesWithError.incrementAndGet();
            caseReportErrors.forEach(error -> {
                if (error.getElasticIndex() == null && error.getElasticType() == null) {
                    errors.swallowNonEsData(error, errors.getNon_es_data());
                } else {
                    errors.swallowEsData(error, errors.getEs_data());
                }
            });
        }
    }
}
package eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;

/**
 * Class that defines an import result at the index-type level
 */
public class DocumentsReport {

    @JsonIgnore
    private final EElasticType eElasticType;

    private final AtomicLong total = new AtomicLong();
    private final AtomicLong processed = new AtomicLong();
    private List<ReportError> errors;

    public DocumentsReport(final EElasticType eElasticType) {
        this.eElasticType = eElasticType;
    }

    @JsonIgnore
    public EElasticType getEElasticType() {
        return eElasticType;
    }

    public AtomicLong getProcessed() {
        return processed;
    }

    public AtomicLong getTotal() {
        return total;
    }

    public List<ReportError> getErrors() {
        return errors;
    }

    public void setErrors(final List<ReportError> errors) {
        this.errors = errors;
    }

}

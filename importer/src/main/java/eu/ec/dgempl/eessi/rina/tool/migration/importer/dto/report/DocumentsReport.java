package eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;

/**
 * Class that defines an import result at the index-type level
 */
public class DocumentsReport {

    private final String index;
    private final String type;
    private final AtomicLong total = new AtomicLong();
    private final AtomicLong processed = new AtomicLong();
    private List<ReportError> errors;

    public DocumentsReport(final EElasticType eElasticType) {
        this.index = eElasticType.getIndex();
        this.type = eElasticType.getType();
    }

    public String getIndex() {
        return index;
    }

    public String getType() {
        return type;
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

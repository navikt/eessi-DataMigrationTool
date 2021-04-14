package eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Class that defines an import result at the index-type level
 */
public class CaseDocumentsReport {

    private final String index;
    private final String type;
    private final String importerName;
    private final AtomicLong failed = new AtomicLong();
    private List<ReportError> errors;

    public CaseDocumentsReport(final String index, final String type, final ReportError error) {
        this.index = index;
        this.type = type;
        this.importerName = null;
        this.failed.set(1);
        this.errors = List.of(error);
    }

    public CaseDocumentsReport(final String importerName, final ReportError error) {
        this.index = null;
        this.type = null;
        this.importerName = importerName;
        this.failed.set(1);
        this.errors = List.of(error);
    }

    public String getIndex() {
        return index;
    }

    public String getType() {
        return type;
    }

    public String getImporterName() {
        return importerName;
    }

    public AtomicLong getFailed() {
        return failed;
    }

    public List<ReportError> getErrors() {
        return errors;
    }

    public void setErrors(final List<ReportError> errors) {
        this.errors = errors;
    }
}

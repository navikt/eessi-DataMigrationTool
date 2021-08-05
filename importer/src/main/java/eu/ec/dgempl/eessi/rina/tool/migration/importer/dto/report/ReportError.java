package eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report;

public class ReportError {

    private final String elasticIndex;
    private final String elasticType;
    private final String elasticId;
    private final String importerName;
    private final String stacktrace;

    public ReportError(
            final String elasticIndex,
            final String elasticType,
            final String elasticId,
            final String importerName,
            final String stacktrace) {
        this.elasticIndex = elasticIndex;
        this.elasticType = elasticType;
        this.elasticId = elasticId;
        this.importerName = importerName;
        this.stacktrace = stacktrace;
    }

    public String getElasticIndex() {
        return elasticIndex;
    }

    public String getElasticType() {
        return elasticType;
    }

    public String getElasticId() {
        return elasticId;
    }

    public String getImporterName() {
        return importerName;
    }

    public String getStacktrace() {
        return stacktrace;
    }
}

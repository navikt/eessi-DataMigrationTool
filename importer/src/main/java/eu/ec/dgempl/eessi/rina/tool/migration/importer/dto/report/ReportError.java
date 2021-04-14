package eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report;

public class ReportError {

    private String elasticIndex;
    private String elasticType;
    private String elasticId;
    private String importerName;
    private String stacktrace;

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

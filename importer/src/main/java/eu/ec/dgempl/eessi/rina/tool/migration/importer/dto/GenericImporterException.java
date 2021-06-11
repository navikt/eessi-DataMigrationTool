package eu.ec.dgempl.eessi.rina.tool.migration.importer.dto;

import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;

public class GenericImporterException extends RuntimeException {

    private EElasticType eElasticType;
    private String elasticId;
    private DocumentsReport documentsReport;

    public GenericImporterException(final EElasticType eElasticType, final String elasticId) {
        this.eElasticType = eElasticType;
        this.elasticId = elasticId;
    }

    public GenericImporterException(final String message, final EElasticType eElasticType, final String elasticId) {
        super(message);
        this.eElasticType = eElasticType;
        this.elasticId = elasticId;
    }

    public GenericImporterException(final String message, final Throwable cause,
            final EElasticType eElasticType, final String elasticId) {
        super(message, cause);
        this.eElasticType = eElasticType;
        this.elasticId = elasticId;
    }

    public GenericImporterException(final Throwable cause, final EElasticType eElasticType, final String elasticId) {
        super(cause);
        this.eElasticType = eElasticType;
        this.elasticId = elasticId;
    }

    public GenericImporterException(final Throwable cause, final EElasticType eElasticType, final String elasticId,
            final DocumentsReport documentsReport) {
        super(cause);
        this.eElasticType = eElasticType;
        this.elasticId = elasticId;
        this.documentsReport = documentsReport;
    }

    public GenericImporterException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace,
            final EElasticType eElasticType, final String elasticId) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.eElasticType = eElasticType;
        this.elasticId = elasticId;
    }

    public EElasticType getEElasticType() {
        return eElasticType;
    }

    public String getElasticId() {
        return elasticId;
    }

    public DocumentsReport getDocumentsReport() {
        return documentsReport;
    }
}

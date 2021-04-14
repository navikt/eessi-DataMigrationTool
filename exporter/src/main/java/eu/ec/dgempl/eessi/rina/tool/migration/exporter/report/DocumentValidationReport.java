package eu.ec.dgempl.eessi.rina.tool.migration.exporter.report;

import java.util.List;

import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.EsDocument;

/**
 * Class that defines the validation result at the document level
 */
public class DocumentValidationReport {
    private final String index;
    private final String type;
    private final String objectId;
    private final boolean isValid;
    private final List<ValidationResult> errors;

    /**
     * Private constructor
     * 
     * @param index
     *            the elasticsearch index
     * @param type
     *            the elasticsearch type
     * @param objectId
     *            the elasticsearch document id
     * @param isValid
     *            flag that indicates whether the elasticsearch document is considered valid
     * @param errors
     *            list of errors found when validating the elasticsearch document
     */
    private DocumentValidationReport(String index, String type, String objectId, boolean isValid, List<ValidationResult> errors) {
        PreconditionsHelper.notEmpty(index, "index");
        PreconditionsHelper.notEmpty(type, "type");
        PreconditionsHelper.notEmpty(objectId, "objectId");
        PreconditionsHelper.notNull(errors, "errors");

        this.index = index;
        this.type = type;
        this.objectId = objectId;
        this.isValid = isValid;
        this.errors = errors;
    }

    /**
     * Creates a report for the {@code document} containing the list of {@code errors}
     * 
     * @param document
     * @param errors
     * @return
     */
    public static DocumentValidationReport forDocument(EsDocument document, List<ValidationResult> errors) {
        PreconditionsHelper.notNull(document, "document");
        PreconditionsHelper.notNull(errors, "errors");

        boolean isValid = errors.size() == 0;

        return new DocumentValidationReport(document.getIndex(), document.getType(), document.getObjectId(), isValid, errors);
    }

    public String getIndex() {
        return index;
    }

    public String getType() {
        return type;
    }

    public String getObjectId() {
        return objectId;
    }

    public boolean isValid() {
        return isValid;
    }

    public List<ValidationResult> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        return "DocumentValidationReport{" + "index='" + index + '\'' + ", type='" + type + '\'' + ", objectId='" + objectId + '\''
                + ", isValid=" + isValid + ", errors=" + errors.size() + '}';
    }
}

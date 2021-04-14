package eu.ec.dgempl.eessi.rina.tool.migration.exporter.report;

import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.EValidationResult;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.GsonWrapper;

/**
 * Class that defines the validation result at the field level
 */
public class ValidationResult {
    private final String field;
    private final Object value;
    private final EValidationResult result;
    private final Object details;

    /**
     * Private constructor
     * 
     * @param field
     *            the json path of the validated object
     * @param value
     *            the object being validated
     * @param result
     *            the result to be wrapped in the validation result
     * @param details
     *            object containing details on the validation result
     */
    private ValidationResult(String field, Object value, EValidationResult result, Object details) {
        PreconditionsHelper.notEmpty(field, "field");
        PreconditionsHelper.notNull(result, "result");

        this.field = field;
        this.value = value;
        this.result = result;
        this.details = details;
    }

    /**
     * Defines a valid result with no additional details
     *
     * @param field
     *            the json path of the validated object
     * @param value
     *            the object being validated
     * @return the validation result
     */
    public static ValidationResult ok(String field, Object value) {
        PreconditionsHelper.notEmpty(field, "field");

        return new ValidationResult(field, value, EValidationResult.VALID, null);
    }

    /**
     * Defines a valid result with additional details
     *
     * @param field
     *            the json path of the validated object
     * @param value
     *            the object being validated
     * @param details
     *            object containing details on the validation result
     * @return the validation result
     */
    public static ValidationResult ok(String field, Object value, Object details) {
        PreconditionsHelper.notEmpty(field, "field");

        return new ValidationResult(field, value, EValidationResult.VALID, details);
    }

    /**
     * Defines a non valid result with no additional details
     *
     * @param field
     *            the json path of the validated object
     * @param value
     *            the object being validated
     * @param result
     *            the result to be wrapped in the validation result
     * @return the validation result
     */
    public static ValidationResult error(String field, Object value, EValidationResult result) {
        PreconditionsHelper.notEmpty(field, "field");
        PreconditionsHelper.notNull(result, "result");

        return new ValidationResult(field, value, result, null);
    }

    /**
     * Defines a non valid result with additional details
     *
     * @param field
     *            the json path of the validated object
     * @param value
     *            the object being validated
     * @param result
     *            the result to be wrapped in the validation result
     * @param details
     *            object containing details on the validation result
     * @return the validation result
     */
    public static ValidationResult error(String field, Object value, EValidationResult result, Object details) {
        PreconditionsHelper.notEmpty(field, "field");
        PreconditionsHelper.notNull(result, "result");

        return new ValidationResult(field, value, result, GsonWrapper.stringify(details));
    }

    public EValidationResult getResult() {
        return result;
    }

    public String getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }

    public Object getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "ValidationResult{" + "field='" + field + '\'' + ", value=" + value + ", result=" + result + ", details=" + details + '}';
    }
}
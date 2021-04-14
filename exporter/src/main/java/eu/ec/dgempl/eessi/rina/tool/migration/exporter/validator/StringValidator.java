package eu.ec.dgempl.eessi.rina.tool.migration.exporter.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.EValidationResult;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.ValidationContext;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.report.ValidationResult;

/**
 * Validates if object can be represented as string
 */
public class StringValidator extends AbstractValidator {

    public StringValidator(String... params) {
        super(params);
    }

    @Override
    List<ValidationResult> internalValidate(String path, Object obj, ValidationContext context) {
        PreconditionsHelper.notEmpty(path, "path");
        PreconditionsHelper.notNull(context, "context");

        List<ValidationResult> results = new ArrayList<>();

        // check if valid string
        results.addAll(validateString(path, obj));

        // check the length constraint
        if (params.length == 1) {
            int maxLength = Integer.parseInt(params[0]);
            results.addAll(validateLength(path, obj, maxLength));
        }

        return results;
    }

    @Override
    boolean checkParams(String... params) {
        if (params.length <= 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Method that checks if {@code obj} can be represented as string. {@code null} is considered a valid string. Numeric types can be
     * converted to strings and are accepted.
     *
     * @param path
     *            the json path referencing the {@code obj}
     * @param obj
     *            the object that is evaluated
     * @return the list of {@link ValidationResult}s
     */
    private List<ValidationResult> validateString(String path, Object obj) {
        PreconditionsHelper.notEmpty(path, "path");

        List<ValidationResult> results = new ArrayList<>();

        if (obj == null) {
            results.add(ValidationResult.ok(path, obj));
        } else if (obj instanceof String) {
            results.add(ValidationResult.ok(path, obj));
        } else if (obj instanceof Number) {
            results.add(ValidationResult.ok(path, obj));
        } else if (obj instanceof Boolean) {
            results.add(ValidationResult.ok(path, obj));
        } else {
            results.add(ValidationResult.error(path, obj, EValidationResult.INVALID_STRING));
        }

        return results;
    }

    /**
     * Method that checks if {@code obj} can be represented as string with a maximum length. {@code null} is considered a valid string.
     * Numeric types can be converted to strings and are accepted.
     *
     * @param path
     *            the json path referencing the {@code obj}
     * @param obj
     *            the object that is evaluated
     * @param length
     *            the maximum string length
     * @return the list of {@link ValidationResult}s
     */
    private List<ValidationResult> validateLength(String path, Object obj, int length) {
        PreconditionsHelper.notEmpty(path, "path");

        List<ValidationResult> results = new ArrayList<>();

        if (obj == null) {
            results.add(ValidationResult.ok(path, obj));
            return results;
        }

        String s = null;

        if (obj instanceof String) {
            s = (String) obj;
        } else if (obj instanceof Number) {
            s = obj.toString();
        }

        if (s != null && s.length() > length) {
            Map<String, Integer> details = new HashMap<>();
            details.put("expected", length);
            details.put("actual", s.length());
            results.add(ValidationResult.error(path, obj, EValidationResult.INVALID_LENGTH, details));
        }

        return results;
    }
}

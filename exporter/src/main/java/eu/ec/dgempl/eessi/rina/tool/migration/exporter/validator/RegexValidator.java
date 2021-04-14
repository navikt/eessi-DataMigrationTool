package eu.ec.dgempl.eessi.rina.tool.migration.exporter.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.EValidationResult;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.ValidationContext;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.report.ValidationResult;

/**
 * Validates if object has a specific format
 */
public class RegexValidator extends AbstractValidator {
    private Pattern pattern;

    public RegexValidator(String... params) {
        super(params);
        pattern = Pattern.compile(params[0]);
    }

    @Override
    List<ValidationResult> internalValidate(String path, Object obj, ValidationContext context) {
        PreconditionsHelper.notEmpty(path, "path");
        PreconditionsHelper.notNull(context, "context");

        List<ValidationResult> results = new ArrayList<>();

        if (obj == null) {
            results.add(ValidationResult.ok(path, obj));
        } else if (obj instanceof String) {
            if (pattern.matcher((String) obj).matches()) {
                results.add(ValidationResult.ok(path, obj));
            } else {
                String details = String.format("String does not match regex [regex=%s]", params[0]);
                results.add(ValidationResult.error(path, obj, EValidationResult.INVALID_STRING, details));
            }
        } else {
            results.add(ValidationResult.error(path, obj, EValidationResult.INVALID_STRING));
        }

        return results;
    }

    @Override
    boolean checkParams(String... params) {
        if (params.length == 1) {
            return true;
        } else {
            return false;
        }
    }
}

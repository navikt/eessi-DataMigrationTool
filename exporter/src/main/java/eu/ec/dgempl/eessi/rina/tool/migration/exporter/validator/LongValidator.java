package eu.ec.dgempl.eessi.rina.tool.migration.exporter.validator;

import java.util.ArrayList;
import java.util.List;

import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.EValidationResult;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.ValidationContext;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.report.ValidationResult;

/**
 * Validates if object can be represented as long
 */
public class LongValidator extends AbstractValidator {

    @Override
    List<ValidationResult> internalValidate(String path, Object obj, ValidationContext context) {
        PreconditionsHelper.notEmpty(path, "path");
        PreconditionsHelper.notNull(context, "context");

        List<ValidationResult> results = new ArrayList<>();

        if (obj == null) {
            results.add(ValidationResult.ok(path, obj));
        } else if (obj instanceof Number) {
            if (obj instanceof Byte || obj instanceof Short || obj instanceof Integer || obj instanceof Long) {
                results.add(ValidationResult.ok(path, obj));
            } else {
                String details = "Object is of numeric type, but larger than long.";
                results.add(ValidationResult.error(path, obj, EValidationResult.INVALID_LONG, details));
            }
        } else if (obj instanceof String) {
            try {
                Long.parseLong((String) obj);
                results.add(ValidationResult.ok(path, obj));
            } catch (NumberFormatException nfe) {
                String details = "Object is of type String, and cannot be converted to long";
                results.add(ValidationResult.error(path, obj, EValidationResult.INVALID_LONG, details));
            }
        } else {
            results.add(ValidationResult.error(path, obj, EValidationResult.INVALID_LONG));
        }

        return results;
    }
}

package eu.ec.dgempl.eessi.rina.tool.migration.exporter.validator;

import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import eu.ec.dgempl.eessi.rina.tool.migration.common.util.DateResolver;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.EValidationResult;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.ValidationContext;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.report.ValidationResult;

/**
 * Validates if object can be represented as date
 */
public class DateValidator extends AbstractValidator {

    @Override
    List<ValidationResult> internalValidate(String path, Object obj, ValidationContext context) {
        PreconditionsHelper.notEmpty(path, "path");
        PreconditionsHelper.notNull(context, "context");

        List<ValidationResult> results = new ArrayList<>();

        if (obj == null) {
            results.add(ValidationResult.ok(path, obj));
        } else if (obj instanceof String) {
            try {
                DateResolver.parse((String) obj);
            } catch (DateTimeParseException dtpe) {
                String details = "Object is of type String, and cannot be converted to date";
                results.add(ValidationResult.error(path, obj, EValidationResult.INVALID_DATE, details));
            }
            results.add(ValidationResult.ok(path, obj));
            return results;
        } else {
            results.add(ValidationResult.error(path, obj, EValidationResult.INVALID_DATE));
        }

        return results;
    }
}

package eu.ec.dgempl.eessi.rina.tool.migration.exporter.validator;

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
            } catch (Exception e) {
                String details = e.getMessage();
                results.add(ValidationResult.error(path, obj, EValidationResult.INVALID_DATE, details));
            }
            results.add(ValidationResult.ok(path, obj));
            return results;
        } else if (obj instanceof Number) {
            try {
                DateResolver.parse(String.valueOf(obj));
            } catch (Exception e) {
                String details = e.getMessage();
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

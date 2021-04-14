package eu.ec.dgempl.eessi.rina.tool.migration.exporter.validator;

import java.util.ArrayList;
import java.util.List;

import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.EValidationResult;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.ValidationContext;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.report.ValidationResult;

/**
 * Validates if object is not null
 */
public class NotNullValidator extends AbstractValidator {

    @Override
    List<ValidationResult> internalValidate(String path, Object obj, ValidationContext context) {
        PreconditionsHelper.notEmpty(path, "path");
        PreconditionsHelper.notNull(context, "context");

        List<ValidationResult> results = new ArrayList<>();

        if (obj != null) {
            results.add(ValidationResult.ok(path, obj));
        } else {
            results.add(ValidationResult.error(path, obj, EValidationResult.INVALID_NULL));
        }

        return results;
    }
}

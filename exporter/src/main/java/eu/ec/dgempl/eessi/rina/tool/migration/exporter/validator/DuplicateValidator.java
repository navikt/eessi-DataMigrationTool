package eu.ec.dgempl.eessi.rina.tool.migration.exporter.validator;

import java.util.ArrayList;
import java.util.List;

import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.EValidationResult;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.ValidationContext;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.report.ValidationResult;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.util.JsonPathHelper;

/**
 * Validates if object is a duplicate of another field
 */
public class DuplicateValidator extends AbstractValidator {

    public DuplicateValidator(String... params) {
        super(params);
    }

    @Override
    List<ValidationResult> internalValidate(String path, Object obj, ValidationContext context) {
        PreconditionsHelper.notEmpty(path, "path");
        PreconditionsHelper.notNull(context, "context");

        List<ValidationResult> results = new ArrayList<>();

        Object found = JsonPathHelper.getObjectInMapByPath(context.getDocument().getObject(), params[0]);

        if (obj == null) {
            if (found == null) {
                results.add(ValidationResult.ok(path, obj));
            } else {
                String details = String.format("The values of the two fields are not identical [path1=%s, path2=%s]", path, params[0]);
                results.add(ValidationResult.error(path, obj, EValidationResult.INVALID_DUPLICATE, details));
            }
        } else {
            if (obj.equals(found)) {
                results.add(ValidationResult.ok(path, obj));
            } else {
                String details = String.format("The values of the two fields are not identical [path1=%s, path2=%s]", path, params[0]);
                results.add(ValidationResult.error(path, obj, EValidationResult.INVALID_DUPLICATE, details));
            }
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

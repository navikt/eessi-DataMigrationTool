package eu.ec.dgempl.eessi.rina.tool.migration.exporter.validator;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.EValidationResult;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.ValidationContext;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.report.ValidationResult;

/**
 * Validates if object can be represented as date
 */
public class DateValidator extends AbstractValidator {

    // @formatter:off
    private static final DateTimeFormatter[] SUPPORTED_DATE_FORMATS = new DateTimeFormatter[] {
            DateTimeFormatter.ISO_ZONED_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    };
    // @formatter:on

    @Override
    List<ValidationResult> internalValidate(String path, Object obj, ValidationContext context) {
        PreconditionsHelper.notEmpty(path, "path");
        PreconditionsHelper.notNull(context, "context");

        List<ValidationResult> results = new ArrayList<>();

        if (obj == null) {
            results.add(ValidationResult.ok(path, obj));
        } else if (obj instanceof String) {

            // try to parse it with any of the supported formats
            for (DateTimeFormatter format : SUPPORTED_DATE_FORMATS) {
                try {
                    ZonedDateTime.parse((String) obj, format);
                    results.add(ValidationResult.ok(path, obj));
                    return results;
                } catch (DateTimeParseException dtpe) {
                    // ignore and try another format
                }
            }

            // if all of them fail, report error
            String details = "Object is of type String, and cannot be converted to date";
            results.add(ValidationResult.error(path, obj, EValidationResult.INVALID_DATE, details));
        } else {
            results.add(ValidationResult.error(path, obj, EValidationResult.INVALID_DATE));
        }

        return results;
    }
}

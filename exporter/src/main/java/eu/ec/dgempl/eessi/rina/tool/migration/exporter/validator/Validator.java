package eu.ec.dgempl.eessi.rina.tool.migration.exporter.validator;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.lang.Nullable;

import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.ValidationContext;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.report.ValidationResult;

/**
 * Interface for validator objects. Validators check if objects can be converted to different data types
 */
public interface Validator {

    /**
     * Method for validating objects
     * 
     * @param path
     *            the json path referencing the {@code obj}
     * @param obj
     *            the object that is validated
     * @param context
     *            the validation context
     * @return the list of the validation results
     */
    List<ValidationResult> validate(@NotBlank String path, @Nullable Object obj, @NotNull ValidationContext context);

}

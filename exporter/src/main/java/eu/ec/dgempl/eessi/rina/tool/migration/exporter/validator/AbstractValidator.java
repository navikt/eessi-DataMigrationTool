package eu.ec.dgempl.eessi.rina.tool.migration.exporter.validator;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.lang.Nullable;

import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.EValidationResult;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.ValidationContext;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.report.ValidationResult;

/**
 * Abstract class that implements {@link Validator}. Its purpose is to provide a default implementation of
 * {@link Validator#validate(String, Object, ValidationContext)}
 */
public abstract class AbstractValidator implements Validator {
    String[] params;

    public AbstractValidator(String... params) {
        // check params
        if (!checkParams(params)) {
            throw new IllegalArgumentException(String.format("Invalid validator parameters [validator=%s, params=%s]",
                    this.getClass().getSimpleName(), String.join(",", params)));
        }

        this.params = params;
    }

    /**
     * Implementation of {@link Validator#validate(String, Object, ValidationContext)}. The implementation follows these steps: check the
     * params injected in the Validators, call the internal validation method, and then post-processes the results
     *
     * @param path
     *            the json path referencing the {@code obj}
     * @param obj
     *            the object that is validated
     * @param context
     *            the validation context
     * @return the list of validation results
     */
    @Override
    public List<ValidationResult> validate(String path, Object obj, ValidationContext context) {
        PreconditionsHelper.notEmpty(path, "path");
        PreconditionsHelper.notNull(context, "context");

        List<ValidationResult> results = internalValidate(path, obj, context);

        // post-process the results
        if (results.size() == 0) {
            throw new IllegalStateException("Invalid validation result");
        }

        // @formatter:off
        List<ValidationResult> errors = results.stream()
                .filter(v -> !v.getResult().equals(EValidationResult.VALID))
                .collect(Collectors.toList());
        // @formatter:on

        if (errors.size() > 0) {
            return errors;
        } else {
            // if there is no error, remove eventual EResultTypes.VALID duplicates caused by multiple checks inside the Validator
            // (e.g. type is ok, length is ok)
            return results.stream().limit(1).collect(Collectors.toList());
        }
    }

    /**
     * Abstract validation method. This must be overridden by implementing classes
     *
     * @param path
     *            the json path referencing the {@code obj}
     * @param obj
     *            the object that is validated
     * @param context
     *            the validation context
     * @return the list of validation results
     */
    abstract List<ValidationResult> internalValidate(@NotBlank String path, @Nullable Object obj, @NotNull ValidationContext context);

    /**
     * Method for validating the params injected in the validator. The method is not made abstract; by default params are considered valid.
     * Implementers of this class may override this method
     *
     * @param params
     *            the injected parameters
     * @return boolean value indicating whether the params are valid in the context of the validator
     */
    boolean checkParams(String... params) {
        return true;
    }
}

package eu.ec.dgempl.eessi.rina.tool.migration.exporter.validator;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import eu.ec.dgempl.eessi.rina.tool.migration.common.model.EEsIndex;
import eu.ec.dgempl.eessi.rina.tool.migration.common.model.EEsType;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.EValidationResult;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.ValidationContext;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.report.ValidationResult;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.service.EnumService;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.util.JsonPathHelper;

/**
 * Validates if object is a valid enum value
 */
public class EnumValidator extends AbstractValidator {
    private static final Logger logger = LoggerFactory.getLogger(EnumValidator.class);

    private final EnumService enumService;

    public EnumValidator(EnumService enumService, String... params) {
        super(params);
        this.enumService = enumService;
    }

    @Override
    List<ValidationResult> internalValidate(String path, Object obj, ValidationContext context) {
        PreconditionsHelper.notEmpty(path, "path");
        PreconditionsHelper.notNull(context, "context");

        List<ValidationResult> results = new ArrayList<>();

        if (obj == null) {
            results.add(ValidationResult.ok(path, obj));
        } else if (obj instanceof String) {
            if (StringUtils.isEmpty(obj)) {
                results.add(ValidationResult.ok(path, obj));
            } else {
                // add exception for assignment policy
                // rules.actor can also be 'Creator'
                boolean isIndexConfigurations = context.getDocument().getIndex().equalsIgnoreCase(EEsIndex.CONFIGURATIONS.value());
                boolean isTypeApplicationProfile = context.getDocument().getType().equalsIgnoreCase(EEsType.ASSIGNMENTPOLICY.value());
                boolean isPathRulesActors = JsonPathHelper.normalisePath(path).equals("rules.actors");
                boolean isValueCreator = ((String) obj).equalsIgnoreCase("creator");

                if (isIndexConfigurations && isTypeApplicationProfile && isPathRulesActors && isValueCreator) {
                    results.add(ValidationResult.ok(path, obj));
                    return results;
                }

                try {
                    boolean found = enumService.exists(params[0], (String) obj);
                    if (found) {
                        results.add(ValidationResult.ok(path, obj));
                    } else {
                        String acceptedValues = enumService.getAcceptedValues(params[0]);
                        String details = "Expected value from enum " + params[0] + ". Accepted values in format 'name:values' are: "
                                + acceptedValues;
                        results.add(ValidationResult.error(path, obj, EValidationResult.INVALID_ENUM, details));
                    }
                } catch (IllegalAccessException | NoClassDefFoundError e) {
                    // if there was any problem loading the enum, the validation returns ok
                    logger.info("Enum not found [enum={}]", params[0]);
                    results.add(ValidationResult.ok(path, obj));
                }
            }
        } else {
            try {
                String acceptedValues = enumService.getAcceptedValues(params[0]);
                String details = "Value is not of type string" + ". Accepted values in format 'name:values' are: " + acceptedValues;
                results.add(ValidationResult.error(path, obj, EValidationResult.INVALID_ENUM, details));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
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

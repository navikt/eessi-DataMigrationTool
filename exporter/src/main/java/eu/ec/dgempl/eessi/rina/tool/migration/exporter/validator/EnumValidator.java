package eu.ec.dgempl.eessi.rina.tool.migration.exporter.validator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.util.ContentNavigator;
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
                boolean exceptionIndex = context.getDocument().getIndex().equalsIgnoreCase(EEsIndex.CONFIGURATIONS.value());
                boolean exceptionType = context.getDocument().getType().equalsIgnoreCase(EEsType.ASSIGNMENTPOLICY.value());
                boolean exceptionPath = JsonPathHelper.normalisePath(path).equals("rules.actors");
                boolean exceptionValue = ((String) obj).equalsIgnoreCase("creator");
                if (exceptionIndex && exceptionType && exceptionPath && exceptionValue) {
                    results.add(ValidationResult.ok(path, obj));
                    return results;
                }
                // add exception for cases_casestructuredmetadata
                // caseAssignment.actors.name can also be 'System'
                exceptionIndex = context.getDocument().getIndex().equalsIgnoreCase(EEsIndex.CASES.value());
                exceptionType = context.getDocument().getType().equalsIgnoreCase(EEsType.CASESTRUCTUREDMETADATA.value());
                exceptionPath = JsonPathHelper.normalisePath(path).equals("caseAssignment.actors.name");
                exceptionValue = ((String) obj).equalsIgnoreCase("System");
                if (exceptionIndex && exceptionType && exceptionPath && exceptionValue) {
                    results.add(ValidationResult.ok(path, obj));
                    return results;
                }
                // add exception for cases_document
                // conversations.participants.role = 'CounterParty' and conversations.participants.organisation.id AT:*
                exceptionIndex = context.getDocument().getIndex().equalsIgnoreCase(EEsIndex.CASES.value());
                exceptionType = context.getDocument().getType().equalsIgnoreCase(EEsType.DOCUMENT.value());
                exceptionPath = JsonPathHelper.normalisePath(path).equals("conversations.participants.role");
                exceptionValue = ((String) obj).equalsIgnoreCase("CounterParty");
                if (exceptionIndex && exceptionType && exceptionPath && exceptionValue) {
                    Object orgId = getOrganisationId(path, context.getDocument().getObject());
                    if (orgId instanceof String) {
                        String organisationId = (String) orgId;
                        if (organisationId.startsWith("AT:")) {
                            logger.info(String.format(
                                    "Encountered conversations.participants.role with value CounterParty and country code AT in document with id %s. Skipping this validation. This value will be replaced by Receiver",
                                    context.getDocument().getObjectId()));
                            results.add(ValidationResult.ok(path, obj));
                            return results;
                        }
                    }
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

    private Object getOrganisationId(final String path, final Map<String, Object> map) {
        String orgIdPath = path.replace("role", "organisation.id");
        LinkedHashMap<String, Integer> constructedPath = ContentNavigator.constructNavigationPath(orgIdPath);
        return ContentNavigator.getFieldWithArrays(map, constructedPath);
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

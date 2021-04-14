package eu.ec.dgempl.eessi.rina.tool.migration.exporter.validator;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ec.dgempl.eessi.rina.tool.migration.common.model.EEsIndex;
import eu.ec.dgempl.eessi.rina.tool.migration.common.model.EEsType;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.EValidationResult;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.ValidationContext;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.report.ValidationResult;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.util.ContentNavigator;

/**
 * Validates if object is not null
 */
public class NotNullValidator extends AbstractValidator {

    private static final Logger logger = LoggerFactory.getLogger(NotNullValidator.class);

    @Override
    List<ValidationResult> internalValidate(String path, Object obj, ValidationContext context) {
        PreconditionsHelper.notEmpty(path, "path");
        PreconditionsHelper.notNull(context, "context");

        List<ValidationResult> results = new ArrayList<>();

        // add a special handler for audit_auditentry documents with message.action null
        // try to get the value from message.source.eventType
        if ("message.action".equals(path) && obj == null) {
            if (context.getDocument() != null && context.getDocument().getObject() != null
                    && ContentNavigator.getField(context.getDocument().getObject(), "message", "source", "eventType") != null) {
                logger.info("AuditEntry message action is null. This value will be replaced with message.source.eventType");
                results.add(ValidationResult.ok(path, obj));
                return results;
            }
        }

        // add a special handler for notifications_alarm documents with creationDate null
        // set value to default date 1/1/1970
        if ("creationDate".equals(path) && obj == null){
            if(context.getDocument() != null && EEsIndex.NOTIFICATIONS.value().equals(context.getDocument().getIndex()) && EEsType.ALARM.value().equals(context.getDocument().getType())){
                logger.info(String.format("Notifications alarm creation date is null in document with id %s. This value will be replaced with default date 1/1/1970", context.getDocument().getObjectId()));
                results.add(ValidationResult.ok(path, obj));
                return results;
            }
        }

        // add a special handler for identity_user documents with firstName, lastName or salt null
        // set default values
        if (obj == null && ("firstName".equals(path) || "lastName".equals(path) || "salt".equals(path))){
            if(context.getDocument() != null && EEsIndex.IDENTITY.value().equals(context.getDocument().getIndex()) && EEsType.USER.value().equals(context.getDocument().getType())){
                logger.info(String.format("Identity user %s is null in document with id %s. This value will be replaced with default value", path, context.getDocument().getObjectId()));
                results.add(ValidationResult.ok(path, obj));
                return results;
            }
        }

        if (obj != null) {
            results.add(ValidationResult.ok(path, obj));
        } else {
            results.add(ValidationResult.error(path, obj, EValidationResult.INVALID_NULL));
        }

        return results;
    }
}

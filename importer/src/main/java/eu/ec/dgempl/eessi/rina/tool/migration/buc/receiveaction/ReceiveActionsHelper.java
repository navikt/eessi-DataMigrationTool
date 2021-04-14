package eu.ec.dgempl.eessi.rina.tool.migration.buc.receiveaction;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import eu.ec.dgempl.eessi.rina.buc.api.model.ActionDO;
import eu.ec.dgempl.eessi.rina.buc.core.model.CreateActionTrigger;
import eu.ec.dgempl.eessi.rina.buc.core.model.Document;
import eu.ec.dgempl.eessi.rina.buc.core.model.EActionStatus;
import eu.ec.dgempl.eessi.rina.buc.core.model.EActionType;
import eu.ec.dgempl.eessi.rina.buc.core.model.EDocumentType;
import eu.ec.dgempl.eessi.rina.buc.core.model.Trigger;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EApplicationRole;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ECountryCode;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.CaseParticipant;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;

public class ReceiveActionsHelper {

    /**
     * Returns the country code for the case owner participant
     *
     * @param participants
     * @return
     */
    public static ECountryCode getCaseOwnerCountryCode(final List<CaseParticipant> participants) {

        PreconditionsHelper.notNull(participants, "rinaCase");

        // @formatter:off
        CaseParticipant owner = participants.stream()
                                        .filter(p -> EApplicationRole.PO.equals(p.getApplicationRole()))
                                        .findFirst()
                                        .orElse(null);
        // @formatter:on

        if (owner == null) {
            throw new RuntimeException("No case owner participant found on case");
        }

        return owner.getOrganisation().getCountryCode();

    }

    /**
     * Returns a new {@link ActionDO} object for the given {@code trigger},
     * which would have been created
     *
     * @param caseId
     * @param trigger
     * @return
     */
    public static ActionDO createReceiveActionForTrigger(String caseId, final CreateActionTrigger trigger) {

        PreconditionsHelper.notNull(caseId, "caseId");
        PreconditionsHelper.notNull(trigger, "trigger");

        ActionDO actionDO = new ActionDO();
        actionDO.setId(UUID.randomUUID().toString());
        actionDO.setStatus(EActionStatus.ACTIVE);
        actionDO.setType(EActionType.DOC_RECEIVE);
        actionDO.setCanClose(false);
        actionDO.setCaseId(caseId);
        actionDO.setDocumentType(trigger.getDocumentType());

        return actionDO;
    }

    /**
     * Creates a new action object for the given {@code caseId} and
     * {@code documentType}.
     *
     * @param caseId
     * @param documentType
     * @return
     */
    public static ActionDO createDefaultReceiveAction(String caseId, EDocumentType documentType) {

        PreconditionsHelper.notEmpty(caseId, "caseId");
        PreconditionsHelper.notNull(documentType, "documentType");

        ActionDO actionDO = new ActionDO();
        actionDO.setId(UUID.randomUUID().toString());
        actionDO.setStatus(EActionStatus.ACTIVE);
        actionDO.setType(EActionType.DOC_RECEIVE);
        actionDO.setCanClose(false);
        actionDO.setCaseId(caseId);
        actionDO.setDocumentType(documentType);

        return actionDO;
    }

    /**
     * Creates a new DOC_UPDATE_RECEIVE action object for the given
     * {@code caseId} and {@code documentType}.
     *
     * @param caseId
     * @param documentType
     * @return
     */
    public static ActionDO createDefaultReceiveUpdateAction(String caseId, EDocumentType documentType) {

        PreconditionsHelper.notEmpty(caseId, "caseId");
        PreconditionsHelper.notNull(documentType, "documentType");

        ActionDO actionDO = new ActionDO();
        actionDO.setId(UUID.randomUUID().toString());
        actionDO.setStatus(EActionStatus.ACTIVE);
        actionDO.setType(EActionType.DOC_UPDATE_RECEIVE);
        actionDO.setCanClose(false);
        actionDO.setCaseId(caseId);
        actionDO.setDocumentType(documentType);

        return actionDO;
    }

    /**
     * Returns {@code true} if the trigger is a {@link CreateActionTrigger},
     * {@code false} otherwise.
     *
     * @param trigger
     * @return
     */
    public static boolean isCreateActionTrigger(final Trigger trigger) {

        if (trigger != null) {

            return CreateActionTrigger.class.isAssignableFrom(trigger.getClass());

        }

        return false;
    }

    /**
     * Returns a list of {@link CreateActionTrigger}s for the given {@code doc},
     * which are creating a {@link EActionType#DOC_RECEIVE} tasks.
     *
     * @param doc
     * @return
     */
    public static List<CreateActionTrigger> getCreateReceiveActionTriggersForDocument(final Document doc) {

        PreconditionsHelper.notNull(doc, "doc");

        if (doc.getTriggers() == null) {
            return Collections.emptyList();
        }

        // @formatter:off
        return doc.getTriggers().getTrigger().stream()
                    .filter(t -> isCreateReceiveActionTrigger(t))
                    .map(CreateActionTrigger.class::cast)
                    .collect(Collectors.toList());
        // @formatter:on

    }

    /**
     * Returns a list of {@link CreateActionTrigger}s for the given {@code doc},
     * onAction {@link EActionType#DOC_RECEIVE} which are creating a
     * {@link EActionType#DOC_RECEIVE} tasks.
     *
     * @param doc
     * @return
     */
    public static List<CreateActionTrigger> getCreateOnReceiveReceiveActionTriggersForDocument(final Document doc) {

        PreconditionsHelper.notNull(doc, "doc");

        if (doc.getTriggers() == null) {
            return Collections.emptyList();
        }

        // @formatter:off
        return doc.getTriggers().getTrigger().stream()
                    .filter(t -> isCreateOnReceiveReceiveActionTrigger(t))
                    .map(CreateActionTrigger.class::cast)
                    .collect(Collectors.toList());
        // @formatter:on

    }

    /**
     * Returns a list of {@link CreateActionTrigger}s for the given {@code doc},
     * onAction {@link EActionType#DOC_SEND} and
     * {@link EActionType#DOC_SEND_PARTICIPANTS} which are creating a
     * {@link EActionType#DOC_RECEIVE} tasks.
     *
     * @param doc
     * @return
     */
    public static List<CreateActionTrigger> getCreateOnSendReceiveActionTriggersForDocument(final Document doc) {

        PreconditionsHelper.notNull(doc, "doc");

        if (doc.getTriggers() == null) {
            return Collections.emptyList();
        }

        // @formatter:off
        return doc.getTriggers().getTrigger().stream()
                    .filter(t -> isCreateOnSendReceiveActionTrigger(t))
                    .map(CreateActionTrigger.class::cast)
                    .collect(Collectors.toList());
        // @formatter:on

    }

    /**
     * Returns {@code true} if the given {@code trigger} is of type
     * {@link CreateActionTrigger} and the action it is supposed to create is
     * {@link EActionType#DOC_RECEIVE}.
     *
     * @param trigger
     * @return
     */
    public static boolean isCreateReceiveActionTrigger(final Trigger trigger) {

        // check if it is create action trigger
        if (CreateActionTrigger.class.isAssignableFrom(trigger.getClass()) == false) {
            return false;
        }

        // cast and check the action type
        CreateActionTrigger createActionTrigger = (CreateActionTrigger) trigger;
        return createActionTrigger.getActionType().equals(EActionType.DOC_RECEIVE);

    }

    /**
     * Returns {@code true} if the given {@code trigger} is of type
     * {@link CreateActionTrigger}, the action it is supposed to create is
     * {@link EActionType#DOC_RECEIVE} and the OnAction type is
     * {@link EActionType#DOC_RECEIVE}.
     *
     * @param trigger
     * @return
     */
    public static boolean isCreateOnReceiveReceiveActionTrigger(final Trigger trigger) {

        // check if it is create action trigger
        if (CreateActionTrigger.class.isAssignableFrom(trigger.getClass()) == false) {
            return false;
        }

        // cast and check the action type
        CreateActionTrigger createActionTrigger = (CreateActionTrigger) trigger;
        return (createActionTrigger.getActionType().equals(EActionType.DOC_RECEIVE)
                && createActionTrigger.getOnAction().equals(EActionType.DOC_RECEIVE));
    }

    /**
     * Returns {@code true} if the given {@code trigger} is of type
     * {@link CreateActionTrigger}, the action it is supposed to create is
     * {@link EActionType#DOC_RECEIVE} and the OnAction type is
     * {@link EActionType#DOC_SEND} or
     * {@link EActionType#DOC_SEND_PARTICIPANTS}.
     *
     * @param trigger
     * @return
     */
    public static boolean isCreateOnSendReceiveActionTrigger(final Trigger trigger) {

        // check if it is create action trigger
        if (CreateActionTrigger.class.isAssignableFrom(trigger.getClass()) == false) {
            return false;
        }

        // cast and check the action type
        CreateActionTrigger createActionTrigger = (CreateActionTrigger) trigger;
        return (createActionTrigger.getActionType().equals(EActionType.DOC_RECEIVE)
                && (createActionTrigger.getOnAction().equals(EActionType.DOC_SEND)
                || createActionTrigger.getOnAction().equals(EActionType.DOC_SEND_PARTICIPANTS)));
    }
}

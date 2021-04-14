package eu.ec.dgempl.eessi.rina.tool.migration.buc.receiveaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.buc.api.common.NamingHelper;
import eu.ec.dgempl.eessi.rina.buc.api.model.ActionDO;
import eu.ec.dgempl.eessi.rina.buc.core.model.Case;
import eu.ec.dgempl.eessi.rina.buc.core.model.CreateActionTrigger;
import eu.ec.dgempl.eessi.rina.buc.core.model.EActionType;
import eu.ec.dgempl.eessi.rina.buc.core.model.ECaseRole;
import eu.ec.dgempl.eessi.rina.buc.core.model.EDocumentType;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ECountryCode;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EDocumentDirection;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EDocumentStatus;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.repo.DocumentRepoExtended;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.ActionService;

/**
 * Processes and create {@link ActionDO} objects for every
 * {@code CreateActionTrigger} definition from every document in the {@code buc}
 * definition for the action type {@link EActionType#DOC_RECEIVE}.
 */
@Component
public class SimpleReceiveActionProducer implements ReceiveActionProducer {

    private static final Logger logger = LoggerFactory.getLogger(SimpleReceiveActionProducer.class);

    @Autowired
    private RinaCaseRepo rinaCaseRepo;

    @Autowired
    private DocumentRepoExtended documentRepoExtended;

    @Autowired
    private ActionService actionService;

    @Override
    public void createReceiveActions(RinaCase rinaCase, Case buc) throws Exception {

        String caseId = rinaCase.getId();

        logger.debug("Creating simple RECEIVE actions for case [id={}, type={}]", caseId, NamingHelper.getBucId(buc));

        // get the action objects
        List<ActionDO> actions = getReceiveActions(rinaCase, buc);

        // persist the receive actions
        actions.forEach(a -> actionService.saveAction(a));
    }

    /**
     * Get type of sent documents
     *
     * @param rinaCase
     * @return
     */
    private Set<String> getSentDocumentTypes(RinaCase rinaCase) {

        List<Document> documents = documentRepoExtended.findByRinaCaseId(rinaCase.getId());

        if (CollectionUtils.isNotEmpty(documents)) {
            // @formatter:off
            return documents.stream()
                    .filter(d ->(EDocumentDirection.OUT.equals(d.getDirection()))
                            && (EDocumentStatus.ACTIVE.equals(d.getStatus())
                            || EDocumentStatus.SENT.equals(d.getStatus())
                            || EDocumentStatus.CANCELLED.equals(d.getStatus())))
                    .map(d -> d.getDocumentTypeVersion().getDocumentType().getType())
                    .collect(Collectors.toSet());
            // @formatter:on
        }

        return Collections.emptySet();
    }

    /**
     * Returns the received starter document type.
     *
     * @param rinaCase
     * @return
     */
    private String getReceivedStarterDocumentType(RinaCase rinaCase) {

        List<Document> documents = documentRepoExtended.findByRinaCaseId(rinaCase.getId());

        if (CollectionUtils.isNotEmpty(documents)) {
            // @formatter:off
            return documents.stream()
                    .filter(d -> (EDocumentDirection.IN.equals(d.getDirection()) && d.isStarter()))
                    .map(d -> d.getDocumentTypeVersion().getDocumentType().getType())
                    .findFirst()
                    .orElse(null);
            // @formatter:on
        }

        return null;
    }

    /**
     * Get type of received documents
     *
     * @param rinaCase
     * @return
     */
    private Set<String> getReceivedDocumentTypes(RinaCase rinaCase) {

        List<Document> documents = documentRepoExtended.findByRinaCaseId(rinaCase.getId());

        if (CollectionUtils.isNotEmpty(documents)) {
            // @formatter:off
            return documents.stream()
                    .filter(d ->(EDocumentDirection.IN.equals(d.getDirection())))
                    .map(d -> d.getDocumentTypeVersion().getDocumentType().getType())
                    .collect(Collectors.toSet());
            // @formatter:on
        }

        return Collections.emptySet();
    }

    /**
     * Generate Actions of type {@link EActionType#DOC_RECEIVE} related to
     * Received documents in the Case
     *
     * @param rinaCase
     * @param buc
     * @param receivedDocumentTypes
     * @return
     */
    private List<ActionDO> getOnReceiveReceiveActions(RinaCase rinaCase, Case buc, Set<String> receivedDocumentTypes) {

        // @formatter:off
        List<ActionDO> actions = buc.getDocuments().getDocument().stream()

                // filter documents
                .filter(d ->  receivedDocumentTypes.contains(d.getType().value()))

                // get only create action triggers for DOC_RECEIVE
                .map(ReceiveActionsHelper::getCreateOnReceiveReceiveActionTriggersForDocument)

                // process the results
                .flatMap(x -> x.stream())

                // filter
                .filter(t -> filter(t))

                // create action for each one of them
                .map(t -> createReceiveAction(rinaCase, t))

                // collect
                .collect(Collectors.toList());
        // @formatter:on

        return actions;
    }

    /**
     * Generate Actions of type {@link EActionType#DOC_RECEIVE} related to Sent
     * documents in the Case
     *
     * @param rinaCase
     * @param buc
     * @param sentDocumentTypes
     * @return
     */
    private List<ActionDO> getOnSendReceiveActions(RinaCase rinaCase, Case buc, Set<String> sentDocumentTypes) {
        // @formatter:off
        List<ActionDO> actions = buc.getDocuments().getDocument().stream()

                // filter documents
                .filter(d ->  sentDocumentTypes.contains(d.getType().value()))

                // get only create action triggers for DOC_RECEIVE
                .map(ReceiveActionsHelper::getCreateOnSendReceiveActionTriggersForDocument)

                // process the results
                .flatMap(x -> x.stream())

                // filter
                .filter(t -> filter(t))

                // create action for each one of them
                .map(t -> createReceiveAction(rinaCase, t))

                // collect
                .collect(Collectors.toList());
        // @formatter:on

        return actions;
    }

    /**
     * Collects all {@code CreateActionTrigger} definitions from every document
     * in the {@code buc} definition for the action type
     * {@link EActionType#DOC_RECEIVE}. For every such definition will create a
     * new {@link ActionDO} object
     *
     * @param rinaCase
     * @param buc
     * @return
     */
    protected List<ActionDO> getReceiveActions(final RinaCase rinaCase, final Case buc) {

        List<ActionDO> actions = new ArrayList<>();

        // get receive action for starter document in case of CP
        if (ECaseRole.CP.equals(buc.getRole())) {
            String starterType = getReceivedStarterDocumentType(rinaCase);
            if (starterType != null) {
                actions.add(ReceiveActionsHelper.createDefaultReceiveAction(rinaCase.getId(), EDocumentType.fromValue(starterType)));
            }
        }

        // get receive actions for SENT documents
        Set<String> sentDocumentTypes = getSentDocumentTypes(rinaCase);
        actions.addAll(getOnSendReceiveActions(rinaCase, buc, sentDocumentTypes));

        // get receive actions for RECEIVED documents
        Set<String> receivedDocumentTypes = getReceivedDocumentTypes(rinaCase);
        actions.addAll(getOnReceiveReceiveActions(rinaCase, buc, receivedDocumentTypes));

        return removeDuplicates(actions);
    }

    /**
     * Clean the list, remove duplicate receive actions for same document type
     *
     * @param actions
     * @return
     */
    private List<ActionDO> removeDuplicates(List<ActionDO> actions) {

        Set<EDocumentType> documentTypes = new HashSet<EDocumentType>();
        List<ActionDO> processedActions = new ArrayList<ActionDO>();
        for (ActionDO action : actions) {
            if (documentTypes.contains(action.getDocumentType()))
                continue;
            documentTypes.add(action.getDocumentType());
            processedActions.add(action);
        }
        return processedActions;
    }

    /**
     * Creates a receive action according to the rules
     *
     * @param rinaCase
     * @param trigger
     * @return
     */
    protected ActionDO createReceiveAction(final RinaCase rinaCase, final CreateActionTrigger trigger) {

        // create the action
        ActionDO action = ReceiveActionsHelper.createReceiveActionForTrigger(rinaCase.getId(), trigger);

        // process P3000
        processP3000(rinaCase, action);

        return action;
    }

    /**
     * If the document type if P3000, will be change according to the case
     * ownere country
     *
     * @param rinaCase
     * @param action
     */
    protected void processP3000(final RinaCase rinaCase, final ActionDO action) {

        if (EDocumentType.P_3000.equals(action.getDocumentType())) {

            ECountryCode countryCode = ReceiveActionsHelper.getCaseOwnerCountryCode(rinaCase.getParticipants());
            action.setDocumentType(EDocumentType.fromValue("P3000_" + countryCode.toString()));

        }

    }

    /**
     * Returns {@code false} if this trigger should be ommitted from the
     * processing, {@code true} otherwise
     *
     * @param trigger
     * @return
     */
    protected boolean filter(final CreateActionTrigger trigger) {

        return EDocumentType.X_003.equals(trigger.getDocumentType()) == false
                && EDocumentType.X_004.equals(trigger.getDocumentType()) == false
                && EDocumentType.X_008.equals(trigger.getDocumentType()) == false
                && EDocumentType.X_010.equals(trigger.getDocumentType()) == false
                && EDocumentType.X_011.equals(trigger.getDocumentType()) == false
                && EDocumentType.X_012.equals(trigger.getDocumentType()) == false
                && EDocumentType.X_013.equals(trigger.getDocumentType()) == false;
    }

}

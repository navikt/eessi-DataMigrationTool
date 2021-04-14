package eu.ec.dgempl.eessi.rina.tool.migration.buc.receiveaction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.buc.api.common.NamingHelper;
import eu.ec.dgempl.eessi.rina.buc.api.model.ActionDO;
import eu.ec.dgempl.eessi.rina.buc.api.utils.BucDefinitionHelper;
import eu.ec.dgempl.eessi.rina.buc.core.model.Case;
import eu.ec.dgempl.eessi.rina.buc.core.model.CreateActionTrigger;
import eu.ec.dgempl.eessi.rina.buc.core.model.EActionType;
import eu.ec.dgempl.eessi.rina.buc.core.model.EDocumentType;
import eu.ec.dgempl.eessi.rina.buc.core.model.EResultType;
import eu.ec.dgempl.eessi.rina.buc.core.model.Trigger;
import eu.ec.dgempl.eessi.rina.buc.precondition.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EDocumentStatus;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.repo.DocumentRepo;
import eu.ec.dgempl.eessi.rina.repo.DocumentRepoExtended;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.ActionService;

/**
 * Processes and creates {@link ActionDO} objects for documents, which might receive reply documents.
 */
@Component
public class ReceiveReplyActionProducer implements ReceiveActionProducer {

    private static final Logger logger = LoggerFactory.getLogger(ReceiveReplyActionProducer.class);

    @Autowired
    private ActionService actionService;

    @Autowired
    private DocumentRepoExtended documentRepoEx;

    @Autowired
    private DocumentRepo documentRepo;

    @Override
    public void createReceiveActions(RinaCase rinaCase, Case buc) throws Exception {

        String caseId = rinaCase.getId();

        logger.debug("Creating RECEIVE_REPLY actions for case [id={}, type={}]", caseId, NamingHelper.getBucId(buc));

        List<ActionDO> actions = new ArrayList<>();

        // get the action objects for received docs
        actions.addAll(getReceiveActionsForNotCancelledReceivedDocs(caseId, buc));

        // get the action objects for received docs
        actions.addAll(getReceiveActionsForSentDocs(caseId, buc));

        // save actions
        actions.forEach(a -> actionService.saveAction(a));
    }

    /**
     * Returns a list of received {@link Document} objects for the given {@code caseId}
     *
     * @param caseId
     * @return
     */
    protected List<Document> getReceivedDocumentsNotCancelled(final String caseId) {

        // @formatter:off
        List<Document> documents = documentRepoEx.findByRinaCaseId(caseId);
        return documents
                .stream()
                .filter(d -> EDocumentStatus.RECEIVED.equals(d.getStatus()))
                .collect(Collectors.toList());
        // @formatter:on

    }

    /**
     * Returns a list of sent {@link Document} objects for the given {@code caseId}
     *
     * @param caseId
     * @return
     */
    protected List<Document> getSentDocuments(final String caseId) {

        // @formatter:off
        List<Document> documents = documentRepoEx.findByRinaCaseId(caseId);
        return documents
                .stream()
                .filter(d -> EDocumentStatus.SENT.equals(d.getStatus()) || EDocumentStatus.ACTIVE.equals(d.getStatus()) )
                .collect(Collectors.toList());
        // @formatter:on

    }

    /**
     * For every document from the given {@code docs} list, will find in the {@code buc} definition if the document allows multiple
     * versions. If yes, it will create an action object with the type {@link EActionType#DOC_UPDATE_RECEIVE}. Method will return a
     * collection of all such actions.
     *
     * @param caseId
     * @param buc
     * @return
     */
    protected List<ActionDO> getReceiveActionsForSentDocs(final String caseId, final Case buc) {

        // get sent docs
        List<Document> docs = getSentDocuments(caseId);

        List<ActionDO> receiveActions = new ArrayList<>();

        for (Document doc : docs) {

            // get document type
            EDocumentType docType = EDocumentType.fromValue(doc.getDocumentTypeVersion().getDocumentType().getType());

            // get BUC document definition
            eu.ec.dgempl.eessi.rina.buc.core.model.Document bucDocument = BucDefinitionHelper.getDocumentDefinition(docType, buc);

            if (bucDocument != null && bucDocument.getTriggers() != null) {

                for (Trigger t : bucDocument.getTriggers().getTrigger()) {

                    if (ReceiveActionsHelper.isCreateActionTrigger(t) && filter((CreateActionTrigger) t)) {

                        // cast and create action
                        CreateActionTrigger createTrigger = (CreateActionTrigger) t;
                        if ((createTrigger.getOnAction().equals(EActionType.DOC_SEND)
                                || createTrigger.getOnAction().equals(EActionType.DOC_SEND_PARTICIPANTS))
                                && createTrigger.getOnResult().equals(EResultType.SUCCESS)
                                && createTrigger.getActionType().equals(EActionType.DOC_RECEIVE_REPLY)) {

                            // create receive reply action
                            receiveActions.add(createReceiveReplyAction(caseId, createTrigger, doc, bucDocument));

                        }
                    }

                }

            }

        }

        return receiveActions;

    }

    /**
     * For every document from the given {@code docs} list, will find in the {@code buc} definition if the document allows multiple
     * versions. If yes, it will create an action object with the type {@link EActionType#DOC_UPDATE_RECEIVE}. Method will return a
     * collection of all such actions.
     *
     * @param caseId
     * @param buc
     * @return
     */
    protected List<ActionDO> getReceiveActionsForNotCancelledReceivedDocs(final String caseId, final Case buc) {

        // get received not cancelled docs
        List<Document> docs = getReceivedDocumentsNotCancelled(caseId);

        List<ActionDO> receiveActions = new ArrayList<>();

        for (Document doc : docs) {

            // get document type
            EDocumentType docType = EDocumentType.fromValue(doc.getDocumentTypeVersion().getDocumentType().getType());

            // get BUC document definition
            eu.ec.dgempl.eessi.rina.buc.core.model.Document bucDocument = BucDefinitionHelper.getDocumentDefinition(docType, buc);

            if (bucDocument != null && bucDocument.getTriggers() != null) {

                for (Trigger t : bucDocument.getTriggers().getTrigger()) {

                    if (ReceiveActionsHelper.isCreateActionTrigger(t) && filter((CreateActionTrigger) t)) {

                        // cast and create action
                        CreateActionTrigger createTrigger = (CreateActionTrigger) t;
                        if (createTrigger.getOnAction().equals(EActionType.DOC_RECEIVE)
                                && createTrigger.getOnResult().equals(EResultType.SUCCESS)
                                && createTrigger.getActionType().equals(EActionType.DOC_RECEIVE_REPLY)) {

                            // create receive reply action
                            receiveActions.add(createReceiveReplyAction(caseId, createTrigger, doc, bucDocument));

                        }
                    }

                }

            }

        }

        return receiveActions;

    }

    /**
     * Returns a new {@link ActionDO} object with {@link EActionType#DOC_RECEIVE} for the given parameters in the context of the RECEIVED
     * document.
     *
     * @param caseId
     * @param trigger
     * @param dbDocument
     * @param document
     * @return
     */
    protected ActionDO createReceiveReplyAction(String caseId, CreateActionTrigger trigger, Document dbDocument,
            final eu.ec.dgempl.eessi.rina.buc.core.model.Document document) {

        PreconditionsHelper.notBlank(caseId, "caseId");
        PreconditionsHelper.notNull(dbDocument, "dbDocument");
        PreconditionsHelper.notNull(document, "document");
        PreconditionsHelper.notNull(trigger, "trigger");

        logger.debug("Creating RECEIVE action for a REPLY [caseId={}, docType={}, docId={}]", caseId, document.getType(),
                dbDocument.getId());

        // create the default receive action
        ActionDO actionDO = ReceiveActionsHelper.createReceiveActionForTrigger(caseId, trigger);

        // set either the grandparent as related
        if (trigger.isSameParentDocument()) {

            // get the parent doc
            if (dbDocument.getParent() == null) {
                throw new IllegalStateException(String.format("sameParentDocument set to true for document with no Parent document"));
            }

            actionDO.setParentDocumentId(dbDocument.getParent().getId());
            actionDO.setParentDocumentType(
                    EDocumentType.fromValue(dbDocument.getParent().getDocumentTypeVersion().getDocumentType().getType()));

        }
        // or set the parent as related
        else {

            actionDO.setParentDocumentId(dbDocument.getId());
            actionDO.setParentDocumentType(EDocumentType.fromValue(dbDocument.getDocumentTypeVersion().getDocumentType().getType()));

        }

        return actionDO;
    }

    /**
     * Filter method
     *
     * @param trigger
     * @return
     */
    protected boolean filter(final CreateActionTrigger trigger) {

        return true;

    }

}

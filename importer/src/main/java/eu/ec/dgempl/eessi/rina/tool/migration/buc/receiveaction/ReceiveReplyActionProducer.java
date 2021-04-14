package eu.ec.dgempl.eessi.rina.tool.migration.buc.receiveaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.ActionService;

/**
 * Processes and creates {@link ActionDO} objects for documents, which might receive reply documents.
 */
@Component
public class ReceiveReplyActionProducer implements ReceiveActionProducer {

    private static final Logger logger = LoggerFactory.getLogger(ReceiveReplyActionProducer.class);

    @Autowired
    private ActionService actionService;

    @Override
    public void createReceiveActions(RinaCase rinaCase, Case buc) throws Exception {

        String caseId = rinaCase.getId();

        logger.debug("Creating RECEIVE_REPLY actions for case [id={}, type={}]", caseId, NamingHelper.getBucId(buc));

        List<ActionDO> actions = new ArrayList<>();

        // get the action objects for received docs
        actions.addAll(getReceiveActionsForNotCancelledReceivedDocs(caseId, buc));

        // get the action objects for received docs
        actions.addAll(getReceiveActionsForSentDocs(caseId, buc));

        // get the exceptional receive reply actions.

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

        // TODO: retrieve the received documents from the DB
        return Collections.emptyList();

    }

    /**
     * Returns a list of sent {@link Document} objects for the given {@code caseId}
     * 
     * @param caseId
     * @return
     */
    protected List<Document> getSentDocuments(final String caseId) {

        // TODO: retrieve the sent documents from the DB
        return Collections.emptyList();

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

            if (bucDocument.getTriggers() != null) {

                for (Trigger t : bucDocument.getTriggers().getTrigger()) {

                    if (ReceiveActionsHelper.isCreateActionTrigger(t)) {

                        // cast and create action
                        CreateActionTrigger createTrigger = (CreateActionTrigger) t;
                        if ((createTrigger.getOnAction().equals(EActionType.DOC_SEND)
                                || createTrigger.getOnAction().equals(EActionType.DOC_SEND_PARTICIPANTS))
                                && createTrigger.getOnResult().equals(EResultType.SUCCESS)
                                && createTrigger.getActionType().equals(EActionType.DOC_RECEIVE_REPLY)) {

                            // create receive reply action
                            receiveActions.add(createReceiveReplyActionForSentDocument(caseId, createTrigger, doc, bucDocument));

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

            if (bucDocument.getTriggers() != null) {

                for (Trigger t : bucDocument.getTriggers().getTrigger()) {

                    if (ReceiveActionsHelper.isCreateActionTrigger(t)) {

                        // cast and create action
                        CreateActionTrigger createTrigger = (CreateActionTrigger) t;
                        if (createTrigger.getOnAction().equals(EActionType.DOC_RECEIVE)
                                && createTrigger.getOnResult().equals(EResultType.SUCCESS)
                                && createTrigger.getActionType().equals(EActionType.DOC_RECEIVE_REPLY)) {

                            // create receive reply action
                            receiveActions.add(createReceiveReplyActionForNonCancelledDocument(caseId, createTrigger, doc, bucDocument));

                        }
                    }

                }

            }

        }

        return receiveActions;

    }

    /**
     * Returns a new {@link ActionDO} object with {@link EActionType#DOC_RECEIVE} for the given parameters in the context of a SENT document
     * 
     * @param caseId
     * @param trigger
     * @param dbDocument
     * @param document
     * @return
     */
    protected ActionDO createReceiveReplyActionForSentDocument(String caseId, CreateActionTrigger trigger, Document dbDocument,
            final eu.ec.dgempl.eessi.rina.buc.core.model.Document document) {

        PreconditionsHelper.notBlank(caseId, "caseId");
        PreconditionsHelper.notNull(dbDocument, "dbDocument");
        PreconditionsHelper.notNull(document, "document");
        PreconditionsHelper.notNull(trigger, "trigger");

        logger.debug("Creating RECEIVE action for a REPLY [caseId={}, docType={}, docId={}]", caseId, document.getType(),
                dbDocument.getId());

        ActionDO actionDO = ReceiveActionsHelper.createReceiveActionForTrigger(caseId, trigger);

        return actionDO;
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
    protected ActionDO createReceiveReplyActionForNonCancelledDocument(String caseId, CreateActionTrigger trigger, Document dbDocument,
            final eu.ec.dgempl.eessi.rina.buc.core.model.Document document) {

        PreconditionsHelper.notBlank(caseId, "caseId");
        PreconditionsHelper.notNull(dbDocument, "dbDocument");
        PreconditionsHelper.notNull(document, "document");
        PreconditionsHelper.notNull(trigger, "trigger");

        logger.debug("Creating RECEIVE action for a REPLY [caseId={}, docType={}, docId={}]", caseId, document.getType(),
                dbDocument.getId());

        ActionDO actionDO = ReceiveActionsHelper.createReceiveActionForTrigger(caseId, trigger);
        // DocumentMetadataDO parentDocument = documentMetadataService.findByDocumentIdAndCaseId(relatedDocumentId, caseId);
        //
        // if (trigger.isSameParentDocument()) {
        //
        // if (StringUtils.isBlank(parentDocument.getParentDocumentId())) {
        // throw new IllegalArgumentException(String.format("sameParentDocument set to true for document with no Parent document"));
        // }
        //
        // DocumentMetadataDO grandParentDocument = documentMetadataService.findByDocumentIdAndCaseId(parentDocument.getParentDocumentId(),
        // caseId);
        // actionDO.setParentDocumentId(grandParentDocument.getId());
        // actionDO.setParentDocumentType(grandParentDocument.getDocumentType());
        // } else {
        // actionDO.setParentDocumentId(relatedDocumentId);
        // actionDO.setParentDocumentType(parentDocument.getDocumentType());
        // }

        return actionDO;
    }

    protected boolean filter(final Document dbDoc, eu.ec.dgempl.eessi.rina.buc.core.model.Document bucDoc) {

        return true;
    }

}

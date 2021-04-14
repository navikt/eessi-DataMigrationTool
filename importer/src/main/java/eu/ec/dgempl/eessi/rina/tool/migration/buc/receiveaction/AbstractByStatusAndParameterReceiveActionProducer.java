package eu.ec.dgempl.eessi.rina.tool.migration.buc.receiveaction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import eu.ec.dgempl.eessi.rina.buc.api.common.NamingHelper;
import eu.ec.dgempl.eessi.rina.buc.api.model.ActionDO;
import eu.ec.dgempl.eessi.rina.buc.api.utils.BucDefinitionHelper;
import eu.ec.dgempl.eessi.rina.buc.core.model.Case;
import eu.ec.dgempl.eessi.rina.buc.core.model.EActionType;
import eu.ec.dgempl.eessi.rina.buc.core.model.EDocumentParameterType;
import eu.ec.dgempl.eessi.rina.buc.core.model.EDocumentType;
import eu.ec.dgempl.eessi.rina.buc.precondition.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EDocumentStatus;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.ActionService;

/**
 * Abstract receive action producer which creates a DOC_RECEIVE action in the same fashion. The child classes should implement a method to
 * retrieve the correct documents and to check for a document parameter.
 */
public abstract class AbstractByStatusAndParameterReceiveActionProducer implements ReceiveActionProducer {

    private static final Logger logger = LoggerFactory.getLogger(AbstractByStatusAndParameterReceiveActionProducer.class);

    @Autowired
    private ActionService actionService;

    /**
     * Which status of document should be filtered
     * 
     * @return
     */
    protected abstract EDocumentStatus getDocumentStatus();

    /**
     * Which parameter must be {@code true}
     * 
     * @return
     */
    protected abstract EDocumentParameterType getDocumentParameter();

    /**
     * Which document type should be set to created receive action
     * 
     * @return
     */
    protected abstract EDocumentType getDocumentTypeForReceiveAction();

    @Override
    public void createReceiveActions(RinaCase rinaCase, Case buc) throws Exception {

        String caseId = rinaCase.getId();

        logger.debug("Creating DOC_RECEIVE actions X008 documents for case [id={}, type={}]", caseId, NamingHelper.getBucId(buc));

        // get the action objects
        List<Document> docs = getDocumentsToProcess(rinaCase, buc);

        // get the receive update actions
        List<ActionDO> receiveUpdateActions = getReceiveActions(caseId, docs, buc);

        // save actions
        receiveUpdateActions.forEach(a -> actionService.saveAction(a));
    }

    /**
     * Returns a list of received {@link Document} objects for the given {@code caseId}
     * 
     * @param rinaCase
     * @return
     */
    protected List<Document> getDocumentsToProcess(final RinaCase rinaCase, final Case buc) {

        // @formatter:off
        return rinaCase.getDocuments()
                        .stream()
                        .filter(d -> getDocumentStatus().equals(d.getStatus()) && isParameterTrue(d, buc))
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
    protected List<ActionDO> getReceiveActions(final String caseId, final List<Document> docs, final Case buc) {

        List<ActionDO> receiveUpdateActions = new ArrayList<>();

        for (Document doc : docs) {

            receiveUpdateActions.add(createReceiveActionForDocument(caseId, doc));

        }

        return receiveUpdateActions;

    }

    /**
     * Returns {@code true} if the document can be cancelled, i.e. an X008 can be received
     *
     * @param doc
     * @param buc
     * @return
     */
    protected boolean isParameterTrue(Document doc, Case buc) {

        // get document type
        EDocumentType docType = EDocumentType.fromValue(doc.getDocumentTypeVersion().getDocumentType().getType());

        // get BUC document definition
        eu.ec.dgempl.eessi.rina.buc.core.model.Document bucDocument = BucDefinitionHelper.getDocumentDefinition(docType, buc);

        // not all documents must have definition in the BUC. In that case, the parameter is considered false
        if (bucDocument != null) {
            if (bucDocument.getParameters() != null) {
                String paramValue = BucDefinitionHelper.getDocumentParameterByKey(getDocumentParameter(),
                        bucDocument.getParameters().getParameter());

                if (StringUtils.isNotBlank(paramValue)) {
                    return Boolean.valueOf(paramValue);
                }

            }
        }

        return false;

    }

    /**
     * Returns a new {@link ActionDO} object with {@link EActionType#DOC_RECEIVE} for X008 for the given {@code document} and {@code caseId}
     * 
     * @param caseId
     * @return
     */
    protected ActionDO createReceiveActionForDocument(String caseId, Document dbDocument) {

        PreconditionsHelper.notNull(caseId, "caseId");
        PreconditionsHelper.notNull(dbDocument, "dbDocument");

        logger.debug("Creating DOC_RECEIVE action for document type {} for [caseId={}, docId={}]", getDocumentTypeForReceiveAction(),
                caseId, dbDocument.getId());

        ActionDO actionDO = ReceiveActionsHelper.createDefaultReceiveAction(caseId, getDocumentTypeForReceiveAction());
        actionDO.setDocumentId(dbDocument.getId());

        // set parent document
        actionDO.setParentDocumentId(dbDocument.getId());
        actionDO.setParentDocumentType(EDocumentType.fromValue(dbDocument.getDocumentTypeVersion().getDocumentType().getType()));

        return actionDO;
    }

    /**
     * Filter admin docs
     * 
     * @param doc
     * @return
     */
    protected boolean filter(final Document doc) {

        // @formatter:off
        return true;
        // @formatter:on

    }

}

package eu.ec.dgempl.eessi.rina.tool.migration.buc.receiveaction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
import eu.ec.dgempl.eessi.rina.repo.DocumentRepoExtended;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.ActionService;

/**
 * Processes and creates {@link ActionDO} objects for every document, which has been received and might receive and update
 */
@Component
public class ReceiveUpdateActionProducer implements ReceiveActionProducer {

    private static final Logger logger = LoggerFactory.getLogger(ReceiveUpdateActionProducer.class);

    private final DocumentRepoExtended documentRepo;

    @Autowired
    private ActionService actionService;

    public ReceiveUpdateActionProducer(final DocumentRepoExtended documentRepo) {
        this.documentRepo = documentRepo;
    }

    @Override
    public void createReceiveActions(RinaCase rinaCase, Case buc) throws Exception {

        String caseId = rinaCase.getId();

        logger.debug("Creating RECEIVE_UPDATE actions for case [id={}, type={}]", caseId, NamingHelper.getBucId(buc));

        // get the action objects
        List<Document> docs = getReceivedDocuments(rinaCase);

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
    protected List<Document> getReceivedDocuments(final RinaCase rinaCase) {

        // @formatter:off
        List<Document> documents = documentRepo.findByRinaCaseId(rinaCase.getId());
        return documents
                        .stream()
                        .filter(d -> EDocumentStatus.RECEIVED.equals(d.getStatus()))
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

            // get document type
            EDocumentType docType = EDocumentType.fromValue(doc.getDocumentTypeVersion().getDocumentType().getType());

            // get BUC document definition
            eu.ec.dgempl.eessi.rina.buc.core.model.Document bucDocument = BucDefinitionHelper.getDocumentDefinition(docType, buc);

            // check if receive update action should be generated
            if (hasUpdate(bucDocument) && filter(doc, bucDocument)) {
                receiveUpdateActions.add(createReceiveUpdateActionForDocument(caseId, doc, bucDocument));
            }

        }

        return receiveUpdateActions;

    }

    /**
     * Returns {@code true} if the given document has multiple versions, {code false} othrwise.
     *
     * @param doc
     * @return
     */
    protected boolean hasUpdate(final eu.ec.dgempl.eessi.rina.buc.core.model.Document doc) {

        if (doc != null && doc.getParameters() != null) {
            String hasMultipleVersions = BucDefinitionHelper.getDocumentParameterByKey(EDocumentParameterType.HAS_MULTIPLE_VERSIONS,
                    doc.getParameters().getParameter());

            if (StringUtils.isNotBlank(hasMultipleVersions)) {
                return Boolean.valueOf(hasMultipleVersions);
            }

        }

        return false;
    }

    /**
     * Returns a new {@link ActionDO} object with {@link EActionType#DOC_RECEIVE} for the given {@code document} and {@code caseId}
     *
     * @param caseId
     * @param dbDocument
     * @param document
     * @return
     */
    protected ActionDO createReceiveUpdateActionForDocument(String caseId, Document dbDocument,
            final eu.ec.dgempl.eessi.rina.buc.core.model.Document document) {

        PreconditionsHelper.notNull(caseId, "caseId");
        PreconditionsHelper.notNull(dbDocument, "dbDocument");
        PreconditionsHelper.notNull(document, "document");

        logger.debug("Creating RECEIVE_UPDATE action for [caseId={}, docType={}, docId={}]", caseId, document.getType(),
                dbDocument.getId());

        ActionDO actionDO = ReceiveActionsHelper.createDefaultReceiveUpdateAction(caseId, document.getType());
        actionDO.setDocumentId(dbDocument.getId());

        // set parent document
        if (dbDocument.getParent() != null) {
            actionDO.setParentDocumentId(dbDocument.getParent().getId());
            actionDO.setParentDocumentType(
                    EDocumentType.fromValue(dbDocument.getParent().getDocumentTypeVersion().getDocumentType().getType()));
        }

        // process P3000
        processP3000(dbDocument, actionDO);

        return actionDO;
    }

    /**
     * If the document type if P3000, will be change according to the country
     *
     * @param dbDocument
     * @param action
     */
    protected void processP3000(Document dbDocument, final ActionDO action) {

        if (EDocumentType.P_3000.equals(action.getDocumentType())) {

            EDocumentType docType = EDocumentType.fromValue(dbDocument.getDocumentTypeVersion().getDocumentType().getType());
            action.setDocumentType(docType);

        }

    }

    /**
     * Filter admin docs
     *
     * @param dbDoc
     * @param bucDoc
     * @return
     */
    protected boolean filter(final Document dbDoc, eu.ec.dgempl.eessi.rina.buc.core.model.Document bucDoc) {

        return EDocumentType.X_001.equals(bucDoc.getType()) == false && EDocumentType.X_002.equals(bucDoc.getType()) == false
                && EDocumentType.X_003.equals(bucDoc.getType()) == false && EDocumentType.X_004.equals(bucDoc.getType()) == false
                && EDocumentType.X_005.equals(bucDoc.getType()) == false && EDocumentType.X_006.equals(bucDoc.getType()) == false
                && EDocumentType.X_007.equals(bucDoc.getType()) == false && EDocumentType.X_008.equals(bucDoc.getType()) == false
                && EDocumentType.X_009.equals(bucDoc.getType()) == false && EDocumentType.X_010.equals(bucDoc.getType()) == false
                && EDocumentType.X_011.equals(bucDoc.getType()) == false && EDocumentType.X_012.equals(bucDoc.getType()) == false
                && EDocumentType.X_013.equals(bucDoc.getType()) == false;

    }

}

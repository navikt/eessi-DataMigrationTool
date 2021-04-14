package eu.ec.dgempl.eessi.rina.tool.migration.buc.receiveaction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.buc.api.common.NamingHelper;
import eu.ec.dgempl.eessi.rina.buc.api.model.ActionDO;
import eu.ec.dgempl.eessi.rina.buc.api.model.EDocumentStatus;
import eu.ec.dgempl.eessi.rina.buc.core.model.Case;
import eu.ec.dgempl.eessi.rina.buc.core.model.EActionStatus;
import eu.ec.dgempl.eessi.rina.buc.core.model.EActionType;
import eu.ec.dgempl.eessi.rina.buc.core.model.EDocumentType;
import eu.ec.dgempl.eessi.rina.buc.precondition.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.repo.DocumentRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.ActionService;

/**
 * RECEIVE action producer for the X004 actions
 */
@Component
public class X004ReceiveActionProducer implements ReceiveActionProducer {

    private static final Logger logger = LoggerFactory.getLogger(X004ReceiveActionProducer.class);

    @Autowired
    DocumentRepo documentRepo;

    @Autowired
    ActionService actionService;

    @Override
    public void createReceiveActions(final RinaCase rinaCase, Case bucDefinition) throws Exception {

        logger.debug("Creating X004 RECEIVE actions for case [id={}, type={}]", rinaCase.getId(), NamingHelper.getBucId(bucDefinition));

        List<Document> x003Documents = documentRepo.findByRinaCaseIdAndDocumentTypeVersionDocumentTypeType(rinaCase.getId(),
                EDocumentType.X_003.name());

        boolean isMLC = isMLC(bucDefinition);

        if (!isMLC) {
            // bilateral case
            x003Documents.forEach(item -> {

                if (EDocumentStatus.SENT.name().equals(item.getStatus().name())) {
                    actionService.saveAction(createX004ReceiveAction(rinaCase.getId(), item, isMLC));
                }
            });

        } else {
            // multi-lateral case
            for (Document item : x003Documents) {

                if (EDocumentStatus.SENT.name().equals(item.getStatus().name())) {
                    actionService.saveAction(createX004ReceiveAction(rinaCase.getId(), item, isMLC));
                    break;
                }
            }
        }
    }

    /**
     * Create an Action object for the DOC_RECEIVE of X004 as a reply to X003
     *
     * @param caseId
     * @param document
     * @param isMlc
     * @return
     */
    protected ActionDO createX004ReceiveAction(final String caseId, Document document, boolean isMlc) {

        PreconditionsHelper.notNull(caseId, "caseId");
        PreconditionsHelper.notNull(document, "document");

        ActionDO actionDO = new ActionDO();
        actionDO.setId(UUID.randomUUID().toString());
        actionDO.setStatus(EActionStatus.ACTIVE);
        actionDO.setType(EActionType.DOC_RECEIVE);
        actionDO.setCanClose(false);
        actionDO.setCaseId(caseId);
        actionDO.setDocumentType(EDocumentType.X_004);

        if (!isMlc) {
            actionDO.setParentDocumentId(document.getId());
            actionDO.setParentDocumentType(EDocumentType.X_003);
        }

        return actionDO;
    }

    // methods used solely for testing purposes in the jUnit.

    /**
     * Get all the X002, for which a RECEIVE action of X003 should be created
     *
     * @param caseId
     * @return
     */
    protected List<Document> getAllX003(final String caseId) {
        return documentRepo.findByRinaCaseIdAndDocumentTypeVersionDocumentTypeType(caseId, EDocumentType.X_003.value());
    }

    /**
     * @param caseId
     * @param buc
     * @return
     */
    protected List<ActionDO> getReceiveActions(final String caseId, final List<Document> docs, final Case buc) {

        List<ActionDO> receiveUpdateActions = new ArrayList<>();

        for (Document doc : docs) {
            receiveUpdateActions.add(createX004ReceiveAction(caseId, doc, false));

        }
        return receiveUpdateActions;
    }
}
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
 * RECEIVE action producer for the X003 actions
 */
@Component
public class X003ReceiveActionProducer implements ReceiveActionProducer {

    private static final Logger logger = LoggerFactory.getLogger(X003ReceiveActionProducer.class);

    @Autowired
    DocumentRepo documentRepo;

    @Autowired
    ActionService actionService;

    @Override
    public void createReceiveActions(final RinaCase rinaCase, Case bucDefinition) throws Exception {

        logger.debug("Creating X003 RECEIVE actions for case [id={}, type={}]", rinaCase.getId(),
                NamingHelper.getBucId(bucDefinition));

        List<Document> x002Documents = documentRepo
                .findByRinaCaseIdAndDocumentTypeVersionDocumentTypeType(rinaCase.getId(), EDocumentType.X_002.name());

        x002Documents.forEach(d -> {
            if (EDocumentStatus.SENT.name().equals(d.getStatus().name())) {
                actionService.saveAction(createX003ReceiveAction(rinaCase.getId(), d));
            }
        });
    }

    /**
     * Create an Action object for the DOC_RECEIVE of X003 as a reply to X002
     *
     * @param caseId
     * @param x002
     * @return
     */
    protected ActionDO createX003ReceiveAction(final String caseId, Document x002) {

        PreconditionsHelper.notNull(caseId, "caseId");
        PreconditionsHelper.notNull(x002, "x002");

        ActionDO actionDO = new ActionDO();
        actionDO.setId(UUID.randomUUID().toString());
        actionDO.setStatus(EActionStatus.ACTIVE);
        actionDO.setType(EActionType.DOC_RECEIVE);
        actionDO.setCanClose(false);
        actionDO.setCaseId(caseId);
        actionDO.setDocumentType(EDocumentType.X_003);
        actionDO.setParentDocumentId(x002.getId());
        actionDO.setParentDocumentType(EDocumentType.X_002);

        return actionDO;
    }

    // methods used solely for testing purposes in the jUnit.

    /**
     * Get all the X002, for which a RECEIVE action of X003 should be created
     *
     * @param caseId
     * @return
     */
    protected List<Document> getAllX002(final String caseId) {
        return documentRepo.
                findByRinaCaseIdAndDocumentTypeVersionDocumentTypeType(caseId, EDocumentType.X_002.value());
    }

    /**
     * @param caseId
     * @param buc
     * @return
     */
    protected List<ActionDO> getReceiveActions(final String caseId, final List<Document> docs, final Case buc) {

        List<ActionDO> receiveUpdateActions = new ArrayList<>();

        for (Document doc : docs) {
            receiveUpdateActions.add(createX003ReceiveAction(caseId, doc));

        }
        return receiveUpdateActions;
    }
}
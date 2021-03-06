package eu.ec.dgempl.eessi.rina.tool.migration.buc.receiveaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
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
 * RECEIVE action producer for the X013 actions
 */
@Component
public class X013ReceiveActionProducer implements ReceiveActionProducer {

    private static final Logger logger = LoggerFactory.getLogger(X013ReceiveActionProducer.class);

    @Autowired
    DocumentRepo documentRepo;

    @Autowired
    ActionService actionService;

    @Override
    public void createReceiveActions(final RinaCase rinaCase, Case bucDefinition) throws Exception {

        String caseId = rinaCase.getId();

        logger.debug("Creating X013 RECEIVE actions for case [id={}, type={}]", caseId, NamingHelper.getBucId(bucDefinition));

        // select all X012
        List<Document> x012 = getAllSentX012(caseId);

        // for every X012 create DOC_RECEIVE with docType=X013 and parentDocumentId=X012.id
        x012.forEach(d -> actionService.saveAction(createX013Receive(caseId, d)));

    }

    /**
     * Get all the X012, for which a RECEIVE action of X013 should be created
     *
     * @param caseId
     * @return
     */
    protected List<Document> getAllSentX012(final String caseId) {
        // @formatter:off
        List<Document> allX012 = documentRepo.findByRinaCaseIdAndDocumentTypeVersionDocumentTypeType(
                caseId,
                EDocumentType.X_012.value());

        if (CollectionUtils.isNotEmpty(allX012)) {
            return allX012.stream()
                    .filter(d -> EDocumentStatus.SENT.name().equals(d.getStatus().name()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
        // @formatter:on        
    }

    /**
     * @param caseId
     * @param buc
     * @return
     */
    protected List<ActionDO> getReceiveActions(final String caseId, final List<Document> docs, final Case buc) {

        List<ActionDO> receiveUpdateActions = new ArrayList<>();

        for (Document doc : docs) {

            receiveUpdateActions.add(createX013Receive(caseId, doc));

        }

        return receiveUpdateActions;

    }

    /**
     * Create an Action object for the DOC_RECEIVE of X013 as a reply to X012
     *
     * @param caseId
     * @param x012
     * @return
     */
    protected ActionDO createX013Receive(final String caseId, Document x012) {

        PreconditionsHelper.notNull(caseId, "caseId");
        PreconditionsHelper.notNull(x012, "x012");

        // setup the basic parameters
        ActionDO actionDO = new ActionDO();
        actionDO.setId(UUID.randomUUID().toString());
        actionDO.setStatus(EActionStatus.ACTIVE);
        actionDO.setType(EActionType.DOC_RECEIVE);
        actionDO.setCanClose(false);
        actionDO.setCaseId(caseId);
        actionDO.setDocumentType(EDocumentType.X_013);

        // setup the parent document id and type
        actionDO.setParentDocumentId(x012.getId());
        actionDO.setParentDocumentType(EDocumentType.X_012);

        return actionDO;

    }

}

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
import eu.ec.dgempl.eessi.rina.buc.core.model.Case;
import eu.ec.dgempl.eessi.rina.buc.core.model.CreateActionTrigger;
import eu.ec.dgempl.eessi.rina.buc.core.model.Document;
import eu.ec.dgempl.eessi.rina.buc.core.model.EActionType;
import eu.ec.dgempl.eessi.rina.buc.core.model.EDocumentType;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ECountryCode;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.repo.DocumentRepo;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.ActionService;

/**
 * Processes and create {@link ActionDO} objects for every {@code CreateActionTrigger} definition from every document in the {@code buc}
 * definition for the action type {@link EActionType#DOC_RECEIVE}.
 */
@Component
public class SimpleReceiveActionProducer implements ReceiveActionProducer {

    private static final Logger logger = LoggerFactory.getLogger(SimpleReceiveActionProducer.class);

    @Autowired
    private RinaCaseRepo rinaCaseRepo;

    @Autowired
    private DocumentRepo documentRepo;

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
     * Collects all {@code CreateActionTrigger} definitions from every document in the {@code buc} definitin for the action type
     * {@link EActionType#DOC_RECEIVE}. For every such definition will create a new {@link ActionDO} object
     * 
     * @param rinaCase
     * @param buc
     * @return
     */
    protected List<ActionDO> getReceiveActions(final RinaCase rinaCase, final Case buc) {

        // @formatter:off
        List<ActionDO> actions = buc.getDocuments().getDocument().stream()
                
                            // filter documents
                            .filter(d -> filter(d))
                        
                            // get only create action triggers for DOC_RECEIVE
                            .map(ReceiveActionsHelper::getCreateReceiveActionTriggersForDocument)
                            
                            // process the results
                            .flatMap(x -> x.stream())
                        
                            // filter 
                            .filter(t -> filter(t))
                        
                            // create action for each one of them
                            .map(t -> createReceiveAction(rinaCase, t))
                        
                            // collect
                            .collect(Collectors.toList());
        // @formatter:on         

        // add DA073A receive actions
        actions.addAll(processDA073A(rinaCase));

        // add S053A receive actions
        actions.addAll(processS053A(rinaCase));

        return actions;
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
     * If the document type if P3000, will be change according to the case ownere country
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
     * Process DA073A doc
     * 
     * @param rinaCase
     */
    protected List<ActionDO> processDA073A(final RinaCase rinaCase) {

        // create a receive action for all these docs
        List<eu.ec.dgempl.eessi.rina.model.jpa.entity.Document> da071_docs = documentRepo
                .findByRinaCaseIdAndDocumentTypeVersionDocumentTypeType(rinaCase.getId(), EDocumentType.DA_071.value());

        List<ActionDO> da073a_docs = new ArrayList<>();
        for (eu.ec.dgempl.eessi.rina.model.jpa.entity.Document doc : da071_docs) {

            ActionDO action = ReceiveActionsHelper.createDefaultReceiveAction(rinaCase.getId(), EDocumentType.DA_073_A);
            action.setParentDocumentId(doc.getId());
            action.setParentDocumentType(EDocumentType.DA_071);

        }

        return da073a_docs;

    }

    /**
     * Process S_053_A doc
     * 
     * @param rinaCase
     */
    protected List<ActionDO> processS053A(final RinaCase rinaCase) {

        // create a receive action for all these docs
        List<eu.ec.dgempl.eessi.rina.model.jpa.entity.Document> s051_docs = documentRepo
                .findByRinaCaseIdAndDocumentTypeVersionDocumentTypeType(rinaCase.getId(), EDocumentType.S_051.value());

        List<ActionDO> s053a_docs = new ArrayList<>();
        for (eu.ec.dgempl.eessi.rina.model.jpa.entity.Document doc : s051_docs) {

            ActionDO action = ReceiveActionsHelper.createDefaultReceiveAction(rinaCase.getId(), EDocumentType.S_053_A);
            action.setParentDocumentId(doc.getId());
            action.setParentDocumentType(EDocumentType.S_051);

        }

        return s053a_docs;

    }

    /**
     * Returns {@code false} if this trigger should be ommitted from the processing, {@code true} otherwise
     * 
     * @param trigger
     * @return
     */
    protected boolean filter(final CreateActionTrigger trigger) {

        return EDocumentType.DA_073_A.equals(trigger.getDocumentType()) == false
                && EDocumentType.S_053_A.equals(trigger.getDocumentType()) == false
                && EDocumentType.X_003.equals(trigger.getDocumentType()) == false
                && EDocumentType.X_004.equals(trigger.getDocumentType()) == false
                && EDocumentType.X_008.equals(trigger.getDocumentType()) == false
                && EDocumentType.X_010.equals(trigger.getDocumentType()) == false
                && EDocumentType.X_011.equals(trigger.getDocumentType()) == false
                && EDocumentType.X_012.equals(trigger.getDocumentType()) == false
                && EDocumentType.X_013.equals(trigger.getDocumentType()) == false;
    }

    /**
     * Returns {@code false} if this document should be ommitted from the processing, {@code true} otherwise
     * 
     * @param document
     * @return
     */
    protected boolean filter(final Document document) {

        // TODO: filter out the exceptional documents
        return true;
    }

}

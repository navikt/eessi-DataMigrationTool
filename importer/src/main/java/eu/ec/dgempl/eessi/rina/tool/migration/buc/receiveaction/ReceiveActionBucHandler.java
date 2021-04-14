package eu.ec.dgempl.eessi.rina.tool.migration.buc.receiveaction;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.buc.core.model.Case;
import eu.ec.dgempl.eessi.rina.buc.core.model.ECaseRole;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ECaseStatus;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.buc.BucDefinitionImporterFactory;
import eu.ec.dgempl.eessi.rina.tool.migration.buc.BucHandler;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;

/**
 * Handler for creating the RECEIVE actions, which are missing in the elastic DB.
 */
@Component
public class ReceiveActionBucHandler implements BucHandler {

    private static final Logger logger = LoggerFactory.getLogger(ReceiveActionBucHandler.class);

    @Autowired
    private RinaCaseRepo rinaCaseRepo;

    @Autowired
    private BucDefinitionImporterFactory bucDefinitionFactory;

    @Autowired
    private List<ReceiveActionProducer> receiveActionProducers;

    @Override
    public void processCase(String caseId, String bucType, ECaseRole caseRole, String modelVersion) throws Exception {

        PreconditionsHelper.notEmpty(caseId, "caseId");
        PreconditionsHelper.notEmpty(bucType, "bucType");
        PreconditionsHelper.notNull(caseRole, "caseRole");
        PreconditionsHelper.notEmpty(modelVersion, "modelVersion");

        logger.debug("Processing RECEIVE actions for the case [caseId={}]", caseId);

        // TODO: check if the case has been started
        RinaCase rinaCase = rinaCaseRepo.findById(caseId);

        // skip the removed cases
        if (ECaseStatus.REMOVED.equals(rinaCase.getStatus())) {
            return;
        }

        // build the BUC definition object
        final Case buc = bucDefinitionFactory.loadBucConfiguration(bucType, caseRole, modelVersion);

        // call every receive action producer
        receiveActionProducers.forEach(p -> {
            try {
                p.createReceiveActions(rinaCase, buc);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

}

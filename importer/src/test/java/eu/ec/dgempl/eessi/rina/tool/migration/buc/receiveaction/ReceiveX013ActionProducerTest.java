package eu.ec.dgempl.eessi.rina.tool.migration.buc.receiveaction;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import clover.org.jfree.util.Log;
import eu.ec.dgempl.eessi.rina.buc.api.model.ActionDO;
import eu.ec.dgempl.eessi.rina.buc.core.model.Case;
import eu.ec.dgempl.eessi.rina.buc.core.model.ECaseRole;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.buc.BucDefinitionImporterFactory;

/**
 * Tests the creation of DOC_RECEIVE actions for X013
 */
@Transactional
public class ReceiveX013ActionProducerTest extends AbstractBucProcessorTest {

    private static final Logger logger = LoggerFactory.getLogger(ReceiveX013ActionProducerTest.class);

    @Autowired
    private BucDefinitionImporterFactory bucDefinitionImporterFactory;

    @Autowired
    private X013ReceiveActionProducer x013ReceiveActionProducer;

    @Autowired
    private RinaCaseRepo rinaCaseRepo;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testPBuc01_PO() throws Exception {

        Case pBuc01_v42 = bucDefinitionImporterFactory.loadBucConfiguration("p_buc_01", ECaseRole.PO, "4.2");
        Assert.assertNotNull(pBuc01_v42);

        String caseId = "16";
        
        RinaCase rinaCase = rinaCaseRepo.findById(caseId);
        
        if (rinaCase == null) {
            Log.error("Case not found " + caseId);
            return;
        }
                    
        
        List<ActionDO> actions = getReceiveUpdateActions(rinaCase, pBuc01_v42);

        Assert.assertNotNull(actions);
        logger.info("{}", actions.stream().map(a -> a.getDocumentType().value()).collect(Collectors.joining(",")));

    }

    @Test
    public void testPBuc01_CP() throws Exception {

        Case pBuc01_v42 = bucDefinitionImporterFactory.loadBucConfiguration("p_buc_01", ECaseRole.CP, "4.2");
        Assert.assertNotNull(pBuc01_v42);

        String caseId = "17";
        
        RinaCase rinaCase = rinaCaseRepo.findById(caseId);
        
        if (rinaCase == null) {
            Log.error("Case not found " + caseId);
            return;
        }

        List<ActionDO> actions = getReceiveUpdateActions(rinaCase, pBuc01_v42);

        Assert.assertNotNull(actions);
        logger.info("{}", actions.stream().map(a -> a.getDocumentType().value()).collect(Collectors.joining(",")));

    }

    /**
     * Gets the created actions for the case and BUC
     * 
     * @param rinaCase
     * @param bucDefinition
     * @return
     */
    protected List<ActionDO> getReceiveUpdateActions(final RinaCase rinaCase, final Case bucDefinition) {

        List<Document> receivedDocs = x013ReceiveActionProducer.getAllX012(rinaCase.getId());

        // get the actions
        return x013ReceiveActionProducer.getReceiveActions(rinaCase.getId(), receivedDocs, bucDefinition);

    }

}

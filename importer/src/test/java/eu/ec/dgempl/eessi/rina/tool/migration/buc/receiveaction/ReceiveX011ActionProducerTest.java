package eu.ec.dgempl.eessi.rina.tool.migration.buc.receiveaction;

import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.ec.dgempl.eessi.rina.buc.api.model.ActionDO;
import eu.ec.dgempl.eessi.rina.buc.core.model.Case;
import eu.ec.dgempl.eessi.rina.buc.core.model.ECaseRole;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.buc.BucDefinitionImporterFactory;

/**
 * Tests the creation of DOC_RECEIVE actions for X011
 */
@Transactional
public class ReceiveX011ActionProducerTest extends AbstractBucProcessorTest {

    private static final Logger logger = LoggerFactory.getLogger(ReceiveX011ActionProducerTest.class);

    @Autowired
    private BucDefinitionImporterFactory bucDefinitionImporterFactory;

    @Autowired
    private X011ReceiveActionProducer x011ReceiveActionProducer;

    @Autowired
    private RinaCaseRepo rinaCaseRepo;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testPBuc01_PO() throws Exception {

        Case awBuc03_v42 = bucDefinitionImporterFactory.loadBucConfiguration("aw_buc_03", ECaseRole.PO, "4.2");
        Assert.assertNotNull(awBuc03_v42);

        RinaCase rinaCase = rinaCaseRepo.findById("4");

        List<ActionDO> actions = getReceiveUpdateActions(rinaCase, awBuc03_v42);

        Assert.assertNotNull(actions);
        logger.info("{}", getLogActions(actions));

    }

    @Test
    public void testPBuc01_CP() throws Exception {

        Case pBuc01_v42 = bucDefinitionImporterFactory.loadBucConfiguration("p_buc_01", ECaseRole.CP, "4.2");
        Assert.assertNotNull(pBuc01_v42);

        RinaCase rinaCase = rinaCaseRepo.findById("2");

        List<ActionDO> actions = getReceiveUpdateActions(rinaCase, pBuc01_v42);

        Assert.assertNotNull(actions);
        logger.info("{}", getLogActions(actions));

    }

    /**
     * Gets the created actions for the case and BUC
     * 
     * @param rinaCase
     * @param bucDefinition
     * @return
     */
    protected List<ActionDO> getReceiveUpdateActions(final RinaCase rinaCase, final Case bucDefinition) {

        // get the docs
        List<Document> receivedDocs = x011ReceiveActionProducer.getDocumentsToProcess(rinaCase, bucDefinition);

        // get the actions
        return x011ReceiveActionProducer.getReceiveActions(rinaCase.getId(), receivedDocs, bucDefinition);

    }

}

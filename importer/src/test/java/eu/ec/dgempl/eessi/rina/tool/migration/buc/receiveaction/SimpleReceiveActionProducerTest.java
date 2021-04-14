package eu.ec.dgempl.eessi.rina.tool.migration.buc.receiveaction;

import java.io.FileNotFoundException;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
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
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.buc.BucDefinitionImporterFactory;

/**
 * Tests the creation of simple receive actions creation for a case.
 */
@Transactional
public class SimpleReceiveActionProducerTest extends AbstractBucProcessorTest {

    private static final Logger logger = LoggerFactory.getLogger(SimpleReceiveActionProducerTest.class);

    @Autowired
    private BucDefinitionImporterFactory bucDefinitionImporterFactory;

    @Autowired
    private SimpleReceiveActionProducer simpleReceiveActionProducer;

    @Autowired
    private RinaCaseRepo rinaCaseRepo;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    @Ignore
    public void testDbConnection() throws Exception {

        logger.info("Testing the Spring Contetx initialization for test and DB connection");
        List<RinaCase> cases = rinaCaseRepo.findAll();
        logger.info("Found {} cases in the DB", cases.size());

        Assert.assertEquals(1, 1);

    }

    @Test
    public void testBucDefinitionLoading() throws Exception {

        Case sBuc23_v41 = bucDefinitionImporterFactory.loadBucConfiguration("s_buc_23", ECaseRole.CP, "4.1");
        Assert.assertNotNull(sBuc23_v41);

        Case pBuc01_v41 = bucDefinitionImporterFactory.loadBucConfiguration("p_buc_01", ECaseRole.PO, "4.1");
        Assert.assertNotNull(pBuc01_v41);

        Case pBuc01_v42 = bucDefinitionImporterFactory.loadBucConfiguration("p_buc_01", ECaseRole.PO, "4.2");
        Assert.assertNotNull(pBuc01_v42);

        exceptionRule.expect(FileNotFoundException.class);
        Case awBuc01a_v41 = bucDefinitionImporterFactory.loadBucConfiguration("aw_buc_01x", ECaseRole.PO, "4.1");
        Assert.assertNotNull(awBuc01a_v41);

    }

    @Test
    public void testPBuc01_PO() throws Exception {

        Case pBuc01_v42 = bucDefinitionImporterFactory.loadBucConfiguration("p_buc_01", ECaseRole.PO, "4.2");
        Assert.assertNotNull(pBuc01_v42);

        RinaCase rinaCase = rinaCaseRepo.findById("1");

        List<ActionDO> actions = simpleReceiveActionProducer.getReceiveActions(rinaCase, pBuc01_v42);
        Assert.assertNotNull(actions);
        logger.info("{}", getLogActions(actions));

        Assert.assertEquals(14, actions.size());

    }

    @Test
    public void testPBuc01_CP() throws Exception {

        Case pBuc01_v42 = bucDefinitionImporterFactory.loadBucConfiguration("p_buc_01", ECaseRole.CP, "4.2");
        Assert.assertNotNull(pBuc01_v42);

        RinaCase rinaCase = rinaCaseRepo.findById("2");

        List<ActionDO> actions = simpleReceiveActionProducer.getReceiveActions(rinaCase, pBuc01_v42);
        Assert.assertNotNull(actions);
        logger.info("{}", getLogActions(actions));

        Assert.assertEquals(17, actions.size());

    }

}

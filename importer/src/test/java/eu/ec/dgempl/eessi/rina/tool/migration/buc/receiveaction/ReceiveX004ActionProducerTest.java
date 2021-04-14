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

import eu.ec.dgempl.eessi.rina.buc.api.model.ActionDO;
import eu.ec.dgempl.eessi.rina.buc.core.model.Case;
import eu.ec.dgempl.eessi.rina.buc.core.model.ECaseRole;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.buc.BucDefinitionImporterFactory;

import clover.org.jfree.util.Log;

/**
 * Tests the creation of DOC_RECEIVE actions for X004
 */
@Transactional
public class ReceiveX004ActionProducerTest extends AbstractBucProcessorTest {

    private static final Logger logger = LoggerFactory.getLogger(ReceiveX004ActionProducerTest.class);

    @Autowired
    private BucDefinitionImporterFactory bucDefinitionImporterFactory;

    @Autowired
    private X003ReceiveActionProducer x003ReceiveActionProducer;

    @Autowired
    private RinaCaseRepo rinaCaseRepo;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testPBuc01_PO() throws Exception {

        Case ub_buc_01 = bucDefinitionImporterFactory.loadBucConfiguration("ub_buc_01", ECaseRole.CP, "4.2");
        Assert.assertNotNull(ub_buc_01);

        String caseId = "12";

        RinaCase rinaCase = rinaCaseRepo.findById(caseId);

        if (rinaCase == null) {
            Log.error("Case not found " + caseId);
            return;
        }

        List<ActionDO> actions = getReceiveUpdateActions(rinaCase, ub_buc_01);

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

        List<Document> receivedDocs = x003ReceiveActionProducer.getAllX002(rinaCase.getId());
        return x003ReceiveActionProducer.getReceiveActions(rinaCase.getId(), receivedDocs, bucDefinition);
    }
}
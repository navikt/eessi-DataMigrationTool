package eu.ec.dgempl.eessi.rina.tool.migration.buc.receiveaction;

import eu.ec.dgempl.eessi.rina.buc.api.model.ActionDO;
import eu.ec.dgempl.eessi.rina.buc.core.model.Case;
import eu.ec.dgempl.eessi.rina.buc.core.model.ECaseRole;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.buc.BucDefinitionImporterFactory;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Tests the creation of simple receive actions creation for a case.
 */
@Transactional
public class ReceiveReplyActionProducerTest extends AbstractBucProcessorTest {

    private static final Logger logger = LoggerFactory.getLogger(ReceiveReplyActionProducerTest.class);

    @Autowired
    private BucDefinitionImporterFactory bucDefinitionImporterFactory;

    @Autowired
    private ReceiveReplyActionProducer receiveReplyActionProducer;

    @Autowired
    private RinaCaseRepo rinaCaseRepo;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testAwBuc23() throws Exception {

        Case awBuc23_v42 = bucDefinitionImporterFactory.loadBucConfiguration("aw_buc_23", ECaseRole.PO, "4.2");
        Assert.assertNotNull(awBuc23_v42);

        RinaCase rinaCase = rinaCaseRepo.findById("4");

        List<ActionDO> actions = getReceiveReplyForReceivedDocsActions(rinaCase, awBuc23_v42);

        Assert.assertNotNull(actions);
        logger.info("{}", getLogActions(actions));

        actions = getReceiveReplyForSentDocsActions(rinaCase, awBuc23_v42);

        Assert.assertNotNull(actions);
        logger.info("{}", getLogActions(actions));

    }

    /**
     * Gets the created actions for received not-cancelled docs
     * 
     * @param rinaCase
     * @param bucDefinition
     * @return
     */
    protected List<ActionDO> getReceiveReplyForReceivedDocsActions(final RinaCase rinaCase, final Case bucDefinition) {

        // get the actions
        return receiveReplyActionProducer.getReceiveActionsForNotCancelledReceivedDocs(rinaCase.getId(), bucDefinition);

    }

    /**
     * Gets the created actions for sent docs
     *
     * @param rinaCase
     * @param bucDefinition
     * @return
     */
    protected List<ActionDO> getReceiveReplyForSentDocsActions(final RinaCase rinaCase, final Case bucDefinition) {

        // get the actions
        return receiveReplyActionProducer.getReceiveActionsForSentDocs(rinaCase.getId(), bucDefinition);

    }

}

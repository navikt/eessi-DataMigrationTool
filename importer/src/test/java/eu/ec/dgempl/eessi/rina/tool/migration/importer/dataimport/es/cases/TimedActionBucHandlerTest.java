package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es.cases;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.ec.dgempl.eessi.rina.buc.core.model.ECaseRole;
import eu.ec.dgempl.eessi.rina.tool.migration.buc.receiveaction.AbstractBucProcessorTest;
import eu.ec.dgempl.eessi.rina.tool.migration.buc.timedaction.TimedActionBucHandler;

@Transactional
public class TimedActionBucHandlerTest extends AbstractBucProcessorTest {

    @Autowired
    private TimedActionBucHandler timedActionBucHandler;

    @Test
    public void testCreateTimedAction() throws Exception {

        // timedActionBucHandler.processCase("37", "la_buc_03", ECaseRole.PO, "4.2");

        timedActionBucHandler.processCase("31", "la_buc_02", ECaseRole.PO, "4.2");

    }

}

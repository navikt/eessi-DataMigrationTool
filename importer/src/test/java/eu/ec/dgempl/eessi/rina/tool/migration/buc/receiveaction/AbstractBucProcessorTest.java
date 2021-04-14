package eu.ec.dgempl.eessi.rina.tool.migration.buc.receiveaction;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.ec.dgempl.eessi.rina.buc.api.model.ActionDO;
import eu.ec.dgempl.eessi.rina.tool.migration.buc.SpringContextConfiguration;

/**
 * Abstract test class for tests for
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringContextConfiguration.class)
public abstract class AbstractBucProcessorTest {

    /**
     * Returns a log string for action.
     * 
     * @param action
     * @return
     */
    public String getLogAction(ActionDO action) {

        return String.format("[docType=%s,docId=%s,parentDocId=%s,parentDocType=%s]", action.getDocumentType(), action.getDocumentId(),
                action.getParentDocumentId(), action.getParentDocumentType());

    }

    /**
     * Concatenates all actions into a single log string and returns.
     * 
     * @param actions
     */
    public String getLogActions(List<ActionDO> actions) {

        return actions.stream().map(a -> getLogAction(a)).collect(Collectors.joining(","));

    }

}

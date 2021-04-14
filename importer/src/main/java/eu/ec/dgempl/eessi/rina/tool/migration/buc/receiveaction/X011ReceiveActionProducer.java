package eu.ec.dgempl.eessi.rina.tool.migration.buc.receiveaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.buc.api.model.ActionDO;
import eu.ec.dgempl.eessi.rina.buc.core.model.EDocumentParameterType;
import eu.ec.dgempl.eessi.rina.buc.core.model.EDocumentType;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EDocumentStatus;

/**
 * Processes and creates {@link ActionDO} objects for RECEIVE action and document type X011 for every document, that has been sent and
 * hasReject=true
 */
@Component
public class X011ReceiveActionProducer extends AbstractByStatusAndParameterReceiveActionProducer {

    private static final Logger logger = LoggerFactory.getLogger(X011ReceiveActionProducer.class);

    @Override
    protected EDocumentStatus getDocumentStatus() {
        return EDocumentStatus.SENT;
    }

    @Override
    protected EDocumentParameterType getDocumentParameter() {
        return EDocumentParameterType.HAS_REJECT;
    }

    @Override
    protected EDocumentType getDocumentTypeForReceiveAction() {
        return EDocumentType.X_011;
    }
}

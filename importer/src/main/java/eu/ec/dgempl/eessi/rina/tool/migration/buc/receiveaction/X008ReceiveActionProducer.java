package eu.ec.dgempl.eessi.rina.tool.migration.buc.receiveaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.buc.api.model.ActionDO;
import eu.ec.dgempl.eessi.rina.buc.core.model.EDocumentParameterType;
import eu.ec.dgempl.eessi.rina.buc.core.model.EDocumentType;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EDocumentStatus;

/**
 * Processes and creates {@link ActionDO} objects for RECEIVE action and document type X008 for every document, that has been received and
 * hasCancel=true
 */
@Component
public class X008ReceiveActionProducer extends AbstractByStatusAndParameterReceiveActionProducer {

    private static final Logger logger = LoggerFactory.getLogger(X008ReceiveActionProducer.class);

    @Override
    protected EDocumentStatus getDocumentStatus() {
        return EDocumentStatus.RECEIVED;
    }

    @Override
    protected EDocumentParameterType getDocumentParameter() {
        return EDocumentParameterType.HAS_CANCEL;
    }

    @Override
    protected EDocumentType getDocumentTypeForReceiveAction() {
        return EDocumentType.X_008;
    }
}

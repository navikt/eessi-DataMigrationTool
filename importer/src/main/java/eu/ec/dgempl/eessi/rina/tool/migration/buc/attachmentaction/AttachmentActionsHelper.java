package eu.ec.dgempl.eessi.rina.tool.migration.buc.attachmentaction;

import java.util.UUID;

import eu.ec.dgempl.eessi.rina.buc.api.model.ActionDO;
import eu.ec.dgempl.eessi.rina.buc.core.model.EActionStatus;
import eu.ec.dgempl.eessi.rina.buc.core.model.EActionType;
import eu.ec.dgempl.eessi.rina.buc.core.model.EDocumentType;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;

public class AttachmentActionsHelper {

    public static ActionDO createDefaultAttachmentAction(String caseId, EDocumentType documentType, EActionType actionType) {

        PreconditionsHelper.notEmpty(caseId, "caseId");
        PreconditionsHelper.notNull(documentType, "documentType");

        ActionDO actionDO = new ActionDO();
        actionDO.setId(UUID.randomUUID().toString());
        actionDO.setStatus(EActionStatus.ACTIVE);
        actionDO.setType(actionType);
        actionDO.setCanClose(false);
        actionDO.setCaseId(caseId);
        actionDO.setDocumentType(documentType);

        return actionDO;
    }
}

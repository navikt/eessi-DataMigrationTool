package eu.ec.dgempl.eessi.rina.tool.migration.buc.attachmentaction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.buc.api.model.ActionDO;
import eu.ec.dgempl.eessi.rina.buc.api.utils.BucDefinitionHelper;
import eu.ec.dgempl.eessi.rina.buc.core.model.Case;
import eu.ec.dgempl.eessi.rina.buc.core.model.EActionStatus;
import eu.ec.dgempl.eessi.rina.buc.core.model.EActionType;
import eu.ec.dgempl.eessi.rina.buc.core.model.ECaseRole;
import eu.ec.dgempl.eessi.rina.buc.core.model.EDocumentParameterType;
import eu.ec.dgempl.eessi.rina.buc.core.model.EDocumentType;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ECaseStatus;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EDocumentStatus;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.repo.DocumentRepoExtended;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.buc.BucDefinitionImporterFactory;
import eu.ec.dgempl.eessi.rina.tool.migration.buc.BucHandler;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.ActionService;

@Component
public class AttachmentActionBucHandler implements BucHandler {

    private static final Logger logger = LoggerFactory.getLogger(AttachmentActionBucHandler.class);

    private final DocumentRepoExtended documentRepo;

    private final RinaCaseRepo rinaCaseRepo;

    @Autowired
    private ActionService actionService;

    @Autowired
    private BucDefinitionImporterFactory bucDefinitionFactory;

    private final List<EDocumentStatus> ALLOWED_DOCUMENT_STATUSES = List.of(
            EDocumentStatus.ACTIVE,
            EDocumentStatus.NEW,
            EDocumentStatus.SENT);

    public AttachmentActionBucHandler(final DocumentRepoExtended documentRepo, final RinaCaseRepo rinaCaseRepo) {
        this.documentRepo = documentRepo;
        this.rinaCaseRepo = rinaCaseRepo;
    }

    @Override
    public void processCase(final String caseId, final String bucType, final ECaseRole caseRole, final String modelVersion)
            throws Exception {

        PreconditionsHelper.notEmpty(caseId, "caseId");

        logger.debug("Processing ATTACHMENT actions for the case [caseId={}]", caseId);

        RinaCase rinaCase = rinaCaseRepo.findById(caseId);

        if (ECaseStatus.REMOVED == rinaCase.getStatus()) {
            return;
        }

        // build the BUC definition object
        final Case buc = bucDefinitionFactory.loadBucConfiguration(bucType, caseRole, modelVersion);

        List<Document> docs = getActiveDocuments(rinaCase);

        if (CollectionUtils.isNotEmpty(docs)) {
            List<ActionDO> attachmentActions = getAttachmentActions(rinaCase, docs, buc);

            attachmentActions.forEach(actionService::saveAction);

        }
    }

    private List<ActionDO> getAttachmentActions(final RinaCase rinaCase, final List<Document> docs, final Case buc) {

        List<ActionDO> attachmentActions = new ArrayList<>();

        for (Document doc : docs) {

            // get document type
            EDocumentType docType = EDocumentType.fromValue(doc.getDocumentTypeVersion().getDocumentType().getType());

            // get BUC document definition
            eu.ec.dgempl.eessi.rina.buc.core.model.Document bucDocument = BucDefinitionHelper.getDocumentDefinition(docType, buc);

            if (allowsAttachments(bucDocument)) {
                EActionStatus actionStatus = getActionStatus(rinaCase.getStatus());
                String caseId = rinaCase.getId();

                attachmentActions.add(createAddAttachmentAction(caseId, actionStatus, doc, bucDocument));
                attachmentActions.add(createRemoveAttachmentAction(caseId, actionStatus, doc, bucDocument));
            }
        }

        return attachmentActions;
    }

    private ActionDO createAddAttachmentAction(
            final String caseId,
            final EActionStatus actionStatus,
            final Document doc,
            final eu.ec.dgempl.eessi.rina.buc.core.model.Document bucDocument) {

        return createAttachmentAction(caseId, actionStatus, doc, bucDocument, EActionType.ADD_ATTACHMENT);
    }

    private ActionDO createRemoveAttachmentAction(
            final String caseId,
            final EActionStatus actionStatus,
            final Document doc,
            final eu.ec.dgempl.eessi.rina.buc.core.model.Document bucDocument) {

        return createAttachmentAction(caseId, actionStatus, doc, bucDocument, EActionType.REMOVE_ATTACHMENT);
    }

    private ActionDO createAttachmentAction(
            final String caseId,
            final EActionStatus actionStatus,
            final Document doc,
            final eu.ec.dgempl.eessi.rina.buc.core.model.Document bucDocument,
            final EActionType actionType) {

        ActionDO actionDO = AttachmentActionsHelper.createDefaultAttachmentAction(caseId, bucDocument.getType(), actionType);
        actionDO.setDocumentId(doc.getId());
        actionDO.setStatus(actionStatus);

        if (doc.getParent() != null) {
            actionDO.setParentDocumentId(doc.getParent().getId());
            actionDO.setParentDocumentType(
                    EDocumentType.fromValue(doc.getParent().getDocumentTypeVersion().getDocumentType().getType()));
        }

        // process P3000
        processP3000(doc, actionDO);

        return actionDO;
    }

    private List<Document> getActiveDocuments(final RinaCase rinaCase) {

        List<Document> documents = documentRepo.findByRinaCaseId(rinaCase.getId());

        return documents
                .stream()
                .filter(d -> ALLOWED_DOCUMENT_STATUSES.contains(d.getStatus()))
                .collect(Collectors.toList());
    }

    private boolean allowsAttachments(final eu.ec.dgempl.eessi.rina.buc.core.model.Document doc) {

        if (doc != null && doc.getParameters() != null) {
            String allowsAttachments = BucDefinitionHelper.getDocumentParameterByKey(EDocumentParameterType.ALLOWS_ATTACHMENTS,
                    doc.getParameters().getParameter());

            if (StringUtils.isNotBlank(allowsAttachments)) {
                return Boolean.valueOf(allowsAttachments);
            }
        }

        return false;
    }

    /**
     * If the document type if P3000, will be change according to the country
     *
     * @param dbDocument
     * @param action
     */
    protected void processP3000(Document dbDocument, final ActionDO action) {

        if (EDocumentType.P_3000.equals(action.getDocumentType())) {

            EDocumentType docType = EDocumentType.fromValue(dbDocument.getDocumentTypeVersion().getDocumentType().getType());
            action.setDocumentType(docType);

        }
    }

    private EActionStatus getActionStatus(final ECaseStatus status) {

        if (ECaseStatus.CLOSED == status) {
            return EActionStatus.SUSPENDED;
        }

        return EActionStatus.ACTIVE;
    }

}

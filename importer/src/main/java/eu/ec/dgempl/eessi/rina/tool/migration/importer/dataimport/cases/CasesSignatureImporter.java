package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.cases;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils.*;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EDocumentStatus;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentBversion;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentConversation;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Signature;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.UserMessage;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.DocumentBversionRepo;
import eu.ec.dgempl.eessi.rina.repo.DocumentConversationRepo;
import eu.ec.dgempl.eessi.rina.repo.DocumentRepo;
import eu.ec.dgempl.eessi.rina.repo.SignatureRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.CaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.CaseSignatureFields;

@Component
@ElasticTypeImporter(type = EElasticType.CASES_SIGNATURE)
public class CasesSignatureImporter extends AbstractDataImporter implements CaseImporter {

    private final DocumentRepo documentRepo;
    private final DocumentBversionRepo documentBversionRepo;
    private final DocumentConversationRepo documentConversationRepo;
    private final SignatureRepo signatureRepo;

    public CasesSignatureImporter(
            final DocumentRepo documentRepo,
            final DocumentBversionRepo documentBversionRepo,
            final DocumentConversationRepo documentConversationRepo,
            final SignatureRepo signatureRepo) {
        this.documentRepo = documentRepo;
        this.documentBversionRepo = documentBversionRepo;
        this.documentConversationRepo = documentConversationRepo;
        this.signatureRepo = signatureRepo;
    }

    @Override
    public DocumentsReport importData(final String caseId) {
        return run(this::processCaseSignatureData, caseId);
    }

    private void processCaseSignatureData(final MapHolder doc) {

        UserMessage userMessage = getUserMessage(doc);

        if (userMessage != null) {
            Signature signature = beanMapper.map(doc, Signature.class);
            signature.setUserMessage(userMessage);
            signatureRepo.saveAndFlush(signature);
        } else {
            setAndSaveSignatureForAllUserMessages(doc);
        }
    }

    private UserMessage getUserMessage(final MapHolder doc) {
        String caseId = doc.string(CaseSignatureFields.CASE_ID);
        String targetSedId = doc.string(CaseSignatureFields.TARGET_SED_ID);
        String messageId = doc.string(CaseSignatureFields.MESSAGE_ID);

        Document document = documentRepo.findByIdAndRinaCaseId(targetSedId, caseId);
        if (document == null) {
            throw new EntityNotFoundEessiRuntimeException(
                    Document.class,
                    UniqueIdentifier.id, targetSedId,
                    UniqueIdentifier.caseId, caseId);
        }

        return document.getDocumentConversations().stream()
                .map(DocumentConversation::getUserMessages)
                .flatMap(List::stream)
                .filter(message -> message.getId().equals(messageId))
                .findFirst()
                .orElse(null);
    }

    private void setAndSaveSignatureForAllUserMessages(MapHolder doc) {
        DocumentConversation documentConversation = getDocumentConversation(doc);

        if (documentConversation == null) {
            throw new RuntimeException(String.format(
                    "Could not find the referenced conversations.userMessage for caseId=%s and documentId=%s",
                    doc.string(CaseSignatureFields.CASE_ID),
                    doc.string(CaseSignatureFields.TARGET_SED_ID)));
        }

        List<Signature> signatures = documentConversation.getUserMessages()
                .stream()
                .map(userMessage -> {
                    Signature signature = beanMapper.map(doc, Signature.class);
                    signature.setUserMessage(userMessage);
                    return signature;
                })
                .collect(Collectors.toList());

        saveInBulk(() -> signatures, () -> signatureRepo);
    }

    private DocumentConversation getDocumentConversation(MapHolder doc) {
        String targetSedId = doc.string(CaseSignatureFields.TARGET_SED_ID);
        String caseId = doc.string(CaseSignatureFields.CASE_ID);
        String versionId = doc.string(CaseSignatureFields.VERSION_ID);

        DocumentConversation documentConversation = null;

        Document document = documentRepo.findByIdAndRinaCaseId(targetSedId, caseId);

        if (document == null) {
            throw new EntityNotFoundEessiRuntimeException(
                    Document.class,
                    UniqueIdentifier.id, targetSedId,
                    UniqueIdentifier.caseId, caseId);
        }

        if (StringUtils.isNotBlank(versionId)) {
            documentConversation = getDocumentConversationByVersionId(document, Integer.parseInt(versionId));
        }

        if (documentConversation == null) {
            documentConversation = getDocumentConversation(document, document.getDocumentConversations());
        }

        return documentConversation;
    }

    private DocumentConversation getDocumentConversationByVersionId(Document document, int versionId) {
        DocumentBversion documentBversion = documentBversionRepo.findByDocumentIdAndDocumentRinaCaseIdAndId(
                document.getId(),
                document.getRinaCase().getId(),
                versionId);

        if (documentBversion != null) {
            List<DocumentConversation> byDocumentBversion = documentConversationRepo.findByDocumentBversion(documentBversion);
            return getDocumentConversation(document, byDocumentBversion);
        }

        return null;
    }

    @Nullable
    private DocumentConversation getDocumentConversation(Document document, List<DocumentConversation> documentConversations) {
        DocumentConversation documentConversation;
        if (EDocumentStatus.SENT == document.getStatus() || EDocumentStatus.RECEIVED == document.getStatus()) {
            documentConversation = getLastSentOrReceivedConversation(documentConversations);
        } else {
            documentConversation = documentRepo.getLastConversationByCaseAndDocument(document.getRinaCase().getId(), document.getId());
        }
        return documentConversation;
    }

    private DocumentConversation getLastSentOrReceivedConversation(List<DocumentConversation> documentConversations) {
        if (CollectionUtils.isNotEmpty(documentConversations)) {
            for (int i = documentConversations.size() - 1; i >= 0; i--) {
                DocumentConversation conversation = documentConversations.get(i);
                if (conversation.getDate() == null && conversation.getReceivedDate() == null)
                    continue;
                return conversation;
            }
        }
        return null;
    }
}

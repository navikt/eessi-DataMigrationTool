package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.cases;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper._abstract.AbstractLocalMapper.*;
import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.DateUtils.*;
import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils.*;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.commons.date.ZonedDateTimePeriod;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EUserMessageDirection;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentAttachment;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentBversion;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentConversation;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.UserMessage;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.UserMessageResponse;
import eu.ec.dgempl.eessi.rina.repo.DocumentAttachmentRepo;
import eu.ec.dgempl.eessi.rina.repo.DocumentBversionRepo;
import eu.ec.dgempl.eessi.rina.repo.DocumentConversationRepo;
import eu.ec.dgempl.eessi.rina.repo.DocumentRepo;
import eu.ec.dgempl.eessi.rina.repo.UserMessageRepo;
import eu.ec.dgempl.eessi.rina.repo.UserMessageResponseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.CaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.DocumentFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.MapUtils;

@Component
@ElasticTypeImporter(type = EElasticType.CASES_DOCUMENT)
public class DocumentImporter extends AbstractDataImporter implements CaseImporter {

    private final DocumentRepo documentRepo;
    private final DocumentAttachmentRepo documentAttachmentRepo;
    private final DocumentBversionRepo documentBversionRepo;
    private final DocumentConversationRepo documentConversationRepo;
    private final UserMessageResponseRepo userMessageResponseRepo;
    private final UserMessageRepo userMessageRepo;

    private final Logger logger = LoggerFactory.getLogger(DocumentImporter.class);

    private final List<String> UNSUPPORTED_DOCUMENT_TYPES = List.of("DummyChooseParts");

    public DocumentImporter(final DocumentRepo documentRepo, final DocumentAttachmentRepo documentAttachmentRepo,
            final DocumentBversionRepo documentBversionRepo, final DocumentConversationRepo documentConversationRepo,
            final UserMessageResponseRepo userMessageResponseRepo, UserMessageRepo userMessageRepo) {
        this.documentRepo = documentRepo;
        this.documentAttachmentRepo = documentAttachmentRepo;
        this.documentBversionRepo = documentBversionRepo;
        this.documentConversationRepo = documentConversationRepo;
        this.userMessageResponseRepo = userMessageResponseRepo;
        this.userMessageRepo = userMessageRepo;
    }

    @Override
    public DocumentsReport importData(final String caseId) {
        return run(this::importDocumentData, caseId);
    }

    private void importDocumentData(final MapHolder doc) {
        String docType = doc.string(DocumentFields.TYPE);

        if (UNSUPPORTED_DOCUMENT_TYPES.contains(docType)) {
            doc.visitAll();
            logger.warn("Found dummy documents in MultiStarter context for case: {}, docId: {}", doc.string(DocumentFields.CASE_ID),
                    doc.string(DocumentFields.ID));
            return;
        }

        Document document = beanMapper.map(doc, Document.class);

        mapDocumentBversions(doc, document);

        documentRepo.saveAndFlush(document);

        updateUserMessagesDirection(document, document.getDocumentConversations());

        List<UserMessageResponse> userMessageResponses = updateUserMessagesUserMessageResponseAndGet(document);

        saveInBulk(document::getDocumentConversations, () -> documentConversationRepo);

        saveInBulk(() -> userMessageResponses, () -> userMessageResponseRepo);

        saveInBulk(() -> setUserMessageResponseAndGet(userMessageResponses), () -> userMessageRepo);

        saveInBulk(document::getDocumentBversions, () -> documentBversionRepo);

        setDocumentBversionOnDocumentConversations(doc, document);

        mapDocumentBversion(doc, document);
        mapAttachments(doc);
    }

    private List<UserMessage> setUserMessageResponseAndGet(List<UserMessageResponse> userMessageResponses) {
        //@formatter:off
        return userMessageResponses.stream()
                .peek(umr -> umr.getUserMessage().setUserMessageResponse(umr))
                .map(UserMessageResponse::getUserMessage)
                .collect(Collectors.toList());
        //@formatter:on
    }

    private void setDocumentBversionOnDocumentConversations(final MapHolder doc, final Document document) {
        Map<String, Optional<String>> conversationsVersionId = doc.listToMapHolder(DocumentFields.CONVERSATIONS).stream()
                .collect(Collectors.toMap(conversation -> conversation.string(DocumentFields.CONVERSATION_ID),
                        conversation -> Optional.ofNullable(conversation.string(DocumentFields.VERSION_ID))));

        for (DocumentConversation documentConversation : document.getDocumentConversations()) {
            String versionId = conversationsVersionId.get(documentConversation.getId()).orElse(null);

            Optional<DocumentBversion> documentBversion;

            if (StringUtils.isNotBlank(versionId)) {
                documentBversion = getDocumentBversion(document.getDocumentBversions(), versionId);

                if (documentBversion.isEmpty()) {
                    documentBversion = getLastBversion(document.getDocumentBversions());
                }

            } else {
                documentBversion = getLastBversion(document.getDocumentBversions());
            }

            documentBversion.ifPresent(documentConversation::setDocumentBversion);
        }

        saveInBulk(document::getDocumentConversations, () -> documentConversationRepo);
    }

    private List<UserMessageResponse> updateUserMessagesUserMessageResponseAndGet(final Document document) {
        return document.getDocumentConversations().stream().map(DocumentConversation::getUserMessages).flatMap(List::stream)
                .map(userMessage -> {
                    UserMessageResponse userMessageResponse = userMessage.getUserMessageResponse();
                    userMessage.setUserMessageResponse(null);
                    return userMessageResponse;
                }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private void updateUserMessagesDirection(final Document document, final List<DocumentConversation> documentConversations) {
        documentConversations.stream().map(DocumentConversation::getUserMessages).flatMap(List::stream)
                .forEach(userMessage -> userMessage.setDirection(EUserMessageDirection.fromString(document.getDirection().name())));
    }

    private void mapDocumentBversions(final MapHolder doc, final Document document) {
        List<MapHolder> versions = doc.listToMapHolder(DocumentFields.VERSIONS);
        if (CollectionUtils.isNotEmpty(versions)) {
            versions.stream().map(version -> beanMapper.map(version, DocumentBversion.class, mctxb().addProp("doc", document).build()))
                    .filter(MapUtils.distinctByKey(x -> Arrays.asList(x.getId(), x.getAudit().getCreatedAt(), x.getAudit().getCreatedBy())))
                    .forEach(document::addDocumentBversion);

            List<DocumentBversion> bversions = document.getDocumentBversions();

            Optional<DocumentBversion> documentBversion = bversions.stream()
                    .min(Comparator.comparing(bversion -> bversion.getAudit().getCreatedAt()));

            documentBversion.ifPresent(bversion -> document.getAudit().setCreatedAt(bversion.getAudit().getCreatedAt()));

            documentBversion = getLastBversion(bversions);

            documentBversion.ifPresent(bversion -> document.getAudit().setUpdatedAt(bversion.getAudit().getCreatedAt()));
        }

    }

    private void mapDocumentBversion(final MapHolder doc, final Document document) {
        String documentVersionId = doc.string(DocumentFields.VERSION_ID);
        Optional<DocumentBversion> bversion = Optional.empty();
        final List<DocumentBversion> bversions = document.getDocumentBversions();

        if (StringUtils.isNotBlank(documentVersionId)) {
            bversion = getDocumentBversion(bversions, documentVersionId);
        }

        if (bversion.isEmpty()) {
            bversion = getLastBversion(bversions);
        }

        bversion.ifPresent(document::setDocumentBversion);
        documentRepo.saveAndFlush(document);
    }

    @NotNull
    private Optional<DocumentBversion> getLastBversion(final List<DocumentBversion> bversions) {
        return bversions.stream().max(Comparator.comparing(documentBversion -> documentBversion.getAudit().getCreatedAt()));
    }

    @NotNull
    private Optional<DocumentBversion> getDocumentBversion(final List<DocumentBversion> bversions, final String documentVersion) {
        return bversions.stream().filter(version -> version.getId() == Integer.parseInt(documentVersion)).findFirst();
    }

    private void mapAttachments(final MapHolder a) {
        List<MapHolder> attachments = a.listToMapHolder(DocumentFields.ATTACHMENTS);
        if (CollectionUtils.isNotEmpty(attachments)) {
            List<DocumentBversion> documentBversions = attachments.stream().map(attachment -> {
                DocumentAttachment documentAttachment = beanMapper.map(attachment, DocumentAttachment.class);
                return getVersions(attachment, documentAttachment);
            }).flatMap(List::stream).collect(Collectors.toList());

            saveInBulk(() -> documentBversions, () -> documentBversionRepo);
        }
    }

    private List<DocumentBversion> getVersions(final MapHolder a, final DocumentAttachment b) {
        List<DocumentBversion> documentBversions = b.getDocument().getDocumentBversions();
        List<MapHolder> versions = a.listToMapHolder(DocumentFields.ATTACHMENT_VERSIONS);

        documentAttachmentRepo.saveAndFlush(b);

        if (CollectionUtils.isNotEmpty(versions)) {
            Map<ZonedDateTimePeriod, Integer> intervalPairs = getIntervalsMap(documentBversions);
            versions.forEach(version -> {
                ZonedDateTime creationDate = parseDate(version.string("date"));
                int intervalIndex = getIntervalIndex(intervalPairs, creationDate);
                if (intervalIndex > -1) {
                    for (int idx = intervalIndex; idx < documentBversions.size(); idx++) {
                        documentBversions.get(idx).addDocumentAttachment(b);
                    }
                }
            });
        } else {
            documentBversions.forEach(documentBversion -> documentBversion.addDocumentAttachment(b));
        }

        return documentBversions;
    }

    @Override
    public boolean processesEmptyCase() {
        return true;
    }
}

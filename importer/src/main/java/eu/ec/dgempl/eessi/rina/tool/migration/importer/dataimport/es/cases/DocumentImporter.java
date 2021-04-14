package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es.cases;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EUserMessageDirection;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentAttachment;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentBversion;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentConversation;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.UserMessage;
import eu.ec.dgempl.eessi.rina.repo.DocumentBversionRepo;
import eu.ec.dgempl.eessi.rina.repo.DocumentConversationRepo;
import eu.ec.dgempl.eessi.rina.repo.DocumentRepo;
import eu.ec.dgempl.eessi.rina.repo.UserMessageResponseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.CaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.DocumentFields;

@Component
@ElasticTypeImporter(type = EElasticType.CASES_DOCUMENT)
public class DocumentImporter extends AbstractDataImporter implements CaseImporter {

    private final DocumentRepo documentRepo;
    private final DocumentBversionRepo documentBversionRepo;
    private final DocumentConversationRepo documentConversationRepo;
    private final UserMessageResponseRepo userMessageResponseRepo;

    public DocumentImporter(
            final DocumentRepo documentRepo,
            final DocumentBversionRepo documentBversionRepo,
            final DocumentConversationRepo documentConversationRepo,
            final UserMessageResponseRepo userMessageResponseRepo) {
        this.documentRepo = documentRepo;
        this.documentBversionRepo = documentBversionRepo;
        this.documentConversationRepo = documentConversationRepo;
        this.userMessageResponseRepo = userMessageResponseRepo;
    }

    @Override
    public void importData(final String caseId) {
        run(this::importDocumentData, caseId);
    }

    private void importDocumentData(final MapHolder doc) {
        Document document = beanMapper.map(doc, Document.class);

        documentRepo.saveAndFlush(document);
        List<DocumentConversation> documentConversations = document.getDocumentConversations();
        documentConversations
                .stream()
                .map(DocumentConversation::getUserMessages)
                .flatMap(List::stream)
                .forEach(userMessage -> userMessage.setDirection(EUserMessageDirection.fromString(document.getDirection().name())));

        saveInBulk(() -> documentConversations, () -> documentConversationRepo);
        saveInBulk(
                () -> documentConversations
                        .stream()
                        .map(DocumentConversation::getUserMessages)
                        .flatMap(List::stream)
                        .map(UserMessage::getUserMessageResponse)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()),
                () -> userMessageResponseRepo);

        mapDocumentBversions(doc, document);
        mapAttachments(doc);
    }

    private void mapDocumentBversions(final MapHolder doc, final Document document) {
        saveAndGetDocumentBversions(doc, document);
        mapDocumentBversion(doc, document);
    }

    private void saveAndGetDocumentBversions(final MapHolder doc, final Document document) {
        List<MapHolder> versions = doc.listToMapHolder(DocumentFields.VERSIONS);
        if (CollectionUtils.isNotEmpty(versions)) {
            versions.stream()
                    .map(version -> beanMapper.map(version, DocumentBversion.class, mctxb().addProp("doc", document).build()))
                    .forEach(document::addDocumentBversion);

            saveInBulk(document::getDocumentBversions, () -> documentBversionRepo);
        }
    }

    private void mapDocumentBversion(final MapHolder doc, final Document document) {
        String documentVersion = doc.string(DocumentFields.VERSION_ID);
        Optional<DocumentBversion> bversion = Optional.empty();
        final List<DocumentBversion> bversions = document.getDocumentBversions();

        if (StringUtils.isNotBlank(documentVersion)) {
            bversion = bversions.stream().filter(version -> version.getId() == Integer.parseInt(documentVersion)).findFirst();
        }

        if (!bversion.isPresent()) {
            bversion = bversions.stream().max(Comparator.comparing(documentBversion -> documentBversion.getAudit().getCreatedAt()));
        }

        bversion.ifPresent(document::setDocumentBversion);
        documentRepo.saveAndFlush(document);
    }

    private void mapAttachments(final MapHolder a) {
        List<MapHolder> attachments = a.listToMapHolder(DocumentFields.ATTACHMENTS);
        if (CollectionUtils.isNotEmpty(attachments)) {
            List<DocumentBversion> documentBversions = attachments.stream()
                    .map(attachment -> {
                        DocumentAttachment documentAttachment = beanMapper.map(attachment, DocumentAttachment.class);
                        return getVersions(attachment, documentAttachment);
                    })
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            saveInBulk(() -> documentBversions, () -> documentBversionRepo);
        }
    }

    private List<DocumentBversion> getVersions(final MapHolder a, final DocumentAttachment b) {
        List<MapHolder> versions = a.listToMapHolder(DocumentFields.ATTACHMENT_VERSIONS);
        if (CollectionUtils.isNotEmpty(versions)) {
            return versions.stream()
                    .map(version -> Integer.valueOf(version.string(DocumentFields.ATTACHMENT_VERSION_ID)))
                    .map(id -> documentBversionRepo.findByDocumentAndId(b.getDocument(), id))
                    .peek(bversion -> bversion.addDocumentAttachment(b))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public boolean processesEmptyCase() {
        return true;
    }
}

package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EMimeType;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentAttachment;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.DocumentRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.DmtEnumNotFoundException;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.DocumentFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.helper.AttachmentsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.UserService;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToDocumentAttachmentMapper extends AbstractMapToEntityMapper<MapHolder, DocumentAttachment> {

    private final DocumentRepo documentRepo;
    private final UserService userService;

    @Autowired
    private AttachmentsHelper attachmentsHelper;

    public MapToDocumentAttachmentMapper(final DocumentRepo documentRepo,
            final UserService userService) {
        this.documentRepo = documentRepo;
        this.userService = userService;
    }

    @Override
    public void mapAtoB(final MapHolder a, final DocumentAttachment b, final MappingContext context) {
        mapDocument(a, b);
        mapAudit(a, b);

        b.setFilename(a.string(DocumentFields.FILE_NAME));
        b.setId(a.string(DocumentFields.ATTACHMENT_ID));
        b.setMedical(a.bool(DocumentFields.MEDICAL, Boolean.FALSE));
        b.setName(a.string(DocumentFields.ATTACHMENT_NAME));

        mapMimeType(a, b);
        mapPathname(a, b);
    }

    private void mapMimeType(MapHolder a, DocumentAttachment b) {
        String mimeType = a.string(DocumentFields.ATTACHMENT_MIME_TYPE);
        EMimeType eMimeType = EMimeType.lookup(mimeType).orElseThrow(
                () -> new DmtEnumNotFoundException(EMimeType.class, a.addPath(DocumentFields.MIME_TYPE), mimeType)
        );
        b.setMimeType(eMimeType);
    }

    private void mapPathname(final MapHolder a, final DocumentAttachment b) {
        String caseId = a.string(DocumentFields.CASE_ID);
        String attachmentId = a.string(DocumentFields.ATTACHMENT_ID);
        b.setPathname(attachmentsHelper.getAttachmentPathname(caseId, attachmentId));
    }

    private void mapAudit(final MapHolder a, final DocumentAttachment b) {
        setDefaultAudit(b::setAudit);

        mapDate(a, DocumentFields.ATTACHMENT_CREATION_DATE, b.getAudit()::setCreatedAt);
        mapDate(a, DocumentFields.ATTACHMENT_LAST_UPDATE, b.getAudit()::setUpdatedAt);

        String creatorId = a.string(DocumentFields.ATTACHMENT_CREATOR_ID, true);
        String creatorName = a.string(DocumentFields.ATTACHMENT_CREATOR_NAME, true);
        RinaCase rinaCase = null;
        if (b.getDocument() != null && b.getDocument().getRinaCase() != null) {
            rinaCase = b.getDocument().getRinaCase();
        }
        IamUser creator = userService.resolveUser(creatorId, creatorName, rinaCase);

        b.getAudit().setCreatedBy(creator.getId());
        b.getAudit().setUpdatedBy(creator.getId());

    }

    private void mapDocument(final MapHolder a, final DocumentAttachment b) {
        String caseId = a.string(DocumentFields.CASE_ID);
        String documentId = a.string(DocumentFields.DOCUMENT_ID);

        Document document = documentRepo.findByIdAndRinaCaseId(documentId, caseId);

        if (document == null) {
            throw new EntityNotFoundEessiRuntimeException(Document.class, UniqueIdentifier.id, documentId, UniqueIdentifier.caseId, caseId);
        }

        b.setDocument(document);
    }
}

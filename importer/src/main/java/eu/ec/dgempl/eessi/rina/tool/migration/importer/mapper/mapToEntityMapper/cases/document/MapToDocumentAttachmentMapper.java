package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.document;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EMimeType;
import eu.ec.dgempl.eessi.rina.model.exception.runtime.enums.EnumNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentAttachment;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.DocumentRepo;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.DocumentFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.helper.AttachmentsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToDocumentAttachmentMapper extends AbstractMapToEntityMapper<MapHolder, DocumentAttachment> {

    private final DocumentRepo documentRepo;
    private final IamUserRepo iamUserRepo;

    @Autowired
    private AttachmentsHelper attachmentsHelper;

    public MapToDocumentAttachmentMapper(final DocumentRepo documentRepo, final IamUserRepo iamUserRepo) {
        this.documentRepo = documentRepo;
        this.iamUserRepo = iamUserRepo;
    }

    @Override
    public void mapAtoB(final MapHolder a, final DocumentAttachment b, final MappingContext context) {
        mapDocument(a, b);
        mapAudit(a, b);

        b.setFilename(a.string(DocumentFields.FILE_NAME));
        b.setId(a.string(DocumentFields.ATTACHMENT_ID));
        b.setMedical(a.bool(DocumentFields.MEDICAL, Boolean.FALSE));
        b.setMimeType(EMimeType.lookup(a.string(DocumentFields.ATTACHMENT_MIME_TYPE)).orElseThrow(EnumNotFoundEessiRuntimeException::new));
        b.setName(a.string(DocumentFields.ATTACHMENT_NAME));

        mapPathname(a, b);
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
        IamUser creator = findById(creatorId, iamUserRepo::findById, IamUser.class);
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

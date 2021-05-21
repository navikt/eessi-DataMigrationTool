package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.comment;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentComment;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.Audit;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.DocumentRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.CommentFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.UserService;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToDocumentCommentMapper extends AbstractMapToEntityMapper<MapHolder, DocumentComment> {

    private final DocumentRepo documentRepo;
    private final UserService userService;

    public MapToDocumentCommentMapper(final DocumentRepo documentRepo,
            final UserService userService) {
        this.documentRepo = documentRepo;
        this.userService = userService;
    }

    @Override
    public void mapAtoB(final MapHolder a, final DocumentComment b, final MappingContext context) {
        mapDocument(a, b);
        mapAudit(a, b);

        b.setId(a.string(CommentFields.ID));
        b.setText(a.string(CommentFields.COMMENT));
    }

    private void mapAudit(final MapHolder a, final DocumentComment b) {
        Audit audit = new Audit();

        mapDate(a, CommentFields.DATE, audit::setCreatedAt);
        mapDate(a, CommentFields.DATE, audit::setUpdatedAt);

        String creatorId = a.string(CommentFields.CREATOR_ID, true);
        String creatorName = a.string(CommentFields.CREATOR_NAME, true);
        RinaCase rinaCase = null;
        if (b.getDocument() != null && b.getDocument().getRinaCase() != null) {
            rinaCase = b.getDocument().getRinaCase();
        }
        IamUser creator = userService.resolveUser(creatorId, creatorName, rinaCase);

        audit.setCreatedBy(creator.getId());
        audit.setUpdatedBy(creator.getId());

        b.setAudit(audit);
    }

    private void mapDocument(final MapHolder a, final DocumentComment b) {
        String caseId = a.string(CommentFields.CASE_ID);
        String documentId = a.string(CommentFields.DOCUMENT_ID);

        Document document = documentRepo.findByIdAndRinaCaseId(documentId, caseId);
        if (document == null) {
            throw new EntityNotFoundEessiRuntimeException(Document.class, UniqueIdentifier.id, documentId, UniqueIdentifier.caseId, caseId);
        }

        b.setDocument(document);
    }
}

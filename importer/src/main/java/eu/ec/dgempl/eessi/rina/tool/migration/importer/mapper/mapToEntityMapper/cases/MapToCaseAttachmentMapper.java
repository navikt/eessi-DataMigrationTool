package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EMimeType;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.CaseAttachment;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.Audit;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.DmtEnumNotFoundException;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.CaseAttachmentFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.helper.AttachmentsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.UserService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToCaseAttachmentMapper extends AbstractMapToEntityMapper<MapHolder, CaseAttachment> {

    private final RinaCaseRepo rinaCaseRepo;
    private final UserService userService;

    @Autowired
    private AttachmentsHelper attachmentsHelper;

    public MapToCaseAttachmentMapper(final RinaCaseRepo rinaCaseRepo,
            final UserService userService) {
        this.rinaCaseRepo = rinaCaseRepo;
        this.userService = userService;
    }

    @Override
    public void mapAtoB(final MapHolder a, final CaseAttachment b, final MappingContext context) {
        mapRinaCase(a, b);
        mapAudit(a, b);
        mapMimeType(a, b);
        mapPathname(a, b);

        b.setFilename(a.string(CaseAttachmentFields.FILE_NAME));
        b.setId(a.string(CaseAttachmentFields.ID));
        b.setMedical(a.bool(CaseAttachmentFields.MEDICAL, Boolean.FALSE));
        b.setName(a.string(CaseAttachmentFields.NAME));

    }

    private void mapPathname(final MapHolder a, final CaseAttachment b) {
        String caseId = a.string(CaseAttachmentFields.CASE_ID);
        String attachmentId = a.string(CaseAttachmentFields.ID);
        b.setPathname(attachmentsHelper.getAttachmentPathname(caseId, attachmentId));
    }

    private void mapMimeType(final MapHolder a, final CaseAttachment b) {
        String mimeType = a.string(CaseAttachmentFields.MIME_TYPE);
        EMimeType eMimeType = EMimeType.lookup(mimeType).orElseThrow(
                () -> new DmtEnumNotFoundException(EMimeType.class, a.addPath(CaseAttachmentFields.MIME_TYPE), mimeType)
        );
        b.setMimeType(eMimeType);
    }

    private void mapAudit(final MapHolder a, final CaseAttachment b) {
        Audit audit = new Audit();
        mapDate(a, CaseAttachmentFields.CREATION_DATE, audit::setCreatedAt);
        mapDate(a, CaseAttachmentFields.LAST_UPDATE, audit::setUpdatedAt);

        String creatorId = a.string(CaseAttachmentFields.CREATOR_ID, true);
        String creatorName = a.string(CaseAttachmentFields.CREATOR_NAME, true);
        IamUser iamUser = userService.resolveUser(creatorId, creatorName, b.getRinaCase());

        audit.setCreatedBy(iamUser.getId());
        audit.setUpdatedBy(iamUser.getId());

        b.setAudit(audit);
    }

    private void mapRinaCase(final MapHolder a, final CaseAttachment b) {
        String caseId = a.string(CaseAttachmentFields.CASE_ID);
        RinaCase rinaCase = RepositoryUtils.findById(caseId, rinaCaseRepo::findById, RinaCase.class);
        b.setRinaCase(rinaCase);
    }
}

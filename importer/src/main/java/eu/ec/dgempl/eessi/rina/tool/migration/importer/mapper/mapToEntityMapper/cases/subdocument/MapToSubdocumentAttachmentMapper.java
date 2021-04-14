package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.subdocument;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EMimeType;
import eu.ec.dgempl.eessi.rina.model.exception.runtime.enums.EnumNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.SubdocumentAttachment;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.helper.AttachmentsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToSubdocumentAttachmentMapper extends AbstractMapToEntityMapper<MapHolder, SubdocumentAttachment> {

    private final IamUserRepo iamUserRepo;

    @Autowired
    private AttachmentsHelper attachmentsHelper;

    public MapToSubdocumentAttachmentMapper(final IamUserRepo iamUserRepo) {
        this.iamUserRepo = iamUserRepo;
    }

    @Override
    public void mapAtoB(final MapHolder a, final SubdocumentAttachment b, final MappingContext context) {
        b.setId(a.string("id"));
        b.setFilename(a.string("fileName"));
        b.setMedical(a.bool("medical", Boolean.FALSE));
        b.setName(a.string("name"));

        mapAudit(a, b);
        mapMimeType(a, b);
        mapPathname(a, b);
    }

    private void mapPathname(final MapHolder a, final SubdocumentAttachment b) {
        String caseId = a.string("caseId");
        String attachmentId = a.string("id");
        b.setPathname(attachmentsHelper.getAttachmentPathname(caseId, attachmentId));
    }

    private void mapMimeType(final MapHolder a, final SubdocumentAttachment b) {
        String mimeType = a.string("mimeType");
        EMimeType eMimeType = EMimeType.lookup(mimeType).orElseThrow(EnumNotFoundEessiRuntimeException::new);
        b.setMimeType(eMimeType);
    }

    private void mapAudit(final MapHolder a, final SubdocumentAttachment b) {
        setDefaultAudit(b::setAudit);

        mapDate(a, "creationDate", b.getAudit()::setCreatedAt);
        mapDate(a, "lastUpdate", b.getAudit()::setUpdatedAt);

        String userId = a.string("creator.id", true);
        IamUser iamUser = RepositoryUtils.findById(userId, iamUserRepo::findById, IamUser.class);
        b.getAudit().setCreatedBy(iamUser.getId());
        b.getAudit().setUpdatedBy(iamUser.getId());
    }
}

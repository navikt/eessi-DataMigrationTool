package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.document;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EDocumentStatus;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentBversion;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.DocumentFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToDocumentBversionMapper extends AbstractMapToEntityMapper<MapHolder, DocumentBversion> {

    private final IamUserRepo iamUserRepo;

    public MapToDocumentBversionMapper(final IamUserRepo iamUserRepo) {
        this.iamUserRepo = iamUserRepo;
    }

    @Override
    public void mapAtoB(final MapHolder a, final DocumentBversion b, final MappingContext context) {
        Document doc = (Document) context.getProperty("doc");

        b.setId(Integer.parseInt(a.string(DocumentFields.BVERSION_ID)));

        mapAudit(a, b, doc);
    }

    private void mapAudit(final MapHolder a, final DocumentBversion b, final Document doc) {
        setDefaultAudit(b::setAudit);

        mapDate(a, DocumentFields.DATE, b.getAudit()::setCreatedAt);
        mapDate(a, DocumentFields.DATE, b.getAudit()::setUpdatedAt);

        String userId = a.string(DocumentFields.USER_ID, true);
        if (doc.getStatus() != EDocumentStatus.EMPTY) {
            IamUser iamUser = RepositoryUtils.findById(userId, iamUserRepo::findById, IamUser.class);
            b.getAudit().setCreatedBy(iamUser.getId());
            b.getAudit().setUpdatedBy(iamUser.getId());
        }
    }
}

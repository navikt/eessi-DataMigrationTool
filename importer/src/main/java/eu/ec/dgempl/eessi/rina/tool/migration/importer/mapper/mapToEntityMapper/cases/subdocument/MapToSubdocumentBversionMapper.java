package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.subdocument;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils.*;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.SubdocumentBversion;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToSubdocumentBversionMapper extends AbstractMapToEntityMapper<MapHolder, SubdocumentBversion> {

    private final IamUserRepo iamUserRepo;

    public MapToSubdocumentBversionMapper(final IamUserRepo iamUserRepo) {
        this.iamUserRepo = iamUserRepo;
    }

    @Override
    public void mapAtoB(final MapHolder a, final SubdocumentBversion b, final MappingContext context) {
        b.setId(Integer.parseInt(a.string("id")));
        mapAudit(a, b);
    }

    private void mapAudit(final MapHolder a, final SubdocumentBversion b) {
        setDefaultAudit(b::setAudit);

        String userId = a.string("user.id", true);
        IamUser iamUser = findById(userId, iamUserRepo::findById, IamUser.class);

        mapDate(a, "date", b.getAudit()::setCreatedAt);
        mapDate(a, "date", b.getAudit()::setUpdatedAt);

        b.getAudit().setCreatedBy(iamUser.getId());
        b.getAudit().setUpdatedBy(iamUser.getId());

    }
}

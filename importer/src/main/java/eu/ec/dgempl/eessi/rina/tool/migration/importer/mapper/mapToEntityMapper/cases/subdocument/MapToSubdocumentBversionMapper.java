package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.subdocument;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.SubdocumentBversion;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.UserService;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToSubdocumentBversionMapper extends AbstractMapToEntityMapper<MapHolder, SubdocumentBversion> {

    private final UserService userService;

    public MapToSubdocumentBversionMapper(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public void mapAtoB(final MapHolder a, final SubdocumentBversion b, final MappingContext context) {
        b.setId(Integer.parseInt(a.string("id")));
        RinaCase rinaCase = (RinaCase) context.getProperty("rinaCase");
        mapAudit(a, b, rinaCase);
    }

    private void mapAudit(final MapHolder a, final SubdocumentBversion b, final RinaCase rinaCase) {
        setDefaultAudit(b::setAudit);

        String userId = a.string("user.id", true);
        String userName = a.string("user.name", true);
        IamUser iamUser = userService.resolveUser(userId, userName, rinaCase);

        mapDate(a, "date", b.getAudit()::setCreatedAt);
        mapDate(a, "date", b.getAudit()::setUpdatedAt);

        b.getAudit().setCreatedBy(iamUser.getId());
        b.getAudit().setUpdatedBy(iamUser.getId());
    }
}

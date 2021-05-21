package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.document;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EDocumentStatus;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentBversion;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.DocumentFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.UserService;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToDocumentBversionMapper extends AbstractMapToEntityMapper<MapHolder, DocumentBversion> {

    private final UserService userService;

    public MapToDocumentBversionMapper(final UserService userService) {
        this.userService = userService;
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

        if (doc.getStatus() != EDocumentStatus.EMPTY) {

            String userId = a.string(DocumentFields.USER_ID, true);
            String userName = a.string(DocumentFields.USER_NAME, true);
            IamUser iamUser = userService.resolveUser(userId, userName, doc.getRinaCase());

            b.getAudit().setCreatedBy(iamUser.getId());
            b.getAudit().setUpdatedBy(iamUser.getId());
        }
    }
}

package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.audit;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.audit.EAuditedObjectType;
import eu.ec.dgempl.eessi.rina.model.exception.runtime.enums.EnumEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.AuditObject;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.AuditEventFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToAuditObjectMapper extends AbstractMapToEntityMapper<MapHolder, AuditObject> {

    @Override
    public void mapAtoB(final MapHolder a, final AuditObject b, final MappingContext context) {
        b.setId(a.string(AuditEventFields.AUDIT_OBJECT_ID));
        b.setDetails(a.string(AuditEventFields.DETAILS));

        mapType(a, b);
    }

    private void mapType(final MapHolder a, final AuditObject b) {
        String type = a.string(AuditEventFields.AUDIT_OBJECT_TYPE);
        EAuditedObjectType eAuditedObjectType = EAuditedObjectType.lookup(type).orElseThrow(EnumEessiRuntimeException::new);
        b.setAuditObjectType(eAuditedObjectType);
    }
}

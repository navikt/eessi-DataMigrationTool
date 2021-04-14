package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.audit;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.audit.EParticipantRole;
import eu.ec.dgempl.eessi.rina.model.enumtypes.audit.EParticipantType;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.AuditParticipant;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.DmtEnumNotFoundException;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.AuditEventFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToAuditParticipantMapper extends AbstractMapToEntityMapper<MapHolder, AuditParticipant> {

    @Override
    public void mapAtoB(final MapHolder a, final AuditParticipant b, final MappingContext context) {
        b.setId(a.string(AuditEventFields.AUDIT_PARTICIPANT_ID));

        mapRole(a, b);
        mapType(a, b);

    }

    private void mapType(final MapHolder a, final AuditParticipant b) {
        String type = a.string(AuditEventFields.AUDIT_PARTICIPANT_TYPE);
        EParticipantType eParticipantType = EParticipantType.lookup(type).orElseThrow(
                () -> new DmtEnumNotFoundException(EParticipantType.class, a.addPath(AuditEventFields.AUDIT_PARTICIPANT_TYPE), type)
        );
        b.setParticipantType(eParticipantType);
    }

    private void mapRole(final MapHolder a, final AuditParticipant b) {
        String role = a.string(AuditEventFields.AUDIT_PARTICIPANT_ROLE);
        EParticipantRole eParticipantRole = EParticipantRole.lookup(role).orElseThrow(
                () -> new DmtEnumNotFoundException(EParticipantRole.class, a.addPath(AuditEventFields.AUDIT_PARTICIPANT_ROLE), role)
        );
        b.setParticipantRole(eParticipantRole);
    }
}

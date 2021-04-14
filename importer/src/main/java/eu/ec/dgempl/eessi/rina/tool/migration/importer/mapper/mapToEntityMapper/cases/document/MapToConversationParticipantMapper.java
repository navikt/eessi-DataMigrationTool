package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EConversationParticipantRole;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.ConversationParticipant;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Organisation;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.DmtEnumNotFoundException;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.DocumentFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.OrganisationService;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToConversationParticipantMapper extends AbstractMapToEntityMapper<MapHolder, ConversationParticipant> {

    @Autowired
    private OrganisationService organisationService;

    @Override
    public void mapAtoB(final MapHolder a, final ConversationParticipant b, final MappingContext context) {
        mapOrganisation(a, b);
        mapRole(a, b);
    }

    private void mapOrganisation(final MapHolder a, final ConversationParticipant b) {
        String orgId = a.string(DocumentFields.ORGANISATION_ID, true);
        Organisation organisation = organisationService.getOrSaveOrganisation(orgId);
        b.setOrganisation(organisation);
    }

    private void mapRole(final MapHolder a, final ConversationParticipant b) {
        String role = a.string(DocumentFields.ROLE);
        EConversationParticipantRole eRole = EConversationParticipantRole.lookup(role).orElseThrow(
                () -> new DmtEnumNotFoundException(EConversationParticipantRole.class, a.addPath(DocumentFields.ROLE), role));
        b.setConversationParticipantRole(eRole);
    }
}

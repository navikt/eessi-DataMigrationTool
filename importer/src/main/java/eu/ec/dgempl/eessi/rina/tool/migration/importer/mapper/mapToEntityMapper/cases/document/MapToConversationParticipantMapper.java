package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.document;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils.*;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EConversationParticipantRole;
import eu.ec.dgempl.eessi.rina.model.exception.runtime.enums.EnumNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.ConversationParticipant;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Organisation;
import eu.ec.dgempl.eessi.rina.repo.OrganisationRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.DocumentFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToConversationParticipantMapper extends AbstractMapToEntityMapper<MapHolder, ConversationParticipant> {

    private final OrganisationRepo organisationRepo;

    public MapToConversationParticipantMapper(final OrganisationRepo organisationRepo) {
        this.organisationRepo = organisationRepo;
    }

    @Override
    public void mapAtoB(final MapHolder a, final ConversationParticipant b, final MappingContext context) {
        mapOrganisation(a, b);
        mapRole(a, b);
    }

    private void mapOrganisation(final MapHolder a, final ConversationParticipant b) {
        String orgId = a.string(DocumentFields.ORGANISATION_ID, true);
        Organisation organisation = findById(orgId, organisationRepo::findById, Organisation.class);
        b.setOrganisation(organisation);
    }

    private void mapRole(final MapHolder a, final ConversationParticipant b) {
        String role = a.string(DocumentFields.ROLE);
        EConversationParticipantRole eRole = EConversationParticipantRole.lookup(role).orElseThrow(EnumNotFoundEessiRuntimeException::new);
        b.setConversationParticipantRole(eRole);
    }
}

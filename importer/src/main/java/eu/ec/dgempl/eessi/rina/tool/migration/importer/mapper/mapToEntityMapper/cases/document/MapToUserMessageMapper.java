package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.document;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ECaseActionType;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ERINAMessageType;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EUserMessageStatus;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Organisation;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.UserMessage;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.UserMessageResponse;
import eu.ec.dgempl.eessi.rina.repo.OrganisationRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.DocumentFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToUserMessageMapper extends AbstractMapToEntityMapper<MapHolder, UserMessage> {

    private final OrganisationRepo organisationRepo;
    private final RinaJsonMapper rinaJsonMapper;

    public MapToUserMessageMapper(final OrganisationRepo organisationRepo, final RinaJsonMapper rinaJsonMapper) {
        this.organisationRepo = organisationRepo;
        this.rinaJsonMapper = rinaJsonMapper;
    }

    @Override
    public void mapAtoB(final MapHolder a, final UserMessage b, final MappingContext context) {
        b.setId(a.string(DocumentFields.USER_MESSAGE_ID));

        mapSbdh(a, b);
        mapAction(a, b);
        mapStatus(a, b);
        mapOrganisation(a, DocumentFields.SENDER, b::setSender);
        mapOrganisation(a, DocumentFields.RECEIVER, b::setReceiver);
        mapResponse(a, b);
    }

    private void mapSbdh(final MapHolder a, final UserMessage b) {
        try {
            Map<String, Object> sbdh = a.getMap(DocumentFields.SBDH);

            // fix isMedical flag; in ES the field is named 'medical'; the DTOs use 'isMedical'
            fixMedicalFieldName(sbdh);

            b.setSbdh(rinaJsonMapper.mapToJson(sbdh));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not map sbdh for userMessage " + b.getId(), e);
        }
    }

    private void fixMedicalFieldName(Map<String, Object> sbdh) {
        List<Map<String, Object>> attachments = (List<Map<String, Object>>) sbdh.get(DocumentFields.ATTACHMENTS);

        if (attachments != null && !attachments.isEmpty()) {
            for (Map<String, Object> attachment : attachments) {
                if (attachment.containsKey(DocumentFields.MEDICAL)) {
                    Boolean isMedical = (Boolean) attachment.get(DocumentFields.MEDICAL);
                    attachment.remove(DocumentFields.MEDICAL);
                    attachment.put(DocumentFields.IS_MEDICAL, isMedical);
                }
            }
        }
    }

    private void mapStatus(final MapHolder a, final UserMessage b) {
        Boolean isSent = a.bool(DocumentFields.IS_SENT, Boolean.FALSE);
        if (isSent) {
            b.setStatus(EUserMessageStatus.SENT);
        }
    }

    private void mapAction(final MapHolder a, final UserMessage b) {
        MapHolder sbdhHolder = a.getMapHolder(DocumentFields.SBDH);
        String action = sbdhHolder.string(DocumentFields.DOCUMENT_IDENTIFICATION_ACTION, true);
        ECaseActionType eCaseActionType = ECaseActionType.fromString(action);
        b.setAction(eCaseActionType);
    }

    private void mapResponse(final MapHolder a, final UserMessage b) {
        if (a.containsKey(DocumentFields.ACK)) {
            mapUserMessageResponse(a.getMapHolder(DocumentFields.ACK), b, ERINAMessageType.ACK);
        } else {
            if (a.containsKey(DocumentFields.ERROR)) {
                mapUserMessageResponse(a.getMapHolder(DocumentFields.ERROR), b, ERINAMessageType.ERROR);
            }
        }
    }

    private void mapUserMessageResponse(MapHolder a, UserMessage b, ERINAMessageType messageType) {
        UserMessageResponse userMessageResponse = new UserMessageResponse(b);
        userMessageResponse.setType(messageType);
        userMessageResponse.setId(a.string(DocumentFields.USER_MESSAGE_RESPONSE_ID));
        userMessageResponse.setDescription(a.string(DocumentFields.DESCRIPTION));
        mapDate(a, DocumentFields.USER_MESSAGE_RESPONSE_DATE, userMessageResponse::setLastUpdate);
    }

    private void mapOrganisation(final MapHolder a, final String key, final Consumer<Organisation> organisationConsumer) {
        organisationConsumer.accept(findOrganisation(a, key));
    }

    private Organisation findOrganisation(final MapHolder a, String key) {
        String orgId = a.string(key + ".id", true);
        return RepositoryUtils.findById(orgId, organisationRepo::findById, Organisation.class);
    }

}

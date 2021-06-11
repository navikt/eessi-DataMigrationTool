package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.document;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.ec.dgempl.apclient.sbdh.builder.DocumentIdentificationBuilder;
import eu.ec.dgempl.apclient.sbdh.builder.StandardBusinessDocumentHeaderBuilder;
import eu.ec.dgempl.apclient.sbdh.model.CaseActionType;
import eu.ec.dgempl.apclient.sbdh.model.CaseIdentification;
import eu.ec.dgempl.apclient.sbdh.model.DocumentIdentification;
import eu.ec.dgempl.apclient.sbdh.model.Partner;
import eu.ec.dgempl.apclient.sbdh.model.StandardBusinessDocumentHeader;
import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ECaseActionType;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EConversationParticipantRole;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EDocumentStatus;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ERINAMessageType;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EUserMessageStatus;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentConversation;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Organisation;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.UserMessage;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.UserMessageResponse;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.DmtEnumNotFoundException;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.DocumentFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.OrganisationService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.SbdhUtils;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToUserMessageMapper extends AbstractMapToEntityMapper<MapHolder, UserMessage> {

    private static final Logger logger = LoggerFactory.getLogger(MapToUserMessageMapper.class);

    private final RinaJsonMapper rinaJsonMapper;

    @Autowired
    private OrganisationService organisationService;

    public MapToUserMessageMapper(final RinaJsonMapper rinaJsonMapper) {
        this.rinaJsonMapper = rinaJsonMapper;
    }

    @Override
    public void mapAtoB(final MapHolder a, final UserMessage b, final MappingContext context) {
        b.setId(a.string(DocumentFields.USER_MESSAGE_ID));

        mapOrganisation(a, DocumentFields.SENDER, b::setSender);
        mapOrganisation(a, DocumentFields.RECEIVER, b::setReceiver);
        mapSbdh(a, b, context);
        mapAction(a, b);
        mapStatus(a, b);
        mapResponse(a, b);
    }

    private void mapSbdh(final MapHolder a, final UserMessage b, final MappingContext context) {
        try {
            Map<String, Object> sbdh = a.getMap(DocumentFields.SBDH);

            if (sbdh == null) {
                try {
                    // Create and empty default SBDH and resolve the actionType
                    sbdh = rinaJsonMapper.transformObjectToMap(generateEmptySbdhWithActionType(a, b, context));
                    a.put(DocumentFields.SBDH, sbdh);
                    b.setSbdh(rinaJsonMapper.mapToJson(sbdh));
                    logger.info("Sbdh is null. Added a default empty sbdh to usermessage and resolved the action type");
                    return;
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Error getting sbdh from userMessage. Sbdh is null. Tried to replace null sbdh with default empty sbdh but creation of the empty sbdh failed.");
                }
            }

            // fix isMedical flag; in ES the field is named 'medical'; the DTOs use 'isMedical'
            fixMedicalFieldName(sbdh);
            // fix isProtectedPerson flag; in ES the field is named 'protectedPerson'; the DTOs use 'isProtectedPerson'
            fixProtectedFieldName(sbdh);

            try {
                rinaJsonMapper.mapToObject(sbdh, StandardBusinessDocumentHeader.class);
            } catch (Exception e) {
                SbdhUtils.fixSbdhEnumValues(sbdh, "");
            }

            try {
                rinaJsonMapper.mapToObject(sbdh, StandardBusinessDocumentHeader.class);
            } catch (Exception e) {
                throw new RuntimeException("Could not deserialize sbdh for userMessage " + b.getId(), e);
            }

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

    private void fixProtectedFieldName(Map<String, Object> sbdh) {
        Map<String, Object> caseIdentification = (Map<String, Object>) sbdh.get(DocumentFields.CASE_IDENTIFICATION);
        if (caseIdentification.containsKey(DocumentFields.PROTECTED_PERSON)) {
            Boolean isProtectedPerson = (Boolean) caseIdentification.get(DocumentFields.PROTECTED_PERSON);
            caseIdentification.remove(DocumentFields.PROTECTED_PERSON);
            caseIdentification.put(DocumentFields.IS_PROTECTED_PERSON, isProtectedPerson);
        }
    }

    private void mapStatus(final MapHolder a, final UserMessage b) {
        String status = a.string(DocumentFields.STATUS);

        if (StringUtils.isNotBlank(status)) {
            EUserMessageStatus eUserMessageStatus = EUserMessageStatus.lookup(status).orElseThrow(
                    () -> new DmtEnumNotFoundException(EUserMessageStatus.class, a.addPath(DocumentFields.STATUS), status));
            b.setStatus(eUserMessageStatus);
        }

        Boolean isSent = a.bool(DocumentFields.IS_SENT, Boolean.FALSE);
        if (isSent) {
            b.setStatus(EUserMessageStatus.SENT);
        }
    }

    private void mapAction(final MapHolder a, final UserMessage b) {
        MapHolder sbdhHolder = a.getMapHolder(DocumentFields.SBDH);
        if (sbdhHolder != null && sbdhHolder.getHolding() != null) {
            String action = sbdhHolder.string(DocumentFields.DOCUMENT_IDENTIFICATION_ACTION, true);
            ECaseActionType eCaseActionType = ECaseActionType.fromString(action);
            b.setAction(eCaseActionType);
        }
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

        b.setUserMessageResponse(userMessageResponse);
    }

    private void mapOrganisation(final MapHolder a, final String key, final Consumer<Organisation> organisationConsumer) {
        organisationConsumer.accept(findOrganisation(a, key));
    }

    private Organisation findOrganisation(final MapHolder a, String key) {
        String orgKey = key + ".id";
        String orgId = a.string(orgKey, true);

        if (StringUtils.isBlank(orgId)) {
            throw new RuntimeException(String.format("Could not load organisation [id=%s], [key=%s]", orgId, orgKey));
        }

        return organisationService.getOrSaveOrganisation(orgId);
    }

    @NotNull
    private StandardBusinessDocumentHeader generateEmptySbdhWithActionType(
            final MapHolder a,
            final UserMessage b,
            final MappingContext context) {

        Document document = (Document) context.getProperty("doc");
        DocumentConversation documentConversation = (DocumentConversation) context.getProperty("conversation");
        RinaCase rinaCase = document.getRinaCase();

        DocumentIdentification documentIdentification = createDocumentIdentification(a, document);
        CaseIdentification caseIdentification = createCaseIdentification(rinaCase);
        Partner sender = createSender(b);
        List<Partner> receivers = createReceivers(documentConversation);

        StandardBusinessDocumentHeaderBuilder sbdhBuilder = StandardBusinessDocumentHeaderBuilder.aStandardBusinessDocumentHeader();
        sbdhBuilder.withCaseIdentification(caseIdentification);
        sbdhBuilder.withDocumentIdentification(documentIdentification);
        sbdhBuilder.withSender(sender);
        sbdhBuilder.withReceivers(receivers);

        return sbdhBuilder.build();
    }

    private List<Partner> createReceivers(DocumentConversation documentConversation) {
        return documentConversation.getConversationParticipants()
                .stream()
                .filter(conversationParticipant -> conversationParticipant
                        .getConversationParticipantRole() == EConversationParticipantRole.RECEIVER)
                .map(conversationParticipant -> {
                    Partner partner = new Partner();
                    partner.setIdentifier(conversationParticipant.getOrganisation().getId());
                    return partner;
                })
                .collect(Collectors.toList());
    }

    @NotNull
    private DocumentIdentification createDocumentIdentification(MapHolder a, Document document) {
        ECaseActionType actionType = getActionType(document, a.string(DocumentFields.ID));

        DocumentIdentificationBuilder documentIdentificationBuilder = DocumentIdentificationBuilder.aDocumentIdentification();
        documentIdentificationBuilder.withSetIdentifier(document.getId());
        documentIdentificationBuilder.withAction(CaseActionType.fromValue(actionType.value()));
        return documentIdentificationBuilder.build();
    }

    @NotNull
    private Partner createSender(UserMessage b) {
        Partner sender = new Partner();
        Organisation senderOrg = b.getSender();
        sender.setIdentifier(senderOrg.getId());
        return sender;
    }

    @NotNull
    private CaseIdentification createCaseIdentification(RinaCase rinaCase) {
        CaseIdentification caseIdentification = new CaseIdentification();

        String caseInternationalId = rinaCase.getInternationalId();
        if (caseInternationalId != null) {
            caseInternationalId = caseInternationalId.replace("_Removed", "");
            caseIdentification.setIdentifier(caseInternationalId);
        }
        return caseIdentification;
    }

    private ECaseActionType getActionType(final Document document, String userMessageId) {
        PreconditionsHelper.notNull(document, "document");
        PreconditionsHelper.notNull(userMessageId, "userMessageId");

        ECaseActionType actionType = null;

        boolean isStarter = false;
        if (document.isStarter() != null) {
            isStarter = document.isStarter();
        }

        boolean isStarterSent = false;
        RinaCase rinaCase = document.getRinaCase();
        if (rinaCase != null) {
            if (rinaCase.isStarterSent() != null) {
                isStarterSent = rinaCase.isStarterSent();
            }
        } else {
            logger.info(String.format(
                    "Could not resolve the action type for userMessage with id %s because rina case was not found",
                    userMessageId));
            return actionType;
        }

        EDocumentStatus documentStatus = document.getStatus();
        if (documentStatus == null) {
            logger.info(
                    String.format("Could not resolve the action type for userMessage with id %s because document status is null",
                            userMessageId));
            return actionType;
        }

        if (isStarter) {
            if (isStarterSent) {
                if (documentStatus.equals(EDocumentStatus.NEW)) {
                    actionType = ECaseActionType.NEW;
                } else {
                    actionType = ECaseActionType.UPDATE;
                }
            } else {
                actionType = ECaseActionType.START;
            }
        } else {
            if (documentStatus.equals(EDocumentStatus.NEW)) {
                actionType = ECaseActionType.NEW;
            } else {
                actionType = ECaseActionType.UPDATE;
            }
        }
        logger.info(
                String.format("Resolving the action type for userMessage with id %s. Setting action type: %s", userMessageId, actionType));
        return actionType;
    }
}

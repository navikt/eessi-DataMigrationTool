package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.businessExceptions;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.ec.dgempl.apclient.model.exception.StructureValidationException;
import eu.ec.dgempl.apclient.sbdh.builder.SbdhBuilder;
import eu.ec.dgempl.apclient.sbdh.model.CaseActionType;
import eu.ec.dgempl.apclient.sbdh.model.ContactTypeIdentifier;
import eu.ec.dgempl.apclient.sbdh.model.InternetMediaType;
import eu.ec.dgempl.apclient.sbdh.model.StandardBusinessDocumentHeader;
import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EApplicationRole;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ECaseActionType;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ECaseStatus;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EMimeType;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.*;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityWithTooManyRecordsEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.ProcessDefVersionRepo;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.DateResolver;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.PendingMessageFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.OrganisationService;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToPendingMessageMapper extends AbstractMapToEntityMapper<MapHolder, PendingMessage> {

    private static final Logger logger = LoggerFactory.getLogger(MapToPendingMessageMapper.class);

    private final ProcessDefVersionRepo processDefVersionRepo;
    private final RinaCaseRepo rinaCaseRepo;
    private final RinaJsonMapper rinaJsonMapper;

    @Autowired
    private OrganisationService organisationService;

    public MapToPendingMessageMapper(final ProcessDefVersionRepo processDefVersionRepo, final RinaCaseRepo rinaCaseRepo,
            final RinaJsonMapper rinaJsonMapper) {

        this.processDefVersionRepo = processDefVersionRepo;
        this.rinaCaseRepo = rinaCaseRepo;
        this.rinaJsonMapper = rinaJsonMapper;
    }

    @Override
    public void mapAtoB(final MapHolder a, final PendingMessage b, final MappingContext context) {

        b.setActionType(ECaseActionType.fromString(a.string(PendingMessageFields.ACTION_TYPE)));
        b.setCause(a.string(PendingMessageFields.CAUSE));
        mapDate(a, b);
        b.setId(a.string(PendingMessageFields.ID));
        b.setContentLocation(a.string(PendingMessageFields.CONTENT_LOCATION, true));

        b.setInternationalCaseId(a.string(PendingMessageFields.INTERNATIONAL_CASE_ID));
        b.setIsProtectedPerson(a.bool(PendingMessageFields.PUBLIC_PERSON));
        b.setShouldNotify(a.bool(PendingMessageFields.NOTIFY, Boolean.FALSE));
        b.setIsProcessed(false);

        b.setIsSelected(a.bool(PendingMessageFields.IS_SELECTED, Boolean.FALSE));
        b.setIsFilteredOut(a.bool(PendingMessageFields.IS_FILTERED_OUT, Boolean.FALSE));
        b.setIsExpanded(a.bool(PendingMessageFields.EXPANDED, Boolean.FALSE));

        b.setProblem(a.string(PendingMessageFields.PROBLEM));

        mapReceiver(a, b);
        mapSender(a, b);
        mapProcessDefVersion(a, b);
        mapRinaCase(a, b);
        mapPendingAttachments(a, b);

        // this mapping use the values already mapped in PendingMessage
        mapSbdh(a, b);
    }

    private void mapDate(MapHolder a, PendingMessage b) {
        String date = a.string(PendingMessageFields.DATE);

        if (date == null || date.isEmpty()) {
            throw new RuntimeException("Date is missing for pending message with id " + b.getId());
        }

        b.setDate(DateResolver.parse(date));
    }

    private void mapReceiver(MapHolder a, PendingMessage b) {
        String receiverId = a.string(PendingMessageFields.RECEIVER_ID, true);
        Organisation receiver = organisationService.getOrSaveOrganisation(receiverId);

        b.setReceiver(receiver);
    }

    private void mapSender(MapHolder a, PendingMessage b) {
        String senderId = a.string(PendingMessageFields.SENDER_ID, true);
        Organisation sender = organisationService.getOrSaveOrganisation(senderId);

        b.setSender(sender);
    }

    private void mapProcessDefVersion(MapHolder a, PendingMessage b) {
        String bucName = a.string(PendingMessageFields.BUC_TYPE, true);
        String bucVersion = a.string(PendingMessageFields.BUC_VERSION, true);

        ProcessDefVersion processDefVersion = processDefVersionRepo.findByProcessDefIdAndBusinessVersion(bucName, bucVersion);

        b.setProcessDefVersion(processDefVersion);
    }

    private void mapRinaCase(MapHolder a, PendingMessage b) {
        String caseInternationalId = a.string(PendingMessageFields.INTERNATIONAL_CASE_ID);
        String receiverId = a.string(PendingMessageFields.RECEIVER_ID, true);
        EApplicationRole eApplicationRole = getCaseRole(a.listToMapHolder(PendingMessageFields.PARTICIPANTS), receiverId);

        RinaCase rinaCase = null;
        try {
            rinaCase = getRinaCase(caseInternationalId, receiverId, eApplicationRole);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }

        if (rinaCase != null) {
            b.setRinaCase(rinaCase);
        }
    }

    private void mapPendingAttachments(MapHolder a, PendingMessage b) {
        Map<String, String> attachmentsMap = new HashMap<>();
        List<PendingAttachment> pendingAttachments = new ArrayList<>();
        List<MapHolder> attachments = a.listToMapHolder(PendingMessageFields.ATTACHMENT_IDENTIFICATIONS);

        for (MapHolder attachment : attachments) {
            String id = attachment.string(PendingMessageFields.ID);

            EMimeType mimeType = EMimeType.fromString(attachment.string(PendingMessageFields.MIME_TYPE));
            String pathname = attachment.string(PendingMessageFields.ATTACHMENT_CONTENT_LOCATION);

            Path path = Paths.get(pathname);
            String filename = path.getFileName().toString();

            PendingAttachment pendingAttachment = new PendingAttachment(b, mimeType, id, filename, pathname);
            pendingAttachment.setMedical(attachment.bool(PendingMessageFields.MEDICAL));

            if (attachmentsMap.containsKey(id)) {
                if (pathname.equals(attachmentsMap.get(id))) {
                    continue;
                } else {
                    throw new RuntimeException(
                            "Duplicate attachment id " + id + " but different location for pending message with id " + b.getId());
                }
            } else {
                attachmentsMap.put(id, pathname);
            }

            pendingAttachments.add(pendingAttachment);
        }

        b.setPendingAttachments(pendingAttachments);
    }

    private EApplicationRole getCaseRole(List<MapHolder> participants, String participantId) {
        for (MapHolder participant : participants) {
            String organisationId = participant.string(PendingMessageFields.ORGANISATION_ID, true);

            if (organisationId != null && organisationId.equals(participantId)) {
                String role = participant.string(PendingMessageFields.ROLE);
                return EApplicationRole.fromString(role);
            }
        }

        return null;
    }

    private void mapSbdh(MapHolder a, PendingMessage b) {
        SbdhBuilder sbdhBuilder = SbdhBuilder.standardBusinessDocumentHeader();

        String senderId = a.string(PendingMessageFields.SENDER_ID, true);
        EApplicationRole senderRole = getCaseRole(a.listToMapHolder(PendingMessageFields.PARTICIPANTS), senderId);

        // fix Counterparty vs CounterParty
        String roleName = senderRole == EApplicationRole.CP ? ContactTypeIdentifier.COUNTERPARTY.value() : senderRole.getConstant();

        sbdhBuilder.withSender(senderId, ContactTypeIdentifier.fromValue(roleName));

        for (MapHolder participant : a.listToMapHolder(PendingMessageFields.PARTICIPANTS)) {
            String participantId = participant.string(PendingMessageFields.ORGANISATION_ID, true);

            if (participantId != null && !participantId.equals(senderId)) {
                String role = participant.string(PendingMessageFields.ROLE);

                // fix Counterparty vs CounterParty
                if (role.equals(EApplicationRole.CP.getConstant())) {
                    role = ContactTypeIdentifier.COUNTERPARTY.value();
                }

                sbdhBuilder.withAddedReceiver(participantId, ContactTypeIdentifier.fromValue(role));
            }
        }

        String sedType = a.string(PendingMessageFields.SED_TYPE, true);
        String sedTypeVersion = a.string(PendingMessageFields.SED_TYPE_VERSION, true);
        String id = a.string(PendingMessageFields.ID);
        String sedId = a.string(PendingMessageFields.SED_ID, true);
        String sedVersion = a.string(PendingMessageFields.SED_VERSION, true);
        CaseActionType caseActionType = CaseActionType.fromValue(a.string(PendingMessageFields.ACTION_TYPE));
        ZonedDateTime creationDate = DateResolver.parse(a.string(PendingMessageFields.SED_CREATION_DATE, true));

        sbdhBuilder.withDocumentIdentification(sedType, sedTypeVersion, sedId, sedVersion, id, caseActionType,
                Date.from(creationDate.toInstant()));

        for (PendingAttachment pendingAttachment : b.getPendingAttachments()) {
            sbdhBuilder.withAddedAttachment(InternetMediaType.fromValue(pendingAttachment.getMimeType().toString()),
                    pendingAttachment.getId(), pendingAttachment.getSectionReference(), pendingAttachment.getFilename(),
                    pendingAttachment.isMedical());
        }

        String caseInternationalId = a.string(PendingMessageFields.INTERNATIONAL_CASE_ID);
        String bucName = a.string(PendingMessageFields.BUC_TYPE, true);
        String bucVersion = a.string(PendingMessageFields.BUC_VERSION, true);
        boolean isPublicPerson = a.bool(PendingMessageFields.PUBLIC_PERSON);

        sbdhBuilder.withCaseIdentification(bucName, bucVersion, caseInternationalId, isPublicPerson);

        sbdhBuilder.withRelatedDocumentSetIdentification(a.string(PendingMessageFields.RELATED_DOCUMENT_ID, true));

        StandardBusinessDocumentHeader sbdh = null;
        try {
            sbdh = sbdhBuilder.build();
        } catch (StructureValidationException e) {
            throw new RuntimeException("Error creating the SBDH", e);
        }

        try {
            b.setSbdh(rinaJsonMapper.objToJson(sbdh));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error on serializing sbdh for pending message with id " + b.getId(), e);
        }
    }

    private RinaCase getRinaCase(final String internationalCaseId, final String tenantId, final EApplicationRole eApplicationRole) {
        List<RinaCase> rinaCases = rinaCaseRepo.findByInternationalIdAndTenantId(internationalCaseId, tenantId);

        if (CollectionUtils.isEmpty(rinaCases)) {
            return null;
        }

        rinaCases.removeIf(rinaCase -> !rinaCase.getApplicationRole().equals(eApplicationRole));

        if (rinaCases.size() > 1) {
            rinaCases = rinaCases.stream()
                    .filter(rinaCase -> ECaseStatus.REMOVED != rinaCase.getStatus() && ECaseStatus.ARCHIVED != rinaCase.getStatus())
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(rinaCases)) {
                return null;
            }

            if (rinaCases.size() > 1) {
                logger.warn("Too many cases with international id [{}] and tenant id [{}]", internationalCaseId, tenantId);
                throw new EntityWithTooManyRecordsEessiRuntimeException(RinaCase.class, UniqueIdentifier.internationalId,
                        internationalCaseId, UniqueIdentifier.tenantId, tenantId);
            }
        }

        return rinaCases.get(0);
    }
}

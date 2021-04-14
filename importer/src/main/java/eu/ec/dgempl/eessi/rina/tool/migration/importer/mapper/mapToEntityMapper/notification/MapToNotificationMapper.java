package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.notification;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EAssignmentRequestStatus;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ENotificationStatus;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ENotificationType;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ERole;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ESeverity;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ESourceType;
import eu.ec.dgempl.eessi.rina.model.exception.runtime.enums.EnumNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.AssignmentRequest;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentType;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Notification;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Organisation;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Role;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.DocumentTypeRepo;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepo;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepoExtended;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.repo.RoleRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.NotificationFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.OrganisationService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.CasesUtils;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToNotificationMapper extends AbstractMapToEntityMapper<MapHolder, Notification> {

    private final String SYSTEM = "system";
    private final String SYSTEM_ID = "0";
    private final String NO_USERNAME = "-1";

    private final IamUserRepo iamUserRepo;
    private final IamUserRepoExtended iamUserRepoExtended;
    private final DocumentTypeRepo documentTypeRepo;
    private final RinaCaseRepo rinaCaseRepo;
    private final RoleRepo roleRepo;

    @Autowired
    private OrganisationService organisationService;

    public MapToNotificationMapper(final IamUserRepo iamUserRepo, final IamUserRepoExtended iamUserRepoExtended,
            final DocumentTypeRepo documentTypeRepo, final RinaCaseRepo rinaCaseRepo, final RoleRepo roleRepo) {

        this.iamUserRepo = iamUserRepo;
        this.iamUserRepoExtended = iamUserRepoExtended;
        this.documentTypeRepo = documentTypeRepo;
        this.rinaCaseRepo = rinaCaseRepo;
        this.roleRepo = roleRepo;
    }

    @Override
    public void mapAtoB(final MapHolder a, final Notification b, final MappingContext context) {

        b.setId(a.string(NotificationFields.ID));
        b.setReason(a.string(NotificationFields.REASON));
        b.setFailureDescription(a.string(NotificationFields.FAILURE_DESCRIPTION, true));
        b.setCategory(a.string(NotificationFields.CATEGORY));
        b.setSeverity(ESeverity.getBySeverity(a.string(NotificationFields.SEVERITY)));
        b.setType(ENotificationType.getENotificationType(a.string(NotificationFields.TYPE)));
        b.setStatus(ENotificationStatus.getENotificationStatus(a.string(NotificationFields.STATUS)));

        mapDate(a, NotificationFields.DUE_DATE, b::setDueDate);

        b.setIsRead(Boolean.parseBoolean(a.string(NotificationFields.IS_READ)));
        b.setSourceType(ESourceType.getByType(a.string(NotificationFields.SOURCE_TYPE)));

        mapCreator(a, b);
        mapSender(a, b);
        mapReceiver(a, b);
        mapDocumentType(a, b);
        mapCase(a, b);
        mapUsers(a, b);
        mapAssignmentRequests(a, b);
        mapLog(a, b);
    }

    private void mapLog(final MapHolder a, final Notification b) {
        setDefaultLog(b::setLog);

        mapDate(a, "creationDate", b.getLog()::setCreatedAt);
        mapDate(a, "lastUpdate", b.getLog()::setUpdatedAt);
    }

    private void mapCreator(final MapHolder a, final Notification b) {
        IamUser iamUser = null;

        final String userId = getUserId(a);
        if (StringUtils.isNotBlank(userId)) {
            iamUser = RepositoryUtils.findById(userId, iamUserRepo::findById);
        }

        if (iamUser == null) {
            final String username = getUsername(a, NotificationFields.CREATOR_NAME);
            iamUser = RepositoryUtils.getIamUser(() -> username, () -> iamUserRepoExtended);
        }

        b.setCreator(iamUser);
    }

    private String getUserId(MapHolder a) {
        String userId = a.string(NotificationFields.CREATOR_ID, true);

        if (NO_USERNAME.equals(userId)) {
            userId = SYSTEM_ID;
        }

        return userId;
    }

    private String getUsername(MapHolder a, String path) {
        String username = a.string(path, true);

        if (username == null || username.equalsIgnoreCase(SYSTEM) || username.equals(NO_USERNAME)) {
            username = SYSTEM;
        }
        return username;
    }

    private void mapSender(final MapHolder a, final Notification b) {
        String senderId = a.string(NotificationFields.SENDER, true);
        Organisation sender = organisationService.getOrSaveOrganisation(senderId);

        b.setSender(sender);
    }

    private void mapReceiver(final MapHolder a, final Notification b) {
        if (a.get(NotificationFields.RECEIVER, true) == null) {
            return;
        }

        String receiverId = null;
        if (a.get(NotificationFields.RECEIVER_ORGANISATION, true) == null) {
            receiverId = a.string(NotificationFields.RECEIVER_ID, true);
        } else {
            receiverId = a.string(NotificationFields.RECEIVER_ORGANISATION_ID, true);
        }

        Organisation receiver = organisationService.getOrSaveOrganisationWithDefault(receiverId, null);

        if (receiver == null) {
            final String username = getUsername(a, NotificationFields.RECEIVER_NAME);
            try {
                IamUser iamUser = RepositoryUtils.getIamUser(() -> username, () -> iamUserRepoExtended);
                receiver = iamUser.getTenant().getOrganisation();
            } catch (Exception e) {
                // ignore exception
            }

        }

        b.setReceiver(receiver);
    }

    private void mapDocumentType(final MapHolder a, final Notification b) {
        String type = a.string(NotificationFields.DOCUMENT_TYPE, true);
        DocumentType documentType = RepositoryUtils.findById(type, documentTypeRepo::findByType);

        b.setDocumentType(documentType);
    }

    private void mapCase(final MapHolder a, final Notification b) {
        String caseId = a.string(NotificationFields.CASE_ID);

        if (!CasesUtils.isDefaultCase(caseId)) {
            RinaCase rinaCase = RepositoryUtils.findById(caseId, rinaCaseRepo::findById, RinaCase.class);
            b.setRinaCase(rinaCase);
        }
    }

    private void mapUsers(final MapHolder a, final Notification b) {
        List<IamUser> iamUsers = new ArrayList<>();
        List<MapHolder> responsibleParties = a.listToMapHolder(NotificationFields.RESPONSIBLE_PARTIES);

        for (MapHolder responsibleParty : responsibleParties) {
            String username = responsibleParty.string(NotificationFields.NAME);

            IamUser iamUser = RepositoryUtils.getIamUser(() -> username, () -> iamUserRepoExtended);

            iamUsers.add(iamUser);
        }

        b.setIamUsers(iamUsers);
    }

    private void mapAssignmentRequests(final MapHolder a, final Notification b) {
        List<AssignmentRequest> assignmentRequests = new ArrayList<>();

        if (a.get(NotificationFields.ASSIGNMENT_REQUESTS, false) != null) {
            List<String> actors = a.list(NotificationFields.ACTORS, String.class, true);

            for (String actor : actors) {
                ERole eRole = ERole.fromString(actor);

                if (eRole == null) {
                    throw new EnumNotFoundEessiRuntimeException("Missing role " + actor + " for notification with id " + b.getId());
                }

                Role role = roleRepo.findByName(eRole);

                if (role == null) {
                    throw new EntityNotFoundEessiRuntimeException(Role.class, UniqueIdentifier.name, eRole.name());
                }

                AssignmentRequest assignmentRequest = new AssignmentRequest(b, b.getRinaCase(), role);
                assignmentRequest.setStatus(EAssignmentRequestStatus.PENDING);
                assignmentRequest.setId(b.getId());

                assignmentRequests.add(assignmentRequest);
            }
        }

        b.setAssignmentRequests(assignmentRequests);
    }
}
package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.document;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.MappingContextBuilder.*;
import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EDocumentDirection;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EDocumentStatus;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentConversation;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentTypeVersion;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.DocumentRepo;
import eu.ec.dgempl.eessi.rina.repo.DocumentTypeVersionRepo;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.DmtEnumNotFoundException;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.DocumentFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.UserService;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToDocumentMapper extends AbstractMapToEntityMapper<MapHolder, Document> {

    private static final Logger logger = LoggerFactory.getLogger(MapToDocumentMapper.class);

    private final DocumentRepo documentRepo;
    private final DocumentTypeVersionRepo documentTypeVersionRepo;
    private final RinaCaseRepo rinaCaseRepo;
    private final RinaJsonMapper rinaJsonMapper;
    private final UserService userService;

    public MapToDocumentMapper(final DocumentRepo documentRepo,
            final DocumentTypeVersionRepo documentTypeVersionRepo,
            final RinaCaseRepo rinaCaseRepo,
            final RinaJsonMapper rinaJsonMapper,
            final UserService userService) {
        this.documentRepo = documentRepo;
        this.documentTypeVersionRepo = documentTypeVersionRepo;
        this.rinaCaseRepo = rinaCaseRepo;
        this.rinaJsonMapper = rinaJsonMapper;
        this.userService = userService;
    }

    @Override
    public void mapAtoB(final MapHolder a, final Document b, final MappingContext context) {
        b.setId(a.string(DocumentFields.ID));

        mapCaseId(a, b);
        mapDocumentTypeVersion(a, b);

        b.setDisplayName(a.string(DocumentFields.DISPLAY_NAME));
        b.setName(b.getDocumentTypeVersion().getDocumentType().getName());
        b.setIsDummyDocument(a.bool(DocumentFields.IS_DUMMY_DOCUMENT, Boolean.FALSE));
        b.setMimeType(a.string(DocumentFields.MIME_TYPE));
        b.setInternalId(a.string(DocumentFields.INTERNAL_ID));
        b.setHasMultipleVersions(a.bool(DocumentFields.HAS_MULTIPLE_VERSIONS, Boolean.FALSE));
        b.setHasBusinessValidation(a.bool(DocumentFields.HAS_BUSINESS_VALIDATION, Boolean.FALSE));
        b.setHasCancel(a.bool(DocumentFields.HAS_CANCEL, Boolean.FALSE));
        b.setHasLetter(a.bool(DocumentFields.HAS_LETTER, Boolean.FALSE));
        b.setHasReject(a.bool(DocumentFields.HAS_REJECT, Boolean.FALSE));
        b.setHasReplyClarify(a.bool(DocumentFields.HAS_REPLY_CLARIFY, Boolean.FALSE));
        b.setIsStarter(a.bool(DocumentFields.STARTER, Boolean.FALSE));
        b.setIsMlc(a.bool(DocumentFields.IS_MLC, Boolean.FALSE));
        b.setSelectParticipants(a.bool(DocumentFields.SELECT_PARTICIPANTS, Boolean.FALSE));
        b.setIsAdmin(a.bool(DocumentFields.IS_ADMIN, Boolean.FALSE));
        b.setHasClarify(a.bool(DocumentFields.HAS_CLARIFY, Boolean.FALSE));
        b.setToSenderOnly(a.bool(DocumentFields.TO_SENDER_ONLY, Boolean.FALSE));
        b.setCreateTemplate(a.string(DocumentFields.CREATE_TEMPLATE));
        b.setAllowsAttachments(a.bool(DocumentFields.ALLOWS_ATTACHMENTS, Boolean.FALSE));
        b.setIsBulk(a.bool(DocumentFields.BULK, Boolean.FALSE));
        b.setIsFirstDocument(a.bool(DocumentFields.IS_FIRST_DOCUMENT, Boolean.FALSE));
        b.setCanBeSentWithoutChild(a.bool(DocumentFields.CAN_BE_SENT_WITHOUT_CHILD, Boolean.FALSE));
        b.setIsSendExecuted(a.bool(DocumentFields.IS_SEND_EXECUTED, Boolean.FALSE));

        mapDocOrder(a, b);
        mapDmProcessId(a, b);
        mapStatus(a, b);
        mapSubprocessId(a, b);
        mapDirection(a, b);
        mapConversations(a, b);
        mapParent(a, b);
        mapDate(a, DocumentFields.RECEIVE_DATE, b::setReceivedAt);
        mapReferenceManager(a, b);
        mapValidationErrors(a, b);
        mapIsValid(a, b);
        mapAudit(a, b);

    }

    private void mapAudit(final MapHolder a, final Document b) {
        setDefaultAudit(b::setAudit);

        if (b.getStatus() != EDocumentStatus.EMPTY) {

            String creatorId = a.string(DocumentFields.CREATOR_ID, true);
            String creatorName = a.string(DocumentFields.CREATOR_NAME, true);
            IamUser creator = userService.resolveUser(creatorId, creatorName, b.getRinaCase());

            b.getAudit().setCreatedBy(creator.getId());
            b.getAudit().setUpdatedBy(creator.getId());
        }
    }

    private void mapDocOrder(final MapHolder a, final Document b) {
        Integer docOrder = a.integer(DocumentFields.ORDER);
        b.setDocOrder(docOrder != null ? docOrder : 1);
    }

    private void mapDmProcessId(final MapHolder a, final Document b) {
        String dmProcessId = a.string(DocumentFields.DM_PROCESS_ID);
        if (dmProcessId != null) {
            b.setDmProcessId(Long.valueOf(dmProcessId));
        }
    }

    private void mapIsValid(final MapHolder a, final Document b) {
        String valid = a.string(DocumentFields.IS_VALID, true);
        if (StringUtils.isNotBlank(valid)) {
            b.setIsValid(valid.equalsIgnoreCase("valid"));
        }
    }

    private void mapValidationErrors(final MapHolder a, final Document b) {
        List<MapHolder> validationMessages = a.listToMapHolder(DocumentFields.VALIDATION_MESSAGES, true);
        if (CollectionUtils.isNotEmpty(validationMessages)) {
            try {
                List<Map<String, Object>> messages = validationMessages.stream().map(MapHolder::getHolding).collect(Collectors.toList());
                b.setValidationErrors(rinaJsonMapper.listOfObjectToJson(messages));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Could not map validation messages", e);
            }
        }
    }

    private void mapReferenceManager(final MapHolder a, final Document b) {
        Map<String, Object> referenceManager = a.getMap(DocumentFields.REFERENCE_MANAGER);
        if (MapUtils.isNotEmpty(referenceManager)) {
            try {
                b.setBusinessReferenceManager(rinaJsonMapper.mapToJson(referenceManager));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Could not map referenceManager", e);
            }
        }
    }

    private void mapParent(final MapHolder a, final Document b) {
        String parentDocumentId = a.string(DocumentFields.PARENT_DOCUMENT_ID);

        if (StringUtils.isNotBlank(parentDocumentId)) {
            String rinaCaseId = a.string(DocumentFields.CASE_ID);
            Document parent = documentRepo.findByIdAndRinaCaseId(parentDocumentId, rinaCaseId);

            if (parent == null) {
                throw new EntityNotFoundEessiRuntimeException(
                        Document.class,
                        UniqueIdentifier.id, parentDocumentId,
                        UniqueIdentifier.caseId, rinaCaseId);
            }

            b.setParent(parent);
        }
    }

    private void mapStatus(final MapHolder a, final Document b) {
        String status = a.string(DocumentFields.STATUS);
        EDocumentStatus eDocumentStatus = EDocumentStatus.lookup(status).orElseThrow(
                () -> new DmtEnumNotFoundException(EDocumentStatus.class, a.addPath(DocumentFields.STATUS), status)
        );
        b.setStatus(eDocumentStatus);
    }

    private void mapSubprocessId(final MapHolder a, final Document b) {
        Object subProcessId = a.getDeep(DocumentFields.SUB_PROCESS_ID);

        if (subProcessId != null) {
            b.setSubProcessId(Long.parseLong(String.valueOf(subProcessId)));
        }
    }

    private void mapDirection(final MapHolder a, final Document b) {
        String direction = a.string(DocumentFields.DIRECTION);
        EDocumentDirection eDocumentDirection = EDocumentDirection.lookup(direction).orElseThrow(
                () -> new DmtEnumNotFoundException(EDocumentDirection.class, a.addPath(DocumentFields.DIRECTION), direction)
        );
        b.setDirection(eDocumentDirection);
    }

    private void mapCaseId(final MapHolder a, final Document b) {
        String caseId = a.string(DocumentFields.CASE_ID);
        RinaCase rinaCase = findById(caseId, rinaCaseRepo::findById);

        if (rinaCase != null) {
            b.setRinaCase(rinaCase);
        }
    }

    private void mapDocumentTypeVersion(final MapHolder a, final Document b) {
        String documentType = a.string(DocumentFields.TYPE);
        String documentVersion = a.string(DocumentFields.TYPE_VERSION);
        DocumentTypeVersion documentTypeVersion = documentTypeVersionRepo.findByDocumentTypeTypeAndBusinessVersion(
                documentType,
                documentVersion);

        if (documentTypeVersion == null) {
            throw new EntityNotFoundEessiRuntimeException(
                    DocumentTypeVersion.class,
                    UniqueIdentifier.documentType,
                    documentType,
                    UniqueIdentifier.version,
                    documentVersion);
        }

        b.setDocumentTypeVersion(documentTypeVersion);
    }

    private void mapConversations(final MapHolder a, final Document b) {
        List<MapHolder> conversations = a.listToMapHolder(DocumentFields.CONVERSATIONS);
        if (CollectionUtils.isNotEmpty(conversations)) {
            conversations.stream()
                    .map(conversation -> mapperFacade.map(
                            conversation,
                            DocumentConversation.class,
                            mctxb()
                                    .addProp("doc", b)
                                    .build()))
                    .forEach(b::addDocumentConversation);
        }
    }
}

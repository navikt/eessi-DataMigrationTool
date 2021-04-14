package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.immutable;

import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;

import eu.ec.dgempl.eessi.rina.buc.core.model.EActionType;
import eu.ec.dgempl.eessi.rina.buc.core.model.ECaseRole;
import eu.ec.dgempl.eessi.rina.buc.core.model.EDocumentRole;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EActionStatus;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EApplicationRole;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ECaseStatus;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EConversationParticipantRole;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EDocumentDirection;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EOperationType;
import eu.ec.dgempl.eessi.rina.model.enumtypes.audit.EEventType;

public final class EnumDOMapper {

    private EnumDOMapper() {
        //
    }

    //@formatter:off
    public static final BiMap<EDocumentDirection, eu.ec.dgempl.eessi.rina.buc.api.model.DocumentMetadataDO.EDocumentDirection> DOCUMENT_DIRECTION_MAPPING
            = new ImmutableBiMap.Builder<EDocumentDirection, eu.ec.dgempl.eessi.rina.buc.api.model.DocumentMetadataDO.EDocumentDirection>()
            .put(EDocumentDirection.IN,  eu.ec.dgempl.eessi.rina.buc.api.model.DocumentMetadataDO.EDocumentDirection.IN)
            .put(EDocumentDirection.OUT,  eu.ec.dgempl.eessi.rina.buc.api.model.DocumentMetadataDO.EDocumentDirection.OUT)
            .build();
    //@formatter:on

    //@formatter:off
    public static final BiMap<EConversationParticipantRole, EDocumentRole> CONVERSATION_ROLE_MAPPING = new ImmutableBiMap.Builder<EConversationParticipantRole, EDocumentRole>()
            .put(EConversationParticipantRole.PARTICIPANT, EDocumentRole.PARTICIPANT)
            .put(EConversationParticipantRole.SENDER, EDocumentRole.SENDER)
            .put(EConversationParticipantRole.RECEIVER, EDocumentRole.RECEIVER)
            .build();
    //@formatter:on

    //@formatter:off
    public static final BiMap<EApplicationRole, ECaseRole> CASE_ROLE_MAPPING = new ImmutableBiMap.Builder<EApplicationRole, ECaseRole>()
            .put(EApplicationRole.PO, ECaseRole.PO)
            .put(EApplicationRole.CP, ECaseRole.CP)
            .build();
    //@formatter:on

    //@formatter:off
    public static final BiMap<ECaseStatus, eu.ec.dgempl.eessi.rina.buc.core.model.ECaseStatus> CASE_STATUS_MAPPING = new ImmutableBiMap.Builder<eu.ec.dgempl.eessi.rina.model.enumtypes.ECaseStatus, eu.ec.dgempl.eessi.rina.buc.core.model.ECaseStatus>()
            .put(eu.ec.dgempl.eessi.rina.model.enumtypes.ECaseStatus.OPEN, eu.ec.dgempl.eessi.rina.buc.core.model.ECaseStatus.OPEN)
            .put(eu.ec.dgempl.eessi.rina.model.enumtypes.ECaseStatus.ARCHIVED, eu.ec.dgempl.eessi.rina.buc.core.model.ECaseStatus.ARCHIVED)
            .put(eu.ec.dgempl.eessi.rina.model.enumtypes.ECaseStatus.CLOSED, eu.ec.dgempl.eessi.rina.buc.core.model.ECaseStatus.CLOSED)
            .put(eu.ec.dgempl.eessi.rina.model.enumtypes.ECaseStatus.REMOVED, eu.ec.dgempl.eessi.rina.buc.core.model.ECaseStatus.REMOVED)
            .build();
    //@formatter:on

    //@formatter:off
    public static final BiMap<EOperationType, EActionType> ACTION_TYPE_MAPPING = new ImmutableBiMap.Builder<EOperationType, EActionType>()
            .put(EOperationType.CREATE_CASE, EActionType.CASE_CREATE)
            .put(EOperationType.ARCHIVE_CASE, EActionType.CASE_ARCHIVE)
            .put(EOperationType.SELECT_PARTICIPANTS, EActionType.CASE_SET_PARTICIPANTS)
            .put(EOperationType.UPDATE_PARTICIPANTS, EActionType.CASE_UPDATE_PARTICIPANTS)
            .put(EOperationType.DELETE_CASE, EActionType.CASE_DELETE)
            .put(EOperationType.FORWARD_CASE, EActionType.CASE_FORWARD)
            .put(EOperationType.LOCAL_CLOSE, EActionType.CASE_LOCAL_CLOSE)
            .put(EOperationType.LOCAL_REOPEN, EActionType.CASE_LOCAL_REOPEN)
            .put(EOperationType.CREATE, EActionType.DOC_CREATE)
            .put(EOperationType.CREATE_CHILD, EActionType.DOC_CREATE_CHILD)
            .put(EOperationType.CREATE_REPLY, EActionType.DOC_CREATE_REPLY)
            .put(EOperationType.UPDATE, EActionType.DOC_UPDATE)
            .put(EOperationType.READ, EActionType.DOC_READ)
            .put(EOperationType.READ_PARTICIPANTS, EActionType.DOC_READ_PARTICIPANTS)
            .put(EOperationType.SEND_PARTICIPANTS, EActionType.DOC_SEND_PARTICIPANTS)
            .put(EOperationType.SEND, EActionType.DOC_SEND)
            .put(EOperationType.CANCEL, EActionType.DOC_CANCEL)
            .put(EOperationType.CANCEL_RECEIVE, EActionType.DOC_CANCEL_RECEIVE)
            .put(EOperationType.DELETE, EActionType.DOC_DELETE)
            .put(EOperationType.RECEIVE, EActionType.DOC_RECEIVE)
            .put(EOperationType.RECEIVE_UPDATE, EActionType.DOC_UPDATE_RECEIVE)
            .put(EOperationType.RECEIVE_REPLY, EActionType.DOC_RECEIVE_REPLY)
            .put(EOperationType.ADD_ATTACHMENT, EActionType.ADD_ATTACHMENT)
            .put(EOperationType.REMOVE_ATTACHMENT, EActionType.REMOVE_ATTACHMENT)
            .put(EOperationType.ADD_SUBDOCUMENT, EActionType.ADD_SUBDOCUMENT)
            .put(EOperationType.UPDATE_SUBDOCUMENT, EActionType.UPDATE_SUBDOCUMENT)
            .put(EOperationType.REMOVE_SUBDOCUMENT, EActionType.REMOVE_SUBDOCUMENT)
            .put(EOperationType.IMPORT_SUBDOCUMENT, EActionType.IMPORT_SUBDOCUMENT)
            .put(EOperationType.SUBDOCUMENT, EActionType.READ_SUBDOCUMENT)
            .put(EOperationType.REQUEST_APPROVAL, EActionType.REQUEST_APPROVAL)
            .put(EOperationType.RESTORE_CASE, EActionType.RESTORE_CASE)
            .build();
    //@formatter:on

    //@formatter:off
    public static final BiMap<EActionStatus, eu.ec.dgempl.eessi.rina.buc.core.model.EActionStatus> ACTION_STATUS_MAPPING =
            new ImmutableBiMap.Builder<eu.ec.dgempl.eessi.rina.model.enumtypes.EActionStatus, eu.ec.dgempl.eessi.rina.buc.core.model.EActionStatus>()
                    .put(EActionStatus.ACTIVE, eu.ec.dgempl.eessi.rina.buc.core.model.EActionStatus.ACTIVE)
                    .put(EActionStatus.CANCELLED, eu.ec.dgempl.eessi.rina.buc.core.model.EActionStatus.CANCELLED)
                    .put(EActionStatus.EXECUTED, eu.ec.dgempl.eessi.rina.buc.core.model.EActionStatus.EXECUTED)
                    .put(EActionStatus.SUSPENDED, eu.ec.dgempl.eessi.rina.buc.core.model.EActionStatus.SUSPENDED)
                    .build();
    //@formatter:on

    //TODO: Audit move to another mapper
    //@formatter:off
    public static final Map<EOperationType, EEventType> OPERATION_TYPE_EVENT_TYPE_MAPPING =
            new ImmutableMap.Builder<EOperationType, EEventType>()
                    .put(EOperationType.CREATE, EEventType.CREATE_DOCUMENT)
                    .put(EOperationType.UPDATE,EEventType.UPDATE_DOCUMENT)

                    .put(EOperationType.DELETE,EEventType.DELETE_DOCUMENT)
                    .put(EOperationType.SEND,EEventType.SEND_DOCUMENT)
                    .put(EOperationType.UPDATE_SUBDOCUMENT,EEventType.UPDATE_DOCUMENT)
                    .put(EOperationType.ADD_SUBDOCUMENT,EEventType.CREATE_DOCUMENT)
                    .put(EOperationType.IMPORT_SUBDOCUMENT,EEventType.IMPORT_BATCH)

                    .build();
    //@formatter:on

}

package eu.ec.dgempl.eessi.rina.tool.migration.buc.timedaction;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.buc.api.model.ActionDO;
import eu.ec.dgempl.eessi.rina.buc.api.utils.IdHelper;
import eu.ec.dgempl.eessi.rina.buc.core.model.EActionStatus;
import eu.ec.dgempl.eessi.rina.buc.core.model.EActionType;
import eu.ec.dgempl.eessi.rina.buc.core.model.ECaseRole;
import eu.ec.dgempl.eessi.rina.buc.core.model.EDocumentType;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EConversationParticipantRole;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EDocumentDirection;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EDocumentStatus;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.CaseParticipant;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.ConversationParticipant;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentConversation;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentTypeVersion;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.UserMessage;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.UserMessageResponse;
import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.Audit;
import eu.ec.dgempl.eessi.rina.repo.DocumentConversationRepo;
import eu.ec.dgempl.eessi.rina.repo.DocumentRepo;
import eu.ec.dgempl.eessi.rina.repo.DocumentRepoExtended;
import eu.ec.dgempl.eessi.rina.repo.DocumentTypeVersionRepo;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.buc.BucHandler;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.ActionService;

/**
 * BUC Handler, which creates timed actions for the defined cases. Based on the business rule for every case matching the filter will
 * created timed actions.
 */
@Component
public class TimedActionBucHandler implements BucHandler {

    private static final Logger logger = LoggerFactory.getLogger(TimedActionBucHandler.class);

    private final DocumentRepo documentRepo;
    private final DocumentRepoExtended documentRepoExtended;
    private final DocumentConversationRepo documentConversationRepo;
    private final RinaCaseRepo rinaCaseRepo;
    private final DocumentTypeVersionRepo documentTypeVersionRepo;
    private final ActionService actionService;

    private final Map<String, TimedActionDefinition> ACTION_DEFS = Stream
            .of(new TimedActionDefinition[] { new TimedActionDefinition("PO_LA_BUC_02", EDocumentType.A_003, 60),
                    new TimedActionDefinition("PO_LA_BUC_03", EDocumentType.A_008, 30),
                    new TimedActionDefinition("PO_LA_BUC_04", EDocumentType.A_009, 30),
                    new TimedActionDefinition("PO_LA_BUC_05", EDocumentType.A_010, 30),
                    new TimedActionDefinition("PO_LA_BUC_06", EDocumentType.A_005, 30) })
            .collect(Collectors.toMap(TimedActionDefinition::getBucType, d -> d));

    public TimedActionBucHandler(final DocumentRepo documentRepo, final DocumentRepoExtended documentRepoExtended,
            final DocumentConversationRepo documentConversationRepo, final RinaCaseRepo rinaCaseRepo,
            final DocumentTypeVersionRepo documentTypeVersionRepo, final ActionService actionService) {
        this.documentRepo = documentRepo;
        this.documentRepoExtended = documentRepoExtended;
        this.documentConversationRepo = documentConversationRepo;
        this.rinaCaseRepo = rinaCaseRepo;
        this.documentTypeVersionRepo = documentTypeVersionRepo;
        this.actionService = actionService;
    }

    /**
     * Checks that the case is matching the filter criteria for the timed actions as defined in this class If yes it will create the actions
     * with all the needed docs.
     *
     * @param caseId
     * @param bucType
     * @param caseRole
     * @param modelVersion
     * @throws Exception
     */
    @Override
    public void processCase(String caseId, String bucType, ECaseRole caseRole, String modelVersion) throws Exception {

        PreconditionsHelper.notEmpty(caseId, "caseId");

        logger.debug("Processing TIMED actions for the case [caseId={}]", caseId);

        RinaCase rinaCase = rinaCaseRepo.findById(caseId);
        String buc = (caseRole.value() + "_" + bucType).toUpperCase();

        // filter only some case types
        if (needsTimedActions(buc) && hasX001Action(rinaCase) == false) {

            logger.debug("Rina case is eligible for creation of TIMED actions [caseId={}]", caseId);

            // process the timed actions
            processTimedActions(rinaCase, ACTION_DEFS.get(buc));

        }

    }

    /**
     * Retrieves the last document of type from the timed action definition. Based on the original create date of the document will create a
     * timed action and the X001 document.
     *
     * @param rinaCase
     * @param def
     */
    protected void processTimedActions(final RinaCase rinaCase, final TimedActionDefinition def) {

        PreconditionsHelper.notNull(rinaCase, "rinaCase");
        PreconditionsHelper.notNull(def, "def");

        // get the document which should trigger the timed action
        Document doc = getLastDocument(rinaCase, def.getTriggeringSed());

        if (doc != null) {

            logger.debug("Found last document of type [docId={}, type={}] for the case [caseId={}]. Going to create TIMED action for X001",
                    doc.getId(), def.getTriggeringSed(), rinaCase.getId());

            // get the date
            ZonedDateTime conversationDate = getLastDocumentSentDate(doc);

            // compute the delayed time
            ZonedDateTime delayedTime = conversationDate.plusDays(def.delayInDays);

            // create the X001 empty document
            Document x001 = createEmptyX001Document(rinaCase);

            // create action
            ActionDO action = createTimedAction(rinaCase.getId(), x001.getId(), Date.from(delayedTime.toInstant()));
            actionService.saveAction(action);
        }

    }

    /**
     * Get the correct document type version
     *
     * @param businessVersion
     * @return
     */
    protected DocumentTypeVersion getDocumentTypeVersion(String businessVersion) {

        return documentTypeVersionRepo.findByDocumentTypeTypeAndBusinessVersion(EDocumentType.X_001.value(), businessVersion);

    }

    /**
     * Creates the document X001 for the timed action
     *
     * @param rinaCase
     * @return
     */
    public Document createEmptyX001Document(final RinaCase rinaCase) {

        PreconditionsHelper.notNull(rinaCase, "rinaCase");

        Document document = new Document();
        document.setId(IdHelper.getNewUUID());
        document.setRinaCase(rinaCase);
        document.setIsAdmin(true);
        document.setDocumentTypeVersion(getDocumentTypeVersion(rinaCase.getProcessDefVersion().getBusinessVersion()));
        document.setIsStarter(false);
        document.setIsReply(false);
        document.setAllowsAttachments(false);
        document.setHasBusinessValidation(true);
        document.setIsBulk(false);
        document.setOriginalCreatedAt(ZonedDateTime.now());
        document.setIsMlc(true);
        document.setAllowsAttachments(false);
        document.setStatus(EDocumentStatus.EMPTY);
        document.setDirection(EDocumentDirection.OUT);

        List<ConversationParticipant> participants = new ArrayList<>();
        for (CaseParticipant part : rinaCase.getParticipants()) {

            // create participant based on tenant
            if (rinaCase.getTenant().getId().equals(part.getOrganisation().getId())) {
                participants.add(new ConversationParticipant(part.getOrganisation(), EConversationParticipantRole.SENDER));
            } else {
                participants.add(new ConversationParticipant(part.getOrganisation(), EConversationParticipantRole.RECEIVER));
            }

        }

        // set conversation
        DocumentConversation conversation = new DocumentConversation(document, IdHelper.getNewUUID());
        participants.forEach(conversation::addConversationParticipant);

        document.addDocumentConversation(conversation);

        // setup the created on
        Audit audit = new Audit();

        audit.setCreatedAt(ZonedDateTime.now());
        audit.setUpdatedAt(ZonedDateTime.now());

        audit.setCreatedBy("0");
        audit.setUpdatedBy("0");

        document.setAudit(audit);

        // persist
        documentRepo.saveAndFlush(document);
        documentConversationRepo.saveAndFlush(conversation);

        return document;

    }

    /**
     * Returns the timed action to be created
     *
     * @param caseId
     * @param documentId
     * @param availableFrom
     * @return
     */
    public ActionDO createTimedAction(final String caseId, final String documentId, final Date availableFrom) {

        PreconditionsHelper.notEmpty(caseId, "caseId");
        PreconditionsHelper.notNull(availableFrom, "availableFrom");

        ActionDO actionDO = new ActionDO();
        actionDO.setId(UUID.randomUUID().toString());
        actionDO.setStatus(EActionStatus.ACTIVE);
        actionDO.setType(EActionType.DOC_CREATE);
        actionDO.setCanClose(true);
        actionDO.setCaseId(caseId);
        actionDO.setDocumentType(EDocumentType.X_001);
        actionDO.setAvailableFrom(availableFrom);
        actionDO.setDocumentId(documentId);

        return actionDO;
    }

    /**
     * Gets the last SENT document of the given type.
     *
     * @param rinaCase
     * @param documentType
     * @return
     */
    protected Document getLastDocument(final RinaCase rinaCase, final EDocumentType documentType) {

        PreconditionsHelper.notNull(rinaCase, "rinaCase");
        PreconditionsHelper.notNull(documentType, "documentType");
        // @formatter:off
        List<Document> documents = documentRepoExtended.findByRinaCaseId(rinaCase.getId());

        return documents.stream()
                .filter(d ->
                        documentType.value().equals(d.getDocumentTypeVersion().getDocumentType().getType()) &&
                                (
                                        EDocumentStatus.SENT.equals(d.getStatus()) || EDocumentStatus.ACTIVE.equals(d.getStatus())
                                )
                ).findFirst()
                .orElse(null);
        // @formatter:on

    }

    /**
     * Retrieves the date of the last conversation of the given document
     *
     * @param document
     * @return
     */
    protected ZonedDateTime getLastDocumentSentDate(final Document document) {

        PreconditionsHelper.notNull(document, "document");
        // @formatter:off
        return document.getDocumentConversations().stream()
                .map(this::getConversationDate)
                .filter(Objects::nonNull)
                .max(ZonedDateTime::compareTo)
                .orElseThrow(() -> new IllegalStateException("Could not find the last conversation for document [documentId=" + document.getId()+" ]" ));
        // @formatter:on
    }

    private ZonedDateTime getConversationDate(DocumentConversation documentConversation) {
        ZonedDateTime date = documentConversation.getDate();

        if (date != null) {
            return date;
        }

        // @formatter:off
        date = documentConversation.getUserMessages().stream()
                .map(UserMessage::getUserMessageResponse)
                .filter(Objects::nonNull)
                .map(UserMessageResponse::getLastUpdate)
                .filter(Objects::nonNull)
                .min(ZonedDateTime::compareTo)
                .orElse(null);
        // @formatter:on

        if (date != null) {
            return date;
        }

        return documentConversation.getDocument().getAudit().getUpdatedAt();
    }

    /**
     * Returns {@code true} if the case contains any action with a document type X001
     *
     * @param rinaCase
     * @return
     */
    protected boolean hasX001Action(final RinaCase rinaCase) {

        return rinaCase.getActions().stream().anyMatch(a -> a.getDocument() != null
                && EDocumentType.X_001.value().equals(a.getDocument().getDocumentTypeVersion().getDocumentType().getType()));

    }

    /**
     * Returns {@code true} if the timed actions should be processed for the buc type.
     *
     * @param bucType
     * @return
     */
    protected boolean needsTimedActions(String bucType) {

        return ACTION_DEFS.containsKey(bucType);

    }

    /**
     * Wrapper arround definition of the timed action
     */
    class TimedActionDefinition {

        private final String bucType;

        private final EDocumentType triggeringSed;

        private final int delayInDays;

        public TimedActionDefinition(String bucType, EDocumentType triggeringSed, int delayInDays) {
            this.bucType = bucType;
            this.triggeringSed = triggeringSed;
            this.delayInDays = delayInDays;
        }

        public String getBucType() {
            return bucType;
        }

        public EDocumentType getTriggeringSed() {
            return triggeringSed;
        }

        public int getDelayInDays() {
            return delayInDays;
        }
    }
}

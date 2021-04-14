package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases;

import java.util.Arrays;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EActionStatus;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EOperationType;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ERole;
import eu.ec.dgempl.eessi.rina.model.enumtypes.portal.ETagCategory;
import eu.ec.dgempl.eessi.rina.model.enumtypes.portal.ETagType;
import eu.ec.dgempl.eessi.rina.model.exception.runtime.enums.EnumNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Action;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.ActionTag;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentTypeVersion;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.repo.DocumentRepo;
import eu.ec.dgempl.eessi.rina.repo.DocumentTypeVersionRepo;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.ActionFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToActionMapper extends AbstractMapToEntityMapper<MapHolder, Action> {

    private final DocumentRepo documentRepo;
    private final DocumentTypeVersionRepo documentTypeVersionRepo;
    private final RinaCaseRepo rinaCaseRepo;

    public MapToActionMapper(final DocumentRepo documentRepo,
            final DocumentTypeVersionRepo documentTypeVersionRepo, final RinaCaseRepo rinaCaseRepo) {
        this.documentRepo = documentRepo;
        this.documentTypeVersionRepo = documentTypeVersionRepo;
        this.rinaCaseRepo = rinaCaseRepo;
    }

    @Override
    public void mapAtoB(final MapHolder a, final Action b, final MappingContext context) {
        b.setId(a.string(ActionFields.ID));
        b.setName(a.string(ActionFields.NAME));

        mapActor(a, b);
        mapRinaCase(a, b);
        mapDocument(a, b);
        mapDocumentTypeVersion(a, b);
        mapHasBusinessValidation(a, b);
        mapOperation(a, b);
        mapStatus(a, b);
        mapActionTag(a, b);
        mapLog(a, b);

        b.setIsBulk(a.bool(ActionFields.BULK, Boolean.FALSE));
        b.setCanClose(a.bool(ActionFields.CAN_CLOSE, Boolean.FALSE));
        b.setIsCaseRelated(a.bool(ActionFields.CASE_RELATED, Boolean.FALSE));
        b.setDisplayName(a.string(ActionFields.DISPLAY_NAME));
        b.setIsDocumentRelated(a.bool(ActionFields.DOCUMENT_RELATED, Boolean.FALSE));
        b.setIsBulk(a.bool(ActionFields.IS_BULK, Boolean.FALSE));
        b.setIsCaseRelated(a.bool(ActionFields.IS_CASE_RELATED, Boolean.FALSE));
        b.setIsDocumentRelated(a.bool(ActionFields.IS_DOCUMENT_RELATED, Boolean.FALSE));
        b.setDisplayInDocumentId(a.string(ActionFields.PARENT_DOCUMENT_ID));
        b.setRequiresValidDocument(a.bool(ActionFields.REQUIRES_VALID_DOCUMENT, Boolean.FALSE));
        b.setTemplate(a.string(ActionFields.TEMPLATE));
        b.setDisplayType(a.string(ActionFields.TYPE));
        b.setTemplateVersion(a.string(ActionFields.TEMPLATE_TYPE_VERSION));
    }

    private void mapLog(final MapHolder a, final Action b) {
        setDefaultLog(b::setLog);
    }

    private void mapActor(final MapHolder a, final Action b) {
        String actor = a.string(ActionFields.ACTOR);
        ERole eRole = ERole.lookup(actor).orElseThrow(EnumNotFoundEessiRuntimeException::new);
        b.setActor(eRole);
    }

    private void mapRinaCase(final MapHolder a, final Action b) {
        String caseId = a.string(ActionFields.CASE_ID);

        RinaCase rinaCase = rinaCaseRepo.findById(caseId);

        if (rinaCase != null) {
            b.setRinaCase(rinaCase);
        }
    }

    private void mapDocument(final MapHolder a, final Action b) {
        String caseId = a.string(ActionFields.CASE_ID);
        String documentId = a.string(ActionFields.DOCUMENT_ID);

        Document document = documentRepo.findByIdAndRinaCaseId(documentId, caseId);

        if (document != null) {
            b.setDocument(document);
        }
    }

    private void mapDocumentTypeVersion(final MapHolder a, final Action b) {
        String version = a.string(ActionFields.TYPE_VERSION);
        String type = a.string(ActionFields.DOCUMENT_TYPE);

        DocumentTypeVersion documentTypeVersion = documentTypeVersionRepo.findByDocumentTypeTypeAndBusinessVersion(type, version);

        if (documentTypeVersion != null) {
            b.setDocumentTypeVersion(documentTypeVersion);
        }
    }

    private void mapHasBusinessValidation(final MapHolder a, final Action b) {
        Boolean hasBusinessValidation = a.bool(ActionFields.HAS_BUSINESS_VALIDATION, Boolean.FALSE);
        Boolean hasSendValidationOnBulk = a.bool(ActionFields.HAS_SEND_VALIDATION_ON_BULK, Boolean.FALSE);
        b.setHasBusinessValidation(hasBusinessValidation || hasSendValidationOnBulk);
    }

    private void mapOperation(final MapHolder a, final Action b) {
        String operation = a.string(ActionFields.OPERATION);

        EOperationType eOperationType = Arrays.stream(EOperationType.values())
                .filter(operationType ->
                        operationType.getTypeName().equalsIgnoreCase(operation) || operationType.name().equalsIgnoreCase(operation))
                .findFirst()
                .orElseThrow(EnumNotFoundEessiRuntimeException::new);

        b.setOperationType(eOperationType);
    }

    private void mapStatus(final MapHolder a, final Action b) {
        String status = a.string(ActionFields.STATUS);
        EActionStatus eActionStatus = EActionStatus.fromString(status);
        b.setStatus(eActionStatus);
    }

    private void mapActionTag(final MapHolder a, final Action b) {
        String tagType = a.string(ActionFields.TAGS_TYPE, true);
        String tagCategory = a.string(ActionFields.TAGS_CATEGORY, true);

        ETagType eTagType = ETagType.fromString(tagType);
        ETagCategory eTagCategory = ETagCategory.fromString(tagCategory);

        b.setActionTag(new ActionTag(b, eTagType, eTagCategory));
    }

}

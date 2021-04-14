package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.doToEntityMapper;

import static eu.ec.dgempl.eessi.rina.model.enumtypes.EOperationType.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.buc.api.model.ActionDO;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EOperationType;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ERole;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Action;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentTypeVersion;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.repo.DocumentRepo;
import eu.ec.dgempl.eessi.rina.repo.DocumentTypeVersionRepo;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.doToEntityMapper._abstract.AbstractDoToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.immutable.EnumDOMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class ActionDOToActionMapper extends AbstractDoToEntityMapper<ActionDO, Action> {

    public static final Logger logger = LoggerFactory.getLogger(ActionDOToActionMapper.class);

    // REPOs
    private final DocumentRepo documentRepo;
    private final DocumentTypeVersionRepo documentTypeVersionRepo;
    private final RinaCaseRepo rinaCaseRepo;

    @Autowired
    // @formatter:off
    public ActionDOToActionMapper(
            final DocumentRepo documentRepo,
            final DocumentTypeVersionRepo documentTypeVersionRepo,
            final RinaCaseRepo rinaCaseRepo
    ) {
        // @formatter:on
        this.documentRepo = documentRepo;
        this.documentTypeVersionRepo = documentTypeVersionRepo;
        this.rinaCaseRepo = rinaCaseRepo;
    }

    @Override
    public void mapAtoB(ActionDO a, Action b, MappingContext context) {

        EOperationType operationType = EnumDOMapper.ACTION_TYPE_MAPPING.inverse().get(a.getType());
        if (operationType == null) {
            throw new IllegalArgumentException("Operation type unknown: " + a.getType());
        }

        b.setId(a.getId());
        b.setStatus(EnumDOMapper.ACTION_STATUS_MAPPING.inverse().get(a.getStatus()));
        b.setOperationType(operationType);
        b.setCanClose(a.isCanClose());

        RinaCase rinaCase = b.getRinaCase();
        if (a.getCaseId() != null && rinaCase == null) {
            rinaCase = rinaCaseRepo.findById(a.getCaseId());
            b.setRinaCase(rinaCase);
        }

        if (a.getDocumentId() != null && a.getCaseId() != null && b.getDocument() == null) {
            Document document = documentRepo.findByIdAndRinaCase(a.getDocumentId(), rinaCase);
            b.setDocument(document);
        }

        if (a.getParentDocumentId() != null && a.getCaseId() != null && b.getParentDocument() == null) {
            Document document = documentRepo.findByIdAndRinaCase(a.getParentDocumentId(), rinaCase);
            b.setParentDocument(document);
        }

        if (rinaCase != null && a.getDocumentType() != null && b.getDocumentTypeVersion() == null) {
            DocumentTypeVersion documentTypeVersion = documentTypeVersionRepo.findByDocumentTypeTypeAndBusinessVersion(
                    a.getDocumentType().value(), rinaCase.getProcessDefVersion().getBusinessVersion());
            b.setDocumentTypeVersion(documentTypeVersion);
        }

        if (a.getAvailableFrom() != null) {
            b.setAvailableFrom(ZonedDateTime.ofInstant(a.getAvailableFrom().toInstant(), ZoneId.systemDefault()));
        }

        fillDefaultValues(b);

        mapLog(a, b);
    }

    private void mapLog(final ActionDO a, final Action b) {
        setDefaultLog(b::setLog);
    }

    private void fillDefaultValues(Action b) {

        List<EOperationType> REQUIRES_VALID_DOCUMENT = List.of(
                SELECT_PARTICIPANTS, READ_PARTICIPANTS, SEND, SEND_PARTICIPANTS, REQUEST_APPROVAL);

        EOperationType operationType = b.getOperationType();

        b.setName(operationType.getTypeName());
        b.setDisplayName(operationType.getTypeName());
        b.setActor(EOperationType.REQUEST_APPROVAL == operationType ? ERole.UNAUTHORIZED_CLERK : ERole.AUTHORIZED_CLERK);
        b.setRequiresValidDocument(REQUIRES_VALID_DOCUMENT.contains(operationType));

        Document document = b.getDocument();
        if (document != null) {
            b.setIsBulk(document.isBulk());
            b.setHasBusinessValidation(document.hasBusinessValidation());
        }

        boolean isDocumentRelated = document != null;
        b.setIsDocumentRelated(isDocumentRelated);
        b.setIsCaseRelated(!isDocumentRelated);
    }
}

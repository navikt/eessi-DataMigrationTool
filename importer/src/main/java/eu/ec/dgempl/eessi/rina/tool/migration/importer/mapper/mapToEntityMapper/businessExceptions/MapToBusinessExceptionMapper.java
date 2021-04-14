package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.businessExceptions;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.BusinessException;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.PendingMessage;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.DocumentRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.BusinessExceptionFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToBusinessExceptionMapper extends AbstractMapToEntityMapper<MapHolder, BusinessException> {

    private final String DEFAULT_BUSINESS_EXCEPTION_CASE_ID = "0";

    private final DocumentRepo documentRepo;

    public MapToBusinessExceptionMapper(final DocumentRepo documentRepo) {
        this.documentRepo = documentRepo;
    }

    @Override
    public void mapAtoB(final MapHolder a, final BusinessException b, final MappingContext context) {

        b.setId(a.string(BusinessExceptionFields.ID));

        b.setReason(a.string(BusinessExceptionFields.REASON));

        String documentId = a.string(BusinessExceptionFields.DOCUMENT_ID, true);

        Document document = documentRepo.findByIdAndRinaCaseId(documentId, DEFAULT_BUSINESS_EXCEPTION_CASE_ID);

        if (document == null) {

            // documents like X050 are saved during import with caseId=null
            document = documentRepo.findByIdAndRinaCaseId(documentId, null);

            if (document == null) {
                throw new EntityNotFoundEessiRuntimeException(Document.class, UniqueIdentifier.id, documentId);
            }
        }

        b.setDocument(document);

        MapHolder pendingMessageHolder = a.getMapHolder(BusinessExceptionFields.SOURCE);

        PendingMessage pendingMessage = mapperFacade.map(pendingMessageHolder, PendingMessage.class);

        pendingMessage.setIsProcessed(true);
        b.setPendingMessage(pendingMessage);

        mapDate(a, BusinessExceptionFields.DATE, b::setDate);
    }
}

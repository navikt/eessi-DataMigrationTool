package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.subdocument;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Subdocument;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.SubdocumentContent;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.SubdocumentRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.DocumentContentFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToSubdocumentContentMapper extends AbstractMapToEntityMapper<MapHolder, SubdocumentContent> {

    private final SubdocumentRepo subdocumentRepo;
    private final RinaJsonMapper rinaJsonMapper;

    public MapToSubdocumentContentMapper(final SubdocumentRepo subdocumentRepo,
            final RinaJsonMapper rinaJsonMapper) {
        this.subdocumentRepo = subdocumentRepo;
        this.rinaJsonMapper = rinaJsonMapper;
    }

    @Override
    public void mapAtoB(final MapHolder a, final SubdocumentContent b, final MappingContext context) {
        mapSubdocument(a, b);
        mapContent(a, b);
    }

    private void mapContent(final MapHolder a, final SubdocumentContent b) {
        Map<String, Object> content = a.getMap(DocumentContentFields.CONTENT);
        if (MapUtils.isNotEmpty(content)) {
            try {
                b.setContent(rinaJsonMapper.mapToJson(content));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Could not map document content for subdocument: " + b.getSubdocument().getId(), e);
            }
        }
    }

    private void mapSubdocument(final MapHolder a, final SubdocumentContent b) {
        String subdocumentId = a.string(DocumentContentFields.ID);
        String parentDocumentId = a.string(DocumentContentFields.PARENT_DOCUMENT_ID);
        String caseId = a.string(DocumentContentFields.CASE_ID);

        Subdocument subdocument = subdocumentRepo.findByIdAndDocumentIdAndRinaCaseIdAndIsActiveTrue(
                subdocumentId,
                parentDocumentId,
                caseId);

        if (subdocument == null) {
            throw new EntityNotFoundEessiRuntimeException(Document.class, UniqueIdentifier.id, subdocumentId, UniqueIdentifier.caseId,
                    caseId);
        }

        b.setSubdocument(subdocument);
    }

}

package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.document;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentContent;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.DocumentRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.DocumentContentFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.CasesUtils;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToDocumentContentMapper extends AbstractMapToEntityMapper<MapHolder, DocumentContent> {

    private final DocumentRepo documentRepo;
    private final RinaJsonMapper rinaJsonMapper;

    public MapToDocumentContentMapper(final DocumentRepo documentRepo,
            final RinaJsonMapper rinaJsonMapper) {
        this.documentRepo = documentRepo;
        this.rinaJsonMapper = rinaJsonMapper;
    }

    @Override
    public void mapAtoB(final MapHolder a, final DocumentContent b, final MappingContext context) {
        mapDocument(a, b);
        mapContent(a, b);
    }

    private void mapContent(final MapHolder a, final DocumentContent b) {
        Map<String, Object> content = a.getMap(DocumentContentFields.CONTENT);
        if (MapUtils.isNotEmpty(content)) {
            try {
                b.setContent(rinaJsonMapper.mapToJson(content));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Could not map document content for document: " + b.getDocument().getId(), e);
            }
        }
    }

    private void mapDocument(final MapHolder a, final DocumentContent b) {
        String documentId = a.string(DocumentContentFields.ID);
        String caseId = a.string(DocumentContentFields.CASE_ID);

        Document document = documentRepo.findByIdAndRinaCaseId(documentId, CasesUtils.getCaseId(caseId));
        if (document == null) {
            throw new EntityNotFoundEessiRuntimeException(Document.class, UniqueIdentifier.id, documentId, UniqueIdentifier.caseId, caseId);
        }

        b.setDocument(document);
    }

}

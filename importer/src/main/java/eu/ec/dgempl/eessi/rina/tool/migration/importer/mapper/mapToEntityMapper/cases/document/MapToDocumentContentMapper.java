package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.document;

import java.util.Map;

import javax.xml.bind.ValidationException;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.ec.dgempl.eessi.rina.commons.transformation.SchemaAwareJson2XmlConverter;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EValidationXSDTypes;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentContent;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentTypeVersion;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.DocumentRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.DocumentContentFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.helper.GlobalParamHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.CasesUtils;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToDocumentContentMapper extends AbstractMapToEntityMapper<MapHolder, DocumentContent> {

    private final DocumentRepo documentRepo;

    @Autowired
    private GlobalParamHelper globalParamHelper;

    public MapToDocumentContentMapper(final DocumentRepo documentRepo) {
        this.documentRepo = documentRepo;
    }

    @Override
    public void mapAtoB(final MapHolder a, final DocumentContent b, final MappingContext context) {
        mapDocument(a, b);
        mapContent(a, b);
    }

    private void mapContent(final MapHolder a, final DocumentContent b) {
        Map<String, Object> content = a.getMap(DocumentContentFields.CONTENT);
        if (MapUtils.isNotEmpty(content)) {
            Document document = b.getDocument();
            try {
                DocumentTypeVersion documentTypeVersion = document.getDocumentTypeVersion();
                String type = documentTypeVersion.getDocumentType().getType();
                String businessVersion = documentTypeVersion.getBusinessVersion();

                boolean isBulk = document.isBulk();
                if (isBulk && !"3.2".equals(businessVersion)) {
                    type += "_Master";
                }

                String xsdPath = globalParamHelper.getXSDPath(
                        type,
                        businessVersion,
                        EValidationXSDTypes.Full);

                SchemaAwareJson2XmlConverter schemaAwareJson2XmlConverter = new SchemaAwareJson2XmlConverter(xsdPath);

                b.setContent(schemaAwareJson2XmlConverter.toJson(schemaAwareJson2XmlConverter.toXml(content)));
            } catch (JsonProcessingException | ValidationException e) {
                throw new RuntimeException("Could not map document content for document: " + document.getId(), e);
            }
        } else {
            throw new RuntimeException(String.format("No content found for document with id: [%s]", b.getDocument().getId()));
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

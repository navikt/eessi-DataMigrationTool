package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases.document;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.ELanguage;
import eu.ec.dgempl.eessi.rina.model.exception.runtime.enums.EnumEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentBversion;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentThumbnail;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.DocumentBversionRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.DocumentThumbnailFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToDocumentThumbnailMapper extends AbstractMapToEntityMapper<MapHolder, DocumentThumbnail> {

    private final DocumentBversionRepo documentBversionRepo;

    public MapToDocumentThumbnailMapper(final DocumentBversionRepo documentBversionRepo) {
        this.documentBversionRepo = documentBversionRepo;
    }

    @Override
    public void mapAtoB(final MapHolder a, final DocumentThumbnail b, final MappingContext context) {
        mapDocumentBversion(a, b);
        mapLanguage(a, b);

        b.setContent(a.string(DocumentThumbnailFields.THUMBNAIL_CONTENT));
    }

    private void mapDocumentBversion(final MapHolder a, final DocumentThumbnail b) {
        String caseId = a.string(DocumentThumbnailFields.CASE_ID);
        String documentId = a.string(DocumentThumbnailFields.DOCUMENT_ID);
        String versionId = a.string(DocumentThumbnailFields.VERSION_ID);

        DocumentBversion documentBversion = documentBversionRepo.findByDocumentIdAndDocumentRinaCaseIdAndId(
                documentId,
                caseId,
                Integer.parseInt(versionId));

        if (documentBversion == null) {
            throw new EntityNotFoundEessiRuntimeException(
                    DocumentBversion.class,
                    UniqueIdentifier.caseId, caseId,
                    UniqueIdentifier.version, versionId);
        }

        b.setDocumentBversion(documentBversion);
    }

    private void mapLanguage(final MapHolder a, final DocumentThumbnail b) {
        String internalId = a.string(DocumentThumbnailFields.INTERNAL_ID);
        String[] idParts = internalId.split("_");

        if (idParts.length != 4) {
            throw new RuntimeException("Could not extract language for document thumbnail: " + internalId);
        }

        String lang = idParts[idParts.length - 1];
        ELanguage eLanguage = ELanguage.lookup(lang).orElseThrow(EnumEessiRuntimeException::new);
        b.setLang(eLanguage);
    }
}

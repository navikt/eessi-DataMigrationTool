package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.cases;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentBversion;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentContent;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Subdocument;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.SubdocumentBversion;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.SubdocumentContent;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.DocumentBversionRepo;
import eu.ec.dgempl.eessi.rina.repo.DocumentContentRepo;
import eu.ec.dgempl.eessi.rina.repo.SubdocumentBversionRepo;
import eu.ec.dgempl.eessi.rina.repo.SubdocumentContentRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.CaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.DocumentContentFields;

@Component
@ElasticTypeImporter(type = EElasticType.CASES_DOCUMENTCONTENT)
public class DocumentContentImporter extends AbstractDataImporter implements CaseImporter {

    private final DocumentContentRepo documentContentRepo;
    private final DocumentBversionRepo documentBversionRepo;
    private final SubdocumentContentRepo subdocumentContentRepo;
    private final SubdocumentBversionRepo subdocumentBversionRepo;

    public DocumentContentImporter(final DocumentContentRepo documentContentRepo,
            final DocumentBversionRepo documentBversionRepo,
            final SubdocumentContentRepo subdocumentContentRepo,
            final SubdocumentBversionRepo subdocumentBversionRepo) {
        this.documentContentRepo = documentContentRepo;
        this.documentBversionRepo = documentBversionRepo;
        this.subdocumentContentRepo = subdocumentContentRepo;
        this.subdocumentBversionRepo = subdocumentBversionRepo;
    }

    @Override
    public DocumentsReport importData(final String caseId) {
        return run(this::processDocumentData, caseId);
    }

    public void processDocumentData(final MapHolder doc) {
        String parentDocumentId = doc.string(DocumentContentFields.PARENT_DOCUMENT_ID);

        if (StringUtils.isNotBlank(parentDocumentId)) {
            SubdocumentContent subdocumentContent = beanMapper.map(doc, SubdocumentContent.class);
            subdocumentContentRepo.saveAndFlush(subdocumentContent);

            String versionId = doc.string(DocumentContentFields.VERSION_ID);
            Subdocument subdocument = subdocumentContent.getSubdocument();
            Document document = subdocument.getDocument();
            SubdocumentBversion subdocumentBversion =
                    subdocumentBversionRepo.findBySubdocumentIdAndSubdocumentDocumentIdAndSubdocumentRinaCaseIdAndId(
                            subdocument.getId(),
                            document.getId(),
                            document.getRinaCase().getId(),
                            Integer.parseInt(versionId)
                    );

            if (subdocumentBversion == null) {
                throw new EntityNotFoundEessiRuntimeException(SubdocumentBversion.class, UniqueIdentifier.id, versionId);
            }

            subdocumentBversion.setSubdocumentContent(subdocumentContent);
            subdocumentBversionRepo.saveAndFlush(subdocumentBversion);

        } else {
            DocumentContent documentContent = beanMapper.map(doc, DocumentContent.class);
            documentContentRepo.saveAndFlush(documentContent);

            String versionId = doc.string(DocumentContentFields.VERSION_ID);
            DocumentBversion documentBversion = documentBversionRepo.findByDocumentAndId(
                    documentContent.getDocument(),
                    Integer.parseInt(versionId));

            if (documentBversion == null) {
                throw new EntityNotFoundEessiRuntimeException(DocumentBversion.class, UniqueIdentifier.id, versionId);
            }

            documentBversion.setDocumentContent(documentContent);
            documentBversionRepo.saveAndFlush(documentBversion);
        }
    }
}

package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.cases;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentThumbnail;
import eu.ec.dgempl.eessi.rina.repo.DocumentThumbnailRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.CaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

@Component
@ElasticTypeImporter(type = EElasticType.CASES_THUMBNAILCONTENT)
public class ThumbnailContentImporter extends AbstractDataImporter implements CaseImporter {

    private final DocumentThumbnailRepo documentThumbnailRepo;

    public ThumbnailContentImporter(final DocumentThumbnailRepo documentThumbnailRepo) {
        this.documentThumbnailRepo = documentThumbnailRepo;
    }

    @Override
    public DocumentsReport importData(final String caseId) {
        return run(this::processThumbnailContentData, caseId);
    }

    private void processThumbnailContentData(final MapHolder doc) {
        DocumentThumbnail documentThumbnail = beanMapper.map(doc, DocumentThumbnail.class);
        documentThumbnailRepo.saveAndFlush(documentThumbnail);
    }
}

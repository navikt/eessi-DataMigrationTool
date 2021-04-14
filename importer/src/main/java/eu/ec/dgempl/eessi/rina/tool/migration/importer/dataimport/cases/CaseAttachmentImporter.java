package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.cases;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.CaseAttachment;
import eu.ec.dgempl.eessi.rina.repo.CaseAttachmentRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.CaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

@Component
@ElasticTypeImporter(type = EElasticType.CASES_ATTACHMENT)
public class CaseAttachmentImporter extends AbstractDataImporter implements CaseImporter {

    private final CaseAttachmentRepo caseAttachmentRepo;

    public CaseAttachmentImporter(final CaseAttachmentRepo caseAttachmentRepo) {
        this.caseAttachmentRepo = caseAttachmentRepo;
    }

    @Override
    public DocumentsReport importData(final String caseId) {
        return run(this::processAttachmentData, caseId);
    }

    private void processAttachmentData(final MapHolder doc) {
        CaseAttachment caseAttachment = beanMapper.map(doc, CaseAttachment.class);
        caseAttachmentRepo.saveAndFlush(caseAttachment);
    }
}

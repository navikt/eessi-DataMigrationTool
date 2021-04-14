package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.postcase.businessExceptions;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.PendingSignature;
import eu.ec.dgempl.eessi.rina.repo.PendingSignatureRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.PostCaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

@Component
@ElasticTypeImporter(type = EElasticType.BUSINESS_EXCEPTION_PENDING_SIGNATURE)
public class PendingSignatureImporter extends AbstractDataImporter implements PostCaseImporter {

    private final PendingSignatureRepo pendingSignatureRepo;

    public PendingSignatureImporter(final PendingSignatureRepo pendingSignatureRepo) {
        this.pendingSignatureRepo = pendingSignatureRepo;
    }

    @Override
    public DocumentsReport importData() {
        return run(this::processPendingSignatureData);
    }

    private void processPendingSignatureData(final MapHolder doc) {
        PendingSignature pendingSignature = beanMapper.map(doc, PendingSignature.class);
        pendingSignatureRepo.saveAndFlush(pendingSignature);
    }
}
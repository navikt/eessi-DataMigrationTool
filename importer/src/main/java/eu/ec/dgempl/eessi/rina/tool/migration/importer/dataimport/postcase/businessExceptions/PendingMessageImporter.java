package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.postcase.businessExceptions;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.PendingMessage;
import eu.ec.dgempl.eessi.rina.repo.PendingMessageRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.PostCaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

@Component
@ElasticTypeImporter(type = EElasticType.BUSINESS_EXCEPTION_PENDING_MESSAGE)
public class PendingMessageImporter extends AbstractDataImporter implements PostCaseImporter {

    private final PendingMessageRepo pendingMessageRepo;

    public PendingMessageImporter(final PendingMessageRepo pendingMessageRepo) {
        this.pendingMessageRepo = pendingMessageRepo;
    }

    @Override
    public DocumentsReport importData() {
        return run(this::processPendingMessageData);
    }

    private void processPendingMessageData(final MapHolder doc) {
        PendingMessage pendingMessage = beanMapper.map(doc, PendingMessage.class);
        pendingMessageRepo.saveAndFlush(pendingMessage);
    }
}
package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.postcase.businessExceptions;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.BusinessException;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.PendingMessage;
import eu.ec.dgempl.eessi.rina.repo.BusinessExceptionRepo;
import eu.ec.dgempl.eessi.rina.repo.PendingMessageRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.PostCaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

@Component
@ElasticTypeImporter(type = EElasticType.BUSINESS_EXCEPTION_EXCEPTION)
public class BusinessExceptionImporter extends AbstractDataImporter implements PostCaseImporter {

    private final PendingMessageRepo pendingMessageRepo;
    private final BusinessExceptionRepo businessExceptionRepo;

    public BusinessExceptionImporter(final PendingMessageRepo pendingMessageRepo, final BusinessExceptionRepo businessExceptionRepo) {
        this.pendingMessageRepo = pendingMessageRepo;
        this.businessExceptionRepo = businessExceptionRepo;
    }

    @Override
    public DocumentsReport importData() {
        return run(this::processBusinessExceptionData);
    }

    private void processBusinessExceptionData(final MapHolder doc) {
        BusinessException businessException = beanMapper.map(doc, BusinessException.class);
        PendingMessage pendingMessage = businessException.getPendingMessage();

        pendingMessageRepo.saveAndFlush(pendingMessage);
        businessExceptionRepo.saveAndFlush(businessException);

    }
}

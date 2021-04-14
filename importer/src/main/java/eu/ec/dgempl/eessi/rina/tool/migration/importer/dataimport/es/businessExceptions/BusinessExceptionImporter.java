package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es.businessExceptions;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.BusinessException;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.PendingMessage;
import eu.ec.dgempl.eessi.rina.repo.BusinessExceptionRepo;
import eu.ec.dgempl.eessi.rina.repo.PendingMessageRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.BusinessExceptionFields;
import org.springframework.stereotype.Component;

@Component
@ElasticTypeImporter(type = EElasticType.BUSINESS_EXCEPTION_EXCEPTION)
public class BusinessExceptionImporter extends AbstractDataImporter {

    private final PendingMessageRepo pendingMessageRepo;
    private final BusinessExceptionRepo businessExceptionRepo;

    public BusinessExceptionImporter(final PendingMessageRepo pendingMessageRepo, final BusinessExceptionRepo businessExceptionRepo) {
        this.pendingMessageRepo = pendingMessageRepo;
        this.businessExceptionRepo = businessExceptionRepo;
    }

    @Override
    public void importData() {
        run(this::processBusinessExceptionData);
    }

    private void processBusinessExceptionData(final MapHolder doc) {
        BusinessException businessException = beanMapper.map(doc, BusinessException.class);
        PendingMessage pendingMessage = businessException.getPendingMessage();

        pendingMessageRepo.saveAndFlush(pendingMessage);
        businessExceptionRepo.saveAndFlush(businessException);

    }
}

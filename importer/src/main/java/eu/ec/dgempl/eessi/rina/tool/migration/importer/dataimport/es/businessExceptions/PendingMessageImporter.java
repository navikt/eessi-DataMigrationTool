package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es.businessExceptions;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.PendingMessage;
import eu.ec.dgempl.eessi.rina.repo.PendingMessageRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import org.springframework.stereotype.Component;


@Component
@ElasticTypeImporter(type = EElasticType.BUSINESS_EXCEPTION_PENDING_MESSAGE)
public class PendingMessageImporter extends AbstractDataImporter {

    private final PendingMessageRepo pendingMessageRepo;

    public PendingMessageImporter(final PendingMessageRepo pendingMessageRepo) {
        this.pendingMessageRepo = pendingMessageRepo;
    }

    @Override
    public void importData() {
        run(this::processPendingMessageData);
    }

    private void processPendingMessageData(final MapHolder doc) {
        PendingMessage pendingMessage = beanMapper.map(doc, PendingMessage.class);
        pendingMessageRepo.saveAndFlush(pendingMessage);
    }
}
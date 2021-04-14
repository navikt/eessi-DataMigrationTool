package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es.businessExceptions;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.PendingSignature;
import eu.ec.dgempl.eessi.rina.repo.PendingSignatureRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import org.springframework.stereotype.Component;



@Component
@ElasticTypeImporter(type = EElasticType.BUSINESS_EXCEPTION_PENDING_SIGNATURE)
public class PendingSignatureImporter extends AbstractDataImporter {

    private final PendingSignatureRepo pendingSignatureRepo;

    public PendingSignatureImporter(final PendingSignatureRepo pendingSignatureRepo) {
        this.pendingSignatureRepo = pendingSignatureRepo;
    }

    @Override
    public void importData() {
        run(this::processPendingSignatureData);
    }

    private void processPendingSignatureData(final MapHolder doc) {
        PendingSignature pendingSignature = beanMapper.map(doc, PendingSignature.class);
        pendingSignatureRepo.saveAndFlush(pendingSignature);
    }
}
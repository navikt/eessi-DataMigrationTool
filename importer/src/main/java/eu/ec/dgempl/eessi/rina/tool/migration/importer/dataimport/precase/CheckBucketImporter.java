package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.precase;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.CheckBucket;
import eu.ec.dgempl.eessi.rina.repo.CheckBucketRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.PreCaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

@Component
@ElasticTypeImporter(type = EElasticType.CHECKS_CHECKBUCKET)
public class CheckBucketImporter extends AbstractDataImporter implements PreCaseImporter {

    private final CheckBucketRepo checkBucketRepo;

    public CheckBucketImporter(final CheckBucketRepo checkBucketRepo) {
        this.checkBucketRepo = checkBucketRepo;
    }

    @Override
    public DocumentsReport importData() {
        return run(this::processCheckDefinitionData);
    }

    private void processCheckDefinitionData(final MapHolder doc) {
        CheckBucket checkBucket = beanMapper.map(doc, CheckBucket.class);
        checkBucketRepo.saveAndFlush(checkBucket);
    }
}

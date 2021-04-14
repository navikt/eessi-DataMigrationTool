package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.precase;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.CheckInstance;
import eu.ec.dgempl.eessi.rina.repo.CheckInstanceRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.PreCaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

@Component
@ElasticTypeImporter(type = EElasticType.CHECKS_CHECKINSTANCE)
public class CheckInstanceImporter extends AbstractDataImporter implements PreCaseImporter {

    private final CheckInstanceRepo checkInstanceRepo;

    public CheckInstanceImporter(final CheckInstanceRepo checkInstanceRepo) {
        this.checkInstanceRepo = checkInstanceRepo;
    }

    @Override
    public DocumentsReport importData() {
        return run(this::processCheckDefinitionData);
    }

    private void processCheckDefinitionData(final MapHolder doc) {
        CheckInstance checkInstance = beanMapper.map(doc, CheckInstance.class);
        checkInstanceRepo.saveAndFlush(checkInstance);
    }
}

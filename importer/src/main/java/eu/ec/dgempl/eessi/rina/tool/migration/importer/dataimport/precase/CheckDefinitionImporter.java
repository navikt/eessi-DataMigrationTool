package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.precase;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.CheckDefinition;
import eu.ec.dgempl.eessi.rina.repo.CheckDefinitionRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.PreCaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

@Component
@ElasticTypeImporter(type = EElasticType.CHECKS_CHECKDEFINITION)
public class CheckDefinitionImporter extends AbstractDataImporter implements PreCaseImporter {

    private final CheckDefinitionRepo checkDefinitionRepo;

    public CheckDefinitionImporter(final CheckDefinitionRepo checkDefinitionRepo) {
        this.checkDefinitionRepo = checkDefinitionRepo;
    }

    @Override
    public DocumentsReport importData() {
        return run(this::processCheckDefinitionData);
    }

    private void processCheckDefinitionData(final MapHolder doc) {
        CheckDefinition checkDefinition = beanMapper.map(doc, CheckDefinition.class);
        checkDefinitionRepo.saveAndFlush(checkDefinition);
    }
}

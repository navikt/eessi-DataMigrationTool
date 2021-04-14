package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.CheckDefinition;
import eu.ec.dgempl.eessi.rina.repo.CheckDefinitionRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

@Component
@ElasticTypeImporter(type = EElasticType.CHECKS_CHECKDEFINITION)
public class CheckDefinitionImporter extends AbstractDataImporter {

    private final CheckDefinitionRepo checkDefinitionRepo;

    public CheckDefinitionImporter(final CheckDefinitionRepo checkDefinitionRepo) {
        this.checkDefinitionRepo = checkDefinitionRepo;
    }

    @Override
    public void importData() {
        run(this::processCheckDefinitionData);
    }

    private void processCheckDefinitionData(final MapHolder doc) {
        CheckDefinition checkDefinition = beanMapper.map(doc, CheckDefinition.class);
        checkDefinitionRepo.saveAndFlush(checkDefinition);
    }
}

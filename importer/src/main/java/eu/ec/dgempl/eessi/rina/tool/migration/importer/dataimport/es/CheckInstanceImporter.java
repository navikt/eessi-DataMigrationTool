package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.CheckInstance;
import eu.ec.dgempl.eessi.rina.repo.CheckInstanceRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

@Component
@ElasticTypeImporter(type = EElasticType.CHECKS_CHECKINSTANCE)
public class CheckInstanceImporter extends AbstractDataImporter {

    private final CheckInstanceRepo checkInstanceRepo;

    public CheckInstanceImporter(final CheckInstanceRepo checkInstanceRepo) {
        this.checkInstanceRepo = checkInstanceRepo;
    }

    @Override
    public void importData() {
        run(this::processCheckDefinitionData);
    }

    private void processCheckDefinitionData(final MapHolder doc) {
        CheckInstance checkInstance = beanMapper.map(doc, CheckInstance.class);
        checkInstanceRepo.saveAndFlush(checkInstance);
    }
}

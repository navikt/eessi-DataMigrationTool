package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es.cases;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.Action;
import eu.ec.dgempl.eessi.rina.repo.ActionRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.CaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

@Component
@ElasticTypeImporter(type = EElasticType.CASES_TASKMETADATA)
public class TaskMetadataImporter extends AbstractDataImporter implements CaseImporter {

    private final ActionRepo actionRepo;

    public TaskMetadataImporter(final ActionRepo actionRepo) {
        this.actionRepo = actionRepo;
    }

    @Override
    public void importData(final String caseId) {
        run(this::processTaskMetadata, caseId);
    }

    private void processTaskMetadata(final MapHolder doc) {
        Action action = beanMapper.map(doc, Action.class);
        actionRepo.saveAndFlush(action);
    }
}

package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.postcase;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.Activity;
import eu.ec.dgempl.eessi.rina.repo.ActivityRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.PostCaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

@Component
@ElasticTypeImporter(type = EElasticType.ACTIVITY)
public class ActivityImporter extends AbstractDataImporter implements PostCaseImporter {

    private final ActivityRepo activityRepo;

    public ActivityImporter(final ActivityRepo activityRepo) {
        this.activityRepo = activityRepo;
    }

    @Override
    public DocumentsReport importData() {
        return run(this::processActivityData);
    }

    private void processActivityData(final MapHolder doc) {
        Activity activity = beanMapper.map(doc, Activity.class);
        activityRepo.saveAndFlush(activity);
    }
}

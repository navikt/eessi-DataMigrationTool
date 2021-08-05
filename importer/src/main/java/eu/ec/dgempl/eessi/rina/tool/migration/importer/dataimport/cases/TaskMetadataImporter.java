package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.cases;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.Action;
import eu.ec.dgempl.eessi.rina.repo.ActionRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.CaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.ActionFields;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@ElasticTypeImporter(type = EElasticType.CASES_TASKMETADATA)
public class TaskMetadataImporter extends AbstractDataImporter implements CaseImporter {

    private final ActionRepo actionRepo;

    private final Logger logger = LoggerFactory.getLogger(TaskMetadataImporter.class);

    private final List<String> UNSUPPORTED_DOCUMENT_TYPES = List.of("DummyChooseParts");

    public TaskMetadataImporter(final ActionRepo actionRepo) {
        this.actionRepo = actionRepo;
    }

    @Override
    public DocumentsReport importData(final String caseId) {
        return run(this::processTaskMetadata, caseId);
    }

    private void processTaskMetadata(final MapHolder doc) {

        if (isLinkedToDummyDocument(doc)) {
            doc.visitAll();
            logger.warn("Found action linked to dummy document in MultiStarter context for case: {}, docId: {}",
                    doc.string(ActionFields.CASE_ID), doc.string(ActionFields.ID));
            return;
        }

        Action action = beanMapper.map(doc, Action.class);
        actionRepo.saveAndFlush(action);
    }

    private boolean isLinkedToDummyDocument(final MapHolder doc) {
        String actionGroupType = doc.string(ActionFields.ACTION_GROUP_TYPE, true);
        String poolGroupType = doc.string(ActionFields.POOL_GROUP_TYPE, true);

        if ((actionGroupType != null && UNSUPPORTED_DOCUMENT_TYPES.contains(actionGroupType))
                || (poolGroupType != null && UNSUPPORTED_DOCUMENT_TYPES.contains(poolGroupType))) {
            return true;
        } else {
            return false;
        }
    }
}

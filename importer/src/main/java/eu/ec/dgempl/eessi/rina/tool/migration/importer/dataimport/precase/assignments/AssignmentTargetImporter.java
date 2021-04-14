package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.precase.assignments;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.AssignmentPolicyTarget;
import eu.ec.dgempl.eessi.rina.repo.AssignmentPolicyTargetRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.PreCaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

@Component
@ElasticTypeImporter(type = EElasticType.CONFIGURATIONS_ASSIGNMENTTARGET)
public class AssignmentTargetImporter extends AbstractDataImporter implements PreCaseImporter {

    private final AssignmentPolicyTargetRepo assignmentPolicyTargetRepo;

    public AssignmentTargetImporter(final AssignmentPolicyTargetRepo assignmentPolicyTargetRepo) {
        this.assignmentPolicyTargetRepo = assignmentPolicyTargetRepo;
    }

    @Override
    public DocumentsReport importData() {
        return run(this::processAssignmentTargetData);
    }

    private void processAssignmentTargetData(final MapHolder doc) {
        AssignmentPolicyTarget assignmentPolicyTarget = beanMapper.map(doc, AssignmentPolicyTarget.class);
        assignmentPolicyTargetRepo.saveAndFlush(assignmentPolicyTarget);
    }
}

package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es.assignments;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.AssignmentPolicyTarget;
import eu.ec.dgempl.eessi.rina.repo.AssignmentPolicyTargetRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

@Component
@ElasticTypeImporter(type = EElasticType.CONFIGURATIONS_ASSIGNMENTTARGET)
public class AssignmentTargetImporter extends AbstractDataImporter {

    private final AssignmentPolicyTargetRepo assignmentPolicyTargetRepo;

    public AssignmentTargetImporter(final AssignmentPolicyTargetRepo assignmentPolicyTargetRepo) {
        this.assignmentPolicyTargetRepo = assignmentPolicyTargetRepo;
    }

    @Override
    public void importData() {
        run(this::processAssignmentTargetData);
    }

    private void processAssignmentTargetData(final MapHolder doc) {
        AssignmentPolicyTarget assignmentPolicyTarget = beanMapper.map(doc, AssignmentPolicyTarget.class);
        assignmentPolicyTargetRepo.saveAndFlush(assignmentPolicyTarget);
    }
}

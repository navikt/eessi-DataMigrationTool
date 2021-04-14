package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.precase.assignments;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.AssignmentPolicy;
import eu.ec.dgempl.eessi.rina.repo.AssignmentPolicyRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.PreCaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

@Component
@ElasticTypeImporter(type = EElasticType.CONFIGURATIONS_ASSIGNMENTPOLICY)
public class AssignmentPolicyImporter extends AbstractDataImporter implements PreCaseImporter {

    private final AssignmentPolicyRepo assignmentPolicyRepo;

    public AssignmentPolicyImporter(final AssignmentPolicyRepo assignmentPolicyRepo) {
        this.assignmentPolicyRepo = assignmentPolicyRepo;
    }

    @Override
    public DocumentsReport importData() {
        return run(this::processAssignmentPolicyData);
    }

    private void processAssignmentPolicyData(final MapHolder doc) {
        AssignmentPolicy assignmentPolicy = beanMapper.map(doc, AssignmentPolicy.class);
        assignmentPolicyRepo.saveAndFlush(assignmentPolicy);
    }
}

package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.cases;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils.*;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.repo.AssignmentRepo;
import eu.ec.dgempl.eessi.rina.repo.CaseParticipantRepo;
import eu.ec.dgempl.eessi.rina.repo.CasePrefillRepo;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.CaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

@Component
@ElasticTypeImporter(type = EElasticType.CASES_CASESTRUCTUREDMETADATA)
public class CaseStructuredMetadataImporter extends AbstractDataImporter implements CaseImporter {

    private final AssignmentRepo assignmentRepo;
    private final CasePrefillRepo casePrefillRepo;
    private final CaseParticipantRepo caseParticipantRepo;
    private final RinaCaseRepo rinaCaseRepo;

    public CaseStructuredMetadataImporter(
            final AssignmentRepo assignmentRepo,
            final CasePrefillRepo casePrefillRepo,
            final CaseParticipantRepo caseParticipantRepo,
            final RinaCaseRepo rinaCaseRepo) {
        this.assignmentRepo = assignmentRepo;
        this.casePrefillRepo = casePrefillRepo;
        this.caseParticipantRepo = caseParticipantRepo;
        this.rinaCaseRepo = rinaCaseRepo;
    }

    @Override
    public DocumentsReport importData(final String caseId) {
        return run(this::processCaseStructuredMetadata, caseId);
    }

    private void processCaseStructuredMetadata(final MapHolder doc) {
        RinaCase rinaCase = beanMapper.map(doc, RinaCase.class,
                mctxb()
                        .addProp("type", this.inferElasticType())
                        .build());

        rinaCaseRepo.saveAndFlush(rinaCase);

        saveInBulk(rinaCase::getCasePrefills, () -> casePrefillRepo);
        saveInBulk(rinaCase::getParticipants, () -> caseParticipantRepo);
        saveInBulk(rinaCase::getAssignments, () -> assignmentRepo);
    }
}

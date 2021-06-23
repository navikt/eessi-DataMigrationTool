package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.cases;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.MappingContextBuilder.*;
import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.TempAction;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.TempAttachment;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.TempDocument;
import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.PersistableWithSid;
import eu.ec.dgempl.eessi.rina.repo.AssignmentRepo;
import eu.ec.dgempl.eessi.rina.repo.CaseParticipantRepo;
import eu.ec.dgempl.eessi.rina.repo.CasePrefillRepo;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.repo.TempActionRepo;
import eu.ec.dgempl.eessi.rina.repo.TempAttachmentRepo;
import eu.ec.dgempl.eessi.rina.repo.TempDocumentRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.common.model.fields.CaseFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.CaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;

@Component
@ElasticTypeImporter(type = EElasticType.CASES_CASESTRUCTUREDMETADATA)
public class CaseStructuredMetadataImporter extends AbstractDataImporter implements CaseImporter {

    private final AssignmentRepo assignmentRepo;
    private final CasePrefillRepo casePrefillRepo;
    private final CaseParticipantRepo caseParticipantRepo;
    private final RinaCaseRepo rinaCaseRepo;
    private final TempActionRepo tempActionRepo;
    private final TempAttachmentRepo tempAttachmentRepo;
    private final TempDocumentRepo tempDocumentRepo;

    public CaseStructuredMetadataImporter(
            final AssignmentRepo assignmentRepo,
            final CasePrefillRepo casePrefillRepo,
            final CaseParticipantRepo caseParticipantRepo,
            final RinaCaseRepo rinaCaseRepo,
            final TempActionRepo tempActionRepo,
            final TempAttachmentRepo tempAttachmentRepo,
            final TempDocumentRepo tempDocumentRepo) {
        this.assignmentRepo = assignmentRepo;
        this.casePrefillRepo = casePrefillRepo;
        this.caseParticipantRepo = caseParticipantRepo;
        this.rinaCaseRepo = rinaCaseRepo;
        this.tempActionRepo = tempActionRepo;
        this.tempAttachmentRepo = tempAttachmentRepo;
        this.tempDocumentRepo = tempDocumentRepo;
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

        saveInBulk(() -> this.getTempDocuments(doc, CaseFields.ACTIONS, TempAction.class), () -> tempActionRepo);
        saveInBulk(() -> this.getTempDocuments(doc, CaseFields.ATTACHMENTS, TempAttachment.class), () -> tempAttachmentRepo);
        saveInBulk(() -> this.getTempDocuments(doc, CaseFields.DOCUMENTS, TempDocument.class), () -> tempDocumentRepo);
    }

    private <T extends PersistableWithSid> List<T> getTempDocuments(MapHolder doc, String key, Class<T> clazz) {
        if (doc.containsKey(key)) {
            List<MapHolder> tempDocs = doc.listToMapHolder(key);

            if (CollectionUtils.isNotEmpty(tempDocs)) {
                return tempDocs.stream()
                        .map(action -> beanMapper.map(action, clazz))
                        .collect(Collectors.toList());
            }

            return Collections.emptyList();

        }

        return Collections.emptyList();
    }
}

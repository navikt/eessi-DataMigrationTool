package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.cases;

import eu.ec.dgempl.eessi.rina.model.enumtypes.ECasePrefillGroup;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EDocumentStatus;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.CasePrefill;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.repo.CasePrefillRepo;
import eu.ec.dgempl.eessi.rina.repo.DocumentRepoExtended;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.CaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.CasePrefillFields.LAST_PROCESSED_X001_DOCUMENT_ID;

@Component
@ElasticTypeImporter(type = EElasticType.NONE, order = 8889)
public class FinalizeCaseImporter extends AbstractDataImporter implements CaseImporter {

    private final CasePrefillRepo casePrefillRepo;
    private final RinaCaseRepo rinaCaseRepo;
    private final DocumentRepoExtended documentRepo;

    public FinalizeCaseImporter(
            final CasePrefillRepo casePrefillRepo,
            final RinaCaseRepo rinaCaseRepo,
            final DocumentRepoExtended documentRepo) {
        this.casePrefillRepo = casePrefillRepo;
        this.rinaCaseRepo = rinaCaseRepo;
        this.documentRepo = documentRepo;
    }

    @Override
    public DocumentsReport importData(final String caseId) {

        RinaCase rinaCase = RepositoryUtils.findById(caseId, rinaCaseRepo::findById, RinaCase.class);

        List<Document> documents = documentRepo.findByRinaCaseId(caseId);

        Optional<Document> document = getValidDocument(documents);

        document.ifPresent(doc -> casePrefillRepo.saveAndFlush(
                new CasePrefill(rinaCase, ECasePrefillGroup.PREFILL, LAST_PROCESSED_X001_DOCUMENT_ID, doc.getId())));

        return null;
    }

    @NotNull
    private Optional<Document> getValidDocument(final List<Document> documents) {
        return documents.stream()
                .filter(d -> ("X001".equals(d.getDocumentTypeVersion().getDocumentType().getType())
                        && (EDocumentStatus.SENT.equals(d.getStatus()) || EDocumentStatus.RECEIVED.equals(d.getStatus()))))
                .max(Comparator.comparing(x -> x.getAudit().getCreatedAt()));
    }
}
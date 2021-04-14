package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.cases;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.CaseComment;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentComment;
import eu.ec.dgempl.eessi.rina.repo.CaseCommentRepo;
import eu.ec.dgempl.eessi.rina.repo.DocumentCommentRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.CaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.CommentFields;

@Component
@ElasticTypeImporter(type = EElasticType.CASES_COMMENT)
public class CommentImporter extends AbstractDataImporter implements CaseImporter {

    private final CaseCommentRepo caseCommentRepo;
    private final DocumentCommentRepo documentCommentRepo;

    public CommentImporter(final CaseCommentRepo caseCommentRepo,
            final DocumentCommentRepo documentCommentRepo) {
        this.caseCommentRepo = caseCommentRepo;
        this.documentCommentRepo = documentCommentRepo;
    }

    @Override
    public DocumentsReport importData(final String caseId) {
        return run(this::processCommentData, caseId);
    }

    private void processCommentData(final MapHolder doc) {
        String documentId = doc.string(CommentFields.DOCUMENT_ID);

        if (!"0".equals(documentId)) {
            DocumentComment documentComment = beanMapper.map(doc, DocumentComment.class);
            documentCommentRepo.saveAndFlush(documentComment);
        } else {
            CaseComment caseComment = beanMapper.map(doc, CaseComment.class);
            caseCommentRepo.saveAndFlush(caseComment);
        }
    }
}

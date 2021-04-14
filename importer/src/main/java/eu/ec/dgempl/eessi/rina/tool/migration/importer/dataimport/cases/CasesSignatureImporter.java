package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.cases;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.Signature;
import eu.ec.dgempl.eessi.rina.repo.SignatureRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.CaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

@Component
@ElasticTypeImporter(type = EElasticType.CASES_SIGNATURE)
public class CasesSignatureImporter extends AbstractDataImporter implements CaseImporter {

    private final SignatureRepo signatureRepo;

    public CasesSignatureImporter(final SignatureRepo signatureRepo) {
        this.signatureRepo = signatureRepo;
    }

    @Override
    public DocumentsReport importData(final String caseId) {
        return run(this::processCaseSignatureData, caseId);
    }

    private void processCaseSignatureData(final MapHolder doc) {
        Signature signature = beanMapper.map(doc, Signature.class);
        signatureRepo.saveAndFlush(signature);
    }
}

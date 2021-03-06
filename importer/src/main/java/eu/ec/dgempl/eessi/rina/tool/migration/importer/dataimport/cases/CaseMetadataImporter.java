package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.cases;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.MappingContextBuilder.*;
import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils.*;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.CasePrefillRepo;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.common.model.fields.CaseFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.CaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@ElasticTypeImporter(type = EElasticType.CASES_CASEMETADATA)
public class CaseMetadataImporter extends AbstractDataImporter implements CaseImporter {

    private final CasePrefillRepo casePrefillRepo;
    private final RinaCaseRepo rinaCaseRepo;

    public CaseMetadataImporter(
            final CasePrefillRepo casePrefillRepo,
            final RinaCaseRepo rinaCaseRepo) {
        this.casePrefillRepo = casePrefillRepo;
        this.rinaCaseRepo = rinaCaseRepo;
    }

    @Override
    public DocumentsReport importData(final String caseId) {
        return run(this::importCaseMetadata, caseId);
    }

    private void importCaseMetadata(final MapHolder doc) {
        String caseId = doc.string(CaseFields.ID);
        RinaCase rinaCase = rinaCaseRepo.findById(caseId);

        if (rinaCase == null) {
            throw new EntityNotFoundEessiRuntimeException(RinaCase.class, UniqueIdentifier.id, caseId);
        }

        beanMapper.map(
                doc,
                rinaCase,
                mctxb()
                        .addProp("type", this.inferElasticType())
                        .build());

        rinaCaseRepo.saveAndFlush(rinaCase);

        saveInBulk(rinaCase::getCasePrefills, () -> casePrefillRepo);
    }
}

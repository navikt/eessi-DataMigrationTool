package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es.cases;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.buc.core.model.ECaseRole;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EApplicationRole;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.ProcessDefVersion;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.buc.CaseProcessor;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.CaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils;

@Component
@ElasticTypeImporter(type = EElasticType.NONE, order = 8888)
public class ReceiveActionImporter extends AbstractDataImporter implements CaseImporter {

    @Autowired
    private CaseProcessor caseProcessor;

    private final RinaCaseRepo rinaCaseRepo;

    public ReceiveActionImporter(final RinaCaseRepo rinaCaseRepo) {
        this.rinaCaseRepo = rinaCaseRepo;
    }

    @Override
    public void importData(final String caseId) {
        RinaCase rinaCase = RepositoryUtils.findById(caseId, rinaCaseRepo::findById, RinaCase.class);

        ProcessDefVersion processDefVersion = rinaCase.getProcessDefVersion();
        EApplicationRole eApplicationRole = rinaCase.getApplicationRole();
        ECaseRole eCaseRole = EApplicationRole.PO == eApplicationRole ? ECaseRole.PO : ECaseRole.CP;

        try {
            caseProcessor.processCase(caseId, processDefVersion.getProcessDef().getId(), eCaseRole, processDefVersion.getBusinessVersion());
        } catch (Exception e) {
            throw new RuntimeException("Could not create receive Actions for case: " + caseId, e);
        }

    }
}

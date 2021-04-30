package eu.ec.dgempl.eessi.rina.tool.migration.importer.service.cases;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import eu.ec.dgempl.eessi.rina.tool.migration.common.model.fields.CaseFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.cases._abstract.AbstractCasesRunnerService;

@Service
public class EsCasesRunnerService extends AbstractCasesRunnerService {

    @Override
    public List<String> getCases() {
        try {
            List<String> caseIds = esClientService.getCaseIds();
            caseIds.add(0, CaseFields.DEFAULT_CASE_ID);
            return caseIds;
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    @Override
    protected boolean shouldCleanupPostCaseResource() {
        return false;
    }
}

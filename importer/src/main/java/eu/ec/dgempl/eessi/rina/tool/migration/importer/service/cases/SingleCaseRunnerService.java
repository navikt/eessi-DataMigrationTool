package eu.ec.dgempl.eessi.rina.tool.migration.importer.service.cases;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.cases._abstract.AbstractCasesRunnerService;

@Service
public class SingleCaseRunnerService extends AbstractCasesRunnerService {

    private String caseId;

    @Override
    protected List<String> getCases() {
        return Collections.singletonList(caseId);
    }

    public void setCaseId(final String caseId) {
        this.caseId = caseId;
    }
}

package eu.ec.dgempl.eessi.rina.tool.migration.buc;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.buc.core.model.ECaseRole;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;

/**
 * Service for handling any additional operations on the imported cases, after the data have been imported.
 */
@Component
public class CaseProcessor {

    private static final Logger logger = LoggerFactory.getLogger(CaseProcessor.class);

    /**
     * List of BUC handlers
     */
    @Autowired
    private List<BucHandler> handlers;

    @Value("${buc.definitions.path}")
    private String bucDefinitionsPath;

    /**
     * Executes any implemented and registered {@link BucHandler} implementations for the given case.
     *
     * @param caseId
     * @param bucType
     * @param caseRole
     * @param modelVersion
     * @throws Exception
     */
    public void processCase(String caseId, String bucType, ECaseRole caseRole, String modelVersion) throws Exception {

        PreconditionsHelper.notEmpty(caseId, "caseId");
        PreconditionsHelper.notEmpty(bucType, "bucType");
        PreconditionsHelper.notNull(caseRole, "caseRole");
        PreconditionsHelper.notEmpty(modelVersion, "modelVersion");

        logger.debug("Processing additional operations on the case [caseId={}]", caseId);

        // call all defined handlers
        if (handlers != null) {
            handlers.forEach(h -> {
                try {
                    h.processCase(caseId, bucType, caseRole, modelVersion);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

    }

}

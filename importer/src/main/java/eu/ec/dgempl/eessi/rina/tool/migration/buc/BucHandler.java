package eu.ec.dgempl.eessi.rina.tool.migration.buc;

import eu.ec.dgempl.eessi.rina.buc.core.model.ECaseRole;

/**
 * Handler interface for any operations, which need to be done on a case after the data has been migrated
 */
public interface BucHandler {

    void processCase(String caseId, String bucType, ECaseRole caseRole, String modelVersion) throws Exception;

}

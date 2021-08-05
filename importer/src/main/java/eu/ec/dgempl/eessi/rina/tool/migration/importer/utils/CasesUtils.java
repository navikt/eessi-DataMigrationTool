package eu.ec.dgempl.eessi.rina.tool.migration.importer.utils;

import eu.ec.dgempl.eessi.rina.tool.migration.common.model.fields.CaseFields;

public final class CasesUtils {

    public static String getCaseId(String caseId) {
        return isDefaultCase(caseId) ? null : caseId;
    }

    public static boolean isDefaultCase(String caseId) {
        return CaseFields.DEFAULT_CASE_ID.equalsIgnoreCase(caseId);
    }
}

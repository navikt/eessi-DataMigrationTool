package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport;

import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;

public interface CaseImporter extends DataImporter {

    default DocumentsReport importData(String caseId) {
        return null;
    }

    default boolean processesEmptyCase() {
        return false;
    }

    long countDocsByCaseId(String caseId);
}

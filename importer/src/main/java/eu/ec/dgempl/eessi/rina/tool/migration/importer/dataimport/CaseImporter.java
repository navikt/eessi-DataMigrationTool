package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport;

import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;

public interface CaseImporter extends DataImporter {

    default DocumentsReport importData(String caseId) {
        return null;
    }

    default void processDocumentData(MapHolder doc) {
    }

    long countDocsByCaseId(String caseId);
}

package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport;

import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;

/**
 * Common contract for a post-case importer from a datasource
 *
 * @author Silviu Stefan
 */
public interface PostCaseImporter extends DataImporter {

    DocumentsReport importData();

}

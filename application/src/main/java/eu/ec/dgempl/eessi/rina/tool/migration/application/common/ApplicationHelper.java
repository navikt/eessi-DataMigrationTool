package eu.ec.dgempl.eessi.rina.tool.migration.application.common;

import java.io.IOException;

import eu.ec.dgempl.eessi.rina.tool.migration.common.service.EsClientService;

public class ApplicationHelper {

    /**
     * Exits the program with the success code
     */
    public static void exitWithSuccess(EsClientService elasticsearchService) {
        closeElasticsearchConnection(elasticsearchService);
        System.exit(0);
    }

    /**
     * Exits the program with the error exit code
     */
    public static void exitWithError(EsClientService elasticsearchService) {
        closeElasticsearchConnection(elasticsearchService);
        System.exit(1);
    }

    private static void closeElasticsearchConnection(final EsClientService elasticsearchService) {
        if (elasticsearchService != null) {
            try {
                // close elasticsearch connection
                elasticsearchService.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void showValidationErrorsFound(String reportingFolder) {
        String help = System.lineSeparator() + "Errors found during validation." + System.lineSeparator()
                + "Please review reports on folder: " + reportingFolder + System.lineSeparator();
        System.out.println(help);
    }


    public static void showNoValidationErrorsFound() {
        String help = System.lineSeparator() + "Errors NOT found during validation." + System.lineSeparator();
        System.out.println(help);
    }

    public static void showProceedingToImport() {
        String help = System.lineSeparator() + "Proceeding to import." + System.lineSeparator();
        System.out.println(help);
    }
}

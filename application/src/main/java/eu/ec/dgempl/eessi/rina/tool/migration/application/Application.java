package eu.ec.dgempl.eessi.rina.tool.migration.application;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.ImporterUtils.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.PlatformTransactionManager;

import eu.ec.dgempl.eessi.rina.tool.migration.common.service.EsClientService;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.report.CaseValidationReport;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.report.DocumentValidationReport;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.report.ValidationResult;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.service.ValidationService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.CaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.DataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.SequenceUpdateService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.CasesUtils;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.ProgrammaticTransactionUtil;

@SpringBootApplication
@ComponentScan(basePackages = { "eu.ec.dgempl.eessi.rina.tool.migration.*" })
public class Application implements CommandLineRunner, ApplicationContextAware {

    private final EsClientService elasticsearchService;
    private final ValidationService validationService;
    private final PlatformTransactionManager transactionManager;
    private ApplicationContext applicationContext;

    @Value("${reporting.folder}")
    private String reportingFolder;

    private final static String ARG_VALIDATE_ALL = "-validate-all";
    private final static String ARG_VALIDATE_CASE = "-validate-case";
    private final static String ARG_VALIDATE_AND_IMPORT = "-validate-import";
    private final static String ARG_IMPORT_ALL = "-import-all";
    private final static String ARG_IMPORT_CASE = "-import-case";
    private final static String ARG_HELP_LONG = "-help";
    private final static String ARG_HELP_SHORT = "-h";

    private final Logger logger = LoggerFactory.getLogger(Application.class);

    @Autowired
    public Application(EsClientService elasticsearchService, ValidationService validationService,
            PlatformTransactionManager transactionManager) {
        this.elasticsearchService = elasticsearchService;
        this.validationService = validationService;
        this.transactionManager = transactionManager;
    }

    public static void main(String[] args) {

        // Accepted args:
        //
        // -validate-all (default)
        // -validate-case case
        // -validate-import [importer_name]
        // -import-all [importer_name]
        // -import-case case
        // -help, -h

        boolean proceed = true;

        switch (args.length) {
            case 0: {
                showNoArgsInfo();
                break;
            }
            case 1: {
                List<String> arguments = Arrays.stream(args).collect(Collectors.toList());
                if ((arguments.contains(ARG_HELP_LONG) || arguments.contains(ARG_HELP_SHORT)) || (!arguments.contains(ARG_VALIDATE_ALL)
                        && !arguments.contains(ARG_VALIDATE_AND_IMPORT) && !arguments.contains(ARG_IMPORT_ALL))) {
                    showHelp();
                    proceed = false;
                }
                break;
            }
            case 2: {
                List<String> arguments = Arrays.stream(args).collect(Collectors.toList());
                if (!arguments.get(0).equals(ARG_VALIDATE_CASE) && !arguments.get(0).equals(ARG_IMPORT_CASE)
                        && !arguments.get(0).equals(ARG_VALIDATE_AND_IMPORT) && !arguments.contains(ARG_IMPORT_ALL)) {
                    showHelp();
                    proceed = false;
                }
                break;
            }
            default:
                showHelp();
                proceed = false;
                break;
        }

        if (proceed)
            SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        // Only possible args so far:
        //
        // -validate-all (default)
        // -validate-case case
        // -validate-import [importer_name]
        // -import-all [importer_name]
        // -import-case case

        switch (args.length) {
            case 0: {
                runValidation();
                break;
            }
            case 1: {
                List<String> arguments = Arrays.stream(args).collect(Collectors.toList());
                if (arguments.contains(ARG_VALIDATE_ALL)) {
                    runValidation();
                } else if (arguments.contains(ARG_VALIDATE_AND_IMPORT)) {
                    runValidationAndImport(null);
                } else {
                    runImport(null);
                }
                break;
            }
            case 2: {
                List<String> arguments = Arrays.stream(args).collect(Collectors.toList());
                switch (arguments.get(0)) {
                    case ARG_VALIDATE_CASE:
                        runValidationCaseId(arguments.get(1));
                        break;
                    case ARG_VALIDATE_AND_IMPORT:
                        runValidationAndImport(arguments.get(1));
                        break;
                    case ARG_IMPORT_CASE:
                        runImportCaseId(arguments.get(1));
                        break;
                    case ARG_IMPORT_ALL:
                        runImport(arguments.get(1));
                }
                break;
            }
            default:
                break;
        }

    }

    private void runValidation() throws Exception {
        // call the validator
        List<DocumentValidationReport> result = validationService.validateAll();

        List<ValidationResult> validationResults = result.stream().map(DocumentValidationReport::getErrors).flatMap(List::stream)
                .collect(Collectors.toList());

        if (validationResults.size() > 0) {
            showValidationErrorsFound(reportingFolder);
        } else {
            showNoValidationErrorsFound();
        }

        exitWithSuccess();
    }

    private void runValidationCaseId(String caseId) throws Exception {
        // call the validator
        CaseValidationReport caseValidationReport = validationService.validateSingleCase(caseId);

        List<ValidationResult> validationResults = caseValidationReport.getErrors().stream().map(DocumentValidationReport::getErrors)
                .flatMap(List::stream).collect(Collectors.toList());

        if (validationResults.size() > 0) {
            showValidationErrorsFound(reportingFolder);
        } else {
            showNoValidationErrorsFound();
        }

        exitWithSuccess();
    }

    private void runValidationAndImport(String importerName) throws Exception {
        // call the validator
        List<DocumentValidationReport> result = validationService.validateAll();

        List<ValidationResult> validationResults = result.stream().map(DocumentValidationReport::getErrors).flatMap(List::stream)
                .collect(Collectors.toList());

        if (validationResults.size() > 0) {
            showValidationErrorsFound(reportingFolder);
            exitWithSuccess();
        } else {
            showNoValidationErrorsFound();
            showProceedingToImport();
            runImport(importerName);
        }
    }

    private void runImport(String importerName) {
        try {
            Predicate<Object> filterPredicate = null;
            if (importerName != null) {
                filterPredicate = (clazz) -> ((Class<?>) clazz).getName().endsWith("." + importerName);
            }

            for (DataImporter importer : initializeImporters(filterPredicate)) {
                importer.importData();
            }
        } catch (Exception e) {
            logger.error("Error occurred when importing: ", e);
            exitWithError();
        }

        exitWithSuccess();
    }

    private void runImportCaseId(String caseId) {
        try {
            ProgrammaticTransactionUtil.processSuccessfulTransaction(transactionManager, () -> {
                for (CaseImporter importer : initializeCaseImporters()) {
                    if (!CasesUtils.isDefaultCase(caseId) || (CasesUtils.isDefaultCase(caseId) && importer.processesEmptyCase())) {
                        importer.importData(caseId);
                    }
                }
                applicationContext.getBean(SequenceUpdateService.class).updateSequences();
            });
        } catch (Exception e) {
            logger.error("Error occurred when importing case: " + caseId, e);
            exitWithError();
        }

        exitWithSuccess();
    }

    private List<DataImporter> initializeImporters(final Predicate<Object> filterPredicate) {
        Map<String, Object> annotatedClasses = applicationContext.getBeansWithAnnotation(ElasticTypeImporter.class);

        return getDataImporters(annotatedClasses, importer -> (filterPredicate == null || filterPredicate.test(importer.getClass())));
    }

    private List<CaseImporter> initializeCaseImporters() {
        return getCaseImporters(applicationContext);
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Exits the program with the error exit code
     */
    protected void exitWithError() {
        if (elasticsearchService != null) {
            try {
                // close elasticsearch connection
                elasticsearchService.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.exit(1);
    }

    /**
     * Exits the program with the success code
     */
    protected void exitWithSuccess() {
        if (elasticsearchService != null) {
            try {
                // close elasticsearch connection
                elasticsearchService.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

    private static void showHelp() {
        String help = String.join(System.lineSeparator(), "", "---Data Migration Help---", "", "Usage: java -jar <jar_name> [options]", "",
                "Options:", "", " " + ARG_VALIDATE_ALL + "              Default execution. Validates the data from the source",
                "                            indicated in the config/applications.properties", "",
                " " + ARG_VALIDATE_CASE + " value       Validates the specified caseId indicated",
                "                            in the 'value' parameter.", "",
                " " + ARG_VALIDATE_AND_IMPORT + "           First validates the data and then imports it", "",
                " " + ARG_IMPORT_ALL + "                Imports the data without previous validation", "",
                " " + ARG_IMPORT_CASE + " value         Imports the specified caseId indicated",
                "                            in the 'value' parameter", "",
                " " + ARG_HELP_LONG + ", " + ARG_HELP_SHORT + "                  Shows this help", "");
        System.out.println(help);
    }

    private static void showNoArgsInfo() {
        String help = System.lineSeparator() + "Starting application with DEFAULT execution: only validation." + System.lineSeparator()
                + "Please execute 'java -jar <jar_name> " + ARG_HELP_LONG + "' to see different execution options" + System.lineSeparator();
        System.out.println(help);
    }

    private static void showValidationErrorsFound(String reportingFolder) {
        String help = System.lineSeparator() + "Errors found during validation." + System.lineSeparator()
                + "Please review reports on folder: " + reportingFolder + System.lineSeparator();
        System.out.println(help);
    }

    private static void showNoValidationErrorsFound() {
        String help = System.lineSeparator() + "Errors NOT found during validation." + System.lineSeparator();
        System.out.println(help);
    }

    private static void showProceedingToImport() {
        String help = System.lineSeparator() + "Proceeding to import." + System.lineSeparator();
        System.out.println(help);
    }
}

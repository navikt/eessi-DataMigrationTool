package eu.ec.dgempl.eessi.rina.tool.migration.application.validation;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;

import eu.ec.dgempl.eessi.rina.tool.migration.common.service.EsClientService;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.report.CaseValidationReport;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.report.DocumentValidationReport;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.report.ValidationResult;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.service.ValidationService;

@SpringBootApplication(
        exclude = {
                DataSourceAutoConfiguration.class,
                DataSourceTransactionManagerAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class}
)
@ComponentScan(basePackages = { "eu.ec.dgempl.eessi.rina.tool.migration.*" },
        excludeFilters = {
                @Filter(type = FilterType.REGEX, pattern = "eu.ec.dgempl.eessi.rina.tool.migration.importer.*"),
                @Filter(type = FilterType.REGEX, pattern = "eu.ec.dgempl.eessi.rina.tool.migration.buc.*"),
                @Filter(type = FilterType.REGEX, pattern = "eu.ec.dgempl.eessi.rina.repo.*"),
                @Filter(type = FilterType.REGEX, pattern = "eu.ec.dgempl.eessi.rina.tool.migration.application.full.*")
        })
public class ValidationApp implements CommandLineRunner {

    private final EsClientService elasticsearchService;
    private final ValidationService validationService;

    @Value("${reporting.folder}")
    private String reportingFolder;

    private final static String ARG_VALIDATE_ALL = "-validate-all";
    private final static String ARG_VALIDATE_CASE = "-validate-case";

    public ValidationApp(EsClientService elasticsearchService,
            ValidationService validationService) {
        this.elasticsearchService = elasticsearchService;
        this.validationService = validationService;
    }

    @Override
    public void run(String... args) throws Exception {

        // Only possible args so far:
        //
        // -validate-all (default)
        // -validate-case case

        switch (args.length) {
            case 0: {
                runValidation();
                break;
            }
            case 1: {
                List<String> arguments = Arrays.stream(args).collect(Collectors.toList());
                if (arguments.contains(ARG_VALIDATE_ALL)) {
                    runValidation();
                }
                break;
            }
            case 2: {
                List<String> arguments = Arrays.stream(args).collect(Collectors.toList());
                if (ARG_VALIDATE_CASE.equals(arguments.get(0))) {
                    runValidationCaseId(arguments.get(1));
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

    private static void showValidationErrorsFound(String reportingFolder) {
        String help = System.lineSeparator() + "Errors found during validation." + System.lineSeparator()
                + "Please review reports on folder: " + reportingFolder + System.lineSeparator();
        System.out.println(help);
    }

    private static void showNoValidationErrorsFound() {
        String help = System.lineSeparator() + "Errors NOT found during validation." + System.lineSeparator();
        System.out.println(help);
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

}

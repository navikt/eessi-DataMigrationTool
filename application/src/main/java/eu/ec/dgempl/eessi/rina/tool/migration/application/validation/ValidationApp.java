package eu.ec.dgempl.eessi.rina.tool.migration.application.validation;

import static eu.ec.dgempl.eessi.rina.tool.migration.application.common.ApplicationHelper.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.service.ValidationService;

@SpringBootApplication(
        exclude = {
                DataSourceAutoConfiguration.class,
                DataSourceTransactionManagerAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class }
)
@ComponentScan(basePackages = { "eu.ec.dgempl.eessi.rina.tool.migration.*" },
        excludeFilters = {
                @Filter(type = FilterType.REGEX, pattern = "eu.ec.dgempl.eessi.rina.tool.migration.importer.*"),
                @Filter(type = FilterType.REGEX, pattern = "eu.ec.dgempl.eessi.rina.tool.migration.buc.*"),
                @Filter(type = FilterType.REGEX, pattern = "eu.ec.dgempl.eessi.rina.repo.*"),
                @Filter(type = FilterType.REGEX, pattern = "eu.ec.dgempl.eessi.rina.tool.migration.application.full.*")
        })
public class ValidationApp implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ValidationApp.class);

    private final EsClientService elasticsearchService;
    private final ValidationService validationService;

    @Value("${reporting.folder}")
    private String reportingFolder;

    private final static String ARG_VALIDATE_ALL = "-validate-all";
    private final static String ARG_VALIDATE_CASE = "-validate-case";
    private final static String ARG_VALIDATE_CASES_BULK = "-validate-cases-bulk";

    public ValidationApp(EsClientService elasticsearchService,
            ValidationService validationService) {
        this.elasticsearchService = elasticsearchService;
        this.validationService = validationService;
    }

    @Override
    public void run(String... args) throws Exception {

        List<String> arguments = Arrays.stream(args).collect(Collectors.toList());
        logger.info("Started DMT with the following command: java -jar EESSI-RINA-DATA-MIGRATION-{} {}",
                ValidationApp.class.getPackage().getImplementationVersion(),
                String.join(" ", arguments));

        // Only possible args so far:
        //
        // -validate-all (default)
        // -validate-case case
        // -validate-case-bulk caseFile.txt

        switch (args.length) {
            case 0: {
                runValidation();
                break;
            }
            case 1: {
                if (args[0].equalsIgnoreCase(ARG_VALIDATE_ALL)) {
                    runValidation();
                }
                break;
            }
            case 2: {
                if (args[0].equalsIgnoreCase(ARG_VALIDATE_CASE)) {
                    runValidationCaseId(args[1]);
                } else if (args[0].equalsIgnoreCase(ARG_VALIDATE_CASES_BULK)) {
                    runValidationCaseBulk(args[1]);
                }
                break;
            }
            default:
                break;
        }

    }

    private void runValidation() {
        logger.info("Start validating everything");

        try {
            // call the validator
            int numberOfErrors = validationService.validateAll();

            if (numberOfErrors > 0) {
                showValidationErrorsFound(reportingFolder);
            } else {
                showNoValidationErrorsFound();
            }
        } catch (Exception e) {
            logger.error("A problem occurred when validating Elasticsearch resources", e);
            exitWithError(elasticsearchService);
        }

        exitWithSuccess(elasticsearchService);
    }

    private void runValidationCaseBulk(final String inputFile) {
        if (!Files.exists(new File(inputFile).toPath())) {
            logger.error("Could not find specified validator file [filename={}]", inputFile);
            exitWithError(elasticsearchService);
        }
        List<String> caseIds = getCasesFromFile(inputFile);

        logger.info("Start validating cases [caseIds={}]", caseIds.toString());

        try {
            // call the validator
            int numberOfErrors = validationService.validateBulkCases(caseIds);

            if (numberOfErrors > 0) {
                showValidationErrorsFound(reportingFolder);
            } else {
                showNoValidationErrorsFound();
            }
        } catch (Exception e) {
            logger.error("A problem occurred when validating bulk cases", e);
            exitWithError(elasticsearchService);
        }

        exitWithSuccess(elasticsearchService);
    }

    private void runValidationCaseId(String caseId) {
        logger.info("Start validating case [caseId={}]", caseId);

        try {
            // call the validator
            int numberOfErrors = validationService.validateCase(caseId);

            if (numberOfErrors > 0) {
                showValidationErrorsFound(reportingFolder);
            } else {
                showNoValidationErrorsFound();
            }
        } catch (Exception e) {
            logger.error("A problem occurred when validating case [caseId={}]", caseId, e);
            exitWithError(elasticsearchService);
        }

        exitWithSuccess(elasticsearchService);
    }

    private List<String> getCasesFromFile(String fileName) {
        List<String> cases = new ArrayList<>();
        try {
            cases = Files.readAllLines(new File(fileName).toPath(), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cases;
    }
}

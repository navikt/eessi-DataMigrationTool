package eu.ec.dgempl.eessi.rina.tool.migration.application.full;

import static eu.ec.dgempl.eessi.rina.tool.migration.application.common.ApplicationHelper.*;
import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.ImporterUtils.*;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.common.service.EsClientService;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.GsonWrapper;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.service.ValidationService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.PostCaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.PreCaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.IndexReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.SummaryReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.KeystoreService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.ScriptExecutionService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.cases.EsCasesRunnerService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.cases.FileCasesRunnerService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.cases.SingleCaseRunnerService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.ImporterUtils;

@SpringBootApplication
@ComponentScan(basePackages = { "eu.ec.dgempl.eessi.rina.tool.migration.*" },
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "eu.ec.dgempl.eessi.rina.tool.migration.application.validation.*")
        })
public class FullApp implements CommandLineRunner, ApplicationContextAware {

    private final EsClientService elasticsearchService;
    private final ValidationService validationService;
    private final KeystoreService keystoreService;
    private final RinaJsonMapper rinaJsonMapper;
    private ApplicationContext applicationContext;

    @Value("${reporting.folder}")
    private String reportingFolder;
    private String SUMMARY_REPORT_PATH;

    private final static String ARG_VALIDATE_AND_IMPORT = "-validate-import";
    private final static String ARG_IMPORT_ALL = "-import-all";
    private final static String ARG_IMPORT_CASE = "-import-case";
    private final static String ARG_IMPORT_CASES_BULK = "-import-cases-bulk";

    private final Logger logger = LoggerFactory.getLogger(FullApp.class);

    public FullApp(final EsClientService elasticsearchService, final ValidationService validationService,
            final KeystoreService keystoreService,
            final RinaJsonMapper rinaJsonMapper) {
        this.elasticsearchService = elasticsearchService;
        this.validationService = validationService;
        this.keystoreService = keystoreService;
        this.rinaJsonMapper = rinaJsonMapper;
    }

    @Override
    public void run(String... args) throws Exception {

        List<String> arguments = Arrays.stream(args).collect(Collectors.toList());
        logger.info("Started DMT with the following command: java -jar EESSI-RINA-DATA-MIGRATION-{} {}",
                FullApp.class.getPackage().getImplementationVersion(),
                String.join(" ", arguments));

        SUMMARY_REPORT_PATH = reportingFolder.concat("/importer/SummaryReport.json");

        // Only possible args so far:
        //
        // -validate-all (default)
        // -validate-case case
        // -validate-import [importer_name]
        // -import-all [importer_name]
        // -import-case-bulk caseFile.txt
        // -import-case case

        switch (args.length) {
            case 0: {
                break;
            }
            case 1: {
                if (arguments.contains(ARG_VALIDATE_AND_IMPORT)) {
                    runValidationAndImport(null);
                } else {
                    runImport(null);
                }
                break;
            }
            case 2: {
                switch (arguments.get(0)) {
                    case ARG_VALIDATE_AND_IMPORT:
                        runValidationAndImport(arguments.get(1));
                        break;
                    case ARG_IMPORT_CASE:
                        runImportCaseId(arguments.get(1));
                        break;
                    case ARG_IMPORT_CASES_BULK:
                        runImportCaseBulk(arguments.get(1));
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

    private void runValidationAndImport(String importerName) throws Exception {
        // call the validator
        int numberOfErrors = validationService.validateAll();

        if (numberOfErrors > 0) {
            showValidationErrorsFound(reportingFolder);
            exitWithSuccess(elasticsearchService);
        } else {
            showNoValidationErrorsFound();
            showProceedingToImport();
            runImport(importerName);
        }
    }

    private void runImport(String importerName) {
        try {
            if (StringUtils.isEmpty(importerName)) {
                ScriptExecutionService scriptExecutionService = applicationContext.getBean(ScriptExecutionService.class);
                scriptExecutionService.cleanupTables();
            }

            SummaryReport summaryReport = new SummaryReport();

            Predicate<PreCaseImporter> preCaseImporterPredicate =
                    importer -> StringUtils.isEmpty(importerName) || importer.getClass().getName().endsWith("." + importerName);
            List<IndexReport> indexReportsPre = runPreCaseImporters(preCaseImporterPredicate);
            summaryReport.swallow(indexReportsPre);

            if (StringUtils.isEmpty(importerName)) {
                EsCasesRunnerService esCasesRunnerService = applicationContext.getBean(EsCasesRunnerService.class);
                summaryReport.swallow(esCasesRunnerService.importData());
            }

            Predicate<PostCaseImporter> postCaseImporterPredicate =
                    importer -> StringUtils.isEmpty(importerName) || importer.getClass().getName().endsWith("." + importerName);
            List<IndexReport> indexReportsPost = runPostCaseImporters(postCaseImporterPredicate);
            summaryReport.swallow(indexReportsPost);

            logger.info(rinaJsonMapper.objToJson(summaryReport));
            GsonWrapper.writeToFile(summaryReport, SUMMARY_REPORT_PATH);

            keystoreService.syncJksBusinessKeysWithDb();

        } catch (Exception e) {
            logger.error("Error occurred when importing: ", e);
            exitWithError(elasticsearchService);
        }

        exitWithSuccess(elasticsearchService);
    }

    private void runImportCaseBulk(final String inputFile) {
        if (!Files.exists(new File(inputFile).toPath())) {
            logger.error("Could not find specified import file.");
            exitWithError(elasticsearchService);
        }

        FileCasesRunnerService fileCasesRunnerService = applicationContext.getBean(FileCasesRunnerService.class);
        fileCasesRunnerService.setCasesInputFile(inputFile);
        try {
            SummaryReport summaryReport = new SummaryReport();
            summaryReport.swallow(fileCasesRunnerService.importData());

            logger.info(rinaJsonMapper.objToJson(summaryReport));
            GsonWrapper.writeToFile(summaryReport, SUMMARY_REPORT_PATH);

        } catch (Exception e) {
            logger.error("A problem occurred when importing bulk cases", e);
            exitWithError(elasticsearchService);
        }

        exitWithSuccess(elasticsearchService);
    }

    private List<IndexReport> runPreCaseImporters(final Predicate<PreCaseImporter> preCaseImporterPredicate) {
        List<PreCaseImporter> preCaseImporters = getPreCaseImporters(applicationContext, preCaseImporterPredicate);
        return preCaseImporters.stream().map(
                importer -> ImporterUtils.runImporter(importer, logger, rinaJsonMapper)).collect(
                Collectors.toList());
    }

    private List<IndexReport> runPostCaseImporters(final Predicate<PostCaseImporter> postCaseImporterPredicate) {
        List<PostCaseImporter> postCaseImporters = getPostCaseImporters(applicationContext, postCaseImporterPredicate);
        return postCaseImporters.stream().map(
                importer -> ImporterUtils.runImporter(importer, logger, rinaJsonMapper)).collect(
                Collectors.toList());
    }

    private void runImportCaseId(String caseId) {
        SingleCaseRunnerService singleCaseRunnerService = applicationContext.getBean(SingleCaseRunnerService.class);
        singleCaseRunnerService.setCaseId(caseId);
        try {
            SummaryReport summaryReport = new SummaryReport();
            summaryReport.swallow(singleCaseRunnerService.importData());

            logger.info(rinaJsonMapper.objToJson(summaryReport));
            GsonWrapper.writeToFile(summaryReport, SUMMARY_REPORT_PATH);

        } catch (Exception e) {
            logger.error("Could not import case {}", caseId, e);
            exitWithError(elasticsearchService);
        }

        exitWithSuccess(elasticsearchService);
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}

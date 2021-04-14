package eu.ec.dgempl.eessi.rina.tool.migration.importer.service.cases._abstract;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.common.service.EsClientService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.CaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.PostCaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.GenericImporterException;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.CaseReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.CasesSummaryReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.IndexReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.ReportError;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.SummaryReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.DatabaseCleanupService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.SequenceUpdateService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.CasesUtils;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.ImporterUtils;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.ProgrammaticTransactionUtil;

public abstract class AbstractCasesRunnerService {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCasesRunnerService.class);

    private ApplicationContext applicationContext;
    private PlatformTransactionManager transactionManager;
    private RinaJsonMapper rinaJsonMapper;
    private DatabaseCleanupService databaseCleanupService;
    protected EsClientService esClientService;

    public SummaryReport importData() throws Exception {
        List<CaseImporter> importers = initializeImporters();

        List<String> esCases = esClientService.getCaseIds();
        List<String> cases = getCases();
        Instant batchStart = Instant.now();

        System.out.println("\nStart processing for " + cases.size() + " CASES");

        if (shouldCleanupPostCaseResource()) {
            databaseCleanupService.cleanupPostCaseResources();
        }

        CasesSummaryReport casesSummaryReport = new CasesSummaryReport();

        for (int i = 0; i < cases.size(); i++) {
            String caseId = cases.get(i);

            if (!CasesUtils.isDefaultCase(caseId) && !esCases.contains(caseId)) {
                logger.error("Could not find caseId {} in ES", caseId);
                continue;
            }

            CaseReport caseReport = startImportersForCase(importers, caseId);
            casesSummaryReport.swallow(caseReport);

            if (i != 0 && (i % 100) == 0) {
                System.out.println("Cases processed: " + i + ". Pending to process: " + (cases.size() - i));
            }
        }

        List<IndexReport> postCaseImportersReports = null;
        if (shouldCleanupPostCaseResource()) {
            List<PostCaseImporter> postCaseImporters = ImporterUtils.getPostCaseImporters(applicationContext, importer -> true);
            postCaseImportersReports = postCaseImporters.stream().map(importer -> ImporterUtils.runImporter(importer, logger, rinaJsonMapper)).collect(
                    Collectors.toList());
        }

        float processTime = Duration.between(batchStart, Instant.now()).toMillis() / 1000F;
        System.out.println("Finished processing for " + cases.size() + " CASES in " + processTime + " seconds");
        System.out.println("\n-----------------------------------------------------");

        logger.info(rinaJsonMapper.objToJson(casesSummaryReport));

        SummaryReport summaryReport = new SummaryReport();
        summaryReport.swallow(casesSummaryReport);
        summaryReport.swallow(postCaseImportersReports);

        return summaryReport;
    }

    protected abstract List<String> getCases();

    protected boolean shouldCleanupPostCaseResource() {
        return true;
    }

    private CaseReport startImportersForCase(final List<CaseImporter> importers, final String caseId) {
        logger.info("Started import for case " + caseId);

        CaseReport caseReport = new CaseReport();
        caseReport.setCaseId(caseId);

        try {
            // Count existing documents before processing them
            for (CaseImporter importer : importers) {
                if (!CasesUtils.isDefaultCase(caseId) || (CasesUtils.isDefaultCase(caseId) && importer.processesEmptyCase())) {
                    long docsByCaseId = importer.countDocsByCaseId(caseId);
                    if (docsByCaseId > 0) {
                        caseReport.setupReport(importer.inferElasticType(), docsByCaseId);
                    }
                }
            }

            ProgrammaticTransactionUtil.processSuccessfulTransaction(transactionManager, () -> {

                if (shouldCleanupPostCaseResource()) {
                    databaseCleanupService.cleanupCaseResources(caseId);
                }

                for (CaseImporter importer : importers) {
                    if (!CasesUtils.isDefaultCase(caseId) || (CasesUtils.isDefaultCase(caseId) && importer.processesEmptyCase())) {
                        try {
                            DocumentsReport documentsReport = importer.importData(caseId);
                            if (documentsReport != null && documentsReport.getProcessed().longValue() > 0) {
                                caseReport.swallow(documentsReport);
                            }
                        } catch (Exception e) {
                            List<ReportError> errors = caseReport.getErrors();

                            if (errors == null) {
                                errors = new ArrayList<>();
                                caseReport.setErrors(errors);
                            }

                            EElasticType eElasticType = importer.inferElasticType();
                            String importerName = importer.getClass().getSimpleName();
                            String documentId = null;
                            String index = null;
                            String type = null;

                            if (e.getCause() instanceof GenericImporterException && EElasticType.NONE != eElasticType) {
                                documentId = ((GenericImporterException) e.getCause()).getElasticId();
                                index = eElasticType.getIndex();
                                type = eElasticType.getType();
                            }

                            errors.add(new ReportError(
                                    index,
                                    type,
                                    documentId,
                                    importerName,
                                    ExceptionUtils.getStackTrace(e)));
                            throw e;
                        }
                    }
                }
                applicationContext.getBean(SequenceUpdateService.class).updateSequences();
                logger.info("Imported case " + caseId + ": " + rinaJsonMapper.objToJson(caseReport));
            });
        } catch (Exception e) {
            try {
                logger.error("Error occurred when importing case: " + caseId + ". Importing status until error: " + rinaJsonMapper
                        .objToJson(caseReport));
            } catch (JsonProcessingException jsonProcessingException) {
                jsonProcessingException.printStackTrace();
            }
            return caseReport;
        }
        return caseReport;
    }

    private List<CaseImporter> initializeImporters() {
        return ImporterUtils.getCaseImporters(applicationContext);
    }

    @Autowired
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setTransactionManager(final PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Autowired
    public void setEsClientService(final EsClientService esClientService) {
        this.esClientService = esClientService;
    }

    @Autowired
    public void setRinaJsonMapper(final RinaJsonMapper rinaJsonMapper) {
        this.rinaJsonMapper = rinaJsonMapper;
    }

    @Autowired
    public void setDatabaseCleanupService(DatabaseCleanupService databaseCleanupService) {
        this.databaseCleanupService = databaseCleanupService;
    }
}

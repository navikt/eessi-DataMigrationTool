package eu.ec.dgempl.eessi.rina.tool.migration.importer.service.cases._abstract;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.elasticsearch.search.SearchHit;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.Pair;
import org.springframework.transaction.PlatformTransactionManager;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.common.config.RunConfiguration;
import eu.ec.dgempl.eessi.rina.tool.migration.common.model.EEsIndex;
import eu.ec.dgempl.eessi.rina.tool.migration.common.model.EEsType;
import eu.ec.dgempl.eessi.rina.tool.migration.common.model.EsSearchQueryTerm;
import eu.ec.dgempl.eessi.rina.tool.migration.common.model.fields.CaseFields;
import eu.ec.dgempl.eessi.rina.tool.migration.common.service.EsClientService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.CaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.PostCaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.cases.DocumentContentImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.cases.DocumentImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.cases.NotificationImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.GenericImporterException;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.CaseReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.CasesSummaryReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.IndexReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.ReportError;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.SummaryReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.DocumentContentFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.DocumentFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.ScriptExecutionService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.SequenceUpdateService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.ImporterUtils;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.LogUtils;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.ProgrammaticTransactionUtil;

public abstract class AbstractCasesRunnerService {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCasesRunnerService.class);

    private ApplicationContext applicationContext;
    private PlatformTransactionManager transactionManager;
    private RinaJsonMapper rinaJsonMapper;
    private ScriptExecutionService scriptExecutionService;
    private RunConfiguration runConfiguration;
    protected EsClientService esClientService;

    public SummaryReport importData() throws Exception {

        List<String> esCases = esClientService.getCaseIds();
        List<String> cases = getCases();
        Instant batchStart = Instant.now();

        int threadsNumber = runConfiguration.getThreadsNumber();
        String logSuffix = threadsNumber > 1 ? " using " + threadsNumber + " threads" : "";

        System.out.println("\nStart processing for " + cases.size() + " CASES" + logSuffix);
        logger.info("Start processing for " + cases.size() + " CASES" + logSuffix);

        if (shouldCleanupPostCaseResource()) {
            scriptExecutionService.cleanupPostCaseResources();
        }

        CasesSummaryReport casesSummaryReport = new CasesSummaryReport();

        ExecutorService executor = Executors.newFixedThreadPool(threadsNumber);

        Map<Long, List<CaseImporter>> importersMap = new ConcurrentHashMap<>();

        List<Future<Void>> results = new ArrayList<>();

        for (int i = 0; i < cases.size(); i++) {

            String caseId = cases.get(i);
            if (isValidCase(esCases, caseId)) {
                final int idx = i;

                results.add(executor.submit(() -> {
                    List<CaseImporter> importers = assignImportersToThread(importersMap);
                    processSingleCase(cases, casesSummaryReport, caseId, idx, importers);

                    return null;
                }));

            }
        }

        getFutureResults(results);

        executor.shutdown();

        scriptExecutionService.cleanupCasePrefill();

        if (cases.contains(CaseFields.DEFAULT_CASE_ID)) {
            handleCaseZero(casesSummaryReport, shouldCleanupPostCaseResource(), threadsNumber);
        }

        List<IndexReport> postCaseImportersReports = null;
        if (shouldCleanupPostCaseResource()) {
            List<PostCaseImporter> postCaseImporters = ImporterUtils.getPostCaseImporters(applicationContext, importer -> true);
            postCaseImportersReports = postCaseImporters.stream().map(
                    importer -> ImporterUtils.runImporter(importer, logger, rinaJsonMapper)).collect(
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

    private void processSingleCase(
            final List<String> cases,
            final CasesSummaryReport casesSummaryReport,
            final String caseId,
            final int idx,
            final List<CaseImporter> importers) {

        CaseReport caseReport = startImportersForCase(importers, caseId);
        casesSummaryReport.swallow(caseReport);

        if (idx != 0 && (idx % 100) == 0) {
            System.out.println("Cases processed: " + idx + ". Pending to process: " + (cases.size() - idx));
        }
    }

    private boolean isValidCase(List<String> esCases, String caseId) {
        if (!esCases.contains(caseId)) {
            if (!CaseFields.DEFAULT_CASE_ID.equalsIgnoreCase(caseId)) {
                logger.error("Could not find caseId {} in ES", caseId);
            }
            return false;
        }
        return true;
    }

    private synchronized List<CaseImporter> assignImportersToThread(final Map<Long, List<CaseImporter>> importersMap) {
        final Long threadId = Thread.currentThread().getId();
        return importersMap.computeIfAbsent(threadId, key -> initializeImporters());
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
                long docsByCaseId = importer.countDocsByCaseId(caseId);
                if (docsByCaseId > 0) {
                    caseReport.setupReport(importer.inferElasticType(), docsByCaseId);
                }
            }

            ProgrammaticTransactionUtil.processSuccessfulTransaction(transactionManager, () -> {

                if (shouldCleanupPostCaseResource()) {
                    scriptExecutionService.cleanupCaseResources(caseId);
                }

                for (CaseImporter importer : importers) {
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

                        if (e.getCause() instanceof GenericImporterException) {
                            documentId = ((GenericImporterException) e.getCause()).getElasticId();

                            if (EElasticType.NONE != eElasticType) {
                                index = eElasticType.getIndex();
                                type = eElasticType.getType();
                            }
                            DocumentsReport documentsReport = ((GenericImporterException) e.getCause()).getDocumentsReport();
                            if (documentsReport != null) {
                                caseReport.swallow(documentsReport);
                            }
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

    private void handleCaseZero(
            final CasesSummaryReport casesSummaryReport,
            final boolean cleanupCaseResources,
            final int threadsNumber) throws Exception {

        if (cleanupCaseResources) {
            scriptExecutionService.cleanupCaseResources(CaseFields.DEFAULT_CASE_ID);
        }

        DocumentImporter documentImporter = applicationContext.getBean(DocumentImporter.class);
        DocumentContentImporter documentContentImporter = applicationContext.getBean(DocumentContentImporter.class);
        NotificationImporter notificationImporter = applicationContext.getBean(NotificationImporter.class);

        CaseReport caseReport = setupCaseReport(List.of(documentImporter, documentContentImporter, notificationImporter));
        DocumentsReport documentsReport = getDocumentReport(caseReport, EElasticType.CASES_DOCUMENT);
        DocumentsReport documentContentsReport = getDocumentReport(caseReport, EElasticType.CASES_DOCUMENTCONTENT);

        logger.info(
                "Started import for caseId {} with {} documents and {} documentContents",
                CaseFields.DEFAULT_CASE_ID,
                documentsReport.getTotal(),
                documentContentsReport.getTotal());
        System.out.println("Started import for special case " + CaseFields.DEFAULT_CASE_ID +
                (threadsNumber > 1 ? " using " + threadsNumber + " threads" : ""));

        ExecutorService executor = Executors.newFixedThreadPool(threadsNumber);
        Consumer<Map<String, Pair<MapHolder, List<MapHolder>>>> docsAndDocContentsConsumer = createDocsAndDocContentsConsumer(
                documentImporter,
                documentContentImporter,
                documentsReport,
                documentContentsReport,
                executor);

        handleDocsAndDocContentsMap(docsAndDocContentsConsumer);

        executor.shutdown();

        caseReport.swallow(documentsReport);
        caseReport.swallow(documentContentsReport);

        DocumentsReport notificationsReport = notificationImporter.importDataWithoutTransaction(CaseFields.DEFAULT_CASE_ID);
        caseReport.swallow(notificationsReport);

        logger.info("Imported case {}: {}", CaseFields.DEFAULT_CASE_ID, rinaJsonMapper.objToJson(caseReport));
        System.out.println("Imported special case " + CaseFields.DEFAULT_CASE_ID);
        casesSummaryReport.swallow(caseReport);
    }

    private Consumer<Map<String, Pair<MapHolder, List<MapHolder>>>> createDocsAndDocContentsConsumer(
            final DocumentImporter documentImporter,
            final DocumentContentImporter documentContentImporter,
            final DocumentsReport documentsReport,
            final DocumentsReport documentContentsReport,
            final ExecutorService executor) {

        final AtomicInteger globalCounter = new AtomicInteger(0);
        final AtomicLong processedDocs = new AtomicLong(0);
        final Instant batchStart = Instant.now();

        return docsAndContentsMap -> {
            final Instant batchTimeStart = Instant.now();
            final List<Future<Void>> results = new ArrayList<>();

            docsAndContentsMap.values().forEach(pair -> results.add(executor.submit(() -> {
                processDocAndContentPair(
                        documentImporter,
                        documentContentImporter,
                        documentsReport,
                        documentContentsReport,
                        pair);

                return null;
            })));

            getFutureResults(results);

            LogUtils.printImportProcess(
                    documentsReport.getTotal(),
                    globalCounter,
                    processedDocs,
                    batchStart,
                    batchTimeStart,
                    logger);
        };
    }

    private void processDocAndContentPair(
            final DocumentImporter documentImporter,
            final DocumentContentImporter documentContentImporter,
            final DocumentsReport documentsReport,
            final DocumentsReport documentContentsReport,
            final Pair<MapHolder, List<MapHolder>> pair) {

        try {
            ProgrammaticTransactionUtil.processSuccessfulTransaction(transactionManager, () -> {
                processDoc(documentImporter, documentsReport, pair.getFirst());

                List<MapHolder> docContents = pair.getSecond();
                docContents.forEach(content ->
                        processDoc(documentContentImporter, documentContentsReport, content)
                );
            });
        } catch (Exception e) {
            // ignore exception since the transactionality is done on document and documentContent pairs
        }
    }

    private void handleDocsAndDocContentsMap(Consumer<Map<String, Pair<MapHolder, List<MapHolder>>>> docsAndContentsMapConsumer)
            throws IOException {

        EsSearchQueryTerm queryTerm = new EsSearchQueryTerm("caseId", CaseFields.DEFAULT_CASE_ID);

        esClientService.processAllFilterByTerms(
                EEsIndex.CASES.value(),
                new String[] { EEsType.DOCUMENT.value() },
                (SearchHit[] hits) -> {
                    Map<String, Pair<MapHolder, List<MapHolder>>> docsAndContentsMap = new ConcurrentHashMap<>();

                    List<String> docIds = Stream.of(hits)
                            .map(searchHit -> {
                                MapHolder docHolder = createMapHolder(searchHit);

                                String id = docHolder.string(DocumentFields.ID);
                                docsAndContentsMap.put(id, Pair.of(docHolder, new ArrayList<>()));

                                return id;
                            })
                            .collect(Collectors.toList());

                    try {
                        esClientService.processDocumentContentsByCaseIdAndIds(
                                CaseFields.DEFAULT_CASE_ID,
                                docIds.toArray(new String[] {}),
                                createDocContentsConsumer(docsAndContentsMap));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    docsAndContentsMapConsumer.accept(docsAndContentsMap);
                },
                queryTerm
        );
    }

    private void getFutureResults(final List<Future<Void>> results) {
        try {
            for (Future<Void> result : results) {
                result.get();
            }
        } catch (InterruptedException | ExecutionException ex) {
            logger.info("ERROR getting future results: " + ex.getMessage());
        }
    }

    @NotNull
    private Consumer<SearchHit[]> createDocContentsConsumer(Map<String, Pair<MapHolder, List<MapHolder>>> docsAndContentsMap) {
        return (SearchHit[] docContentHits) -> {
            for (SearchHit hit : docContentHits) {
                MapHolder docContentHolder = createMapHolder(hit);
                String docId = docContentHolder.string(DocumentContentFields.ID);

                List<MapHolder> docContents = docsAndContentsMap.get(docId).getSecond();
                docContents.add(docContentHolder);
            }
        };
    }

    @NotNull
    private MapHolder createMapHolder(SearchHit searchHit) {
        Map<String, Object> doc = searchHit.getSourceAsMap();
        doc.put("_id", searchHit.getId());
        ConcurrentHashMap<String, Boolean> visitedFields = new ConcurrentHashMap<>();
        visitedFields.put("_id", true);
        return new MapHolder(doc, visitedFields, "");
    }

    private void processDoc(
            final CaseImporter importer,
            final DocumentsReport documentsReport,
            final MapHolder doc) {
        EElasticType eElasticType = importer.inferElasticType();
        try {
            importer.processDocumentData(doc);
            documentsReport.getProcessed().incrementAndGet();
        } catch (Exception e) {
            String _id = doc.string("_id");
            List<ReportError> errors = documentsReport.getErrors();
            if (errors == null) {
                errors = new ArrayList<>();
                documentsReport.setErrors(errors);
            }
            errors.add(new ReportError(
                    eElasticType.getIndex(),
                    eElasticType.getType(),
                    _id,
                    null,
                    ExceptionUtils.getStackTrace(e)));

            logger.warn("Could not import document with id: [{}]", _id, e);

            throw e;
        }
    }

    @NotNull
    private CaseReport setupCaseReport(List<CaseImporter> importers) {
        CaseReport caseReport = new CaseReport();
        caseReport.setCaseId(CaseFields.DEFAULT_CASE_ID);

        for (CaseImporter importer : importers) {
            caseReport.setupReport(importer.inferElasticType(), importer.countDocsByCaseId(CaseFields.DEFAULT_CASE_ID));
        }

        return caseReport;
    }

    private DocumentsReport getDocumentReport(CaseReport caseReport, EElasticType eElasticType) {
        return caseReport.getReport().get(CaseReport.getKey(eElasticType.getIndex(), eElasticType.getType()));
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
    public void setRunConfiguration(final RunConfiguration runConfiguration) {
        this.runConfiguration = runConfiguration;
    }

    @Autowired
    public void setDatabaseCleanupService(ScriptExecutionService scriptExecutionService) {
        this.scriptExecutionService = scriptExecutionService;
    }
}

package eu.ec.dgempl.eessi.rina.tool.migration.exporter.service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.ec.dgempl.eessi.rina.tool.migration.common.config.RunConfiguration;
import eu.ec.dgempl.eessi.rina.tool.migration.common.model.EEsIndex;
import eu.ec.dgempl.eessi.rina.tool.migration.common.model.EEsType;
import eu.ec.dgempl.eessi.rina.tool.migration.common.service.EsClientService;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.GsonWrapper;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.aggregator.Aggregator;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.aggregator.ValidationAggregator;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.cache.CacheEntry;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.EsDocument;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.report.CaseValidationReport;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.report.ContextValidationReport;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.report.DocumentValidationReport;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.report.ValidationResult;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.util.ContentNavigator;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.util.IndexTypeHelper;

/**
 * Service that defines methods for validating different types of resources
 */
@Service
public class ValidationService {
    private static final Logger logger = LoggerFactory.getLogger(ValidationService.class);

    @Value("${reporting.folder}")
    private String reportsFolder;
    private final EsClientService elasticsearchService;
    private final ParserService parser;
    private final CacheService cacheService;
    private final RunConfiguration runConfiguration;

    private Instant globalStart = Instant.now();

    private static final List<EEsIndex> MULTI_THREAD_INDICES = List.of(
            EEsIndex.AUDIT,
            EEsIndex.ENTITIES
    );

    @Autowired
    public ValidationService(EsClientService elasticsearchService, ParserService parser, CacheService cacheService,
            RunConfiguration runConfiguration) {
        this.elasticsearchService = elasticsearchService;
        this.parser = parser;
        this.cacheService = cacheService;
        this.runConfiguration = runConfiguration;
    }

    /**
     * Method for validating all the resources in the entire Elasticsearch, in all known indices
     *
     * @throws IOException
     */
    public int validateAll() throws IOException {
        globalStart = Instant.now();
        int numberOfErrors = 0;

        int threadsNumber = runConfiguration.getThreadsNumber();
        ExecutorService executor = Executors.newFixedThreadPool(threadsNumber);

        numberOfErrors += validateAllInIndex(EEsIndex.ADMIN.value(), executor);
        numberOfErrors += validateAllInIndex(EEsIndex.AUDIT.value(), executor);
        numberOfErrors += validateAllInIndex(EEsIndex.BUSINESSEXCEPTIONS.value(), executor);
        numberOfErrors += validateAllInIndex(EEsIndex.CHECKS.value(), executor);
        numberOfErrors += validateAllInIndex(EEsIndex.CONFIGURATIONS.value(), executor);
        numberOfErrors += validateAllInIndex(EEsIndex.ENTITIES.value(), executor);
        numberOfErrors += validateAllInIndex(EEsIndex.GLOBALCONFIGURATIONS.value(), executor);
        numberOfErrors += validateAllInIndex(EEsIndex.IDENTITY.value(), executor);
        numberOfErrors += validateAllInIndex(EEsIndex.RESOURCES.value(), executor);
        numberOfErrors += validateAllInIndex(EEsIndex.VOCABULARIES.value(), executor);
        numberOfErrors += validateAllCases(executor);

        reportIgnoredDocuments();

        executor.shutdown();

        System.out.println("Validation has finished.");

        return numberOfErrors;
    }

    /**
     * Method for validating all the resources found in a specific {@code index}
     *
     * @param index the elasticsearch index
     * @throws IOException
     */
    public int validateAllInIndex(String index, ExecutorService executorService) throws IOException {
        PreconditionsHelper.notEmpty(index, "index");

        // initialize stats variables
        AtomicLong processedCount = new AtomicLong();
        Instant indexStart = Instant.now();

        ContextValidationReport report = new ContextValidationReport();
        AtomicInteger numberOfErrors = new AtomicInteger();
        int part = -1;

        // get the list of types in the index
        String[] types = IndexTypeHelper.getTypesByIndex(index).toArray(new String[0]);

        // get the number of documents in the index to be processed
        long totalCount = elasticsearchService.getCount(index, types);

        logStartValidation(index, totalCount);

        if (totalCount == 0) {
            System.out.println(String.format("Index: %s. Documents in index: 0.", index));
        } else {
            // define a processor that will be applied to all the SearchHit results
            Consumer<SearchHit[]> processor = hits -> {
                Instant batchStart = Instant.now();
                final List<Future<Void>> results = new ArrayList<>();

                for (SearchHit hit : hits) {
                    if (MULTI_THREAD_INDICES.contains(EEsIndex.fromValue(index))) {
                        results.add(executorService.submit(() -> {
                            processSingleSearchHit(report, numberOfErrors, hit);
                            return null;
                        }));
                    } else {
                        processSingleSearchHit(report, numberOfErrors, hit);
                    }
                }

                getFutureResults(results);

                Instant now = Instant.now();
                processedCount.addAndGet(hits.length);
                float batchTime = calculateTimeBetween(batchStart, now);
                float indexTime = calculateTimeBetween(indexStart, now);
                float totalTime = calculateTimeBetween(globalStart, now);

                System.out.println(String.format("Index: %s. Processed: %d/%d. Batch time: %.2f. Index time: %.2f. Total time: %.2f.",
                        index, processedCount.get(), totalCount, batchTime, indexTime, totalTime));
            };

            // query and process results
            elasticsearchService.processAll(index, types, processor);
        }

        logFinishedValidation(index, report);

        writeReport(report, index, part);

        return numberOfErrors.get();
    }

    private void processSingleSearchHit(final ContextValidationReport report, final AtomicInteger numberOfErrors, final SearchHit hit) {
        Map<String, Object> internalObj = hit.getSourceAsMap();
        EsDocument internalDocument = new EsDocument(hit.getIndex(), hit.getType(), hit.getId());
        internalDocument.setObject(internalObj);
        EsDocument internalParent = new EsDocument(hit.getIndex(), hit.getType(), hit.getId());

        // validate the document
        DocumentValidationReport internalReport = validateSingleDocument(internalDocument, internalParent);

        // update the total number of errors
        numberOfErrors.addAndGet(internalReport.getErrors().size());

        // aggregate results at the index level
        report.swallow(internalReport);
    }

    /**
     * Method for validating all the resources contained in all known cases
     *
     * @throws IOException
     */
    public int validateAllCases(final ExecutorService executorService) throws IOException {
        int batchSize = 50;

        // get the list of caseIds
        List<String> caseIds = elasticsearchService.getCaseIds();

        // add the special value "0" - documents like X050, SYN002, SYN005 have caseId="0"
        caseIds.add("0");

        // add the special value "tempcaseid"
        // caseIds.add("tempcaseid");

        return processCases(caseIds, batchSize, executorService);
    }

    /**
     * Method for validating all the resources contained in a list of cases
     *
     * @throws IOException
     */
    public int validateBulkCases(final List<String> caseIds) throws IOException {
        int batchSize = 10;

        int threadsNumber = runConfiguration.getThreadsNumber();
        ExecutorService executor = Executors.newFixedThreadPool(threadsNumber);

        int numberOfErrors = processCases(caseIds, batchSize, executor);

        executor.shutdown();

        return numberOfErrors;
    }

    public int validateCase(String caseId) throws IOException {
        String index = EEsIndex.CASES.value();
        int part = -1;

        ContextValidationReport report = new ContextValidationReport();

        // generate validation report
        CaseValidationReport caseReport = validateSingleCase(caseId);

        // propagate the report
        report.swallow(caseReport);

        writeReport(report, index, part);

        return caseReport.getErrors().size();
    }

    private void reportIgnoredDocuments() throws IOException {
        String index = EEsIndex.CASES.value();
        String[] types = IndexTypeHelper.getTypesByIndex(index).toArray(new String[0]);

        System.out.println("Start compiling the list of documents that can be ignored.\n");
        logger.info("Start compiling the list of documents that can be ignored.");

        Instant indexStart = Instant.now();

        Map<String, Map<String, Integer>> ignored = elasticsearchService.getOrphanResources(index, types);

        Map<String, Object> processed = new LinkedHashMap<>();

        Map<String, Integer> aggregated = new HashMap<>();
        ignored.values().stream().forEach(v -> {
            v.entrySet().stream().forEach(entry -> {
                Integer no = aggregated.get(entry.getKey());
                if (no == null) {
                    aggregated.put(entry.getKey(), entry.getValue());
                } else {
                    aggregated.put(entry.getKey(), no + entry.getValue());
                }
            });
        });
        processed.put("aggregated", aggregated);
        processed.put("detailed", ignored);

        Instant now = Instant.now();
        float indexTime = Duration.between(indexStart, now).toMillis() / 1000F;
        float totalTime = Duration.between(globalStart, now).toMillis() / 1000F;

        System.out.println(String.format("Index: %s. Index time: %.2f. Total time: %.2f.", index, indexTime, totalTime));
        System.out.println("\nFinished compiling the list of documents that can be ignored.");
        System.out.println("\n-----------------------------------------------------\n");

        logger.info("Finished compiling the list of documents that can be ignored.");
        logger.info(GsonWrapper.stringify(processed));

        GsonWrapper.writeToFile(processed, reportsFolder + "/validator/ignored.json");
    }

    /**
     * Method for validating all the resources contained in a single case identified by {@code caseId}
     *
     * @param caseId the id of the case
     * @return the case-level report
     * @throws IOException
     */
    private CaseValidationReport validateSingleCase(String caseId) throws IOException {
        PreconditionsHelper.notEmpty(caseId, "caseId");

        // TODO comment/uncomment lines below; they are just for testing
//        Instant globalStart = Instant.now();
//        AtomicReference<Instant> batchStart = new AtomicReference<>(Instant.now());
//        AtomicInteger totalNumberOfDocuments = new AtomicInteger();

        String tenantId;

        if (caseId.equals("0")) {
            tenantId = "-";
        } else {
            Map<String, Object> tenant = elasticsearchService.getWhoami(caseId);
            tenantId = (String) ContentNavigator.getField(tenant, "whoami", "id");

            if (tenantId == null) {
                logger.error("Could not extract whoami [case={}]", caseId);
                tenantId = "UNKNOWN";
            }
        }

        CaseValidationReport caseReport = new CaseValidationReport(caseId, tenantId);

        // validate CASEMETADATA
        DocumentValidationReport documentReport = validateCaseDocument(caseId, EEsType.CASEMETADATA);

        // aggregate results at the case level
        if (documentReport != null) {
            caseReport.swallow(documentReport);
        }

        // validate CASESTRUCTUREDMETADATA
        documentReport = validateCaseDocument(caseId, EEsType.CASESTRUCTUREDMETADATA);

        // aggregate results at the case level
        if (documentReport != null) {
            caseReport.swallow(documentReport);
        }

        // define a processor that will be applied to all the SearchHit results
        Consumer<SearchHit[]> processor = hits -> {
            Arrays.stream(hits).forEach(hit -> {
                // try to help the cache - add document and subdocument ids (they are searched the most)
                if (!caseId.equals("0") && (hit.getType().equals(EEsType.DOCUMENT.value()) || hit.getType().equals(
                        EEsType.SUBDOCUMENT.value()))) {
                    CacheEntry entry = new CacheEntry(true, hit.getIndex(), hit.getType(), hit.getId());
                    cacheService.add(entry);
                }

                Map<String, Object> internalObj = hit.getSourceAsMap();
                EsDocument internalDocument = new EsDocument(hit.getIndex(), hit.getType(), hit.getId());
                internalDocument.setObject(internalObj);
                EsDocument internalParent = new EsDocument(EEsIndex.CASES.value(), EEsType.CASEMETADATA.value(), caseId);

                // validate the document
                DocumentValidationReport internalReport = validateSingleDocument(internalDocument, internalParent);

                // aggregate results at the case level
                caseReport.swallow(internalReport);
            });

            // TODO comment/uncomment lines below; they are just for testing
//            Instant current = Instant.now();
//            float batchTime = Duration.between(batchStart.get(), current).toMillis() / 1000F;
//            float totalTime = Duration.between(globalStart, current).toMillis() / 1000F;
//            totalNumberOfDocuments.addAndGet(hits.length);
//            System.out.println(String.format("Batch: %d. Total: %d. Batch time: %.2f. Total time: %.2f.", hits.length,
//                    totalNumberOfDocuments.get(), batchTime, totalTime));
//            batchStart.set(Instant.now());
        };

        // process all resources belonging to a case, except for CASEMETADATA and CASESTRUCTUREDMETADATA
        elasticsearchService.processAll(caseId, processor);

        logger.info(caseReport.toString());

        // if there are any errors, write the report on the disk
        if (caseReport.getErrors().size() > 0) {
            String reportPath = reportsFolder + "/validator/cases/case_" + tenantId.replaceAll(":", "-") + "_" + caseId + ".json";
            GsonWrapper.writeToFile(caseReport, reportPath);
        }

        return caseReport;
    }

    private int processCases(final List<String> caseIds, final int batchSize, final ExecutorService executor) throws IOException {
        ContextValidationReport report = new ContextValidationReport();
        final AtomicInteger numberOfErrors = new AtomicInteger();
        final AtomicInteger processed = new AtomicInteger(0);
        String index = EEsIndex.CASES.value();
        int casesCount = caseIds.size();
        int part = -1;

        Instant indexStart = Instant.now();
        final AtomicReference<Instant> batchStart = new AtomicReference<>(Instant.now());

        logStartValidation(index, casesCount);

        List<Future<Void>> results = new ArrayList<>();

        for (int i = 0; i < casesCount; i++) {

            final String caseId = caseIds.get(i);

            results.add(executor.submit(() -> {

                logger.info("Start validating case with id {}", caseId);
                try {
                    // generate validation report
                    CaseValidationReport caseReport = validateSingleCase(caseId);

                    // propagate the report upward
                    report.swallow(caseReport);

                    // update the total number of errors
                    numberOfErrors.addAndGet(caseReport.getErrors().size());

                    synchronized (processed) {
                        processed.getAndIncrement();

                        if (finishedProcessingBatch(processed.get(), batchSize, casesCount)) {
                            logBatchStats(index, processed.get(), casesCount, batchStart.get(), indexStart);
                            batchStart.set(Instant.now());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }));

        }

        getFutureResults(results);

        logFinishedValidation(index, report);

        writeReport(report, index, part);

        return numberOfErrors.get();
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

    /**
     * Method for validating all the fields contained in a document identified by {@code document}
     *
     * @param document the document that is validated
     * @param parent   the parent of {@code document}
     * @return the document-level report
     */
    private DocumentValidationReport validateSingleDocument(EsDocument document, EsDocument parent) {
        PreconditionsHelper.notNull(document, "document");
        PreconditionsHelper.notNull(parent, "parent");

        // instantiate an aggregator
        Aggregator aggregator = new ValidationAggregator();

        // parse the document and process fields
        List<ValidationResult> results = (List<ValidationResult>) parser.parse(document, parent, aggregator);

        // create the validation report
        DocumentValidationReport documentReport = DocumentValidationReport.forDocument(document, results);

        logger.info(documentReport.toString());

        return documentReport;
    }

    /**
     * Method for validating documents of type CASEMETADATA and CASESTRUCTUREDMETADATA
     *
     * @param caseId the case id
     * @param type   CASEMETADATA or CASESTRUCTUREDMETADATA
     * @return
     */
    private DocumentValidationReport validateCaseDocument(String caseId, EEsType type) throws IOException {
        PreconditionsHelper.notEmpty(caseId, "caseId");
        PreconditionsHelper.notNull(type, "type");

        // the method only handles CASEMETADATA and CASESTRUCTUREDMETADATA
        if (!type.equals(EEsType.CASEMETADATA) && !type.equals(EEsType.CASESTRUCTUREDMETADATA)) {
            return null;
        }

        // there is no case with caseId=0
        if (caseId.equals("0")) {
            return null;
        }

        String index = EEsIndex.CASES.value();

        // get object from elasticsearch
        Map<String, Object> obj = elasticsearchService.get(index, type.value(), caseId);

        // set document
        EsDocument document = new EsDocument(index, type.value(), caseId);
        document.setObject(obj);

        // set parent; same as document
        EsDocument parent = new EsDocument(index, type.value(), caseId);

        // validate the document
        return validateSingleDocument(document, parent);
    }

    private void logStartValidation(String index, long count) {
        System.out.println("Start validating documents in index " + index + ".\n");

        logger.info("Start validating documents in index {}. Documents found: {}.", index, count);
    }

    private void logBatchStats(String index, int batchSize, int indexSize, Instant batchStart, Instant indexStart) {
        Instant now = Instant.now();
        float batchTime = calculateTimeBetween(batchStart, now);
        float indexTime = calculateTimeBetween(indexStart, now);
        float totalTime = calculateTimeBetween(globalStart, now);

        System.out.println(String.format("Index: %s. Processed: %d/%d. Batch time: %.2f. Index time: %.2f. Total time: %.2f.",
                index, batchSize, indexSize, batchTime, indexTime, totalTime));
    }

    private void logFinishedValidation(String index, ContextValidationReport report) {
        System.out.println("\nFinished validating documents in index " + index + ".");
        System.out.println("\n-----------------------------------------------------\n");

        logger.info("Finished validating documents in index {}. Processing time: {}s.", index,
                calculateTimeBetween(globalStart, Instant.now()));
        logger.info(report.toString());
    }

    private void writeReport(ContextValidationReport report, String index, int part) throws IOException {
        String path = reportsFolder + "/validator/";
        if (part == -1) {
            path += index + ".json";
        } else {
            path += index + "_part" + part + ".json";
        }

        GsonWrapper.writeToFile(report, path);
    }

    private boolean finishedProcessingBatch(int processed, int batchSize, int totalSize) {
        return processed % batchSize == 0 || processed == totalSize;
    }

    private float calculateTimeBetween(final Instant start, final Instant end) {
        return Duration.between(start, end).toMillis() / 1000F;
    }
}

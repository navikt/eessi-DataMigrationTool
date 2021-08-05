package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataprocessor.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.elasticsearch.search.SearchHit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import eu.ec.dgempl.eessi.rina.buc.core.model.EDocumentType;
import eu.ec.dgempl.eessi.rina.tool.migration.common.config.RunConfiguration;
import eu.ec.dgempl.eessi.rina.tool.migration.common.model.EsSearchQueryTerm;
import eu.ec.dgempl.eessi.rina.tool.migration.common.service.EsClientService;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataprocessor.DataProcessor;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.datareport.DataReporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.GenericImporterException;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.ReportError;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.DocumentFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.ListSortUtils;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.LogUtils;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.ProgrammaticTransactionUtil;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EsDataProcessor implements DataProcessor {

    private static final Logger logger = LoggerFactory.getLogger(EsDataProcessor.class);

    private final EsClientService esClientService;
    private final PlatformTransactionManager transactionManager;
    private final DataReporter dataReporter;
    private final RunConfiguration runConfiguration;

    private static final List<EElasticType> MULTI_THREAD_IMPORTERS = List.of(
            EElasticType.AUDIT_AUDITENTRY,
            EElasticType.ORGANISATION
    );

    public EsDataProcessor(EsClientService esClientService, PlatformTransactionManager transactionManager, DataReporter dataReporter,
            RunConfiguration runConfiguration) {
        this.esClientService = esClientService;
        this.transactionManager = transactionManager;
        this.dataReporter = dataReporter;
        this.runConfiguration = runConfiguration;
    }

    @Override
    public DocumentsReport process(final EElasticType eElasticType, final Consumer<MapHolder> docProcessor, final boolean transactional)
            throws Exception {

        String index = eElasticType.getIndex();
        String[] types = new String[] { eElasticType.getType() };
        Instant batchStart = Instant.now();
        int threadsNumber = runConfiguration.getThreadsNumber();

        boolean isMultiThreadingEnabled = threadsNumber > 1 && MULTI_THREAD_IMPORTERS.contains(eElasticType);
        String logPrefix = isMultiThreadingEnabled ? " using " + threadsNumber + " threads" : "";
        System.out.println("\nStart processing for index: " + eElasticType.name() + logPrefix);

        long docCount = esClientService.getCount(index, types);

        DocumentsReport documentsReport = new DocumentsReport(eElasticType);
        documentsReport.getTotal().set(docCount);

        ExecutorService executor = Executors.newFixedThreadPool(threadsNumber);
        Consumer<SearchHit[]> hitsConsumer = getHitsConsumer(docProcessor, () -> documentsReport, transactional, eElasticType, executor);

        if (eElasticType == EElasticType.CONFIGURATIONS_ASSIGNMENTPOLICY) {
            esClientService.processAssignmentPoliciesWithoutChildren(hitsConsumer);
            esClientService.processAssignmentPoliciesWithChildren(hitsConsumer);
        } else {
            esClientService.processAll(index, types, hitsConsumer);
        }

        float processTime = Duration.between(batchStart, Instant.now()).toMillis() / 1000F;
        System.out.println("Finished processing for index: " + eElasticType.name() + ", with " + docCount + " documents in " + processTime
                + " seconds");
        System.out.println("\n-----------------------------------------------------");

        executor.shutdown();

        return documentsReport;
    }

    @Override
    public DocumentsReport process(
            final EElasticType eElasticType,
            final Consumer<MapHolder> docProcessor,
            final String caseId,
            final boolean transactional) throws Exception {

        String index = eElasticType.getIndex();
        String[] types = new String[] { eElasticType.getType() };

        DocumentsReport documentsReport = new DocumentsReport(eElasticType);
        documentsReport.getTotal().set(countDocsByCaseId(caseId, eElasticType));

        Consumer<SearchHit[]> hitsConsumer = getHitsConsumer(docProcessor, () -> documentsReport, transactional, eElasticType, null);

        if (eElasticType == EElasticType.CASES_DOCUMENT) {
            esClientService.processDocuments(caseId, hitsConsumer);
        } else {
            EsSearchQueryTerm queryTerm;
            if (eElasticType == EElasticType.CASES_CASESTRUCTUREDMETADATA || eElasticType == EElasticType.CASES_CASEMETADATA) {
                queryTerm = new EsSearchQueryTerm("id", caseId);
            } else {
                queryTerm = new EsSearchQueryTerm("caseId", caseId);
            }
            esClientService.processAllFilterByTerms(index, types, hitsConsumer, queryTerm);
        }

        return documentsReport;
    }

    public long countDocsByCaseId(final String caseId, final EElasticType eElasticType) throws Exception {
        PreconditionsHelper.notEmpty(caseId, "caseId");
        PreconditionsHelper.notNull(eElasticType, "elasticType");

        String index = eElasticType.getIndex();
        String[] types = new String[] { eElasticType.getType() };

        return esClientService.getCountByCaseId(caseId, index, types);
    }

    @NotNull
    private Consumer<SearchHit[]> getHitsConsumer(
            final Consumer<MapHolder> docProcessor,
            final Supplier<DocumentsReport> reportSupplier,
            final boolean transactional,
            final EElasticType eElasticType,
            final ExecutorService executor) {

        String parentDocIdFieldName = getParentDocIdFieldName(eElasticType);

        return getHitsConsumer(docProcessor, reportSupplier, transactional, eElasticType, parentDocIdFieldName, executor);
    }

    @NotNull
    private Consumer<SearchHit[]> getHitsConsumer(
            final Consumer<MapHolder> docProcessor,
            final Supplier<DocumentsReport> reportSupplier,
            final boolean transactional,
            final EElasticType eElasticType,
            final String parentDocIdFieldName,
            final ExecutorService executor) {

        final AtomicLong docsCount = reportSupplier.get().getTotal();
        final AtomicInteger globalCounter = new AtomicInteger(0);
        final AtomicLong processedDocs = new AtomicLong(0);
        final Instant batchStart = Instant.now();

        return (hits) -> {
            final Instant batchTimeStart = Instant.now();

            final List<Future<Void>> results = new ArrayList<>();

            if (StringUtils.isNotBlank(parentDocIdFieldName)) {
                hits = sortHits(hits, parentDocIdFieldName);
            }

            if (eElasticType == EElasticType.CASES_DOCUMENT) {
                // process documents of certain type first
                List<String> types = List.of(EDocumentType.R_017.value());
                hits = reorderHits(hits, types);
            }

            for (SearchHit searchHit : hits) {
                if (MULTI_THREAD_IMPORTERS.contains(eElasticType)) {
                    results.add(executor.submit(() -> {
                        processSearchHit(docProcessor, reportSupplier, transactional, eElasticType, searchHit);
                        return null;
                    }));
                } else {
                    processSearchHit(docProcessor, reportSupplier, transactional, eElasticType, searchHit);
                }
            }

            try {
                for (Future<Void> result : results) {
                    result.get();
                }
            } catch (InterruptedException | ExecutionException ex) {
                logger.info("ERROR getting future results: " + ex.getMessage());
            }

            processedDocs.addAndGet(hits.length);

            if (MULTI_THREAD_IMPORTERS.contains(eElasticType)) {
                LogUtils.printImportProcess(docsCount, globalCounter, processedDocs, batchStart, batchTimeStart, logger);
            }
        };
    }

    private void processSearchHit(
            final Consumer<MapHolder> docProcessor,
            final Supplier<DocumentsReport> reportSupplier,
            final boolean transactional,
            final EElasticType eElasticType,
            final SearchHit searchHit) {

        Map<String, Object> doc = searchHit.getSourceAsMap();
        doc.put("_id", searchHit.getId());
        ConcurrentHashMap<String, Boolean> visitedFields = new ConcurrentHashMap<>();
        visitedFields.put("_id", true);
        MapHolder docHolder = new MapHolder(doc, visitedFields, "");

        if (transactional) {
            try {
                ProgrammaticTransactionUtil.processSuccessfulTransaction(transactionManager, () -> {
                    docProcessor.accept(docHolder);
                    reportSupplier.get().getProcessed().incrementAndGet();
                });
            } catch (Exception e) {
                synchronized (this) {
                    String documentId = docHolder.string("_id");
                    dataReporter.reportProblem(eElasticType, documentId, e.getMessage(), e);
                    List<ReportError> errors = reportSupplier.get().getErrors();
                    if (errors == null) {
                        errors = new ArrayList<>();
                        reportSupplier.get().setErrors(errors);
                    }
                    errors.add(new ReportError(eElasticType.getIndex(), eElasticType.getType(), documentId, null,
                            ExceptionUtils.getStackTrace(e)));
                }
            }
        } else {
            try {
                docProcessor.accept(docHolder);
                reportSupplier.get().getProcessed().incrementAndGet();
            } catch (Exception e) {
                throw new GenericImporterException(e, eElasticType, docHolder.string("_id"), reportSupplier.get());
            }
        }
    }

    private SearchHit[] sortHits(SearchHit[] hits, String parentDocIdFieldName) {
        setDefaultParentIdIfMissing(hits, parentDocIdFieldName);
        List<SearchHit> sortedHits = ListSortUtils.depthFirstTreeSort(
                Arrays.asList(hits),
                Comparator.comparing(hit -> (String) hit.getSourceAsMap().get(parentDocIdFieldName)),
                hit -> (String) hit.getSourceAsMap().get(parentDocIdFieldName),
                hit -> {
                    Map<String, Object> source = hit.getSourceAsMap();
                    if (source.containsKey("id")) {
                        return (String) source.get("id");
                    } else {
                        return hit.getId();
                    }
                }
        );

        return sortedHits.toArray(new SearchHit[] {});
    }

    private SearchHit[] reorderHits(SearchHit[] hits, List<String> types) {
        return Arrays.stream(hits)
                .sorted((o1, o2) -> {
                    String o1Type = (String) o1.getSourceAsMap().get(DocumentFields.TYPE);
                    String o2Type = (String) o2.getSourceAsMap().get(DocumentFields.TYPE);
                    if (!types.contains(o1Type) && !types.contains(o2Type)) {
                        return 0;
                    }
                    if (!types.contains(o1Type)) {
                        return 1;
                    }
                    if (!types.contains(o2Type)) {
                        return -1;
                    }
                    return Integer.compare(types.indexOf(o1Type), types.indexOf(o2Type));
                })
                .collect(Collectors.toList())
                .toArray(new SearchHit[] {});
    }

    private void setDefaultParentIdIfMissing(SearchHit[] hits, String parentDocIdFieldName) {
        for (SearchHit hit : hits) {
            Map<String, Object> source = hit.getSourceAsMap();

            if (source.containsKey(parentDocIdFieldName)) {
                String parentDocId = (String) source.get(parentDocIdFieldName);
                if (StringUtils.isBlank(parentDocId) || parentDocId.equals("null")) {
                    source.put(parentDocIdFieldName, ListSortUtils.ROOT_PARENT_ID);
                }
            } else {
                source.put(parentDocIdFieldName, ListSortUtils.ROOT_PARENT_ID);
            }
        }
    }

    @Nullable
    private String getParentDocIdFieldName(EElasticType eElasticType) {
        String parentDocIdFieldName = null;

        // @formatter:off
        switch (eElasticType) {
            case ACTIVITY:
                parentDocIdFieldName = "repetitionParentId";
                break;
            case GROUP:
                parentDocIdFieldName = "parentGroupId";
                break;
            case CASES_DOCUMENT:
                parentDocIdFieldName = "parentDocumentId";
                break;
            default:
                break;
        }
        // @formatter:on

        return parentDocIdFieldName;
    }

}

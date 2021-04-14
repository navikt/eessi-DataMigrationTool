package eu.ec.dgempl.eessi.rina.tool.migration.exporter.service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.util.IdHelper;
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

    private Instant globalStart = Instant.now();

    @Autowired
    public ValidationService(EsClientService elasticsearchService, ParserService parser, CacheService cacheService) {
        this.elasticsearchService = elasticsearchService;
        this.parser = parser;
        this.cacheService = cacheService;
    }

    /**
     * Method for validating all the resources in the entire Elasticsearch, in all known indices
     * 
     * @throws IOException
     */
    public List<DocumentValidationReport> validateAll() throws IOException {
        globalStart = Instant.now();

        List<DocumentValidationReport> reportsResult = new ArrayList<>();

        reportsResult.addAll(validateAllInIndex(EEsIndex.ADMIN.value()));
        reportsResult.addAll(validateAllInIndex(EEsIndex.AUDIT.value()));
        reportsResult.addAll(validateAllInIndex(EEsIndex.BUSINESSEXCEPTIONS.value()));
        reportsResult.addAll(validateAllInIndex(EEsIndex.CHECKS.value()));
        reportsResult.addAll(validateAllInIndex(EEsIndex.CONFIGURATIONS.value()));
        reportsResult.addAll(validateAllInIndex(EEsIndex.ENTITIES.value()));
        reportsResult.addAll(validateAllInIndex(EEsIndex.GLOBALCONFIGURATIONS.value()));
        reportsResult.addAll(validateAllInIndex(EEsIndex.IDENTITY_V1.value()));
        reportsResult.addAll(validateAllInIndex(EEsIndex.RESOURCES.value()));
        reportsResult.addAll(validateAllInIndex(EEsIndex.VOCABULARIES.value()));
        reportsResult.addAll(validateAllCases());

        System.out.println("Validation has finished.");

        return reportsResult;
    }

    /**
     * Method for validating all the resources found in a specific {@code index}
     * 
     * @param index
     *            the elasticsearch index
     * @throws IOException
     */
    public List<DocumentValidationReport> validateAllInIndex(String index) throws IOException {
        PreconditionsHelper.notEmpty(index, "index");

        // initialize stats variables
        AtomicLong processedCount = new AtomicLong();
        Instant indexStart = Instant.now();

        ContextValidationReport report = new ContextValidationReport();

        // get the list of types in the index
        String[] types = IndexTypeHelper.getTypesByIndex(index).toArray(new String[0]);

        // get the number of documents in the index to be processed
        long totalCount = elasticsearchService.getCount(index, types);

        List<DocumentValidationReport> reportsResult = new ArrayList<>();

        System.out.println("Start validating documents in index " + index + ".\n");

        logger.info("Start validating documents in index {}. Documents found: {}.", index, totalCount);

        if (totalCount == 0) {
            System.out.println(String.format("Index: %s. Documents in index: 0.", index));
        } else {
            // define a processor that will be applied to all the SearchHit results
            Consumer<SearchHit[]> processor = hits -> {
                Instant batchStart = Instant.now();

                for (SearchHit hit : hits) {
                    Map<String, Object> internalObj = hit.getSourceAsMap();
                    EsDocument internalDocument = new EsDocument(hit.getIndex(), hit.getType(), hit.getId());
                    internalDocument.setObject(internalObj);
                    EsDocument internalParent = new EsDocument(hit.getIndex(), hit.getType(), hit.getId());

                    // validate the document
                    DocumentValidationReport internalReport = validateSingleDocument(internalDocument, internalParent);

                    reportsResult.add(internalReport);

                    // aggregate results at the index level
                    report.swallow(internalReport);
                }

                Instant now = Instant.now();
                processedCount.addAndGet(hits.length);
                float batchTime = Duration.between(batchStart, now).toMillis() / 1000F;
                float indexTime = Duration.between(indexStart, now).toMillis() / 1000F;
                float totalTime = Duration.between(globalStart, now).toMillis() / 1000F;

                System.out.println(String.format("Index: %s. Processed: %d/%d. Batch time: %.2f. Index time: %.2f. Total time: %.2f.",
                        index, processedCount.get(), totalCount, batchTime, indexTime, totalTime));
            };

            // query and process results
            elasticsearchService.processAll(index, types, processor);
        }

        System.out.println("\nFinished validating documents in index " + index + ".");
        System.out.println("\n-----------------------------------------------------\n");

        logger.info("Finished validating documents in index {}. Processing time: {}s.", index,
                Duration.between(globalStart, Instant.now()).toMillis() / 1000F);
        logger.info(report.toString());

        GsonWrapper.writeToFile(report, reportsFolder + "/validator/" + index + ".json");

        return reportsResult;
    }

    /**
     * Method for validating all the resources contained in all known cases
     *
     * @throws IOException
     */
    public List<DocumentValidationReport> validateAllCases() throws IOException {
        ContextValidationReport report = new ContextValidationReport();

        Instant indexStart = Instant.now();
        Instant batchStart = Instant.now();

        String index = EEsIndex.CASES.value();

        // get the list of caseIds
        List<String> caseIds = elasticsearchService.getCaseIds();

        // add the special value "0" - documents like X050, SYN002, SYN005 have caseId="0"
        caseIds.add("0");

        // add the special value "tempcaseid"
        // caseIds.add("tempcaseid");

        System.out.println("Start validating documents in index " + index + ".\n");

        List<DocumentValidationReport> reportsResult = new ArrayList<>();

        logger.info("Start validating documents in index {}. Documents found: {}.", index, caseIds.size());

        for (int i = 0; i < caseIds.size(); i++) {
            String caseId = caseIds.get(i);

            // generate validation report
            CaseValidationReport caseReport = validateSingleCase(caseId);

            // propagate the report upward
            report.swallow(caseReport);

            reportsResult.addAll(caseReport.getErrors());

            if (((i + 1) % 50 == 0) || (i + 1 == caseIds.size())) {
                Instant now = Instant.now();
                float batchTime = Duration.between(batchStart, now).toMillis() / 1000F;
                float indexTime = Duration.between(indexStart, now).toMillis() / 1000F;
                float totalTime = Duration.between(globalStart, now).toMillis() / 1000F;

                System.out.println(String.format("Index: %s. Processed: %d/%d. Batch time: %.2f. Index time: %.2f. Total time: %.2f.",
                        index, i + 1, caseIds.size(), batchTime, indexTime, totalTime));

                batchStart = Instant.now();
            }
        }

        System.out.println("\nFinished validating documents in index " + index + ".");
        System.out.println("\n-----------------------------------------------------\n");

        logger.info("Finished validating documents in index {}. Processing time: {}s.", index,
                Duration.between(globalStart, Instant.now()).toMillis() / 1000F);
        logger.info(report.toString());

        GsonWrapper.writeToFile(report, reportsFolder + "/validator/" + index + ".json");

        return reportsResult;
    }

    /**
     * Method for validating all the resources contained in a single case identified by {@code caseId}
     *
     * @param caseId
     *            the id of the case
     * @return the case-level report
     * @throws IOException
     */
    public CaseValidationReport validateSingleCase(String caseId) throws IOException {
        PreconditionsHelper.notEmpty(caseId, "caseId");

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

        // prepare the cache entry context; this will be cases_casemetadata_<caseId>
        String cacheEntryContext = IdHelper.getDocumentReference(EEsIndex.CASES.value(), EEsType.CASEMETADATA.value(), caseId);

        // define a processor that will be applied to all the SearchHit results
        Consumer<SearchHit[]> processor = hits -> {
            Arrays.stream(hits).forEach(hit -> {
                // try to help the cache - add document and subdocument ids (they are searched the most)
                if (hit.getType().equals(EEsType.DOCUMENT.value()) || hit.getType().equals(EEsType.SUBDOCUMENT.value())) {
                    CacheEntry entry = new CacheEntry(true, hit.getIndex(), hit.getType(), hit.getId(), cacheEntryContext);
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
        };

        // process all resources belonging to a case, except for CASEMETADATA and CASESTRUCTUREDMETADATA
        elasticsearchService.processAll(caseId, processor);

        // clear resources from cache
        cacheService.removeEntriesByContext(cacheEntryContext);

        logger.info(caseReport.toString());

        // if there are any errors, write the report on the disk
        if (caseReport.getErrors().size() > 0) {
            String reportPath = reportsFolder + "/validator/cases/case_" + tenantId.replaceAll(":", "-") + "_" + caseId + ".json";
            GsonWrapper.writeToFile(caseReport, reportPath);
        }

        return caseReport;
    }

    /**
     * Method for validating all the fields contained in a document identified by {@code document}
     * 
     * @param document
     *            the document that is validated
     * @param parent
     *            the parent of {@code document}
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
     * @param caseId
     *            the case id
     * @param type
     *            CASEMETADATA or CASESTRUCTUREDMETADATA
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

}

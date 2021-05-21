package eu.ec.dgempl.eessi.rina.tool.migration.common.service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.ec.dgempl.eessi.rina.tool.migration.common.model.EEsIndex;
import eu.ec.dgempl.eessi.rina.tool.migration.common.model.EEsType;
import eu.ec.dgempl.eessi.rina.tool.migration.common.model.EsSearchQueryTerm;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;

/**
 * Service for retrieving data from Elasticsearch
 */
@Service
public class EsClientService {
    private final RestHighLevelClient client;
    private final RestClient lowLevelClient;

    @Value("${elasticsearch.batch.size:1000}")
    private int BATCH_SIZE;

    @Value("${elasticsearch.scroll.keepAlive:5}")
    private long SCROLL_KEEP_ALIVE_MINS;

    @Autowired
    public EsClientService(@Qualifier("highLevelClient") RestHighLevelClient client, RestClient lowLevelClient) {
        this.client = client;
        this.lowLevelClient = lowLevelClient;
    }

    /**
     * Method for getting an elasticsearch document by {@code index}, {@code type} and {@code documentId}
     *
     * @param index
     *            the elasticsearch index
     * @param type
     *            the elasticsearch type
     * @param documentId
     *            the elasticsearch internal id of the document
     * @return Map containing the document
     * @throws IOException
     */
    public Map<String, Object> get(String index, String type, String documentId) throws IOException {
        PreconditionsHelper.notEmpty(index, "index");
        PreconditionsHelper.notEmpty(type, "type");
        PreconditionsHelper.notEmpty(documentId, "documentId");

        GetRequest getRequest = new GetRequest(index, type, documentId);
        GetResponse getResponse = client.get(getRequest);
        return getResponse.getSourceAsMap();
    }

    /**
     * Method for checking if a document exists in elasticsearch
     *
     * @param index
     *            the elasticsearch index
     * @param type
     *            the elasticsearch type
     * @param documentId
     *            the elasticsearch internal id of the document
     * @return Boolean flag indicating whether the document exists in elasticsearch
     * @throws IOException
     */
    public boolean exists(String index, String type, String documentId) throws IOException {
        PreconditionsHelper.notEmpty(index, "index");
        PreconditionsHelper.notEmpty(type, "type");
        PreconditionsHelper.notEmpty(documentId, "documentId");

        GetRequest request = new GetRequest(index, type, documentId).fetchSourceContext(new FetchSourceContext(false));
        GetResponse getResponse = client.get(request);
        return getResponse.isExists();
    }

    /**
     * Method that retrieves the number of documents found in {@code index}, filtered by {@code types}
     *
     * @param index
     *            the elasticsearch index
     * @param types
     *            the elasticsearch type
     * @return the number of documents
     * @throws IOException
     */
    public long getCount(String index, String[] types) throws IOException {
        PreconditionsHelper.notEmpty(index, "index");
        PreconditionsHelper.notNull(types, "types");

        //@formatter:off
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.matchAllQuery())
                .size(0);
        //@formatter:on

        //@formatter:off
        SearchRequest searchRequest = new SearchRequest()
                .indices(index)
                .types(types)
                .source(sourceBuilder);
        //@formatter:on

        SearchResponse response = client.search(searchRequest);

        return response.getHits().getTotalHits();
    }

    /**
     * Method that retrieves the number of documents found in {@code index} by caseId and filtered by {@code types}
     *
     * @param caseId
     *            the case id
     * @param index
     *            the elastic index
     * @param types
     *            the elastic type
     * @return the number of documents
     * @throws IOException
     */
    public long getCountByCaseId(String caseId, String index, String[] types) throws IOException {
        PreconditionsHelper.notEmpty(caseId, "caseId");
        PreconditionsHelper.notNull(index, "index");
        PreconditionsHelper.notNull(types, "types");

        // construct the query
        EsSearchQueryTerm queryTerm;

        if (types.length == 1 && EEsIndex.CASES.value().equals(index)
                && (EEsType.CASESTRUCTUREDMETADATA.value().equals(types[0]) || EEsType.CASEMETADATA.value().equals(types[0]))) {
            queryTerm = new EsSearchQueryTerm("id", caseId);
        } else {
            queryTerm = new EsSearchQueryTerm("caseId", caseId);
        }
        QueryBuilder query = QueryBuilders.boolQuery().must(QueryBuilders.termQuery(queryTerm.getField(), queryTerm.getValue()));

        //@formatter:off
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(query)
                .size(0);
        //@formatter:on

        //@formatter:off
        SearchRequest searchRequest = new SearchRequest()
                .indices(index)
                .types(types)
                .source(sourceBuilder);
        //@formatter:on

        SearchResponse response = client.search(searchRequest);

        return response.getHits().getTotalHits();
    }

    /**
     * Method for extracting the list of case ids from elasticsearch. The ids are extracted from
     * {@link eu.ec.dgempl.eessi.rina.tool.migration.common.model.EEsIndexType#CASES_CASEMETADATA}. The query that extracts the ids does not
     * fetch the source of the documents
     *
     * @return the list of case ids
     * @throws IOException
     */
    public List<String> getCaseIds() throws IOException {
        List<String> results = new ArrayList<>();

        //@formatter:off
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.matchAllQuery())
                .fetchSource(false) // do not fetch the source 
                .size(BATCH_SIZE);
        //@formatter:on

        // create a consumer that extracts the id from SearchHit objects
        Consumer<SearchHit[]> processor = hits -> {
            Arrays.stream(hits).forEach(hit -> results.add(hit.getId()));
        };

        String[] types = new String[] { EEsType.CASEMETADATA.value() };

        // process all documents from cases_casemetadata using the consumer defined above
        processAllByQuery(EEsIndex.CASES.value(), types, sourceBuilder, processor, false);

        return results;
    }

    /**
     * Method for fetching the tenant id that is responsible for the case identified by {@code caseId}
     *
     * @param caseId
     *            the case id
     * @return
     * @throws IOException
     */
    public Map<String, Object> getWhoami(String caseId) throws IOException {
        PreconditionsHelper.notEmpty(caseId, "caseId");

        String[] includes = new String[] { "whoami.id" };
        String[] excludes = Strings.EMPTY_ARRAY;
        FetchSourceContext fetchSourceContext = new FetchSourceContext(true, includes, excludes);

        GetRequest request = new GetRequest(EEsIndex.CASES.value(), EEsType.CASESTRUCTUREDMETADATA.value(), caseId)
                .fetchSourceContext(fetchSourceContext);

        return client.get(request).getSourceAsMap();
    }

    public Map<String, Map<String, Integer>> getOrphanResources(String index, String[] types) throws IOException {
        PreconditionsHelper.notEmpty(index, "index");
        PreconditionsHelper.notNull(types, "types");

        Map<String, Map<String, Integer>> results = new HashMap<>();

        //@formatter:off
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.boolQuery()
                        .mustNot(QueryBuilders.termQuery("_type", EEsType.CASEMETADATA.value()))
                )
                .fetchSource(new String[] { "caseId", "id" }, Strings.EMPTY_ARRAY) 
                .size(BATCH_SIZE);
        //@formatter:on

        Consumer<SearchHit[]> processor = hits -> {
            //@formatter:off
            List<String> caseIds = Arrays.stream(hits)
                    .map(EsClientService::extractCaseId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            //@formatter:on

            List<String> missingCaseIds = null;
            try {
                missingCaseIds = getMissingCaseIds(caseIds);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            List<String> finalMissingCaseIds = missingCaseIds;

            //@formatter:off
            Arrays.stream(hits).forEach(hit -> {
                String caseId = extractCaseId(hit);
                
                if (caseId == null || finalMissingCaseIds.contains(caseId)) {
                    //hashmap allows null keys
                    if (results.get(caseId) == null) {
                        results.put(caseId, new HashMap<>());
                    }
                    Map<String, Integer> mapTypeToNumber = results.get(caseId);
                    Integer no = mapTypeToNumber.get(hit.getType());
                    if (no == null) {
                        mapTypeToNumber.put(hit.getType(), 1);
                    } else {
                        mapTypeToNumber.put(hit.getType(), no + 1);
                    }
                }
            });
            //@formatter:on
        };

        processAllByQuery(index, types, sourceBuilder, processor, false);

        return results;
    }

    private List<String> getMissingCaseIds(List<String> caseIds) throws IOException {
        //@formatter:off
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders
                        .boolQuery()
                        .must(QueryBuilders.termsQuery("id", caseIds))
                )
                .fetchSource(false)
                .size(BATCH_SIZE);
        //@formatter:on

        //@formatter:off
        SearchRequest searchRequest = new SearchRequest()
                .indices(EEsIndex.CASES.value())
                .types(EEsType.CASEMETADATA.value())
                .source(sourceBuilder);
        //@formatter:on

        SearchResponse response = client.search(searchRequest);

        //@formatter:off
        List<String> found = Arrays.stream(response.getHits().getHits())
                .map(SearchHit::getId)
                .collect(Collectors.toList());
        //@formatter:on

        //@formatter:off
        return caseIds.stream()
                .filter(caseId -> !caseId.equals("0") && !found.contains(caseId))
                .collect(Collectors.toList());
        //@formatter:on
    }

    private static String extractCaseId(SearchHit hit) {
        PreconditionsHelper.notNull(hit, "hit");

        String fieldName;
        if (hit.getType().equalsIgnoreCase(EEsType.CASEMETADATA.value())
                || hit.getType().equalsIgnoreCase(EEsType.CASESTRUCTUREDMETADATA.value())) {
            fieldName = "id";
        } else {
            fieldName = "caseId";
        }
        return (String) hit.getSourceAsMap().get(fieldName);
    }

    /**
     * Method for processing all documents found in a specific {@code index} and all {@code types}. The processing method is injected by the
     * caller
     *
     * @param index
     *            the elasticsearch index
     * @param types
     *            the list of elasticsearch types
     * @param processor
     *            the processing method that will be called on all the query results
     * @throws IOException
     */
    public void processAll(String index, String[] types, Consumer<SearchHit[]> processor) throws IOException {
        PreconditionsHelper.notEmpty(index, "index");
        PreconditionsHelper.notNull(types, "types");
        PreconditionsHelper.notNull(processor, "processor");

        //@formatter:off
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.matchAllQuery())
                .size(BATCH_SIZE);
        //@formatter:on

        processAllByQuery(index, types, sourceBuilder, processor, false);
    }

    /**
     * Method for processing all documents belonging to a specific case identified by {@code caseId}. This method does not handle
     * CASEMETADATA and CASESTRUCTUREDMETADATA objects
     *
     * @param caseId
     *            the case id
     * @param processor
     *            the processing method that will be called on all the query results
     */
    public void processAll(String caseId, Consumer<SearchHit[]> processor) throws IOException {
        PreconditionsHelper.notEmpty(caseId, "caseId");
        PreconditionsHelper.notNull(processor, "processor");

        // create a query term filter for fetching all resources with a specific caseId
        EsSearchQueryTerm queryTerm = new EsSearchQueryTerm("caseId", caseId);

        // create the list of all types in index CASES that contain case-related resources
        // ignore from the list CASES_CASEMETADATA and CASES_CASESTRUCTUREDMETADATA
        // this list is in a specific order which helps maximize the cache hits and minimize the number of queries to Elasticsearch
        // @formatter:off
        String[] caseTypes = new String[] {
                EEsType.DOCUMENT.value(),
                EEsType.SUBDOCUMENT.value(),
                EEsType.ATTACHMENT.value(),
                EEsType.ATTACHMENTCONTENT.value(),
                EEsType.COMMENT.value(),
                EEsType.DOCUMENTCONTENT.value(),
                EEsType.SIGNATURE.value(),
                EEsType.TASKMETADATA.value(),
                EEsType.USERMESSAGEHEADER.value()
        };
        // @formatter:on

        // query and process results
        processAllFilterByTerms(EEsIndex.CASES.value(), caseTypes, processor, queryTerm);

        // create the list of all types in index NOTIFICATIONS that contain case-related resources
        // @formatter:off
        String[] notificationTypes = new String[] {
                EEsType.ALARM.value(),
                EEsType.NOTIFICATION.value()
        };
        // @formatter:on

        // query and process notification results
        processAllFilterByTerms(EEsIndex.NOTIFICATIONS.value(), notificationTypes, processor, queryTerm);
    }

    /**
     * Method for processing elasticsearch documents that have a certain type (i.e. R017)
     *
     * @param caseId
     *            the case id
     * @param processor
     *            the processing method that will be called on all the query results
     * @param documentType
     *            the type of the documents that need to be processed
     * @throws IOException
     */
    public void processDocumentsWithType(String caseId, Consumer<SearchHit[]> processor, String documentType) throws IOException {
        PreconditionsHelper.notEmpty(caseId, "caseId");
        PreconditionsHelper.notNull(processor, "processor");

        //@formatter:off
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("caseId", caseId))
                        .must(QueryBuilders.termQuery("type", documentType.toLowerCase()))
                )
                .size(BATCH_SIZE);
        //@formatter:on

        String[] types = new String[] { EEsType.DOCUMENT.value() };

        processAllByQuery(EEsIndex.CASES.value(), types, sourceBuilder, processor, false);
    }

    /**
     * Method for processing elasticsearch documents that have a different type than the one provided (i.e. R017)
     *
     * @param caseId
     *            the case id
     * @param processor
     *            the processing method that will be called on all the query results
     * @param documentType
     *            the type of the documents that need to be skipped from processing
     * @throws IOException
     */
    public void processDocumentsWithTypeNot(String caseId, Consumer<SearchHit[]> processor, String documentType) throws IOException {
        PreconditionsHelper.notEmpty(caseId, "caseId");
        PreconditionsHelper.notNull(processor, "processor");

        //@formatter:off
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("caseId", caseId))
                        .mustNot(QueryBuilders.termQuery("type", documentType.toLowerCase())))
                .size(BATCH_SIZE);
        //@formatter:on

        String[] types = new String[] { EEsType.DOCUMENT.value() };

        processAllByQuery(EEsIndex.CASES.value(), types, sourceBuilder, processor, false);
    }

    /**
     * Method for processing BUC assignment_policies that don't have child policies
     *
     * @param processor
     *            the processing method that will be called on all the query results
     * @throws IOException
     */
    public void processAssignmentPoliciesWithoutChildren(Consumer<SearchHit[]> processor) throws IOException {
        PreconditionsHelper.notNull(processor, "processor");

        //@formatter:off
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.boolQuery()
                        .mustNot(QueryBuilders.existsQuery("policies")))
                .size(BATCH_SIZE);
        //@formatter:on

        String[] types = new String[] { EEsType.ASSIGNMENTPOLICY.value() };

        processAllByQuery(EEsIndex.CONFIGURATIONS.value(), types, sourceBuilder, processor, false);
    }

    /**
     * Method for processing BUC assignment_policies that have child policies
     *
     * @param processor
     *            the processing method that will be called on all the query results
     * @throws IOException
     */
    public void processAssignmentPoliciesWithChildren(Consumer<SearchHit[]> processor) throws IOException {
        PreconditionsHelper.notNull(processor, "processor");

        //@formatter:off
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.existsQuery("policies")))
                .size(BATCH_SIZE);
        //@formatter:on

        String[] types = new String[] { EEsType.ASSIGNMENTPOLICY.value() };

        processAllByQuery(EEsIndex.CONFIGURATIONS.value(), types, sourceBuilder, processor, false);
    }

    /**
     * Method for processing all documents found in a specific {@code index} and all {@code types} and filtered by a term query composed
     * form {@code terms}. The processing method is injected by the caller.
     *
     * @param index
     *            the elasticsearch index
     * @param types
     *            the list of elasticsearch types
     * @param processor
     *            the processing method that will be called on all the query results
     * @param terms
     *            the list of query terms to be applied for filtering the elasticsearch results
     * @throws IOException
     */
    public void processAllFilterByTerms(String index, String[] types, Consumer<SearchHit[]> processor, EsSearchQueryTerm... terms)
            throws IOException {
        PreconditionsHelper.notEmpty(index, "index");
        PreconditionsHelper.notNull(types, "types");
        PreconditionsHelper.notNull(processor, "processor");

        // construct the query
        QueryBuilder query;
        if (terms == null || terms.length == 0) {
            query = QueryBuilders.matchAllQuery();
        } else {
            query = QueryBuilders.boolQuery();
            for (EsSearchQueryTerm term : terms) {
                ((BoolQueryBuilder) query).must(QueryBuilders.termQuery(term.getField(), term.getValue()));
            }
        }

        //@formatter:off
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(query)
                .size(BATCH_SIZE);
        //@formatter:on

        processAllByQuery(index, types, sourceBuilder, processor, false);
    }

    public void processAllFilterByTermsArray(String index, String[] types, Consumer<SearchHit[]> processor, String field, String[] values)
            throws IOException {
        PreconditionsHelper.notEmpty(index, "index");
        PreconditionsHelper.notNull(types, "types");
        PreconditionsHelper.notNull(processor, "processor");

        // construct the query
        QueryBuilder query = QueryBuilders.boolQuery();
        ((BoolQueryBuilder) query).must(QueryBuilders.termsQuery(field, values));

        //@formatter:off
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(query)
                .size(BATCH_SIZE);
        //@formatter:on

        processAllByQuery(index, types, sourceBuilder, processor, false);
    }

    /**
     * Method for processing all documents found in a specific {@code index} and all {@code types}. A {@code sourceBuilder} must be provided
     * for filtering the elasticsearch results. The processing method is injected by the caller.
     *
     * @param index
     *            the elasticsearch index
     * @param types
     *            the list of elasticsearch types
     * @param sourceBuilder
     *            a SourceBuilder instance to be injected into the query
     * @param processor
     *            the processing method that will be called on all the query results
     * @param withStats
     *            boolean flag that enables time stats
     * @throws IOException
     */
    private void processAllByQuery(String index, String[] types, SearchSourceBuilder sourceBuilder, Consumer<SearchHit[]> processor,
            boolean withStats) throws IOException {
        PreconditionsHelper.notEmpty(index, "index");
        PreconditionsHelper.notNull(types, "types");
        PreconditionsHelper.notNull(sourceBuilder, "sourceBuilder");
        PreconditionsHelper.notNull(processor, "processor");

        Instant globalStart = null;
        Instant globalEnd;
        float globalTime;

        Instant fetchStart = null;
        Instant fetchEnd;
        float fetchTime;

        Instant processingStart = null;
        Instant processingEnd;
        float processingTime;

        int totalCount = 0;
        float totalFetchTime = 0;
        float totalProcessingTime = 0;

        if (withStats) {
            System.out.println(String.format("Processing index: %s, types: %s", index, String.join(",", types)));
            globalStart = Instant.now();
            fetchStart = Instant.now();
        }

        // instantiate the scroll
        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(SCROLL_KEEP_ALIVE_MINS));

        //@formatter:off
        SearchRequest searchRequest = new SearchRequest()
                .indices(index)
                .types(types)
                .source(sourceBuilder)
                .scroll(scroll);
        //@formatter:on

        SearchResponse response = client.search(searchRequest);
        String scrollId = response.getScrollId();
        SearchHit[] hits = response.getHits().getHits();

        if (withStats) {
            fetchEnd = Instant.now();
            fetchTime = Duration.between(fetchStart, fetchEnd).toMillis() / 1000F;
            totalFetchTime += fetchTime;
            totalCount += hits.length;

            System.out.print(String.format("Documents: %d. Fetch time: %.2f. Fetch speed: %.2f", totalCount, fetchTime,
                    hits.length / (fetchTime * 100)));
        }

        while (hits != null && hits.length > 0) {
            if (withStats) {
                processingStart = Instant.now();
            }

            // process results
            processor.accept(hits);

            if (withStats) {
                processingEnd = Instant.now();
                processingTime = Duration.between(processingStart, processingEnd).toMillis() / 1000F;
                totalProcessingTime += processingTime;

                System.out.println(String.format(". Processing time: %.2f. Processing speed: %.2f", processingTime,
                        hits.length / (processingTime * 100)));

                fetchStart = Instant.now();
            }

            // fetch another batch
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
            response = client.searchScroll(scrollRequest);
            scrollId = response.getScrollId();
            hits = response.getHits().getHits();

            if (withStats && hits != null && hits.length > 0) {
                fetchEnd = Instant.now();
                fetchTime = Duration.between(fetchStart, fetchEnd).toMillis() / 1000F;
                totalFetchTime += fetchTime;
                totalCount += hits.length;

                System.out.println(String.format("Documents: %d. Fetch time: %.2f. Fetch speed: %.2f", totalCount, fetchTime,
                        hits.length / (fetchTime * 100)));
            }
        }

        if (withStats) {
            globalEnd = Instant.now();
            globalTime = Duration.between(globalStart, globalEnd).toMillis() / 1000F;

            System.out.println(String.format(
                    "Documents: %d. Total time: %.2f. Total fetch time: %.2f. Average fetch speed: %.2f. Total processing time: %.2f. Average processing speed: %.2f",
                    totalCount, globalTime, totalFetchTime, totalCount / (totalFetchTime * 100), totalProcessingTime,
                    totalCount / (totalProcessingTime * 100)));
        }

        // close the scroll when finished fetching all documents
        clearScroll(scrollId);
    }

    public BulkResponse deleteBulk(EEsIndex eesIndex, EEsType eesType, List<String> ids) throws IOException {
        // bulk-delete the ids
        if (ids == null || ids.size() == 0) return null;
        BulkRequest bulkRequest = new BulkRequest();
        for (String id : ids) {
            bulkRequest.add(new DeleteRequest().id(id).index(eesIndex.value()).type(eesType.value()));
        }
        return client.bulk(bulkRequest);
    }

    public BulkResponse insertBulk(EEsIndex eesIndex, EEsType eesType, Map<String, Object> documents) throws IOException {
        // bulk-delete the ids
        if (documents == null || documents.size() == 0) return null;
        BulkRequest bulkRequest = new BulkRequest();
        for (String id : documents.keySet()) {
            bulkRequest.add(new IndexRequest().id(id).index(eesIndex.value()).type(eesType.value()).source((String)documents.get(id)));
        }
        return client.bulk(bulkRequest);
    }

    /**
     * Method for closing an elasticsearch scroll
     *
     * @param scrollId
     *            the id of the elasticsearch scroll
     * @return
     * @throws IOException
     */
    private boolean clearScroll(String scrollId) throws IOException {
        PreconditionsHelper.notEmpty(scrollId, "scrollId");

        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        return client.clearScroll(clearScrollRequest).isSucceeded();
    }

    /**
     * Method for closing the elasticsearch client connection
     *
     * @throws IOException
     */
    public void close() throws IOException {
        lowLevelClient.close();
    }
}

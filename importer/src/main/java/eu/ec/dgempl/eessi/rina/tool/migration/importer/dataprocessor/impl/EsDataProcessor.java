package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataprocessor.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.elasticsearch.search.SearchHit;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import eu.ec.dgempl.eessi.rina.tool.migration.common.model.EsSearchQueryTerm;
import eu.ec.dgempl.eessi.rina.tool.migration.common.service.EsClientService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataprocessor.DataProcessor;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.datareport.DataReporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.FieldMappingService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.ProgrammaticTransactionUtil;

@Component
public class EsDataProcessor implements DataProcessor {

    @Autowired
    private EsClientService esClientService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private DataReporter dataReporter;

    @Autowired
    private FieldMappingService fieldMappingService;

    @Override
    public void process(final EElasticType eElasticType, final Consumer<MapHolder> docProcessor, final boolean transactional)
            throws Exception {

        String index = eElasticType.getIndex();
        String[] types = new String[] { eElasticType.getType() };
        Instant batchStart = Instant.now();

        System.out.println("\nStart processing for index: " + eElasticType.name());

        long docCount = esClientService.getCount(index, types);

        Consumer<SearchHit[]> hitsConsumer = getHitsConsumer(docProcessor, transactional, eElasticType);

        if (eElasticType == EElasticType.CONFIGURATIONS_ASSIGNMENTPOLICY) {
            esClientService.processAssignmentPoliciesWithoutChildren(hitsConsumer);
            esClientService.processAssignmentPoliciesWithChildren(hitsConsumer);
        } else {
            if (eElasticType == EElasticType.GROUP) {
                esClientService.processGroupsWithoutParent(hitsConsumer);
                esClientService.processGroupsWithParent(hitsConsumer);
            } else {
                esClientService.processAll(index, types, hitsConsumer);
            }
        }
        float processTime = Duration.between(batchStart, Instant.now()).toMillis() / 1000F;
        System.out.println("Finished processing for index: " + eElasticType.name() + ", with " + docCount + " documents in " + processTime
                + " seconds");
        System.out.println("\n-----------------------------------------------------");
    }

    @Override
    public void process(final EElasticType eElasticType, final Consumer<MapHolder> docProcessor, final String caseId,
            final boolean transactional) throws Exception {
        String index = eElasticType.getIndex();
        String[] types = new String[] { eElasticType.getType() };

        long docCount = esClientService.getCount(index, types);

        Consumer<SearchHit[]> hitsConsumer = getHitsConsumer(docProcessor, transactional, eElasticType);
        if (eElasticType == EElasticType.CASES_DOCUMENT) {
            esClientService.processDocumentsWithoutParent(caseId, hitsConsumer);
            esClientService.processDocumentsWithParent(caseId, hitsConsumer);
        } else {
            EsSearchQueryTerm queryTerm;
            if (eElasticType == EElasticType.CASES_CASESTRUCTUREDMETADATA || eElasticType == EElasticType.CASES_CASEMETADATA) {
                queryTerm = new EsSearchQueryTerm("id", caseId);
            } else {
                queryTerm = new EsSearchQueryTerm("caseId", caseId);
            }
            esClientService.processAllFilterByTerms(index, types, hitsConsumer, queryTerm);
        }
    }

    @NotNull
    private Consumer<SearchHit[]> getHitsConsumer(final Consumer<MapHolder> docProcessor, final boolean transactional,
            final EElasticType eElasticType) {

        Consumer<SearchHit[]> searchHits = (hits) -> {
            // List<String> unreviewedResult = new ArrayList<>();
            for (SearchHit searchHit : hits) {

                Map<String, Object> doc = searchHit.getSourceAsMap();
                doc.put("_id", searchHit.getId());

                ConcurrentHashMap<String, Boolean> visitedFields = new ConcurrentHashMap<>();
                visitedFields.put("_id", true);

                MapHolder docHolder = new MapHolder(doc, visitedFields, "");
                if (transactional) {
                    try {
                        ProgrammaticTransactionUtil.processSuccessfulTransaction(transactionManager, () -> docProcessor.accept(docHolder));

                    } catch (Exception e) {
                        dataReporter.reportProblem(eElasticType, docHolder.string("_id"), e.getMessage(), e);
                    }
                } else {
                    docProcessor.accept(docHolder);
                }
                // unreviewedResult.addAll(
                // new ArrayList<>(fieldMappingService.checkUnreviewedFields(docHolder, eElasticType, docHolder.getVisitedFields())));

            }
            // if (!unreviewedResult.isEmpty()) {
            // unreviewedResult = unreviewedResult.stream().distinct().collect(Collectors.toList());
            // fieldMappingService.writeOutputFiles(eElasticType, unreviewedResult);
            // System.out.println("created file with unreviewed fields for elastic type: " + eElasticType);
            // }
        };

        return searchHits;
    }
}

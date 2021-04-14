package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es.cases;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils.*;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentBversion;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Subdocument;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.SubdocumentBversion;
import eu.ec.dgempl.eessi.rina.repo.SubdocumentBversionRepo;
import eu.ec.dgempl.eessi.rina.repo.SubdocumentRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.CaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

import ma.glasnost.orika.MappingContext;

@Component
@ElasticTypeImporter(type = EElasticType.CASES_SUBDOCUMENT)
public class SubdocumentImporter extends AbstractDataImporter implements CaseImporter {

    private final SubdocumentRepo subdocumentRepo;
    private final SubdocumentBversionRepo subdocumentBversionRepo;

    public SubdocumentImporter(final SubdocumentRepo subdocumentRepo,
            final SubdocumentBversionRepo subdocumentBversionRepo) {
        this.subdocumentRepo = subdocumentRepo;
        this.subdocumentBversionRepo = subdocumentBversionRepo;
    }

    @Override
    public void importData(final String caseId) {
        run(this::processSubdocumentData, caseId);
    }

    private void processSubdocumentData(final MapHolder doc) {
        Long no = doc.asLong("no");
        String caseId = doc.string("caseId");
        List<MapHolder> subdocumentMapHolders = doc.listToMapHolder("subdocuments");

        if (CollectionUtils.isNotEmpty(subdocumentMapHolders)) {
            MappingContext mappingContext = mctxb()
                    .addProp("caseId", caseId)
                    .addProp("no", no)
                    .build();

            List<Subdocument> subdocuments = subdocumentMapHolders.stream()
                    .map(subdocument -> beanMapper.map(subdocument, Subdocument.class, mappingContext))
                    .collect(Collectors.toList());

            saveInBulk(() -> subdocuments, () -> subdocumentRepo);

            mapAndSaveSubdocumentBversions(subdocuments);

            mapAndSaveSubdocumentActiveBversion(subdocuments);
        }
    }

    private void mapAndSaveSubdocumentActiveBversion(final List<Subdocument> subdocuments) {
        saveInBulk(
                () -> subdocuments.stream()
                        .peek(subdocument -> {

                            Optional<SubdocumentBversion> lastSubdocumetBversion =
                                    subdocument.getSubdocumentBversions().stream()
                                            .max(Comparator.comparing(SubdocumentBversion::getId));
                            lastSubdocumetBversion.ifPresent(subdocument::setSubdocumentBversion);

                        }).collect(Collectors.toList()),
                () -> subdocumentRepo);
    }

    private void mapAndSaveSubdocumentBversions(final List<Subdocument> subdocuments) {
        List<SubdocumentBversion> bversions = subdocuments
                .stream()
                .peek(subdocument -> {
                    Document document = subdocument.getDocument();
                    List<DocumentBversion> documentBversions = document.getDocumentBversions().stream().sorted(
                            Comparator.comparing(DocumentBversion::getId))
                            .collect(Collectors.toList());

                    List<SubdocumentBversion> subdocumentBversions = subdocument.getSubdocumentBversions().stream().sorted(
                            Comparator.comparing(SubdocumentBversion::getId))
                            .collect(Collectors.toList());

                    Map<Pair<ZonedDateTime, ZonedDateTime>, Integer> intervalPairs =
                            getIntervalsMap(documentBversions);

                    subdocumentBversions.forEach(
                            subdocumentBversion -> {
                                int intervalIndex = getIntervalIndex(intervalPairs, subdocumentBversion.getAudit().getCreatedAt());
                                if (intervalIndex > -1) {
                                    for (int idx = intervalIndex; idx < documentBversions.size(); idx++) {
                                        documentBversions.get(idx).addSubdocumentBversion(subdocumentBversion);
                                    }
                                }
                            }
                    );
                })
                .map(Subdocument::getSubdocumentBversions)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        saveInBulk(() -> bversions, () -> subdocumentBversionRepo);
    }

    @NotNull
    private Map<Pair<ZonedDateTime, ZonedDateTime>, Integer> getIntervalsMap(final List<DocumentBversion> documentBversions) {
        Map<Pair<ZonedDateTime, ZonedDateTime>, Integer> intervalPairs = new HashMap<>();
        for (int i = 0; i < documentBversions.size(); i++) {
            DocumentBversion first = documentBversions.get(i);

            Pair<ZonedDateTime, ZonedDateTime> pair;

            if (i == documentBversions.size() - 1) {
                pair = Pair.of(first.getAudit().getCreatedAt(), ZonedDateTime.now());
            } else {
                DocumentBversion second = documentBversions.get(i + 1);
                pair = Pair.of(first.getAudit().getCreatedAt(), second.getAudit().getCreatedAt());
            }
            intervalPairs.put(pair, i);
        }
        return intervalPairs;
    }

    private int getIntervalIndex(Map<Pair<ZonedDateTime, ZonedDateTime>, Integer> intervals, ZonedDateTime creationDate) {
        Optional<Integer> first = intervals.entrySet()
                .stream()
                .filter(pairIntegerEntry -> {
                    Pair<ZonedDateTime, ZonedDateTime> pair = pairIntegerEntry.getKey();
                    return creationDate.isAfter(pair.getLeft()) && creationDate.isBefore(pair.getRight());
                })
                .map(Map.Entry::getValue)
                .findFirst();

        return first.orElse(-1);
    }

}

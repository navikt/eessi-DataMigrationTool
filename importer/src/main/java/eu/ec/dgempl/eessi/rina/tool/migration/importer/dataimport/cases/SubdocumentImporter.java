package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.cases;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.DateUtils.*;
import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.MappingContextBuilder.*;
import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.commons.date.ZonedDateTimePeriod;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentBversion;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Subdocument;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.SubdocumentBversion;
import eu.ec.dgempl.eessi.rina.repo.SubdocumentBversionRepo;
import eu.ec.dgempl.eessi.rina.repo.SubdocumentRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.CaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;

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
    public DocumentsReport importData(final String caseId) {
        return run(this::processSubdocumentData, caseId);
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

                    Map<ZonedDateTimePeriod, Integer> intervalPairs = getIntervalsMap(documentBversions);

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
}

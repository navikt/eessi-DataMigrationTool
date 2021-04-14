package eu.ec.dgempl.eessi.rina.tool.migration.importer.utils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;

import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.CaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.DataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;

public class ImporterUtils {

    @NotNull
    public static List<DataImporter> getDataImporters(
            final Map<String, Object> annotatedClasses,
            final Predicate<Object> importerPredicate) {

        Optional<Object> nonValidDataImporter = annotatedClasses.values().stream()
                .filter(importer -> !DataImporter.class.isAssignableFrom(importer.getClass()))
                .findFirst();

        if (nonValidDataImporter.isPresent()) {
            throw new IllegalStateException("Found annotated instance but not implementing the interface 'DataImporter'");
        }

        return annotatedClasses.values().stream()
                .filter(importerPredicate)
                .filter(o -> !CaseImporter.class.isAssignableFrom(o.getClass()))
                .map(DataImporter.class::cast)
                .sorted((o1, o2) -> {
                    ElasticTypeImporter importType1 = o1.getClass().getDeclaredAnnotation(ElasticTypeImporter.class);
                    ElasticTypeImporter importType2 = o2.getClass().getDeclaredAnnotation(ElasticTypeImporter.class);

                    if (importType1 == null || importType2 == null) {
                        throw new IllegalStateException("Importers collection contains instances without ElasticTypeImporter annotation.");
                    }

                    int order1 = importType1.order() != 0 ? importType1.order() : importType1.type().getOrder();
                    int order2 = importType2.order() != 0 ? importType2.order() : importType2.type().getOrder();
                    return Integer.compare(order1, order2);
                })
                .collect(Collectors.toList());
    }

    public static List<CaseImporter> getCaseImporters(ApplicationContext applicationContext) {
        Map<String, CaseImporter> annotatedClasses = applicationContext.getBeansOfType(CaseImporter.class);
        return annotatedClasses.values().stream()
                .sorted((o1, o2) -> {
                    ElasticTypeImporter importType1 = o1.getClass().getDeclaredAnnotation(ElasticTypeImporter.class);
                    ElasticTypeImporter importType2 = o2.getClass().getDeclaredAnnotation(ElasticTypeImporter.class);

                    if (importType1 == null || importType2 == null) {
                        throw new IllegalStateException("Importers collection contains instances without ElasticTypeImporter annotation.");
                    }

                    int order1 = importType1.order() != 0 ? importType1.order() : importType1.type().getOrder();
                    int order2 = importType2.order() != 0 ? importType2.order() : importType2.type().getOrder();
                    return Integer.compare(order1, order2);
                })
                .collect(Collectors.toList());
    }

}

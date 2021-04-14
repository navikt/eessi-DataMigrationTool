package eu.ec.dgempl.eessi.rina.tool.migration.importer.utils;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.CaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.DataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.PostCaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.PreCaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.IndexReport;

public class ImporterUtils {

    public static List<PreCaseImporter> getPreCaseImporters(
            final ApplicationContext applicationContext,
            final Predicate<PreCaseImporter> predicate) {
        return getImporters(applicationContext, PreCaseImporter.class, predicate);
    }

    public static List<PostCaseImporter> getPostCaseImporters(
            final ApplicationContext applicationContext,
            final Predicate<PostCaseImporter> predicate) {
        return getImporters(applicationContext, PostCaseImporter.class, predicate);
    }

    public static List<CaseImporter> getCaseImporters(ApplicationContext applicationContext) {
        return getImporters(applicationContext, CaseImporter.class, caseImporter -> true);
    }

    private static <T extends DataImporter> List<T> getImporters(
            final ApplicationContext applicationContext,
            final Class<T> tClass,
            final Predicate<T> predicate) {

        Map<String, T> annotatedClasses = applicationContext.getBeansOfType(tClass);

        return annotatedClasses.values().stream()
                .filter(predicate)
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

    public static IndexReport runImporter(final DataImporter importer, final Logger logger, final RinaJsonMapper rinaJsonMapper) {
        EElasticType eElasticType = importer.inferElasticType();
        logger.info("Started import for index: [{}] and type: [{}] ", eElasticType.getIndex(), eElasticType.getType());

        IndexReport indexReport = new IndexReport();

        DocumentsReport documentsReport = importer instanceof PreCaseImporter ?
                ((PreCaseImporter) importer).importData() :
                ((PostCaseImporter) importer).importData();

        indexReport.swallow(documentsReport);

        try {
            logger.info("Imported: " + rinaJsonMapper.objToJson(indexReport));
        } catch (JsonProcessingException e) {
            logger.error("Could not log documentsReport", e);
        }

        return indexReport;
    }
}

package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataprocessor.DataProcessor;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.datareport.DataReporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.BeanMapper;

public abstract class AbstractDataImporter {

    protected BeanMapper beanMapper;
    protected DataProcessor dataProcessor;
    protected DataReporter dataReporter;
    protected RinaJsonMapper rinaJsonMapper;
    protected PlatformTransactionManager transactionManager;

    protected DocumentsReport run(Consumer<MapHolder> itemProcessor) {
        return run(itemProcessor, true);
    }

    protected DocumentsReport run(Consumer<MapHolder> itemProcessor, String caseId) {
        return run(itemProcessor, caseId, false);
    }

    protected DocumentsReport run(Consumer<MapHolder> itemProcessor, boolean transactional) {
        EElasticType elasticType = inferElasticType();

        try {
            return dataProcessor.process(elasticType, itemProcessor, transactional);
        } catch (Exception e) {
            throw new RuntimeException("Could not process document for " + elasticType.name(), e);
        }
    }

    protected DocumentsReport run(Consumer<MapHolder> itemProcessor, String caseId, boolean transactional) {
        EElasticType elasticType = inferElasticType();

        try {
            return dataProcessor.process(elasticType, itemProcessor, caseId, transactional);
        } catch (Exception e) {
            throw new RuntimeException("Could not process document for " + elasticType.name(), e);
        }
    }

    public long countDocsByCaseId(String caseId) {
        EElasticType elasticType = inferElasticType();

        if (!EElasticType.NONE.equals(elasticType)) {
            try {
                return dataProcessor.countDocsByCaseId(caseId, elasticType);
            } catch (Exception e) {
                throw new RuntimeException("Could not process document for " + elasticType.name(), e);
            }
        }

        return -1;
    }

    /**
     * Obtains the elastic type from the annotation of the class.
     *
     * @return EElasticType
     */
    public EElasticType inferElasticType() {

        ElasticTypeImporter annotatedElasticType = this.getClass().getAnnotation(ElasticTypeImporter.class);
        if (annotatedElasticType != null) {
            return annotatedElasticType.type();
        } else {
            throw new IllegalStateException(String.format("The importer class '%s' is not annotated with ElasticTypeImporter annotation",
                    this.getClass().getName()));
        }
    }

    protected String getId(MapHolder doc) {
        return doc.string("_id");
    }

    @Autowired
    public void setBeanMapper(BeanMapper beanMapper) {
        this.beanMapper = beanMapper;
    }

    @Autowired
    public final void setDataReporter(DataReporter dataReporter) {
        this.dataReporter = dataReporter;
    }

    @Autowired
    public void setRinaJsonMapper(RinaJsonMapper rinaJsonMapper) {
        this.rinaJsonMapper = rinaJsonMapper;
    }

    @Autowired
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Autowired
    public void setDataFeeder(DataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }
}

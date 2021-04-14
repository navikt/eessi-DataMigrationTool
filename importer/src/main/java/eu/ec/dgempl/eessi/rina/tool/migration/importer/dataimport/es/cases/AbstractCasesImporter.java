package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es.cases;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.tool.migration.common.service.EsClientService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.CaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.CaseFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.CasesUtils;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.ImporterUtils;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.ProgrammaticTransactionUtil;

@Component
@ElasticTypeImporter(type = EElasticType.NONE, order = 99)
public class AbstractCasesImporter extends AbstractDataImporter {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCasesImporter.class);

    private final ApplicationContext applicationContext;

    @Autowired
    private EsClientService esClientService;

    public AbstractCasesImporter(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void importData() {
        List<CaseImporter> importers = initializeImporters();
        List<String> cases = getCases();
        Instant batchStart = Instant.now();

        System.out.println("\nStart processing for " + cases.size() + " CASES");

        IntStream.range(0, cases.size()).forEach(i -> {
            startImportersForCase(importers, cases.get(i));
            if (i != 0 && (i % 100) == 0) {
                System.out.println("Cases processed: " + i + ". Pending to process: " + (cases.size() - i));
            }
        });

        float processTime = Duration.between(batchStart, Instant.now()).toMillis() / 1000F;
        System.out.println("Finished processing for " + cases.size() + " CASES in " + processTime + " seconds");
        System.out.println("\n-----------------------------------------------------");
    }

    private void startImportersForCase(final List<CaseImporter> importers, final String caseId) {
        try {
            ProgrammaticTransactionUtil.processSuccessfulTransaction(transactionManager, () -> {
                for (CaseImporter importer : importers) {
                    if (!CasesUtils.isDefaultCase(caseId) || (CasesUtils.isDefaultCase(caseId) && importer.processesEmptyCase())) {
                        try {
                            importer.importData(caseId);
                        } catch (Exception e) {
                            dataReporter.reportProblem(importer.inferElasticType(), caseId, e.getMessage());
                            throw e;
                        }
                    }
                }
            });
        } catch (Exception e) {
            logger.error("Error occurred when importing case: " + caseId, e);
        }
    }

    private List<CaseImporter> initializeImporters() {
        return ImporterUtils.getCaseImporters(applicationContext);
    }

    private List<String> getCases() {
        try {
            List<String> caseIds = esClientService.getCaseIds();
            caseIds.add(0, CaseFields.DEFAULT_CASE_ID);
            return caseIds;
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

}

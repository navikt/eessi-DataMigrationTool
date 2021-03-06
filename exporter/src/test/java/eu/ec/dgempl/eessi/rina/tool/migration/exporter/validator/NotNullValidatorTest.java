package eu.ec.dgempl.eessi.rina.tool.migration.exporter.validator;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import eu.ec.dgempl.eessi.rina.tool.migration.common.config.RunConfiguration;
import eu.ec.dgempl.eessi.rina.tool.migration.common.service.EsClientService;
import eu.ec.dgempl.eessi.rina.tool.migration.common.service.OrganisationLoaderService;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.GsonWrapper;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.EsDocument;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.report.DocumentValidationReport;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.report.ValidationResult;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.service.CacheService;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.service.EnumService;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.service.ParserService;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.service.SchemaProviderService;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.service.ValidationService;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.service.ValidatorProviderService;

@RunWith(MockitoJUnitRunner.class)
public class NotNullValidatorTest {

    @Mock
    EsClientService elasticClient;

    private ValidationService validationService;

    /*
     * Configuration
     */

    @Before
    public void setUp() {
        try {
            EnumService enumService = new EnumService();
            CacheService cacheService = new CacheService();
            RunConfiguration runConfiguration = new RunConfiguration();
            OrganisationLoaderService organisationLoaderService = new OrganisationLoaderService();
            ValidatorProviderService validatorProviderService = new ValidatorProviderService(elasticClient, cacheService, enumService,
                    organisationLoaderService);
            SchemaProviderService schemaProviderService = new SchemaProviderService(validatorProviderService,
                    "src/test/resources/field_mappings");
            ParserService parserService = new ParserService(schemaProviderService);
            validationService = new ValidationService(elasticClient, parserService, cacheService, runConfiguration);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Tests
     */

    @Test
    public void testRequiredSbdhNotNull() {
        try {
            Map<String, Object> obj = GsonWrapper.loadFromClasspathResource("CaseDocumentWithSbdhNull.json", Map.class);

            doReturn(Boolean.TRUE).when(elasticClient).exists(any(), any(), any());

            EsDocument esDocument = new EsDocument("cases", "document", "135175_92239b7594414399b92a74d2");
            esDocument.setObject(obj);
            EsDocument esDocument1 = new EsDocument("cases", "document", "135175");
            esDocument1.setObject(Collections.emptyMap());

            DocumentValidationReport documentReport = Whitebox.invokeMethod(validationService, "validateSingleDocument", esDocument,
                    esDocument1);

            List<ValidationResult> errors = documentReport.getErrors();
            Assert.assertEquals(0, errors.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSearchDefinitionWithValidName() {
        try {
            Map<String, Object> obj = GsonWrapper.loadFromClasspathResource("ConfigurationsSearchdefinition.json", Map.class);

            EsDocument esDocument = new EsDocument("configurations", "searchdefinition", "objectId");
            esDocument.setObject(obj);

            DocumentValidationReport documentReport = Whitebox.invokeMethod(validationService, "validateSingleDocument", esDocument,
                    esDocument);

            List<ValidationResult> errors = documentReport.getErrors();
            Assert.assertEquals(0, errors.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSearchDefinitionWithNullName() {
        try {
            Map<String, Object> obj = GsonWrapper.loadFromClasspathResource("ConfigurationsSearchdefinitionWithNullName.json", Map.class);

            EsDocument esDocument = new EsDocument("configurations", "searchdefinition", "objectId");
            esDocument.setObject(obj);

            DocumentValidationReport documentReport = Whitebox.invokeMethod(validationService, "validateSingleDocument", esDocument,
                    esDocument);

            List<ValidationResult> errors = documentReport.getErrors();
            Assert.assertEquals(0, errors.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSearchDefinitionWithEmptyName() {
        try {
            Map<String, Object> obj = GsonWrapper.loadFromClasspathResource("ConfigurationsSearchdefinitionWithEmptyName.json", Map.class);

            EsDocument esDocument = new EsDocument("configurations", "searchdefinition", "objectId");
            esDocument.setObject(obj);

            DocumentValidationReport documentReport = Whitebox.invokeMethod(validationService, "validateSingleDocument", esDocument,
                    esDocument);

            List<ValidationResult> errors = documentReport.getErrors();
            Assert.assertEquals(0, errors.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

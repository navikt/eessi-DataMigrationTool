package eu.ec.dgempl.eessi.rina.tool.migration.exporter.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.gson.Gson;

import eu.ec.dgempl.eessi.rina.tool.migration.common.model.EEsType;
import eu.ec.dgempl.eessi.rina.tool.migration.common.service.EsClientService;
import eu.ec.dgempl.eessi.rina.tool.migration.common.service.OrganisationLoaderService;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.cache.CacheEntry;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.EValidationResult;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.EsDocument;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.ValidationContext;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.report.ValidationResult;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.schema.Schema;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.service.CacheService;

@RunWith(MockitoJUnitRunner.class)
public class ReferenceValidatorTest {

    @Mock
    Schema schema;
    @Mock
    EsClientService elasticClient;
    @Mock
    CacheService cacheService;
    @Mock
    OrganisationLoaderService organisationLoaderService;

    private EsDocument mockedEsDocument;
    private EsDocument mockedEsDocumentParent;
    private ValidationContext validationContext;
    private String path = "testPath";
    private String validParam1 = "apclientlog";
    private String validParam2 = "checkinstance";
    private String testString = "testString";
    private ReferenceValidator referenceValidator;

    /*
     * Configuration
     */

    private void setUpValidationContext() {
        setUpValidationContextWithFiles("Basic.json", "Parent.json");
    }

    private void setUpValidationContextWithEmptyStatusValue() {
        setUpValidationContextWithFiles("WithEmptyStatusValue.json", "Parent.json");
    }

    private void setUpValidationContextWithDocumentTypeComment() {
        setUpValidationContextWithFiles("WithDocumentTypeComment.json", "Parent.json");
    }

    private void setUpValidationContextWithDocumentTypeDocumentContentAndParentDocumentIdIsNotNullAndCaseIdIsNull() {
        setUpValidationContextWithFiles("WithDocumentTypeDocumentContentAndParentDocumentIdAndNoCaseId.json", "Parent.json");
    }

    private void setUpValidationContextWithDocumentTypeDocumentContentAndWithParentDocumentIdAndCaseId() {
        setUpValidationContextWithFiles("WithDocumentTypeDocumentContentAndParentDocumentIdAndCaseId.json", "Parent.json");
    }

    private void setUpValidationContextWithFiles(String document, String parent) {

        String folderPath = "/src/test/resources/";
        String filePath = new File("").getAbsolutePath().concat(folderPath);
        Gson gson = new Gson();

        // Get document values
        try {
            mockedEsDocument = gson.fromJson(new FileReader(filePath.concat(document)), EsDocument.class);
            mockedEsDocumentParent = gson.fromJson(new FileReader(filePath.concat(parent)), EsDocument.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        validationContext = Mockito.spy(new ValidationContext(mockedEsDocument, mockedEsDocumentParent, schema));

        doReturn(mockedEsDocument).when(validationContext).getDocument();
        doReturn(mockedEsDocumentParent).when(validationContext).getParent();
    }

    private void setUpReferenceValidator() {
        setUpReferenceValidatorWithParams(validParam1, validParam2);
    }

    private void setUpReferenceValidatorWithParams(String param1, String param2) {
        referenceValidator = new ReferenceValidator(elasticClient, cacheService, organisationLoaderService, param1, param2);
    }

    /*
     * Tests
     */

    @Test
    public void whenAllFieldsAreInformedAndObjectIsCorrect_thenAssertionSucceeds() {

        setUpValidationContext();
        setUpReferenceValidator();

        Object object = testString;
        try {
            doReturn(Boolean.TRUE).when(elasticClient).exists(any(), any(), any());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenObjectIsNull_thenAssertionSucceeds() {

        setUpValidationContext();
        setUpReferenceValidator();

        ValidationResult expectedResult = ValidationResult.ok(path, null, null);

        List<ValidationResult> result = referenceValidator.validate(path, null, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformed_AndStatusIsEmptyValue_AndPathIsCreatorId_thenAssertionSucceeds() {

        setUpValidationContextWithEmptyStatusValue();
        setUpReferenceValidator();
        path = "creator.id";

        Object object = testString;

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformed_AndStatusIsEmptyValue_AndPathIsVersionsUsersId_thenAssertionSucceeds() {

        setUpValidationContextWithEmptyStatusValue();
        setUpReferenceValidator();
        path = "versions.user.id";

        Object object = testString;

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformed_AndStatusIsEmptyValue_AndPathIsAnotherValue_thenAssertionSucceeds() {

        setUpValidationContextWithEmptyStatusValue();
        setUpReferenceValidator();
        Object object = testString;
        try {
            doReturn(Boolean.TRUE).when(elasticClient).exists(any(), any(), any());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformedAndObjectIsNotString_thenValidationResultIsError() {

        setUpValidationContext();
        setUpReferenceValidator();
        Object object = Boolean.TRUE;

        ValidationResult expectedResult = ValidationResult.error(path, object, EValidationResult.INVALID_REFERENCE);

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformed_AndDocumentTypeEqualsCommentType_AndPathIsDocumentId_AndObjectIs0_thenAssertionSucceeds() {

        setUpValidationContextWithDocumentTypeComment();
        setUpReferenceValidator();
        Object object = "0";
        path = "documentId";

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformed_AndIndexIsIdentityV1_AndTypeIsUser_AndObjectIsSystem_thenAssertionSucceeds() {

        setUpValidationContext();
        setUpReferenceValidatorWithParams("identity_v1", "user");
        Object object = "system";
        path = "documentId";

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformed_AndIndexIsIdentityV1_AndTypeIsUser_AndObjectIs0_thenAssertionSucceeds() {

        setUpValidationContext();
        setUpReferenceValidatorWithParams("identity_v1", "user");
        Object object = "0";
        path = "documentId";

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformed_AndDocumentTypeEqualsDocumentContent_AndPathIsId_AndParentDocumentIdIsNotNull_AndCaseIdIsNull_thenValidationResultIsError() {

        setUpValidationContextWithDocumentTypeDocumentContentAndParentDocumentIdIsNotNullAndCaseIdIsNull();
        String param1 = "identity_v1";
        String param2 = "user";
        setUpReferenceValidatorWithParams(param1, param2);
        Object object = "1";
        path = "id";

        ValidationResult expectedResult = ValidationResult.error(path, object, EValidationResult.INVALID_REFERENCE,
                String.format("Could not create the reference id [params=%s,%s]", param1, param2));

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformed_AndIdRequiresFormatCase__Id_AndCacheEntryIsFalse_AndElasticClientIsTrue_thenAssertionSucceeds() {

        setUpValidationContextWithDocumentTypeDocumentContentAndWithParentDocumentIdAndCaseId();
        setUpReferenceValidator();
        Object object = "3123xff324";
        path = "id";
        try {
            doReturn(Boolean.TRUE).when(elasticClient).exists(any(), any(), any());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);

        String cacheEntryContext = mockedEsDocumentParent.getIndex() + "_" + mockedEsDocumentParent.getType() + "_"
                + mockedEsDocumentParent.getObjectId();
        CacheEntry expectedEntry = new CacheEntry(true, validParam1, EEsType.SUBDOCUMENT.value(), "case__" + object, cacheEntryContext);

        ArgumentCaptor<CacheEntry> argument = ArgumentCaptor.forClass(CacheEntry.class);
        Mockito.verify(cacheService).add(argument.capture());
        Assertions.assertThat(argument.getValue()).isEqualToComparingFieldByField(expectedEntry);
    }

    @Test
    public void whenAllFieldsAreInformed_AndCacheEntryIsTrue_thenAssertionSucceeds() {

        setUpValidationContext();
        setUpReferenceValidator();
        Object object = "1";
        path = "id";

        CacheEntry cacheEntry = new CacheEntry(true, "index", "type", "documentId", "context");
        doReturn(cacheEntry).when(cacheService).get(any(), any(), any());

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformed_AndCacheEntryIsFalse_AndElasticClientIsTrue_thenAssertionSucceeds() {

        setUpValidationContext();
        setUpReferenceValidator();
        Object object = "1";
        path = "id";
        try {
            doReturn(Boolean.TRUE).when(elasticClient).exists(any(), any(), any());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformed_AndCacheEntryIsFalse_AndElasticClientIsFalse_thenValidationResultIsError() {

        setUpValidationContext();
        setUpReferenceValidator();
        Object object = "1";
        path = "id";
        try {
            doReturn(Boolean.FALSE).when(elasticClient).exists(any(), any(), any());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ValidationResult expectedResult = ValidationResult.error(path, object, EValidationResult.INVALID_REFERENCE,
                String.format("Invalid reference id [index=%s,type=%s,id=%s]", validParam1, validParam2, object));

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformed_AndCacheEntryIsFalse_AndElasticClientIsFalse_AndTypeIsChanged_thenValidationResultIsError() {

        setUpValidationContextWithDocumentTypeDocumentContentAndWithParentDocumentIdAndCaseId();
        setUpReferenceValidator();
        Object object = "1";
        path = "id";
        try {
            doReturn(Boolean.FALSE).when(elasticClient).exists(any(), any(), any());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String newType = EEsType.SUBDOCUMENT.value();

        ValidationResult expectedResult = ValidationResult.error(path, object, EValidationResult.INVALID_REFERENCE,
                String.format("Invalid reference id [index=%s,type=%s,id=case__%s]", validParam1, newType, object));

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);

        String cacheEntryContext = mockedEsDocumentParent.getIndex() + "_" + mockedEsDocumentParent.getType() + "_"
                + mockedEsDocumentParent.getObjectId();
        CacheEntry expectedEntry = new CacheEntry(false, validParam1, EEsType.SUBDOCUMENT.value(), "case__" + object, cacheEntryContext);

        ArgumentCaptor<CacheEntry> argument = ArgumentCaptor.forClass(CacheEntry.class);
        Mockito.verify(cacheService).add(argument.capture());
        Assertions.assertThat(argument.getValue()).isEqualToComparingFieldByField(expectedEntry);
    }

    @Test
    public void whenThereAreMoreThan3ValidatorParams_thenIllegalArgumentExceptionIsThrown() {

        String param1 = "param1";
        String param2 = "param2";
        String param3 = "param3";
        String param4 = "param4";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            referenceValidator = new ReferenceValidator(elasticClient, cacheService, organisationLoaderService, param1, param2, param3,
                    param4);
        });

        assertEquals(exception.getClass(), IllegalArgumentException.class);
        assertEquals(exception.getMessage(), String
                .format("Invalid validator parameters [validator=ReferenceValidator, params=%s,%s,%s,%s]", param1, param2, param3, param4));
    }

    @Test
    public void whenThereAreLessThan2ValidatorParams_thenIllegalArgumentExceptionIsThrown() {

        String param1 = "param1";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            referenceValidator = new ReferenceValidator(elasticClient, cacheService, organisationLoaderService, param1);
        });

        assertEquals(exception.getClass(), IllegalArgumentException.class);
        assertEquals(exception.getMessage(),
                String.format("Invalid validator parameters [validator=ReferenceValidator, params=%s]", param1));
    }

    @Test
    public void whenThereIsNoPath_thenIllegalArgumentExceptionIsThrown() {

        setUpReferenceValidator();
        Object object = testString;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            referenceValidator.validate(null, object, validationContext);
        });

        assertEquals(exception.getClass(), IllegalArgumentException.class);
        assertEquals(exception.getMessage(), "The parameter 'path' cannot be empty");
    }

    @Test
    public void whenThereIsNoContext_thenNullPointerExceptionIsThrown() {

        setUpReferenceValidator();
        Object object = testString;

        Exception exception = assertThrows(NullPointerException.class, () -> {
            referenceValidator.validate(path, object, null);
        });

        assertEquals(exception.getClass(), NullPointerException.class);
        assertEquals(exception.getMessage(), "The parameter 'context' cannot be empty");
    }

    // Field mappings

    @Test
    public void whenAllFieldsAreInformed_invalidReferences_thenIllegalArgumentExceptionIsThrown() {

        String index = "notFoundIndex";
        String type = "notFoundType";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            referenceValidator = new ReferenceValidator(elasticClient, cacheService, organisationLoaderService, index, type);
        });

        assertEquals(exception.getClass(), IllegalArgumentException.class);
        assertEquals(exception.getMessage(),
                String.format("Invalid validator parameters [validator=ReferenceValidator, params=%s,%s]", index, type));
    }

    //
    // Reference(entities,organization)
    //

    @Test
    public void whenAllFieldsAreInformed_ReferenceEntitiesOrganization_AndCacheEntryIsFalse_AndElasticClientIsFalse_thenValidationResultIsError() {

        String index = "entities";
        String type = "organisation";

        setUpValidationContext();
        setUpReferenceValidatorWithParams(index, type);

        Object object = "1";
        path = "id";
        try {
            doReturn(Boolean.FALSE).when(elasticClient).exists(any(), any(), any());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ValidationResult expectedResult = ValidationResult.error(path, object, EValidationResult.INVALID_REFERENCE,
                String.format("Invalid reference id [index=%s,type=%s,id=%s]", index, type, object));

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);

        String cacheEntryContext = mockedEsDocumentParent.getIndex() + "_" + mockedEsDocumentParent.getType() + "_"
                + mockedEsDocumentParent.getObjectId();
        CacheEntry expectedEntry = new CacheEntry(false, index, type, object.toString(), cacheEntryContext);

        ArgumentCaptor<CacheEntry> argument = ArgumentCaptor.forClass(CacheEntry.class);
        Mockito.verify(cacheService).add(argument.capture());
        Assertions.assertThat(argument.getValue()).isEqualToComparingFieldByField(expectedEntry);
    }

    @Test
    public void whenAllFieldsAreInformed_ReferenceEntitiesOrganization_AndCacheEntryIsFalse_AndElasticClientIsTrue_thenValidationResultIsError() {

        String index = "entities";
        String type = "organisation";

        setUpValidationContext();
        setUpReferenceValidatorWithParams(index, type);

        Object object = "1";
        path = "id";
        try {
            doReturn(Boolean.TRUE).when(elasticClient).exists(any(), any(), any());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);

        String cacheEntryContext = mockedEsDocumentParent.getIndex() + "_" + mockedEsDocumentParent.getType() + "_"
                + mockedEsDocumentParent.getObjectId();
        CacheEntry expectedEntry = new CacheEntry(true, index, type, object.toString(), cacheEntryContext);

        ArgumentCaptor<CacheEntry> argument = ArgumentCaptor.forClass(CacheEntry.class);
        Mockito.verify(cacheService).add(argument.capture());
        Assertions.assertThat(argument.getValue()).isEqualToComparingFieldByField(expectedEntry);
    }

    //
    // Reference(identity_v1,user)
    //

    @Test
    public void whenAllFieldsAreInformed_ReferenceIdentityV1User_AndCacheEntryIsFalse_AndElasticClientIsFalse_thenValidationResultIsError() {

        String index = "identity_v1";
        String type = "user";

        setUpValidationContext();
        setUpReferenceValidatorWithParams(index, type);

        Object object = "1";
        path = "id";
        try {
            doReturn(Boolean.FALSE).when(elasticClient).exists(any(), any(), any());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ValidationResult expectedResult = ValidationResult.error(path, object, EValidationResult.INVALID_REFERENCE,
                String.format("Invalid reference id [index=%s,type=%s,id=%s]", index, type, object));

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);

        String cacheEntryContext = mockedEsDocumentParent.getIndex() + "_" + mockedEsDocumentParent.getType() + "_"
                + mockedEsDocumentParent.getObjectId();
        CacheEntry expectedEntry = new CacheEntry(false, index, type, object.toString(), cacheEntryContext);

        ArgumentCaptor<CacheEntry> argument = ArgumentCaptor.forClass(CacheEntry.class);
        Mockito.verify(cacheService).add(argument.capture());
        Assertions.assertThat(argument.getValue()).isEqualToComparingFieldByField(expectedEntry);
    }

    @Test
    public void whenAllFieldsAreInformed_ReferenceIdentityV1User_AndCacheEntryIsFalse_AndElasticClientIsTrue_thenValidationResultIsError() {

        String index = "identity_v1";
        String type = "user";

        setUpValidationContext();
        setUpReferenceValidatorWithParams(index, type);

        Object object = "1";
        path = "id";
        try {
            doReturn(Boolean.TRUE).when(elasticClient).exists(any(), any(), any());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);

        String cacheEntryContext = mockedEsDocumentParent.getIndex() + "_" + mockedEsDocumentParent.getType() + "_"
                + mockedEsDocumentParent.getObjectId();
        CacheEntry expectedEntry = new CacheEntry(true, index, type, object.toString(), cacheEntryContext);

        ArgumentCaptor<CacheEntry> argument = ArgumentCaptor.forClass(CacheEntry.class);
        Mockito.verify(cacheService).add(argument.capture());
        Assertions.assertThat(argument.getValue()).isEqualToComparingFieldByField(expectedEntry);
    }

    //
    // Reference(identity_v1,group)
    //

    @Test
    public void whenAllFieldsAreInformed_ReferenceIdentityV1Group_AndCacheEntryIsFalse_AndElasticClientIsFalse_thenValidationResultIsError() {

        String index = "identity_v1";
        String type = "group";

        setUpValidationContext();
        setUpReferenceValidatorWithParams(index, type);

        Object object = "1";
        path = "id";
        try {
            doReturn(Boolean.FALSE).when(elasticClient).exists(any(), any(), any());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ValidationResult expectedResult = ValidationResult.error(path, object, EValidationResult.INVALID_REFERENCE,
                String.format("Invalid reference id [index=%s,type=%s,id=%s]", index, type, object));

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);

        String cacheEntryContext = mockedEsDocumentParent.getIndex() + "_" + mockedEsDocumentParent.getType() + "_"
                + mockedEsDocumentParent.getObjectId();
        CacheEntry expectedEntry = new CacheEntry(false, index, type, object.toString(), cacheEntryContext);

        ArgumentCaptor<CacheEntry> argument = ArgumentCaptor.forClass(CacheEntry.class);
        Mockito.verify(cacheService).add(argument.capture());
        Assertions.assertThat(argument.getValue()).isEqualToComparingFieldByField(expectedEntry);
    }

    @Test
    public void whenAllFieldsAreInformed_ReferenceIdentityV1Group_AndCacheEntryIsFalse_AndElasticClientIsTrue_thenValidationResultIsError() {

        String index = "identity_v1";
        String type = "group";

        setUpValidationContext();
        setUpReferenceValidatorWithParams(index, type);

        Object object = "1";
        path = "id";
        try {
            doReturn(Boolean.TRUE).when(elasticClient).exists(any(), any(), any());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);

        String cacheEntryContext = mockedEsDocumentParent.getIndex() + "_" + mockedEsDocumentParent.getType() + "_"
                + mockedEsDocumentParent.getObjectId();
        CacheEntry expectedEntry = new CacheEntry(true, index, type, object.toString(), cacheEntryContext);

        ArgumentCaptor<CacheEntry> argument = ArgumentCaptor.forClass(CacheEntry.class);
        Mockito.verify(cacheService).add(argument.capture());
        Assertions.assertThat(argument.getValue()).isEqualToComparingFieldByField(expectedEntry);
    }

    //
    // Reference(cases,document,caseId_id)
    //

    @Test
    public void whenAllFieldsAreInformed_ReferenceCasesDocumentCaseId_id_AndCacheEntryIsFalse_AndElasticClientIsFalse_thenValidationResultIsError() {

        String index = "cases";
        String type = "document";

        setUpValidationContext();
        setUpReferenceValidatorWithParams(index, type);

        Object object = "caseId_id";
        path = "id";
        try {
            doReturn(Boolean.FALSE).when(elasticClient).exists(any(), any(), any());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ValidationResult expectedResult = ValidationResult.error(path, object, EValidationResult.INVALID_REFERENCE,
                String.format("Invalid reference id [index=%s,type=%s,id=%s]", index, type, object));

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);

        String cacheEntryContext = mockedEsDocumentParent.getIndex() + "_" + mockedEsDocumentParent.getType() + "_"
                + mockedEsDocumentParent.getObjectId();
        CacheEntry expectedEntry = new CacheEntry(false, index, type, object.toString(), cacheEntryContext);

        ArgumentCaptor<CacheEntry> argument = ArgumentCaptor.forClass(CacheEntry.class);
        Mockito.verify(cacheService).add(argument.capture());
        Assertions.assertThat(argument.getValue()).isEqualToComparingFieldByField(expectedEntry);
    }

    @Test
    public void whenAllFieldsAreInformed_ReferenceCasesDocumentCaseId_id_AndCacheEntryIsFalse_AndElasticClientIsTrue_thenValidationResultIsError() {

        String index = "cases";
        String type = "document";

        setUpValidationContext();
        setUpReferenceValidatorWithParams(index, type);

        Object object = "caseId_id";
        path = "id";
        try {
            doReturn(Boolean.TRUE).when(elasticClient).exists(any(), any(), any());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);

        String cacheEntryContext = mockedEsDocumentParent.getIndex() + "_" + mockedEsDocumentParent.getType() + "_"
                + mockedEsDocumentParent.getObjectId();
        CacheEntry expectedEntry = new CacheEntry(true, index, type, object.toString(), cacheEntryContext);

        ArgumentCaptor<CacheEntry> argument = ArgumentCaptor.forClass(CacheEntry.class);
        Mockito.verify(cacheService).add(argument.capture());
        Assertions.assertThat(argument.getValue()).isEqualToComparingFieldByField(expectedEntry);
    }

    //
    // Reference(cases,document,caseId_parentDocumentId)
    //

    @Test
    public void whenAllFieldsAreInformed_ReferenceCasesDocumentCaseId_parentDocumentId_AndCacheEntryIsFalse_AndElasticClientIsFalse_thenValidationResultIsError() {

        String index = "cases";
        String type = "document";

        setUpValidationContext();
        setUpReferenceValidatorWithParams(index, type);

        Object object = "caseId_parentDocumentId";
        path = "id";
        try {
            doReturn(Boolean.FALSE).when(elasticClient).exists(any(), any(), any());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ValidationResult expectedResult = ValidationResult.error(path, object, EValidationResult.INVALID_REFERENCE,
                String.format("Invalid reference id [index=%s,type=%s,id=%s]", index, type, object));

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);

        String cacheEntryContext = mockedEsDocumentParent.getIndex() + "_" + mockedEsDocumentParent.getType() + "_"
                + mockedEsDocumentParent.getObjectId();
        CacheEntry expectedEntry = new CacheEntry(false, index, type, object.toString(), cacheEntryContext);

        ArgumentCaptor<CacheEntry> argument = ArgumentCaptor.forClass(CacheEntry.class);
        Mockito.verify(cacheService).add(argument.capture());
        Assertions.assertThat(argument.getValue()).isEqualToComparingFieldByField(expectedEntry);
    }

    @Test
    public void whenAllFieldsAreInformed_ReferenceCasesDocumentCaseId_parentDocumentId_AndCacheEntryIsFalse_AndElasticClientIsTrue_thenValidationResultIsError() {

        String index = "cases";
        String type = "document";

        setUpValidationContext();
        setUpReferenceValidatorWithParams(index, type);

        Object object = "caseId_parentDocumentId";
        path = "id";
        try {
            doReturn(Boolean.TRUE).when(elasticClient).exists(any(), any(), any());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);

        String cacheEntryContext = mockedEsDocumentParent.getIndex() + "_" + mockedEsDocumentParent.getType() + "_"
                + mockedEsDocumentParent.getObjectId();
        CacheEntry expectedEntry = new CacheEntry(true, index, type, object.toString(), cacheEntryContext);

        ArgumentCaptor<CacheEntry> argument = ArgumentCaptor.forClass(CacheEntry.class);
        Mockito.verify(cacheService).add(argument.capture());
        Assertions.assertThat(argument.getValue()).isEqualToComparingFieldByField(expectedEntry);
    }

    //
    // Reference(cases,document,caseId_subdocuments.parentDocumentId)
    //

    @Test
    public void whenAllFieldsAreInformed_ReferenceCasesDocumentCaseId_subdocuments_parentDocumentId_AndCacheEntryIsFalse_AndElasticClientIsFalse_thenValidationResultIsError() {

        String index = "cases";
        String type = "document";

        setUpValidationContext();
        setUpReferenceValidatorWithParams(index, type);

        Object object = "caseId_subdocuments.parentDocumentId";
        path = "id";
        try {
            doReturn(Boolean.FALSE).when(elasticClient).exists(any(), any(), any());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ValidationResult expectedResult = ValidationResult.error(path, object, EValidationResult.INVALID_REFERENCE,
                String.format("Invalid reference id [index=%s,type=%s,id=%s]", index, type, object));

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);

        String cacheEntryContext = mockedEsDocumentParent.getIndex() + "_" + mockedEsDocumentParent.getType() + "_"
                + mockedEsDocumentParent.getObjectId();
        CacheEntry expectedEntry = new CacheEntry(false, index, type, object.toString(), cacheEntryContext);

        ArgumentCaptor<CacheEntry> argument = ArgumentCaptor.forClass(CacheEntry.class);
        Mockito.verify(cacheService).add(argument.capture());
        Assertions.assertThat(argument.getValue()).isEqualToComparingFieldByField(expectedEntry);
    }

    @Test
    public void whenAllFieldsAreInformed_ReferenceCasesDocumentCaseId_subdocuments_parentDocumentId_AndCacheEntryIsFalse_AndElasticClientIsTrue_thenValidationResultIsError() {

        String index = "cases";
        String type = "document";

        setUpValidationContext();
        setUpReferenceValidatorWithParams(index, type);

        Object object = "caseId_subdocuments.parentDocumentId";
        path = "id";
        try {
            doReturn(Boolean.TRUE).when(elasticClient).exists(any(), any(), any());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);

        String cacheEntryContext = mockedEsDocumentParent.getIndex() + "_" + mockedEsDocumentParent.getType() + "_"
                + mockedEsDocumentParent.getObjectId();
        CacheEntry expectedEntry = new CacheEntry(true, index, type, object.toString(), cacheEntryContext);

        ArgumentCaptor<CacheEntry> argument = ArgumentCaptor.forClass(CacheEntry.class);
        Mockito.verify(cacheService).add(argument.capture());
        Assertions.assertThat(argument.getValue()).isEqualToComparingFieldByField(expectedEntry);
    }

    //
    // Reference(cases,document,caseId_documentId)
    //

    @Test
    public void whenAllFieldsAreInformed_ReferenceCasesDocumentCaseId_documentId_AndCacheEntryIsFalse_AndElasticClientIsFalse_thenValidationResultIsError() {

        String index = "cases";
        String type = "document";

        setUpValidationContext();
        setUpReferenceValidatorWithParams(index, type);

        Object object = "caseId_documentId";
        path = "id";
        try {
            doReturn(Boolean.FALSE).when(elasticClient).exists(any(), any(), any());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ValidationResult expectedResult = ValidationResult.error(path, object, EValidationResult.INVALID_REFERENCE,
                String.format("Invalid reference id [index=%s,type=%s,id=%s]", index, type, object));

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);

        String cacheEntryContext = mockedEsDocumentParent.getIndex() + "_" + mockedEsDocumentParent.getType() + "_"
                + mockedEsDocumentParent.getObjectId();
        CacheEntry expectedEntry = new CacheEntry(false, index, type, object.toString(), cacheEntryContext);

        ArgumentCaptor<CacheEntry> argument = ArgumentCaptor.forClass(CacheEntry.class);
        Mockito.verify(cacheService).add(argument.capture());
        Assertions.assertThat(argument.getValue()).isEqualToComparingFieldByField(expectedEntry);
    }

    @Test
    public void whenAllFieldsAreInformed_ReferenceCasesDocumentCaseId_documentId_AndCacheEntryIsFalse_AndElasticClientIsTrue_thenValidationResultIsError() {

        String index = "cases";
        String type = "document";

        setUpValidationContext();
        setUpReferenceValidatorWithParams(index, type);

        Object object = "caseId_documentId";
        path = "id";
        try {
            doReturn(Boolean.TRUE).when(elasticClient).exists(any(), any(), any());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);

        String cacheEntryContext = mockedEsDocumentParent.getIndex() + "_" + mockedEsDocumentParent.getType() + "_"
                + mockedEsDocumentParent.getObjectId();
        CacheEntry expectedEntry = new CacheEntry(true, index, type, object.toString(), cacheEntryContext);

        ArgumentCaptor<CacheEntry> argument = ArgumentCaptor.forClass(CacheEntry.class);
        Mockito.verify(cacheService).add(argument.capture());
        Assertions.assertThat(argument.getValue()).isEqualToComparingFieldByField(expectedEntry);
    }

    //
    // Reference(cases,document,caseId_targetSedId)
    //

    @Test
    public void whenAllFieldsAreInformed_ReferenceCasesDocumentCaseId_targetSedId_AndCacheEntryIsFalse_AndElasticClientIsFalse_thenValidationResultIsError() {

        String index = "cases";
        String type = "document";

        setUpValidationContext();
        setUpReferenceValidatorWithParams(index, type);

        Object object = "caseId_targetSedId";
        path = "id";
        try {
            doReturn(Boolean.FALSE).when(elasticClient).exists(any(), any(), any());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ValidationResult expectedResult = ValidationResult.error(path, object, EValidationResult.INVALID_REFERENCE,
                String.format("Invalid reference id [index=%s,type=%s,id=%s]", index, type, object));

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);

        String cacheEntryContext = mockedEsDocumentParent.getIndex() + "_" + mockedEsDocumentParent.getType() + "_"
                + mockedEsDocumentParent.getObjectId();
        CacheEntry expectedEntry = new CacheEntry(false, index, type, object.toString(), cacheEntryContext);

        ArgumentCaptor<CacheEntry> argument = ArgumentCaptor.forClass(CacheEntry.class);
        Mockito.verify(cacheService).add(argument.capture());
        Assertions.assertThat(argument.getValue()).isEqualToComparingFieldByField(expectedEntry);
    }

    @Test
    public void whenAllFieldsAreInformed_ReferenceCasesDocumentCaseId_targetSedId_AndCacheEntryIsFalse_AndElasticClientIsTrue_thenValidationResultIsError() {

        String index = "cases";
        String type = "document";

        setUpValidationContext();
        setUpReferenceValidatorWithParams(index, type);

        Object object = "caseId_targetSedId";
        path = "id";
        try {
            doReturn(Boolean.TRUE).when(elasticClient).exists(any(), any(), any());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);

        String cacheEntryContext = mockedEsDocumentParent.getIndex() + "_" + mockedEsDocumentParent.getType() + "_"
                + mockedEsDocumentParent.getObjectId();
        CacheEntry expectedEntry = new CacheEntry(true, index, type, object.toString(), cacheEntryContext);

        ArgumentCaptor<CacheEntry> argument = ArgumentCaptor.forClass(CacheEntry.class);
        Mockito.verify(cacheService).add(argument.capture());
        Assertions.assertThat(argument.getValue()).isEqualToComparingFieldByField(expectedEntry);
    }

    //
    // Reference(cases,document,caseId_attachments.documentId)
    //

    @Test
    public void whenAllFieldsAreInformed_ReferenceCasesDocumentCaseId_attachments_documentId_AndCacheEntryIsFalse_AndElasticClientIsFalse_thenValidationResultIsError() {

        String index = "cases";
        String type = "document";

        setUpValidationContext();
        setUpReferenceValidatorWithParams(index, type);

        Object object = "caseId_attachments.documentId";
        path = "id";
        try {
            doReturn(Boolean.FALSE).when(elasticClient).exists(any(), any(), any());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ValidationResult expectedResult = ValidationResult.error(path, object, EValidationResult.INVALID_REFERENCE,
                String.format("Invalid reference id [index=%s,type=%s,id=%s]", index, type, object));

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);

        String cacheEntryContext = mockedEsDocumentParent.getIndex() + "_" + mockedEsDocumentParent.getType() + "_"
                + mockedEsDocumentParent.getObjectId();
        CacheEntry expectedEntry = new CacheEntry(false, index, type, object.toString(), cacheEntryContext);

        ArgumentCaptor<CacheEntry> argument = ArgumentCaptor.forClass(CacheEntry.class);
        Mockito.verify(cacheService).add(argument.capture());
        Assertions.assertThat(argument.getValue()).isEqualToComparingFieldByField(expectedEntry);
    }

    @Test
    public void whenAllFieldsAreInformed_ReferenceCasesDocumentCaseId_attachments_documentId_AndCacheEntryIsFalse_AndElasticClientIsTrue_thenValidationResultIsError() {

        String index = "cases";
        String type = "document";

        setUpValidationContext();
        setUpReferenceValidatorWithParams(index, type);

        Object object = "caseId_attachments.documentId";
        path = "id";
        try {
            doReturn(Boolean.TRUE).when(elasticClient).exists(any(), any(), any());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = referenceValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);

        String cacheEntryContext = mockedEsDocumentParent.getIndex() + "_" + mockedEsDocumentParent.getType() + "_"
                + mockedEsDocumentParent.getObjectId();
        CacheEntry expectedEntry = new CacheEntry(true, index, type, object.toString(), cacheEntryContext);

        ArgumentCaptor<CacheEntry> argument = ArgumentCaptor.forClass(CacheEntry.class);
        Mockito.verify(cacheService).add(argument.capture());
        Assertions.assertThat(argument.getValue()).isEqualToComparingFieldByField(expectedEntry);
    }
}

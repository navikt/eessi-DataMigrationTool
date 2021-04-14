package eu.ec.dgempl.eessi.rina.tool.migration.exporter.validator;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.gson.Gson;

import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.EValidationResult;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.EsDocument;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.ValidationContext;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.report.ValidationResult;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.schema.Schema;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.service.EnumService;

@RunWith(MockitoJUnitRunner.class)
public class EnumValidatorTest {

    @Mock
    Schema schema;

    private ValidationContext validationContext;
    private String path = "testPath";
    private EnumValidator enumValidator;
    private EnumService enumService;
    private EsDocument mockedEsDocument;

    /*
     * Configuration
     */

    @Before
    public void setUp() {

        setUpValidationContextWithFiles("Basic.json");
        enumService = new EnumService();
    }

    private void setUpValidationContextWithFiles(String document) {

        String folderPath = "/src/test/resources/";
        String filePath = new File("").getAbsolutePath().concat(folderPath);
        Gson gson = new Gson();

        // Get document values
        try {
            mockedEsDocument = gson.fromJson(new FileReader(filePath.concat(document)), EsDocument.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        validationContext = Mockito.spy(new ValidationContext(mockedEsDocument, mockedEsDocument, schema));
    }

    /*
     * Tests
     */

    @Test
    public void whenEnumValueExists_thenAssertionSucceeds() {

        Object object = "mess";
        String enumType = "ECategoryType";
        enumValidator = new EnumValidator(enumService, enumType);

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = enumValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenEnumValueNotExists_thenAssertionFails() {

        Object object = "messi";
        String enumType = "ECategoryType";
        enumValidator = new EnumValidator(enumService, enumType);

        /*
         * ECategoryType values: MESSAGING("mess"), SECURITY("sec"), BUSINESS("bus");
         */

        ValidationResult expectedResult = ValidationResult.error(path, object, EValidationResult.INVALID_ENUM, "Expected value from enum "
                + enumType + ". Accepted values in format 'name:values' are: [security : [sec], business : [bus], messaging : [mess]]");

        List<ValidationResult> result = enumValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenEnumValueNotExists_thenAssertionFails_2() {

        Object object = "Nobody";
        String enumType = "ERole";
        enumValidator = new EnumValidator(enumService, enumType);

        /*
         * ERole values: SUPERVISOR("Supervisor"), AUTHORIZED_CLERK("Authorized"), UNAUTHORIZED_CLERK("Nonauthorized"), AUDITOR("Auditor"),
         * VIEWER("Viewer"), MEDICAL("Medical"), VIP("Vip"), EVERYONE("Everyone");
         */

        ValidationResult expectedResult = ValidationResult.error(path, object, EValidationResult.INVALID_ENUM, "Expected value from enum "
                + enumType
                + ". Accepted values in format 'name:values' are: [viewer : [viewer], medical : [medical], everyone : [everyone], authorized_clerk : [authorized], unauthorized_clerk : [nonauthorized], auditor : [auditor], vip : [vip], supervisor : [supervisor]]");

        List<ValidationResult> result = enumValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenEnumValueIsNotString_thenAssertionFails() {

        Object object = 2;
        String enumType = "ECategoryType";
        enumValidator = new EnumValidator(enumService, enumType);

        ValidationResult expectedResult = ValidationResult.error(path, object, EValidationResult.INVALID_ENUM,
                "Value is not of type string. Accepted values in format 'name:values' are: [security : [sec], business : [bus], messaging : [mess]]");

        List<ValidationResult> result = enumValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }
}
package eu.ec.dgempl.eessi.rina.tool.migration.exporter.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.EsDocument;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.ValidationContext;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.report.ValidationResult;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.schema.Schema;

@RunWith(MockitoJUnitRunner.class)
public class StringValidatorTest {

    @Mock
    EsDocument esDocument;
    @Mock
    Schema schema;

    private ValidationContext validationContext;
    private String path = "testPath";
    private String testString = "testString";
    private StringValidator stringValidator;
    private StringValidator stringValidatorWith1Param;
    private StringValidator stringValidatorWith2Params;

    /*
     * Configuration
     */

    @Before
    public void setUp() {
        validationContext = new ValidationContext(esDocument, esDocument, schema);
        stringValidator = new StringValidator();
    }

    /*
     * Tests
     */

    @Test
    public void whenAllFieldsAreInformedAndObjIsCorrect_thenAssertionSucceeds() {

        Object object = testString;

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = stringValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformedAndObjIsEmptyString_thenAssertionSucceeds() {

        Object object = "";

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = stringValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformedAndObjTypeIsNumber_thenAssertionSucceeds() {

        Object object = 12345;

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = stringValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformedAndObjIsIncorrectType_thenValidationResultIsError() {

        Object object = Boolean.TRUE;

        ValidationResult expectedResult = ValidationResult.ok(path, object);

        List<ValidationResult> result = stringValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenThereIs1ValidatorParamAndValueIsNumber_thenAssertionSucceeds() {

        Object object = testString;
        stringValidatorWith1Param = new StringValidator("250");

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = stringValidatorWith1Param.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenThereIs1ValidatorParamAndValueIsNotNumber_thenNumberFormatExceptionIsThrown() {

        String param1 = "param1";
        Object object = testString;
        stringValidatorWith1Param = new StringValidator(param1);

        Exception exception = assertThrows(NumberFormatException.class, () -> {
            stringValidatorWith1Param.validate(path, object, validationContext);
        });

        assertEquals(exception.getClass(), NumberFormatException.class);
        assertEquals(exception.getMessage(), String.format("For input string: \"%s\"", param1));
    }

    @Test
    public void whenThereAreMoreThan1ValidatorParams_thenIllegalArgumentExceptionIsThrown() {

        String param1 = "param1";
        String param2 = "param2";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            stringValidatorWith2Params = new StringValidator(param1, param2);
        });

        assertEquals(exception.getClass(), IllegalArgumentException.class);
        assertEquals(exception.getMessage(),
                String.format("Invalid validator parameters [validator=StringValidator, params=%s,%s]", param1, param2));
    }

    @Test
    public void whenThereIsNoPath_thenIllegalArgumentExceptionIsThrown() {

        Object object = testString;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            stringValidator.validate(null, object, validationContext);
        });

        assertEquals(exception.getClass(), IllegalArgumentException.class);
        assertEquals(exception.getMessage(), "The parameter 'path' cannot be empty");
    }

    @Test
    public void whenObjIsNull_thenAssertionSucceeds() {

        ValidationResult expectedResult = ValidationResult.ok(path, null, null);

        List<ValidationResult> result = stringValidator.validate(path, null, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenThereIsNoContext_thenNullPointerExceptionIsThrown() {

        Object object = testString;

        Exception exception = assertThrows(NullPointerException.class, () -> {
            stringValidator.validate(path, object, null);
        });

        assertEquals(exception.getClass(), NullPointerException.class);
        assertEquals(exception.getMessage(), "The parameter 'context' cannot be empty");
    }
}

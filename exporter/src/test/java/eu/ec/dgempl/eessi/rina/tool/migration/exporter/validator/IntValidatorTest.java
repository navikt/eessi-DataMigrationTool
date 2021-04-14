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

import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.EValidationResult;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.EsDocument;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.ValidationContext;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.report.ValidationResult;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.schema.Schema;

@RunWith(MockitoJUnitRunner.class)
public class IntValidatorTest {

    @Mock
    EsDocument esDocument;
    @Mock
    Schema schema;

    private ValidationContext validationContext;
    private String path = "testPath";
    private IntValidator intValidator;

    /*
     * Configuration
     */

    @Before
    public void setUp() {
        validationContext = new ValidationContext(esDocument, esDocument, schema);
        intValidator = new IntValidator();
    }

    /*
     * Tests
     */

    @Test
    public void whenAllFieldsAreInformedAndObjIsCorrect_thenAssertionSucceeds() {

        Object object = 2;

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = intValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformedAndObjIsByte_thenValidationSucceeds() {

        Object object = Byte.valueOf("22");

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = intValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformedAndObjIsShort_thenValidationSucceeds() {

        Object object = Short.valueOf("22");

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = intValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformedAndObjIsInt_thenValidationSucceeds() {

        Object object = Integer.valueOf("22");

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = intValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformedAndObjIsLong_thenValidationResultIsError() {

        Object object = Long.valueOf("28922");

        ValidationResult expectedResult = ValidationResult.error(path, object, EValidationResult.INVALID_INTEGER,
                "Object is of numeric type, but larger than int.");

        List<ValidationResult> result = intValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformedAndObjIsACorrectString_thenValidationSucceeds() {

        Object object = "22";

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = intValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformedAndObjIsAnIncorrectString_thenValidationResultIsError() {

        Object object = "Ab2";

        ValidationResult expectedResult = ValidationResult.error(path, object, EValidationResult.INVALID_INTEGER,
                "Object is of type String, and cannot be converted to int");

        List<ValidationResult> result = intValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformedAndObjIsIncorrectType_thenValidationResultIsError() {

        Object object = Boolean.TRUE;

        ValidationResult expectedResult = ValidationResult.error(path, object, EValidationResult.INVALID_INTEGER);

        List<ValidationResult> result = intValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenThereIsNoPath_thenIllegalArgumentExceptionIsThrown() {

        Object object = 2;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            intValidator.validate(null, object, validationContext);
        });

        assertEquals(exception.getClass(), IllegalArgumentException.class);
        assertEquals(exception.getMessage(), "The parameter 'path' cannot be empty");
    }

    @Test
    public void whenObjIsNull_thenAssertionSucceeds() {

        ValidationResult expectedResult = ValidationResult.ok(path, null, null);

        List<ValidationResult> result = intValidator.validate(path, null, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenThereIsNoContext_thenNullPointerExceptionIsThrown() {

        Object object = 2;

        Exception exception = assertThrows(NullPointerException.class, () -> {
            intValidator.validate(path, object, null);
        });

        assertEquals(exception.getClass(), NullPointerException.class);
        assertEquals(exception.getMessage(), "The parameter 'context' cannot be empty");
    }
}

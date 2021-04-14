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
public class LongValidatorTest {

    @Mock
    EsDocument esDocument;
    @Mock
    Schema schema;

    private ValidationContext validationContext;
    private String path = "testPath";
    private LongValidator longValidator;

    /*
     * Configuration
     */

    @Before
    public void setUp() {
        validationContext = new ValidationContext(esDocument, esDocument, schema);
        longValidator = new LongValidator();
    }

    /*
     * Tests
     */

    @Test
    public void whenAllFieldsAreInformedAndObjIsCorrect_thenAssertionSucceeds() {

        Object object = 231231214124L;

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = longValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformedAndObjIsByte_thenValidationSucceeds() {

        Object object = Byte.valueOf("127");

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = longValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformedAndObjIsShort_thenValidationSucceeds() {

        Object object = Short.valueOf("23132");

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = longValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformedAndObjIsInt_thenValidationSucceeds() {

        Object object = Integer.valueOf("212312312");

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = longValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformedAndObjIsNotValidNumberType_thenValidationResultIsError() {

        Object object = Double.valueOf("28923232");

        ValidationResult expectedResult = ValidationResult.error(path, object, EValidationResult.INVALID_LONG,
                "Object is of numeric type, but larger than long.");

        List<ValidationResult> result = longValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformedAndObjIsACorrectString_thenValidationSucceeds() {

        Object object = "2232131231";

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = longValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformedAndObjIsAnIncorrectString_thenValidationResultIsError() {

        Object object = "Ab2";

        ValidationResult expectedResult = ValidationResult.error(path, object, EValidationResult.INVALID_LONG,
                "Object is of type String, and cannot be converted to long");

        List<ValidationResult> result = longValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformedAndObjIsIncorrectType_thenValidationResultIsError() {

        Object object = Boolean.TRUE;

        ValidationResult expectedResult = ValidationResult.error(path, object, EValidationResult.INVALID_LONG);

        List<ValidationResult> result = longValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenThereIsNoPath_thenIllegalArgumentExceptionIsThrown() {

        Object object = 231241231237L;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            longValidator.validate(null, object, validationContext);
        });

        assertEquals(exception.getClass(), IllegalArgumentException.class);
        assertEquals(exception.getMessage(), "The parameter 'path' cannot be empty");
    }

    @Test
    public void whenObjIsNull_thenAssertionSucceeds() {

        ValidationResult expectedResult = ValidationResult.ok(path, null, null);

        List<ValidationResult> result = longValidator.validate(path, null, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenThereIsNoContext_thenNullPointerExceptionIsThrown() {

        Object object = 2;

        Exception exception = assertThrows(NullPointerException.class, () -> {
            longValidator.validate(path, object, null);
        });

        assertEquals(exception.getClass(), NullPointerException.class);
        assertEquals(exception.getMessage(), "The parameter 'context' cannot be empty");
    }
}

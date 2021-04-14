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
public class BooleanValidatorTest {

    @Mock
    EsDocument esDocument;
    @Mock
    Schema schema;

    private ValidationContext validationContext;
    private String path = "testPath";
    private BooleanValidator booleanValidator;

    /*
     * Configuration
     */

    @Before
    public void setUp() {
        validationContext = new ValidationContext(esDocument, esDocument, schema);
        booleanValidator = new BooleanValidator();
    }

    /*
     * Tests
     */

    @Test
    public void whenAllFieldsAreInformedAndObjIsTrue_thenAssertionSucceeds() {

        Object object = Boolean.TRUE;

        ValidationResult expectedResult = ValidationResult.ok(path, Boolean.TRUE, null);

        List<ValidationResult> result = booleanValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformedAndObjIsFalse_thenAssertionSucceeds() {

        Object object = Boolean.FALSE;

        ValidationResult expectedResult = ValidationResult.ok(path, Boolean.FALSE, null);

        List<ValidationResult> result = booleanValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformedAndObjIsIncorrectType_thenValidationResultIsError() {

        Object object = "testObject";

        ValidationResult expectedResult = ValidationResult.error(path, object, EValidationResult.INVALID_BOOLEAN);

        List<ValidationResult> result = booleanValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenThereIsNoPath_thenIllegalArgumentExceptionIsThrown() {

        Object object = Boolean.TRUE;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            booleanValidator.validate(null, object, validationContext);
        });

        assertEquals(exception.getClass(), IllegalArgumentException.class);
        assertEquals(exception.getMessage(), "The parameter 'path' cannot be empty");
    }

    @Test
    public void whenObjIsNull_thenAssertionSucceeds() {

        ValidationResult expectedResult = ValidationResult.ok(path, null, null);

        List<ValidationResult> result = booleanValidator.validate(path, null, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenThereIsNoContext_thenNullPointerExceptionIsThrown() {

        Object object = Boolean.TRUE;

        Exception exception = assertThrows(NullPointerException.class, () -> {
            booleanValidator.validate(path, object, null);
        });

        assertEquals(exception.getClass(), NullPointerException.class);
        assertEquals(exception.getMessage(), "The parameter 'context' cannot be empty");
    }
}

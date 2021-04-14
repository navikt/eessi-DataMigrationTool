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
public class RegExValidatorTest {

    @Mock
    EsDocument esDocument;
    @Mock
    Schema schema;

    private ValidationContext validationContext;
    private String path = "testPath";
    private RegexValidator regexValidator;
    private String regExpParam = "[0-9a-zA-Z]*";

    /*
     * Configuration
     */

    @Before
    public void setUp() {
        validationContext = new ValidationContext(esDocument, esDocument, schema);
        regexValidator = new RegexValidator(regExpParam);
    }

    /*
     * Tests
     */

    @Test
    public void whenRegexValidatorHasNoParams_thenValidationResultIsError() {

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            regexValidator = new RegexValidator();
        });

        assertEquals(exception.getClass(), IllegalArgumentException.class);
        assertEquals(exception.getMessage(), String.format("Invalid validator parameters [validator=%s, params=]", "RegexValidator"));
    }

    @Test
    public void whenObjIsNull_thenAssertionSucceeds() {

        ValidationResult expectedResult = ValidationResult.ok(path, null, null);

        List<ValidationResult> result = regexValidator.validate(path, null, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformedAndObjIsIncorrectType_thenValidationResultIsError() {

        Object object = Boolean.TRUE;

        ValidationResult expectedResult = ValidationResult.error(path, object, EValidationResult.INVALID_STRING);

        List<ValidationResult> result = regexValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformedAndPatternMatches_thenAssertionSucceeds() {

        Object object = "123Test456Matching789";

        ValidationResult expectedResult = ValidationResult.ok(path, object, null);

        List<ValidationResult> result = regexValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformedAndPatternNotMatches_thenValidationResultIsError() {

        Object object = "test_Not_Matching";

        ValidationResult expectedResult = ValidationResult.error(path, object, EValidationResult.INVALID_STRING,
                String.format("String does not match regex [regex=%s]", regExpParam));

        List<ValidationResult> result = regexValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenThereIsNoPath_thenIllegalArgumentExceptionIsThrown() {

        Object object = Boolean.TRUE;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            regexValidator.validate(null, object, validationContext);
        });

        assertEquals(exception.getClass(), IllegalArgumentException.class);
        assertEquals(exception.getMessage(), "The parameter 'path' cannot be empty");
    }

    @Test
    public void whenThereIsNoContext_thenNullPointerExceptionIsThrown() {

        Object object = "test";

        Exception exception = assertThrows(NullPointerException.class, () -> {
            regexValidator.validate(path, object, null);
        });

        assertEquals(exception.getClass(), NullPointerException.class);
        assertEquals(exception.getMessage(), "The parameter 'context' cannot be empty");
    }
}

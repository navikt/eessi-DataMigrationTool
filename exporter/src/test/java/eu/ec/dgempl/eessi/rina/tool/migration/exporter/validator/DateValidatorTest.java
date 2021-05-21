package eu.ec.dgempl.eessi.rina.tool.migration.exporter.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZonedDateTime;
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
public class DateValidatorTest {

    @Mock
    EsDocument esDocument;
    @Mock
    Schema schema;

    private ValidationContext validationContext;
    private String path = "testPath";
    private DateValidator dateValidator;
    private String correctDate = "2020-06-25T22:00:00.000Z";
    private String incorrectDate = "202006-25T22:00:00.000Z";

    /*
     * Configuration
     */

    @Before
    public void setUp() {
        validationContext = new ValidationContext(esDocument, esDocument, schema);
        dateValidator = new DateValidator();
    }

    /*
     * Tests
     */

    @Test
    public void whenAllFieldsAreInformedAndObjIsCorrect_thenAssertionSucceeds() {

        Object object = correctDate;

        ValidationResult expectedResult = ValidationResult.ok(path, correctDate, null);

        List<ValidationResult> result = dateValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);

        // 2020-06-25T22:00:00.000Z
        object = "2020-06-25T22:00:00.000Z";
        expectedResult = ValidationResult.ok(path, object, null);
        result = dateValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);

        // 2020-06-25T22:00:00.000 (local time, no timezone)
        object = "2020-06-25T22:00:00.000";
        expectedResult = ValidationResult.ok(path, object, null);
        result = dateValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);

        // 1593122400 (unix timestamp in seconds, as string)
        object = "1593122400";
        expectedResult = ValidationResult.ok(path, object, null);
        result = dateValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);

        // 1593122400000 (unix timestamp in millis, as string)
        object = "1593122400000";
        expectedResult = ValidationResult.ok(path, object, null);
        result = dateValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);

        // 1593122400 (unix timestamp in seconds, as int)
        object = 1593122400;
        expectedResult = ValidationResult.ok(path, object, null);
        result = dateValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);

        // 1593122400 (unix timestamp in seconds, as long)
        object = 1593122400L;
        expectedResult = ValidationResult.ok(path, object, null);
        result = dateValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);

        // 1593122400000 (unix timestamp in millis, as long)
        object = 1593122400000L;
        expectedResult = ValidationResult.ok(path, object, null);
        result = dateValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformedAndObjIsIncorrectType_thenValidationResultIsError() {

        Object object = Boolean.TRUE;

        ValidationResult expectedResult = ValidationResult.error(path, object, EValidationResult.INVALID_DATE);

        List<ValidationResult> result = dateValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenAllFieldsAreInformedAndDateIsIncorrect_thenValidationResultIsError() {

        Object object = incorrectDate;

        ValidationResult expectedResult = ValidationResult.error(path, incorrectDate, EValidationResult.INVALID_DATE,
                "Could not parse ZonedDateTime: " + incorrectDate);

        List<ValidationResult> result = dateValidator.validate(path, object, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenThereIsNoPath_thenIllegalArgumentExceptionIsThrown() {

        Object object = ZonedDateTime.parse(correctDate);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            dateValidator.validate(null, object, validationContext);
        });

        assertEquals(exception.getClass(), IllegalArgumentException.class);
        assertEquals(exception.getMessage(), "The parameter 'path' cannot be empty");
    }

    @Test
    public void whenObjIsNull_thenAssertionSucceeds() {

        ValidationResult expectedResult = ValidationResult.ok(path, null, null);

        List<ValidationResult> result = dateValidator.validate(path, null, validationContext);

        assertEquals(1, result.size());
        Assertions.assertThat(result.get(0)).isEqualToComparingFieldByField(expectedResult);
    }

    @Test
    public void whenThereIsNoContext_thenNullPointerExceptionIsThrown() {

        Object object = ZonedDateTime.parse(correctDate);

        Exception exception = assertThrows(NullPointerException.class, () -> {
            dateValidator.validate(path, object, null);
        });

        assertEquals(exception.getClass(), NullPointerException.class);
        assertEquals(exception.getMessage(), "The parameter 'context' cannot be empty");
    }
}

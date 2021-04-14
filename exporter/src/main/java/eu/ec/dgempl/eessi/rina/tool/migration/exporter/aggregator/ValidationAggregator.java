package eu.ec.dgempl.eessi.rina.tool.migration.exporter.aggregator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.EValidationResult;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.ValidationContext;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.report.ValidationResult;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.schema.SchemaEntry;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.util.JsonPathHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.validator.Validator;

/**
 * Implementation of {@link Aggregator}. This class provides methods for handling objects of type {@link List<ValidationResult>}
 */
public class ValidationAggregator implements Aggregator<List<ValidationResult>> {
    private static final Logger logger = LoggerFactory.getLogger(ValidationAggregator.class);

    @Override
    public List<ValidationResult> process(String path, Object obj, ValidationContext context) {
        PreconditionsHelper.notNull(path, "path");
        PreconditionsHelper.notNull(context, "context");

        String index = context.getDocument().getIndex();
        String type = context.getDocument().getType();
        String id = context.getDocument().getObjectId();

        // find the entry in schema corresponding to path
        SchemaEntry schemaEntry = context.getSchema().getSchemaEntry(JsonPathHelper.normalisePath(path));

        // no entry means unknown field; fail only if object is not null
        if (schemaEntry == null) {
            if (obj != null) {
                logger.info("Field validation FAIL: unknown path [index={},type={},id={},path={}]", index, type, id, path);
                List<ValidationResult> validationResults = new ArrayList<>();
                validationResults.add(ValidationResult.error(path, null, EValidationResult.UNKNOWN_FIELD));
                return validationResults;
            } else {
                logger.debug("Field validation SKIP: unknown path, object is null [path={}]", path);
                return new ArrayList<>();
            }
        }

        // get the list of aggregators
        List<Validator> validators = schemaEntry.getValidators();

        // no validator means the field is known and ignored
        if (validators.size() == 0) {
            logger.debug("Field validation SKIP: ignored path [path={}]", path);
            return new ArrayList<>();
        }

        // collect the list of errors
        // @formatter:off
        List<ValidationResult> errors = validators.stream()
                // for each validator, call the validate method, which returns a List<ValidationResult>
                .map(v -> v.validate(path, obj, context))

                // flatten the List<List<ValidationResult>> to List<ValidationResult> 
                .flatMap(x -> x.stream())

                // filter out valid results, keep only errors
                .filter(validationResult -> !validationResult.getResult().equals(EValidationResult.VALID))

                // return the list of errors
                .collect(Collectors.toList());
        // @formatter:on

        // log errors
        if (errors.size() != 0) {
            String s = errors.stream().map(Object::toString).collect(Collectors.joining(","));
            logger.info("Field validation FAIL [index={},type={},id={},path={},errors={}]", index, type, id, path, s);
        } else {
            logger.debug("Field validation PASS [path={}]", path);
        }

        return errors;
    }

    @Override
    public List<ValidationResult> identity() {
        return new ArrayList<>();
    }

    @Override
    public List<ValidationResult> accumulate(List<ValidationResult> a, List<ValidationResult> b) {
        PreconditionsHelper.notNull(a, "a");
        PreconditionsHelper.notNull(b, "b");

        return Stream.concat(a.stream(), b.stream()).collect(Collectors.toList());
    }
}

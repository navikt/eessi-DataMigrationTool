package eu.ec.dgempl.eessi.rina.tool.migration.exporter.schema;

import java.util.List;

import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.validator.Validator;

/**
 * Class that defines the entries of the validation schema
 */
public class SchemaEntry {
    private final String path;
    private final boolean isIgnored;
    private final List<Validator> validators;

    /**
     * Private constructor
     * 
     * @param path
     *            the path to the object that is validated
     * @param isIgnored
     *            flag that indicates whether the object can be ignored from validation
     * @param validators
     *            the list of validators to be applied to objects with this {@code path}
     */
    private SchemaEntry(String path, boolean isIgnored, List<Validator> validators) {
        PreconditionsHelper.notEmpty(path, "path");
        PreconditionsHelper.notNull(validators, "validators");

        this.path = path;
        this.isIgnored = isIgnored;
        this.validators = validators;
    }

    /**
     * Static factory method for creating {@link SchemaEntry} instances
     * 
     * @param path
     *            the path to the object that is validated
     * @param isIgnored
     *            flag that indicates whether the object can be ignored from validation
     * @param validators
     *            the list of validators to be applied to objects with this {@code path}
     * @return
     */
    public static SchemaEntry from(String path, boolean isIgnored, List<Validator> validators) {
        PreconditionsHelper.notEmpty(path, "path");
        PreconditionsHelper.notNull(validators, "validators");

        return new SchemaEntry(path, isIgnored, validators);
    }

    /**
     * Static factory method for creating {@link SchemaEntry} instances. The instances created by this method will not be ignored from
     * validation
     *
     * @param path
     *            the path to the object that is validated
     * @param validators
     *            the list of validators to be applied to objects with this {@code path}
     * @return
     */
    public static SchemaEntry from(String path, List<Validator> validators) {
        PreconditionsHelper.notEmpty(path, "path");
        PreconditionsHelper.notNull(validators, "validators");

        return new SchemaEntry(path, false, validators);
    }

    public String getPath() {
        return path;
    }

    public boolean isIgnored() {
        return isIgnored;
    }

    public List<Validator> getValidators() {
        return validators;
    }
}

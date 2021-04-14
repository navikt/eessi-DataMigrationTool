package eu.ec.dgempl.eessi.rina.tool.migration.exporter.aggregator;

import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.ValidationContext;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.validator.Validator;

/**
 * Dummy aggregator that provides a test implementation of {@link Aggregator}
 */
public class DummyAggregator implements Aggregator<Boolean> {
    @Override
    public Boolean process(String path, Object obj, ValidationContext context) {
        PreconditionsHelper.notNull(path, "path");
        PreconditionsHelper.notNull(context, "context");

        return true;
    }

    @Override
    public Boolean identity() {
        return true;
    }

    @Override
    public Boolean accumulate(Boolean a, Boolean b) {
        PreconditionsHelper.notNull(a, "a");
        PreconditionsHelper.notNull(b, "b");

        return true;
    }

    @Override
    public Boolean applyValidation(Validator v, String path, Object obj, ValidationContext context) {
        return true;
    }

}

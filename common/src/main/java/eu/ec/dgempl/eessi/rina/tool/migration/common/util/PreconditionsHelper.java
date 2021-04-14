package eu.ec.dgempl.eessi.rina.tool.migration.common.util;

import java.util.function.Predicate;

import org.springframework.util.StringUtils;

import com.google.common.base.Preconditions;

/**
 * Helper class which wraps calls to guava {@link Preconditions} and format a common messages.
 */
public class PreconditionsHelper {

    public static final String MSG_PARAM_EMPTY = "The parameter '%s' cannot be empty";
    public static final String MSG_PARAM_INVALID = "The parameter '%s' is not valid";

    /**
     * Wrapper for the {@link Preconditions#checkNotNull(Object, Object)}.
     *
     * @param o
     * @param name
     */
    public static void notNull(final Object o, String name) {
        Preconditions.checkNotNull(o, String.format(MSG_PARAM_EMPTY, name));
    }

    /**
     * Wrapper for the {@link Preconditions#checkArgument(boolean, Object)}.
     *
     * @param o
     * @param name
     */
    public static void notEmpty(final String o, String name) {
        Preconditions.checkArgument(!StringUtils.isEmpty(o), String.format(MSG_PARAM_EMPTY, name));
    }

    /**
     * Tests the given predicate with the object {@code o} through the method {@link Preconditions#checkArgument(boolean, Object)}
     *
     * @param checkFunction
     * @param o
     * @param name
     * @param <T>
     */
    public static <T> void check(Predicate<T> checkFunction, final T o, String name) {
        Preconditions.checkArgument(checkFunction.test(o), String.format(MSG_PARAM_INVALID, name));
    }
}

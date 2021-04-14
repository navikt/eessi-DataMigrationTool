package eu.ec.dgempl.eessi.rina.tool.migration.exporter.aggregator;

import java.util.function.BinaryOperator;
import java.util.function.Function;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.lang.Nullable;

import eu.ec.dgempl.eessi.rina.tool.migration.exporter.model.ValidationContext;
import eu.ec.dgempl.eessi.rina.tool.migration.exporter.validator.Validator;

/**
 * Interface that provides the contract for defining aggregator classes. Aggregators must provide methods for processing streams using
 * {@link java.util.stream.Stream#map(Function)} and {@link java.util.stream.Stream#reduce(BinaryOperator)}.
 *
 * @param <T>
 */
public interface Aggregator<T> {

    /**
     * Method that executes a specific action on an {@code obj}
     * 
     * @param path
     *            the json path referencing the {@code obj}
     * @param obj
     *            the object on which the action is applied
     * @return the result of the action
     */
    T process(@NotNull String path, @Nullable Object obj, @NotNull ValidationContext context);

    /**
     * Method that defines the identity object of the generic type {@link T}
     * 
     * @return the identity object
     */
    T identity();

    /**
     * Method that aggregates two objects of type {@link T} into a single object
     * 
     * @param a
     *            object of type {@link T}
     * @param b
     *            object of type {@link T}
     * @return the resulting object of type {@link T}
     */
    T accumulate(@NotNull T a, @NotNull T b);

    T applyValidation(@NotNull Validator v, @NotBlank String path, @Nullable Object obj, @NotNull ValidationContext context);

}

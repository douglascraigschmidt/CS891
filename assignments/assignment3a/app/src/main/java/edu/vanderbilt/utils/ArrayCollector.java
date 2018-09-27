package edu.vanderbilt.utils;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * A mutable reduction operation that accumulates input elements
 * into a mutable result container.  These reduction operations
 * can be performed either sequentially or in parallel.
 */
public class ArrayCollector<T>
       implements Collector<T,
                            Array<T>,
                            Array<T>> {
    /**
     * A function that creates and returns a new mutable result
     * container that will hold all the elements in the stream.
     *
     * @return a function which returns a new, mutable result container
     */
    @Override
    public Supplier<Array<T>> supplier() {
        // TODO - grads you fill in here (ugrads you can optionally
        // implement this method).
        return null;
    }

    /**
     * A function that folds an Array into the mutable result
     * container.
     *
     * @return a function which folds a value into a mutable result container
     */
    @Override
    public BiConsumer<Array<T>, T> accumulator() {
        // TODO - grads you fill in here (ugrads you can optionally
        // implement this method).
        return null;
    }

    /**
     * A function that accepts two partial results and merges them.
     * The combiner function may fold state from one argument into the
     * other and return that, or may return a new result container.
     *
     * @return a function which combines two partial results into a combined
     * result
     */
    @Override
    public BinaryOperator<Array<T>> combiner() {
        // TODO - grads you fill in here (ugrads you can optionally
        // implement this method).
        return null;
    }

    /**
     * Perform the final transformation from the intermediate
     * accumulation type {@code A} to the final result type {@code
     * R}.
     *
     * @return a function which transforms the intermediate result to
     * the final result
     */
    @Override
    public Function<Array<T>, Array<T>> finisher() {
        // TODO - grads you fill in here (ugrads you can optionally
        // implement this method).
        return null;
    }

    /**
     * Returns a {@code Set} of {@code Collector.Characteristics}
     * indicating the characteristics of this Collector.  This set
     * should be immutable.
     *
     * @return An immutable set of collector characteristics, which in
     * this case is simply UNORDERED
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set characteristics() {
        // TODO - grads you fill in here (ugrads you can optionally
        // implement this method).
        return null;
    }

    /**
     * This static factory method creates a new ArrayCollector.
     *
     * @return A new ArrayCollector()
     */
    public static <E> Collector<E, ?, Array<E>> toArray() {
        // TODO - grads you fill in here (ugrads you can optionally
        // implement this method).
        return null;
    }
}

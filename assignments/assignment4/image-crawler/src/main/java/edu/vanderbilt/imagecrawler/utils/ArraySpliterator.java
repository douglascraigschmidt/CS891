package edu.vanderbilt.imagecrawler.utils;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * Defines an object for traversing and partitioning
 * elements of an Array.
 */
public final class ArraySpliterator<E>
        extends Spliterators.AbstractSpliterator<E> {
    /**
     * The array to traverse and/or partition.
     */
    // TODO - you fill in here

    /**
     * Current index, modified on advance/split.
     */
    // TODO - you fill in here

    /**
     * One past the end of the spliterator range.
     */
    // TODO - you fill in here

    /**
     * Create new spliterator covering the given range.
     */
    ArraySpliterator(Array<E> array,
                     int origin,
                     int end) {
        super(array.size(),
                Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED);

        // TODO - you fill in here.
    }

    /**
     * If a remaining element exists, performs the given action on
     * it, returning true; else returns false.  Throw
     * NullPointerException of {@code action} is null.
     */
    public boolean tryAdvance(Consumer<? super E> action) {
        // TODO - you fill in here (replace 'return false' with proper code).
        return false;
    }

    /**
     * Returns a Spliterator covering elements that will, upon
     * return from this method, not be covered by this Spliterator.
     */
    public ArraySpliterator<E> trySplit() {
        // TODO - you fill in here (replace 'return null' with proper code).
        return null;
    }
}

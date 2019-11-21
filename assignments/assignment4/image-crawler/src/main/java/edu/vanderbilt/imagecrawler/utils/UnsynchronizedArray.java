package edu.vanderbilt.imagecrawler.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A generic unsynchronized array class implemented via a single
 * contiguous buffer.
 */
@SuppressWarnings("ALL")
public class UnsynchronizedArray<E>
        implements Array<E> {
    /**
     * Default initial capacity (declared 'protected' for unit tests).
     */
    protected static final int DEFAULT_CAPACITY = 10;

    /**
     * Shared empty array instance used for empty instances.
     */
    private static final Object[] EMPTY_ELEMENTDATA = {};

    /**
     * The array buffer that stores all the array elements.  The
     * capacity is the length of this array buffer.
     */
    private Object[] mElementData;

    /**
     * The size of the array (the number of elements it contains).
     * This field also indicates the next "open" slot in the array,
     * i.e., where a call to add() will place the new element:
     * mElementData[mSize] = element.
     */
    private int mSize;

    /*
     * The following methods and nested iterator class use Java 7 features.
     */

    /**
     * Constructs an empty array with an initial capacity of ten.
     */
    public UnsynchronizedArray() {
        mElementData = EMPTY_ELEMENTDATA;
    }

    /**
     * Constructs an empty array with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity of the array
     * @throws IllegalArgumentException if the specified initial capacity
     *                                  is negative
     */
    public UnsynchronizedArray(int initialCapacity) {
        // TODO -- you fill in here.
    }

    /**
     * Constructs a array containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param c the collection whose elements are to be placed into this array
     * @throws NullPointerException if the specified collection is null
     */
    public UnsynchronizedArray(Collection<? extends E> c) {
        // TODO -- you fill in here.
    }

    /**
     * Returns <tt>true</tt> if this collection contains no elements.
     *
     * @return <tt>true</tt> if this collection contains no elements
     */
    public boolean isEmpty() {
        // TODO -- you fill in here (replace 'return false' with proper code).
        return false;
    }

    /**
     * Returns the number of elements in this collection.
     *
     * @return the number of elements in this collection
     */
    public int size() {
        // TODO -- you fill in here (replace 'return 0' with proper code).
        return 0;
    }

    /**
     * Returns the index of the first occurrence of the specified
     * element in this array, or -1 if this array does not contain the
     * element.
     *
     * @param o element to search for
     * @return the index of the first occurrence of the specified element in
     * this array, or -1 if this array does not contain the element
     */
    public int indexOf(Object o) {
        // TODO -- you fill in here (replace 'return -1' with proper code).
        return -1;
    }

    /**
     * Appends all of the elements in the specified collection to the
     * end of this array, in the order that they are returned by the
     * specified collection's Iterator.  The behavior of this
     * operation is undefined if the specified collection is modified
     * while the operation is in progress.  This implies that the
     * behavior of this call is undefined if the specified collection
     * is this array, and this array is nonempty.
     *
     * @param c collection containing elements to be added to this array
     * @return <tt>true</tt> if this array changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */
    public boolean addAll(Collection<? extends E> c) {
        // TODO -- you fill in here (replace 'return false' with proper code).
        return false;
    }

    /**
     * Appends all of the elements in the specified Array to the end
     * of this array, in the order that they are returned by the
     * specified collection's Iterator.  The behavior of this
     * operation is undefined if the specified collection is modified
     * while the operation is in progress.  This implies that the
     * behavior of this call is undefined if the specified collection
     * is this array, and this array is nonempty.
     *
     * @param a array containing elements to be added to this array
     * @return <tt>true</tt> if this array changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */
    public boolean addAll(Array<E> a) {
        // TODO -- you fill in here (replace 'return false' with proper code).
        return false;
    }

    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from
     * their indices).
     *
     * @param index the index of the element to be removed
     * @return the element that was removed from the list
     * @throws IndexOutOfBoundsException
     */
    public E remove(int index) {
        // TODO -- you fill in here (replace 'return null' with proper code).
        return null;
    }

    /**
     * Checks if the given index is in range (i.e., index is
     * non-negative and it not equal to or larger than the size of the
     * Array) and throws the IndexOutOfBoundsException if it's not.
     * <p>
     * Normally should be declared as 'private', but for unit test access,
     * has been declared 'protected'.
     */
    @Override
    public void rangeCheck(int index) {
        // TODO -- you fill in here.
    }

    /**
     * Returns the element at the specified position in this array.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this array
     * @throws IndexOutOfBoundsException
     */
    public E get(int index) {
        // TODO -- you fill in here (replace 'return null' with proper code).
        return null;
    }

    /**
     * Replaces the element at the specified position in this list with
     * the specified element.
     *
     * @param index   index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException
     */
    public E set(int index, E element) {
        // TODO -- you fill in here (replace 'return null' with proper code).
        return null;
    }

    /**
     * Appends the specified element to the end of this array.
     *
     * @param element to be appended to this array
     * @return {@code true}
     */
    public boolean add(E element) {
        // TODO -- you fill in here (replace 'return false' with proper code).
        return false;
    }

    /**
     * Ensure the array is large enough to hold {@code minCapacity}
     * elements.  The array will be expanded if necessary.
     * <p>
     * Normally should be declared as 'private', but for unit test access,
     * has been declared 'protected'.
     */
    @Override
    public void ensureCapacityInternal(int minCapacity) {
        // TODO -- you fill in here.
    }

    /**
     * @return a reference to the underlying unsynchronized array
     */
    public Array<E> toUnsynchronizedArray() {
        return this;
    }

    /**
     * @return a reference to the underlying buffer containing all of the elements in this Array
     * object in proper sequence
     */
    public Object[] uncheckedToArray() {
        return mElementData;
    }

    /**
     * Returns an array containing all of the elements in this Array
     * object in proper sequence (from first to last element).
     *
     * <p>The returned array will be "safe" in that no references to
     * it are maintained by this array.  (In other words, this method
     * must allocate a new array).  The caller is thus free to modify
     * the returned array.
     *
     * <p>This method acts as bridge between array-based and
     * collection-based APIs.
     *
     * @return an array containing all of the elements in this Array
     * object in proper sequence
     */
    public Object[] toArray() {
        return Arrays.copyOf(mElementData, mSize);
    }

    /**
     * Returns an array containing all of the elements in this list in
     * proper sequence (from first to last element); the runtime type
     * of the returned array is that of the specified array.  If the
     * list fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of
     * the specified array and the size of this list.
     *
     * @param a the array into which the elements of the list are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose.
     * @return an array containing the elements of the list
     * @throws ArrayStoreException  if the runtime type of the specified array
     *                              is not a supertype of the runtime type of every element in
     *                              this list
     * @throws NullPointerException if the specified array is null
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < mSize) {
            // Make a new array of a's runtime type, but this array's contents.
            return (T[]) Arrays.copyOf(mElementData,
                    mSize,
                    a.getClass());
        }

        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(mElementData, 0, a, 0, mSize);

        if (a.length > mSize) {
            a[mSize] = null;
        }

        return a;
    }

    /**
     * Returns an iterator over the elements in this Array in proper
     * sequence.
     *
     * @return an iterator over the elements in this Array in proper
     * sequence
     */
    public Iterator<E> iterator() {
        // TODO -- you fill in here (replace 'return null' with proper code).
        return null;
    }

    /**
     * Replaces each element of this list with the result of applying
     * the operator to that element.  Errors or runtime exceptions
     * thrown by the operator are relayed to the caller.
     *
     * @param operator the operator to applyTransform to each element
     */
    public void replaceAll(UnaryOperator<E> operator) {
        // TODO - you fill in here (this implementation can use a for loop).
    }

    /*
     * The following methods and nested class use Java 8 features.
     */

    /**
     * Performs the given action for each element of the array until
     * all elements have been processed or the action throws an
     * exception.  Unless otherwise specified by the implementing
     * class, actions are performed in the order of iteration (if an
     * iteration order is specified).  Exceptions thrown by the action
     * are relayed to the caller.
     *
     * @param action The action to be performed for each element
     */
    public void forEach(Consumer<? super E> action) {
        if (Assignment.isGraduateTodo()) {
            // TODO - Graduate students you fill in here (use the
            // Stream forEach() method).
        } else if (Assignment.isUndergraduateTodo()) {
            // TODO - Undergraduate students you fill in here (use a
            // Java for-each loop).
        } else {
            throw new IllegalStateException("Invalid Assignment type");
        }
    }

    /**
     * Creates a {@link Spliterator} over the elements in the array.
     *
     * @return a {@code Spliterator} over the elements in the array
     */
    public Spliterator<E> spliterator() {
        // TODO - you fill in here (replace 'return null' with proper code).
        return null;
    }

    /**
     * @return A parallel stream.
     */
    public Stream<E> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }

    /**
     * @return A sequential stream.
     */
    public Stream<E> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * This class defines an iterator over the elements in an Array in
     * proper sequence.
     */
    public class ArrayIterator implements Iterator<E> {
        /**
         * Current position in the Array (defaults to 0).
         */
        // TODO - you fill in here.

        /**
         * Index of last element returned; -1 if no such element.
         */
        // TODO - you fill in here.

        /**
         * @return True if the iteration has more elements that
         * haven't been iterated through yet, else false.
         */
        @Override
        public boolean hasNext() {
            // TODO - you fill in here (replace 'return false' with proper code).
            return false;
        }

        /**
         * @return The next element in the iteration.
         * @throws NoSuchElementException if there's no next element
         */
        @Override
        public E next() {
            // TODO - you fill in here (replace 'return null' with proper code).
            return null;
        }

        /**
         * Removes from the underlying collection the last element
         * returned by this iterator. This method can be called only
         * once per call to next().
         *
         * @throws IllegalStateException if no last element was
         *                               returned by the iterator
         */
        @Override
        public void remove() {
            // TODO - you fill in here
        }
    }
}

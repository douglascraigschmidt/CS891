package edu.vanderbilt.imagecrawler.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * A synchronized wrapper for the Array class.
 */
public class SynchronizedArray<E>
        implements Array<E> {
    // TODO -- you fill in any necessary field(s).

    /**
     * Constructs an empty synchronized array.
     */
    public SynchronizedArray() {
        // TODO -- you fill in here.
    }

    /**
     * Constructs a synchronized array from an {@code
     * unsynchronizedArray}.
     */
    public SynchronizedArray(Array<E> unsynchronizedArray) {
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
    public SynchronizedArray(Collection<? extends E> c) {
        // TODO -- you fill in here.
    }

    /**
     * Returns <tt>true</tt> if this collection contains no elements.
     *
     * @return <tt>true</tt> if this collection contains no elements
     */
    public boolean isEmpty() {
        // TODO -- you fill in here (replace 'false' with correct code).
        return false;
    }

    /**
     * Returns the number of elements in this collection.
     *
     * @return the number of elements in this collection
     */
    public int size() {
        // TODO -- you fill in here (replace '0' with correct code).
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
        // TODO -- you fill in here (replace '-1' with correct code).
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
        // TODO -- you fill in here (replace 'false' with correct code).
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
     * @param a collection containing elements to be added to this array
     * @return <tt>true</tt> if this array changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */
    public boolean addAll(Array<E> a) {
        // TODO -- you fill in here (replace 'false' with correct code).
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
        // TODO -- you fill in here (replace 'null' with correct code).
        return null;
    }

    /**
     * Returns the element at the specified position in this array.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this array
     * @throws IndexOutOfBoundsException
     */
    public E get(int index) {
        // TODO -- you fill in here (replace 'null' with correct code).
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
        // TODO -- you fill in here (replace 'null' with correct code).
        return null;
    }

    /**
     * Appends the specified element to the end of this array.
     *
     * @param element to be appended to this array
     * @return {@code true}
     */
    public boolean add(E element) {
        // TODO -- you fill in here (replace 'false' with correct code).
        return false;
    }

    /**
     * @return a copy of the underlying unsynchronized array
     */
    public Array<E> toUnsynchronizedArray() {
        // TODO -- you fill in here (replace 'null' with correct code).
        return null;
    }

    /**
     * @return a reference to the underlying buffer containing all of the elements in this Array
     * object in proper sequence
     */
    public Object[] uncheckedToArray() {
        // TODO -- you fill in here (replace 'null' with correct code).
        return null;
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
        // TODO -- you fill in here (replace 'null' with correct code).
        return null;
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
        // TODO -- you fill in here (replace 'null' with correct code).
        return null;
    }

    /**
     * Returns an iterator over the elements in this Array in proper
     * sequence.  Must be manually synchronized by the user.
     *
     * @return an iterator over the elements in this Array in proper
     * sequence
     */
    public Iterator<E> iterator() {
        // TODO - you fill in here (replace 'null' with correct code).
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
        // TODO -- you fill in here.
    }

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
        // TODO -- you fill in here.
    }

    /*
     * The following methods and nested class use Java 8 features.
     */

    /**
     * Must be manually synchronized by the user.
     *
     * @return A parallel stream.
     */
    public Stream<E> parallelStream() {
        // TODO -- you fill in here (replace 'null' with correct code).
        return null;

    }

    /**
     * Must be manually synchronized by the user.
     *
     * @return A sequential stream.
     */
    public Stream<E> stream() {
        // TODO -- you fill in here (replace 'null' with correct code).
        return null;
    }

    /**
     * Creates a {@link Spliterator} over the elements in the array.
     * Must be manually synchronized by the user.
     *
     * @return a {@code Spliterator} over the elements in the array
     */
    public Spliterator<E> spliterator() {
        // TODO -- you fill in here (replace 'null' with correct code).
        return null;
    }

    @Override
    public void ensureCapacityInternal(int minCapacity) {
        // TODO -- you fill in here.
    }

    @Override
    public void rangeCheck(int index) {
        // TODO -- you fill in here.
    }
}

package edu.vanderbilt.imagecrawler.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * A generic array interface.
 */
@SuppressWarnings("ALL")
public interface Array<E>
        extends Iterable<E> {
    /**
     * Returns <tt>true</tt> if this collection contains no elements.
     *
     * @return <tt>true</tt> if this collection contains no elements
     */
    boolean isEmpty();

    /**
     * Returns the number of elements in this collection.
     *
     * @return the number of elements in this collection
     */
    int size();

    /**
     * Returns the index of the first occurrence of the specified
     * element in this array, or -1 if this array does not contain the
     * element.
     *
     * @param o element to search for
     * @return the index of the first occurrence of the specified element in
     * this array, or -1 if this array does not contain the element
     */
    int indexOf(Object o);

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
    boolean addAll(Collection<? extends E> c);

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
    boolean addAll(Array<E> a);

    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from
     * their indices).
     *
     * @param index the index of the element to be removed
     * @return the element that was removed from the list
     * @throws IndexOutOfBoundsException
     */
    E remove(int index);

    /**
     * Returns the element at the specified position in this array.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this array
     * @throws IndexOutOfBoundsException
     */
    E get(int index);

    /**
     * Replaces the element at the specified position in this list with
     * the specified element.
     *
     * @param index   index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException
     */
    E set(int index, E element);

    /**
     * Appends the specified element to the end of this array.
     *
     * @param element to be appended to this array
     * @return {@code true}
     */
    boolean add(E element);

    /**
     * @return a reference to the underlying unsynchronized array
     */
    Array<E> toUnsynchronizedArray();

    /**
     * @return a reference to the underlying buffer containing all of the elements in this Array
     * object in proper sequence
     */
    Object[] uncheckedToArray();

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
    Object[] toArray();

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
    <T> T[] toArray(T[] a);

    /**
     * Returns an iterator over the elements in this Array in proper
     * sequence.
     *
     * @return an iterator over the elements in this Array in proper
     * sequence
     */
    Iterator<E> iterator();

    /**
     * Replaces each element of this list with the result of applying
     * the operator to that element.  Errors or runtime exceptions
     * thrown by the operator are relayed to the caller.
     *
     * @param operator the operator to applyTransform to each element
     */
    void replaceAll(UnaryOperator<E> operator);

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
    void forEach(Consumer<? super E> action);

    /**
     * @return A parallel stream.
     */
    Stream<E> parallelStream();

    /**
     * @return A sequential stream.
     */
    Stream<E> stream();

    /**
     * Creates a {@link Spliterator} over the elements in the array.
     *
     * @return a {@code Spliterator} over the elements in the array
     */
    Spliterator<E> spliterator();

    /**
     * Exposed in this interface for testing only.
     */
    void ensureCapacityInternal(int minCapacity);

    /**
     * Exposed for in this interface testing only.
     */
    void rangeCheck(int index);
}

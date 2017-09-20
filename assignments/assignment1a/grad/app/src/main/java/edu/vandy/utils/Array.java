package edu.vandy.utils;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A generic array class implemented via a single contiguous buffer.
 */
public class Array<E>
       implements Iterable<E> {
    /**
     * The array buffer that stores all the array elements.  The
     * capacity is the length of this array buffer.
     */
    private Object[] mElementData;

    /**
     * Index to the last element in the array.
     */
    private int mEnd;

    /**
     * The size of the Array (the number of elements it contains).
     */
    private int mSize;

    /**
     * Default initial capacity.
     */
    private static final int DEFAULT_CAPACITY = 10;

    /**
     * Shared empty array instance used for empty instances.
     */
    private static final Object[] sEMPTY_ELEMENTDATA = {};

    /*
     * The following methods and nested class use Java 7 features.
     */

    /**
     * Constructs an empty array with an initial capacity of ten.
     */
    public Array() {
        mElementData = sEMPTY_ELEMENTDATA;
    }

    /**
     * Constructs an empty array with the specified initial capacity.
     *
     * @param  initialCapacity  the initial capacity of the array
     * @throws IllegalArgumentException if the specified initial capacity
     *         is negative
     */
    public Array(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Array capacity cannot be negative");
        }
        mElementData = new Object[initialCapacity];
        mSize=0;
        mEnd=-1;
    }

    /**
     * Constructs a array containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param c the collection whose elements are to be placed into this array
     * @throws NullPointerException if the specified collection is null
     */
    public Array(Collection<? extends E> c) {
        if (c == null) {
            throw new NullPointerException("Collection param cannot be null");
        }
        mElementData = c.toArray();
        mSize = mElementData.length;
        mEnd = mSize-1;
    }

    /**
     * Returns <tt>true</tt> if this collection contains no elements.
     *
     * @return <tt>true</tt> if this collection contains no elements
     */
    public boolean isEmpty() {
        if (mElementData == sEMPTY_ELEMENTDATA) {
            mElementData = new Object[DEFAULT_CAPACITY];
            mSize=0;
            mEnd=-1;
        }
        if (mSize>0){
            return false;
        }
        return true;
    }
    
    /**
     * Returns the number of elements in this collection.  If this collection
     * contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of elements in this collection
     */
    public int size() {
        return mSize;
    }
    
    /**
     * Returns the index of the first occurrence of the specified
     * element in this array, or -1 if this array does not contain the
     * element.
     *
     * @param o element to search for
     * @return the index of the first occurrence of the specified element in
     *         this array, or -1 if this array does not contain the element
     */
    public int indexOf(Object o) {
        int counter=0;
        if (!isEmpty()){
            for(Object element:mElementData){
                if (element.equals(o)){
                    return counter;
                }
                counter++;
            }
        }
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
        if (c == null) {
            throw new NullPointerException();
        }
        Array<E> tempArray = new Array(c);
        addAll(tempArray);
        return true;
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
        if (a == null){
            throw new NullPointerException();
        }
        ensureCapacityInternal(a.size());
        for (int i=0;i<a.size();i++) {
            E element = a.get(i);
            if (element!=null) {
                add(element);
            }
        }
        return true;
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
        rangeCheck(index);
        if (isEmpty()){
            return null;
        }
        E oldObject = (E) mElementData[index];
        if (index == mEnd){
            mElementData[index]=null;
            mSize--;
            mEnd--;
        }
        else if (index==0){
            mElementData=Arrays.copyOfRange(mElementData, index+1, mSize);
            mSize--;
            mEnd--;
        }
        else{
            Array<E> tempArray = new  Array(Arrays.asList(Arrays.copyOfRange( mElementData, index+1, mEnd+1)));
            mElementData = Arrays.copyOfRange(mElementData,0,index);
            mSize=index;
            mEnd=mSize-1;
            addAll(tempArray);
        }

        return oldObject;
    }

    /**
     * Checks if the given index is in range (i.e., index is
     * non-negative and it not equal to or larger than the size of the
     * Array) and throws the IndexOutOfBoundsException if it's not.
     */
    private void rangeCheck(int index) {
        if ((index < 0)||(index > mEnd)) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Returns the element at the specified position in this array.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this array
     * @throws IndexOutOfBoundsException
     */
    public E get(int index) {
        rangeCheck(index);
        if (isEmpty()){
            return null;
        }
        return (E) mElementData[index];
    }

    /**
     * Replaces the element at the specified position in this list with
     * the specified element.
     *
     * @param index index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException
     */
    public E set(int index, E element) {
        rangeCheck(index);
        if (isEmpty()){
            mElementData[index] = element;
            return null;
        }
        if (mElementData[index]!=null){
            E oldElement = (E) mElementData[index];
            mElementData[index] = element;
            return oldElement;
        }
        else{
            mElementData[index] = element;
            return null;
        }
    }

    /**
     * Appends the specified element to the end of this array.
     **
     * @param element to be appended to this array
     * @return {@code true}
     */
    public boolean add(E element) {
        ensureCapacityInternal(1);
        if (isEmpty()){
            mElementData[0] = element;
        }
        else {
            mElementData[mSize] = element;
        }
        mSize++;
        mEnd++;
        return true;
    }
    
    /**
     * Ensure the array is large enough to hold @a minCapacity
     * elements.  The array will be expanded if necessary.
     */
    private void ensureCapacityInternal(int minCapacity) {
        if (mElementData == sEMPTY_ELEMENTDATA){
            mElementData = new Object[DEFAULT_CAPACITY];
            mSize=0;
            mEnd=-1;
        }
        if (mElementData.length-mSize < minCapacity){
            mElementData = Arrays.copyOf(mElementData, mSize+minCapacity);
        }
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
     *         object in proper sequence
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
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in
     *         this list
     * @throws NullPointerException if the specified array is null
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < mSize)
            // Make a new array of a's runtime type, but my contents:
            return (T[]) Arrays.copyOf(mElementData,
                                       mSize,
                                       a.getClass());

        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(mElementData,
                         0,
                         a,
                         0,
                         mSize);

        if (a.length > mSize)
            a[mSize] = null;
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
        return new ArrayIterator();
    }

    /**
     * This class defines an iterator over the elements in an Array in
     * proper sequence.
     */
    private class ArrayIterator 
           implements Iterator<E> {
        /**
         * Current position in the Array (defaults to 0).
         */
        private int position=0;

        /**
         * Index of last element returned; -1 if no such element.
         */
        private int lastReturn=-1;

        /** 
         * @return True if the iteration has more elements that
         * haven't been iterated through yet, else false
         */
        @Override
        public boolean hasNext() {
            return (size()>position);
        }

        /*
         * @return The next element in the iteration
         *
         * @throws IndexOutOfBounds exception if there are no more
         * elements to iterate through
         */
        @Override
        public E next() {
            if (position>=size()) {
                throw new IndexOutOfBoundsException();
            }
            lastReturn=position;
            position++;
            return get(lastReturn);
        }

        /**
         * Removes from the underlying collection the last element
         * returned by this iterator. This method can be called only
         * once per call to next().
         *
         * @throws IllegalStateException if no last element was
         * returned by the iterator
         */
        @Override
        public void remove() {
            if(lastReturn==-1){
                throw new IllegalStateException();
            }
            else if (position!=lastReturn){
                Array.this.remove(lastReturn);
                position--;
                lastReturn--;
            }
        }
    }
}

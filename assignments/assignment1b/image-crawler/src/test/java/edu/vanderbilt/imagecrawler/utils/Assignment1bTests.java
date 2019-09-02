package edu.vanderbilt.imagecrawler.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.LongStream;

import admin.ArrayHelper;
import admin.AssignmentTestRule;
import admin.ReflectionHelper;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test program for the Java 7 features of the Array class.
 * Note that since the sample input data contains objects whose
 * equals() methods always returns false, care must be used to
 * use assertSame() instead of assertEquals() where necessary.
 */
public class Assignment1bTests {
    @Rule
    public AssignmentTestRule assignmentTestRule = new AssignmentTestRule();

    /**
     * Maximum test size (must be an even value).
     */
    private final int mMaxTestSize = 20;

    /**
     * A list containing:
     * <p>
     * 1. Unique Object instances.
     * <p>
     * 2. Duplicate Object instances that will return true when
     * compared with both equals() and ==
     * <p>
     * 3. Two or more duplicated ObjectIsSameButNotEqual instances
     * which is a special class I've made that always returns
     * false for equals() and true for ==. These objects will
     * help detect cases where == is used instead of equals().
     * <p>
     * 4. One or more null values.
     * <p>
     * 5. Two or more ObjectIsEqualButNotSame instances that will
     * return true when compared with equals() but false when
     * compared with ==.
     */
    private final List<Object> mMixedInput = ArrayHelper.constructMixedInput();

    /**
     * A list with a similar content to mMixedInput but with all
     * entries shuffled.
     */
    private final List<Object> mShuffledMixedInput =
            ArrayHelper.constructShuffledMixedInput();

    /**
     * A special list to ensure that Array implementation always uses
     * equals() method for comparisons and not the sameness operator ==.
     * <p>
     * It contains:
     * <p>
     * Two or more ObjectIsEqualButNotSame instances that will
     * return true when compared with equals() but false when
     * compared with ==.
     */
    private final List<Object> mInputSameButNotEqual =
            ArrayHelper.constructInputWithObjectsSameButNotEqual();

    /**
     * A list of mMaxTestSize random integers (with no duplicates).
     */
    private final List<Object> mInputWithNoDups =
            ArrayHelper.getInputNoDups(mMaxTestSize);

    /**
     * An exception rule.
     */
    @Rule
    public ExpectedException mException = ExpectedException.none();

    @Test
    public void testConstructorWithValidCapacityInput() {
        for (int i = 0; i <= mMaxTestSize; i++) {
            Array<Integer> array = newArray(i);
            Object[] data = ArrayHelper.getElements(array);
            assertNotNull(data);
            assertEquals(data.length, i);
            assertEquals(0, array.size());
        }
    }

    //--------------------------------------------------------------------------
    // Tests
    //--------------------------------------------------------------------------

    /**
     * This test ensures that the Assignment class's types property has at least one
     * enumerated entry.
     */
    @Test
    public void testAssignmentTypes() {
        // At least one todo type should be a member of the types set (possibly both).
        assertTrue(Assignment.isGraduateTodo() || Assignment.isUndergraduateTodo());
    }

    /**
     * This test should always pass, even on an empty skeleton since it's purpose
     * is to ensure that the student does not change or alter the default constructor.
     */
    @Test
    public void testDefaultConstructor() {
        Array<Object> array = newArray();
        Object[] elements = ArrayHelper.getElements(array);
        assertNotNull(elements);

        Object[] expected =
                ReflectionHelper.findFirstMatchingFieldValue(
                        array, Object[].class, "sEMPTY_ELEMENTDATA");

        assertSame(expected, elements);
        assertEquals(0, elements.length);
        assertEquals(0, array.size());
    }

    /**
     * Test constructors.
     */

    @Test
    public void testConstructorWithInvalidCapacityInput() {
        mException.expect(IllegalArgumentException.class);
        newArray(-1);
    }

    @Test
    public void testConstructorWithNonEmptyCollectionInput() {
        List<Object> input = mShuffledMixedInput;
        Array<Object> array = newArray(input);
        Object[] data = ArrayHelper.getElements(array);

        assertNotNull(data);
        assertEquals(input.size(), data.length);
        assertEquals(input.size(), array.size());
        for (int i = 0; i < input.size(); i++) {
            // Use same() and not equals() to ensure that
            // array has the same objects and not clones.
            assertSame(input.get(i), data[i]);
        }
    }

    @Test
    public void testConstructorWithEmptyCollectionInput() {
        ArrayList<Object> emptyCollection = new ArrayList<>(0);
        Array<Object> array = newArray(emptyCollection);
        Object[] data = ArrayHelper.getElements(array);

        assertNotNull(data);
        assertEquals(0, data.length);
        assertEquals(0, array.size());
    }

    @Test
    public void testConstructorWithInvalidCollectionInput() {
        mException.expect(NullPointerException.class);
        //noinspection ConstantConditions
        newArray(null);
    }

    /**
     * Test size method.
     */

    @Test
    public void testSizeBlackBox() {
        List<Object> input = mShuffledMixedInput;
        Array<Object> array = newArray(input);
        assertEquals(input.size(), array.size());
    }

    /**
     * This test will catch cases where mElementData is
     * needlessly referenced in size() method.
     */
    @Test
    public void testSizeOnlyUsesSizePropertyWhiteBox() throws Exception {
        List<Object> input = mShuffledMixedInput;
        Array<Object> array = newArray(input);
        ReflectionHelper.injectValueIntoMatchingField(
                array, null, Object[].class, "mElementData");
        ReflectionHelper.injectValueIntoMatchingField(
                array, 1, int.class, "mSize");
        assertEquals(1, array.size());
    }

    /**
     * Test isEmpty method.
     */

    @Test
    public void testIsEmptyWithZeroCapacityBlackBox() {
        Array<Object> array = newArray(0);
        // First make sure that constructor actually worked as expected.
        assertNotNull(ArrayHelper.getElements(array));
        assertEquals(0, ArrayHelper.getElements(array).length);

        // Test method.
        assertTrue(array.isEmpty());
    }

    @Test
    public void testIsEmptyWithPositiveCapacityBlackBox() {
        Array<Object> array = newArray(1);
        assertEquals(1, ArrayHelper.getElements(array).length);

        // Test method.
        assertTrue(array.isEmpty());
    }

    /**
     * This test will catch cases where mElementData is
     * needlessly referenced in isEmpty() method.
     */
    @Test
    public void testIsEmptyWhiteBox() throws Exception {
        List<Object> input = mShuffledMixedInput;
        Array<Object> array = newArray(input);
        assertEquals(input.size(), array.size());

        ReflectionHelper.injectValueIntoMatchingField(
                array, null, Object[].class, "mElementData");
        ReflectionHelper.injectValueIntoMatchingField(
                array, 1, int.class, "mSize");
        assertFalse(array.isEmpty());
    }

    /**
     * Test rangeCheck method
     */

    @Test
    public void testRangeCheckOnlyUsesSizeMember() throws IllegalAccessException {
        int fakeSize = 10;
        Array<Object> array = newArray(fakeSize);
        assertEquals(10, ArrayHelper.getElements(array).length);

        // Set size to an arbitrary value even though there
        // are no backing elements in this empty array; rangeCheck
        // should not only care about mSize and not the contents of
        // the backing element data.
        ArrayHelper.setSize(array, fakeSize);

        for (int i = 0; i < fakeSize; i++) {
            // Should never throw an exception.
            array.rangeCheck(i);
        }
    }

    @Test
    public void testRangeCheckOnEmptyArray() {
        mException.expect(IndexOutOfBoundsException.class);
        (newArray()).rangeCheck(0);
    }

    @Test
    public void testRangeCheckIndexBelowLowerBound() {
        mException.expect(IndexOutOfBoundsException.class);
        (newArray(mMixedInput)).rangeCheck(-1);
    }

    @Test
    public void testRangeCheckIndexAboveUpperBound() {
        mException.expect(IndexOutOfBoundsException.class);
        (newArray(mMixedInput)).rangeCheck(mMixedInput.size());
    }

    /**
     * Test indexOf method
     */

    @Test
    public void testIndexOf() {
        List<Object> input = mInputWithNoDups;
        Array<Object> array = newArray(input);
        assertEquals(input.size(), array.size());

        for (int i = 0; i < input.size(); i++) {
            Object object = input.get(i);
            int indexExpected = input.indexOf(object);
            int indexReturned = array.indexOf(object);
            assertSame(indexExpected, indexReturned);
        }

        assertEquals(array.indexOf(null), -1);
    }

    /**
     * Since the shuffled mixed input is used, this is a
     * full coverage test that covers all possible cases
     * of the indexOf() method (nulls, duplicates, clones,
     * objects where equals() always returns false).
     */
    @Test
    public void testIndexOfForAllInputVariations() {
        List<Object> input = mShuffledMixedInput;
        Array<Object> array = newArray(input);
        assertEquals(input.size(), array.size());

        for (int i = 0; i < input.size(); i++) {
            Object object = input.get(i);
            int indexExpected = input.indexOf(object);
            int indexReturned = array.indexOf(object);
            assertSame(indexExpected, indexReturned);
        }
    }

    /**
     * This test only checks that the indexOf uses equals and not ==.
     */
    @Test
    public void testIndexOfUsesEqualsMethodAndNotSamenessOperator() {
        List<Object> input = mInputSameButNotEqual;
        Array<Object> array = newArray(input);
        assertEquals(input.size(), array.size());

        for (int i = 0; i < input.size(); i++) {
            Object object = input.get(i);

            // Sanity check - should be same but not equal.
            assertSame(object, object);
            assertNotEquals(object, object);

            // Now test for expected value.
            assertEquals(-1, array.indexOf(object));
        }

        assertEquals(array.indexOf(null), -1);
    }

    /**
     * Test indexOf() for false positives (matches).
     */
    @Test
    public void testIndexOfNonexistentObjects() {
        List<Object> input = mMixedInput;
        Array<Object> array = newArray(input);
        assertEquals(input.size(), array.size());

        // Note that nulls and ObjectIsEqualButIsNotSame objects will
        // match, so we ignore those when looking for false positives.
        List<Object> nonExistentInput = ArrayHelper.constructMixedInput();

        for (int i = 0; i < nonExistentInput.size(); i++) {
            Object object = nonExistentInput.get(i);
            int indexReturned = array.indexOf(object);
            if (object == null || object instanceof ArrayHelper.ObjectIsEqualButIsNotSame) {
                assertNotEquals(-1, indexReturned);
            } else {
                assertEquals(-1, indexReturned);
            }
        }
    }

    /**
     * Test add allAll(Collection) method.
     */

    @Test
    public void testAddAllCollectionNullPointerException() {
        mException.expect(NullPointerException.class);
        Array<Object> array = newArray();
        array.addAll((Collection) null);
    }

    @Test
    public void testAddAllCollectionToZeroSizedArray() {
        List<Object> input = mShuffledMixedInput;
        Array<Object> array = newArray(0);
        assertEquals(0, array.size());

        // The test
        assertTrue(array.addAll(input));

        assertEquals(input.size(), array.size());

        for (int i = 0; i < input.size(); i++) {
            Object object = input.get(i);
            assertSame(object, array.get(i));
        }
    }

    @Test
    public void testAddAllCollectionAppendsToEnd() {
        List<Object> input = mShuffledMixedInput;
        Array<Object> array = newArray(input);
        assertEquals(input.size(), array.size());

        List<Object> inputToAppend = ArrayHelper.constructShuffledMixedInput();

        // The test
        assertTrue(array.addAll(inputToAppend));

        // Match the change in our input for later comparison/verification.
        input.addAll(inputToAppend);
        assertEquals(input.size(), array.size());

        for (int i = 0; i < input.size(); i++) {
            Object object = input.get(i);
            assertSame(object, array.get(i));
        }
    }

    @Test
    public void testAddAllEmptyCollectionReturnsNotChanged() {
        List<Object> input = mShuffledMixedInput;
        Array<Object> array = newArray(input);
        assertEquals(input.size(), array.size());

        // Capture backing data array to ensure that it is not
        // modified when addAll is called with an empty collection.
        Object[] elementsBefore = ArrayHelper.getElements(array);
        int expectedLength = elementsBefore.length;

        List<Object> emptyInput = new ArrayList<>(10);

        // The test
        assertFalse(array.addAll(emptyInput));

        // Nothing should change
        assertEquals(input.size(), array.size());
        Object[] elementsAfter = ArrayHelper.getElements(array);

        // Should be the same backing object.
        assertSame(elementsBefore, elementsAfter);

        // Backing object length should not have changed.
        assertEquals(expectedLength, elementsAfter.length);
    }

    /**
     * Test add allAll(Array) method.
     */

    @Test
    public void testAddAllArrayNullPointerException() {
        mException.expect(NullPointerException.class);
        Array<Object> array = newArray();
        array.addAll((Array<Object>) null);
    }

    @Test
    public void testAddAllArrayToZeroSizedArray() {
        List<Object> input = mShuffledMixedInput;
        Array<Object> array = newArray(0);
        assertEquals(0, array.size());

        array.addAll(input);

        assertEquals(input.size(), array.size());

        for (int i = 0; i < input.size(); i++) {
            Object object = input.get(i);
            assertSame(object, array.get(i));
        }
    }

    @Test
    public void testAddAllArrayAppendsToEnd() {
        List<Object> input = mShuffledMixedInput;
        Array<Object> array = newArray(input);
        assertEquals(input.size(), array.size());

        List<Object> inputToAppend = ArrayHelper.constructShuffledMixedInput();
        array.addAll(inputToAppend);

        input.addAll(inputToAppend);
        assertEquals(input.size(), array.size());

        for (int i = 0; i < input.size(); i++) {
            Object object = input.get(i);
            assertSame(object, array.get(i));
        }
    }

    @Test
    public void testAddAllEmptyArrayReturnsNotChanged() {
        List<Object> input = mShuffledMixedInput;
        Array<Object> array = newArray(input);
        assertEquals(input.size(), array.size());

        // Capture backing data array to ensure that it is not
        // modified when addAll is called with an empty collection.
        Object[] elementsBefore = ArrayHelper.getElements(array);
        int expectedLength = elementsBefore.length;

        Array<Object> emptyInput = newArray(10);

        // The test
        assertFalse(array.addAll(emptyInput));

        // Nothing should change
        assertEquals(input.size(), array.size());
        Object[] elementsAfter = ArrayHelper.getElements(array);

        // Should be the same backing object.
        assertSame(elementsBefore, elementsAfter);

        // Backing object length should not have changed.
        assertEquals(expectedLength, elementsAfter.length);
    }

    /**
     * Test remove method.
     */

    @Test
    public void testRemove() {
        List<Object> input = mShuffledMixedInput;
        Array<Object> arraySpy = spy(newArray(input));
        assertEquals(input.size(), arraySpy.size());

        Random random = new Random();
        while (input.size() > 0) {
            // Reset spy so that we can match a single call to rangeCheck().
            //noinspection unchecked
            reset(arraySpy);

            int index = random.nextInt(input.size());
            Object expectedObject = input.remove(index);
            Object returnedObject = arraySpy.remove(index);

            assertEquals(input.size(), arraySpy.size());
            assertSame(expectedObject, returnedObject);
            assertEquals(input.size(), arraySpy.size());

            // Check for the required rangeCheck call.
            verify(arraySpy, times(1)).rangeCheck(index);

            for (int i = 0; i < input.size(); i++) {
                assertSame(input.get(i), arraySpy.get(i));
            }
        }

        assertEquals(0, input.size());
        assertEquals(0, arraySpy.size());
    }

    @Test
    public void testRemoveFromEmptyArray() {
        mException.expect(IndexOutOfBoundsException.class);
        (newArray()).remove(0);
    }

    @Test
    public void testRemoveIndexBelowLowerBound() {
        mException.expect(IndexOutOfBoundsException.class);
        (newArray(mMixedInput)).remove(-1);
    }

    @Test
    public void testRemoveIndexAboveUpperBound() {
        mException.expect(IndexOutOfBoundsException.class);
        (newArray(mMixedInput)).remove(mMixedInput.size());
    }

    /**
     * Test get method.
     */

    @Test
    public void testGet() {
        List<Object> input = mShuffledMixedInput;
        Array<Object> arraySpy = spy(newArray(input));
        assertEquals(input.size(), arraySpy.size());

        for (int i = 0; i < input.size(); i++) {
            Object expected = input.get(i);
            Object result = arraySpy.get(i);
            assertSame(expected, result);
        }

        // Check for the required number of rangeCheck call.
        verify(arraySpy, times(input.size())).rangeCheck(anyInt());
    }

    @Test
    public void testGetFromEmptyArray() {
        mException.expect(IndexOutOfBoundsException.class);
        (newArray()).get(0);
    }

    @Test
    public void testGetIndexBelowLowerBound() {
        mException.expect(IndexOutOfBoundsException.class);
        (newArray(mMixedInput)).get(-1);
    }

    @Test
    public void testGetIndexAboveUpperBound() {
        mException.expect(IndexOutOfBoundsException.class);
        (newArray(mMixedInput)).get(mMixedInput.size());
    }

    /**
     * Test set method.
     */

    @Test
    public void testSet() {
        List<Object> input = ArrayHelper.constructShuffledMixedInput();
        List<Object> pool = ArrayHelper.constructShuffledMixedInput();

        Array<Object> arraySpy = spy(newArray(input));
        assertEquals(input.size(), arraySpy.size());

        for (int i = 0; i < input.size(); i++) {
            Object setObject = pool.get(i);
            Object expectedOldValue = input.set(i, setObject);
            Object returnedOldValue = arraySpy.set(i, setObject);
            assertSame(expectedOldValue, returnedOldValue);
            ArrayHelper.assertSameContentsAndLength(
                    input.toArray(), ArrayHelper.getElements(arraySpy));
        }

        ArrayHelper.assertSameContentsAndLength(
                pool.toArray(), ArrayHelper.getElements(arraySpy));

        // Check for the required number of rangeCheck calls.
        verify(arraySpy, times(pool.size())).rangeCheck(anyInt());
    }

    @Test
    public void testSetFromEmptyArray() {
        mException.expect(IndexOutOfBoundsException.class);
        (newArray()).set(0, null);
    }

    @Test
    public void testSetIndexBelowLowerBound() {
        mException.expect(IndexOutOfBoundsException.class);
        (newArray(mMixedInput)).set(-1, null);
    }

    @Test
    public void testSetIndexAboveUpperBound() {
        mException.expect(IndexOutOfBoundsException.class);
        (newArray(mMixedInput)).set(mMixedInput.size(), null);
    }

    /**
     * Test ensureCapacity method.
     */

    @Test
    public void testEnsureInternalCapacityWhenNoChangeIsRequired() {
        Array<Object> array = newArray();

        // First call to ensure an initial capacity.
        array.ensureCapacityInternal(1);
        Object[] expected = ArrayHelper.getElements(array);
        assertTrue(expected.length >= 1);

        // Second call with same capacity should produce no change.
        array.ensureCapacityInternal(1);
        Object[] objects = ArrayHelper.getElements(array);

        // Backing data should not have changed in any way.
        assertSame(expected, objects);

        // Array size should also remain at 0.
        assertEquals(0, array.size());
    }

    @Test
    public void testEnsureInternalCapacityWith1to100() {
        Array<Object> array = newArray();

        int maxCapacity = 100;
        for (int i = 0; i < maxCapacity; i++) {
            // Test call.
            array.ensureCapacityInternal(i);
            Object[] expectedElementsArray = ArrayHelper.getElements(array);
            assertTrue(expectedElementsArray.length >= i);
            assertEquals(0, array.size());

            // Backing array should not change when subsequent calls
            // to ensureCapacityInternal() are called with sizes less
            // than the current capacity.
            for (int j = 0; j <= i; j++) {
                // Test call.
                array.ensureCapacityInternal(j);
                Object[] elementsArray = ArrayHelper.getElements(array);
                assertSame(expectedElementsArray, elementsArray);
                assertEquals(0, array.size());
            }
        }
    }

    @Test
    public void testEnsureInternalCapacityIsEfficient() {
        Array<Object> array = newArray();

        int allocs = 0;

        for (int i = 0; i < 1000; i++) {
            Object[] oldData = ArrayHelper.getElements(array);
            array.ensureCapacityInternal(i);
            Object[] newData = ArrayHelper.getElements(array);
            if (oldData != newData) {
                allocs++;
            }
        }

        assertTrue(allocs + " memory allocations occurred for parameters [1..1000]; " +
                        "allocation algorithm needs to be more efficient!",
                allocs < 14);

        assertTrue(allocs + " memory allocation(s) occurred for parameters [1..1000]; " +
                        "allocation algorithm needs to be more efficient!",
                allocs > 1);
    }

    @Test
    public void testEnsureInternalCapacityCopiesData() {
        List<Object> input = mShuffledMixedInput;
        Array<Object> array = newArray(input);
        ArrayHelper.assertSameContents(input.toArray(), array.toArray());

        for (int i = 0; i < 1000; i++) {
            Object[] oldData = ArrayHelper.getElements(array);
            array.ensureCapacityInternal(i);
            Object[] newData = ArrayHelper.getElements(array);
            if (oldData != newData) {
                ArrayHelper.assertSameContents(input.toArray(), newData);
                break;
            }
        }
    }

    @Test
    public void testAdd() {
        List<Object> expected = mShuffledMixedInput;
        Array<Object> array = spy(newArray(expected));

        ArrayHelper.assertSameContents(expected.toArray(), array.toArray());

        int addCalls = 0;

        for (int i = 0; i < 100; i++) {
            List<Object> newObjects = ArrayHelper.constructShuffledMixedInput();

            for (Object object: newObjects) {
                expected.add(object);
                array.add(object);
                addCalls++;
            }

            Object[] elements = ArrayHelper.getElements(array);
            ArrayHelper.assertSameContents(expected.toArray(), elements);
            assertEquals(expected.size(), array.size());
        }

        verify(array, times(addCalls)).ensureCapacityInternal(anyInt());
    }

    @Test
    public void testForEachUndergraduate() {
        if (!Assignment.testType(Assignment.UNDERGRADUATE)) {
            return;
        }

        Array<Object> original = newArray(mShuffledMixedInput);
        assertArrayEquals(mShuffledMixedInput.toArray(), original.toArray());

        Array<Object> a1 = spy(newArray(mShuffledMixedInput));
        assertArrayEquals(mShuffledMixedInput.toArray(), original.toArray());

        Array<Object> a2 = newArray();
        assertNotNull(ArrayHelper.getElements(a2));

        a1.forEach(a2::add);

        // Ensure that a1 hasn't changed.
        ArrayHelper.assertArrayEquals(original, a1);

        // a2 should have the same contents and size as a1.
        ArrayHelper.assertArrayEquals(a1, a2);

        // Should use iterator for implementation.
        verify(a1, times(1)).iterator();

        // Should not call stream helper.
        verify(a1, never()).stream();
    }

    @Test
    public void testForEachGraduate() {
        if (!Assignment.testType(Assignment.GRADUATE)) {
            return;
        }

        Array<Object> original = newArray(mShuffledMixedInput);
        Array<Object> a1 = spy(newArray(mShuffledMixedInput));
        Array<Object> a2 = newArray();

        a1.forEach(a2::add);

        // Ensure that a1 hasn't changed.
        ArrayHelper.assertArrayEquals(original, a1);

        // a2 should have the same contents and size as a1.
        ArrayHelper.assertArrayEquals(a1, a2);

        // Should call stream helper once.
        verify(a1, times(1)).stream();

        // Should use an for loop and not an inefficient iterator.
        verify(a1, never()).iterator();
    }

    @Test
    public void testReplaceAll() {
        List<Long> expected = LongStream.rangeClosed(1, 20).boxed().collect(toList());
        Array<Long> array = newArray(expected);

        // Mimic with ArrayList.
        expected.replaceAll(i -> i + 1);

        // Test method.
        array.replaceAll(i -> i + 1);

        assertArrayEquals(expected.toArray(), array.toArray());
    }

    /**
     * Test iterator.
     */

    @Test
    public void testIteratorWhenEmpty() {
        Array<Object> array = newArray();
        Iterator<Object> iterator = array.iterator();
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testIteratorWhenNotEmpty() {
        List<Object> input = mShuffledMixedInput;
        Array<Object> array = newArray(input);

        Iterator<Object> iterator = array.iterator();
        Iterator<Object> expected = input.iterator();

        while (expected.hasNext() && iterator.hasNext()) {
            assertSame(expected.next(), iterator.next());
        }

        assertEquals(expected.hasNext(), iterator.hasNext());

        mException.expect(NoSuchElementException.class);
        iterator.next();
    }

    @Test
    public void testIteratorThrowsNoSuchElementExceptionOnEmptyArray() {
        Array<Object> array = newArray();
        mException.expect(NoSuchElementException.class);
        array.iterator().next();
    }

    @Test
    public void testIteratorThrowsNoSuchElementExceptionPastEndOfArray() {
        List<Object> input = mShuffledMixedInput;
        Array<Object> array = newArray(input);
        Iterator<Object> iterator = array.iterator();

        for (int i = 0; i < input.size(); i++) {
            assertTrue(iterator.hasNext());
            iterator.next();
        }

        assertFalse(iterator.hasNext());

        mException.expect(NoSuchElementException.class);
        iterator.next();
    }

    @Test
    public void testIteratorWithAddAndSet() {
        List<Object> input = new ArrayList<>();
        List<Object> pool = ArrayHelper.constructShuffledMixedInput();

        Array<Object> array = newArray();

        // Start with empty array.
        assertEquals(input.iterator().hasNext(), array.iterator().hasNext());

        // Start with empty array.
        ArrayHelper.assertSameIteratorContents(input.iterator(), array.iterator());

        for (Object poolObject : pool) {
            // Test iterator after add operation.

            Object tempObject = new Object();
            input.add(tempObject);
            array.add(tempObject);

            ArrayHelper.assertSameIteratorContents(input.iterator(), array.iterator());
            ArrayHelper.assertSameContents(input.toArray(), array.toArray());

            // Test iterator after set operation.

            int lastIndex = input.size() - 1;
            input.set(lastIndex, poolObject);
            array.set(lastIndex, poolObject);

            ArrayHelper.assertSameIteratorContents(input.iterator(), array.iterator());
            ArrayHelper.assertSameContents(input.toArray(), array.toArray());
        }

        // Test iterator after removing items.

        Random random = new Random();

        while (input.size() > 0) {
            // Test iterator after add operation.

            int randomIndex = random.nextInt(input.size());
            input.remove(randomIndex);
            array.remove(randomIndex);

            ArrayHelper.assertSameIteratorContents(input.iterator(), array.iterator());
        }

        mException.expect(NoSuchElementException.class);
        array.iterator().next();
    }

    @Test
    public void testIteratorCanRemoveAllElements() {
        List<Object> input = mShuffledMixedInput;
        Array<Object> array = newArray(input);

        ArrayHelper.assertSameIteratorContents(input.iterator(), array.iterator());

        Iterator<Object> expected = input.iterator();
        Iterator<Object> iterator = array.iterator();

        while (expected.hasNext() && iterator.hasNext()) {
            Object objectExpected = expected.next();
            Object object = iterator.next();
            assertSame(objectExpected, object);
            expected.remove();
            iterator.remove();
            ArrayHelper.assertSameContents(input.toArray(), array.toArray());
            assertEquals(input.size(), array.size());
        }

        assertEquals(expected.hasNext(), iterator.hasNext());
        assertEquals(0, input.size());
        assertEquals(0, array.size());
    }

    /**
     * Test Spliterator.
     */

    @Test
    public void runSpliteratorTest19() {
        if (!Assignment.testType(Assignment.GRADUATE)) {
            System.out.println("Skipping Spliterator test (graduate only test)");
            return;
        }

        Array<Long> array = newArray();

        Long factorialOf19 = 121645100408832000L;

        for (long i = 1; i <= 19; i++) {
            array.add(i);
        }

        Long f1 = array
                // Convert to a sequential stream.
                .stream()

                // Perform a reduction.
                .reduce(1L, (x, y) -> (x * y));

        assertEquals(factorialOf19, f1);

        Long f2 = array
                // Convert to a parallel stream.
                .parallelStream()

                // Perform a reduction.
                .reduce(1L, (x, y) -> (x * y));

        assertEquals(factorialOf19, f2);
    }

    @Test
    public void runSpliteratorTest20() {
        if (!Assignment.testType(Assignment.GRADUATE)) {
            System.out.println("Skipping Spiterator test (graduate only test)");
            return;
        }

        Array<Long> array = newArray();

        Long factorialOf20 = 2432902008176640000L;

        for (long i = 1; i <= 20; i++)
            array.add(i);

        Long f1 = array
                // Convert to a parallel stream.
                .stream()

                // Perform a reduction.
                .reduce(1L, (x, y) -> (x * y));

        assertEquals(factorialOf20, f1);

        Long f2 = array
                // Convert to a parallel stream.
                .parallelStream()

                // Perform a reduction.
                .reduce(1L, (x, y) -> (x * y));

        assertEquals(factorialOf20, f2);
    }

    private <T> Array<T> newArray(List<T> input) {
        return new UnsynchronizedArray<>(input);
    }

    private <T> Array<T> newArray() {
        return new UnsynchronizedArray<>();
    }

    private <T> Array<T> newArray(int size) {
        return new UnsynchronizedArray<>(size);
    }
}
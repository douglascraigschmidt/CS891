package edu.vanderbilt.imagecrawler.utils

import admin.ArrayHelper
import admin.AssignmentTests
import admin.ReflectionHelper
import org.junit.Assert.*
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.*
import java.util.*

/**
 * Test program for the Java 7 features of the Array class.
 * Note that since the sample input data contains objects whose
 * equals() methods always returns false, care must be used to
 * use assertSame() instead of assertEquals() where necessary.
 */
class UnsynchronizedArrayTests : AssignmentTests() {
    /**
     * Maximum test size (must be an even value).
     */
    private val mMaxTestSize = 20

    /**
     * A list containing:
     *
     * 1. Unique Object instances.
     * 2. Duplicate Object instances that will return true when
     *    compared with both equals() and ==
     * 3. Two or more duplicated ObjectIsSameButNotEqual instances
     *    which is a special class I've made that always returns
     *    false for equals() and true for ==. These objects will
     *    help detect cases where == is used instead of equals().
     * 4. One or more null values.
     * 5. Two or more ObjectIsEqualButNotSame instances that will
     *    return true when compared with equals() but false when
     *    compared with ==.
     */
    private val mixedInput = ArrayHelper.constructMixedInput()

    /**
     * A list with a similar content to mMixedInput but with all
     * entries shuffled.
     */
    private val shuffledMixedInput = ArrayHelper.constructShuffledMixedInput()

    /**
     * A special list to ensure that Array implementation always uses
     * equals() method for comparisons and not the sameness operator ==.
     *
     * It contains:
     *
     * Two or more ObjectIsEqualButNotSame instances that will
     * return true when compared with equals() but false when
     * compared with ==.
     */
    private val mInputSameButNotEqual = ArrayHelper.constructInputWithObjectsSameButNotEqual()

    /**
     * A list of mMaxTestSize random integers (with no duplicates).
     */
    private val mInputWithNoDups = ArrayHelper.getInputNoDups(mMaxTestSize)

    @Test
    fun `constructor must handle all valid capacity inputs`() {
        for (i in 0..mMaxTestSize) {
            val array = newArray<Int>(i)
            val data = ArrayHelper.getElements(array)
            assertNotNull(data)
            assertEquals(data.size, i)
            assertEquals(0, array.size())
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
    fun `Assignment types field must be set`() {
        // At least one todo type should be a member of the types set (possibly both).
        assertTrue(Assignment.isGraduateTodo() || Assignment.isUndergraduateTodo())
    }

    /**
     * This test should always pass, even on an empty skeleton since it's purpose
     * is to ensure that the student does not change or alter the default constructor.
     */
    @Test
    fun `test default constructor`() {
        val array = newArray<Any>()
        val elements = ArrayHelper.getElements(array)
        assertNotNull(elements)

        val expected = ReflectionHelper.findFirstMatchingFieldValue<kotlin.Array<Any>>(
                array, kotlin.Array<Any>::class.java, "EMPTY_ELEMENTDATA")

        assertSame(expected, elements)
        assertEquals(0, elements.size)
        assertEquals(0, array.size())
    }

    /**
     * Test constructors.
     */
    @Test
    fun `constructor must handle non-empty collection input`() {
        val input = shuffledMixedInput
        val array = newArray(input)
        val data = ArrayHelper.getElements(array)

        assertNotNull(data)
        assertEquals(input.size, data.size)
        assertEquals(input.size, array.size())
        for (i in input.indices) {
            // Use same() and not equals() to ensure that
            // array has the same objects and not clones.
            assertSame(input[i], data[i])
        }
    }

    @Test
    fun `constructor must handle empty collection input`() {
        val emptyCollection = ArrayList<Any>(0)
        val array = newArray(emptyCollection)
        val data = ArrayHelper.getElements(array)

        assertNotNull(data)
        assertEquals(0, data.size)
        assertEquals(0, array.size())
    }

    @Test
    fun `constructor must handle an invalid capacity input`() {
        exception.expect(IllegalArgumentException::class.java)
        newArray<Any>(-1)
    }

    @Test
    fun `constructor must handle an invalid collection input`() {
        exception.expect(NullPointerException::class.java)
        newArray<Any>(null)
    }

    /**
     * Test size method.
     */

    @Test
    fun `size() black box test`() {
        val input = shuffledMixedInput
        val array = newArray(input)
        assertEquals(input.size, array.size())
    }

    /**
     * This test will catch cases where mElementData is
     * needlessly referenced in size() method.
     */
    @Test
    @Throws(Exception::class)
    fun `size() method must only use size property`() {
        val input = shuffledMixedInput
        val array = newArray(input)
        ReflectionHelper.injectValueIntoMatchingField(
                array, null, Array::class.java, "mElementData")
        ReflectionHelper.injectValueIntoMatchingField(
                array, 1, Int::class.javaPrimitiveType, "mSize")
        assertEquals(1, array.size())
    }

    /**
     * Test isEmpty method.
     */

    @Test
    fun `isEmpty() must return true when array has a zero capacity`() {
        val array = newArray<Any>(0)
        // First make sure that constructor actually worked as expected.
        assertNotNull(ArrayHelper.getElements(array))
        assertEquals(0, ArrayHelper.getElements(array).size)

        // Test method.
        assertTrue(array.isEmpty)
    }

    @Test
    fun `isEmpty() should return true when a the capacity is greater than 0 but no elements exist`() {
        val array = newArray<Any>(1)
        assertEquals(1, ArrayHelper.getElements(array).size)

        // Test method.
        assertTrue(array.isEmpty)
    }

    /**
     * This test will catch cases where mElementData is
     * needlessly referenced in isEmpty() method.
     */
    @Test
    @Throws(Exception::class)
    fun `isEmpty() white box test`() {
        val input = shuffledMixedInput
        val array = newArray(input)
        assertEquals(input.size, array.size())

        ReflectionHelper.injectValueIntoMatchingField(
                array, null, Array::class.java, "mElementData")
        ReflectionHelper.injectValueIntoMatchingField(
                array, 1, Int::class.javaPrimitiveType, "mSize")
        assertFalse(array.isEmpty)
    }

    /**
     * Test rangeCheck method
     */

    @Test
    @Throws(IllegalAccessException::class)
    fun `rangeCheck() should only use size member`() {
        val fakeSize = 10
        val array = newArray<Any>(fakeSize)
        assertEquals(10, ArrayHelper.getElements(array).size)

        // Set size to an arbitrary value even though there
        // are no backing elements in this empty array; rangeCheck
        // should not only care about mSize and not the contents of
        // the backing element data.
        ArrayHelper.setSize(array, fakeSize)

        for (i in 0 until fakeSize) {
            // Should never throw an exception.
            array.rangeCheck(i)
        }
    }

    @Test
    fun `rangeCheck() should throw IndexOutOfBoundsException when array is empty`() {
        exception.expect(IndexOutOfBoundsException::class.java)
        newArray<Any>().rangeCheck(0)
    }

    @Test
    fun `rangeCheck() should throw IndexOutOfBounds exception for a negative index`() {
        exception.expect(IndexOutOfBoundsException::class.java)
        newArray(mixedInput).rangeCheck(-1)
    }

    @Test
    fun `rangeCheck() should throw IndexOutOfBounds exception for an index that exceeds the upper bound`() {
        exception.expect(IndexOutOfBoundsException::class.java)
        newArray(mixedInput).rangeCheck(mixedInput.size)
    }

    /**
     * Test indexOf method
     */

    @Test
    fun `indexOf() black box test`() {
        val input = mInputWithNoDups
        val array = newArray(input)
        assertEquals(input.size, array.size())

        for (i in input.indices) {
            val obj = input[i]
            val indexExpected = input.indexOf(obj)
            val indexReturned = array.indexOf(obj)
            assertSame(indexExpected, indexReturned)
        }

        assertEquals(array.indexOf(null), -1)
    }

    /**
     * Since the shuffled mixed input is used, this is a
     * full coverage test that covers all possible cases
     * of the indexOf() method (nulls, duplicates, clones,
     * objects where equals() always returns false).
     */
    @Test
    fun `indexOf() must handle all possible input variations`() {
        val input = shuffledMixedInput
        val array = newArray(input)
        assertEquals(input.size, array.size())

        for (i in input.indices) {
            val obj = input[i]
            val indexExpected = input.indexOf(obj)
            val indexReturned = array.indexOf(obj)
            assertSame(indexExpected, indexReturned)
        }
    }

    /**
     * This test only checks that the indexOf uses equals and not ==.
     */
    @Test
    fun `indexOf() must use equals() and not == operator`() {
        val input = mInputSameButNotEqual
        val array = newArray(input)
        assertEquals(input.size, array.size())

        for (i in input.indices) {
            val obj = input[i]

            // Sanity check - should be same but not equal.
            assertSame(obj, obj)
            assertNotEquals(obj, obj)

            // Now test for expected value.
            assertEquals(-1, array.indexOf(obj))
        }

        assertEquals(array.indexOf(null), -1)
    }

    /**
     * Test indexOf() for false positives (matches).
     */
    @Test
    fun `indexOf() must only return -1 for input objects that don't exist in the array`() {
        val input = mixedInput
        val array = newArray(input)
        assertEquals(input.size, array.size())

        // Note that nulls and ObjectIsEqualButIsNotSame objects will
        // match, so we ignore those when looking for false positives.
        val nonExistentInput = ArrayHelper.constructMixedInput()

        for (i in nonExistentInput.indices) {
            val obj = nonExistentInput[i]
            val indexReturned = array.indexOf(obj)
            if (obj == null || obj is ArrayHelper.ObjectIsEqualButIsNotSame) {
                assertNotEquals(-1, indexReturned)
            } else {
                assertEquals(-1, indexReturned)
            }
        }
    }

    /**
     * Test add addAll(Collection) method.
     */

    @Test
    fun `addAll(Collection) must throw a NullPointerException when input is a null collection`() {
        exception.expect(NullPointerException::class.java)
        val array = newArray<Any>()
        array.addAll(null as Collection<*>?)
    }

    @Test
    fun `addAll(Collection) must handle a mixed input collection`() {
        val input = shuffledMixedInput
        val array = newArray<Any>(0)
        assertEquals(0, array.size())

        // The test
        assertTrue(array.addAll(input))

        assertEquals(input.size, array.size())

        for (i in input.indices) {
            val obj = input[i]
            assertSame(obj, array.get(i))
        }
    }

    @Test
    fun `addAll(Collection) must append an input collection to the end of the array`() {
        val input = shuffledMixedInput
        val array = newArray(input)
        assertEquals(input.size, array.size())

        val inputToAppend = ArrayHelper.constructShuffledMixedInput()

        // The test
        assertTrue(array.addAll(inputToAppend))

        // Match the change in our input for later comparison/verification.
        input.addAll(inputToAppend)
        assertEquals(input.size, array.size())

        for (i in input.indices) {
            val obj = input[i]
            assertSame(obj, array.get(i))
        }
    }

    @Test
    fun `addAll(Collection) must not change array when input is an empty collection`() {
        val input = shuffledMixedInput
        val array = newArray(input)
        assertEquals(input.size, array.size())

        // Capture backing data array to ensure that it is not
        // modified when addAll is called with an empty collection.
        val elementsBefore = ArrayHelper.getElements(array)
        val expectedLength = elementsBefore.size

        val emptyInput = ArrayList<Any>(10)

        // The test
        assertFalse(array.addAll(emptyInput))

        // Nothing should change
        assertEquals(input.size, array.size())
        val elementsAfter = ArrayHelper.getElements(array)

        // Should be the same backing object.
        assertSame(elementsBefore, elementsAfter)

        // Backing object length should not have changed.
        assertEquals(expectedLength, elementsAfter.size)
    }

    /**
     * Test add addAll(Array) method.
     */

    @Test
    fun `addAll(Array) must throw a NullPointerException when input is a null Array`() {
        exception.expect(NullPointerException::class.java)
        val array = newArray<Any>()
        array.addAll(null as Array<Any>?)
    }

    @Test
    fun `addAll(Array) must handle a mixed input Array`() {
        val input = shuffledMixedInput
        val array = newArray<Any>(0)
        assertEquals(0, array.size())

        // The test
        assertTrue(array.addAll(input))

        assertEquals(input.size, array.size())

        for (i in input.indices) {
            val obj = input[i]
            assertSame(obj, array.get(i))
        }
    }

    @Test
    fun `addAll(Array) must append input to the end of the array`() {
        val input = shuffledMixedInput
        val array = newArray(input)
        assertEquals(input.size, array.size())

        val inputToAppend = ArrayHelper.constructShuffledMixedInput()
        array.addAll(inputToAppend)

        input.addAll(inputToAppend)
        assertEquals(input.size, array.size())

        for (i in input.indices) {
            val obj = input[i]
            assertSame(obj, array.get(i))
        }
    }

    @Test
    fun `addAll(Array) must not change array when input is an empty array`() {
        val input = shuffledMixedInput
        val array = newArray(input)
        assertEquals(input.size, array.size())

        // Capture backing data array to ensure that it is not
        // modified when addAll is called with an empty collection.
        val elementsBefore = ArrayHelper.getElements(array)
        val expectedLength = elementsBefore.size

        val emptyInput = newArray<Any>(10)

        // The test
        assertFalse(array.addAll(emptyInput))

        // Nothing should change
        assertEquals(input.size, array.size())
        val elementsAfter = ArrayHelper.getElements(array)

        // Should be the same backing object.
        assertSame(elementsBefore, elementsAfter)

        // Backing object length should not have changed.
        assertEquals(expectedLength, elementsAfter.size)
    }

    /**
     * Test remove method.
     */

    @Test
    fun `test remove() method`() {
        val input = shuffledMixedInput
        val arraySpy = spy(newArray(input))
        assertEquals(input.size, arraySpy.size())

        val random = Random()
        while (input.size > 0) {
            // Reset spy so that we can match a single call to rangeCheck().

            reset(arraySpy)

            val index = random.nextInt(input.size)
            val expectedObject = input.removeAt(index)
            val returnedObject = arraySpy.remove(index)

            assertEquals(input.size, arraySpy.size())
            assertSame(expectedObject, returnedObject)
            assertEquals(input.size, arraySpy.size())

            // Check for the required rangeCheck call.
            verify(arraySpy, times(1)).rangeCheck(index)

            for (i in input.indices) {
                assertSame(input[i], arraySpy.get(i))
            }
        }

        assertEquals(0, input.size)
        assertEquals(0, arraySpy.size())
    }

    @Test
    fun `remove() must throw IndexOutOfBoundsException when removing from an empty array`() {
        exception.expect(IndexOutOfBoundsException::class.java)
        newArray<Any>().remove(0)
    }

    @Test
    fun `remove() must throw IndexOutOfBoundsException when input index is negative`() {
        exception.expect(IndexOutOfBoundsException::class.java)
        newArray(mixedInput).remove(-1)
    }

    @Test
    fun `remove() must throw IndexOutOfBoundsException when input index exceeds array upper bound`() {
        exception.expect(IndexOutOfBoundsException::class.java)
        newArray(mixedInput).remove(mixedInput.size)
    }

    /**
     * Test get method.
     */

    @Test
    fun `test get() method`() {
        val input = shuffledMixedInput
        val arraySpy = spy(newArray(input))
        assertEquals(input.size, arraySpy.size())

        for (i in input.indices) {
            val expected = input[i]
            val result = arraySpy.get(i)
            assertSame(expected, result)
        }

        // Check for the required number of rangeCheck call.
        verify(arraySpy, times(input.size)).rangeCheck(anyInt())
    }

    @Test
    fun `get() must throw IndexOutOfBoundsException when array is empty`() {
        exception.expect(IndexOutOfBoundsException::class.java)
        newArray<Any>().get(0)
    }

    @Test
    fun `get() must throw IndexOutOfBoundsException when input index is negative`() {
        exception.expect(IndexOutOfBoundsException::class.java)
        newArray(mixedInput).get(-1)
    }

    @Test
    fun `get() must throw IndexOutOfBoundsException when input index exceeds array upper bound`() {
        exception.expect(IndexOutOfBoundsException::class.java)
        newArray(mixedInput).get(mixedInput.size)
    }

    /**
     * Test set method.
     */

    @Test
    fun `test set() method`() {
        val input = ArrayHelper.constructShuffledMixedInput()
        val pool = ArrayHelper.constructShuffledMixedInput()

        val arraySpy = spy(newArray(input))
        assertEquals(input.size, arraySpy.size())

        for (i in input.indices) {
            val setObject = pool[i]
            val expectedOldValue = input.set(i, setObject)
            val returnedOldValue = arraySpy.set(i, setObject)
            assertSame(expectedOldValue, returnedOldValue)
            ArrayHelper.assertSameContentsAndLength(
                    input.toTypedArray(), ArrayHelper.getElements(arraySpy))
        }

        ArrayHelper.assertSameContentsAndLength(
                pool.toTypedArray(), ArrayHelper.getElements(arraySpy))

        // Check for the required number of rangeCheck calls.
        verify(arraySpy, times(pool.size)).rangeCheck(anyInt())
    }

    @Test
    fun `set() must throw IndexOutOfBoundsException when array is empty`() {
        exception.expect(IndexOutOfBoundsException::class.java)
        newArray<Any>().set(0, null)
    }

    @Test
    fun `set() must throw IndexOutOfBoundsException when input index is negative`() {
        exception.expect(IndexOutOfBoundsException::class.java)
        newArray(mixedInput).set(-1, null)
    }

    @Test
    fun `set() must throw IndexOutOfBoundsException when input index exceeds array upper bound`() {
        exception.expect(IndexOutOfBoundsException::class.java)
        newArray(mixedInput).set(mixedInput.size, null)
    }

    /**
     * Test ensureCapacity method.
     */

    @Test
    fun `ensureInternalCapacity() should only reallocate data members when absolutely necessary`() {
        val array = newArray<Any>()

        // First call to ensure an initial capacity.
        array.ensureCapacityInternal(1)
        val expected = ArrayHelper.getElements(array)
        assertTrue(expected.isNotEmpty())

        // Second call with same capacity should produce no change.
        array.ensureCapacityInternal(1)
        val objects = ArrayHelper.getElements(array)

        // Backing data should not have changed in any way.
        assertSame(expected, objects)

        // Array size should also remain at 0.
        assertEquals(0, array.size())
    }

    @Test
    fun `ensureInternalCapacity() should efficiently handle capacity input in range 1 to 100`() {
        val array = newArray<Any>()

        val maxCapacity = 100
        for (i in 0 until maxCapacity) {
            // Test call.
            array.ensureCapacityInternal(i)
            val expectedElementsArray = ArrayHelper.getElements(array)
            assertTrue(expectedElementsArray.size >= i)
            assertEquals(0, array.size())

            // Backing array should not change when subsequent calls
            // to ensureCapacityInternal() are called with sizes less
            // than the current capacity.
            for (j in 0..i) {
                // Test call.
                array.ensureCapacityInternal(j)
                val elementsArray = ArrayHelper.getElements(array)
                assertSame(expectedElementsArray, elementsArray)
                assertEquals(0, array.size())
            }
        }
    }

    @Test
    fun `ensureInternalCapacity() must allocate memory efficiently`() {
        val array = newArray<Any>()
        val minAllocs = 2
        val maxAllocs = 13

        var allocs = 0

        for (i in 0..999) {
            val oldData = ArrayHelper.getElements(array)
            array.ensureCapacityInternal(i)
            val newData = ArrayHelper.getElements(array)
            if (oldData !== newData) {
                allocs++
            }
        }

        assertTrue("$allocs memory allocation(s) occurred for parameters [1..1000]; " +
                "allocation algorithm should perform more memory allocations!",
                allocs >= minAllocs)

        assertTrue("$allocs memory allocations occurred for parameters [1..1000]; " +
                "allocation algorithm should be more efficient (maximum of $maxAllocs allocations)!",
                allocs <= maxAllocs)
    }

    @Test
    fun `ensureInternalCapacity() must always copy input data reallocating storage`() {
        val input = shuffledMixedInput
        val array = newArray(input)
        ArrayHelper.assertSameContents(input.toTypedArray(), array.toArray())

        for (i in 0..999) {
            val oldData = ArrayHelper.getElements(array)
            array.ensureCapacityInternal(i)
            val newData = ArrayHelper.getElements(array)
            if (oldData !== newData) {
                ArrayHelper.assertSameContents(input.toTypedArray(), newData)
                break
            }
        }
    }

    @Test
    fun `test add() method`() {
        val expected = shuffledMixedInput
        val array = spy(newArray(expected))

        ArrayHelper.assertSameContents(expected.toTypedArray(), array.toArray())

        var addCalls = 0

        for (i in 0..99) {
            val newObjects = ArrayHelper.constructShuffledMixedInput()

            for (obj in newObjects) {
                expected.add(obj)
                array.add(obj)
                addCalls++
            }

            val elements = ArrayHelper.getElements(array)
            ArrayHelper.assertSameContents(expected.toTypedArray(), elements)
            assertEquals(expected.size, array.size())
        }

        verify(array, times(addCalls)).ensureCapacityInternal(anyInt())
    }

    private fun <T> newArray(): Array<T> = UnsynchronizedArray()

    private fun <T> newArray(size: Int): Array<T> = UnsynchronizedArray(size)

    private fun <T> newArray(input: List<T>?): Array<T> = UnsynchronizedArray(input)
}
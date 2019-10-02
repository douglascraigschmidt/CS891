package edu.vanderbilt.imagecrawler.utils

import admin.AssignmentTests
import admin.ReflectionHelper
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnit

/**
 * Tests UnsynchronizedArray Spliterator inner class if base class [skipTest] flag is false.
 */
open class UnsynchronizedArraySpliteratorTests : AssignmentTests() {
    @Mock
    lateinit var mockArray: UnsynchronizedArray<Int>

    @Rule
    @JvmField
    var mockitoRule = MockitoJUnit.rule()!!

    @Test
    fun `tryAdvance with null parameter should throw NullPointerException`() {
        val array = newArray<Int>()
        val spliterator = array.spliterator()
        exception.expect(NullPointerException::class.java)
        spliterator.tryAdvance(null)
    }

    //TODO:monte finish this for 2a
    @Ignore
    fun `tryAdvance should call uncheckedToArray()`() {
        `when`(mockArray.spliterator()).thenCallRealMethod()
        `when`(mockArray.uncheckedToArray()).thenCallRealMethod()

//        ReflectionHelper.injectValueIntoMatchingField(
//                mockArray, null, Array::class.java, "mElementData")
//        ReflectionHelper.injectValueIntoMatchingField(
//                mockArray, 1, Int::class.javaPrimitiveType, "mSize")

        val spliterator = mockArray.spliterator()
        spliterator.tryAdvance(null)
    }

    @Test
    fun `Spliterator must properly handle sequential and parallel calculation of factorial of 19`() {
        if (!assignmentType(Assignment.GRADUATE)) return

        val array = newArray<Long>()

        val factorialOf19 = 121645100408832000L

        for (i in 1..19) {
            array.add(i.toLong())
        }

        val f1 = array
                // Convert to a sequential stream.
                .stream()

                // Perform a reduction.
                .reduce(1L) { x, y -> x * y }

        assertEquals(factorialOf19, f1)

        val f2 = array
                // Convert to a parallel stream.
                .parallelStream()

                // Perform a reduction.
                .reduce(1L) { x, y -> x * y }

        assertEquals(factorialOf19, f2)
    }

    @Test
    fun `Spliterator must properly handle sequential and parallel calculation of factorial of 20`() {
        if (!assignmentType(Assignment.GRADUATE)) return

        val array = newArray<Long>()

        val factorialOf20 = 2432902008176640000L

        for (i in 1..20)
            array.add(i.toLong())

        val f1 = array
                // Convert to a parallel stream.
                .stream()

                // Perform a reduction.
                .reduce(1L) { x, y -> x * y }

        assertEquals(factorialOf20, f1)

        val f2 = array
                // Convert to a parallel stream.
                .parallelStream()

                // Perform a reduction.
                .reduce(1L) { x, y -> x * y }

        assertEquals(factorialOf20, f2)
    }

    private fun <T> newArray(): Array<T> = UnsynchronizedArray()
}

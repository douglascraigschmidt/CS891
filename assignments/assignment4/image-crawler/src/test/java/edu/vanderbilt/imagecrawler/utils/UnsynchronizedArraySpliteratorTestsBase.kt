package edu.vanderbilt.imagecrawler.utils

import admin.AssignmentTests
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.mockito.Mock
import org.mockito.Mockito.*
import java.util.function.Consumer

/**
 * Tests UnsynchronizedArray Spliterator inner class if base class [skipTest] flag is false.
 */
open class UnsynchronizedArraySpliteratorTestsBase : AssignmentTests() {
    @Mock
    lateinit var mockArray: UnsynchronizedArray<Int>

    @Mock
    lateinit var mockConsumer: Consumer<Int>

    open fun `tryAdvance with null parameter should throw NullPointerException`() {
        val array = newArray<Int>()
        val spliterator = array.spliterator()
        exception.expect(NullPointerException::class.java)
        spliterator.tryAdvance(null)
    }

    open fun `tryAdvance should call uncheckedToArray()`() {
        val array = arrayOf(1,2,3,4,5)
        val spliterator = ArraySpliterator<Int>(mockArray, 0, 10)
        `when`(mockArray.uncheckedToArray()).thenReturn(array)

        assertTrue(spliterator.tryAdvance(mockConsumer))

        verify(mockArray, times(1)).uncheckedToArray()
        verify(mockConsumer, times(1)).accept(anyInt())
    }

    open fun `Spliterator must properly handle sequential and parallel calculation of factorial of 19`() {
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

    open fun `Spliterator must properly handle sequential and parallel calculation of factorial of 20`() {
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

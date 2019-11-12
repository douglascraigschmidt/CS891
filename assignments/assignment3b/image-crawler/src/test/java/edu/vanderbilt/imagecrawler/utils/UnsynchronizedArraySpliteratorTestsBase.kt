package edu.vanderbilt.imagecrawler.utils

import admin.AssignmentTests
import admin.reflectiveEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.Mockito.*
import java.util.function.Consumer
import kotlin.random.Random
import kotlin.test.assertNotNull


/**
 * Tests UnsynchronizedArray Spliterator inner class if base class [skipTest] flag is false.
 */
open class UnsynchronizedArraySpliteratorTestsBase : AssignmentTests() {
    @Mock
    lateinit var mockArray: UnsynchronizedArray<Int>

    @Mock
    lateinit var mockConsumer: Consumer<Int>

    open fun `tryAdvance() with null parameter should throw NullPointerException`() {
        val array = newArray<Int>()
        exception.expect(NullPointerException::class.java)
        array.spliterator().tryAdvance(null)
    }

    open fun `tryAdvance() should return call the passed consumer with the appropriate value`() {
        val max = Random.nextInt(10, 20)
        val array = Array(max * 2) { Random.nextInt(1000, 2000) }
        var index = 0

        `when`(mockArray.uncheckedToArray()).thenReturn(array)
        `when`(mockConsumer.accept(anyInt())).thenAnswer {
            val input = it.arguments[0] as Int
            assertEquals(array[index], input)
            index++
        }

        (0..array.size).forEach {
            assertEquals(it < max,
                    ArraySpliterator<Int>(mockArray, it, max)
                            .tryAdvance(mockConsumer))
        }

        verify(mockArray, times(max)).uncheckedToArray()
        verify(mockConsumer, times(max)).accept(anyInt())
    }

    open fun `trySplit() should return null or a split array when appropriate`() {
        val max = 10
        for (i in 0 until max * 2) {
            val result = ArraySpliterator<Int>(mockArray, i, max).trySplit()
            if (i >= max - 1) {
                assertNull(result)
            } else {
                val mid = i + max ushr 1
                result.reflectiveEquals(ArraySpliterator<Int>(mockArray, i, mid))
                assertNotNull(result)
            }
        }
    }

    open fun `trySplit() should split when proper conditions are met`() {
        val max = Random.nextInt(10, 20)
        val array = Array(max * 2) { Random.nextInt(1000, 2000) }
        var index = 0

        `when`(mockArray.uncheckedToArray()).thenReturn(array)
        `when`(mockConsumer.accept(anyInt())).thenAnswer {
            val input = it.arguments[0] as Int
            assertEquals(array[index], input)
            index++
        }

        (0..array.size).forEach {
            assertEquals(it < max,
                    ArraySpliterator<Int>(mockArray, it, max)
                            .tryAdvance(mockConsumer))
        }

        verify(mockArray, times(max)).uncheckedToArray()
        verify(mockConsumer, times(max)).accept(anyInt())
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

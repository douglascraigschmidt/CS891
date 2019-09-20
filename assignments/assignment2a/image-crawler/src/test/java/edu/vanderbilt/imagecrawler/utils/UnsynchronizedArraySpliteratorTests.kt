package edu.vanderbilt.imagecrawler.utils

import admin.AssignmentTestRule
import admin.AssignmentTests
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.rules.ExpectedException

/**
 * Tests UnsynchronizedArray Spliterator inner class if base class [skipTest] flag is false.
 */
open class UnsynchronizedArraySpliteratorTests : AssignmentTests() {
    @DisplayName("Spliterator must properly handle sequential and parallel calculation of factorials")
    @ParameterizedTest(name = "sequential and parallel factorial calculation of {0} must be equal to {1}")
    @CsvSource("19, 121645100408832000", "20, 2432902008176640000")
    fun `test spliterator using a factorials`(number: Int, factorial: Long) {
        if (skipTest) return

        val array = newArray<Long>()

        for (i in 1..number) {
            array.add(i.toLong())
        }

        val f1 = array
                // Convert to a sequential stream.
                .stream()

                // Perform a reduction.
                .reduce(1L) { x, y -> x * y }

        assertEquals(factorial, f1)

        val f2 = array
                // Convert to a parallel stream.
                .parallelStream()

                // Perform a reduction.
                .reduce(1L) { x, y -> x * y }

        assertEquals(factorial, f2)
    }

    private fun <T> newArray(): Array<T> {
        return UnsynchronizedArray()
    }
}

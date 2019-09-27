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
    @Test
    fun runSpliteratorTest19() {
        if (!Assignment.testType(Assignment.GRADUATE)) {
            return
        }

        val array = UnsynchronizedArray<Long>()

        val factorialOf19 = 121645100408832000L

        for (i in 1..19) {
            array.add(i.toLong())
        }

        val f1 = array
                // Convert to a sequential stream.
                .stream()!!

                // Perform a reduction.
                .reduce(1L) { x, y -> x!! * y!! }

        assertEquals(factorialOf19, f1)

        val f2 = array
                // Convert to a parallel stream.
                .parallelStream()!!

                // Perform a reduction.
                .reduce(1L) { x, y -> x!! * y!! }

        assertEquals(factorialOf19, f2)
    }

    @Test
    fun runSpliteratorTest20() {
        if (!Assignment.testType(Assignment.GRADUATE)) {
            return
        }

        val array = UnsynchronizedArray<Long>()

        val factorialOf20 = 2432902008176640000L

        for (i in 1..20)
            array.add(i.toLong())

        val f1 = array
                // Convert to a parallel stream.
                .stream()!!

                // Perform a reduction.
                .reduce(1L) { x, y -> x!! * y!! }

        assertEquals(factorialOf20, f1)

        val f2 = array
                // Convert to a parallel stream.
                .parallelStream()!!

                // Perform a reduction.
                .reduce(1L) { x, y -> x!! * y!! }

        assertEquals(factorialOf20, f2)
    }
}

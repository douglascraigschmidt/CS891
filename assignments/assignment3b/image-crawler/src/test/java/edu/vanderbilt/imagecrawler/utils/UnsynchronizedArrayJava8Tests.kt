package edu.vanderbilt.imagecrawler.utils

import admin.ArrayHelper
import admin.AssignmentTests
import edu.vanderbilt.imagecrawler.utils.Assignment.GRADUATE
import edu.vanderbilt.imagecrawler.utils.Assignment.UNDERGRADUATE
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.Mockito.*
import java.util.function.Consumer

class UnsynchronizedArrayJava8Tests : AssignmentTests() {
    /**
     * A list with a similar content to mMixedInput but with all
     * entries shuffled.
     */
    private val shuffledMixedInput = ArrayHelper.constructShuffledMixedInput()

    @Test
    fun `forEach() must work as expected for UNDERGRADUATE assignment`() {
        if (!runAs(UNDERGRADUATE)) return

        val original = newArray<Any?>(shuffledMixedInput)
        assertArrayEquals(shuffledMixedInput.toTypedArray(), original.toArray())

        val a1 = spy<Array<Any?>>(newArray(shuffledMixedInput))
        assertArrayEquals(shuffledMixedInput.toTypedArray(), original.toArray())

        val a2 = newArray<Any?>()
        assertNotNull(ArrayHelper.getElements(a2))

        a1.forEach(Consumer { a2.add(it) })

        // Ensure that a1 hasn't changed.
        ArrayHelper.assertArrayEquals(original, a1)

        // a2 should have the same contents and size as a1.
        ArrayHelper.assertArrayEquals(a1, a2)

        // Should use iterator for implementation.
        verify<Array<Any?>>(a1, times(1)).iterator()

        // Should not call stream helper.
        verify<Array<Any?>>(a1, never()).stream()
    }

    @Test
    fun `forEach() must work as expected for GRADUATE assignment`() {
        if (!runAs(GRADUATE)) return

        val original = newArray<Any?>(shuffledMixedInput)
        val a1 = spy<Array<Any?>>(newArray(shuffledMixedInput))
        val a2 = newArray<Any?>()

        a1.forEach(Consumer {
            a2.add(it)
        })

        // Ensure that a1 hasn't changed.
        ArrayHelper.assertArrayEquals(original, a1)

        // a2 should have the same contents and size as a1.
        ArrayHelper.assertArrayEquals(a1, a2)

        // Should call stream helper once.
        verify<Array<Any?>>(a1, times(1)).stream()

        // Should use an for loop and not an inefficient iterator.
        verify<Array<Any?>>(a1, never()).iterator()
    }

    @Test
    fun `replaceAll() must work as expected`() {
        val expected = (1..20L).toMutableList()
        val array = newArray(expected)

        // Mimic with ArrayList.
        expected.replaceAll { it + 1 }

        // Test method.
        array.replaceAll { it + 1 }

        assertArrayEquals(expected.toTypedArray(), array.toArray())
    }

    private fun <T> newArray(input: List<T>?): Array<T> {
        return UnsynchronizedArray(input)
    }

    private fun <T> newArray(): Array<T> {
        return UnsynchronizedArray()
    }
}

package edu.vanderbilt.imagecrawler.utils

import admin.AssignmentTests
import edu.vanderbilt.imagecrawler.utils.Assignment.*
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit
import java.util.stream.Collector
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ArrayCollectorTests : AssignmentTests() {
    @Mock
    lateinit var collector: ArrayCollector<Int>

    @Mock
    lateinit var array: UnsynchronizedArray<Int>

    @Mock
    lateinit var arrayExtra: UnsynchronizedArray<Int>

    @Rule
    @JvmField
    var mockitoRule = MockitoJUnit.rule()!!

    @Test
    fun supplierUndergrad() {
        if (!assignmentType(UNDERGRADUATE)) return

        val result = ArrayCollector<Any>().supplier()
        assertNotNull(result)
        assertTrue(result.get() is UnsynchronizedArray)
    }

    @Test
    fun supplierGrad() {
        if (!assignmentType(GRADUATE)) return

        val result = ArrayCollector<Any>().supplier()
        assertNotNull(result)
        assertTrue(result.get() is SynchronizedArray)
    }

    @Test
    fun accumulator() {
        `when`(collector.accumulator()).thenCallRealMethod()
        val result = collector.accumulator()
        assertNotNull(result)
        result.accept(array, 2)
        verify(array, times(1)).add(2)
    }

    @Test
    fun combinerGrad() {
        if (!assignmentType(GRADUATE)) return

        val result = ArrayCollector<Any>().combiner()
        assertNull(result)
    }

    @Test
    fun combinerUndergrad() {
        if (!assignmentType(UNDERGRADUATE)) return

        `when`(collector.combiner()).thenCallRealMethod()
        val result = collector.combiner()
        assertNotNull(result)
        result.apply(array, arrayExtra)
        verify(array, times(1)).addAll(arrayExtra)
    }

    @Test
    fun finisherUndergrad() {
        if (!assignmentType(UNDERGRADUATE)) return

        `when`(collector.finisher()).thenCallRealMethod()
        val result = collector.finisher()
        assertNotNull(result)
        assertEquals(array, result.apply(array))
    }

    @Test
    fun finisherGrad() {
        if (!assignmentType(GRADUATE)) return

        `when`(collector.finisher()).thenCallRealMethod()
        val result = collector.finisher()
        assertNotNull(result)

        result.apply(array)
        verify(array, times(1)).toUnsynchronizedArray()
    }

    @Test
    fun characteristicsGrad() {
        if (!assignmentType(GRADUATE)) return

        `when`(collector.characteristics()).thenCallRealMethod()
        val result = collector.characteristics()
        assertNotNull(result)
        assertTrue(result.contains(Collector.Characteristics.UNORDERED))
        assertTrue(result.contains(Collector.Characteristics.IDENTITY_FINISH))
        assertTrue(result.contains(Collector.Characteristics.CONCURRENT))
    }

    @Test
    fun characteristicsUndergrad() {
        if (!assignmentType(UNDERGRADUATE)) return

        `when`(collector.characteristics()).thenCallRealMethod()
        val result = collector.characteristics()
        assertNotNull(result)
        assertTrue(result.contains(Collector.Characteristics.UNORDERED))
        assertTrue(result.contains(Collector.Characteristics.IDENTITY_FINISH))
    }

    @Test
    fun toArray() {
        val result = ArrayCollector.toArray<Any>()
        assertNotNull(result)
        assertTrue(result is ArrayCollector)
    }
}

package edu.vanderbilt.imagecrawler.utils

import admin.AssignmentTests
import org.mockito.Mock
import org.mockito.Mockito.*
import java.util.stream.Collector
import kotlin.test.*

open class ArrayCollectorTestsBase : AssignmentTests() {
    @Mock
    lateinit var collector: ArrayCollector<Int>

    @Mock
    lateinit var array: UnsynchronizedArray<Int>

    @Mock
    lateinit var arrayExtra: UnsynchronizedArray<Int>

    open fun supplier() {
        val result = ArrayCollector<Any>().supplier()
        assertNotNull(result)

        if (isGradAssignment()) {
            assertTrue(result.get() is SynchronizedArray)
        } else {
            assertTrue(result.get() is UnsynchronizedArray)
        }
    }

    open fun accumulator() {
        `when`(collector.accumulator()).thenCallRealMethod()
        val result = collector.accumulator()
        assertNotNull(result)
        result.accept(array, 2)
        verify(array, times(1)).add(2)
    }

    open fun combiner() {
        `when`(collector.combiner()).thenCallRealMethod()
        val result = collector.combiner()

        if (isGradAssignment()) {
            assertNull(result)
        } else {
            assertNotNull(result)
            result.apply(array, arrayExtra)
            verify(array, times(1)).addAll(arrayExtra)
        }
    }

    open fun finisher() {
        `when`(collector.finisher()).thenCallRealMethod()
        val result = collector.finisher()
        assertNotNull(result)

        if (isGradAssignment()) {
            assertNotSame(array, result.apply(array))
            verify(array, times(1)).toUnsynchronizedArray()
        } else {
            assertSame(array, result.apply(array))
        }
    }

    open fun characteristics() {
        `when`(collector.characteristics()).thenCallRealMethod()
        val result = collector.characteristics()
        assertNotNull(result)
        assertTrue(result.contains(Collector.Characteristics.UNORDERED))
        assertTrue(result.contains(Collector.Characteristics.IDENTITY_FINISH))
        assertEquals(result.contains(Collector.Characteristics.CONCURRENT), isGradAssignment())
    }

    open fun toArray() {
        val result = ArrayCollector.toArray<Any>()
        assertNotNull(result)
        assertTrue(result is ArrayCollector)
    }
}

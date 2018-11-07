package edu.vanderbilt.imagecrawler.utils

import org.junit.Test
import java.util.stream.Collector
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class Assignment3TestsArrayCollector {
    @Test
    fun supplier() {
        assertNotNull(ArrayCollector<Any>().supplier())
    }

    @Test
    fun accumulator() {
        assertNotNull(ArrayCollector<Any>().accumulator())
    }

    @Test
    fun combiner() {
        assertNull(ArrayCollector<Any>().combiner())
    }

    @Test
    fun finisher() {
        assertNotNull(ArrayCollector<Any>().finisher())
    }

    @Test
    fun characteristics() {
        val characteristics = ArrayCollector<Any>().characteristics()
        assertNotNull(characteristics)
        assertTrue(characteristics.contains(Collector.Characteristics.UNORDERED))
        assertTrue(characteristics.contains(Collector.Characteristics.IDENTITY_FINISH))
        assertTrue(characteristics.contains(Collector.Characteristics.CONCURRENT))
    }

    @Test
    fun toArray() {
        assertNotNull(ArrayCollector.toArray<Any>())
    }
}

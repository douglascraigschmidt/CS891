package edu.vanderbilt.imagecrawler.utils

import edu.vanderbilt.imagecrawler.utils.Assignment.*
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

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
        assertNotNull(ArrayCollector<Any>().characteristics())
    }

    @Test
    fun toArray() {
        assertNotNull(ArrayCollector.toArray<Any>())
    }
}

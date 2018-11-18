package edu.vanderbilt.imagecrawler.utils

import edu.vanderbilt.imagecrawler.utils.Assignment.*
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class Assignment2bTestsArrayCollector {
    @Test
    fun supplier() {
    // @@ Monte, can you please check that the GRADUATE supplier is SynchronizedArray
    // and the UNDERGRADUATE supplier is UnsynchronizedArray?
        assertNotNull(ArrayCollector<Any>().supplier())
    }

    @Test
    fun accumulator() {
    // @@ Monte, can you please check that the accumulator is Array::add?
        assertNotNull(ArrayCollector<Any>().accumulator())
    }

    @Test
    fun combinerGrad() {
        if (!testType(GRADUATE)) {
            return
        }

        assertNull(ArrayCollector<Any>().combiner())
    }

    @Test
    fun combinerUndergrad() {
        if (!testType(UNDERGRADUATE)) {
            return
        }

        assertNotNull(ArrayCollector<Any>().combiner())
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

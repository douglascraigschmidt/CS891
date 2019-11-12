package edu.vanderbilt.imagecrawler.utils

import org.junit.Before
import org.junit.Test

/**
 * Tests UnsynchronizedArray Iterator inner class for graduate students only.
 */
class ArrayCollectorTestsForGrads: ArrayCollectorTestsBase() {
    /**
     * This function will throw an AssumptionViolatedException causing
     * the test runner to ignore each test function in this class if
     * the current project assignment does not include the GRADUATE flag.
     */
    @Before
    fun before() {
        graduateTest()
    }

    @Test
    override fun supplier() {
        super.supplier()
    }

    @Test
    override fun accumulator() {
        super.accumulator()
    }

    @Test
    override fun combiner() {
        super.combiner()
    }

    @Test
    override fun finisher() {
        super.finisher()
    }

    @Test
    override fun characteristics() {
        super.characteristics()
    }

    @Test
    override fun toArray() {
        super.toArray()
    }
}

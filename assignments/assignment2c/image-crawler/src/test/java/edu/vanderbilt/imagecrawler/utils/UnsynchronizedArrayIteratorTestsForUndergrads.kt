package edu.vanderbilt.imagecrawler.utils

import org.junit.Before
import org.junit.Test

/**
 * Tests UnsynchronizedArray Iterator inner class for graduate students only.
 */
class UnsynchronizedArrayIteratorTestsForUndergrads : UnsynchronizedArrayIteratorTestsBase() {
    /**
     * This function will throw an AssumptionViolatedException causing
     * the test runner to ignore each test function in this class if
     * the current project assignment does not include the GRADUATE flag.
     */
    @Before
    fun before() {
        undergraduateTest()
    }

    @Test
    override fun `iterator must handle an empty array`() {
        super.`iterator must handle an empty array`()
    }

    @Test
    override fun `iterator hasNext() and next() should work in conjunction to properly allow access to all array elements`() {
        super.`iterator hasNext() and next() should work in conjunction to properly allow access to all array elements`()
    }

    @Test
    override fun `iterator next() must throw NoSuchElementException when array is empty`() {
        super.`iterator next() must throw NoSuchElementException when array is empty`()
    }

    @Test
    override fun `iterator next() must throw NoSuchElementException for all calls after end of array is reached`() {
        super.`iterator next() must throw NoSuchElementException for all calls after end of array is reached`()
    }

    @Test
    override fun `iterator add() and set() methods must work as expected`() {
        super.`iterator add() and set() methods must work as expected`()
    }

    @Test
    override fun `iterator remove() must be able to remove all elements`() {
        super.`iterator remove() must be able to remove all elements`()
    }
}

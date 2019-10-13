package edu.vanderbilt.imagecrawler.utils

import org.junit.Before
import org.junit.Test

/**
 * Tests UnsynchronizedArray Spliterator inner class for undergraduate students.
 */
class UnsynchronizedArraySpliteratorTestsForUndergrads : UnsynchronizedArraySpliteratorTestsBase() {
    /**
     * This function will throw an AssumptionViolatedException causing
     * the test runner to ignore each test function in this class if
     * the current project assignment does not include the UNDERGRADUATE flag.
     */
    @Before
    fun before() {
        undergraduateTest()
    }

    @Test
    override fun `tryAdvance with null parameter should throw NullPointerException`() {
        super.`tryAdvance with null parameter should throw NullPointerException`()
    }

    @Test
    override fun `tryAdvance should call uncheckedToArray()`() {
        super.`tryAdvance should call uncheckedToArray()`()
    }

    @Test
    override fun `Spliterator must properly handle sequential and parallel calculation of factorial of 19`() {
        super.`Spliterator must properly handle sequential and parallel calculation of factorial of 19`()
    }

    @Test
    override fun `Spliterator must properly handle sequential and parallel calculation of factorial of 20`() {
        super.`Spliterator must properly handle sequential and parallel calculation of factorial of 20`()
    }
}

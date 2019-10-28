package edu.vanderbilt.imagecrawler.utils

import kotlinx.coroutines.InternalCoroutinesApi
import org.junit.Before
import org.junit.Test

@InternalCoroutinesApi
class SynchronizedArrayTestsForGrads : SynchronizedArrayTestsBase() {
    /**
     * This function will throw an AssumptionViolatedException causing
     * the test runner to ignore each test function in this class if
     * the current project assignment does not include the GRADUATE flag.
     */
    @Before
    fun privateBefore() {
        graduateTest()
    }

    @Test
    override fun isEmpty() {
        super.isEmpty()
    }

    @Test
    override fun size() {
        super.size()
    }

    @Test
    override fun indexOf() {
        super.indexOf()
    }

    @Test
    override fun addAllCollection() {
        super.addAllCollection()
    }

    @Test
    override fun addAllArray() {
        super.addAllArray()
    }

    @Test
    override fun remove() {
        super.remove()
    }

    @Test
    override fun get() {
        super.get()
    }

    @Test
    override fun set() {
        super.set()
    }

    @Test
    override fun add() {
        super.add()
    }

    @Test
    override fun toUnsynchronizedArray() {
        super.toUnsynchronizedArray()
    }

    @Test
    override fun uncheckedToArray() {
        super.uncheckedToArray()
    }

    @Test
    override fun toArray() {
        super.toArray()
    }

    @Test
    override fun toArrayTypedArray() {
        super.toArrayTypedArray()
    }

    @Test
    override fun iterator() {
        super.iterator()
    }

    @Test
    override fun replaceAll() {
        super.replaceAll()
    }

    @Test
    override fun forEach() {
        super.forEach()
    }

    @Test
    override fun parallelStream() {
        super.parallelStream()
    }

    @Test
    override fun stream() {
        super.stream()
    }

    @Test
    override fun spliterator() {
        super.spliterator()
    }
}
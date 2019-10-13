package edu.vanderbilt.imagecrawler.utils

import admin.AssignmentTests
import admin.setField
import kotlinx.coroutines.*
import org.junit.Before
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.function.Consumer
import java.util.function.UnaryOperator
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.fail

/**
 * All tests in this class should only test for synchronize calls AFTER testing
 * a method for results. This ordering prevents test from hanging.
 */
@InternalCoroutinesApi
open class SynchronizedArrayTestsBase : AssignmentTests() {
    @Mock
    lateinit var mockArray: Array<Int>

    @Mock
    lateinit var mockDataArray: Array<Int>

    @Mock
    lateinit var mockCollection: Collection<Int>

    @Mock
    lateinit var mockIterator: MutableIterator<Int>

    @Mock
    lateinit var mockUnaryOperator: UnaryOperator<Int>

    @Mock
    lateinit var mockConsumer: Consumer<Int>

    @Mock
    lateinit var mockStream: Stream<Int>

    @Mock
    lateinit var mockSpliterator: Spliterator<Int>

    private val array = SynchronizedArray<Int>()

    private fun testSynchronized(name: String, block: () -> Unit) {
        val start = CountDownLatch(1)
        val stop = CountDownLatch(1)

        // Coroutine that holds a synchronized lock on the test array.
        val blocker = GlobalScope.launch {
            synchronized(array) {
                start.countDown()
                stop.await()
            }
        }

        // Coroutine that runs the passed block of code.
        val runner = GlobalScope.async(start = CoroutineStart.LAZY) {
            block()
        }

        // Blocking main thread coroutine.
        runBlocking {
            start.await()

            try {
                withTimeoutOrNull(10) {
                    runner.await()
                    fail("Method $name is not synchronized")
                }
            } finally {
                stop.countDown()
                blocker.join()
            }

            runner.await()
        }
    }

    @Before
    fun before() {
        array.setField("", mockArray, Array::class.java)
    }

    open fun isEmpty() {
        `when`(mockArray.isEmpty).thenReturn(false)
        assertFalse(array.isEmpty)
        verify(mockArray, times(1)).isEmpty

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.isEmpty
        }
    }

    open fun size() {
        `when`(mockArray.size()).thenReturn(-1)
        assertEquals(-1, array.size())
        verify(mockArray, times(1)).size()

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.size()
        }
    }

    open fun indexOf() {
        `when`(mockArray.indexOf(0)).thenReturn(100)
        assertEquals(100, array.indexOf(0))
        verify(mockArray, times(1)).indexOf(0)

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.indexOf(0)
        }
    }

    open fun addAllCollection() {
        `when`(mockArray.addAll(mockCollection)).thenReturn(true)
        assertEquals(true, array.addAll(mockCollection))
        verify(mockArray, times(1)).addAll(mockCollection)

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.addAll(mockCollection)
        }
    }

    open fun addAllArray() {
        `when`(mockArray.addAll(mockDataArray)).thenReturn(true)
        assertEquals(true, array.addAll(mockDataArray))
        verify(mockArray, times(1)).addAll(mockDataArray)

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.addAll(mockDataArray)
        }
    }

    open fun remove() {
        `when`(mockArray.remove(1)).thenReturn(1000)
        assertEquals(1000, array.remove(1))
        verify(mockArray, times(1)).remove(1)

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.remove(1)
        }
    }

    open fun get() {
        `when`(mockArray.get(1)).thenReturn(1000)
        assertEquals(1000, array.get(1))
        verify(mockArray, times(1)).get(1)

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.get(1)
        }
    }

    open fun set() {
        `when`(mockArray.set(1, 10)).thenReturn(1000)
        assertEquals(1000, array.set(1, 10))
        verify(mockArray, times(1)).set(1, 10)

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.set(1, 10)
        }
    }

    open fun add() {
        `when`(mockArray.add(1)).thenReturn(true)
        assertEquals(true, array.add(1))
        verify(mockArray, times(1)).add(1)

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.add(1)
        }
    }

    open fun toUnsynchronizedArray() {
        assertNotEquals(mockArray, array.toUnsynchronizedArray())

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.toUnsynchronizedArray()
        }
    }

    open fun uncheckedToArray() {
        val data = Array<Any>(1) {}

        `when`(mockArray.uncheckedToArray()).thenReturn(data)
        assertEquals(data, array.uncheckedToArray())
        verify(mockArray, times(1)).uncheckedToArray()

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.uncheckedToArray()
        }
    }

    open fun toArray() {
        val data = Array<Any>(1) {}

        `when`(mockArray.toArray()).thenReturn(data)
        assertEquals(data, array.toArray())
        verify(mockArray, times(1)).toArray()

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.toArray()
        }
    }

    open fun toArrayTypedArray() {
        val data = Array<Any>(1) {}

        `when`(mockArray.toArray(data)).thenReturn(data)
        assertEquals(data, array.toArray(data))
        verify(mockArray, times(1)).toArray(data)

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.toArray(data)
        }
    }

    open fun iterator() {
        `when`(mockArray.iterator()).thenReturn(mockIterator)
        assertEquals(mockIterator, array.iterator())
        verify(mockArray, times(1)).iterator()
    }

    open fun replaceAll() {
        doNothing().`when`(mockArray).replaceAll(mockUnaryOperator)
        array.replaceAll(mockUnaryOperator)
        verify(mockArray, times(1)).replaceAll(mockUnaryOperator)

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.replaceAll(mockUnaryOperator)
        }
    }

    open fun forEach() {
        doNothing().`when`(mockArray).forEach(mockConsumer)
        array.forEach(mockConsumer)
        verify(mockArray, times(1)).forEach(mockConsumer)

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.forEach(mockConsumer)
        }
    }

    open fun parallelStream() {
        `when`(mockArray.parallelStream()).thenReturn(mockStream)
        assertEquals(mockStream, array.parallelStream())
        verify(mockArray, times(1)).parallelStream()
    }

    open fun stream() {
        `when`(mockArray.stream()).thenReturn(mockStream)
        assertEquals(mockStream, array.stream())
        verify(mockArray, times(1)).stream()
    }

    open fun spliterator() {
        `when`(mockArray.spliterator()).thenReturn(mockSpliterator)
        assertEquals(mockSpliterator, array.spliterator())
        verify(mockArray, times(1)).spliterator()
    }
}

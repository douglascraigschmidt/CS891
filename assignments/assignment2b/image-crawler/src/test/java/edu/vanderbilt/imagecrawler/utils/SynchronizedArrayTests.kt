package edu.vanderbilt.imagecrawler.utils

import admin.AssignmentTests
import admin.setField
import kotlinx.coroutines.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit
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
open class SynchronizedArrayTests : AssignmentTests() {
    override val runTest: Boolean = false

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

    @Rule
    @JvmField
    var mockitoRule = MockitoJUnit.rule()!!

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

    private fun testSynchronizedOld(method: String, block: () -> Unit) {
        var locked = false

        GlobalScope.launch {
            synchronized(array) {
                locked = true
                while (locked);
            }
        }

        val job = GlobalScope.async {
            while (!locked);
            block()
        }

        runBlocking {
            withTimeoutOrNull(200) {
                job.await()
                fail("Method $method is not synchronized")
            }
            locked = false
        }
    }

    @Before
    fun before() {
        if (runTest) {
            array.setField("", mockArray, Array::class.java)
        }
    }

    @Test
    fun isEmpty() {
        if (skipTest) return

        `when`(mockArray.isEmpty).thenReturn(false)
        assertFalse(array.isEmpty)
        verify(mockArray, times(1)).isEmpty

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.isEmpty
        }
    }

    @Test
    fun size() {
        if (skipTest) return

        `when`(mockArray.size()).thenReturn(-1)
        assertEquals(-1, array.size())
        verify(mockArray, times(1)).size()

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.size()
        }
    }

    @Test
    fun indexOf() {
        if (skipTest) return

        `when`(mockArray.indexOf(0)).thenReturn(100)
        assertEquals(100, array.indexOf(0))
        verify(mockArray, times(1)).indexOf(0)

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.indexOf(0)
        }
    }

    @Test
    fun addAllCollection() {
        if (skipTest) return

        `when`(mockArray.addAll(mockCollection)).thenReturn(true)
        assertEquals(true, array.addAll(mockCollection))
        verify(mockArray, times(1)).addAll(mockCollection)

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.addAll(mockCollection)
        }
    }

    @Test
    fun addAllArray() {
        if (skipTest) return

        `when`(mockArray.addAll(mockDataArray)).thenReturn(true)
        assertEquals(true, array.addAll(mockDataArray))
        verify(mockArray, times(1)).addAll(mockDataArray)

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.addAll(mockDataArray)
        }
    }

    @Test
    fun remove() {
        if (skipTest) return

        `when`(mockArray.remove(1)).thenReturn(1000)
        assertEquals(1000, array.remove(1))
        verify(mockArray, times(1)).remove(1)

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.remove(1)
        }
    }

    @Test
    fun get() {
        if (skipTest) return

        `when`(mockArray.get(1)).thenReturn(1000)
        assertEquals(1000, array.get(1))
        verify(mockArray, times(1)).get(1)

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.get(1)
        }
    }

    @Test
    fun set() {
        if (skipTest) return

        `when`(mockArray.set(1, 10)).thenReturn(1000)
        assertEquals(1000, array.set(1, 10))
        verify(mockArray, times(1)).set(1, 10)

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.set(1, 10)
        }
    }

    @Test
    fun add() {
        if (skipTest) return

        `when`(mockArray.add(1)).thenReturn(true)
        assertEquals(true, array.add(1))
        verify(mockArray, times(1)).add(1)

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.add(1)
        }
    }

    @Test
    fun toUnsynchronizedArray() {
        if (skipTest) return

        assertNotEquals(mockArray, array.toUnsynchronizedArray())

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.toUnsynchronizedArray()
        }
    }

    @Test
    fun uncheckedToArray() {
        if (skipTest) return

        val data = Array<Any>(1) {}

        `when`(mockArray.uncheckedToArray()).thenReturn(data)
        assertEquals(data, array.uncheckedToArray())
        verify(mockArray, times(1)).uncheckedToArray()

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.uncheckedToArray()
        }
    }

    @Test
    fun toArray() {
        if (skipTest) return

        val data = Array<Any>(1) {}

        `when`(mockArray.toArray()).thenReturn(data)
        assertEquals(data, array.toArray())
        verify(mockArray, times(1)).toArray()

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.toArray()
        }
    }

    @Test
    fun toArrayTypedArray() {
        if (skipTest) return

        val data = Array<Any>(1) {}

        `when`(mockArray.toArray(data)).thenReturn(data)
        assertEquals(data, array.toArray(data))
        verify(mockArray, times(1)).toArray(data)

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.toArray(data)
        }
    }

    @Test
    fun iterator() {
        if (skipTest) return

        `when`(mockArray.iterator()).thenReturn(mockIterator)
        assertEquals(mockIterator, array.iterator())
        verify(mockArray, times(1)).iterator()
    }

    @Test
    fun replaceAll() {
        if (skipTest) return

        doNothing().`when`(mockArray).replaceAll(mockUnaryOperator)
        array.replaceAll(mockUnaryOperator)
        verify(mockArray, times(1)).replaceAll(mockUnaryOperator)

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.replaceAll(mockUnaryOperator)
        }
    }

    @Test
    fun forEach() {
        if (skipTest) return

        doNothing().`when`(mockArray).forEach(mockConsumer)
        array.forEach(mockConsumer)
        verify(mockArray, times(1)).forEach(mockConsumer)

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.forEach(mockConsumer)
        }
    }

    @Test
    fun parallelStream() {
        if (skipTest) return

        `when`(mockArray.parallelStream()).thenReturn(mockStream)
        assertEquals(mockStream, array.parallelStream())
        verify(mockArray, times(1)).parallelStream()
    }

    @Test
    fun stream() {
        if (skipTest) return

        `when`(mockArray.stream()).thenReturn(mockStream)
        assertEquals(mockStream, array.stream())
        verify(mockArray, times(1)).stream()
    }

    @Test
    fun spliterator() {
        if (skipTest) return

        `when`(mockArray.spliterator()).thenReturn(mockSpliterator)
        assertEquals(mockSpliterator, array.spliterator())
        verify(mockArray, times(1)).spliterator()
    }
}

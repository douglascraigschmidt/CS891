package edu.vandy.simulator.managers.beings.executorService

import admin.AssignmentTests
import admin.injectInto
import admin.value
import com.nhaarman.mockitokotlin2.*
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.rules.Timeout.seconds
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.lenient
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collector
import java.util.stream.Stream

class Assignment_2A_ExecutorServiceMgrTest : AssignmentTests() {
    override var timeout: Timeout = seconds(1000000)

    companion object {
        private const val BEING_COUNT = 5
    }

    @Mock
    private lateinit var mFutureMock: Future<BeingCallable>

    @Mock
    private lateinit var beingsMock: List<BeingCallable>

    @Mock
    private lateinit var mListFutureMock: List<Future<BeingCallable>>

    @Mock
    private lateinit var mExecutorMock: ExecutorService

    @Mock
    private lateinit var mStreamBeingCallableMock: Stream<BeingCallable>

    @Mock
    private lateinit var mStreamFutureMock: Stream<Future<BeingCallable>>

    @InjectMocks
    private var mManagerMock: ExecutorServiceMgr = mock()

    // Used to count lambda errors caught by try/catch block
    private var mErrorCount = 0

    @After
    fun after() {
        shutdownExecutor()
    }

    @Test
    fun testNewBeing() {
        doCallRealMethod().whenever(mManagerMock).newBeing()

        // Call SUT.
        val beingCallable = mManagerMock.newBeing()
        assertNotNull("newBeing should not return null.", beingCallable)
    }

    @Test
    fun testRunSimulation() {
        doCallRealMethod().whenever(mManagerMock).runSimulation()

        // Call SUT.
        mManagerMock.runSimulation()

        val inOrder = inOrder(mManagerMock)
        inOrder.verify(mManagerMock).beginBeingThreadPool()
        inOrder.verify(mManagerMock).awaitCompletionOfFutures()
        inOrder.verify(mManagerMock).shutdownNow()
    }

    /**
     * Test common to both UNDERGRADUATE And GRADUATE Assignments.
     */
    @Test
    fun testBeginBeingThreadPool() {
        val mockBeings = createMockBeingList(BEING_COUNT)
        whenever(mManagerMock.beings).thenReturn(mockBeings)
        whenever(mManagerMock.beingCount).thenReturn(mockBeings.size)
        whenever(mManagerMock.createExecutorService(mockBeings.size)).thenReturn(mExecutorMock)
        whenever(mManagerMock.threadCount).thenReturn(BEING_COUNT)
        whenever(mExecutorMock.submit(any<BeingCallable>())).thenReturn(mFutureMock)
        doCallRealMethod().whenever(mManagerMock).beginBeingThreadPool()

        // Call SUT.
        mManagerMock.beginBeingThreadPool()

        verify(mExecutorMock, times(mockBeings.size)).submit(any<BeingCallable>())
        val futureList = mManagerMock.value<List<Future<BeingCallable>>>()
        assertNotNull("Unable to access List<Future<BeingCallable>> field in " +
                "ExecutorServiceMgr class.", futureList)
        assertEquals(
                "Futures list should contain $BEING_COUNT threads.",
                BEING_COUNT.toLong(),
                futureList!!.size.toLong())
        for (future in futureList) {
            assertEquals("Unexpected future value in mFutureList", mFutureMock, future)
        }
    }

    /**
     * Test for GRADUATE use of Java 8 Streams.
     */
    @Test
    fun testBeginBeingThreadPoolGraduate() {
        graduateTest()
        whenever(beingsMock.size).thenReturn(BEING_COUNT)
        whenever(beingsMock.stream()).thenReturn(mStreamBeingCallableMock)
        whenever(mManagerMock.beings).thenReturn(beingsMock)
        whenever(mManagerMock.createExecutorService(beingsMock.size)).thenReturn(mExecutorMock)
        whenever(mManagerMock.threadCount).thenReturn(BEING_COUNT)
        doReturn(mStreamFutureMock).whenever(mStreamBeingCallableMock)
                .map(any<Function<BeingCallable, Future<BeingCallable>>>())
        val futureListMock: List<Future<BeingCallable>> = ArrayList()
        doReturn(futureListMock).whenever(mStreamFutureMock)
                .collect(any<Collector<in Future<BeingCallable>, Any, Any>>())
        doCallRealMethod().whenever(mManagerMock).beginBeingThreadPool()

        // Call SUT.
        mManagerMock.beginBeingThreadPool()

        verify(mManagerMock).createExecutorService(beingsMock.size)
        verify(beingsMock).stream()
        verify(mStreamBeingCallableMock).map(any<Function<BeingCallable, Future<BeingCallable>>>())
        verify(mStreamFutureMock).collect(any<Collector<in Future<BeingCallable>, Any, Any>>())
        val futureList = mManagerMock.value<List<Future<BeingCallable>>>()

        assertNotNull("Unable to access List<Future<BeingCallable>> field in " +
                "ExecutorServiceMgr class.", futureList)
        Assert.assertSame(
                "Futures list should contain $BEING_COUNT threads.",
                futureListMock,
                futureList)
    }

    /**
     * Test common to both UNDERGRADUATE And GRADUATE Assignments.
     */
    @Test
    fun testAwaitCompletionOfFutures() {
        val futureList = createMockFutureList(BEING_COUNT, true)
        futureList.injectInto(mManagerMock)
        doCallRealMethod().whenever(mManagerMock).awaitCompletionOfFutures()

        // Call SUT.
        mManagerMock.awaitCompletionOfFutures()

        futureList.forEach(Consumer { futureMock: Future<BeingCallable> ->
            try {
                verify(futureMock).get()
            } catch (e: Exception) {
                mErrorCount++
            }
        })
        check(mErrorCount <= 0) {
            ("Call to Future.get() failed "
                    + mErrorCount + if (mErrorCount == 1) " time" else "times")
        }
    }

    /**
     * Test for GRADUATE use of Java 8 Streams.
     */
    @Test
    fun testAwaitCompletionOfFuturesGraduate() {
        graduateTest()
        val expectedCount: Long = 999

        mListFutureMock.injectInto(mManagerMock)
        lenient().doReturn(mStreamFutureMock).whenever(mListFutureMock).stream()
        lenient().doReturn(mStreamBeingCallableMock)
                .whenever(mStreamFutureMock).map(any<Function<Future<BeingCallable>, BeingCallable>>())
        lenient().doReturn(expectedCount).whenever(mStreamBeingCallableMock).count()
        doCallRealMethod().whenever(mManagerMock).awaitCompletionOfFutures()

        // Call SUT.
        mManagerMock.awaitCompletionOfFutures()

        try {
            // Support old version awaitCompletionOfFutures method.
            // TODOx: REMOVE THIS AFTER 2019/2020 SCHOOL YEAR!!!!
            verify(mListFutureMock).stream()
            verify(mStreamFutureMock).map(any<Function<Future<BeingCallable>, BeingCallable>>())
            verify(mStreamBeingCallableMock).count()
        } catch (t: Throwable) {
            // Support new awaitCompletionOfFutures method.
            verify(mListFutureMock).forEach(any<Consumer<in Future<BeingCallable>>>())
        }
    }

    @Test
    fun testShutdownNow() {
        mExecutorMock.injectInto(mManagerMock)
        val futureList = createMockFutureList(BEING_COUNT, false)
        futureList.forEach(Consumer { futureMock: Future<BeingCallable> ->
            whenever(futureMock.isCancelled).thenReturn(false)
            whenever(futureMock.isDone).thenReturn(false)
            whenever(futureMock.cancel(anyBoolean())).thenReturn(true)
        })
        futureList.injectInto(mManagerMock)
        doCallRealMethod().whenever(mManagerMock).shutdownNow()

        // Call SUT.
        mManagerMock.shutdownNow()

        // Cleanup - ensure that all futures are cancelled
        futureList.forEach(Consumer { futureMock: Future<BeingCallable> ->
            try {
                verify(futureMock).cancel(anyBoolean())
            } catch (e: Exception) {
            }
        })
    }

    private fun createMockFutureList(count: Int, mockGet: Boolean): List<Future<BeingCallable>> =
            (1..count).map {
                mock<Future<BeingCallable>>().apply {
                    try {
                        if (mockGet) {
                            @Suppress("RemoveExplicitTypeArguments")
                            whenever(get()).thenReturn(mock<BeingCallable>())
                        }
                    } catch (e: Exception) {
                    }
                }
            }

    private fun createMockBeingList(count: Int): List<BeingCallable> =
            (1..count).map {
                @Suppress("RemoveExplicitTypeArguments")
                mock<BeingCallable>()
            }

    /**
     * Make sure to shutdown thread pool executor to prevent the autograder
     * from hanging in the case where the threads continue running after the
     * test is over.
     */
    private fun shutdownExecutor() {
        try {
            val executor = mManagerMock.value<ExecutorService>()
            assertNotNull("Unable to access ExecutorService " +
                    "field in ExecutorServiceMgr class.", executor)
            executor?.shutdownNow()
        } catch (e: Exception) {
            Assert.fail("Unable to access ExecutorService " +
                    "field in ExecutorServiceMgr class: " + e)
        }
    }
}
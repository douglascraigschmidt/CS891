package edu.vandy.simulator.managers.beings.completionService

import admin.AssignmentTests
import admin.ReflectionHelper
import com.nhaarman.mockitokotlin2.*
import edu.vandy.simulator.utils.Assignment
import org.junit.Assert
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
import java.util.concurrent.CompletionService
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

class Assignment_3A_ExecutorCompletionServiceMgrTest : AssignmentTests() {
    companion object {
        private const val BEING_COUNT = 5
    }

    @Mock
    private lateinit var mCompletionServiceMock: CompletionService<BeingCallable>

    @Mock
    private lateinit var mExecutorMock: ExecutorService

    @Mock
    private lateinit var mFutureMock: Future<BeingCallable>

    @InjectMocks
    private var mManagerMock: ExecutorCompletionServiceMgr = mock()

    class SimulatedException : RuntimeException("Simulated exception")

    @Test
    fun testNewBeing() {
        doCallRealMethod().whenever(mManagerMock).newBeing()

        // Call SUT.
        val beingCallable = mManagerMock.newBeing()

        Assert.assertNotNull("newBeing should not return null.", beingCallable)
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

    @Test
    fun testClassFieldsExist() {
        val instance = ExecutorCompletionServiceMgr()
        ReflectionHelper.injectValueIntoFirstMatchingField(instance, null, CompletionService::class.java)
        ReflectionHelper.injectValueIntoFirstMatchingField(instance, null, ExecutorService::class.java)
    }

    @Test
    fun testCreateExecutorService() {
        whenever(mManagerMock.createExecutorService()).thenCallRealMethod()

        // Call SUT.
        val executorService = mManagerMock.createExecutorService()

        Assert.assertNotNull(executorService)
    }

    @Test
    fun testCreateExecutorCompletionService() {
        whenever(mManagerMock.createExecutorCompletionService(ArgumentMatchers.any())).thenCallRealMethod()

        // Call SUT.
        val completionService = mManagerMock.createExecutorCompletionService(mExecutorMock)

        Assert.assertNotNull(completionService)
    }

    /**
     * Test common to both UNDERGRADUATE And GRADUATE Assignments.
     */
    @Test
    fun testBeginBeingThreadPool() {
        val mockBeings = createMockBeingList(BEING_COUNT)
        whenever(mManagerMock.createExecutorService()).thenReturn(mExecutorMock)
        whenever(mManagerMock.createExecutorCompletionService(mExecutorMock)).thenReturn(mCompletionServiceMock)
        whenever(mManagerMock.beings).thenReturn(mockBeings)
        whenever(mCompletionServiceMock.submit(ArgumentMatchers.any(BeingCallable::class.java))).thenReturn(mFutureMock)
        doCallRealMethod().whenever(mManagerMock).beginBeingThreadPool()

        // Call SUT.
        mManagerMock.beginBeingThreadPool()

        verify(mManagerMock, times(1)).createExecutorService()
        verify(mManagerMock, times(1)).createExecutorCompletionService(mExecutorMock)
        verify(mManagerMock, times(1)).beings
        verify(mCompletionServiceMock, times(mockBeings.size)).submit(ArgumentMatchers.any(BeingCallable::class.java))
        ReflectionHelper.findFirstMatchingFieldValue<Any>(mManagerMock, CompletionService::class.java)
        ReflectionHelper.findFirstMatchingField(mManagerMock, ExecutorService::class.java)
    }

    /**
     * Test common to both UNDERGRADUATE And GRADUATE Assignments.
     */
    @Test
    fun testAwaitCompletionOfFuturesUndergraduates() {
        val beingCallableMock = mock<BeingCallable>()
        val mockBeings = createMockBeingList(BEING_COUNT)
        val size = mockBeings.size
        whenever(mManagerMock.beings).thenReturn(mockBeings)
        whenever(mManagerMock.beingCount).thenReturn(size)
        whenever(mCompletionServiceMock.take()).thenReturn(mFutureMock)
        whenever(mFutureMock.get()).thenReturn(beingCallableMock)
        ReflectionHelper.injectValueIntoFirstMatchingField(
                mManagerMock, mExecutorMock, ExecutorService::class.java)
        ReflectionHelper.injectValueIntoFirstMatchingField(
                mManagerMock, mCompletionServiceMock, CompletionService::class.java)
        doCallRealMethod().whenever(mManagerMock).awaitCompletionOfFutures()

        // Call SUT
        mManagerMock.awaitCompletionOfFutures()

        try {
            verify(mManagerMock, atLeast(2)).beingCount
        } catch (t: Throwable) {
            verify(mockBeings, atLeastOnce()).size
        }

        verify(mCompletionServiceMock, times(BEING_COUNT)).take()
        verify(mFutureMock, times(BEING_COUNT)).get()
    }

    @Test
    fun testAwaitCompletionOfFuturesWithExceptionUndergraduates() {
        if (!Assignment.testType(Assignment.UNDERGRADUATE)) {
            println("Skipping testAwaitCompletionOfFutures (undergraduate only test)")
            return
        }
        val mockBeings = createMockBeingList(BEING_COUNT)
        val size = mockBeings.size
        whenever(mManagerMock.beings).thenReturn(mockBeings)
        whenever(mManagerMock.beingCount).thenReturn(size)
        whenever(mCompletionServiceMock.take()).thenReturn(mFutureMock)
        whenever(mFutureMock.get()).thenThrow(SimulatedException())
        ReflectionHelper.injectValueIntoFirstMatchingField(
                mManagerMock, mExecutorMock, ExecutorService::class.java)
        ReflectionHelper.injectValueIntoFirstMatchingField(
                mManagerMock, mCompletionServiceMock, CompletionService::class.java)
        doCallRealMethod().whenever(mManagerMock).awaitCompletionOfFutures()

        // Call SUT
        assertThrows<RuntimeException> { mManagerMock.awaitCompletionOfFutures() }

        try {
            verify(mManagerMock, atLeast(2)).beingCount
        } catch (t: Throwable) {
            verify(mockBeings, atLeastOnce()).size
        }

        verify(mCompletionServiceMock, times(1)).take()
        verify(mFutureMock, times(1)).get()
    }

    @Test
    fun testAwaitCompletionOfFuturesWithExceptionGraduates() {
        if (!Assignment.testType(Assignment.GRADUATE)) {
            println("Skipping testAwaitCompletionOfFutures (graduate only test)")
            return
        }
        val mockBeings = createMockBeingList(BEING_COUNT)
        val size = mockBeings.size
        whenever(mManagerMock.beings).thenReturn(mockBeings)
        whenever(mManagerMock.beingCount).thenReturn(size)
        whenever(mCompletionServiceMock.take()).thenReturn(mFutureMock)
        whenever(mFutureMock.get()).thenThrow(SimulatedException())
        ReflectionHelper.injectValueIntoFirstMatchingField(
                mManagerMock, mExecutorMock, ExecutorService::class.java)
        ReflectionHelper.injectValueIntoFirstMatchingField(
                mManagerMock, mCompletionServiceMock, CompletionService::class.java)
        doCallRealMethod().whenever(mManagerMock).awaitCompletionOfFutures()

        // Call SUT
        assertThrows<RuntimeException> { mManagerMock.awaitCompletionOfFutures() }

        try {
            verify(mManagerMock, atLeast(2)).beingCount
        } catch (t: Throwable) {
            verify(mockBeings, atLeastOnce()).size
        }

        verify(mCompletionServiceMock, times(1)).take()
        verify(mFutureMock, times(1)).get()
    }

    @Test
    fun testShutdownNow() {
        ReflectionHelper.injectValueIntoFirstMatchingField(
                mManagerMock, mExecutorMock, ExecutorService::class.java)
        doCallRealMethod().whenever(mManagerMock).shutdownNow()

        // Call SUT.
        mManagerMock.shutdownNow()

        verify(mExecutorMock, times(1)).shutdownNow()
    }

    private fun createMockBeingList(count: Int): List<BeingCallable> {
        val list = (1..count).map {
            mock<BeingCallable>()
        }

        return spy(list)
    }
}
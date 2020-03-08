package edu.vandy.simulator.managers.palantiri.reentrantLockHashMapSimpleSemaphore

import admin.AssignmentTests
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.*
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import kotlin.random.Random

@ExperimentalCoroutinesApi
class Assignment_2B_SimpleSemaphoreTest : AssignmentTests() {
    private val palantirCount = Random.nextInt(5, 20)

    @Mock
    lateinit var lockMock: Lock

    @Mock
    lateinit var notZeroMock: Condition

    @InjectMocks
    lateinit var semaphore: SimpleSemaphore

    @Test
    fun `acquire one permit`() {
        semaphore.mPermits = palantirCount
        val inOrder = inOrder(lockMock)

        // SUT
        semaphore.acquire()

        inOrder.verify(lockMock).lockInterruptibly()
        inOrder.verify(lockMock).unlock()
        val expectedAvailablePermits = palantirCount - 1
        assertEquals(
                "Available permits should be $expectedAvailablePermits",
                expectedAvailablePermits,
                semaphore.availablePermits())
    }

    @Test
    fun `acquire one permit when none are immediately available`() {
        val expectedAwaitCalls = Random.nextInt(100, 200)
        semaphore.mPermits = -(expectedAwaitCalls - 1)
        whenever(notZeroMock.await()).thenAnswer {
            if (semaphore.mPermits <= 0) {
                semaphore.mPermits++
            }
            Unit
        }
        val inOrder = inOrder(lockMock, notZeroMock)

        // SUT
        semaphore.acquire()

        inOrder.verify(lockMock).lockInterruptibly()
        inOrder.verify(notZeroMock, times(expectedAwaitCalls)).await()
        inOrder.verify(lockMock).unlock()
        val expectedAvailablePermits = 0
        assertEquals(
                "Available permits should be $expectedAvailablePermits",
                expectedAvailablePermits,
                semaphore.availablePermits())
    }

    @Test
    fun `acquire all permits`() {
        semaphore.mPermits = palantirCount
        val inOrder = inOrder(lockMock)

        // SUT
        for (i in 0 until palantirCount) {
            semaphore.acquire()
        }

        for (i in 0 until palantirCount) {
            inOrder.verify(lockMock).lockInterruptibly()
            inOrder.verify(lockMock).unlock()
        }
        val expectedAvailablePermits = 0
        assertEquals(
                "Available permits should be $expectedAvailablePermits",
                expectedAvailablePermits,
                semaphore.availablePermits())
    }

    @Test
    fun `acquire multiple permits with await calls`() {
        semaphore.mPermits = -palantirCount
        doAnswer {
            semaphore.mPermits++
            null
        }.whenever(notZeroMock).await()
        val inOrder = inOrder(lockMock, notZeroMock)

        // SUT
        semaphore.acquire()

        inOrder.verify(lockMock).lockInterruptibly()
        inOrder.verify(notZeroMock, times(palantirCount + 1)).await()
        inOrder.verify(lockMock).unlock()
        val expectedAvailablePermits = 0
        assertEquals(
                "Available permits should be $expectedAvailablePermits",
                expectedAvailablePermits,
                semaphore.availablePermits())
    }

    @Test
    fun `acquire one permit with an await call`() {
        semaphore.mPermits = 0
        doAnswer {
            semaphore.mPermits = 1
            null
        }.whenever(notZeroMock).await()
        val inOrder = inOrder(lockMock, notZeroMock)

        // SUT
        semaphore.acquire()

        inOrder.verify(lockMock).lockInterruptibly()
        inOrder.verify(notZeroMock).await()
        inOrder.verify(lockMock).unlock()
        val expectedAvailablePermits = 0
        assertEquals(
                "Available permits should be $expectedAvailablePermits",
                expectedAvailablePermits,
                semaphore.availablePermits())
    }

    @Test
    fun `acquire permit with await call interrupted`() {
        semaphore.mPermits = 0
        doThrow(InterruptedException("Mock interrupt")).whenever(notZeroMock).await()
        val inOrder = inOrder(lockMock, notZeroMock)

        // SUT
        assertThrows<InterruptedException>(
                "Interrupt should not be swallowed by call to acquire().") {
            semaphore.acquire()
        }

        inOrder.verify(lockMock).lockInterruptibly()
        inOrder.verify(notZeroMock).await()
        inOrder.verify(lockMock).unlock()
        val expectedAvailablePermits = 0
        assertEquals(
                "Available permits should be $expectedAvailablePermits",
                expectedAvailablePermits,
                semaphore.availablePermits())
    }

    @Test
    fun `acquire permit uninterruptibly with permits available`() {
        val semaphoreMock = mock<SimpleSemaphore>()
        doCallRealMethod().whenever(semaphoreMock).acquireUninterruptibly()

        // SUT
        semaphoreMock.acquireUninterruptibly()

        assertFalse(Thread.currentThread().isInterrupted)

        verify(semaphoreMock).acquireUninterruptibly()
        verify(semaphoreMock).acquire()
        verifyNoMoreInteractions(semaphoreMock)
    }

    @Test
    fun `acquire permit uninterruptibly should not be interruptible`() {
        val semaphoreMock = mock<SimpleSemaphore>()
        whenever(semaphoreMock.acquire())
                .thenThrow(InterruptedException("Mock exception"))
                .thenAnswer { Unit }
        doCallRealMethod().whenever(semaphoreMock).acquireUninterruptibly()

        // SUT
        semaphoreMock.acquireUninterruptibly()

        verify(semaphoreMock).acquireUninterruptibly()
        verify(semaphoreMock, times(2)).acquire()
        verifyNoMoreInteractions(semaphoreMock)
    }

    @Test
    fun `acquire permit uninterruptibly should set interrupt flag if interrupted`() {
        val semaphoreMock = mock<SimpleSemaphore>()
        whenever(semaphoreMock.acquire())
                .thenThrow(InterruptedException("Mock exception"))
                .thenAnswer { Unit }
        doCallRealMethod().whenever(semaphoreMock).acquireUninterruptibly()

        // SUT
        semaphoreMock.acquireUninterruptibly()

        assertTrue(Thread.currentThread().isInterrupted)
    }

    @Test
    fun `release permit with signal`() {
        semaphore.mPermits = 0
        val inOrder = inOrder(lockMock, notZeroMock)

        // SUT
        semaphore.release()

        inOrder.verify(lockMock).lock()
        inOrder.verify(notZeroMock).signal()
        inOrder.verify(lockMock).unlock()
        val expectedAvailablePermits = 1
        assertEquals(
                "Available permits should be $expectedAvailablePermits",
                expectedAvailablePermits,
                semaphore.availablePermits())
    }

    @Test
    fun `release permit with no signal`() {
        semaphore.mPermits = -1
        val inOrder = inOrder(lockMock, notZeroMock)

        // SUT
        semaphore.release()

        inOrder.verify(lockMock).lock()
        inOrder.verify(lockMock).unlock()
        verify(notZeroMock, never()).signal()
        val expectedAvailablePermits = 0
        assertEquals(
                "Available permits should be $expectedAvailablePermits",
                expectedAvailablePermits,
                semaphore.availablePermits())
    }
}

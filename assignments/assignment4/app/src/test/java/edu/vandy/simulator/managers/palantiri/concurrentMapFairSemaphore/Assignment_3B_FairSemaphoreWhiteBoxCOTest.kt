package edu.vandy.simulator.managers.palantiri.concurrentMapFairSemaphore

import admin.*
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test
import org.mockito.InOrder
import org.mockito.Mock
import org.mockito.quality.Strictness
import java.util.*
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.test.*

class Assignment_3B_FairSemaphoreWhiteBoxCOTest : AssignmentTests() {
    @Mock
    private lateinit var lockMock: ReentrantLock

    @Mock
    private lateinit var queueMock: LinkedList<FairSemaphoreCO.Waiter?>

    @Mock
    private lateinit var fairSemaphoreMock: FairSemaphoreCO

    @Mock
    private lateinit var waiterMock: FairSemaphoreCO.Waiter

    @Mock
    private lateinit var waiterLockMock: ReentrantLock

    @Mock
    private lateinit var waiterConditionMock: Condition

    private var lockCount = 0
    private var waiterLockCount = 0

    /**
     * Handle mock injections manually so that Mockito doesn't get
     * confused about which locks belong where.
     */
    @Before
    fun before() {
        graduateTest()
        queueMock.injectInto(fairSemaphoreMock)

        lockMock.injectInto(fairSemaphoreMock, Lock::class.java, ReentrantLock::class.java)
        waiterLockMock.injectInto(waiterMock, Lock::class.java, ReentrantLock::class.java)
        waiterConditionMock.injectInto(waiterMock)

        setupLockMock()
        setupWaiterLockMock()

        // This prevents warnings about unused doAnswers that
        // are defined globally by the previous setup...() calls.
        // This is necessary because we never know what methods students
        // will call so we need to stub many methods to catch all
        // possibilities.
        mockitoRule.strictness(Strictness.LENIENT)
    }

    @Test
    fun testAvailablePermits() {
        val expected = 999
        permits = expected
        doCallRealMethod().whenever(fairSemaphoreMock).availablePermits()

        // SUT
        val result = fairSemaphoreMock.availablePermits()

        assertEquals(expected, result)
    }

    @Test
    fun testWaiterFields() {
        // Create real object (not a mock).
        val waiter = FairSemaphoreCO.Waiter()

        // Field may be declared as Lock or ReentrantLock, so check for either.
        val lock = waiter.value<Lock>(Lock::class.java, ReentrantLock::class.java)
        assertNotNull(lock, "Waiter class should have a non-null Lock field.")
        assertTrue(lock is ReentrantLock, "Waiter class Lock field should be set to a non-null ReentrantLock instance.")
        val condition = waiter.value<Condition>()
        assertNotNull(condition, "Waiter class should have a non-null Condition field.")
    }

    @Test
    fun testFairSemaphoreFields() {
        // Create real object (not a mock).
        val fairSemaphore = FairSemaphoreCO(PALANTIRI_COUNT)

        // Field may be declared as Lock or ReentrantLock, so check for either.
        val lock = fairSemaphore.value<Lock>(Lock::class.java, ReentrantLock::class.java)
        assertNotNull(lock, "FairSemaphoreCO class should have a non-null Lock field.")
        assertTrue(lock is ReentrantLock, "FairSemaphoreCO class Lock field should be set to a non-null ReentrantLock instance.")
        val queue = fairSemaphore.value<LinkedList<*>>()
        assertNotNull(queue, "FairSemaphoreCO class should have a non-null LinkedList field.")
        assertEquals(PALANTIRI_COUNT, fairSemaphore.availablePermits())
    }

    @Test
    fun testAcquireUninterruptibly() {
        doCallRealMethod().whenever(fairSemaphoreMock).acquireUninterruptibly()

        // SUT
        try {
            fairSemaphoreMock.acquireUninterruptibly()
            assertFalse(Thread.currentThread().isInterrupted, "Thread should not have interrupted flag set.")
        } catch (t: Throwable) {
            fail("Thread should not throw any exceptions.")
        }

        verify(fairSemaphoreMock).acquire()
    }

    @Test
    fun testAcquireUninterruptiblyWithInterrupt() {
        val interrupts = Random().nextInt(10)
        var loopCount = 0
        doAnswer {
            println("was interrupted = ${Thread.currentThread().isInterrupted}")
            assertFalse(Thread.currentThread().isInterrupted, "InterruptedException was not caught")
            loopCount++
            if (loopCount <= interrupts) {
                println("throwing exception $loopCount")
                throw InterruptedException("Mock interrupt.")
            }
            null
        }.whenever(fairSemaphoreMock).acquire()
        doCallRealMethod().whenever(fairSemaphoreMock).acquireUninterruptibly()

        // SUT
        fairSemaphoreMock.acquireUninterruptibly()

        verify(fairSemaphoreMock, times(interrupts + 1)).acquire()
        assertTrue(Thread.currentThread().isInterrupted, "Thread should have reset the Thread interrupted flag.")
    }

    @Test
    fun testAcquireWithNoBlocking() {
        whenever(fairSemaphoreMock.tryToGetPermit()).thenReturn(true)
        doCallRealMethod().whenever(fairSemaphoreMock).acquire()

        // SUT
        fairSemaphoreMock.acquire()

        verify(fairSemaphoreMock).tryToGetPermit()
        verify(fairSemaphoreMock, never()).waitForPermit()
    }

    @Test
    fun testAcquireWithBlocking() {
        whenever(fairSemaphoreMock.tryToGetPermit()).thenReturn(false)
        doCallRealMethod().whenever(fairSemaphoreMock).acquire()

        // SUT
        fairSemaphoreMock.acquire()

        verify(fairSemaphoreMock).tryToGetPermit()
        verify(fairSemaphoreMock).waitForPermit()
    }

    @Test
    fun testTryToGetPermitWhenLocked() {
        whenTryToGetPermitUnlockedCalledEnsureClassLockedAndReturn(true)
        doCallRealMethod().whenever(fairSemaphoreMock).tryToGetPermit()

        // SUT
        val result = fairSemaphoreMock.tryToGetPermit()

        assertTrue(result, "Method should have returned true.")
        verify(lockMock).lock()
        verify(lockMock).unlock()
    }

    @Test
    fun testTryToGetPermitWhenUnlocked() {
        whenTryToGetPermitUnlockedCalledEnsureClassLockedAndReturn(false)
        doCallRealMethod().whenever(fairSemaphoreMock).tryToGetPermit()

        // SUT
        val result = fairSemaphoreMock.tryToGetPermit()

        assertFalse(result, "Method should have returned false.")
        verify(lockMock).lock()
        verify(lockMock, never()).unlock()
    }

    @Test
    fun testAcquireWithInterrupt() {
        Thread.currentThread().interrupt()
        whenever(fairSemaphoreMock.tryToGetPermit()).thenReturn(false)
        doCallRealMethod().whenever(fairSemaphoreMock).acquire()

        // SUT
        try {
            fairSemaphoreMock.acquire()
            fail("Method should have thrown and InterruptedException.")
        } catch (e: InterruptedException) {
        }

        verify(fairSemaphoreMock, never()).tryToGetPermit()
        verify(fairSemaphoreMock, never()).waitForPermit()
    }

    @Test
    fun testTryToGetPermitUnlockedEmptyQueueAndNoAvailablePermits() {
        val availablePermits = 0
        val expectedResult = false
        val emptyQueue = 0

        // SUT
        testTryToGetPermitUnlocked(emptyQueue, availablePermits, availablePermits, expectedResult)
    }

    @Test
    fun testTryToGetPermitUnlockedEmptyQueueAndAvailablePermits() {
        val availablePermits = 1
        val expectedPermits = availablePermits - 1
        val expectedResult = true
        val emptyQueue = 0

        // SUT
        testTryToGetPermitUnlocked(emptyQueue, availablePermits, expectedPermits, expectedResult)
    }

    @Test
    fun testTryToGetPermitUnlockedFullQueueAndNoAvailablePermits() {
        val availablePermits = 0
        val expectedResult = false
        val notEmptyQueue = 1

        // SUT
        testTryToGetPermitUnlocked(notEmptyQueue, availablePermits, availablePermits, expectedResult)
    }

    @Test
    fun testTryToGetPermitUnlockedFullQueueAndAvailablePermits() {
        val availablePermits = 1
        val expectedResult = false
        val notEmptyQueue = 1

        // SUT
        testTryToGetPermitUnlocked(notEmptyQueue, availablePermits, availablePermits, expectedResult)
    }

    @Test
    fun testWaitForPermitWithNoBlocking() {
        // IMPORTANT:
        // The waitForPermit method should only be called when the class lock has
        // been locked by the calling method.
        lockCount = 1

        // Mock the Waiter.
        waiterMock.mReleased = true
        whenever(fairSemaphoreMock.createWaiter()).thenReturn(waiterMock)
        whenever(queueMock.add(waiterMock))
                .thenAnswer {
                    classLockedAndWaiterLocked()
                    true
                }
        doAnswer {
            classLockedAndWaiterLocked()
            true
        }.whenever(queueMock).add(waiterMock)
        doAnswer {
            classNotLockedAndWaiterLocked()
            true
        }.whenever(waiterConditionMock).await()
        val inOrder = inOrder(lockMock, queueMock, waiterLockMock)
        doCallRealMethod().whenever(fairSemaphoreMock).waitForPermit()

        // SUT
        fairSemaphoreMock.waitForPermit()

        verify(waiterLockMock).lock()
        verify(waiterLockMock).unlock()
        verifyQueueAddCalledOnce()
        verify(lockMock).unlock()

        // Since waiter has already been released (mReleased is true),
        // the SUT should not access any methods on the Condition mock.
        verifyNoMoreInteractions(waiterConditionMock)
        inOrder.verify(waiterLockMock).lock()
        inorderVerifyQueueAdd(inOrder)
        inOrder.verify(lockMock).unlock()
        inOrder.verify(waiterLockMock).unlock()
    }

    @Test
    fun testWaitForPermitWithBlocking() {
        // IMPORTANT:
        // The waitForPermit method should only be called when the class lock has
        // been locked by the calling method.
        lockCount = 1

        // Ensure that the Waiter mock is injected into the SUT method.
        whenever(fairSemaphoreMock.createWaiter()).thenReturn(waiterMock)

        // Set waiter to not be released so that SUT
        // will call condition await() and block.
        waiterMock.mReleased = false
        doAnswer {
            assertTrue(waiterLockCount == 1, "Waiter lock should be locked.")
            waiterMock.mReleased = true
            null
        }.whenever(waiterConditionMock).await()
        doCallRealMethod().whenever(fairSemaphoreMock).waitForPermit()
        val inOrder = inOrder(lockMock, queueMock, waiterLockMock, waiterConditionMock)

        // SUT
        fairSemaphoreMock.waitForPermit()

        verify(waiterLockMock).lock()
        verify(waiterLockMock).unlock()
        verifyQueueAddCalledOnce()
        verify(lockMock).unlock()
        verifyConditionAwaitCalledOnce()
        inOrder.verify(waiterLockMock).lock()
        inorderVerifyQueueAdd(inOrder)
        inOrder.verify(lockMock).unlock()
        inorderVerifyConditionAwait(inOrder)
        inOrder.verify(waiterLockMock).unlock()
    }

    @Test
    fun testWaitForPermitWithInterrupt() {
        // Mock the Waiter.
        whenever(fairSemaphoreMock.createWaiter()).thenReturn(waiterMock)
        waiterMock.mReleased = false
        doAnswer { throw InterruptedException("Mock interrupt") }.whenever(waiterConditionMock).await()

        // Return false so that implementation will call release().
        whenever(queueMock.remove(waiterMock)).thenReturn(false)
        doNothing().whenever(fairSemaphoreMock).release()
        val inOrder = inOrder(fairSemaphoreMock,
                lockMock,
                queueMock,
                waiterLockMock,
                waiterConditionMock)
        doCallRealMethod().whenever(fairSemaphoreMock).waitForPermit()

        // SUT
        try {
            fairSemaphoreMock.waitForPermit()
            fail("InterruptedException should have been rethrown.")
        } catch (t: Throwable) {
            assertTrue(t is InterruptedException, "InterruptedException should have been rethrown.")
        }

        verify(waiterLockMock).lock()
        verify(waiterLockMock).unlock()
        verify(queueMock).add(waiterMock)
        verify(queueMock).remove(waiterMock)
        verify(lockMock).lock()
        verify(lockMock, times(2)).unlock()
        verifyConditionAwaitCalledOnce()
        verify(fairSemaphoreMock).release()

        inOrder.verify(waiterLockMock).lock()
        inOrder.verify(queueMock).add(waiterMock)
        inOrder.verify(lockMock).unlock()
        inorderVerifyConditionAwait(inOrder)
        inOrder.verify(lockMock).lock()
        inOrder.verify(fairSemaphoreMock).release()
        inOrder.verify(lockMock).unlock()
        inOrder.verify(waiterLockMock).unlock()
    }

    @Test
    fun testReleaseWithEmptyQueue() {
        permits = 0
        val expectedPermits = permits + 1

        // Set Waiter mock field.
        waiterMock.mReleased = false
        whenQueuePollCalledVerifyAndReturn(classLockedAndWaiterNotLocked(), null)
        val inOrder = inOrder(lockMock,
                queueMock,
                waiterLockMock,
                waiterConditionMock)
        doCallRealMethod().whenever(fairSemaphoreMock).release()

        // SUT
        fairSemaphoreMock.release()

        assertEquals(expectedPermits, permits, "Available permits should be updated.")
        verify(lockMock).lock()
        verify(lockMock).unlock()
        classNotLockedAndWaiterNotLocked()
        verifyQueuePollCalledOnce()

        inOrder.verify(lockMock).lock()
        inorderVerifyQueuePoll(inOrder)
        inOrder.verify(lockMock).unlock()
    }

    @Test
    fun testReleaseLockingWithNotEmptyQueue() {
        // Don't care about FIFO implementation here, just the locking logic
        // so always return the correct Waiter mock.
        whenQueuePollCalledVerifyAndReturn(classLockedAndWaiterNotLocked(), waiterMock)
        whenConditionSignalCalledVerify(classNotLockedAndWaiterLocked())
        whenConditionSignalCalledVerify(classNotLockedAndWaiterLocked())
        val inOrder = inOrder(lockMock,
                queueMock,
                waiterLockMock,
                waiterConditionMock)
        doCallRealMethod().whenever(fairSemaphoreMock).release()
        doCallRealMethod().whenever(fairSemaphoreMock).availablePermits()

        // SUT
        fairSemaphoreMock.release()

        verify(queueMock, never()).size
        verify(queueMock, never()).removeFirst()
        verify(queueMock, never()).removeFirstOccurrence(any())
        verify(queueMock, never()).remove()

        verify(lockMock).lock()
        verify(waiterLockMock).lock()
        verify(waiterLockMock).unlock()
        verify(waiterConditionMock).signal()
        verifyQueuePollCalledOnce()

        inOrder.verify(lockMock).lock()
        inorderVerifyQueuePoll(inOrder)
        inOrder.verify(lockMock).unlock()
        inOrder.verify(waiterLockMock).lock()
        inOrder.verify(waiterConditionMock).signal()
        inOrder.verify(waiterLockMock).unlock()
        assertTrue(waiterMock.mReleased, "Waiter released flag should be set.")
    }

    @Test
    fun testReleaseFIFOWithNotEmptyQueue() {
        val queueSize = 2
        val expectedQueueSize = queueSize - 1

        // Care about FIFO implementation here so use a real queue with mock
        // Waiters. This allows the user to use more LinkedList methods.
        val queue = buildAndInjectQueue(queueSize)
        val expectedWaiter = queue.first
        permits = 1
        val expectedPermits = permits
        expectedWaiter.mReleased = false
        doCallRealMethod().whenever(fairSemaphoreMock).release()

        // SUT
        fairSemaphoreMock.release()

        assertEquals(expectedPermits, permits, "Available permits should not have changed.")
        assertEquals(expectedQueueSize, queue.size, "A Waiter entry should have been removed from the queue.")
        assertTrue(queue.first !== expectedWaiter, "The waiter was not removed using FIFO ordering.")
        assertTrue(expectedWaiter.mReleased, "Released Waiter's released flag should be set.")
        assertTrue(expectedWaiter.mReleased, "Waiter released flag should be set.")
    }

    private fun inorderVerifyQueueAdd(inOrder: InOrder) {
        try {
            inOrder.verify(queueMock).add(waiterMock)
        } catch (t: Throwable) {
            inOrder.verify(queueMock).addLast(waiterMock)
        }
    }

    private fun testTryToGetPermitUnlocked(
            queueSize: Int,
            availablePermits: Int,
            expectedPermits: Int,
            expectedResult: Boolean) {
        permits = availablePermits

        // Overwrite mock queue with a real one for this test.
        buildAndInjectQueue(queueSize)
        doCallRealMethod().whenever(fairSemaphoreMock).tryToGetPermitUnlocked()
        val result = fairSemaphoreMock.tryToGetPermitUnlocked()
        assertEquals(expectedResult, result, "Method returned wrong result.")
        assertEquals(expectedPermits, permits, "Available permits incorrect.")
    }

    private fun buildAndInjectQueue(size: Int): LinkedList<FairSemaphoreCO.Waiter> {
        val queue = LinkedList<FairSemaphoreCO.Waiter>()
        ReflectionHelper.injectValueIntoFirstMatchingField(fairSemaphoreMock, queue, LinkedList::class.java)
        for (i in 0 until size) {
            val waiterMock = mock<FairSemaphoreCO.Waiter>()
            ReflectionHelper.injectValueIntoFirstMatchingField(
                    waiterMock, mock<ReentrantLock>(), Lock::class.java, ReentrantLock::class.java)
            ReflectionHelper.injectValueIntoFirstMatchingField(
                    waiterMock, mock<Condition>(), Condition::class.java)
            queue.add(waiterMock)
        }
        return queue
    }

    private fun whenTryToGetPermitUnlockedCalledEnsureClassLockedAndReturn(b: Boolean) {
        whenever(fairSemaphoreMock.tryToGetPermitUnlocked())
                .thenAnswer {
                    assertTrue(lockCount == 1, "Object monitor should be locked.")
                    b
                }
    }

    private fun inorderVerifyConditionAwait(inOrder: InOrder) {
        try {
            inOrder.verify(waiterConditionMock).await()
        } catch (t: Throwable) {
            inOrder.verify(waiterConditionMock).awaitUninterruptibly()
        }
    }

    private fun verifyConditionAwaitCalledOnce() {
        try {
            verify(waiterConditionMock).await()
        } catch (t: Throwable) {
            verify(waiterConditionMock).awaitUninterruptibly()
        }
    }

    private fun verifyQueueAddCalledOnce() {
        try {
            verify(queueMock).add(waiterMock)
        } catch (t: Throwable) {
            verify(queueMock).addLast(waiterMock)
        }
    }

    private fun whenConditionSignalCalledVerify(verify: Runnable) {
        doAnswer {
            verify.run()
            waiterMock
        }.whenever(waiterConditionMock).signal()
    }

    private fun whenQueuePollCalledVerifyAndReturn(verify: Runnable, o: Any?) {
        // Return Waiter mock (queue not empty) for all expected queue removal calls.
        whenever(queueMock.poll())
                .thenAnswer {
                    verify.run()
                    o
                }
        whenever(queueMock.pollFirst())
                .thenAnswer {
                    verify.run()
                    o
                }
        whenever(queueMock.remove())
                .thenAnswer {
                    verify.run()
                    o
                }
        whenever(queueMock.removeFirst())
                .thenAnswer {
                    verify.run()
                    o
                }
    }

    private fun setupLockMock() {
        doAnswer {
            lockCount++
            null
        }.whenever(lockMock).lock()
        doAnswer {
            lockCount--
            null
        }.whenever(lockMock).unlock()
        setupWaiterLockMock()
    }

    private fun setupWaiterLockMock() {
        doAnswer {
            waiterLockCount++
            null
        }.whenever(waiterLockMock).lock()
        doAnswer {
            waiterLockCount--
            null
        }.whenever(waiterLockMock).unlock()
    }

    private var permits: Int
        get() = fairSemaphoreMock.getField("", Int::class.javaPrimitiveType!!)
        private set(value) {
            try {
                value.injectInto(fairSemaphoreMock)
            } catch (t: Throwable) {
                throw RuntimeException(t)
            }
        }

    private fun inorderVerifyQueuePoll(inOrder: InOrder) {
        try {
            inOrder.verify(queueMock).poll()
        } catch (t1: Throwable) {
            try {
                inOrder.verify(queueMock).pollFirst()
            } catch (t2: Throwable) {
                try {
                    inOrder.verify(queueMock).remove()
                } catch (t3: Throwable) {
                    inOrder.verify(queueMock).removeFirst()
                }
            }
        }
    }

    private fun verifyQueuePollCalledOnce() {
        try {
            verify(queueMock).poll()
        } catch (t1: Throwable) {
            try {
                verify(queueMock).pollFirst()
            } catch (t2: Throwable) {
                try {
                    verify(queueMock).remove()
                } catch (t3: Throwable) {
                    verify(queueMock).removeFirst()
                }
            }
        }
    }

    private fun classNotLockedAndWaiterNotLocked(): Runnable {
        return Runnable {
            assertClassNotLocked()
            assertWaiterNotLocked()
        }
    }

    private fun classNotLockedAndWaiterLocked(): Runnable {
        return Runnable {
            assertClassNotLocked()
            assertWaiterLocked()
        }
    }

    private fun classLockedAndWaiterNotLocked(): Runnable {
        return Runnable {
            assertClassLocked()
            assertWaiterNotLocked()
        }
    }

    private fun classLockedAndWaiterLocked(): Runnable {
        return Runnable {
            assertClassLocked()
            assertWaiterLocked()
        }
    }

    private fun assertClassLocked() {
        assertTrue(lockCount == 1, "Class lock should be locked.")
    }

    private fun assertClassNotLocked() {
        assertTrue(lockCount == 0, "Class lock should not be locked.")
    }

    private fun assertWaiterLocked() {
        assertTrue(waiterLockCount == 1, "Waiter lock should be locked.")
    }

    private fun assertWaiterNotLocked() {
        assertTrue(waiterLockCount == 0, "Waiter lock should not be locked.")
    }

    private inner class LoopCounter {
        var count = 0
        var interrupts = 0
    }

    companion object {
        private const val PALANTIRI_COUNT = 5
    }
}
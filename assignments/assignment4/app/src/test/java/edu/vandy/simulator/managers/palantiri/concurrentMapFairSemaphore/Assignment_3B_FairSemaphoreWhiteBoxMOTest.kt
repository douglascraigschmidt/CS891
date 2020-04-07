package edu.vandy.simulator.managers.palantiri.concurrentMapFairSemaphore

import admin.AssignmentTests
import admin.ReflectionHelper
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test
import org.mockito.InOrder
import org.mockito.Mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.quality.Strictness
import java.util.*
import kotlin.test.*

class Assignment_3B_FairSemaphoreWhiteBoxMOTest : AssignmentTests() {
    @Mock
    private lateinit var mQueueMock: LinkedList<FairSemaphoreMO.Waiter>

    @Mock
    private lateinit var mFairSemaphoreMock: FairSemaphoreMO

    @Mock
    private lateinit var mWaiterMock: FairSemaphoreMO.Waiter

    /**
     * Handle mock injections manually so that Mockito doesn't get
     * confused about which locks belong where.
     */
    @Before
    fun before() {
        undergraduateTest()
        ReflectionHelper.injectValueIntoFirstMatchingField(
                mFairSemaphoreMock, mQueueMock, LinkedList::class.java)

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
        doCallRealMethod().whenever(mFairSemaphoreMock).availablePermits()
        val result = mFairSemaphoreMock.availablePermits()
        assertEquals(expected, result)
    }

    @Test
    fun testWaiterFields() {
        val waiter = FairSemaphoreMO.Waiter()

        // Field may be declared as Lock or ReentrantLock, so check for either.
        val released = ReflectionHelper.findFirstMatchingFieldValue<Boolean>(waiter, Boolean::class.javaPrimitiveType)
        assertNotNull(released, "Waiter class should have a boolean field field.")
        assertFalse(released, "Waiter class boolean field should be set to a false.")
    }

    @Test
    fun testFairSemaphoreFields() {
        val fairSemaphore = FairSemaphoreMO(PALANTIRI_COUNT)
        val queue = ReflectionHelper.findFirstMatchingFieldValue<LinkedList<*>>(fairSemaphore, LinkedList::class.java)
        assertNotNull(queue, "FairSemaphoreMO class should have a non-null LinkedList field.")
        assertEquals(PALANTIRI_COUNT, fairSemaphore.availablePermits())
    }

    @Test
    fun testAcquireUninterruptibly() {
        doCallRealMethod().whenever(mFairSemaphoreMock).acquireUninterruptibly()
        try {
            mFairSemaphoreMock.acquireUninterruptibly()
            assertFalse(Thread.currentThread().isInterrupted,
                    "Thread should not have interrupted flag set.")
        } catch (t: Throwable) {
            fail("Thread should not throw any exceptions.")
        }
        verify(mFairSemaphoreMock, times(1)).acquire()
    }

    @Test
    fun testAcquireUninterruptiblyWithInterrupt() {
        val loopCounter = LoopCounter()
        val interrupts = 1
        val expectedCount = 2
        try {
            doAnswer {
                loopCounter.count++
                if (++loopCounter.interrupts <= interrupts) {
                    throw InterruptedException("Mock interrupt.")
                }
                null
            }.whenever(mFairSemaphoreMock).acquire()
        } catch (e: InterruptedException) {
            fail("This should never happen.")
        }
        doCallRealMethod().whenever(mFairSemaphoreMock).acquireUninterruptibly()
        mFairSemaphoreMock.acquireUninterruptibly()
        assertTrue(Thread.currentThread().isInterrupted,
                "Thread should have reset the Thread interrupted flag.")
        assertEquals(expectedCount, loopCounter.count, "loop should have run $expectedCount times.")
    }

    @Test
    fun testAcquireWithNoBlocking() {
        whenever(mFairSemaphoreMock.tryToGetPermit()).thenReturn(true)
        doCallRealMethod().whenever(mFairSemaphoreMock).acquire()
        mFairSemaphoreMock.acquire()
        verify(mFairSemaphoreMock, times(1)).tryToGetPermit()
        verify(mFairSemaphoreMock, never()).waitForPermit()
    }

    @Test
    fun testAcquireWithBlocking() {
        whenever(mFairSemaphoreMock.tryToGetPermit()).thenReturn(false)
        doCallRealMethod().whenever(mFairSemaphoreMock).acquire()
        mFairSemaphoreMock.acquire()
        verify(mFairSemaphoreMock, times(1)).tryToGetPermit()
        verify(mFairSemaphoreMock, times(1)).waitForPermit()
    }

    @Test
    fun testAcquireWithInterrupt() {
        Thread.currentThread().interrupt()
        whenever(mFairSemaphoreMock.tryToGetPermit()).thenReturn(false)
        doCallRealMethod().whenever(mFairSemaphoreMock).acquire()
        try {
            mFairSemaphoreMock.acquire()
            fail("Method should have thrown and InterruptedException.")
        } catch (e: InterruptedException) {
        }
        verify(mFairSemaphoreMock, never()).tryToGetPermit()
        verify(mFairSemaphoreMock, never()).waitForPermit()
    }

    @Test
    fun testTryToGetPermitWhenNotLocked() {
        whenTryToGetPermitUnlockedVerify(classLockedAndWaiterNotLocked())
        doCallRealMethod().whenever(mFairSemaphoreMock).tryToGetPermit()
        val result = mFairSemaphoreMock.tryToGetPermit()
        assertTrue(result, "Method should have returned true.")
    }

    private fun whenTryToGetPermitUnlockedVerify(verify: Runnable) {
        whenever(mFairSemaphoreMock.tryToGetPermitUnlocked())
                .thenAnswer {
                    verify.run()
                    true
                }
    }

    @Test
    fun testTryToGetPermitUnlockedEmptyQueueAndNoAvailablePermits() {
        val availablePermits = 0
        val expectedResult = false
        val emptyQueue = 0
        testTryToGetPermitUnlocked(emptyQueue, availablePermits, availablePermits, expectedResult)
    }

    @Test
    fun testTryToGetPermitUnlockedEmptyQueueAndAvailablePermits() {
        val availablePermits = 1
        val expectedPermits = availablePermits - 1
        val expectedResult = true
        val emptyQueue = 0
        testTryToGetPermitUnlocked(emptyQueue, availablePermits, expectedPermits, expectedResult)
    }

    @Test
    fun testTryToGetPermitUnlockedFullQueueAndNoAvailablePermits() {
        val availablePermits = 0
        val expectedResult = false
        val notEmptyQueue = 1
        testTryToGetPermitUnlocked(notEmptyQueue, availablePermits, availablePermits, expectedResult)
    }

    @Test
    fun testTryToGetPermitUnlockedFullQueueAndAvailablePermits() {
        val availablePermits = 1
        val expectedResult = false
        val notEmptyQueue = 1
        testTryToGetPermitUnlocked(notEmptyQueue, availablePermits, availablePermits, expectedResult)
    }

    private fun testTryToGetPermitUnlocked(
            queueSize: Int,
            availablePermits: Int,
            expectedPermits: Int,
            expectedResult: Boolean) {
        permits = availablePermits

        // Overwrite mock queue with a real one for this test.
        buildAndInjectQueue(queueSize)
        doCallRealMethod().whenever(mFairSemaphoreMock).tryToGetPermitUnlocked()
        val result = mFairSemaphoreMock.tryToGetPermitUnlocked()
        assertEquals(expectedResult, result, "Method returned wrong result.")
        assertEquals(expectedPermits, permits, "Available permits incorrect.")
    }

    @Test
    fun testWaitForPermitWithNoBlocking() {
        // Mock the Waiter.
        mWaiterMock.mReleased = true
        whenever(mFairSemaphoreMock.createWaiter()).thenReturn(mWaiterMock)
        doCallRealMethod().whenever(mFairSemaphoreMock).waitForPermit()
        whenTryToGetPermitUnlockedCalledVerifyAndReturn(classLockedAndWaiterLocked(), true)
        whenQueueAddCalledVerify(classLockedAndWaiterLocked())
        whenWaiterWaitCalledVerifyNotReleasedAnd(classNotLockedAndWaiterLocked())
        mFairSemaphoreMock.waitForPermit()
        verify(mFairSemaphoreMock, times(1)).tryToGetPermitUnlocked()
    }

    @Test
    fun testWaitForPermitWithBlocking() {
        // Set release flag to false so that wait() will be called.
        mWaiterMock.mReleased = false

        // Ensure that the Waiter mock is injected into the SUT method.
        whenever(mFairSemaphoreMock.createWaiter()).thenReturn(mWaiterMock)
        whenTryToGetPermitUnlockedCalledVerifyAndReturn(classLockedAndWaiterLocked(), false)
        whenQueueAddCalledVerify(classLockedAndWaiterLocked())
        whenWaiterWaitCalledVerifyAndRelease(classNotLockedAndWaiterLocked())
        val inOrder = inOrder(mFairSemaphoreMock, mQueueMock, mWaiterMock)
        doCallRealMethod().whenever(mFairSemaphoreMock).waitForPermit()
        mFairSemaphoreMock.waitForPermit()
        verify(mFairSemaphoreMock, times(1)).createWaiter()
        verify(mFairSemaphoreMock, times(1)).tryToGetPermitUnlocked()
        verifyQueueAddCalledOnce(mWaiterMock)
        verify(mWaiterMock as Object, times(1)).wait()
        verify(mFairSemaphoreMock, never()).release()
        inOrder.verify(mFairSemaphoreMock).createWaiter()
        inorderVerifyQueueAdd(inOrder)
        inOrder.verify(mWaiterMock as Object).wait()
    }

    @Test
    fun testWaitForPermitWithBlockingAndInterruptWhileQueued() {
        // Set release flag to false so that wait() will be called.
        mWaiterMock.mReleased = false

        // Ensure that the Waiter mock is injected into the SUT method.
        whenever(mFairSemaphoreMock.createWaiter()).thenReturn(mWaiterMock)

        // Return true when queue remove called so that method will not call release().
        whenever(mQueueMock.remove(mWaiterMock)).thenReturn(true)
        whenTryToGetPermitUnlockedCalledVerifyAndReturn(classLockedAndWaiterLocked(), false)
        whenQueueAddCalledVerify(classLockedAndWaiterLocked())
        whenWaiterWaitCalledVerifyNotReleasedAndInterrupt(classNotLockedAndWaiterLocked())
        val inOrder = inOrder(mFairSemaphoreMock, mQueueMock, mWaiterMock)
        doCallRealMethod().whenever(mFairSemaphoreMock).waitForPermit()
        try {
            mFairSemaphoreMock.waitForPermit()
            fail("InterruptedException should have been rethrown.")
        } catch (t: Throwable) {
            assertTrue(t is InterruptedException, "InterruptedException should have been rethrown.")
        }
        verify(mFairSemaphoreMock, times(1)).createWaiter()
        verify(mFairSemaphoreMock, times(1)).tryToGetPermitUnlocked()
        verifyQueueAddCalledOnce(mWaiterMock)
        verify(mWaiterMock as Object, times(1)).wait()
        verify(mFairSemaphoreMock, never()).release()
        inOrder.verify(mFairSemaphoreMock).createWaiter()
        inorderVerifyQueueAdd(inOrder)
        inOrder.verify(mWaiterMock as Object).wait()
        inOrder.verify(mQueueMock).remove(mWaiterMock)
    }

    @Test
    fun testWaitForPermitWithBlockingAndInterruptWhenNotQueued() {
        // Set release flag to false so that wait() will be called.
        mWaiterMock.mReleased = false

        // Ensure that the Waiter mock is injected into the SUT method.
        whenever(mFairSemaphoreMock.createWaiter()).thenReturn(mWaiterMock)

        // Return true when queue remove called so that method will call release().
        whenever(mQueueMock.remove(mWaiterMock)).thenReturn(false)
        whenTryToGetPermitUnlockedCalledVerifyAndReturn(classLockedAndWaiterLocked(), false)
        whenQueueAddCalledVerify(classLockedAndWaiterLocked())
        whenWaiterWaitCalledVerifyNotReleasedAndInterrupt(classNotLockedAndWaiterLocked())
        val inOrder = inOrder(mFairSemaphoreMock, mQueueMock, mWaiterMock)
        doCallRealMethod().whenever(mFairSemaphoreMock).waitForPermit()
        try {
            mFairSemaphoreMock.waitForPermit()
            fail("InterruptedException should have been rethrown.")
        } catch (t: Throwable) {
            assertTrue(t is InterruptedException, "InterruptedException should have been rethrown.")
        }
        verify(mFairSemaphoreMock, times(1)).createWaiter()
        verify(mFairSemaphoreMock, times(1)).tryToGetPermitUnlocked()
        verifyQueueAddCalledOnce(mWaiterMock)
        verify(mWaiterMock as Object, times(1)).wait()
        verify(mFairSemaphoreMock, times(1)).release()
        inOrder.verify(mFairSemaphoreMock).createWaiter()
        inorderVerifyQueueAdd(inOrder)
        inOrder.verify(mWaiterMock as Object).wait()
        inOrder.verify(mQueueMock).remove(mWaiterMock)
        inOrder.verify(mFairSemaphoreMock).release()
    }

    @Test
    fun testReleaseWithEmptyQueue() {
        permits = 0
        val expectedPermits = permits + 1

        // Set Waiter mock field.
        mWaiterMock.mReleased = false
        whenQueuePollCalledVerifyAndReturn(classLockedAndWaiterNotLocked(), null)
        doCallRealMethod().whenever(mFairSemaphoreMock).release()
        mFairSemaphoreMock.release()
        assertEquals(expectedPermits, permits, "Available permits should be updated.")
        verifyQueuePollCalledOnce()
    }

    @Test
    fun testReleaseLockingWithNotEmptyQueue() {
        whenQueuePollCalledVerifyAndReturn(classLockedAndWaiterNotLocked(), mWaiterMock)
        whenWaiterNotifyCalledVerify(classLockedAndWaiterLocked(mWaiterMock))
        doCallRealMethod().whenever(mFairSemaphoreMock).release()
        mFairSemaphoreMock.release()
    }

    @Test
    fun testReleaseFIFOWithNotEmptyQueue() {
        val queueSize = 2
        val expectedQueueSize = queueSize - 1

        // Use a real queue so that all LinkedList methods will function
        // normally in called method and check that FIFO ordering is used.
        val queue = buildAndInjectQueue(queueSize)
        val expectedWaiter = queue.first
        permits = 1
        val expectedPermits = permits
        expectedWaiter.mReleased = false
        doCallRealMethod().whenever(mFairSemaphoreMock).release()
        mFairSemaphoreMock.release()
        assertEquals(expectedPermits, permits, "Available permits should not have changed.")
        assertEquals(expectedQueueSize, queue.size, "A Waiter entry should have been removed from the queue.")
        assertTrue(queue.first !== expectedWaiter, "The waiter was not removed using FIFO ordering.")
        assertTrue(expectedWaiter.mReleased, "Released Waiter's released flag should be set.")
    }

    private fun buildAndInjectQueue(size: Int): LinkedList<FairSemaphoreMO.Waiter> {
        val queue = LinkedList<FairSemaphoreMO.Waiter>()
        ReflectionHelper.injectValueIntoFirstMatchingField(mFairSemaphoreMock, queue, LinkedList::class.java)
        for (i in 0 until size) {
            queue.add(mock())
        }
        return queue
    }

    private fun whenWaiterWaitCalledVerifyNotReleasedAnd(verify: Runnable) {
        doAnswer {
            assertFalse(mWaiterMock.mReleased)
            verify.run()
            null
        }.whenever(mWaiterMock as Object).wait()
    }

    private fun whenWaiterWaitCalledVerifyNotReleasedAndInterrupt(verify: Runnable) {
        doAnswer {
            assertFalse(mWaiterMock.mReleased)
            verify.run()
            throw InterruptedException("Mock interrupt.")
        }.whenever(mWaiterMock as Object).wait()
    }

    private fun whenQueueAddCalledVerify(verify: Runnable) {
        doAnswer {
            verify.run()
            true
        }.whenever(mQueueMock).add(mWaiterMock)
        doAnswer {
            classLockedAndWaiterLocked()
            true
        }.whenever(mQueueMock).addLast(mWaiterMock)
        doAnswer {
            classLockedAndWaiterLocked()
            true
        }.whenever(mQueueMock).add(mQueueMock.size, mWaiterMock)
    }

    private fun inorderVerifyQueueAdd(inOrder: InOrder) {
        try {
            inOrder.verify(mQueueMock).add(mWaiterMock)
        } catch (t1: Throwable) {
            try {
                inOrder.verify(mQueueMock).addLast(mWaiterMock)
            } catch (t2: Throwable) {
                inOrder.verify(mQueueMock).add(mQueueMock.size, mWaiterMock)
            }
        }
    }

    private fun whenTryToGetPermitUnlockedCalledVerifyAndReturn(
            verify: Runnable, returnValue: Boolean) {
        whenever(mQueueMock.add(mWaiterMock))
                .thenAnswer {
                    verify.run()
                    returnValue
                }
    }

    private fun whenWaiterWaitCalledVerifyAndRelease(verify: Runnable) {
        doAnswer {
            verify.run()
            // Set Waiter released flag so that caller will unblock.
            mWaiterMock.mReleased = true
            null
        }.whenever(mWaiterMock as Object).wait()
    }

    private fun whenWaiterNotifyCalledVerify(verify: Runnable) {
        doAnswer {
            verify.run()
            null
        }.whenever(mWaiterMock as Object).wait()
    }

    private fun whenQueuePollCalledVerifyAndReturn(verify: Runnable, o: Any?) {
        // Return Waiter mock (queue not empty) for all expected queue removal calls.
        whenever(mQueueMock.poll())
                .thenAnswer {
                    verify.run()
                    o
                }
        whenever(mQueueMock.pollFirst())
                .thenAnswer {
                    verify.run()
                    o
                }
        whenever(mQueueMock.remove())
                .thenAnswer {
                    verify.run()
                    o
                }
        whenever(mQueueMock.removeFirst())
                .thenAnswer {
                    verify.run()
                    o
                }
    }

    private fun verifyQueuePollCalledOnce() {
        try {
            verify(mQueueMock, times(1)).poll()
        } catch (t1: Throwable) {
            try {
                verify(mQueueMock, times(1)).pollFirst()
            } catch (t2: Throwable) {
                try {
                    verify(mQueueMock, times(1)).remove()
                } catch (t3: Throwable) {
                    verify(mQueueMock, times(1)).removeFirst()
                }
            }
        }
    }

    private fun inorderVerifyQueuePoll(inOrder: InOrder) {
        try {
            inOrder.verify(mQueueMock).poll()
        } catch (t1: Throwable) {
            try {
                inOrder.verify(mQueueMock).pollFirst()
            } catch (t2: Throwable) {
                try {
                    inOrder.verify(mQueueMock).remove()
                } catch (t3: Throwable) {
                    inOrder.verify(mQueueMock).removeFirst()
                }
            }
        }
    }

    private fun verifyQueueAddCalledOnce(waiter: FairSemaphoreMO.Waiter) {
        try {
            verify(mQueueMock, times(1)).add(waiter)
        } catch (t1: Throwable) {
            try {
                verify(mQueueMock, times(1)).add(mQueueMock.size, waiter)
            } catch (t2: Throwable) {
                verify(mQueueMock, times(1)).addLast(waiter)
            }
        }
    }

    private var permits: Int
        private get() = ReflectionHelper.findFirstMatchingFieldValue(mFairSemaphoreMock, Int::class.javaPrimitiveType)
        private set(value) {
            try {
                ReflectionHelper.injectValueIntoFirstMatchingField(
                        mFairSemaphoreMock, value, Int::class.javaPrimitiveType)
            } catch (t: Throwable) {
                throw RuntimeException(t)
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

    private fun classLockedAndWaiterLocked(waiter: FairSemaphoreMO.Waiter): Runnable {
        return Runnable {
            assertClassLocked()
            assertWaiterLocked(waiter)
        }
    }

    private fun assertClassLocked() {
        assertTrue(Thread.holdsLock(mFairSemaphoreMock), "Class lock should be locked.")

    }

    private fun assertClassNotLocked() {
        assertTrue(!Thread.holdsLock(mFairSemaphoreMock), "Class lock should not be locked.")

    }

    private fun assertWaiterLocked() {
        assertTrue(Thread.holdsLock(mWaiterMock), "Waiter lock should be locked.")
    }

    private fun assertWaiterLocked(waiter: FairSemaphoreMO.Waiter) {
        assertTrue(Thread.holdsLock(waiter), "Waiter lock should be locked.")
    }

    private fun assertWaiterNotLocked() {
        assertTrue(!Thread.holdsLock(mWaiterMock), "Waiter lock should not be locked.")

    }

    private inner class LoopCounter {
        var count = 0
        var interrupts = 0
    }

    companion object {
        private const val PALANTIRI_COUNT = 5
    }
}
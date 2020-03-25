package edu.vandy.simulator.managers.palantiri.concurrentMapFairSemaphore;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

import java.util.LinkedList;

import admin.AssignmentTestRule;
import admin.AssignmentTests;
import admin.ReflectionHelper;
import edu.vandy.simulator.utils.Assignment;

import static edu.vandy.simulator.utils.Assignment.UNDERGRADUATE;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class Assignment_3B_FairSemaphoreWhiteBoxMOTest extends AssignmentTests {
    private static final int PALANTIRI_COUNT = 5;

    @Mock
    private LinkedList<FairSemaphoreMO.Waiter> mQueueMock;
    @Mock
    private FairSemaphoreMO mFairSemaphoreMock;
    @Mock
    private FairSemaphoreMO.Waiter mWaiterMock;

    /**
     * Handle mock injections manually so that Mockito doesn't get
     * confused about which locks belong where.
     */
    @Before
    public void before() throws Exception {
        undergraduateTest();

        ReflectionHelper.injectValueIntoFirstMatchingField(
                mFairSemaphoreMock, mQueueMock, LinkedList.class);

        // This prevents warnings about unused doAnswers that
        // are defined globally by the previous setup...() calls.
        // This is necessary because we never know what methods students
        // will call so we need to stub many methods to catch all
        // possibilities.
        mockitoRule.strictness(Strictness.LENIENT);
    }

    @Test
    public void testAvailablePermits() {
        int expected = 999;
        setPermits(expected);

        doCallRealMethod().when(mFairSemaphoreMock).availablePermits();
        int result = mFairSemaphoreMock.availablePermits();

        assertEquals(expected, result);
    }

    @Test
    public void testWaiterFields() {
        FairSemaphoreMO.Waiter waiter = new FairSemaphoreMO.Waiter();

        // Field may be declared as Lock or ReentrantLock, so check for either.
        Boolean released = ReflectionHelper.findFirstMatchingFieldValue(waiter, boolean.class);
        assertNotNull("Waiter class should have a boolean field field.", released);
        assertFalse("Waiter class boolean field should be set to a false.", released);
    }

    @Test
    public void testFairSemaphoreFields() {
        FairSemaphoreMO fairSemaphore = new FairSemaphoreMO(PALANTIRI_COUNT);

        LinkedList<?> queue = ReflectionHelper.findFirstMatchingFieldValue(fairSemaphore, LinkedList.class);
        assertNotNull("FairSemaphoreMO class should have a non-null LinkedList field.", queue);

        assertEquals(PALANTIRI_COUNT, fairSemaphore.availablePermits());
    }

    @Test
    public void testAcquireUninterruptibly() throws Exception {
        doCallRealMethod().when(mFairSemaphoreMock).acquireUninterruptibly();

        try {
            mFairSemaphoreMock.acquireUninterruptibly();
            assertFalse("Thread should not have interrupted flag set.",
                    Thread.currentThread().isInterrupted());
        } catch (Throwable t) {
            fail("Thread should not throw any exceptions.");
        }

        verify(mFairSemaphoreMock, times(1)).acquire();
    }

    @Test
    public void testAcquireUninterruptiblyWithInterrupt() {
        LoopCounter loopCounter = new LoopCounter();
        final int interrupts = 1;
        final int expectedCount = 2;

        try {
            doAnswer(invocation -> {
                loopCounter.count++;
                if (++loopCounter.interrupts <= interrupts) {
                    throw new InterruptedException("Mock interrupt.");
                }
                return null;
            }).when(mFairSemaphoreMock).acquire();
        } catch (InterruptedException e) {
            fail("This should never happen.");
        }

        doCallRealMethod().when(mFairSemaphoreMock).acquireUninterruptibly();

        mFairSemaphoreMock.acquireUninterruptibly();

        assertTrue("Thread should have reset the Thread interrupted flag.",
                Thread.currentThread().isInterrupted());

        assertEquals("loop should have run " + expectedCount + "times",
                expectedCount, loopCounter.count);
    }

    @Test
    public void testAcquireWithNoBlocking() throws Exception {
        when(mFairSemaphoreMock.tryToGetPermit()).thenReturn(true);
        doCallRealMethod().when(mFairSemaphoreMock).acquire();
        mFairSemaphoreMock.acquire();

        verify(mFairSemaphoreMock, times(1)).tryToGetPermit();
        verify(mFairSemaphoreMock, never()).waitForPermit();
    }

    @Test
    public void testAcquireWithBlocking() throws Exception {
        when(mFairSemaphoreMock.tryToGetPermit()).thenReturn(false);
        doCallRealMethod().when(mFairSemaphoreMock).acquire();
        mFairSemaphoreMock.acquire();

        verify(mFairSemaphoreMock, times(1)).tryToGetPermit();
        verify(mFairSemaphoreMock, times(1)).waitForPermit();
    }

    @Test
    public void testAcquireWithInterrupt() throws Exception {
        Thread.currentThread().interrupt();
        when(mFairSemaphoreMock.tryToGetPermit()).thenReturn(false);
        doCallRealMethod().when(mFairSemaphoreMock).acquire();

        try {
            mFairSemaphoreMock.acquire();
            fail("Method should have thrown and InterruptedException.");
        } catch (InterruptedException e) {
        }

        verify(mFairSemaphoreMock, never()).tryToGetPermit();
        verify(mFairSemaphoreMock, never()).waitForPermit();
    }

    @Test
    public void testTryToGetPermitWhenNotLocked() {
        whenTryToGetPermitUnlockedVerify(classLockedAndWaiterNotLocked());

        doCallRealMethod().when(mFairSemaphoreMock).tryToGetPermit();
        boolean result = mFairSemaphoreMock.tryToGetPermit();

        assertTrue("Method should have returned true.", result);
    }

    private void whenTryToGetPermitUnlockedVerify(Runnable verify) {
        when(mFairSemaphoreMock.tryToGetPermitUnlocked())
                .thenAnswer(invocation -> {
                    verify.run();
                    return true;
                });
    }

    @Test
    public void testTryToGetPermitUnlockedEmptyQueueAndNoAvailablePermits() throws Exception {
        int availablePermits = 0;
        int expectedPermits = availablePermits;
        boolean expectedResult = false;
        int emptyQueue = 0;

        testTryToGetPermitUnlocked(emptyQueue, availablePermits, expectedPermits, expectedResult);
    }

    @Test
    public void testTryToGetPermitUnlockedEmptyQueueAndAvailablePermits() throws Exception {
        int availablePermits = 1;
        int expectedPermits = availablePermits - 1;
        boolean expectedResult = true;
        int emptyQueue = 0;

        testTryToGetPermitUnlocked(emptyQueue, availablePermits, expectedPermits, expectedResult);
    }

    @Test
    public void testTryToGetPermitUnlockedFullQueueAndNoAvailablePermits() throws Exception {
        int availablePermits = 0;
        int expectedPermits = availablePermits;
        boolean expectedResult = false;
        int notEmptyQueue = 1;

        testTryToGetPermitUnlocked(notEmptyQueue, availablePermits, expectedPermits, expectedResult);
    }

    @Test
    public void testTryToGetPermitUnlockedFullQueueAndAvailablePermits() throws Exception {
        int availablePermits = 1;
        int expectedPermits = availablePermits;
        boolean expectedResult = false;
        int notEmptyQueue = 1;

        testTryToGetPermitUnlocked(notEmptyQueue, availablePermits, expectedPermits, expectedResult);
    }

    private void testTryToGetPermitUnlocked(
            int queueSize,
            int availablePermits,
            int expectedPermits,
            boolean expectedResult) throws Exception {
        setPermits(availablePermits);

        // Overwrite mock queue with a real one for this test.
        buildAndInjectQueue(queueSize);

        doCallRealMethod().when(mFairSemaphoreMock).tryToGetPermitUnlocked();
        boolean result = mFairSemaphoreMock.tryToGetPermitUnlocked();

        assertEquals("Method returned wrong result.", expectedResult, result);

        assertEquals("Available permits incorrect.", expectedPermits, getPermits());
    }

    @Test
    public void testWaitForPermitWithNoBlocking() throws Exception {
        // Mock the Waiter.
        mWaiterMock.mReleased = true;
        when(mFairSemaphoreMock.createWaiter()).thenReturn(mWaiterMock);

        doCallRealMethod().when(mFairSemaphoreMock).waitForPermit();

        whenTryToGetPermitUnlockedCalledVerifyAndReturn(classLockedAndWaiterLocked(), true);

        whenQueueAddCalledVerify(classLockedAndWaiterLocked());

        whenWaiterWaitCalledVerifyNotReleasedAnd(classNotLockedAndWaiterLocked());

        mFairSemaphoreMock.waitForPermit();

        verify(mFairSemaphoreMock, times(1)).tryToGetPermitUnlocked();
    }

    @Test
    public void testWaitForPermitWithBlocking() throws Exception {
        // Set release flag to false so that wait() will be called.
        mWaiterMock.mReleased = false;

        // Ensure that the Waiter mock is injected into the SUT method.
        when(mFairSemaphoreMock.createWaiter()).thenReturn(mWaiterMock);

        whenTryToGetPermitUnlockedCalledVerifyAndReturn(classLockedAndWaiterLocked(), false);

        whenQueueAddCalledVerify(classLockedAndWaiterLocked());

        whenWaiterWaitCalledVerifyAndRelease(classNotLockedAndWaiterLocked());

        InOrder inOrder = inOrder(mFairSemaphoreMock, mQueueMock, mWaiterMock);

        doCallRealMethod().when(mFairSemaphoreMock).waitForPermit();
        mFairSemaphoreMock.waitForPermit();

        verify(mFairSemaphoreMock, times(1)).createWaiter();
        verify(mFairSemaphoreMock, times(1)).tryToGetPermitUnlocked();
        verifyQueueAddCalledOnce(mWaiterMock);
        verify(mWaiterMock, times(1)).wait();
        verify(mFairSemaphoreMock, never()).release();

        inOrder.verify(mFairSemaphoreMock).createWaiter();
        inorderVerifyQueueAdd(inOrder);
        inOrder.verify(mWaiterMock).wait();
    }

    @Test
    public void testWaitForPermitWithBlockingAndInterruptWhileQueued() throws Exception {
        // Set release flag to false so that wait() will be called.
        mWaiterMock.mReleased = false;

        // Ensure that the Waiter mock is injected into the SUT method.
        when(mFairSemaphoreMock.createWaiter()).thenReturn(mWaiterMock);

        // Return true when queue remove called so that method will not call release().
        when(mQueueMock.remove(mWaiterMock)).thenReturn(true);

        whenTryToGetPermitUnlockedCalledVerifyAndReturn(classLockedAndWaiterLocked(), false);

        whenQueueAddCalledVerify(classLockedAndWaiterLocked());

        whenWaiterWaitCalledVerifyNotReleasedAndInterrupt(classNotLockedAndWaiterLocked());

        InOrder inOrder = inOrder(mFairSemaphoreMock, mQueueMock, mWaiterMock);

        doCallRealMethod().when(mFairSemaphoreMock).waitForPermit();

        try {
            mFairSemaphoreMock.waitForPermit();
            fail("InterruptedException should have been rethrown.");
        } catch (Throwable t) {
            assertTrue("InterruptedException should have been rethrown.",
                    t instanceof InterruptedException);
        }

        verify(mFairSemaphoreMock, times(1)).createWaiter();
        verify(mFairSemaphoreMock, times(1)).tryToGetPermitUnlocked();
        verifyQueueAddCalledOnce(mWaiterMock);
        verify(mWaiterMock, times(1)).wait();
        verify(mFairSemaphoreMock, never()).release();

        inOrder.verify(mFairSemaphoreMock).createWaiter();
        inorderVerifyQueueAdd(inOrder);
        inOrder.verify(mWaiterMock).wait();
        inOrder.verify(mQueueMock).remove(mWaiterMock);
    }

    @Test
    public void testWaitForPermitWithBlockingAndInterruptWhenNotQueued() throws Exception {
        // Set release flag to false so that wait() will be called.
        mWaiterMock.mReleased = false;

        // Ensure that the Waiter mock is injected into the SUT method.
        when(mFairSemaphoreMock.createWaiter()).thenReturn(mWaiterMock);

        // Return true when queue remove called so that method will call release().
        when(mQueueMock.remove(mWaiterMock)).thenReturn(false);

        whenTryToGetPermitUnlockedCalledVerifyAndReturn(classLockedAndWaiterLocked(), false);

        whenQueueAddCalledVerify(classLockedAndWaiterLocked());

        whenWaiterWaitCalledVerifyNotReleasedAndInterrupt(classNotLockedAndWaiterLocked());

        InOrder inOrder = inOrder(mFairSemaphoreMock, mQueueMock, mWaiterMock);

        doCallRealMethod().when(mFairSemaphoreMock).waitForPermit();

        try {
            mFairSemaphoreMock.waitForPermit();
            fail("InterruptedException should have been rethrown.");
        } catch (Throwable t) {
            assertTrue("InterruptedException should have been rethrown.",
                    t instanceof InterruptedException);
        }

        verify(mFairSemaphoreMock, times(1)).createWaiter();
        verify(mFairSemaphoreMock, times(1)).tryToGetPermitUnlocked();
        verifyQueueAddCalledOnce(mWaiterMock);
        verify(mWaiterMock, times(1)).wait();
        verify(mFairSemaphoreMock, times(1)).release();

        inOrder.verify(mFairSemaphoreMock).createWaiter();
        inorderVerifyQueueAdd(inOrder);
        inOrder.verify(mWaiterMock).wait();
        inOrder.verify(mQueueMock).remove(mWaiterMock);
        inOrder.verify(mFairSemaphoreMock).release();
    }

    @Test
    public void testReleaseWithEmptyQueue() {
        setPermits(0);
        int expectedPermits = getPermits() + 1;

        // Set Waiter mock field.
        mWaiterMock.mReleased = false;

        whenQueuePollCalledVerifyAndReturn(classLockedAndWaiterNotLocked(), null);

        doCallRealMethod().when(mFairSemaphoreMock).release();

        mFairSemaphoreMock.release();

        assertEquals("Available permits should be updated.", expectedPermits, getPermits());

        verifyQueuePollCalledOnce();
    }

    @Test
    public void testReleaseLockingWithNotEmptyQueue() throws Exception {
        whenQueuePollCalledVerifyAndReturn(classLockedAndWaiterNotLocked(), mWaiterMock);

        whenWaiterNotifyCalledVerify(classLockedAndWaiterLocked(mWaiterMock));

        doCallRealMethod().when(mFairSemaphoreMock).release();
        mFairSemaphoreMock.release();
    }

    @Test
    public void testReleaseFIFOWithNotEmptyQueue() throws Exception {
        int queueSize = 2;
        int expectedQueueSize = queueSize - 1;

        // Use a real queue so that all LinkedList methods will function
        // normally in called method and check that FIFO ordering is used.
        LinkedList<FairSemaphoreMO.Waiter> queue = buildAndInjectQueue(queueSize);
        FairSemaphoreMO.Waiter expectedWaiter = queue.getFirst();

        setPermits(1);
        int expectedPermits = getPermits();

        expectedWaiter.mReleased = false;

        doCallRealMethod().when(mFairSemaphoreMock).release();
        mFairSemaphoreMock.release();

        assertEquals("Available permits should not have changed.", expectedPermits, getPermits());

        assertEquals("A Waiter entry should have been removed from the queue.",
                expectedQueueSize, queue.size());

        assertTrue("The waiter was not removed using FIFO ordering.",
                queue.getFirst() != expectedWaiter);

        assertTrue("Released Waiter's released flag should be set.", expectedWaiter.mReleased);
    }

    private LinkedList<FairSemaphoreMO.Waiter> buildAndInjectQueue(int size) throws Exception {
        LinkedList<FairSemaphoreMO.Waiter> queue = new LinkedList<>();
        ReflectionHelper.injectValueIntoFirstMatchingField(mFairSemaphoreMock, queue, LinkedList.class);
        for (int i = 0; i < size; i++) {
            queue.add(mock(FairSemaphoreMO.Waiter.class));
        }
        return queue;
    }

    private void whenWaiterWaitCalledVerifyNotReleasedAnd(Runnable verify) throws InterruptedException {
        doAnswer(invocation -> {
            assertFalse(mWaiterMock.mReleased);
            verify.run();
            return null;
        }).when(mWaiterMock).wait();
    }

    private void whenWaiterWaitCalledVerifyNotReleasedAndInterrupt(Runnable verify) throws InterruptedException {
        doAnswer(invocation -> {
            assertFalse(mWaiterMock.mReleased);
            verify.run();
            throw new InterruptedException("Mock interrupt.");
        }).when(mWaiterMock).wait();
    }

    private void whenQueueAddCalledVerify(Runnable verify) {
        doAnswer(invocation -> {
            verify.run();
            return true;
        }).when(mQueueMock).add(mWaiterMock);

        doAnswer(invocation -> {
            classLockedAndWaiterLocked();
            return true;
        }).when(mQueueMock).addLast(mWaiterMock);

        doAnswer(invocation -> {
            classLockedAndWaiterLocked();
            return true;
        }).when(mQueueMock).add(mQueueMock.size(), mWaiterMock);
    }

    private void inorderVerifyQueueAdd(InOrder inOrder) {
        try {
            inOrder.verify(mQueueMock).add(mWaiterMock);
        } catch (Throwable t1) {
            try {
                inOrder.verify(mQueueMock).addLast(mWaiterMock);
            } catch (Throwable t2) {
                inOrder.verify(mQueueMock).add(mQueueMock.size(), mWaiterMock);
            }
        }
    }

    private void whenTryToGetPermitUnlockedCalledVerifyAndReturn(
            Runnable verify, boolean returnValue) {
        when(mQueueMock.add(mWaiterMock))
                .thenAnswer(invocation -> {
                    verify.run();
                    return returnValue;
                });
    }

    private void whenWaiterWaitCalledVerifyAndRelease(Runnable verify) throws InterruptedException {
        doAnswer(invocation -> {
            verify.run();
            // Set Waiter released flag so that caller will unblock.
            mWaiterMock.mReleased = true;
            return null;
        }).when(mWaiterMock).wait();
    }

    private void whenWaiterNotifyCalledVerify(Runnable verify) throws InterruptedException {
        doAnswer(invocation -> {
            verify.run();
            return null;
        }).when(mWaiterMock).wait();
    }

    private void whenQueuePollCalledVerifyAndReturn(Runnable verify, Object o) {
        // Return Waiter mock (queue not empty) for all expected queue removal calls.
        when(mQueueMock.poll())
                .thenAnswer(invocation -> {
                    verify.run();
                    return o;
                });

        when(mQueueMock.pollFirst())
                .thenAnswer(invocation -> {
                    verify.run();
                    return o;
                });

        when(mQueueMock.remove())
                .thenAnswer(invocation -> {
                    verify.run();
                    return o;
                });

        when(mQueueMock.removeFirst())
                .thenAnswer(invocation -> {
                    verify.run();
                    return o;
                });
    }

    private void verifyQueuePollCalledOnce() {
        try {
            verify(mQueueMock, times(1)).poll();
        } catch (Throwable t1) {
            try {
                verify(mQueueMock, times(1)).pollFirst();
            } catch (Throwable t2) {
                try {
                    verify(mQueueMock, times(1)).remove();
                } catch (Throwable t3) {
                    verify(mQueueMock, times(1)).removeFirst();
                }
            }
        }
    }

    private void inorderVerifyQueuePoll(InOrder inOrder) {
        try {
            inOrder.verify(mQueueMock).poll();
        } catch (Throwable t1) {
            try {
                inOrder.verify(mQueueMock).pollFirst();
            } catch (Throwable t2) {
                try {
                    inOrder.verify(mQueueMock).remove();
                } catch (Throwable t3) {
                    inOrder.verify(mQueueMock).removeFirst();
                }
            }
        }
    }

    private void verifyQueueAddCalledOnce(FairSemaphoreMO.Waiter waiter) {
        try {
            verify(mQueueMock, times(1)).add(waiter);
        } catch (Throwable t1) {
            try {
                verify(mQueueMock, times(1)).add(mQueueMock.size(), waiter);
            } catch (Throwable t2) {
                verify(mQueueMock, times(1)).addLast(waiter);
            }
        }
    }

    private int getPermits() {
        Integer available =
                ReflectionHelper.findFirstMatchingFieldValue(mFairSemaphoreMock, int.class);
        assert available != null;
        return available;
    }

    private void setPermits(int value) {
        try {
            ReflectionHelper.injectValueIntoFirstMatchingField(
                    mFairSemaphoreMock, value, int.class);
        } catch (Throwable t) {
            throw new RuntimeException(t);

        }
    }

    private Runnable classNotLockedAndWaiterNotLocked() {
        return () -> {
            assertClassNotLocked();
            assertWaiterNotLocked();
        };
    }

    private Runnable classNotLockedAndWaiterLocked() {
        return () -> {
            assertClassNotLocked();
            assertWaiterLocked();
        };
    }

    private Runnable classLockedAndWaiterNotLocked() {
        return () -> {
            assertClassLocked();
            assertWaiterNotLocked();
        };
    }

    private Runnable classLockedAndWaiterLocked() {
        return () -> {
            assertClassLocked();
            assertWaiterLocked();
        };
    }

    private Runnable classLockedAndWaiterLocked(FairSemaphoreMO.Waiter waiter) {
        return () -> {
            assertClassLocked();
            assertWaiterLocked(waiter);
        };
    }

    private void assertClassLocked() {
        assertTrue("Class lock should be locked.",
                Thread.holdsLock(mFairSemaphoreMock));
    }

    private void assertClassNotLocked() {
        assertTrue("Class lock should not be locked.",
                !Thread.holdsLock(mFairSemaphoreMock));
    }

    private void assertWaiterLocked() {
        assertTrue("Waiter lock should be locked.",
                Thread.holdsLock(mWaiterMock));
    }

    private void assertWaiterLocked(FairSemaphoreMO.Waiter waiter) {
        assertTrue("Waiter lock should be locked.",
                Thread.holdsLock(waiter));
    }

    private void assertWaiterNotLocked() {
        assertTrue("Waiter lock should not be locked.",
                !Thread.holdsLock(mWaiterMock));
    }

    private class LoopCounter {
        int count = 0;
        int interrupts = 0;
    }
}
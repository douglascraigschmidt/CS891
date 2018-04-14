package edu.vandy.simulator.managers.palantiri.concurrentMapFairSemaphore;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.vandy.simulator.ReflectionHelper;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FairSemaphoreWhiteBoxCOTest {
    private static final int PALANTIRI_COUNT = 5;
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock
    private ReentrantLock mLockMock;
    @Mock
    private LinkedList<FairSemaphoreCO.Waiter> mQueueMock;
    @Mock
    private FairSemaphoreCO mFairSemaphoreMock;

    @Mock
    private FairSemaphoreCO.Waiter mWaiterMock;
    @Mock
    private ReentrantLock mWaiterLockMock = mock(ReentrantLock.class);
    @Mock
    private Condition mWaiterConditionMock;

    private int mLockCount = 0;
    private int mWaiterLockCount = 0;

    /**
     * Handle mock injections manually so that Mockito doesn't get
     * confused about which locks belong where.
     */
    @Before
    public void before() throws Exception {
        ReflectionHelper.injectValueIntoFirstMatchingField(
                mFairSemaphoreMock, mQueueMock, LinkedList.class);
        ReflectionHelper.injectValueIntoFirstMatchingField(
                mFairSemaphoreMock, mLockMock, Lock.class, ReentrantLock.class);
        ReflectionHelper.injectValueIntoFirstMatchingField(
                mWaiterMock, mWaiterLockMock, Lock.class, ReentrantLock.class);
        ReflectionHelper.injectValueIntoFirstMatchingField(
                mWaiterMock, mWaiterConditionMock, Condition.class);

        setupLockMock();
        setupWaiterLockMock();

        // This prevents warnings about unused doAnswers that
        // are defined globally by the previous setup...() calls.
        // This is necessary because we never know what methods students
        // will call so we need to stub many methods to catch all
        // possibilities.
        mockitoRule.strictness(Strictness.LENIENT);
    }


    @Test(timeout = 2000)
    public void testAvailablePermits() {
        if (Helpers.ignoreTest(FairSemaphoreCO.class)) {
            return;
        }

        int expected = 999;
        setPermits(expected);

        doCallRealMethod().when(mFairSemaphoreMock).availablePermits();
        int result = mFairSemaphoreMock.availablePermits();

        assertEquals(expected, result);
    }

    @Test(timeout = 2000)
    public void testWaiterFields() {
        if (Helpers.ignoreTest(FairSemaphoreCO.class)) {
            return;
        }

        // Create real object (not a mock).
        FairSemaphoreCO.Waiter waiter = new FairSemaphoreCO.Waiter();

        // Field may be declared as Lock or ReentrantLock, so check for either.
        Lock lock = ReflectionHelper.findFirstlMatchingFieldValue(waiter, Lock.class, ReentrantLock.class);
        assertNotNull("Waiter class should have a non-null Lock field.", lock);
        assertTrue("Waiter class Lock field should be set to a non-null ReentrantLock instance.",
                lock instanceof ReentrantLock);

        Condition condition = ReflectionHelper.findFirstMatchingFieldValue(waiter, Condition.class);
        assertNotNull("Waiter class should have a non-null Condition field.", condition);
    }

    @Test(timeout = 2000)
    public void testFairSemaphoreFields() {
        if (Helpers.ignoreTest(FairSemaphoreCO.class)) {
            return;
        }

        // Create real object (not a mock).
        FairSemaphoreCO fairSemaphore = new FairSemaphoreCO(PALANTIRI_COUNT);

        // Field may be declared as Lock or ReentrantLock, so check for either.
        Lock lock = ReflectionHelper.findFirstlMatchingFieldValue(fairSemaphore, Lock.class, ReentrantLock.class);
        assertNotNull("FairSemaphoreCO class should have a non-null Lock field.", lock);
        assertTrue("FairSemaphoreCO class Lock field should be set to a non-null ReentrantLock instance.",
                lock instanceof ReentrantLock);

        LinkedList<?> queue = ReflectionHelper.findFirstMatchingFieldValue(fairSemaphore, LinkedList.class);
        assertNotNull("FairSemaphoreCO class should have a non-null LinkedList field.", queue);

        assertEquals(PALANTIRI_COUNT, fairSemaphore.availablePermits());
    }

    @Test(timeout = 2000)
    public void testAcquireUninterruptibly() throws Exception {
        if (Helpers.ignoreTest(FairSemaphoreCO.class)) {
            return;
        }

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

    @Test(timeout = 2000)
    public void testAcquireUninterruptiblyWithInterrupt() {
        if (Helpers.ignoreTest(FairSemaphoreCO.class)) {
            return;
        }

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

    @Test(timeout = 2000)
    public void testAcquireWithNoBlocking() throws Exception {
        if (Helpers.ignoreTest(FairSemaphoreCO.class)) {
            return;
        }

        when(mFairSemaphoreMock.tryToGetPermit()).thenReturn(true);
        doCallRealMethod().when(mFairSemaphoreMock).acquire();
        mFairSemaphoreMock.acquire();

        verify(mFairSemaphoreMock, times(1)).tryToGetPermit();
        verify(mFairSemaphoreMock, never()).waitForPermit();
    }

    @Test(timeout = 2000)
    public void testAcquireWithBlocking() throws Exception {
        if (Helpers.ignoreTest(FairSemaphoreCO.class)) {
            return;
        }

        when(mFairSemaphoreMock.tryToGetPermit()).thenReturn(false);
        doCallRealMethod().when(mFairSemaphoreMock).acquire();
        mFairSemaphoreMock.acquire();

        verify(mFairSemaphoreMock, times(1)).tryToGetPermit();
        verify(mFairSemaphoreMock, times(1)).waitForPermit();
    }

    @Test(timeout = 2000)
    public void testTryToGetPermitWhenLocked() {
        if (Helpers.ignoreTest(FairSemaphoreCO.class)) {
            return;
        }

        whenTryToGetPermitUnlockedCalledEnsureClassLockedAndReturn(true);

        doCallRealMethod().when(mFairSemaphoreMock).tryToGetPermit();
        boolean result = mFairSemaphoreMock.tryToGetPermit();

        assertTrue("Method should have returned true.", result);

        verify(mLockMock, times(1)).lock();
        verify(mLockMock, times(1)).unlock();
    }

    @Test(timeout = 2000)
    public void testTryToGetPermitWhenUnlocked() {
        if (Helpers.ignoreTest(FairSemaphoreCO.class)) {
            return;
        }

        whenTryToGetPermitUnlockedCalledEnsureClassLockedAndReturn(false);

        doCallRealMethod().when(mFairSemaphoreMock).tryToGetPermit();
        boolean result = mFairSemaphoreMock.tryToGetPermit();

        assertFalse("Method should have returned false.", result);

        verify(mLockMock, times(1)).lock();
        verify(mLockMock, never()).unlock();
    }

    @Test(timeout = 2000)
    public void testAcquireWithInterrupt() throws Exception {
        if (Helpers.ignoreTest(FairSemaphoreCO.class)) {
            return;
        }

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

    @Test(timeout = 2000)
    public void testTryToGetPermitUnlockedEmptyQueueAndNoAvailablePermits() throws Exception {
        if (Helpers.ignoreTest(FairSemaphoreCO.class)) {
            return;
        }

        int availablePermits = 0;
        int expectedPermits = availablePermits;
        boolean expectedResult = false;
        int emptyQueue = 0;

        testTryToGetPermitUnlocked(emptyQueue, availablePermits, expectedPermits, expectedResult);
    }

    @Test(timeout = 2000)
    public void testTryToGetPermitUnlockedEmptyQueueAndAvailablePermits() throws Exception {
        if (Helpers.ignoreTest(FairSemaphoreCO.class)) {
            return;
        }

        int availablePermits = 1;
        int expectedPermits = availablePermits - 1;
        boolean expectedResult = true;
        int emptyQueue = 0;

        testTryToGetPermitUnlocked(emptyQueue, availablePermits, expectedPermits, expectedResult);
    }

    @Test(timeout = 2000)
    public void testTryToGetPermitUnlockedFullQueueAndNoAvailablePermits() throws Exception {
        if (Helpers.ignoreTest(FairSemaphoreCO.class)) {
            return;
        }

        int availablePermits = 0;
        int expectedPermits = availablePermits;
        boolean expectedResult = false;
        int notEmptyQueue = 1;

        testTryToGetPermitUnlocked(notEmptyQueue, availablePermits, expectedPermits, expectedResult);
    }

    @Test(timeout = 2000)
    public void testTryToGetPermitUnlockedFullQueueAndAvailablePermits() throws Exception {
        if (Helpers.ignoreTest(FairSemaphoreCO.class)) {
            return;
        }

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

    @Test(timeout = 2000)
    public void testWaitForPermitWithNoBlocking() throws Exception {
        if (Helpers.ignoreTest(FairSemaphoreCO.class)) {
            return;
        }

        // IMPORTANT:
        // The waitForPermit method should only be called when the class lock has
        // been locked by the calling method.
        mLockCount = 1;

        // Mock the Waiter.
        mWaiterMock.mReleased = true;
        when(mFairSemaphoreMock.createWaiter()).thenReturn(mWaiterMock);

        doCallRealMethod().when(mFairSemaphoreMock).waitForPermit();

        when(mQueueMock.add(mWaiterMock))
                .thenAnswer(invocation -> {
                    classLockedAndWaiterLocked();
                    return true;
                });

        doAnswer(invocation -> {
            classLockedAndWaiterLocked();
            return true;
        }).when(mQueueMock).add(mWaiterMock);

        doAnswer(invocation -> {
            classNotLockedAndWaiterLocked();
            return true;
        }).when(mWaiterConditionMock).await();

        InOrder inOrder = inOrder(mLockMock, mQueueMock, mWaiterLockMock);
        mFairSemaphoreMock.waitForPermit();

        verify(mWaiterLockMock, times(1)).lock();
        verify(mWaiterLockMock, times(1)).unlock();
        verifyQueueAddCalledOnce();
        verify(mLockMock, times(1)).unlock();

        // Since waiter has already been released (mReleased is true),
        // the SUT should not access any methods on the Condition mock.
        Mockito.verifyNoMoreInteractions(mWaiterConditionMock);

        inOrder.verify(mWaiterLockMock).lock();
        inorderVerifyQueueAdd(inOrder);
        inOrder.verify(mLockMock).unlock();
        inOrder.verify(mWaiterLockMock).unlock();
    }

    private void inorderVerifyQueueAdd(InOrder inOrder) {
        try {
            inOrder.verify(mQueueMock).add(mWaiterMock);
        } catch (Throwable t) {
            inOrder.verify(mQueueMock).addLast(mWaiterMock);
        }
    }

    @Test(timeout = 2000)
    public void testWaitForPermitWithBlocking() throws Exception {
        if (Helpers.ignoreTest(FairSemaphoreCO.class)) {
            return;
        }

        // IMPORTANT:
        // The waitForPermit method should only be called when the class lock has
        // been locked by the calling method.
        mLockCount = 1;

        // Ensure that the Waiter mock is injected into the SUT method.
        when(mFairSemaphoreMock.createWaiter()).thenReturn(mWaiterMock);

        // Set waiter to not be released so that SUT
        // will call condition await() and block.
        mWaiterMock.mReleased = false;
        doAnswer(invocation -> {
            assertTrue("Waiter lock should be locked.", mWaiterLockCount == 1);
            mWaiterMock.mReleased = true;
            return null;
        }).when(mWaiterConditionMock).await();

        doCallRealMethod().when(mFairSemaphoreMock).waitForPermit();

        InOrder inOrder = inOrder(mLockMock, mQueueMock, mWaiterLockMock, mWaiterConditionMock);
        mFairSemaphoreMock.waitForPermit();

        verify(mWaiterLockMock, times(1)).lock();
        verify(mWaiterLockMock, times(1)).unlock();
        verifyQueueAddCalledOnce();
        verify(mLockMock, times(1)).unlock();
        verifyConditionAwaitCalledOnce();

        inOrder.verify(mWaiterLockMock).lock();
        inorderVerifyQueueAdd(inOrder);
        inOrder.verify(mLockMock).unlock();
        inorderVerifyConditionAwait(inOrder);
        inOrder.verify(mWaiterLockMock).unlock();
    }

    @Test(timeout = 2000)
    public void testWaitForPermitWithInterrupt() throws Exception {
        if (Helpers.ignoreTest(FairSemaphoreCO.class)) {
            return;
        }

        // Mock the Waiter.
        when(mFairSemaphoreMock.createWaiter()).thenReturn(mWaiterMock);
        mWaiterMock.mReleased = false;
        doAnswer(invocation -> {
            throw new InterruptedException("Mock interrupt");
        }).when(mWaiterConditionMock).await();

        // Return false so that implementation will call release().
        when(mQueueMock.remove(mWaiterMock)).thenReturn(false);
        doNothing().when(mFairSemaphoreMock).release();

        doCallRealMethod().when(mFairSemaphoreMock).waitForPermit();

        InOrder inOrder =
                inOrder(mFairSemaphoreMock,
                        mLockMock,
                        mQueueMock,
                        mWaiterLockMock,
                        mWaiterConditionMock);

        try {
            mFairSemaphoreMock.waitForPermit();
            fail("InterruptedException should have been rethrown.");
        } catch (Throwable t) {
            assertTrue("InterruptedException should have been rethrown.",
                    t instanceof InterruptedException);
        }

        verify(mWaiterLockMock, times(1)).lock();
        verify(mWaiterLockMock, times(1)).unlock();
        verify(mQueueMock, times(1)).add(mWaiterMock);
        verify(mQueueMock, times(1)).remove(mWaiterMock);
        verify(mLockMock, times(1)).lock();
        verify(mLockMock, times(2)).unlock();
        verifyConditionAwaitCalledOnce();
        verify(mFairSemaphoreMock, times(1)).release();

        inOrder.verify(mWaiterLockMock).lock();
        inOrder.verify(mQueueMock).add(mWaiterMock);
        inOrder.verify(mLockMock).unlock();
        inorderVerifyConditionAwait(inOrder);
        inOrder.verify(mLockMock).lock();
        inOrder.verify(mFairSemaphoreMock).release();
        inOrder.verify(mLockMock).unlock();
        inOrder.verify(mWaiterLockMock).unlock();
    }

    @Test//(timeout = 2000)
    public void testReleaseWithEmptyQueue() {
        if (Helpers.ignoreTest(FairSemaphoreCO.class)) {
            return;
        }

        setPermits(0);
        int expectedPermits = getPermits() + 1;

        // Set Waiter mock field.
        mWaiterMock.mReleased = false;

        whenQueuePollCalledVerifyAndReturn(classLockedAndWaiterNotLocked(), null);

        InOrder inOrder =
                inOrder(mLockMock,
                        mQueueMock,
                        mWaiterLockMock,
                        mWaiterConditionMock);

        doCallRealMethod().when(mFairSemaphoreMock).release();

        mFairSemaphoreMock.release();

        assertEquals("Available permits should be updated.", expectedPermits, getPermits());

        verify(mLockMock, times(1)).lock();
        verify(mLockMock, times(1)).unlock();

        classNotLockedAndWaiterNotLocked();

        verifyQueuePollCalledOnce();

        inOrder.verify(mLockMock).lock();
        inorderVerifyQueuePoll(inOrder);
        inOrder.verify(mLockMock).unlock();
    }

    @Test(timeout = 2000)
    public void testReleaseLockingWithNotEmptyQueue() throws Exception {
        if (Helpers.ignoreTest(FairSemaphoreCO.class)) {
            return;
        }

        // Don't care about FIFO implementation here, just the locking logic
        // so always return the correct Waiter mock.

        whenQueuePollCalledVerifyAndReturn(classLockedAndWaiterNotLocked(), mWaiterMock);

        whenConditionSignalCalledVerify(classNotLockedAndWaiterLocked());

        whenConditionSignalCalledVerify(classNotLockedAndWaiterLocked());

        InOrder inOrder =
                inOrder(mLockMock,
                        mQueueMock,
                        mWaiterLockMock,
                        mWaiterConditionMock);

        doCallRealMethod().when(mFairSemaphoreMock).release();

        mFairSemaphoreMock.release();

        verify(mLockMock, times(1)).lock();
        verify(mWaiterLockMock, times(1)).lock();
        verify(mWaiterLockMock, times(1)).unlock();
        verify(mWaiterConditionMock, times(1)).signal();

        verifyQueuePollCalledOnce();

        inOrder.verify(mLockMock).lock();
        inorderVerifyQueuePoll(inOrder);
        inOrder.verify(mLockMock).unlock();
        inOrder.verify(mWaiterLockMock).lock();
        inOrder.verify(mWaiterConditionMock).signal();
        inOrder.verify(mWaiterLockMock).unlock();

        assertTrue("Waiter released flag should be set.", mWaiterMock.mReleased);
    }

    @Test(timeout = 2000)
    public void testReleaseFIFOWithNotEmptyQueue() throws Exception {
        if (Helpers.ignoreTest(FairSemaphoreCO.class)) {
            return;
        }

        int queueSize = 2;
        int expectedQueueSize = queueSize - 1;

        // Care about FIFO implementation here so use a real queue with mock
        // Waiters. This allows the user to use more LinkedList methods.
        LinkedList<FairSemaphoreCO.Waiter> queue = buildAndInjectQueue(queueSize);
        FairSemaphoreCO.Waiter expectedWaiter = queue.getFirst();

        setPermits(1);
        int expectedPermits = getPermits();

        expectedWaiter.mReleased = false;

        doCallRealMethod().when(mFairSemaphoreMock).release();
        mFairSemaphoreMock.release();

        System.out.println("permits = " + getPermits());
        assertEquals("Available permits should not have changed.", expectedPermits, getPermits());

        assertEquals("A Waiter entry should have been removed from the queue.",
                expectedQueueSize, queue.size());

        assertTrue("The waiter was not removed using FIFO ordering.",
                queue.getFirst() != expectedWaiter);

        assertTrue("Released Waiter's released flag should be set.", expectedWaiter.mReleased);
        assertTrue("Waiter released flag should be set.", expectedWaiter.mReleased);
    }

    private LinkedList<FairSemaphoreCO.Waiter> buildAndInjectQueue(int size) throws Exception {
        LinkedList<FairSemaphoreCO.Waiter> queue = new LinkedList<>();
        ReflectionHelper.injectValueIntoFirstMatchingField(mFairSemaphoreMock, queue, LinkedList.class);
        for (int i = 0; i < size; i++) {
            FairSemaphoreCO.Waiter waiterMock = mock(FairSemaphoreCO.Waiter.class);

            ReflectionHelper.injectValueIntoFirstMatchingField(
                    waiterMock, mock(ReentrantLock.class), Lock.class, ReentrantLock.class);
            ReflectionHelper.injectValueIntoFirstMatchingField(
                    waiterMock, mock(Condition.class), Condition.class);

            queue.add(waiterMock);
        }

        return queue;
    }

    private void whenTryToGetPermitUnlockedCalledEnsureClassLockedAndReturn(boolean b) {
        when(mFairSemaphoreMock.tryToGetPermitUnlocked())
                .thenAnswer(invocation -> {
                    assertTrue("Object monitor should be locked.",
                            mLockCount == 1);
                    return b;
                });
    }

    private void inorderVerifyConditionAwait(InOrder inOrder) {
        try {
            inOrder.verify(mWaiterConditionMock, times(1)).await();
        } catch (Throwable t) {
            inOrder.verify(mWaiterConditionMock, times(1)).awaitUninterruptibly();
        }
    }

    private void verifyConditionAwaitCalledOnce() {
        try {
            verify(mWaiterConditionMock, times(1)).await();
        } catch (Throwable t) {
            verify(mWaiterConditionMock, times(1)).awaitUninterruptibly();
        }
    }

    private void verifyQueueAddCalledOnce() {
        try {
            verify(mQueueMock, times(1)).add(mWaiterMock);
        } catch (Throwable t) {
            verify(mQueueMock, times(1)).addLast(mWaiterMock);
        }
    }

    private void whenConditionSignalCalledVerify(Runnable verify) {
        doAnswer(invocation -> {
            verify.run();
            return mWaiterMock;
        }).when(mWaiterConditionMock).signal();
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

    private void setupLockMock() {
        doAnswer(invocation -> {
            mLockCount++;
            return null;
        }).when(mLockMock).lock();

        doAnswer(invocation -> {
            mLockCount--;
            return null;
        }).when(mLockMock).unlock();

        setupWaiterLockMock();
    }

    private void setupWaiterLockMock() {
        doAnswer(invocation -> {
            mWaiterLockCount++;
            return null;
        }).when(mWaiterLockMock).lock();

        doAnswer(invocation -> {
            mWaiterLockCount--;
            return null;
        }).when(mWaiterLockMock).unlock();
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

    private void assertClassLocked() {
        assertTrue("Class lock should be locked.",
                mLockCount == 1);
    }

    private void assertClassNotLocked() {
        assertTrue("Class lock should not be locked.",
                mLockCount == 0);
    }

    private void assertWaiterLocked() {
        assertTrue("Waiter lock should be locked.",
                mWaiterLockCount == 1);
    }

    private void assertWaiterNotLocked() {
        assertTrue("Waiter lock should not be locked.",
                mWaiterLockCount == 0);
    }

    private class LoopCounter {
        int count = 0;
        int interrupts = 0;
    }
}
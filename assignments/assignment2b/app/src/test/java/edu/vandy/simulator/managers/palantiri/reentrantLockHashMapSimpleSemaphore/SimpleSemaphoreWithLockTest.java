package edu.vandy.simulator.managers.palantiri.reentrantLockHashMapSimpleSemaphore;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class SimpleSemaphoreWithLockTest {
    private static final int PALANTIRI_COUNT = 5;

    @Mock
    public Lock mLockMock;
    @Mock
    public Condition mNotZeroMock;
    @InjectMocks
    public SimpleSemaphore mSimpleSemaphore;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test(timeout = 2000)
    public void testAcquireOne() throws InterruptedException {
        if (Helpers.isReentrantLockSolution(mSimpleSemaphore)) {
            System.out.println("ReentrantLock solution ignored by Lock solution tests.");
            return;
        }

        mSimpleSemaphore.mPermits = PALANTIRI_COUNT;

        doNothing().when(mLockMock).lock();
        doNothing().when(mLockMock).unlock();

        InOrder inOrder = inOrder(mLockMock);

        mSimpleSemaphore.acquire();

        verify(mLockMock).lock();
        verify(mLockMock).unlock();
        inOrder.verify(mLockMock).lock();
        inOrder.verify(mLockMock).unlock();

        int expectedAvailablePermits = PALANTIRI_COUNT - 1;
        assertEquals(
                "Available permits should be " + expectedAvailablePermits,
                expectedAvailablePermits,
                mSimpleSemaphore.availablePermits());
    }

    @Test(timeout = 2000)
    public void testAcquireAll() throws InterruptedException {
        if (Helpers.isReentrantLockSolution(mSimpleSemaphore)) {
            System.out.println("ReentrantLock solution ignored by Lock solution tests.");
            return;
        }

        mSimpleSemaphore.mPermits = PALANTIRI_COUNT;

        doNothing().when(mLockMock).lock();
        doNothing().when(mLockMock).unlock();

        InOrder inOrder = inOrder(mLockMock);

        for (int i = 0; i < PALANTIRI_COUNT; i++) {
            mSimpleSemaphore.acquire();
        }

        verify(mLockMock, times(PALANTIRI_COUNT)).lock();
        verify(mLockMock, times(PALANTIRI_COUNT)).unlock();

        for (int i = 0; i < PALANTIRI_COUNT; i++) {
            inOrder.verify(mLockMock).lock();
            inOrder.verify(mLockMock).unlock();
        }

        int expectedAvailablePermits = 0;
        assertEquals(
                "Available permits should be " + expectedAvailablePermits,
                expectedAvailablePermits,
                mSimpleSemaphore.availablePermits());
    }

    @Test(timeout = 2000)
    public void testAcquireWithAwaitCall() throws InterruptedException {
        if (Helpers.isReentrantLockSolution(mSimpleSemaphore)) {
            System.out.println("ReentrantLock solution ignored by Lock solution tests.");
            return;
        }

        mSimpleSemaphore.mPermits = 0;

        doNothing().when(mLockMock).lock();
        doNothing().when(mLockMock).unlock();
        doAnswer((Answer<Void>) invocation -> {
            mSimpleSemaphore.mPermits = 1;
            return null;
        }).when(mNotZeroMock).await();

        InOrder inOrder = inOrder(mLockMock, mNotZeroMock);

        mSimpleSemaphore.acquire();

        verify(mLockMock).lock();
        verify(mNotZeroMock).await();
        verify(mLockMock).unlock();

        inOrder.verify(mLockMock).lock();
        inOrder.verify(mNotZeroMock).await();
        inOrder.verify(mLockMock).unlock();

        int expectedAvailablePermits = 0;
        assertEquals(
                "Available permits should be " + expectedAvailablePermits,
                expectedAvailablePermits,
                mSimpleSemaphore.availablePermits());
    }

    @Test(timeout = 2000)
    public void testAcquireWithAwaitCallInterrupted() throws InterruptedException {
        if (Helpers.isReentrantLockSolution(mSimpleSemaphore)) {
            System.out.println("ReentrantLock solution ignored by Lock solution tests.");
            return;
        }

        mSimpleSemaphore.mPermits = 0;

        doNothing().when(mLockMock).lock();
        doNothing().when(mLockMock).unlock();
        doThrow(new InterruptedException("Mock interrupt")).when(mNotZeroMock).await();

        InOrder inOrder = inOrder(mLockMock, mNotZeroMock);

        try {
            mSimpleSemaphore.acquire();
            fail("Interrupt should not be swallowed by call to acquire().");
        } catch (Exception e) {
        }

        verify(mLockMock).lock();
        verify(mNotZeroMock).await();
        verify(mLockMock).unlock();

        inOrder.verify(mLockMock).lock();
        inOrder.verify(mNotZeroMock).await();
        inOrder.verify(mLockMock).unlock();

        int expectedAvailablePermits = 0;
        assertEquals(
                "Available permits should be " + expectedAvailablePermits,
                expectedAvailablePermits,
                mSimpleSemaphore.availablePermits());
    }

    @Test(timeout = 2000)
    public void testAcquireUninterruptiblyWithAvailablePermits() throws InterruptedException {
        if (Helpers.isReentrantLockSolution(mSimpleSemaphore)) {
            System.out.println("ReentrantLock solution ignored by Lock solution tests.");
            return;
        }

        mSimpleSemaphore.mPermits = PALANTIRI_COUNT;

        doNothing().when(mLockMock).lock();
        doNothing().when(mLockMock).unlock();

        InOrder inOrder = inOrder(mLockMock);

        mSimpleSemaphore.acquireUninterruptibly();

        verify(mLockMock).lock();
        verify(mLockMock).unlock();
        verify(mNotZeroMock, never()).await();

        inOrder.verify(mLockMock).lock();
        inOrder.verify(mLockMock).unlock();

        int expectedAvailablePermits = PALANTIRI_COUNT - 1;
        assertEquals(
                "Available permits should be " + expectedAvailablePermits,
                expectedAvailablePermits,
                mSimpleSemaphore.availablePermits());
    }


    @Test(timeout = 2000)
    public void acquireUninterruptiblyWithInterrupt() throws InterruptedException {
        if (Helpers.isReentrantLockSolution(mSimpleSemaphore)) {
            System.out.println("ReentrantLock solution ignored by Lock solution tests.");
            return;
        }

        mSimpleSemaphore.mPermits = 0;

        doNothing().when(mLockMock).lock();
        doNothing().when(mLockMock).unlock();
        doAnswer((Answer<Void>) invocation -> {
            if (mSimpleSemaphore.mPermits == 0) {
                mSimpleSemaphore.mPermits = 1;
                throw new InterruptedException("Mock exception.");
            }
            return null;
        }).when(mNotZeroMock).await();

        InOrder inOrder = inOrder(mLockMock, mNotZeroMock);

        try {
            mSimpleSemaphore.acquireUninterruptibly();
        } catch (Exception e) {
        }
        assertTrue(
                "Thread interrupt was not re-issued/propagated.",
                Thread.currentThread().isInterrupted());

        verify(mLockMock).lock();
        verify(mNotZeroMock).await();
        verify(mLockMock).unlock();

        inOrder.verify(mLockMock).lock();
        inOrder.verify(mNotZeroMock).await();
        inOrder.verify(mLockMock).unlock();

        int expectedAvailablePermits = 0;
        assertEquals(
                "Available permits should be " + expectedAvailablePermits,
                expectedAvailablePermits,
                mSimpleSemaphore.availablePermits());
    }

    @Test(timeout = 2000)
    public void testReleaseWithSignal() {
        if (Helpers.isReentrantLockSolution(mSimpleSemaphore)) {
            System.out.println("ReentrantLock solution ignored by Lock solution tests.");
            return;
        }

        mSimpleSemaphore.mPermits = 0;

        doNothing().when(mLockMock).lock();
        doNothing().when(mLockMock).unlock();
        doNothing().when(mNotZeroMock).signal();

        InOrder inOrder = inOrder(mLockMock, mNotZeroMock);

        mSimpleSemaphore.release();

        verify(mLockMock).lock();
        verify(mLockMock).unlock();
        verify(mNotZeroMock, times(1)).signal();

        inOrder.verify(mLockMock).lock();
        inOrder.verify(mNotZeroMock).signal();
        inOrder.verify(mLockMock).unlock();

        int expectedAvailablePermits = 1;
        assertEquals(
                "Available permits should be " + expectedAvailablePermits,
                expectedAvailablePermits,
                mSimpleSemaphore.availablePermits());
    }

    @Test(timeout = 2000)
    public void testReleaseWithNoSignal() {
        if (Helpers.isReentrantLockSolution(mSimpleSemaphore)) {
            System.out.println("ReentrantLock solution ignored by Lock solution tests.");
            return;
        }

        mSimpleSemaphore.mPermits = -1;

        doNothing().when(mLockMock).lock();
        doNothing().when(mLockMock).unlock();

        InOrder inOrder = inOrder(mLockMock, mNotZeroMock);

        mSimpleSemaphore.release();

        verify(mLockMock).lock();
        verify(mLockMock).unlock();
        verify(mNotZeroMock, never()).signal();

        inOrder.verify(mLockMock).lock();
        inOrder.verify(mLockMock).unlock();

        int expectedAvailablePermits = 0;
        assertEquals(
                "Available permits should be " + expectedAvailablePermits,
                expectedAvailablePermits,
                mSimpleSemaphore.availablePermits());
    }
}
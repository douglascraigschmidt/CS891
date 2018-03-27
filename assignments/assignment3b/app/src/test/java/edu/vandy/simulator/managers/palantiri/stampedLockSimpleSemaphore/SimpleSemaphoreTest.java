package edu.vandy.simulator.managers.palantiri.stampedLockSimpleSemaphore;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static java.lang.Thread.sleep;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class SimpleSemaphoreTest {
    private final SimpleSemaphore simpleSemaphore = new SimpleSemaphore(5);
    @Mock
    public SimpleSemaphore mSimpleSemaphore;
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test(timeout = 2000)
    public void testAcquireWithNoWait() throws InterruptedException {
        int availablePermits = 1;
        mSimpleSemaphore.mPermits = availablePermits;

        doCallRealMethod().when(mSimpleSemaphore).acquire();

        mSimpleSemaphore.acquire();

        verify(mSimpleSemaphore, never()).wait();

        int expectedPermits = availablePermits - 1;

        assertEquals(
                "Available permits should be " + expectedPermits,
                expectedPermits,
                mSimpleSemaphore.mPermits);
    }

    @Test(timeout = 2000)
    public void testAcquireWithOneWait() throws InterruptedException {
        int availablePermits = 0;
        mSimpleSemaphore.mPermits = availablePermits;

        doAnswer(invocation -> mSimpleSemaphore.mPermits++).when(mSimpleSemaphore).wait();

        doCallRealMethod().when(mSimpleSemaphore).acquire();

        mSimpleSemaphore.acquire();

        verify(mSimpleSemaphore, times(1)).wait();

        int expectedPermits = 0;

        assertEquals(
                "Available permits should be " + expectedPermits,
                expectedPermits,
                mSimpleSemaphore.mPermits);
    }

    @Test(timeout = 2000)
    public void testAcquireWithTwoWaits() throws InterruptedException {
        int availablePermits = -2;
        mSimpleSemaphore.mPermits = availablePermits;

        doAnswer(invocation -> mSimpleSemaphore.mPermits++).when(mSimpleSemaphore).wait();

        doCallRealMethod().when(mSimpleSemaphore).acquire();

        mSimpleSemaphore.acquire();

        verify(mSimpleSemaphore, times(3)).wait();

        int expectedPermits = 0;

        assertEquals(
                "Available permits should be " + expectedPermits,
                expectedPermits,
                mSimpleSemaphore.mPermits);
    }

    @Test(timeout = 2000)
    public void testAcquireWithInterrupt() throws InterruptedException {
        int availablePermits = -10;
        mSimpleSemaphore.mPermits = availablePermits;

        doThrow(new InterruptedException("Test exception")).when(mSimpleSemaphore).wait();

        doCallRealMethod().when(mSimpleSemaphore).acquire();

        try {
            mSimpleSemaphore.acquire();
            fail("SimpleSemaphore.acquire() should propagate InterruptedException");
        } catch (InterruptedException e) {
        }

        verify(mSimpleSemaphore, times(1)).wait();

        int expectedPermits = availablePermits;

        assertEquals(
                "Available permits should be " + expectedPermits,
                expectedPermits,
                mSimpleSemaphore.mPermits);
    }

    @Test(timeout = 2000)
    public void testAcquireUninterruptiblyWithNoWait() throws InterruptedException {
        int availablePermits = 1;
        mSimpleSemaphore.mPermits = availablePermits;

        doCallRealMethod().when(mSimpleSemaphore).acquire();

        mSimpleSemaphore.acquire();

        verify(mSimpleSemaphore, never()).wait();

        int expectedPermits = availablePermits - 1;

        assertEquals(
                "Available permits should be " + expectedPermits,
                expectedPermits,
                mSimpleSemaphore.mPermits);
    }

    @Test(timeout = 2000)
    public void testAcquireUninterruptiblyWithOneWait() throws InterruptedException {
        int availablePermits = 0;
        mSimpleSemaphore.mPermits = availablePermits;

        doAnswer(invocation -> mSimpleSemaphore.mPermits++).when(mSimpleSemaphore).wait();

        doCallRealMethod().when(mSimpleSemaphore).acquireUninterruptibly();

        mSimpleSemaphore.acquireUninterruptibly();

        verify(mSimpleSemaphore, times(1)).wait();

        int expectedPermits = 0;

        assertEquals(
                "Available permits should be " + expectedPermits,
                expectedPermits,
                mSimpleSemaphore.mPermits);
    }

    @Test(timeout = 2000)
    public void testAcquireUninterruptiblyWithTwoWaits() throws InterruptedException {
        int availablePermits = -2;
        mSimpleSemaphore.mPermits = availablePermits;

        doAnswer(invocation -> mSimpleSemaphore.mPermits++).when(mSimpleSemaphore).wait();

        doCallRealMethod().when(mSimpleSemaphore).acquireUninterruptibly();

        mSimpleSemaphore.acquireUninterruptibly();

        verify(mSimpleSemaphore, times(3)).wait();

        int expectedPermits = 0;

        assertEquals(
                "Available permits should be " + expectedPermits,
                expectedPermits,
                mSimpleSemaphore.mPermits);
    }

    @Test(timeout = 2000)
    public void testAcquireUninterruptiblyWithInterrupt() throws InterruptedException {
        int availablePermits = -1;
        mSimpleSemaphore.mPermits = availablePermits;

        doAnswer(invocation -> {
            mSimpleSemaphore.mPermits++;
            throw new InterruptedException("Test exception 1");
        }).doAnswer(invocation -> {
            mSimpleSemaphore.mPermits++;
            throw new InterruptedException("Test exception 2");
        }).when(mSimpleSemaphore).wait();

        doCallRealMethod().when(mSimpleSemaphore).acquireUninterruptibly();

        try {
            mSimpleSemaphore.acquireUninterruptibly();
        } catch (Exception e) {
        }

        assertTrue("Thread interrupted should have been propagated.",
                Thread.currentThread().isInterrupted());

        verify(mSimpleSemaphore, times(2)).wait();

        int expectedPermits = 0;

        assertEquals(
                "Available permits should be " + expectedPermits,
                expectedPermits,
                mSimpleSemaphore.mPermits);
    }

    @Test(timeout = 2000)
    public void testReleaseWithNotify() {
        int availablePermits = 0;
        final SimpleSemaphore simpleSemaphore = new SimpleSemaphore(availablePermits);

        Thread waiterThread = new Thread(() -> {
            synchronized (simpleSemaphore) {
                try {
                    simpleSemaphore.wait();
                } catch (InterruptedException e) {
                }
            }
        });

        // Sleep to ensure that waiterThread executes simpleSemaphore.wait()
        // before calling SUT simpleSemaphore.release() method.
        try {
            waiterThread.start();
            sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        simpleSemaphore.release();

        try {
            // Wait for waiterThread to be released from wait call.
            waiterThread.join(1000);
            assertFalse("SimpleSemaphore notify method was not called.", waiterThread.isAlive());
        } catch (InterruptedException e) {
            fail("SimpleSemaphore notify method was not called.");
        }

        int expectedPermits = availablePermits + 1;
        assertEquals(expectedPermits, simpleSemaphore.mPermits);
    }

    @Test(timeout = 2000)
    public void testReleaseWithNoNotify() {
        int availablePermits = -1;
        final SimpleSemaphore simpleSemaphore = new SimpleSemaphore(availablePermits);

        Thread waiterThread = new Thread(() -> {
            synchronized (simpleSemaphore) {
                try {
                    simpleSemaphore.wait();
                    fail("SimpleSemaphore notify should not be called.");
                } catch (InterruptedException e) {
                }
            }
        });

        // Sleep to ensure that waiterThread executes simpleSemaphore.wait()
        // before calling SUT simpleSemaphore.release() method.
        try {
            waiterThread.start();
            sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        simpleSemaphore.release();

        try {
            // Wait for waiterThread to be released from wait call.
            waiterThread.join(1000);
            assertTrue("SimpleSemaphore notify should not be called.", waiterThread.isAlive());
        } catch (InterruptedException e) {
        }

        int expectedPermits = availablePermits + 1;
        assertEquals(expectedPermits, simpleSemaphore.mPermits);
    }
}

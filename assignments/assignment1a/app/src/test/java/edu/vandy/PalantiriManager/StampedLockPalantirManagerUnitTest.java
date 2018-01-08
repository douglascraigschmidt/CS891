package edu.vandy.PalantiriManager;

import org.junit.Test;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import edu.vandy.simulator.Simulator;
import edu.vandy.simulator.managers.palantiri.Palantir;
import edu.vandy.simulator.managers.palantiri.PalantirManager;
import edu.vandy.simulator.model.implementation.components.BeingComponent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit test for the StampedLockPalantiriManager.
 */
public class StampedLockPalantirManagerUnitTest {
    /**
     * Keep track of whether an exception occurs.
     */
    protected volatile boolean exc = false;
    /**
     * Keep track of if a runtime exception occurs
     */
    volatile boolean mFailed = false;
    /**
     * Keep track of whether a thread is interrupted.
     */
    volatile boolean mInterrupted = false;

    @Test
    public void testDebugOutput() {
        System.out.println("DEBUG TEST 2: Here is System.out.println() output");
    }

    @Test
    public void testPalantiriManager() {
        PalantirManager palantiriManager =
                buildPalantiriManager(
                        PalantirManager.Factory.Type.STAMPED_LOCK,
                        2);
        assertNotNull(palantiriManager.getPalantirCount() == 2);
    }

    private PalantirManager buildPalantiriManager(
            PalantirManager.Factory.Type type,
            int palantirCount) {
        // Construct an instance of the specified PalantiriManger type.
        Simulator simulator = new Simulator(null);
        PalantirManager palantiriManager =
                PalantirManager.Factory.newManager(
                        type,
                        palantirCount,
                        simulator);
        assertNotNull(palantiriManager);

        return palantiriManager;
    }

    @Test
    public void testAcquire() throws InterruptedException {
        Thread t =
                new Thread(() -> {
                    try {
                        PalantirManager palantiriManager =
                                buildPalantiriManager(
                                        PalantirManager.Factory.Type.STAMPED_LOCK,
                                        2);
                        assertEquals(palantiriManager.availablePermits(), 2);
                        palantiriManager.acquirePalantir(1);
                        assertEquals(palantiriManager.availablePermits(), 1);
                        palantiriManager.acquirePalantir(2);
                        assertEquals(palantiriManager.availablePermits(), 0);
                    } catch (AssertionError e) {
                        exc = true;
                        System.out.println(e);
                    }
                });
        t.start();
        t.join();
        assertEquals(exc, false);
        exc = false;
    }

    @Test
    public void testRelease() throws InterruptedException {
        Thread t =
                new Thread(() -> {
                    try {
                        PalantirManager palantiriManager =
                                buildPalantiriManager(
                                        PalantirManager.Factory.Type.STAMPED_LOCK,
                                        2);
                        assertEquals(palantiriManager.availablePermits(), 2);
                        Palantir palantir1 = palantiriManager.acquirePalantir(1);
                        assertEquals(palantiriManager.availablePermits(), 1);
                        Palantir palantir2 = palantiriManager.acquirePalantir(2);
                        assertEquals(palantiriManager.availablePermits(), 0);
                        palantiriManager.releasePalantir(palantir1);
                        assertEquals(palantiriManager.availablePermits(), 1);
                        palantiriManager.releasePalantir(palantir2);
                        assertEquals(palantiriManager.availablePermits(), 2);
                        palantiriManager.releasePalantir(null);
                        assertEquals(palantiriManager.availablePermits(), 2);
                    } catch (AssertionError e) {
                        exc = true;
                    }
                });
        t.start();
        t.join();
        assertEquals(exc, false);
        exc = true;
    }

    @Test
    public void testavailablePalantiri() throws InterruptedException {
        Thread t =
                new Thread(() -> {
                    try {
                        PalantirManager palantiriManager =
                                buildPalantiriManager(
                                        PalantirManager.Factory.Type.STAMPED_LOCK,
                                        2);
                        assertEquals(palantiriManager.availablePermits(), 2);
                        palantiriManager.acquirePalantir(1);
                        assertEquals(palantiriManager.availablePermits(), 1);
                    } catch (AssertionError e) {
                        exc = true;
                    }
                });
        t.start();
        t.join();
        assertEquals(exc, false);
        exc = true;
    }

    @Test
    public void testConcurrentAccess() {
        // The number of threads that will be trying to run at once.
        final int THREAD_COUNT = 6;

        // The number of threads that we want to let run at once.
        final int PERMIT_COUNT = 2;

        // The number of times each thread will try to access the
        // semaphore.
        final int ACCESS_COUNT = 10;

        PalantirManager palantiriManager =
                buildPalantiriManager(
                        PalantirManager.Factory.Type.STAMPED_LOCK,
                        PERMIT_COUNT);

        assertTrue(THREAD_COUNT > PERMIT_COUNT);

        // The number of threads that currently have a permit.
        final AtomicLong runningThreads = new AtomicLong(0);

        // Keep track of the threads we have so we can wait for them
        // to finish later.
        Thread threads[] =
                new Thread[THREAD_COUNT];

        for (int i = 0; i < THREAD_COUNT; ++i) {
            final Thread t =
                    new Thread(() -> {
                        final Random random = new Random();
                        for (int j = 0;
                             j < ACCESS_COUNT;
                             ++j) {
                            Palantir palantir;
                            try {
                                // Acquire a permit from the Manager.
                                palantir = palantiriManager.acquirePalantir(j);
                            } catch (Exception e) {
                                mInterrupted = true;
                                return;
                            }

                            // Increment the number of threads that have a permit.
                            long running = runningThreads.incrementAndGet();

                            // If there are more threads running than
                            // are supposed to be, throw an error.
                            if (running > PERMIT_COUNT)
                                throw new RuntimeException();

                            // Wait for an indeterminate amount of time.
                            BeingComponent.pauseThread(10, 150, () -> false);

                            // Decrement the number of threads that have a permit.
                            runningThreads.decrementAndGet();

                            // Release the permit
                            palantiriManager.releasePalantir(palantir);
                        }
                    });

            // If any of the threads throw an exception, then we
            // failed.
            t.setUncaughtExceptionHandler((t1, e) -> {
                System.out.println("uncaughtException in testConcurrentAccess()" + e);
                mFailed = true;
            });

            // Keep track of the thread to start/join it later.
            threads[i] = t;
        }

        for (final Thread t : threads)
            t.start();

        for (final Thread t : threads)
            try {
                t.join();
            } catch (InterruptedException e) {
                fail("The main thread was interrupted for some reason.");
            }

        assertFalse(mFailed);
        assertFalse("One of the threads was interrupted while calling acquire(). "
                        + "This shouldn't happen (even if your Semaphore is wrong).",
                mInterrupted);
    }
}

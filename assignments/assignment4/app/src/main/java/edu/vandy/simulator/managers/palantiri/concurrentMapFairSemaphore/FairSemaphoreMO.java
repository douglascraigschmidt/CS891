package edu.vandy.simulator.managers.palantiri.concurrentMapFairSemaphore;

import java.util.LinkedList;

/**
 * Implements a fair semaphore using the Specific Notification pattern
 * (www.dre.vanderbilt.edu/~schmidt/PDF/specific-notification.pdf)
 * using the Java built-in monitor object.  Undergraduate students
 * should implement this class.
*/
public class FairSemaphoreMO
       implements FairSemaphore {
    /**
     * Debugging tag used by the Android logger.
     */
    protected final static String TAG = 
        FairSemaphoreMO.class.getSimpleName();

    /**
     * Define a count of the number of available permits.
     */
    // TODO - you fill in here.  Make sure that this field will ensure
    // its values aren't cached by multiple threads..

    /**
     * Define a class that can be used in the "WaitQueue" to wait for
     * a specific thread to be notified.
     */
    private static class Waiter {
        /**
         * Keeps track of whether the Waiter was released or not to
         * detected and handle "spurious wakeups".
         */
        boolean mReleased = false;
    }

    /**
     * Define a "WaitQueue" that keeps track of the waiters in a FIFO
     * List to ensure "fair" semantics.
     */
    // TODO - you fill in here.

    /**
     * Initialize the fields in the class.
     */
    public FairSemaphoreMO(int availablePermits) {
        // TODO - you fill in here.
    }

    /**
     * Acquire one permit from the semaphore in a manner that cannot
     * be interrupted.
     */
    @Override
    public void acquireUninterruptibly() {
        // TODO -- you fill in here, using a loop to ignore
        // InterruptedExceptions.
    }

    /**
     * Acquire one permit from the semaphore in a manner that can
     * be interrupted.
     */
    @Override
    public void acquire() throws InterruptedException {
        // Bail out quickly if we've been interrupted.
        if (Thread.interrupted())
            throw new InterruptedException();

        // Try to get a permit without blocking.
        else if (!tryToGetPermit())
            // Block until a permit is available.
            waitForPermit();
    }

    /**
     * Handle the case where we can get a permit without blocking.
     *
     * @return Returns true if the permit was obtained, else false.
     */
    private boolean tryToGetPermit() {
        // TODO -- first try the "fast path" where the method doesn't
        // need to block if the queue is empty and permits are
        // available.
    }				

    /**
     * Factors out code that checks to see if a permit can be obtained
     * without blocking.  This method assumes the monitor lock
     * ("intrinsic lock") is held.
     *
     * @return Returns true if the permit was obtained, else false.
     */
    private boolean tryToGetPermitUnlocked() {
        // We don't need to wait if there the queue is empty and
        // permits are available.
        if (mWaitQueue.isEmpty() && mAvailablePermits > 0) {
            // No need to wait, so decrement and return.
            //noinspection NonAtomicOperationOnVolatileField
            --mAvailablePermits;
            return true;
        } else
            return false;
    }

    /**
     * Handle the case where we need to block since there are already
     * waiters in the queue or no permits are available.
     */
    private void waitForPermit() throws InterruptedException {
        // TODO -- implement "fair" semaphore acquire semantics using
        // the Specific Notification pattern.
    }

    /**
     * Return one permit to the semaphore.
     */
    @Override
    public void release() {
        // TODO -- implement "fair" semaphore release semantics
        // using the Specific Notification pattern.
    }

    /**
     * @return The number of available permits.
     */
    @Override
    public int availablePermits() {
        // @@ TODO -- you fill in here replacing 0 with the right
        // value.
        return 0;
    }
}

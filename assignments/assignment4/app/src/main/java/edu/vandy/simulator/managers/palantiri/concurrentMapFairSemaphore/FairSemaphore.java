package edu.vandy.simulator.managers.palantiri.concurrentMapFairSemaphore;

/**
 * Defines an interface for a fair semaphore.
 */
public interface FairSemaphore {
    /**
     * Acquire one permit from the semaphore in a manner that cannot
     * be interrupted.
     */
    void acquireUninterruptibly();

    /**
     * Acquire one permit from the semaphore in a manner that can be
     * interrupted.
     */
    void acquire() throws InterruptedException;

    /**
     * Return one permit to the semaphore.
     */
    void release();

    /**
     * @return The number of available permits.
     */
    int availablePermits();
}


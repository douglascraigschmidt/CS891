package edu.vandy.simulator.managers.palantiri.concurrentMapFairSemaphore;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Defines an interface for a fair semaphore.
*/
interface FairSemaphore {
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


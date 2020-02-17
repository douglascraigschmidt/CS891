package edu.vandy.simulator.managers.palantiri.reentrantLockHashMapSimpleSemaphore;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class defines a counting semaphore with "fair" semantics that
 * are implemented using a Java ReentrantLock and ConditionObject.
 */
public class SimpleSemaphore {
    /**
     * Define a count of the number of available permits.
     */
    // TODO - you fill in here.  Ensure that this field will ensure
    // its values aren't cached by multiple threads..

    /**
     * Define a Lock to protect critical sections.
     */
    // TODO - you fill in here

    /**
     * Define a Condition that's used to wait while the number of
     * permits is 0.
     */
    // TODO - you fill in here

    /**
     * Default constructor used for regression tests.
     */
    public SimpleSemaphore() {
    }

    /**
     * Constructor initialize the fields.
     */
    public SimpleSemaphore(int permits) {
        // TODO -- you fill in here making sure the ReentrantLock has
        // "fair" semantics.
    }

    /**
     * Acquire one permit from the semaphore in a manner that can be
     * interrupted.
     */
    public void acquire()
            throws InterruptedException {
        // TODO -- you fill in here, make sure the lock is always
        // released, e.g., even if an exception occurs.
    }

    /**
     * Acquire one permit from the semaphore in a manner that cannot
     * be interrupted.  If an interrupt occurs while this method is
     * running make sure the interrupt flag is reset when the method
     * returns.
     */
    public void acquireUninterruptibly() {
        // TODO -- you fill in here, make sure the lock is always
        // released, e.g., even if an exception occurs.
    }

    /**
     * Return one permit to the semaphore.
     */
    public void release() {
        // TODO -- you fill in here, make sure the lock is always
        // released, e.g., even if an exception occurs.
    }

    /**
     * Returns the current number of permits.
     */
    protected int availablePermits() {
        // TODO -- you fill in here, replacing 0 with the
        // appropriate field.
        return 0;
    }
}

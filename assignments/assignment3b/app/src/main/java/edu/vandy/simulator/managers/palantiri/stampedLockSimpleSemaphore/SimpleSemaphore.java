package edu.vandy.simulator.managers.palantiri.stampedLockSimpleSemaphore;

/**
 * This class defines a counting semaphore with "fair" semantics that
 * are implemented using Java built-in monitor object features.
 */
class SimpleSemaphore {
    /**
     * Define a count of the number of available permits.
     */
    // TODO - you fill in here.  Ensure that this data member will
    // ensure its values aren't cached by multiple Threads..

    /**
     * Constructor initialize the data members.
     */
    public SimpleSemaphore(int permits) {
        // TODO -- you fill in here. 
    }

    /**
     * Acquire one permit from the semaphore in a manner that can be
     * interrupted.
     */
    public void acquire() throws InterruptedException {
        // TODO -- you fill in here.
    }

    /**
     * Acquire one permit from the semaphore in a manner that cannot
     * be interrupted.  If an interrupt occurs while this method is
     * running make sure to set the interrupt state when the thread
     * returns from this method.
     */
    public void acquireUninterruptibly() {
        // TODO -- you fill in here.
    }

    /**
     * Return one permit to the semaphore.
     */
    public void release() {
        // TODO -- you fill in here.
    }

    /**
     * Returns the current number of permits.
     */
    protected int availablePermits() {
        // TODO -- you fill in here.  
    }
}

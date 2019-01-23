package edu.vandy.simulator.managers.palantiri.spinLockHashMap;

import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * This class emulates a "compare and swap"-style spin lock with
 * non-recursive semantics.
 */
class SpinLock 
      implements CancellableLock {
    /**
     * Define an AtomicBoolean that's used as the basis for an atomic
     * compare-and-swap.  The default state of the spinlock should be
     * "unlocked".
     */
    // TODO -- you fill in here.

    /**
     * @return The AtomicBoolean used for compare-and-swap.
     */
    public AtomicBoolean getOwner() {
        // TODO -- you fill in here, replacing null with the proper
        // code.
        return null;
    }

    /**
     * Acquire the lock only if it is free at the time of invocation.
     * Acquire the lock if it is available and returns immediately
     * with the value true.  If the lock is not available then this
     * method will return immediately with the value false.
     */
    @Override
    public boolean tryLock() {
        // Try to set mOwner's value to true, which succeeds iff its
        // current value is false.
        // TODO -- you fill in here, replacing false with the proper
        // code.
        return false;
    }

    /**
     * Acquire the lock. If the lock is not available then the current
     * thread becomes disabled for thread scheduling purposes and lies
     * dormant until the lock has been acquired.
     *
     * @param isCancelled Supplier that is called to see if the attempt
     *                    to lock should be abandoned due to a pending
     *                    shutdown operation.
     * @throws CancellationException Thrown only if a pending shutdown
     * operation is has been detected by calling the isCancelled supplier.
     */
    @Override
    public void lock(Supplier<Boolean> isCancelled)
        throws CancellationException {
        // Loop trying to set mOwner's value to true, which succeeds
        // iff its current value is false.  Each iteration should also
        // check if a shutdown has been requested and if so throw a
        // cancellation exception.
        // TODO -- you fill in here.
    }

    /**
     * Release the lock.
     */
    @Override
    public void unlock() {
        // Atomically release the lock that's currently held by
        // mOwner.
        // TODO -- you fill in here.
    }
}

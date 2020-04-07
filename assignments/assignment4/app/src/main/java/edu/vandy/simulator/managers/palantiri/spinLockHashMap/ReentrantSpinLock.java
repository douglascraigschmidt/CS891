package edu.vandy.simulator.managers.palantiri.spinLockHashMap;

import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * This class emulates a "compare and swap"-style spin lock with
 * recursive semantics and should be implemented by students taking
 * the class for graduate credit.
 */
class ReentrantSpinLock
        implements CancellableLock {
    /**
     * Define an AtomicReference that's used as the basis for an
     * atomic compare-and-swap.  The default state of the spinlock
     * should be "unlocked".
     */
    // TODO -- you fill in here.

    /**
     * Count the number of times the owner thread has recursively
     * acquired the lock.
     */
    // TODO -- you fill in here.

    /**
     * @return The current recursion count.
     */
    public int getRecursionCount() {
        // TODO -- you fill in here, replacing -1 with the appropriate value.
        return -1;
    }

    /**
     * Acquire the lock only if it is free at the time of invocation.
     * Acquire the lock if it is available and returns immediately
     * with the value true. If the lock is not available then this
     * method will return immediately with the value false.
     */
    @Override
    public boolean tryLock() {
        // Try to set mOwner's value to the thread (true), which
        // succeeds iff its current value is null (false).
        // TODO -- you fill in here, replacing false with the proper code.
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
     *                               operation is has been detected by calling the isCancelled supplier.
     */
    @Override
    public void lock(Supplier<Boolean> isCancelled)
            throws CancellationException {
        // If the current thread owns the lock simply increment the
        // recursion count.  Otherwise, loop trying to set mOwner's
        // value to the current thread reference, which succeeds iff
        // its current value is null.  Each iteration should also
        // check if a shutdown has been requested and if so throw a
        // cancellation exception.  
        // TODO -- you fill in here.
    }

    /**
     * Release the lock.  Throws IllegalMonitorStateException if
     * the calling thread doesn't own the lock.
     */
    @Override
    public void unlock() {
        // If the current owner is trying to unlock then simply
        // decrement the recursion count if it's > 0.  Otherwise,
        // atomically release the lock that's currently held by
        // mOwner. If the lock owner is not the current thread,
        // then throw IllegalMonitorStateException.

        // TODO -- you fill in here.
    }
}

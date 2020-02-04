package edu.vandy.simulator.managers.palantiri.spinLockHashMap;

import java.util.concurrent.CancellationException;
import java.util.function.Supplier;

/**
 * An interface that defines a simple lock that can be canceled.
 */
interface CancellableLock {
    /**
     * Acquire the lock only if it is free at the time of invocation.
     * Acquire the lock if it is available and returns immediately
     * with the value true.  If the lock is not available then this
     * method will return immediately with the value false.
     */
    boolean tryLock();

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
    void lock(Supplier<Boolean> isCancelled) throws CancellationException;

    /**
     * Release the lock.
     */
    void unlock();
}

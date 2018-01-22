package edu.vandy.simulator.managers.palantiri.spinLockHashMap;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import edu.vandy.simulator.managers.palantiri.Palantir;
import edu.vandy.simulator.managers.palantiri.PalantirManager;

/**
 * A PalantirManager implemented using a SpinLock, a Semaphore, and a
 * HashMap.
 */
public class SpinLockHashMapMgr extends PalantirManager {
    /**
     * Debugging tag used by the Android logger.
     */
    protected final static String TAG =
        SpinLockHashMapMgr.class.getSimpleName();

    /**
     * A "spin lock" used to ensure that threads serialize on a
     * critical section.
     */
    // TODO -- you fill in here.

    /**
     * A counting Semaphore that limits concurrent access to the fixed
     * number of available palantiri managed by the PalantirManager.
     */
    // TODO -- you fill in here.

    /**
     * A map that associates the @a Palantiri key to the @a boolean
     * values that keep track of whether the key is available.
     */
    private HashMap<Palantir, Boolean> mPalantiriMap;

    /**
     * Called to allow subclass implementations the opportunity
     * to setup fields and initialize field values.
     */
    @Override
    protected void buildModel() {
        // Create a new HashMap.
        mPalantiriMap = new HashMap<>();

        // Iterate through the List of Palantiri returned via the
        // getPalantiri() factory method and initialize each key in
        // the mPalantiriMap with "true" to indicate it's available.
        // TODO -- you fill in here.

        // Initialize the Semaphore to use a "fair" implementation
        // that mediates concurrent access to the given Palantiri.
        // TODO -- you fill in here.

        // Initialize the SpinLock.
        // TODO -- you fill in here.
    }

    /**
     * Get a Palantir from the PalantiriManager, blocking until one is
     * available.
     *
     * @return The first available Palantir.
     */
    @Override
    public Palantir acquire() {
        // Acquire the Semaphore uninterruptibly and then acquired the
        // spin-lock to ensure that finding the first key in the
        // HashMap whose value is "true" (which indicates it's
        // available for use) occurs in a thread-safe manner.  Replace
        // the value of this key with "false" to indicate the Palantir
        // isn't available, return that palantir to the client, and
        // release the spin-lock.
        // TODO -- you fill in here.


        // This invariant should always hold for all acquire()
        // implementations if implemented correctly. That is the
        // purpose of enforcing the @NotNull along with the
        // CancellationException; It makes it clear that all
        // implementations should either be successful (if implemented
        // correctly) and return a Palantir, or fail because of
        // cancellation. If possible, this implementation needs to be
        // changed so that this statement isn't reached (it is reached
        // currently on every run)
        throw new IllegalStateException("This method should either return a valid " +
                                        "Palantir or throw a CancellationException. " +
                                        "In either case, this statement should not be reached.");
    }

    /**
     * Returns the designated @code palantir to the PalantiriManager
     * so it's available for other beings to use.
     *
     * @param palantir The palantir to release back to the Palantiri pool
     */
    @Override
    protected void release(Palantir palantir) {
        // Put the "true" value back into HashMap for the palantir key
        // in a thread-safe manner and release the Semaphore if all
        // works properly.
        // TODO -- you fill in here.
    }

    /**
     * This method is just intended for use by the regression tests,
     * not by applications.
     *
     * @return the number of available permits on the semaphore.
     */
    @Override
    public int availablePermits() {
        return mAvailablePalantiri.availablePermits();
    }

    /**
     * Called when the simulation is being shutdown to allow model
     * components the opportunity to and release resources and to
     * reset field values.
     */
    @Override
    public void shutdownNow() {
    }
}

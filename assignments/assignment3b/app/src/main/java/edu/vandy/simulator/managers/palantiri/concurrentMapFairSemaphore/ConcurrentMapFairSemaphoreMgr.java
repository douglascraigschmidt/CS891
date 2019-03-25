package edu.vandy.simulator.managers.palantiri.concurrentMapFairSemaphore;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.vandy.simulator.managers.palantiri.Palantir;
import edu.vandy.simulator.managers.palantiri.PalantiriManager;
import edu.vandy.simulator.utils.Assignment;

/**
 * Defines a mechanism that mediates concurrent access to a fixed
 * number of available Palantiri.  This class uses a "fair" Semaphore
 * and a ConcurrentHashMap to mediate concurrent access to the
 * Palantiri.  This class implements a variant of the "Pooling"
 * pattern (kircher-schwanninger.de/michael/publications/Pooling.pdf).
 */
public class ConcurrentMapFairSemaphoreMgr
        extends PalantiriManager {
    /**
     * Debugging tag used by the Android logger.
     */
    protected final static String TAG =
            ConcurrentMapFairSemaphoreMgr.class.getSimpleName();

    /**
     * A FairSemaphore that limits concurrent access to the fixed
     * number of available palantiri managed by the PalantiriManager.
     */
    protected FairSemaphore mAvailablePalantiri;

    /**
     * A concurrent hashmap that associates the @a Palantiri key to
     * the @a boolean values that keep track of whether the key is
     * available.
     *
     * The ConcurrentMap interface is used here to work around an
     * Android version conflict between Java 7 and Java 8. In Java 7,
     * keySet() returns a Set<K>, while Java 8 was changed to return
     * ConcurrentHashMap.KeySetView<K,V>. The compiler will mismatch
     * the signature of the keySet() call when the invoked object is
     * of type ConcurrentHashMap but not if the invoked object is of
     * type ConcurrentMap<K,V>. This is only an issue when compiling
     * with targetCompatibility set to 1.7.
     */
    protected ConcurrentMap<Palantir, Boolean> mPalantiriMap;

    /**
     * Zero parameter constructor required for Factory creation.
     */
    public ConcurrentMapFairSemaphoreMgr() {
    }

    /**
     * Resets the fields to their initial values and tells all beings
     * to reset themselves.
     * <p>
     * Override this class if the being manager implementation has
     * it's own fields or state to reset.
     */
    @Override
    public void reset() {
        super.reset();
    }

    /**
     * Called by super class to build the Palantiri model.
     */
    @Override
    public void buildModel() {
        // Create a new ConcurrentHashMap, iterate through the List of
        // Palantiri and initialize each key in the HashMap with
        // "true" to indicate it's available, and initialize the
        // Semaphore to use a "fair" implementation that mediates
        // concurrent access to the given Palantiri.  Grad students
        // must use a FairSemaphoreCO, whereas ugrad students must use
        // a FairSemaphoreMO.

        // TODO -- you fill in here.

        // Use Java 8 streams to initialize a new ConcurrentHashMap.
        mPalantiriMap = getPalantiri()
            // Convert the list into a stream.
            .stream()

            // Collect palantiri into ConcurrentHashMap and initialize
            // each key to "true" indicating it's available.
            .collect(Collectors.toConcurrentMap(Function.identity(),
                                                p -> true));

        // Initialize the Semaphore to use a "fair" implementation
        // that mediates concurrent access to the given Palantiri.
        // Grad students must use a FairSemaphoreCO, whereas ugrad
        // students must use a FairSemaphoreMO.
        if (Assignment.isUndergraduateTodo()) {
            mAvailablePalantiri = new FairSemaphoreMO(getPalantiri().size());
        } else if (Assignment.isGraduateTodo()) {
            mAvailablePalantiri = new FairSemaphoreCO(getPalantiri().size());
        }
    }

    /**
     * Get a Palantir from the PalantiriManager, blocking until one is
     * available.
     */
    @Override
    @NotNull
    public Palantir acquire() throws CancellationException {
        // Acquire the Semaphore interruptibly and then keep iterating
        // through the ConcurrentHashMap to find the first key in the
        // HashMap whose value is "true" (which indicates it's
        // available for use) and atomically replace the value of this
        // key with "false" to indicate the Palantir isn't available
        // and then return that palantir to the client.  There should
        // be *no* synchronizers in this method.
        // TODO -- you fill in here.

        // Acquire the Semaphore allowing for the premature
        // termination from an Interrupted exception.
        try {
            mAvailablePalantiri.acquire();

            // Keep iterating until we get a palantir.
            for (; ; ) {
                // Iterate through the concurrent hash map.
                for (Palantir pal : mPalantiriMap.keySet()) {
                    if (mPalantiriMap.replace(pal, true, false)) {
                        // Return the Palantiri after finding one that
                        // had previous value of true.
                        return pal;
                    }
                }
            }
        } catch (InterruptedException e) {
            // Wrap the interrupted exception in a
            // CancellationException and throw.
            throw new CancellationException(
                    "ConcurrentFairSemaphoreMgr was interrupted");
        }
    }

    /**
     * Returns the designated @code palantir to the PalantiriManager
     * so that it's available for other Threads to use.  An invalid @a
     * palantir is ignored.
     */
    @Override
    public void release(final Palantir palantir) {
        // Put the "true" value back into ConcurrentHashMap for the
        // palantir key and release the Semaphore if all works
        // properly.  There should be *no* synchronizers in this
        // method.
        // TODO -- you fill in here.

        // Do a simple sanity check!
        if (palantir != null) {
            // Put the "true" value back into ConcurrentHashMap for
            // the Palantir key, which also atomically returns the
            // LeaseState associated with the Palantir back to
            // mNotInUse to indicate it's available again.
            if (!mPalantiriMap.put(palantir, true)) {
                // Release the semaphore if the @a palantir parameter
                // was previously in use.
                mAvailablePalantiri.release();
            }
        }
    }

    /*
     * The following method is just intended for use by the regression
     * tests, not by applications.
     */

    /**
     * Returns the number of available permits on the semaphore.
     */
    protected int availablePermits() {
        return mAvailablePalantiri.availablePermits();
    }

    /**
     * Called when the simulation is being shutdown to allow model
     * components the opportunity to and release resources and to
     * reset field values.  The Beings will have already have been
     * shutdown by the base class before calling this method.
     */
    @Override
    public void shutdownNow() {
        Log.d(TAG, "shutdownNow: called.");
    }
}

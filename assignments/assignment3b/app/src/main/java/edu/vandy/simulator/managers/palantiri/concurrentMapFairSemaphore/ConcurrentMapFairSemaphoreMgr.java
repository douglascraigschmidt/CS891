package edu.vandy.simulator.managers.palantiri.concurrentMapFairSemaphore;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

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
     * <p>
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

        // Initialize the Semaphore to use a "fair" implementation
        // that mediates concurrent access to the given Palantiri.
        // Grad students must use a FairSemaphoreCO, whereas ugrad
        // students must use a FairSemaphoreMO.
        if (Assignment.isUndergraduateTodo()) {
            // TODO -- you fill in here.
        } else if (Assignment.isGraduateTodo()) {
            // TODO -- you fill in here.
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

        // Acquire the Semaphore allowing for the premature
        // termination from an Interrupted exception.
        // TODO -- you fill in here (replace null with proper code).
        return null;
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
        // method. Also, make sure to check if the palantir is null
        // before proceeding..
        // TODO -- you fill in here.
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

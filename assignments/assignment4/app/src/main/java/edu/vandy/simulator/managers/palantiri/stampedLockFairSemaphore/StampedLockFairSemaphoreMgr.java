package edu.vandy.simulator.managers.palantiri.stampedLockFairSemaphore;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;

import edu.vandy.simulator.managers.palantiri.Palantir;
import edu.vandy.simulator.managers.palantiri.PalantiriManager;
import edu.vandy.simulator.managers.palantiri.concurrentMapFairSemaphore.FairSemaphore;
import edu.vandy.simulator.managers.palantiri.concurrentMapFairSemaphore.FairSemaphoreCO;
import edu.vandy.simulator.managers.palantiri.concurrentMapFairSemaphore.FairSemaphoreMO;
import edu.vandy.simulator.utils.Assignment;

import static java.util.stream.Collectors.toMap;

/**
 * Defines a mechanism that mediates concurrent access to a fixed
 * number of available Palantiri.  This class uses a SimpleSemaphore,
 * a HashMap, and a StampedLock to mediate concurrent access to the
 * Palantiri.  This class implements a variant of the "Pooling"
 * pattern (kircher-schwanninger.de/michael/publications/Pooling.pdf).
 */
public class StampedLockFairSemaphoreMgr
        extends PalantiriManager {
    /**
     * Debugging tag used by the Android logger.
     */
    private final static String TAG =
            StampedLockFairSemaphoreMgr.class.getSimpleName();

    /**
     * A counting semaphore that limits concurrent access to the fixed
     * number of available palantiri managed by the
     * StampedLockSimpleSemaphoreMgr.
     */
    FairSemaphore mAvailablePalantiri;

    /**
     * A map that associates the @a Palantiri key to the @a boolean
     * values that keep track of whether the key is available.
     */
    Map<Palantir, Boolean> mPalantiriMap;

    /**
     * A StampedLock synchronizer that protects the Palantiri state.
     */
    // TODO -- you fill in here.  

    /**
     * Zero parameter constructor required for Factory creation.
     */
    public StampedLockFairSemaphoreMgr() {
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

        // Reset all map entries back to true for the next run.
        // TODO -- you fill in here.

        // Since semaphores can't be reset, we have no choice but to
        // create a new FairSemaphore for the next run.
        if (Assignment.isUndergraduateTodo()) {
            // TODO -- you fill in here.
        } else if (Assignment.isGraduateTodo()) {
            // TODO -- you fill in here.
        }

        // Reinitialize the StampedLock.
        // TODO -- you fill in here.
    }

    /**
     * Called by super class to build the Palantiri model.
     */
    @Override
    public void buildModel() {
        // Create a new HashMap, iterate through the List of Palantiri
        // and initialize each key in the HashMap with "true" to
        // indicate it's available, and initialize the Semaphore to
        // use a "fair" implementation that mediates concurrent access
        // to the given Palantiri.

        // Use the getPalantiri() to get a list of Palantiri and
        // initialize each key in the mPalantiriMap with "true" to
        // indicate it's available.  Grad students should use a Java 8
        // stream to initialize mPalantiriMap, whereas ugrad students
        // can implement without using a Java 8 stream.
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

        // Initialize the StampedLock.
        // TODO -- you fill in here.
    }

    /**
     * Get a palantir, blocking until one is available.
     * <p>
     * This method should never return a null Palantir. It may,
     * however, throw a InterruptedException if a shutdown is being
     * processed while a thread is waiting for a Palantir.
     */
    @Override
    @NotNull
    public Palantir acquire() throws CancellationException {
        // Acquire the SimpleSemaphore interruptibly and then use an
        // Iterator to iterate through the HashMap in a thread-safe
        // manner to find the first key in the HashMap whose value is
        // "true" (which indicates it's available for use).  Replace
        // the value of this key with "false" to indicate the Palantir
        // isn't available and then return that palantir to the
        // client.
        // 
        // This implementation should demonstrate StampedLock's
        // support for upgrading a readLock to a writeLock.  You'll
        // need to use an Iterator instead of a for-each loop so that
        // you can restart your search at the beginning of the HashMap
        // if you're unable to atomically upgrade the readlock to a
        // writelock.  This code is tricky, so please carefully read
        // the StampedLock "upgrade" example that's described at
        // docs.oracle.com/javase/8/docs/api/java/util/concurrent/locks/StampedLock.html.

        // TODO -- you fill in here.

        // This method either succeeds by returning a Palantir, or
        // fails if interrupted by a shutdown.  In ether case,
        // reaching this line should not be possible.
        throw new IllegalStateException("This is not possible");
    }

    /**
     * Returns the designated @code palantir to the StampedLockPalantiriManager
     * so that it's available for other Threads to use.
     */
    @Override
    public void release(final Palantir palantir) {
        // Put the "true" value back into HashMap for the palantir key
        // in a thread-safe manner using a write lock and release the
        // SimpleSemaphore if all works properly.
        // TODO -- you fill in here.
    }

    /**
     * Called when the simulation is being shutdown to allow model
     * components the opportunity to and release resources and to
     * reset field values.
     */
    @Override
    public void shutdownNow() {
        Log.d(TAG, "shutdownNow: called.");
    }
}

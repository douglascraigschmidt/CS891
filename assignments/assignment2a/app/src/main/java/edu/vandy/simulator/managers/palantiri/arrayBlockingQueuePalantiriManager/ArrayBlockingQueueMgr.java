package edu.vandy.simulator.managers.palantiri.arrayBlockingQueuePalantiriManager;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CancellationException;

import edu.vandy.simulator.managers.palantiri.Palantir;
import edu.vandy.simulator.managers.palantiri.PalantiriManager;

/**
 * Defines a mechanism that mediates concurrent access to a fixed
 * number of available Palantiri using a Java ArrayBlockingQueue.
 * This implementation is intentionally simple and inefficient since
 * it's not meant for use in production code.
 */
public class ArrayBlockingQueueMgr
        extends PalantiriManager {
    /**
     * Debugging tag used by the Android logger.
     */
    protected final static String TAG =
            ArrayBlockingQueueMgr.class.getSimpleName();

    /**
     * An ArrayBlockingQueue that limits concurrent access to the
     * fixed number of available palantiri managed by the
     * PalantiriManager.
     */
    private ArrayBlockingQueue<Palantir> mAvailablePalantiri;

    /**
     * Resets the fields to their initial values and tells all beings
     * to reset themselves. This method is called at the end of
     * every simulation run so that the manager will be ready for
     * the next simulation run.
     * <p>
     * Override this class if the being manager implementation has
     * it's own fields or state to reset.
     */
    @Override
    public void reset() {
        super.reset();

        // Clear out any elements in the queue.
        mAvailablePalantiri.clear();

        // Add each palantiri back into the queue.
        mAvailablePalantiri.addAll(getPalantiri());
    }

    /**
     * Called by super class to build the Palantiri model.
     * Note that this method is only called when the number
     * of palantiri is changed from the last simulation run.
     */
    @Override
    public void buildModel() {
        int palantiriCount = getPalantirCount();

        // Initialize the ArrayBlockingQueue to use a "fair"
        // implementation that mediates concurrent access to the given
        // Palantiri.
        mAvailablePalantiri =
                new ArrayBlockingQueue<>(palantiriCount, true);

        // Add each palantiri to the queue.
        mAvailablePalantiri.addAll(getPalantiri());
    }

    /**
     * Try to get the next available Palantir from the resource pool,
     * blocking until there are palantir available.
     */
    @NotNull
    public Palantir acquire() throws CancellationException {
        try {
            // Acquire a palantir, blocking until one is available.
            return mAvailablePalantiri.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // This invariant should always hold for all acquire()
        // implementations if implemented correctly. That is the
        // purpose of enforcing the @NotNull along with the
        // CancellationException; It makes it clear that all
        // implementations should either be successful (if implemented
        // correctly) and return a Palantir, or fail because of
        // cancellation. If possible, this implementation needs to be
        // changed so that this statement isn't reached (it is reached
        // currently on every run)
        throw new IllegalStateException(
                "This method should either return a valid " +
                        "Palantir or throw a CancellationException. " +
                        "In either case, this statement should not be reached.");
    }

    /**
     * Returns the designated {@code palantir} back to the
     * PalantiriManager so it's available for other threads to use.
     */
    public void release(final Palantir palantir) {
        // Do a simple sanity check!
        if (palantir != null) {
            try {
                // Add a palantir parameter back to the queue.
                mAvailablePalantiri.put(palantir);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the number of available Palantiri.
     */
    protected int availablePermits() {
        // Returns a count of the number of palantiri that are
        // available for use.
        return mAvailablePalantiri.size();
    }

    /*
     * The following method is just intended for use by the regression
     * tests, not by applications.
     */

    /**
     * Called when the simulation is being shutdown to allow model
     * components the opportunity to and release resources and to
     * reset field values.  The Beings will have already have been
     * shutdown by the base class before calling this method.
     */
    @Override
    public void shutdownNow() {
    }
}

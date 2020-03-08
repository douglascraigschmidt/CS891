package edu.vandy.simulator.managers.palantiri;

import java.util.concurrent.CancellationException;

import edu.vandy.simulator.managers.beings.Being;
import edu.vandy.simulator.model.implementation.components.PalantirComponent;

/**
 * Provides an interface for gazing into a Palantir.  Plays the role
 * of a "command" in the Command pattern.
 */
public class Palantir
        extends PalantirComponent {
    /**
     * PalantiriManager for this Palantir. Used only for
     * error checking and requesting.
     */
    public final PalantiriManager mManager;

    /**
     * For auditing, keeps track of the number of
     * times this Palantir has been used.
     */
    public int mCount = 0;

    /**
     * Constructor initializes the fields.
     */
    public Palantir(PalantiriManager manager) {
        super(manager);
        mManager = manager;
    }

    /**
     * Called to reset a model component to its initial state.
     */
    public void reset() {
        super.reset();
        mCount = 0;
    }

    /**
     * Create a random gaze time between 1 and 5 seconds.
     *
     * @param being The being that is starting to gaze.
     * @throws CancellationException Thrown only if a shutdown
     *                               has been requested that prematurely ends the being's
     *                               busy (gazing) action.
     */
    public void gaze(Being being) throws CancellationException {
        // Notify palantir manager that this palantir is being
        // gazed at by the passed being thread. This call will
        // block this thread for a random time withing the min/max
        // gazing duration range that can be adjusted in the UI
        // layer's settings panel.
        mManager.gazeAtPalantir(being, this);

        // Keep track of gazing count for auditing.
        mCount++;
    }

    /**
     * @return Asks controlling manager if the simulation
     * has a pending error request.
     */
    protected boolean isShutdown() {
        return mManager.isShutdown();
    }

    /**
     * Returns true if @a this is equal to @a other.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Palantir)) {
            return false;
        }
        final Palantir that = (Palantir) other;
        return this.getId() == that.getId();
    }

    /**
     * Returns the hashcode for the Palantir, which is simply its id.
     */
    @Override
    public int hashCode() {
        return (int) getId();
    }

    /**
     * @return The number times this palantir has been gazed at.
     */
    public int getCount() {
        return mCount;
    }

    /**
     * @return Description of class fields excluding null fields.
     */
    @Override
    public String toString() {
        return super.toString()
                + " gazing count = " + mCount;
    }
}

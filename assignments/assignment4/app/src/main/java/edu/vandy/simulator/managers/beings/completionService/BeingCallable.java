package edu.vandy.simulator.managers.beings.completionService;

import java.util.concurrent.Callable;

import edu.vandy.simulator.managers.beings.Being;
import edu.vandy.simulator.managers.beings.BeingManager;
import edu.vandy.simulator.managers.palantiri.Palantir;

/**
 * This class implements the gazing logic of a Being.
 */
class BeingCallable
        extends Being
        implements Callable<BeingCallable> {
    /**
     * Constructor initializes the field.
     *
     * @param manager The controlling BeingManager instance.
     */
    BeingCallable(BeingManager manager) {
        // Call super constructor passing the manager.
        super(manager);
    }

    /**
     * Run the loop that performs the Being gazing logic.
     *
     * @return The being callable object.
     */
    @Override
    public BeingCallable call() {
        // Gaze at a palantir the designated number of times.
        runGazingSimulation(getGazingIterations());

        // TODO -- replace "null" with the appropriate return value.
        return null;
    }

    /**
     * Perform a single gazing operation.
     */
    @Override
    protected void acquirePalantirAndGaze() {
        // Get a palantir from the BeingManager by calling the
        // appropriate base class helper method - this call will block
        // if there are no available palantiri (if a concurrency error
        // occurs in the assignment implementation, null is returned
        // and this being should immediately call Being.error(), which
        // throws an IllegalStateException).  Then gaze at the
        // palantir for this being (which blocks for a random period
        // of time).  Finally, release the palantir for this being via
        // a call to the appropriate base class helper method.

        // TODO -- you fill in here.
    }
}

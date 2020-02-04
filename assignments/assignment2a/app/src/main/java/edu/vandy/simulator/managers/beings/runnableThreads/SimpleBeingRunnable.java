package edu.vandy.simulator.managers.beings.runnableThreads;

import edu.vandy.simulator.managers.beings.Being;
import edu.vandy.simulator.managers.beings.BeingManager;
import edu.vandy.simulator.managers.palantiri.Palantir;

/**
 * This class implements the gazing logic for a being that's
 * implemented via a Java thread.
 */
public class SimpleBeingRunnable
        extends Being
        implements Runnable {
    /**
     * Constructor initializes the field.
     *
     * @param manager The controlling BeingManager instance.
     */
    SimpleBeingRunnable(BeingManager manager) {
        // Call super constructor passing the manager.
        super(manager);
    }

    /**
     * Run the loop that performs the being gazing logic for the
     * designated number of iterations.
     */
    @Override
    public void run() {
        // This template method gazes at a palantir the designated
        // number of times.
        runGazingSimulation(mManager.getGazingIterations());
    }

    /**
     * This hook method performs a single gazing operation.
     */
    @Override
    protected void acquirePalantirAndGaze() {
        // Get a palantir from the PalantiriManager by calling the
        // appropriate Being super class helper method - this call
        // will block if there are no available palantiri (if a
        // concurrency error occurs in the assignment implementation,
        // null is returned and this being should immediately call
        // Being.error(), which throws an IllegalStateException).
        // Then gaze at the palantir for this being (which blocks for
        // a random period of time).  Finally, release the palantir
        // for this being via a call to the appropriate Being super
        // class helper method.
        // TODO -- you fill in here.
    }
}

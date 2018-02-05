package edu.vandy.simulator.managers.beings.executorService;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import edu.vandy.simulator.Controller;
import edu.vandy.simulator.managers.beings.BeingManager;

import static java.util.stream.Collectors.toList;

/**
 * This BeingManager implementation uses the Java ExecutorService to
 * create a pool of Java threads that run the being simulations.
 */
public class ExecutorServiceMgr
       extends BeingManager<BeingCallable> {
    /**
     * Used for Android debugging.
     */
    private final static String TAG =
        ExecutorServiceMgr.class.getName();

    /**
     * The list of futures to BeingCallables that are running
     * concurrently in the ExecutorService's thread pool.
     */
    // TODO -- you fill in here.

    /**
     * The ExecutorService contains a fixed pool of threads.
     */
    // TODO -- you fill in here.

    /**
     * Default constructor.
     */
    public ExecutorServiceMgr() {
    }

    /**
     * @return The futures list.
     */
    protected List<Future<BeingCallable>> getFutureList() {
        // TODO -- you fill in here, replacing null with the
        // appropriate code.
        return null;
    }

    /**
     * @return The executor service.
     */
    protected ExecutorService getExecutor() {
        // TODO -- you fill in here, replacing null with the
        // appropriate code.
        return null;
    }

    /**
     * Resets the fields to their initial values
     * and tells all beings to reset themselves.
     */
    @Override
    public void reset() {
        super.reset();
    }

    /**
     * Abstract method that BeingManagers implement to return a new
     * BeingCallable instance.
     *
     * @return A new typed Being instance.
     */
    @Override
    public BeingCallable newBeing() {
        // Return a new BeingCallable instance.
        // TODO -- you fill in here, replacing null with the
        // appropriate code.
        return null;
    }

    /**
     * This entry point method is called by the Simulator framework to
     * start the being gazing simulation.
     **/
    @Override
    public void runSimulation() {
        // Call a method that uses the ExecutorService to create/start
        // a pool of threads that represent the beings in this
        // simulation.
        // TODO -- you fill in here.

        // Call a method that starts a thread to wait for all futures
        // to complete.
        // TODO -- you fill in here.
    }

    /**
     * Use the ExecutorService to create/start a pool of threads that
     * represent the beings in this simulation.
     */
    protected void beginBeingThreadPool() {
        // All STUDENTS:
        // Create an ExecutorService instance that contains a
        // fixed-size pool of threads, with one thread for each being.
        // Call the BeingManager.getBeings() method to iterate through
        // the BeingCallables, submit each BeingCallable to the
        // ExecutorService, and add it to the list of BeingCallable
        // futures.
        // TODO -- you fill in here.

        // GRADUATE STUDENTS:
        // Use a Java 8 stream to submit each BeingCallable and
        // collect the results into the list of BeingCallable futures.
        // Undergraduate students are free to use a Java 8 stream, but
        // it's not required.
    }

    /**
     * Spawn a thread that waits for all the futures to complete.
     */
    void awaitCompletionOfFutures() {
        // All STUDENTS:
        // Use a for-each loop to wait for all futures to complete.
        // Tell the Controller log when a simulation completes
        // normally.  If an exception is thrown, however, then tell
        // the Controller log which exception was caught.

        // GRADUATE STUDENTS:
        // Use a Java 8 stream (including a map() aggregate operation)
        // instead of a for-each loop to process the futures.
        // Undergraduate students are free to use a Java 8 stream, but
        // it's not required.
    }

    /**
     * Called to run to error the simulation and should only return
     * after all threads have been terminated and all resources
     * cleaned up.
     */
    @Override
    public void shutdownNow() {
        Controller.log(TAG + ": shutdownNow: entered");

        // Cancel all the outstanding BeingCallables via their
        // futures.
        // TODO -- you fill in here.

        // Shutdown the executor *now*.
        // TODO -- you fill in here.

        Controller.log(TAG + ": shutdownNow: exited with "
                       + getRunningBeingCount() + "/"
                       + getBeingCount() + " running beings.");
    }
}

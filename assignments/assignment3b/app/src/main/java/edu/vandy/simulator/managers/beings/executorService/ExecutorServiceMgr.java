package edu.vandy.simulator.managers.beings.executorService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import edu.vandy.simulator.Controller;
import edu.vandy.simulator.managers.beings.BeingManager;
import edu.vandy.simulator.utils.Assignment;

import static edu.vandy.simulator.utils.ExceptionUtils.rethrowSupplier;
import static java.util.stream.Collectors.toList;

/**
 * This BeingManager implementation uses the Java ExecutorService to
 * create a fixed-sized pool of Java threads that run being
 * simulations.
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

        // Call a method that waits for all futures to complete.
        // TODO -- you fill in here.

        // Call this class's shutdownNow() method to cleanly shutdown
        // the executor service.
        // TODO -- you fill in here.
    }

    /**
     * Creates a fixed-size xthread pool executor service.
     *
     * @param size The number of threads in the thread pool
     * @return A fixed-sized thread pool executor
     */
    ExecutorService createExecutorService(int size) {
        // TODO -- you fill in here (replace null with an executor service instance).
        return null;
    }

    /**
     * Use the ExecutorService to create/start a pool of threads that
     * represent the beings in this simulation.
     */
    void beginBeingThreadPool() {
        // All STUDENTS:
        // Create an ExecutorService instance that contains a
        // fixed-size pool of threads. Use the getThreadCount()
        // helper method to set the number of threads in this pool
        // This helper method retrieves the thread count from the
        // application's settings panel thereby allowing you to
        // dynamically tune the pool size at run time and observe
        // how this size affects the level of concurrency).
        // Call the BeingManager.getBeings() method to iterate through
        // the BeingCallables, submit each BeingCallable to the
        // ExecutorService, and add it to the list of BeingCallable
        // futures.

        // GRADUATE STUDENTS:
        // Use a Java 8 stream to submit each BeingCallable and
        // collect the results into the list of BeingCallable futures.
        // Undergraduate students are free to use a Java 8 stream, but
        // it's not required.

        // TODO -- you fill in here.
    }

    /**
     * Wait for all the futures to complete.
     */
    void awaitCompletionOfFutures() {
        // UNDERGRADUATE STUDENTS:
        // Use a for-each loop to wait for all futures to complete.
        // When all futures have completed successfully set a flag
        // that will be used to log the success or failure of this
        // method. If a checked exception is thrown, however, then
        // catch it, immediately stop processing, and wrap/rethrow the
        // exception as a runtime exception.

        // GRADUATE STUDENTS:
        // Use the Java forEach() method instead of a Java for-loop to
        // process the futures and also use the ExceptionUtils
        // rethrowSupplier() method to avoid the need for a try/catch
        // block to handle checked exceptions. Make sure that your
        // solution does not contain any try/catch blocks.  When all
        // futures have completed successfully set a flag that will be
        // used to log the success or failure of this method.
        //
        // Undergraduate students are free to also use the Java
        // forEach() method, but it's not required.

        // TODO -- you fill in here.

        // Used to log if all futures being values were successfully
        // obtained.
        boolean succeeded = false;

        try {
            if (Assignment.isUndergraduateTodo()) {
            } else if (Assignment.isGraduateTodo()) {
            } else {
                throw new IllegalStateException("Invalid assignment type");
            }

        } finally {
            Controller.log(TAG
                    + ": awaitCompletionOfFutures: "
                    + "processing of all "
                    + getBeingCount()
                    + (succeeded ? " succeeded." : "failed."));
        }
    }

    /**
     * Called to terminate the executor service. This method should
     * only return after all threads have been terminated and all
     * resources cleaned up.
     */
    @Override
    public void shutdownNow() {
        Controller.log(TAG + ": shutdownNow: entered");

        // Cancel all the outstanding BeingCallables via their
        // futures, but only if they aren't already done or already
        // canceled.
        // TODO -- you fill in here.

        // Shutdown the executor *now*.
        // TODO -- you fill in here.

        Controller.log(TAG + ": shutdownNow: exited with "
                + getRunningBeingCount() + "/"
                + getBeingCount() + " running beings.");
    }
}

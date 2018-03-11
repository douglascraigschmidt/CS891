package edu.vandy.simulator.managers.beings.asyncTask;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import edu.vandy.simulator.Controller;
import edu.vandy.simulator.managers.beings.BeingManager;

/**
 * This BeingManager implementation uses the Android AsyncTask
 * framework and the Java ExecutorService framework to create a pool
 * of Java threads that run the being simulations.
 */
public class AsyncTaskMgr
        extends BeingManager<AsyncBeing> {
    /**
     * Used for Android debugging.
     */
    private final static String TAG =
            AsyncTaskMgr.class.getName();

    /**
     * A custom ThreadPoolExecutor containing a pool of threads that
     * are allocated dynamically and cached.
     */
    // TODO -- you fill in here.

    /**
     * A CyclicBarrier entry barrier that ensures all background
     * threads start running at the same time.
     */
    // TODO -- you fill in here.

    /**
     * A CountDownLatch exit barrier that ensures the waiter thread
     * doesn't finish until all the BeingAsyncTasks finish.
     */
    // TODO -- you fill in here.

    /**
     * A ThreadFactory that spawns an appropriately named thread for
     * each being.
     */
    protected ThreadFactory mThreadFactory = new ThreadFactory() {
            /**
             * Used to allocate a unique id to each new thread.
             */
            // TODO -- replace null with a properly initialized AtomicInteger.
            private AtomicInteger mId = null;

            /**
             * Constructs a new Thread with a unique thread name.
             *
             * @param runnable a runnable to be executed by new thread instance.
             */
            @Override
            public Thread newThread(Runnable runnable) {
                // Use the mId field to ensure each new thread is
                // given a unique name.
                // TODO -- you fill in here by replacing "return null".
                return null;
            }
    };

    /**
     * Default constructor.
     */
    public AsyncTaskMgr() {
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
     * BeingAsyncTask instance.
     *
     * @return A new typed Being instance.
     */
    @Override
    public AsyncBeing newBeing() {
        // Return a new BeingAsyncTask instance.
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
        // Use the ThreadPoolExecutor to create and execute an
        // BeingAsyncTask for each being.
        // TODO -- you fill in here.

        // Wait for all the beings to finish gazing at the palantiri.
        // TODO -- you fill in here.

        // Call the shutdownNow() method to cleanly shutdown.
        // TODO -- you fill in here.
    }

    /**
     * Create and execute the BeingAsyncTasks that represent
     * the beings in this simulation.
     */
    void beginBeingAsyncTasksGazing() {
        // Store the number of beings to create.
        int beingCount = getBeings().size();

        // Initialize an entry barrier that ensures all BeingAsyncTasks
        // start running at the same time.
        // TODO -- you fill in here.

        // Initialize an exit barrier to ensure the waiter thread
        // doesn't finish until all the BeingAsyncTasks finish.
        // TODO -- you fill in here.

        // Create a ThreadPoolExecutor containing a pool of no more
        // than beingCount threads that are allocated dynamically and
        // cached for up to 60 seconds.  An instance of
        // SynchronousQueue should be used as the work queue and
        // mThreadFactory should be passed as the final parameter.
        // TODO -- you fill in here.

        // Execute all the BeingAsyncTasks on mThreadPoolExecutor,
        // passing in the entry and exit barriers.
        // TODO -- you fill in here.  Graduate students must use Java
        // 8 features, whereas undergraduate students can optionally
        // use Java 8 features.
    }

    /**
     * Wait for all the beings to finish gazing at the palantiri.
     */
    void waitForBeingAsyncTasksToFinishGazing() {
        // Wait for all the threads in the ThreadPoolExecutor to
        // terminate.
        try {
            // Allow all the BeingAsyncTasks to start gazing.
            // TODO -- you fill in here.

            // Wait for all BeingAsyncTasks to stop gazing.
            // TODO -- you fill in here.
        } catch (Exception e) {
            Controller.log(TAG +
                           ": awaitTerminationOfThreadPoolExecutor() caught exception: "
                           + e);
            // Shutdown the simulation now.
            // TODO -- you fill in here.
        }

        // Print the number of beings that were processed.
        Controller.log(TAG +
                ": awaitCompletionOfFutures: exiting with "
                + getRunningBeingCount()
                + "/"
                + getBeings().size()
                + " running beings.");
    }

    /**
     * Called to terminate the BeingAsyncTasks. This method should
     * only return after all threads have been terminated and all
     * resources cleaned up.
     */
    @Override
    public void shutdownNow() {
        Controller.log(TAG + ": shutdownNow: entered");

        // Cancel all the outstanding BeingAsyncTasks immediately.
        // TODO -- you fill in here.  Graduate students must use Java
        // 8 features, whereas undergraduate students can optionally
        // use Java 8 features.

        // Shutdown the executor *now*.
        // TODO -- you fill in here.

        Controller.log(TAG + ": shutdownNow: exited with "
                + getRunningBeingCount() + "/"
                + getBeingCount() + " running beings.");
    }
}

package edu.vandy.simulator.managers.beings.asyncTask;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadPoolExecutor;

import edu.vandy.simulator.Controller;
import edu.vandy.simulator.managers.beings.Being;
import edu.vandy.simulator.managers.beings.BeingManager;
import edu.vandy.simulator.managers.palantiri.Palantir;

/**
 * This class implements the gazing logic of a being by forwarding to
 * an instance of Android AsyncTask.
 */
class AsyncBeing extends Being {

    /**
     * Used for Android debugging.
     */
    private final static String TAG =
            AsyncBeing.class.getName();

    /**
     * Runs the being gazing logic in a background thread.
     */
    protected AsyncBeingTask mAsyncTask;

    /**
     * Constructor initializes the field.
     *
     * @param manager The controlling BeingManager instance.
     */
    AsyncBeing(BeingManager manager) {
        // Call super constructor passing the manager.
        super(manager);
    }

    /**
     * Executes the task with the specified parameters on the given
     * {@code threadPoolExecutor}.
     *
     * @param entryBarrier       A CyclicBarrier entry barrier that ensures all background
     *                           threads start running at the same time.
     * @param exitBarrier        A CountDownLatch exit barrier that ensures the waiter thread
     *                           doesn't finish until all the BeingAsyncTasks finish.
     * @param threadPoolExecutor The executor to use.
     */
    void executeOnExecutor(CyclicBarrier entryBarrier,
                           CountDownLatch exitBarrier,
                           ThreadPoolExecutor threadPoolExecutor) {
        // Create a new async task.
        // TODO -- you fill in here.

        // Execute the task on the given thread pool.
        // TODO -- you fill in here.
    }

    /**
     * A factory method that creates and returns a new BeingAsyncTask
     * that when run, will perform gazing operations.
     *
     * @param entryBarrier A CyclicBarrier entry barrier that ensures all background
     *                     threads start running at the same time.
     * @param exitBarrier  A CountDownLatch exit barrier that ensures the waiter thread
     *                     doesn't finish until all the BeingAsyncTasks finish.
     * @return
     */
    protected AsyncBeingTask createTask(CyclicBarrier entryBarrier,
                                        CountDownLatch exitBarrier) {
        // TODO -- you fill in here by replacying null with the
        // appropriate code.
        return null;
    }

    /**
     * Attempts to cancel execution of this task.
     *
     * @param mayInterruptIfRunning True if the thread executing this
     *                              task should be interrupted;
     *                              otherwise, in-progress tasks are
     *                              allowed to complete.
     */
    public void cancel(boolean mayInterruptIfRunning) {
        // TODO -- you fill in here.
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

    /**
     * An inner AsyncTask class used to perform Being gazing iterations
     * in a background thread.
     *
     * param Integer Used to pass the number of gazing iterations to run.
     * param Void A no-op.
     * param String Used to indicate the success or failure of doInBackground().
     */
    @SuppressLint("StaticFieldLeak")
    class AsyncBeingTask extends AsyncTask<Integer, Void, String> {
        /**
         * A CyclicBarrier entry barrier that ensures all background
         * threads start running at the same time.
         */
        protected CyclicBarrier mEntryBarrier;

        /**
         * A CountDownLatch exit barrier that ensures the waiter
         * thread doesn't finish until all the BeingAsyncTasks finish.
         */
        protected CountDownLatch mExitBarrier;

        /**
         * Constructor initializes barrier fields.
         *
         * @param entryBarrier A CyclicBarrier entry barrier that ensures all background
         *                     threads start running at the same time.
         * @param exitBarrier  A CountDownLatch exit barrier that ensures the waiter thread
         *                     doesn't finish until all the BeingAsyncTasks finish.
         */
        public AsyncBeingTask(CyclicBarrier entryBarrier,
                              CountDownLatch exitBarrier) {
            mEntryBarrier = entryBarrier;
            mExitBarrier = exitBarrier;
        }

        /**
         * Hook method invoked by the AsyncTask framework before
         * doInBackground() starts to run.
         */
        @Override
        protected void onPreExecute() {
            // Call the local log() method to print a string
            // indicating that this method has been called for this
            // being.
            // TODO -- you fill in here.
        }

        /**
         * Run the being gazing logic in a background thread.
         */
        @Override
        protected String doInBackground(Integer... gazingIterations) {
            try {
                // Don't start gazing until all
                // BeingAsyncTasks are ready to run.
                // TODO -- you fill in here.
            } catch (Exception e) {
                return "being failed with exception " 
                    + e.getMessage();
            }

            // Gaze at a palantir the designated # of times.
            runGazingSimulation(gazingIterations[0]);

            return "being succeeded";
        }

        /**
         * Hook method invoked by the AsyncTask framework
         * after doInBackground() completes successfully.
         */
        @Override
        public void onPostExecute(String message) {
            // Print the message via the local log() method method.
            // TODO -- you fill in here.

            // Inform the AsyncTaskMgr that this AsyncTask is done.
            // TODO -- You fill in here.
        }

        /**
         * Hook method invoked by the AsyncTask framework if
         * doInBackground() is cancelled.
         */
        @Override
        public void onCancelled(String s) {
            // Forward to onPostExecute() to inform the AsyncTaskMgr
            // that this AsyncTask has been cancelled.
            // TODO -- You fill in here.
        }

        /**
         * Prints out a formatted logging message.
         *
         * @param format A printf style string format.
         * @param args   Optional printf format arguments.
         */
        protected void log(String format, Object... args) {
            Controller.log(TAG
                           + "[" + Thread.currentThread().getName()
                           + "|" + getId()
                           + "]: "
                           + format, 
                           args);
        }
    }
}

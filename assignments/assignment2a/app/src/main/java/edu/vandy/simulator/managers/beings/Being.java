package edu.vandy.simulator.managers.beings;

import androidx.annotation.CallSuper;
import android.util.Log;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CancellationException;

import edu.vandy.simulator.Controller;
import edu.vandy.simulator.managers.palantiri.Palantir;
import edu.vandy.simulator.model.implementation.components.BeingComponent;

/**
 * This class runs the gazing iteration loop for a single Being
 * thread.
 * <p>
 * BeingManager implementations can subclass this Being class to
 * provide their own style of concurrency object (Runnable, Callable,
 * etc.). The abstract BeingManager class is a generic typed class
 * that receives any subclass of this Being class in its class
 * definition. The BeingManager concrete implementation is required to
 * provide a newBeing() method that is responsible for returning a
 * custom Being subclass object.
 */
public abstract class Being
       extends BeingComponent {
    /**
     * Logging tag.
     */
    private static final String TAG = "Being";

    /**
     * Reference to the controlling simulator.
     */
    public final BeingManager mManager;

    /**
     * The thread this being is being run on and is package
     * private so that manager can access it for shutdown.
     */
    public Thread mThread;

    /**
     * The number of completed iterations.
     */
    public int mCompleted;

    /**
     * Constructor initializes the field.
     *
     * @param manager The controlling BeingManager instance.
     */
    public Being(BeingManager manager) {
        // Call super constructor passing the manager.
        super(manager);

        // Initialize the fields.
        mManager = manager;
    }

    /**
     * @return The number of gazing iterations
     * (maintained by the BeingManager).
     */
    public int getGazingIterations() {
        return mManager.getGazingIterations();
    }

    /**
     * @return The number of completed gazing iterations.
     */
    public int getCompleted() {
        return mCompleted;
    }

    /**
     * Perform the specified number of gazing operations.
     * This method will set the mRunning flag to false when
     * it returns (normally or by rethrowing an exception).
     *
     * @param gazingIterations The number of gazing operations.
     */
    @CallSuper
    public void runGazingSimulation(int gazingIterations) {
        if (isRunning()) {
            error("Should not be possible that 'isRunning' " 
                  + "is true since it is cleared in the finally");
        }

        setRunning(true);

        // Keep track of how many iterations we complete.
        // This count is automatically incremented in any
        // call to the releasePalantir() helper method.
        mCompleted = 0;

        try {
            // Iterate for the designated number of times each Being
            // can gaze into a Palantir.
            while (!isCancelled() && mCompleted != gazingIterations) {
                // Start gazing into the Palantir.
                acquirePalantirAndGaze();
            }
        } catch (CancellationException e) {
            // Swallow cancellation exception but record the event.
            warn(this + " has been cancelled!");
        } finally {
            if (!isCancelled()) {
                Controller.log(this + " completed normally.");
            }

            // This call will clear the mThread field which is used
            // to determine if this thread is currently running.
            setRunning(false);
        }
    }

    /**
     * Perform a single gazing operation.
     */
    protected abstract void acquirePalantirAndGaze();

    /**
     * Helper method that should be called by base classes
     * to acquire a Palantir. It simply forwards the request
     * to the BeingManager.
     */
    public Palantir acquirePalantir() {
        return mManager.acquirePalantir(this);
    }

    /**
     * Helper method that should be called by base classes
     * to release a Palantir. The completed iterations count
     * is updated and the request is forwarded to the
     * BeingManager.
     */
    protected void releasePalantir(Palantir palantir) {
        // Only increment completed value if not cancelled.
        if (!isCancelled()) {
            mCompleted++;
        }
        mManager.releasePalantir(this, palantir);
    }

    /**
     * Called when an unrecoverable error occurs. The error is passed
     * up the chain to the manager to deal with and will also force an
     * IllegalStateException in the calling thread.
     *
     * @param msg An optional message describing the error.
     */
    public void error(@Nullable String msg) {
        mManager.error(msg);
    }

    /**
     * Called when an unrecoverable error occurs. The error
     * is passed up the chain to the manager to deal with.
     *
     * @param throwable An exception that caused the error.
     */
    public void error(Throwable throwable) {
        mManager.error(throwable);
    }

    /**
     * @return {@code true} if this being's thread is running
     * {@code false} if the being's thread has terminated.
     */
    public boolean isRunning() {
        return mThread != null && mThread.isAlive();
    }

    /**
     * Sets the mThread field which is used by {@link #isRunning} to
     * determine if this being thread is alive, and also provides
     * access to the being thread handle when the being manager needs
     * to forcefully shutdown all running beings. This method should
     * be called with {@code true} when a thread first starts, and
     * with {@code false} as the last statement before a thread exits.
     *
     * @param running {@code true} if running, {@code false}
     *                if not running.
     */
    public void setRunning(boolean running) {
        if (running) {
            if (mThread != null && mThread.isAlive()) {
                error("Being: setRunning(true) called when a thread " +
                      "is already running.");
            }
            mThread = Thread.currentThread();
            //Controller.log("setRunning: " + this + " started.");
        } else {
            // Clear the mThread field. This should be
            // called as the last statement before a
            // thread exits.
            if (mThread == null || !mThread.isAlive()) {
                warn("Being: setRunning(false) called but " +
                     "the being thread is not alive.");
            }
            mThread = null;
            //Controller.log("setRunning: " + this + " completed.");
        }
    }

    /**
     * Routes all non-fatal warning messages up the chain.
     *
     * @param message A non-fatal warning message.
     */
    private void warn(String message) {
        mManager.warn(message);
    }

    /**
     * Called to force an interrupt of the being thread. Note that
     * this method does not call shutdownNow to set the cancelled flag
     * because of possible infinite recursion.
     *
     * @return {@code true} if the call is successful and the thread
     * is no longer alive, {@code false} if the thread is still running.
     */
    public boolean interruptNow() {
        Log.e(TAG, "interruptNow: called: " + this);

        if (mThread != null && mThread.isAlive()) {
            Log.e(TAG, "interruptNow: interrupting being "
                  + this + " thread ...");
            mThread.interrupt();
            return !mThread.isAlive();
        } else {
            return true;
        }
    }

    /**
     * Keep toString informative but simple since you view it in the
     * debugger. It can be used in log statements but should not look
     * like one.
     *
     * @return In informative string describing a being's state.
     */
    @Override
    public String toString() {
        return super.toString() 
            + " iterations = " 
            + mCompleted 
            + "/" 
            + getGazingIterations();
    }
}

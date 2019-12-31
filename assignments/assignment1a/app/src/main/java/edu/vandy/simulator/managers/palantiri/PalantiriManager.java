package edu.vandy.simulator.managers.palantiri;

import androidx.annotation.CallSuper;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

import edu.vandy.simulator.Simulator;
import edu.vandy.simulator.managers.beings.Being;
import edu.vandy.simulator.managers.palantiri.arrayBlockingQueuePalantiriManager.ArrayBlockingQueueMgr;
import edu.vandy.simulator.model.interfaces.CancellableComponent;
import edu.vandy.simulator.model.interfaces.Model;
import edu.vandy.simulator.model.interfaces.ModelProvider;

/**
 * Abstract class that builds and the model's list of Palantiri and
 * routes requests from the Simulator to the PalantiriManager subclass
 * implementation of the acquire and release methods. It also provides
 * overloaded {@link #error} methods that can be called to handle any
 * non-recoverable error.  
 *
 * <p> This class also contains a factory for generating different
 * PalantiriManager implementation strategies.
 */
public abstract class PalantiriManager
       implements ModelProvider, 
                  CancellableComponent {
    /**
     * Logging tag.
     */
    private static final String TAG = "PalantiriManager";

    /**
     * A back reference to the controlling Simulator instance that
     * manages all request routing as well as error handling.  (Not
     * currently used by PalantiriManager).
     */
    private Simulator mSimulator;

    /**
     * The list of all Palantiri used in the simulation.
     */
    @NotNull
    public List<Palantir> mPalantiri = new ArrayList<>();

    /**
     * Flag indicating if this model component should shutdown.
     */
    private volatile boolean mCancelled = false;

    /**
     * Flag indicating if this model component is running or active.
     */
    private volatile boolean mRunning = true;

    /**
     * Zero parameter constructor required for Factory creation.
     */
    public PalantiriManager() {
    }

    /**
     * Builds the Palantiri management model. Initialize all class
     * fields prior to the simulation. All Palantiri will have already
     * been created by the super class and the list of Palantiri can
     * be accessed via the getPalantiri() method and the count via
     * getPalantirCount().
     */
    abstract protected void buildModel();

    /**
     * Called to acquire a Palantir and will block until one is
     * available.
     *
     * @return The first available Palantir.
     */
    // @NotNull specified to be illustrative of this abstract
    // methods contractual obligation.
    @SuppressWarnings("NullableProblems")
    @NotNull
    protected abstract Palantir acquire()
            throws CancellationException, InterruptedException;

    /**
     * Returns the designated {@code palantir} to the pool
     * of available Palantiri so that it is available for
     * use by other threads.
     *
     * @param palantir The palantir to release back to
     *                 the Palantir pool.
     */
    protected abstract void release(Palantir palantir);

    /**
     * Called when the simulation is being shutdown
     * to allow model components the opportunity to
     * and release resources and to reset field values.
     */
    public abstract void shutdownNow();

    /**
     * This method is just intended for use by the regression
     * tests, not by applications.
     *
     * @return the number of available permits on the semaphore.
     */
    protected abstract int availablePermits();

    /**
     * @return The number of Palantiri in this model.
     */
    public int getPalantirCount() {
        return getPalantiri().size();
    }

    /**
     * Builds a new simulation model with the specified model parameters.
     *
     * @param simulator     A reference to the controlling simulator.
     * @param palantirCount The number of Palantiri.
     */
    public void buildModel(Simulator simulator, int palantirCount) {
        // Save the controller simulator instance.
        mSimulator = simulator;

        // Initialize the Palantiri (no need to save palantirCount
        // since it's implicit in the mPalantiri size).
        mPalantiri = makePalantiri(palantirCount);

        // Now that the generic model components have been setup,
        // call the concrete implementation's buildModel() method
        // so that it can setup its custom model requirements.
        buildModel();
    }

    /**
     * Create the requested number of Palantiri.
     *
     * @param count The number of Palantiri to create.
     */
    private List<Palantir> makePalantiri(int count) {
        // Reset all palantir id generator to 0.
        Palantir.resetIds();

        // Create a list to hold the generated Palantiri.
        List<Palantir> palantiri = new ArrayList<>(count);

        // Create and add each new Palantir into the list.
        // The id of each Palantir is its position in the list.
        for (int i = 0; i < count; ++i) {
            Palantir palantir = new Palantir(this);
            palantiri.add(palantir);
        }

        return palantiri;
    }

    /**
     * Get the next available Palantir from the resource pool,
     * blocking until one is available.
     *
     * @param beingId The being id (not currently used).
     * @return A Palantir or null if the a shutdown in progress.
     */
    // TODO: fix this and them remove comment...
    //@NotNull
    final public Palantir acquirePalantir(long beingId) {
        try {
            return acquire();
        } catch (InterruptedException e) {
            // TODO: Mapping any exception to a null value is
            // a bad idea. Fix this and use @NotNull for this
            // method and let it rethrow the exception or better
            // yet, not even catch it.
            return null;
        }
    }

    /**
     * Performs a being request to gaze at the previously
     * acquired palantir for a random interval of time.
     * This operation is forwarded to the simulator to handle.
     *
     * @param being The being thread that is requesting to gaze.
     * @param palantir The palantir previously acquired by a call
     *                 to {@link #acquirePalantir(long)}.
     */
    public void gazeAtPalantir(Being being, Palantir palantir) {
        mSimulator.gazeIntoPalantir(being, palantir);
    }

    /**
     * Releases the designated @code palantir so it's available for
     * other Beings to use.  If @a palantir is null it is ignored.
     *
     * @param palantir The Palantir to release.
     */
    final public void releasePalantir(Palantir palantir) {
        release(palantir);
    }

    /**
     * Forwards error request to the controlling simulator.
     * BeingManager concrete subclasses should override this
     * method to cleanly error all being threads and release
     * any allocated resources.
     */
    final public void shutdown() {
        Log.i(TAG, "shutdown: called.");

        // Notify abstract implementation that a shutdown
        // is being performed.
        shutdownNow();

        // Clear running flag now that this component has shutdown.
        mRunning = false;

        Log.i(TAG, "shutdown: completed.");
    }

    /**
     * Should be called periodically to determine if a
     * shutdown has been requested. This method should
     * be called at a relatively fine-grained interval
     * to ensure responsive shutdown requests.
     *
     * @return {@code true} if the component should shutdown.
     */
    @Override
    public boolean isCancelled() {
        return mCancelled;
    }

    /**
     * @return {@code true} if the component is currently
     * running or active (has not been shutdown or has
     * completed).
     */
    @Override
    public boolean isRunning() {
        return mRunning;
    }

    /**
     * Resets the fields to their initial values
     * and tells all beings to reset themselves.
     * <p>
     * Override this class if the being manager
     * implementation has it's own fields or
     * state to reset.
     */
    @CallSuper
    public void reset() {
        // Just reset each palantir.
        mPalantiri.forEach(Palantir::reset);

        // Reset for next simulation run.
        mRunning = false;
        mCancelled = false;
    }

    /**
     * Forwards error report the controlling simulator which will
     * in return call the being manager class to shutdown.
     */
    final protected void error(@NotNull Exception e) {
        mSimulator.error(e);
    }

    /**
     * Forwards error report the controlling simulator which will
     * in return call the being manager class to shutdown.
     */
    final protected void error(@NotNull String msg) {
        mSimulator.error(msg);
    }

    @NotNull
    public List<Palantir> getPalantiri() {
        return mPalantiri;
    }

    /**
     * @return Forwards the model request to the Simulator to handle.
     */
    @Override
    @NotNull
    final public Model getModel() {
        return mSimulator.getModel();
    }

    /**
     * @return Asks controlling Simulator if there is a pending
     * error request.
     */
    final public boolean isShutdown() {
        return mSimulator.isShutdown();
    }

    /**
     * Static factory class used to create new
     * instances of supported Palantiri managers.
     */
    public static class Factory {
        /**
         * Logging tag.
         */
        private static final String TAG = "PalantiriManager.Factory";

        /**
         * Disallow object creation.
         */
        private Factory() {
        }

        /**
         * Creates the specified {@code type} simulator.
         *
         * @param type Type of simulator.
         * @return A transform instance of the specified type.
         */
        public static PalantiriManager newManager(Type type,
                                                  int palantirCount,
                                                  Simulator simulator) {
            try {
                PalantiriManager manager = type.clazz.newInstance();
                manager.buildModel(simulator, palantirCount);
                return manager;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Supported crawlers that can be applied to download, transform,
         * and store images. The enum values are set to class types so
         * that they can be used to create crawler objects using
         * newInstance().
         */
        public enum Type {
            ARRAY_BLOCKING_QUEUE(ArrayBlockingQueueMgr.class),
            NO_MANAGER(NoManager.class);

            public final Class<? extends PalantiriManager> clazz;

            Type(Class<? extends PalantiriManager> clazz) {
                this.clazz = clazz;
            }

            @Override
            public String toString() {
                return clazz.getSimpleName();
            }

            public Class<? extends PalantiriManager> getClazz() {
                return clazz;
            }
        }

        /**
         * An empty simulator implementation that can be used to
         * disable individual Type enum entries.
         */
        public static class NoManager extends PalantiriManager {
            @Override
            protected void buildModel() {
            }

            @Override
            protected Palantir acquire() {
                throw new RuntimeException("Not implemented.");
            }

            @Override
            protected void release(Palantir palantir) {
                throw new RuntimeException("Not implemented.");
            }

            @Override
            protected int availablePermits() {
                throw new RuntimeException("Not implemented.");
            }

            @Override
            public void shutdownNow() {
                throw new RuntimeException("Not implemented.");
            }
        }
    }
}

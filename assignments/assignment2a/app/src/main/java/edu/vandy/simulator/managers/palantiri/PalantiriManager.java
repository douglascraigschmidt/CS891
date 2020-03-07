package edu.vandy.simulator.managers.palantiri;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

import edu.vandy.simulator.Simulator;
import edu.vandy.simulator.managers.beings.Being;
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
     * The list of all Palantiri used in the simulation.
     */
    @NotNull
    public List<Palantir> mPalantiri = new ArrayList<>();
    /**
     * A back reference to the controlling Simulator instance that
     * manages all request routing as well as error handling.  (Not
     * currently used by PalantiriManager).
     */
    public Simulator mSimulator;
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
    //@NotNull
    final public Palantir acquirePalantir(long beingId) {
        try {
            return acquire();
        } catch (InterruptedException e) {
            return null;
        }
    }

    /**
     * Performs a being request to gaze at the previously
     * acquired palantir for a random interval of time.
     * This operation is forwarded to the simulator to handle.
     *
     * @param being    The being thread that is requesting to gaze.
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
        // Notify abstract implementation that a shutdown
        // is being performed.
        shutdownNow();

        // Clear running flag now that this component has shutdown.
        mRunning = false;
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
        mSimulator.throwError(e);
    }

    /**
     * Forwards error report the controlling simulator which will
     * in return call the being manager class to shutdown.
     */
    final protected void error(@NotNull String msg) {
        mSimulator.throwError(msg);
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
                PalantiriManager manager = type.newInstance();
                manager.buildModel(simulator, palantirCount);
                return manager;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public enum Type {
            ARRAY_BLOCKING_QUEUE("arrayBlockingQueuePalantiriManager.ArrayBlockingQueueMgr"),
            SPIN_LOCK_SEMAPHORE("spinLockHashMap.SpinLockHashMapMgr"),
            REENTRANT_LOCK_HASH_MAP_SIMPLE_SEMAPHORE("reentrantLockHashMapSimpleSemaphore.ReentrantLockHashMapSimpleSemaphoreMgr"),
            STAMPED_LOCK("stampedLockSimpleSemaphore.StampedLockSimpleSemaphoreMgr"),
            CONCURRENT_MAP_FAIR_SEMAPHORE("concurrentMapFairSemaphore.ConcurrentMapFairSemaphoreMgr"),
            SUSPENDING_SPIN_LOCK_SEMAPHORE("structuredConcurrency.SpinLockHashMapMgr");

            public final String className;

            Type(String className) {
                this.className = className;
            }

            public boolean isSupported() {
                return getManagerClass() != null;
            }

            public String getCanonicalName() {
                return "edu.vandy.simulator.managers.palantiri." + className;
            }

            private Class<?> getManagerClass() {
                try {
                    // Create a new JavaClassLoader
                    Class<?> clazz = getClass();
                    // Load the target class using its binary name
                    return clazz.getClassLoader().loadClass(getCanonicalName());
                } catch (Exception e) {
                    return null;
                }
            }

            public PalantiriManager newInstance() {
                try {
                    // Load the target class using its binary name
                    Class clazz = getManagerClass();
                    if (clazz == null) {
                        throw new IllegalStateException(
                                "newInstance should only be called for a supported PalantiriManager type");
                    }

                    System.out.println("Loaded class name: " + clazz.getName());

                    // Create a new instance from the loaded class
                    Constructor constructor = clazz.getConstructor();
                    Object crawler = constructor.newInstance();
                    return (PalantiriManager) crawler;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

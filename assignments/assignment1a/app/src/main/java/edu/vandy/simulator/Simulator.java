package edu.vandy.simulator;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import edu.vandy.simulator.managers.beings.Being;
import edu.vandy.simulator.managers.beings.BeingManager;
import edu.vandy.simulator.managers.palantiri.Palantir;
import edu.vandy.simulator.managers.palantiri.PalantiriManager;
import edu.vandy.simulator.model.implementation.components.BeingComponent;
import edu.vandy.simulator.model.implementation.snapshots.BeingSnapshot;
import edu.vandy.simulator.model.implementation.snapshots.ModelSnapshot;
import edu.vandy.simulator.model.implementation.components.PalantirComponent;
import edu.vandy.simulator.model.implementation.snapshots.PalantirSnapshot;
import edu.vandy.simulator.model.implementation.components.SimulatorModel;
import edu.vandy.simulator.model.implementation.snapshots.SimulatorSnapshot;
import edu.vandy.simulator.model.interfaces.ModelController;
import edu.vandy.simulator.model.interfaces.ModelObserver;

import static edu.vandy.simulator.model.implementation.components.SimulatorComponent.State.CANCELLED;
import static edu.vandy.simulator.model.implementation.components.SimulatorComponent.State.COMPLETED;
import static edu.vandy.simulator.model.implementation.components.SimulatorComponent.State.ERROR;
import static edu.vandy.simulator.model.implementation.components.SimulatorComponent.State.IDLE;
import static edu.vandy.simulator.model.implementation.components.SimulatorComponent.State.RUNNING;

/**
 * This class is the main controller for the simulation. It acts as a
 * mediator between the {@link BeingManager} and the {@link
 * PalantiriManager} implementations. Since all requests to acquire
 * and release Palantiri are routed through this class it is a
 * convenient place to check the running simulation for
 * inconsistencies which frees the Being manager to focus more on the
 * implementation details of each specific strategy and less on
 * whether that strategy is implemented correctly.
 * <p>
 * The Simulator is also responsible for managing its own model state
 * and calls the model layers setState method to trigger a snapshot
 * that will be sent to the presentation layer.  The BeingManager and
 * PalantiriManager base classes and all concrete class
 * implementations have no knowledge of state and can therefore only
 * need to include the implementation details of the specific
 * concurrency strategy that they are using/modelling.
 * <p>
 * This class also acts as a sink for all component {@link #error} and
 * {@link #warn} method invocations and is the initiator (starting
 * point) of all {@link #shutdown} and {@link #reset} requests that
 * get forwarded to all model components.
 * <p>
 * To avoid using synchronization statements, the following rule is
 * always adhered to: A being's state determines what being attributes
 * are validated for consistency in the presentation layer. This allows
 * for situations where moving to a state requires also assigning values
 * to being or palantir fields. As long as the being is in the IDLE
 * state, the model checker in the presentation layer will not look at
 * the being's fields. But when the being moves to the BUSY state, the
 * model checker will check that the being has been assigned a valid
 * palantir and that this palantir is not being used by any other being
 * (that is not IDLE). This simple trick prevents non-atomic state
 * changes being detected as errors (when a snapshot is triggered
 * between the field and state changes operations).
 */

public class Simulator
        extends SimulatorModel
        implements ModelController {
    /**
     * Logging tag.
     */
    private static final String TAG = "Simulator";
    /**
     * Keeps track of the number of Beings that currently have
     * a Palantir and stops the simulation if any inconsistency
     * is detected.
     */
    private static AtomicInteger mGazingThreads =
            new AtomicInteger(0);
    /**
     * Flag indicating if multiple shutdown and reset calls should throw
     * exceptions or just issue a warning message.
     */
    private Boolean STRICT_MODE = true;
    /**
     * The BeingManager that manages the creation and running
     * state of all Being components.
     */
    private BeingManager mBeingManager;
    /**
     * The PalantiriManager that manages the acquisition and
     * release of all Palantiri resources.
     */
    private PalantiriManager mPalantiriManager;
    /**
     * Tracks whether a simulator is currently running.
     */
    private boolean mRunning = false;
    /**
     * Flag set when a shutdown has been requested.
     * This flag will be cleared once the shutdown had completed.
     */
    private volatile boolean mShutdown = false;
    /**
     * Flag set when a reset has been requested.
     * This flag will be cleared once the reset had completed.
     */
    private volatile boolean mReset = false;

    /**
     * Constructor which accepts an optional {@link ModelObserver}
     * that will be sent model snapshots whenever this
     * model or any of its components change state.
     *
     * @param observer Optional {@link ModelObserver}.
     */
    public Simulator(ModelObserver observer) {
        // This is the top level model component and therefore it does
        // not need to set the superclass ModelProvider for sending
        // state changes up the chain.
        super(Type.SIMULATOR, IDLE, null);

        if (observer != null) {
            addObserver(observer, false);
        }
    }

    /**
     * Builds a new simulation model with the specified model parameters.
     *
     * @param beingCount       The number of Beings.
     * @param palantirCount    The number of Palantiri.
     * @param gazingIterations The number of Being gazing iterations.
     */
    public void buildModel(BeingManager.Factory.Type beingManagerType,
                           PalantiriManager.Factory.Type palantiriManagerType,
                           int beingCount,
                           int palantirCount,
                           int gazingIterations) {
        // A new model can only be built when there is no running simulation.
        if (isRunning()) {
            error("Unable to build model because a simulation is " +
                    "currently in progress; try calling " +
                    "stop first");
            return;
        }

        // No point wasting time redoing things that already are in the right state.
        if (mBeingManager == null
                || mBeingManager.getClass() != beingManagerType.clazz
                || mBeingManager.getBeingCount() != beingCount
                || mBeingManager.getGazingIterations() != gazingIterations) {

            // Construct an instance of the specified BeingManger type.
            mBeingManager =
                    BeingManager.Factory.newManager(beingManagerType,
                            beingCount,
                            gazingIterations,
                            this);
        }

        if (mBeingManager == null) {
            error("Unable to create Being Manager.");
            return;
        }

        // No point wasting time redoing things that already are in
        // the right state.
        if (mPalantiriManager == null
                || mPalantiriManager.getClass() != palantiriManagerType.clazz
                || mPalantiriManager.getPalantirCount() != palantirCount) {

            // Construct an instance of the specified PalantiriManger type.
            mPalantiriManager =
                    PalantiriManager.Factory.newManager(palantiriManagerType,
                            palantirCount,
                            this);
        }

        if (mPalantiriManager == null) {
            error("Unable to create Palantiri Manager.");
            return;
        }

        // Update current state to IDLE (ready to run) so that the
        // presentation layer will immediately display the idle beings
        // and palantiri.
        setState(IDLE);
    }

    /**
     * Ensure that the simultation is in a valid start state.
     */
    private void validateStartState() {
        if (mShutdown) {
            throw new IllegalStateException("mShutdown still set from a previous shutdown.");
        }
        if (isRunning()) {
            throw new IllegalStateException("start can't be called " +
                    "when a simulation is already running.");
        }

        if (mGazingThreads.get() != 0) {
            throw new IllegalStateException("Number of gazing threads should be 0");
        }

        mBeingManager.validateStartState();
    }

    /**
     * This method is called when the user asks to start the
     * simulation in the context of the main UI Thread.  It performs
     * sanity-checking and bookkeeping operations, as well as runs the
     * BeingManager's simulation.
     **/
    @Override
    public void start() {
        // Ensure we're in the appropriate start state.
        validateStartState();

        // ParallelStreams is the only being manager that will not
        // immediately return. So we can't assume that the simulation
        // has completed when the call to runSimulation() returns.
        try {
            // Set running status and update state.
            setRunning(true);

            // Call abstract method to run the simulation.  This is
            // required to be a blocking call and should only return
            // once the simulation has completed or was cancelled. In
            // either case, no beings should be running once this
            // method returns.
            mBeingManager.runSimulation();

            // Notify presentation layer that the simulation completed.
            setState(COMPLETED);
        } catch (CancellationException e) {
            // Cancelled exception has been thrown by one of the Being
            // manager implementations. Force the simulation to
            // cleanly shutdown if it hasn't already been done.
            if (!mShutdown) {
                shutdown();
            }
        } catch (Exception e) {
            // Notify presentation layer that the simulation
            // encountered an error.
            setState(ERROR, e);
        } finally {
            // Clear running status and update state.
            setRunning(false);

            // Call helper method to report any errors if this
            // simulation model has not been properly shutdown.
            validateShutdownComplete();

            // Always call reset in preparation for the next run. This
            // call will reset both managers whose will, in turn,
            // reset all their managed components (beings and
            // palantiri).
            reset();
        }
    }

    /**
     * Called by BeingManager to request access to the next free
     * Palantiri. This request is simply routed to the current
     * PalantiriManger instance.
     *
     * @param being The Being instance.
     * @return The next available Palantir.
     */
    public Palantir acquirePalantir(Being being) {
        // First set this being's state WAITING since we know
        // that it's about to wait for the next available palantir.
        being.setState(BeingComponent.State.WAITING);

        // Route the palantir request to the palantiri manager.
        Palantir palantir = mPalantiriManager.acquirePalantir(being.getId());

        if (palantir != null) {
            // Keep track of all acquired palantiri and make sure that the
            // current being and palantir manager implementations have no
            // implementation/concurrency errors. This call will
            // automatically shutdown the simulator if it detects an model
            // error and will also throw an exception.
            incrementGazingCountAndCheck(being.getId(), palantir);

            // This Being now "owns" the Palantir.
            being.setPalantirId(palantir.getId());
            // The Palantir "belongs" to this Being.
            palantir.setBeingId(being.getId());
        } else {
            being.setState(BeingComponent.State.ERROR,
                    "Unable to acquire a palantir.");
            error("Simulator#acquirePalantir was unable to acquire a palantir!");
        }

        return palantir;
    }

    /**
     * Performs a being request to gaze at the previously
     * acquired palantir for a random interval of time.
     *
     * @param being    The being thread that is requesting to gaze.
     * @param palantir The palantir previously acquired by a call
     *                 to {@link #acquirePalantir}.
     */
    public void gazeIntoPalantir(Being being, Palantir palantir) {
        if (being.getPalantirId() != palantir.getId()) {
            being.setState(BeingComponent.State.ERROR,
                    "Being attempting to gaze into wrong palantir");
        } else {
            // Set the being state to gazing which will immediately
            // trigger the creation of a model snapshot. This call
            // will not return until the gazing delay has completed.
            being.setState(BeingComponent.State.BUSY);
        }
    }

    /**
     * Called be BeingManager to release a previously acquired
     * Palantir resource. This request is simply routed to the current
     * PalantiriManger instance.
     *
     * @param being    The Being that is releasing the Palantir.
     * @param palantir The Palantir that is being released.
     */
    public void releasePalantir(Being being, Palantir palantir) {
        if (palantir != null) {
            // Set being state to IDLE before clearing it's palantirId
            // field and before calling the palantir manager to release
            // the palantir to avoid placing the model in an inconsistent
            // state due to thread scheduling issues. Note that this
            // ordering is critical.
            being.setState(BeingComponent.State.IDLE);
            being.setPalantirId(-1);
            palantir.setBeingId(-1);

            // Decrement the gazing count before releasing the palantir.
            decrementGazingCount();

            mPalantiriManager.releasePalantir(being.getId(), palantir);
        } else {
            error("Simulator#releasePalantir called with a null palantir!");
        }
    }

    /**
     * Stop the currently running simulation. Note that the shutdown
     * logic requires the call to {@link #shutdown} to block until all
     * threads have been cleanly terminated.
     */
    @Override
    public void stop() {
        Log.i(TAG, "stop: called.");
        if (isRunning()) {
            shutdown();
            setState(CANCELLED);
        }
        Log.i(TAG, "stop: completed.");
    }

    /**
     * This method is called once the shutdown process has completed
     * to ensure that the simulator is in a proper shutdown state and
     * can be reused to run a new simulation. Note that the {@link
     * #warn} helper method is called to log any shutdown validation
     * errors because the {@link #error} method also calls {@link
     * #shutdown} which, in turn, will call this methods (causing
     * infinite recursion).
     */
    private void validateShutdownComplete() {
        // Make sure the BeingManager implementation has terminated
        // all being threads and if not throw an exception so that the
        // problem is fixed immediately.
        if (mGazingThreads.get() > 0) {
            warn("Shutdown Error: Number of gazing threads should be 0.");
        } else {
            int count = mBeingManager.getRunningBeingCount();
            if (count > 0) {
                warn("BeingManager implementation failed to shutdown "
                        + count + " of its "
                        + mBeingManager.getBeingCount()
                        + " being threads.");
            } else {
                warn("Shutdown verified to be successful");
            }
        }
    }

    /**
     * Resets the simulator's field to their initial values and then
     * calls both managers to reset themselves and their owned
     * components.
     */
    @Override
    public void reset() {
        synchronized (this) {
            if (mReset) {
                String msg = "Reset already in progress - fix this!";
                Thread.dumpStack();
                if (STRICT_MODE) {
                    throw new IllegalStateException(msg);
                } else {
                    Log.w(TAG, msg);
                }
            }

            mReset = true;
        }

        try {
            mReset = true;
            mShutdown = false;
            mRunning = false;
            mGazingThreads.set(0);

            // For a more reliable and predictable shutdown outcome
            // first shutdown the being manager so that no being
            // threads makes calls to the Palantiri manager when it is
            // shutting down (below).
            if (mBeingManager != null) {
                mBeingManager.reset();
            }

            // Now that the beings have been shutdown,
            // shutdown the Palantiri manager.
            if (mPalantiriManager != null) {
                mPalantiriManager.reset();
            }
        } catch (Exception e) {
            Log.e(TAG, "Reset must never throw an exception - fix this!");
        } finally {
            mReset = false;
        }
    }

    /**
     * Returns true if the simulation is currently running, else false.
     */
    public boolean isRunning() {
        return mRunning;
    }

    /**
     * Sets flag that indicates if the simulation is running or has
     * halted. The model state is also updated to reflect whether the
     * simulation is running or is halted.
     */
    public void setRunning(boolean running) {
        mRunning = running;
        setState(mRunning ? RUNNING : IDLE);
    }

    /**
     * @return {@code true} if the simulator is being shutdown.
     */
    public boolean isShutdown() {
        return mShutdown;
    }

    /**
     * Sets the static controller flag to so that all threads stop and
     * then stops the being manager. This should only be called once
     * per simulation. A warning is issued if multiple calls are
     * detected so that they can be located and removed.
     */
    @Override
    public void shutdown() {
        Log.i(TAG, "shutdown: entered.");

        try {
            synchronized (this) {
                if (mShutdown) {
                    // Shutdown has already been handled.
                    String msg = "shutdown(): Shutdown already progress; request ignored.";
                    if (STRICT_MODE) {
                        throw new IllegalStateException(msg);
                    } else {
                        warn(msg);
                        Thread.dumpStack();
                    }
                    return;
                }

                // Only set shutdown flag if the simulation is
                // actually running.
                if (isRunning()) {
                    mShutdown = true;
                }
            }

            if (isRunning()) {
                if (!mShutdown) {
                    throw new IllegalStateException("mShutdown should be true!");
                }

                if (mBeingManager != null) {
                    mBeingManager.shutdown();
                }

                if (mPalantiriManager != null) {
                    mPalantiriManager.shutdown();
                }
            }
        } catch (Exception e) {
            warn("shutdown(): Encountered an exception!: " + e);
            e.printStackTrace();
        } finally {
            validateShutdownComplete();

            Log.i(TAG, "shutdown: exited.");
        }
    }

    /**
     * Handle all warning messages. Currently just reports the error
     * to the system logger but could be used to forward the message
     * to the presentation layer for display as a a toast or any other
     * visual representation.
     *
     * @param message A non-fatal warning message.
     */
    public void warn(String message) {
        Log.w("Simulator", "Implementation warning: " + message);
    }

    /**
     * Handle all component error calls. When any model component
     * encounters a non-recoverable error, it should call one of it's
     * base class overloaded {@link #error} methods which will route
     * all calls up to this simulator instance.
     * <p>
     * The error is recorded and then reflected back down to all
     * components as a shutdown request via the {@link #shutdown}
     * method. Since any call to error signals an unrecoverable error,
     * this method will throw an IllegalStateException in the calling
     * thread so that callers don't need to worry about handling error
     * return codes or dealing with an inconsistent model state.
     *
     * @param msg A message describing the error.
     */
    public void error(@NotNull String msg) {
        // Set model error state which will trigger a snapshot that
        // will be sent to the presentation layer.
        setState(ERROR, msg);

        // Call shutdown helper that will propagate the shutdown
        // command to all model components.
        shutdown();

        // Now throw an exception in the calling thread so that the
        // caller does not execute any more code in this inconsistent
        // model state. This will also provide a stack trace for
        // debugging the problem.
        throw new IllegalStateException(msg);
    }

    /**
     * Called by managers when they have encountered an unrecoverable
     * error. Any error event will initiate a shutdown sequence.
     *
     * @param throwable A Throwable cause of the error.
     */
    public void error(@NotNull Throwable throwable) {
        if (throwable instanceof CancellationException) {
            // Cancellation exceptions are only thrown if
            // a shutdown is in progress.
            if (!mShutdown) {
                throw new IllegalStateException(
                        "Framework design error: this " +
                                "should not happen so fix it.");
            }
            throw new CancellationException();
        } else {
            setState(ERROR, throwable);
            // Call shutdown helper that will propagate the shutdown
            // command to all model components.
            shutdown();

            // Now throw an exception in the calling thread so that
            // the caller does not execute any more code in this
            // inconsistent model state. This will also provide a
            // stack trace for debugging the problem.
            throw new IllegalStateException(throwable);
        }
    }

    /**
     * @return The list of Beings in this simulation.
     */
    public List<Being> getBeings() {
        //noinspection unchecked
        return mBeingManager.getBeings();
    }

    /**
     * @return The list of Palantiri in this simulation.
     */
    public List<Palantir> getPalantiri() {
        return mPalantiriManager.getPalantiri();
    }

    /**
     * Builds an immutable component snapshot that describes the
     * current state of this component suitable for pushing to the
     * presentation layer for rendering.
     */
    @Override
    public ModelSnapshot buildModelSnapshot(long triggeredById) {
        // Build a list of being component snapshots.
        List<BeingSnapshot> beingSnapshots =
                getBeings()
                        .stream()
                        .map(BeingComponent::buildSnapshot)
                        .collect(Collectors.toList());

        // Build a list of palantir component snapshots.
        List<PalantirSnapshot> palantirSnapshots =
                getPalantiri()
                        .stream()
                        .map(PalantirComponent::buildSnapshot)
                        .collect(Collectors.toList());

        // Package all three snapshots into a single model snapshot
        // wrapper.
        return new ModelSnapshot(new SimulatorSnapshot(this),
                beingSnapshots,
                palantirSnapshots,
                triggeredById);
    }

    /**
     * This method is used to monitor the lifecycle of being threads
     * and will detect when a concurrency implementation has a logic
     * error relating to thread allocation.
     * <p>
     * This method is called each time a BeingManager calls
     * acquirePalantir() so it is called concurrently from different
     * threads. This method increments the number of threads gazing
     * and checks that the number of threads gazing does not exceed
     * the number of Palantiri in the simulation using an AtomicLong
     * object instantiated above (mGazingThreads).  If the number of
     * gazing threads exceeds the number of Palantiri, then the being
     * manager is told to immediately error (and cleanup resources)
     * and false is returned to end the simulation.
     *
     * @param beingId  The Id of the current Being.
     * @param palantir The Palantir that was just acquired.
     */
    private void incrementGazingCountAndCheck(long beingId,
                                              Palantir palantir) {
        // First make sure that the simulation has not been error.
        if (!isShutdown()) {
            final int numberOfGazingThreads =
                    mGazingThreads.incrementAndGet();

            if (numberOfGazingThreads > mBeingManager.getBeingCount()) {
                String msg =
                        "ERROR, Being "
                                + beingId
                                + " shouldn't have acquired Palantir "
                                + palantir.getId();
                // Let the error handler shutdown the simulator.
                error(msg);
            }
        }
    }

    /**
     * This method is called each time a Being is about to release a
     * Palantir.  It should simply decrement the number of gazing
     * threads in mGazingThreads.
     */
    private void decrementGazingCount() {
        mGazingThreads.decrementAndGet();
    }
}

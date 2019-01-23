package edu.vandy.simulator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.vandy.simulator.managers.beings.Being;
import edu.vandy.simulator.managers.beings.BeingManager;
import edu.vandy.simulator.managers.palantiri.Palantir;
import edu.vandy.simulator.managers.palantiri.PalantiriManager;
import edu.vandy.simulator.model.base.BaseSnapshot;
import edu.vandy.simulator.model.implementation.components.BeingComponent;
import edu.vandy.simulator.model.implementation.components.PalantirComponent;
import edu.vandy.simulator.model.implementation.components.SimulatorModel;
import edu.vandy.simulator.model.implementation.snapshots.BeingSnapshot;
import edu.vandy.simulator.model.implementation.snapshots.ModelSnapshot;
import edu.vandy.simulator.model.implementation.snapshots.PalantirSnapshot;
import edu.vandy.simulator.model.implementation.snapshots.SimulatorSnapshot;
import edu.vandy.simulator.model.interfaces.ModelComponent;
import edu.vandy.simulator.model.interfaces.ModelController;
import edu.vandy.simulator.model.interfaces.ModelObserver;

import static edu.vandy.simulator.model.implementation.components.SimulatorComponent.State.CANCELLED;
import static edu.vandy.simulator.model.implementation.components.SimulatorComponent.State.CANCELLING;
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
     * Builds an immutable full model snapshot that describes the
     * a newly created model and all of it's components. This snapshot
     * is then pushed to the presentation layer so that it can perform
     * the initial rendering of a new model.
     */
    private ModelSnapshot mModelSnapshot = new ModelSnapshot();

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
     * Tracks whether a simulator is currently running, i.e,
     * whether the thread that called startSimulation has not
     * yet returned from that call. This field declared as
     * an AtomicBoolean and should be taken as the ground
     * truth about whether a simulation is running. The
     * component state field is not as reliable since there
     * are 3 possible end states (COMPLETED, CANCELLED,
     * and ERROR) and also, state is "posted" to the
     * application's main thread which suffers from delays
     * and may even suffer from discarded posted events.
     */
    private AtomicBoolean mRunning = new AtomicBoolean(false);

    /**
     * Atomic boolean flag that is set when a shutdown has been
     * requested and is cleared when the shutdown sequence has
     * completed normally or with an error.
     */
    private AtomicBoolean mShutdown = new AtomicBoolean(false);

    /**
     * Keeps track of the thread calls startSimulation so that
     * it can be interrupted from the shutdown method.
     */
    private Thread mRunnerThread = null;

    /**
     * Saved model parameters used for model checking.
     */
    private int mBeingCount;
    private int mPalantirCount;
    private int mGazingIterations;

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
        require(!isRunning(),
                "Unable to build model because a simulation is " +
                        "currently in progress; try calling " +
                        "stop first");

        // Save input parameters for later model checking.
        mBeingCount = beingCount;
        mPalantirCount = palantirCount;
        mGazingIterations = gazingIterations;

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

        require(mBeingManager != null, "Unable to create Being Manager.");

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

        require(mPalantiriManager != null,
                "Unable to create Palantiri Manager.");

        // Build and cache the new model snapshot and set the
        // current state to IDLE. This will also trigger a
        // snapshot update followed by a broadcast to all snapshot
        // observers.
        mModelSnapshot = buildModelSnapshot(this);

        // Explicitly push the initial snapshot to the presentation layer.
        broadcastSnapshot(mModelSnapshot);
    }

    /**
     * This method is called when the user asks to start the
     * simulation in the context of the main UI Thread.  It performs
     * sanity-checking and bookkeeping operations, as well as runs the
     * BeingManager's simulation.
     **/
    @Override
    public void start() {
        if (mRunning.getAndSet(true)) {
            warn("start: simulation is already running (start aborted).");
            return;
        }

        // Don't allow starting while a shutdown is in progress.
        if (isShutdown()) {
            warn("start: a simulation is currently being " +
                    "shutdown (start aborted).");
            return;
        }

        // ParallelStreams is the only being manager that will not
        // immediately return. So we can't assume that the simulation
        // has completed when the call to runSimulation() returns.
        try {
            // Ensure we're in the appropriate start state.
            validateStartState();

            // Set running status and update state.
            setState(RUNNING);

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
            // cleanly shutdown if that is not currently being done.
            if (!isShutdown()) {
                shutdown();
            }
        } catch (Exception e) {
            // Notify presentation layer that the simulation
            // encountered an error.
            setState(ERROR, e);
        } finally {
            // Call helper method to report any errors if this
            // simulation model is not in the expected end state.
            try {
                validateEndState();
            } finally {
                // Always call reset in preparation for the next run. This
                // call will reset both managers whose will, in turn,
                // reset all their managed components (beings and palantiri).
                reset();

                // Clear running flag.
                mRunning.set(false);
            }
        }
    }

    /**
     * Ensure that the simulation is in a valid start state.
     * Note that this method must be called after the mRunning flag
     * has been set to ensure that another thread is unable to enter
     * the start() method while this validation is being performed.
     */
    private void validateStartState() {
        require(isRunning(),
                "validateStartState: mRunning flag should be set.");
        require(!isShutdown(),
                "validateStartState: mShutdown flag should be cleared.");
        require(mGazingThreads.get() == 0,
                "validateStartState: Number of gazing threads should be 0");

        mBeingManager.validateStartState();
    }

    /**
     * Verifies that all shutdown state invariants hold or
     * throws and IllegalStateException at the first encountered
     * exception.
     */
    private void validateShutdownState() {
        require(isShutdown(),
                "validateEndState: mRunning flag should be set.");
        require(!isRunning(),
                "validateEndState: mRunning flag should be set.");
        require(getState() == IDLE ||
                        getState() == COMPLETED ||
                        getState() == CANCELLED ||
                        getState() == ERROR,
                "Shutdown error: simulator state [%1$s] " +
                        "is not a valid shutdown state",
                getState());
    }

    /**
     * This method is called when the simulation has completed
     * either normally, abnormally, or because it was cancelled.
     * If the simulation was not cancelled, then a series of
     * validations are performed on the Beings and Palantiri to
     * detect and report possible simulation implementation errors.
     * Note that this method must be called before the mRunning flag
     * is cleared to ensure that another thread is unable to enter
     * the start() method while this validation is being performed.
     */
    private void validateEndState() {
        require(isRunning(),
                "validateEndState: mRunning flag should be set.");

        if (isShutdown() || getState() == CANCELLED) {
            // If shutting down or already shutdown (cancelled)
            // do not perform any model checking.
            warn("Simulation was shutdown or was cancelled " +
                    "so no model checking will be performed");
            return;
        }

        require(mGazingThreads.get() == 0,
                "validateEndState: Number of gazing threads should be 0");

        // Check that all Beings were created.
        int actualBeingCount = mBeingManager.getBeings().size();
        require(actualBeingCount == mBeingCount,
                "Only [%1$d/%2$d] beings were created.",
                actualBeingCount, mBeingCount);

        // Check that all Palantiri were created.
        int actualPalantirCount = mPalantiriManager.getPalantiri().size();
        require(actualPalantirCount == mPalantirCount,
                "Only [%1$d/%2$d] palantiri were created.",
                actualPalantirCount,
                mPalantirCount);

        // Check if any Beings are still running.
        int runningBeings = mBeingManager.getRunningBeingCount();
        require(runningBeings == 0,
                "BeingManager implementation failed " +
                        "to shutdown [%1$d/%2$d] beings",
                runningBeings,
                mBeingCount);

        // Build a list of any Beings that didn't complete all
        // their gazing iterations.
        List<Being> errorBeings = getBeings().stream()
                .filter(being -> being.getCompleted() != mGazingIterations)
                .collect(Collectors.toList());

        // Check if any Beings did not complete their gazing iterations.
        require(errorBeings.size() == 0,
                errorBeings.stream()
                        .map(being ->
                                String.format(
                                        Locale.getDefault(),
                                        "Being[%1$d] only completed " +
                                                "[%2$d/%3$d] iterations.",
                                        being.getId(),
                                        being.getCompleted(),
                                        mGazingIterations))
                        .reduce((result, string) -> result + "\n" + string)
                        .orElse("Impossible!"));

        // Get the actual number of performed gazing iterations.
        int actualBeingGazingIterations =
                getBeings().stream()
                        .mapToInt(Being::getCompleted)
                        .sum();

        // Check that the actual number of performed gazing
        // iterations matches the expected total number of
        // gazing iterations.
        require(actualBeingGazingIterations ==
                        mGazingIterations * mBeingCount,
                "Simulation only completed [%1$d/%2$d] gazing iterations.",
                actualBeingGazingIterations,
                mGazingIterations * mBeingCount
        );

        // At this point we know the model has the correct
        // number of beings and palantiri and that the being
        // gazing iterations were all performed correctly.
        // Now check those stats against that palantiri instances.

        int expectedTotal =
                mPalantiriManager.getPalantiri().stream()
                        .mapToInt(Palantir::getCount)
                        .sum();

        require(expectedTotal == mGazingIterations * mBeingCount,
                "Palantiri were only gazed into [%1$d/%$2d] times.");
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

            // Being's palantir id field should be cleared.
            if (being.getPalantirId() != -1L) {
                being.setState(BeingComponent.State.ERROR,
                        "Being is already gazing at another palantir.");
                error("Being is already gazing at another palantir.");
            }

            // Palantir's being id field should be cleared.
            if (palantir.getBeingId() != -1L) {
                palantir.setState(PalantirComponent.State.ERROR,
                        "Palantir is still owned by another being.");
                error("Palantir is still owned by another being.");
            }

            // Synchronize these 2 statements so that a snapshot
            // will never be captured when another being triggers
            // one between these two statements.
            synchronized (this) {
                // The Being now "owns" the Palantir.
                being.setPalantirId(palantir.getId());
                // The Palantir is now "owned" by the Being.
                palantir.setBeingId(being.getId());
            }

            // Note that we don't trigger a snapshot
            // until gazeAtPalantir is called.
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
        require(being.getPalantirId() == palantir.getId() &&
                        palantir.getBeingId() == being.getId(),
                "Gazing error: being[%d] and " +
                        "palantir[%d] ids do not match.",
                being.getId(),
                palantir.getId());

        // Set the being state to gazing which will immediately
        // trigger the creation of a model snapshot. This call
        // will not return until the gazing delay has completed.
        being.setState(BeingComponent.State.BUSY);
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
            require(being.getPalantirId() == palantir.getId() &&
                            palantir.getBeingId() == being.getId(),
                    "Gazing error: being[%d] and " +
                            "palantir[%d] ids do not match.",
                    being.getId(),
                    palantir.getId());

            // Clear linked ids and return to the IDLE state BEFORE
            // officially releasing the Palantir so that snapshots
            // capture a valid model state.
            synchronized (this) {
                being.setPalantirId(-1);
                palantir.setBeingId(-1);
                being.setState(BeingComponent.State.IDLE);
            }

            // Decrement the gazing count before releasing the palantir.
            decrementGazingCount();

            mPalantiriManager.releasePalantir(palantir);
        } else {
            being.setState(BeingComponent.State.ERROR,
                    "Being is trying to release a null palantir.");
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
        Controller.log("stop: called.");
        if (isRunning()) {
            setState(CANCELLING);
            shutdown();
            setState(CANCELLED);
        }
        Controller.log("stop: completed.");
    }

    /**
     * Resets the simulator's field to their initial values and then
     * calls both managers to reset themselves and their owned
     * components.
     */
    @Override
    public void reset() {
        try {
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
            error("Reset must never throw an exception - fix this!: " + e);
        }
    }

    /**
     * Returns true if the simulation is currently running, else false.
     */
    public boolean isRunning() {
        return mRunning.get();
    }

    /**
     * @return {@code true} if the simulator is being shutdown.
     */
    public boolean isShutdown() {
        return mShutdown.get();
    }

    /**
     * Sets the static controller flag to so that all threads stop and
     * then stops the being manager. This should only be called once
     * per simulation. A warning is issued if multiple calls are
     * detected so that they can be located and removed.
     */
    @Override
    public void shutdown() {
        Controller.log("shutdown: entered.");
        if (mShutdown.getAndSet(true)) {
            warn("shutdown: already shutting down (ignoring request).");
        }

        try {
            if (isRunning()) {
                if (mBeingManager != null) {
                    mBeingManager.shutdown();
                }

                if (mPalantiriManager != null) {
                    mPalantiriManager.shutdown();
                }
            }
            // Busy wait for simulation thread to end.
            while (isRunning()) {
                Thread.sleep(50);
            }
        } catch (Exception e) {
            warn("shutdown(): Encountered an exception!: " + e);
            e.printStackTrace();
        } finally {
            // Check for shutdown invariants.
            validateShutdownState();

            // Lastly, clear shutdown flag.
            mShutdown.set(false);
            Controller.log("shutdown: exited.");
        }
    }

    /**
     * Handle all warning messages. Currently just reports the error
     * to the system logger but could be used to forward the message
     * to the presentation layer for display as a a toast or any other
     * visual representation.
     *
     * @param msg A non-fatal warning message.
     */
    public void warn(String msg, Object... args) {
        // Format string if args are passed.
        msg = args.length == 0 ? msg : String.format(msg, args);
        System.out.println("WARNING: " + msg);
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
    public void error(@Nullable String msg, Object... args) {
        // Format string if args are passed.
        if (msg == null) {
            msg = "An unspecified error has occurred";
        } else {
            msg = args.length == 0 ? msg : String.format(msg, args);
        }

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
     * Helper method that tests a condition and if false, throws an
     * IllegalStateException using the passed error string.
     *
     * @param msg A message describing the error.
     */
    public void require(Boolean condition, @NotNull String msg, Object... args) {
        // Format string if args are passed.
        msg = args.length == 0 ? msg : String.format(msg, args);

        if (!condition) {
            throw new IllegalStateException(msg);
        }
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
            if (!isShutdown()) {
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

    @Override
    public ModelSnapshot buildModelSnapshot(ModelComponent component) {
        synchronized (this) {
            // Build a list of being component snapshots.
            Map<Long, BeingSnapshot> beingSnapshots =
                    getBeings().stream()
                            .filter(Objects::nonNull)
                            .map(BeingComponent::buildSnapshot)
                            .collect(Collectors.toMap(
                                    BaseSnapshot::getId,
                                    Function.identity()));

            if (beingSnapshots.size() != mBeingCount) {
                warn("Expected %d beings but found %d!",
                        mBeingCount, beingSnapshots.size());
            }

            // Build a list of palantir component snapshots.
            Map<Long, PalantirSnapshot> palantirSnapshots =
                    getPalantiri()
                            .stream()
                            .filter(Objects::nonNull)
                            .map(PalantirComponent::buildSnapshot)
                            .collect(Collectors.toMap(
                                    BaseSnapshot::getId,
                                    Function.identity()));

            if (palantirSnapshots.size() != mPalantirCount) {
                warn("Expected %d palantiri but found %d!",
                        mBeingCount, beingSnapshots.size());
            }

            // Package all three snapshots into a single model snapshot
            // wrapper.
            ModelSnapshot modelSnapshot =
                    new ModelSnapshot(
                            new SimulatorSnapshot(this),
                            beingSnapshots,
                            palantirSnapshots,
                            component);

            if (!modelSnapshot.getBeings().isEmpty()) {
                // Create an immutable copy of this cached model snapshot
                // before existing this synchronized block.
                modelSnapshot.getBeings().values().stream()
                        .filter(b -> b.getPalantirId() != -1)
                        .forEach(b -> {
                            PalantirSnapshot ps =
                                    modelSnapshot.getPalantiri().get(b.getPalantirId());
                            if (ps.getBeingId() != b.getId()) {
                                error("FRAMEWORK SYNCHRONIZATION ERROR!");
                            }
                        });
            }

            return modelSnapshot;
        }
    }

    /**
     * Updates a single component snapshot in the currently cached
     * model snapshot.
     *
     * @param component The component that is triggering this snapshot.
     */
    @Override
    public ModelSnapshot updateModelSnapshot(ModelComponent component) {
        if (true) {
            return buildModelSnapshot(component);
        }

        synchronized (this) {
            switch ((Simulator.Type) component.getType()) {
                case BEING:
                    mModelSnapshot.getBeings().put(component.getId(),
                            ((Being) component).buildSnapshot());
                    break;
                case PALANTIR:
                    mModelSnapshot.getPalantiri().put(component.getId(),
                            ((Palantir) component).buildSnapshot());
                    break;
                case SIMULATOR:
                    mModelSnapshot.setSimulator(buildSnapshot());
                    break;
                default:
                    throw new IllegalStateException("Invalid component type.");
            }

            // Update any palantir snapshots for
            // components that have been modified.
            getPalantiri()
                    .stream()
                    //.filter(BaseComponent::isModified)
                    .map(Palantir::buildSnapshot)
                    .forEach(snapshot ->
                            mModelSnapshot.getPalantiri().put(
                                    snapshot.getId(),
                                    snapshot));

            // Create an immutable copy of this cached model snapshot
            // before existing this synchronized block.
            ModelSnapshot modelSnapshot = new ModelSnapshot(mModelSnapshot);
            modelSnapshot.getBeings().values().stream()
                    .filter(b -> b.getPalantirId() != -1)
                    .forEach(b -> {
                        PalantirSnapshot ps =
                                modelSnapshot.getPalantiri().get(b.getPalantirId());
                        if (ps.getBeingId() != b.getId()) {
                            error("FRAMEWORK SYNCHRONIZATION ERROR!");
                        }
                    });
            return modelSnapshot;
        }
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

    /**
     * @return The current being manager instance.
     */
    public BeingManager getBeingManager() {
        return mBeingManager;
    }

    /**
     * @return The current palantir manager instance.
     */
    public PalantiriManager getPalantirManager() {
        return mPalantiriManager;
    }
}

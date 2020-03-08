package edu.vandy.simulator.model.implementation.components;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import edu.vandy.simulator.Controller;
import edu.vandy.simulator.managers.beings.Being;
import edu.vandy.simulator.model.base.BaseComponent;
import edu.vandy.simulator.model.implementation.snapshots.BeingSnapshot;
import edu.vandy.simulator.model.interfaces.ModelComponent;
import edu.vandy.simulator.model.interfaces.ModelProvider;

import static edu.vandy.simulator.model.implementation.components.BeingComponent.State.ACQUIRING;
import static edu.vandy.simulator.model.implementation.components.BeingComponent.State.CANCELLED;
import static edu.vandy.simulator.model.implementation.components.BeingComponent.State.GAZING;
import static edu.vandy.simulator.model.implementation.components.BeingComponent.State.IDLE;
import static edu.vandy.simulator.model.implementation.components.BeingComponent.State.RELEASING;
import static edu.vandy.simulator.model.implementation.components.BeingComponent.State.REMOVED;
import static edu.vandy.simulator.model.implementation.components.SimulatorModel.Type.BEING;

/**
 * A {@link ModelComponent} base class implementation extended
 * by the Being class. This base class manages all state data
 * and operations thereby hiding the complexity of state
 * management from the Being subclass. Any class that extends
 * this class need only implement the {@link BaseComponent#getId()}
 * method to provide a unique id. The simulator will periodically
 * make calls to {@link #setState} to update the Being state which
 * automatically triggers the creation a model snapshot that is
 * forwarded to the presentation layer.
 */
public abstract class BeingComponent
        extends BaseComponent<SimulatorModel.Type, BeingComponent.State> {
    /**
     * Logging tag.
     */
    @SuppressWarnings("unused")
    private static final String TAG = "BeingComponent";

    /**
     * Default BeingState delays used to synchronize
     * simulation with UI animations (fixed duration).
     */
    private static final int HOLDING_DURATION = 500;

    /**
     * The maximum ACQUIRING and RELEASING durations. They
     * application settings allows for these speeds to be
     * be scaled from 1/4 to 4 times the default duration
     * [250 .. 4000].
     */
    private static final int ACQUIRING_DURATION = 1000;
    private static final int RELEASING_DURATION = ACQUIRING_DURATION;

    /**
     * Time a being should pause after releasing and before
     * trying to acquire the next free palantir (fixed duration).
     */
    private static final int WAITING_DURATION = 0;

    /**
     * Value for all states that have no duration.
     */
    private static final int NO_DURATION = 0;

    /**
     * Thread-safe long value used to allocate unique 1 base
     * indexed being ids.
     */
    private static AtomicInteger sIdProvider = new AtomicInteger(100);
    /**
     * The duration to pause the being thread when it
     * enters a new state. This duration is different
     * for each state and is random for the GAZING state.
     * This field value is passed in the component snapshot
     * so that the presentation layer can synchronize
     * the visual display of being objects (animations).
     */
    public long mDuration;
    /**
     * volatile flag used to signal the current being
     * component to immediately terminate. This flag
     * will be set if a fatal error was encountered
     * or if the application (user) has cancelled a
     * running simulation.
     */
    private volatile boolean mCancelled = false;
    /**
     * The Palantir id being gazed into when the being is
     * in the GAZING state.
     */
    private long mPalantirId = -1;

    /**
     * Constructor for capturing a back reference to the
     * enclosing model class.
     *
     * @param modelProvider An ModelProvider implementation class
     *                      that provides access to the simulation
     *                      Model instance.
     */
    public BeingComponent(ModelProvider modelProvider) {
        // Set default state.
        // Call super class constructor to set this component type
        // and to initialize all base class field values.
        super(BEING, IDLE, sIdProvider.incrementAndGet(), modelProvider);
    }

    /**
     * Called to reset static id generator back to 0.
     * This should only be called if a new model is being
     * created an no previously allocated being components
     * are being used.
     */
    public static void resetIds() {
        sIdProvider.set(100);
    }

    /**
     * Pause the current thread for the given number of milliseconds
     * while also checking the isCancelled supplier periodically to
     * determine if the pause should be terminated prematurely.
     *
     * @param duration    The sleep time duration in milliseconds.
     * @param isCancelled a Supplier that returns true if the
     *                    pause operation should be cancelled.
     * @return The boolean value returned by isCancelled.
     */
    private static boolean pauseThread(long duration,
                                       Supplier<Boolean> isCancelled) {
        // Never pause threads if a shutdown has occurred.
        if (isCancelled.get()) {
            return false;
        }

        try {
            // Perform the sleep calls are performed in
            // 100 millisecond blocks so that this thread
            // can be response to a pending error request.
            // Don't reduce this interval or the animations
            // will end before this pause has completed.
            int SLEEP_INTERVAL = 100;
            long interval = Math.min(SLEEP_INTERVAL, duration);
            while (duration > 0
                    && !isCancelled.get()
                    && !Thread.interrupted()) {
                Thread.sleep(Math.min(interval, duration));
                duration -= interval;
            }
            return !isCancelled.get() && !Thread.interrupted();
        } catch (InterruptedException e) {
            // Prevent uncontrolled shutdowns by trapping
            // interrupted exceptions and mapping them to
            // a false return value.
            return false;
        } finally {
            // Uncomment if required for debugging.
            //if (isCancelled.get()) {
            //    Log.i(TAG, "pauseThread interrupted by isCancelled == TRUE.");
            //}
            //if (Thread.interrupted()) {
            //    Log.i(TAG, "pauseThread interrupted by Thread.interrupted() == TRUE.");
            //}
        }
    }

    /**
     * Pause the current thread for a random number of milliseconds
     * between the specified min and max values.
     *
     * @param min         Minimum millisecond pause time.
     * @param max         Maximum millisecond pause time.
     * @param isCancelled a Supplier that returns true if the
     *                    pause operation should be cancelled.
     * @return {@code true} if the pauseThread completed normally,
     * {@code false} if the pauseThread was interrupted or if a
     * error request has been issued.
     */
    @SuppressWarnings({"UnusedReturnValue", "SameParameterValue"})
    public static boolean pauseThread(int min, int max, Supplier<Boolean> isCancelled) {
        // Pause the thread for a random duration value between
        // the specified min and max range values and return
        // false if the simulator has been error by some
        // other thread during the pause.
        //TODOx: get rid of controller.
        return pauseThread(
                Controller.getRandomDelay(min, max),
                isCancelled);
    }

    /**
     * @return The id of the palantir being gazed into when the
     * being is in the ACQUIRING, GAZING, and RELEASING states.
     */
    public long getPalantirId() {
        return mPalantirId;
    }

    /**
     * Called by {@link edu.vandy.simulator.Simulator#gazeIntoPalantir}
     * to set the palantir id which will be included in model snapshots
     * forwarded to the presentation layer.
     *
     * @param palantirId The id of the palantir being gazed into
     *                   or -1 when gazing has completed.
     */
    public void setPalantirId(long palantirId) {
        mPalantirId = palantirId;
    }

    /**
     * Override base component getState to force CANCELLING
     * state if shutdown now has been called and has set the
     * cancelled flag.
     *
     * @return The current component state.
     */
    @Override
    public State getState() {
        return isCancelled() ? CANCELLED : super.getState();
    }

    /**
     * This method resets a being to it's original
     * created state. It should be called after
     * running a being in a simulation, and/or before
     * re-running a being in a new simulation run.
     */
    public void reset() {
        mCancelled = false;
        mPalantirId = -1;

        // Don't reset the state unless absolutely
        // necessary because this will trigger a
        // snapshot.
        if (getState() != IDLE) {
            setState(IDLE);
        }
    }

    /**
     * Handles model dependant state changes. If the controller
     * has shutdown, the state change is still made (to preserve
     * model consistency), but a snapshot will not be triggered.
     *
     * @param state   BeingState to move to.
     * @param e       The component's encountered exception.
     * @param message A message relating to the state.
     */
    @Override
    public void setState(State state,
                         @Nullable Throwable e,
                         @Nullable String message) {
        // Ensure that if the removed state has been set, that
        // no attempt is ever made to change this state.
        if (isRemoved() && state != REMOVED) {
            throw new IllegalStateException(
                    "Component has been REMOVED and " +
                            "cannot be set to any new state");
        }

        // To hide the implementation details from Being implementations,
        // the BUSY state is broken up into 4 states, each with different
        // durations: ACQUIRING, GAZING, RELEASING, and then IDLE. This
        // allows the presentation to support nice find grained visual
        // effects for the BUSY operation.
        switch (state) {
            case BUSY: {
                setState(ACQUIRING);
                setState(GAZING);
                setState(RELEASING);
                return;
            }
            case REMOVED: {
                // Set removed flag and call base class to immediately
                // trigger a snapshot. Once in a component has moved
                // to the REMOVED state, it can never be moved to
                // any other state.
                setRemoved(true);
                super.setState(state, e, message);
                return;
            }

            default: // Normal state processing.
        }

        // All model specific state values must be set before
        // calling setState which will create a snapshot of
        // all field values.
        mDuration = state.duration();

        // Don't change state in the presentation layer
        // while a being is being cancelled.
        if (!isCancelled()) {
            // Set state triggers snapshot generate/push mechanism.
            super.setState(state, e, message);

            // TODOx: remove log stuff from this if/else and leave
            // the single log before setState call.
            if (mDuration > 0) {
                // Pause this being for the state's required duration
                // before returning control to the calling Being.
                // Only set the state if pauseThread returns true
                // which means that it wasn't prematurely cancelled
                // by a shutdown request.
                if (pauseThread(mDuration, this::isCancelled)) {
                    Controller.log("setState[OUT]: " + this);
                }
            }
        }
    }

    /**
     * @return {code @true} if being has been requested to cancel
     * {@code false if not}.
     */
    public boolean isCancelled() {
        return mCancelled || Thread.interrupted();
    }

    /**
     * Called to perform a forceful the being to immediately shutdown.
     * This default implementation simply sets a static shutdown
     * flag which the Being implementations should be checking
     * regularly to ensure a responsive shutdown.
     */
    public void shutdownNow() {
        if (mCancelled) {
            Controller.log("Implementation Error: shutdownNow of " +
                    "BeingComponent should only be called once.");
        }

        mCancelled = true;

        Controller.log("ShutdownNow: " + this);
    }

    /**
     * Application specific attribute accessor.
     *
     * @return The expected duration of the Being's current state.
     */
    public long getDuration() {
        return mDuration;
    }

    /**
     * Builds an immutable component snapshot that describes
     * the current state of this component suitable for pushing
     * to the presentation layer for rendering.
     */
    @Override
    public BeingSnapshot buildSnapshot() {
        synchronized (this) {
            return new BeingSnapshot((Being) this);
        }
    }

    @Override
    public String toString() {
        return getType() +
                "[" + getId() + "]" +
                " " + getState() +
                " palantirId=" + getPalantirId() +
                (getException() != null ? " e=" + getException() : "") +
                (getMessage() != null ? " msg='" + getMessage() + "'" : "");
    }

    /**
     * The list of states that this Being component supports.
     */
    public enum State {
        HOLDING,    // initial state (waiting for setup to complete)
        IDLE,       // between states (RELEASING -> IDLE -> WAITING)
        BUSY,       // triggers 4 states: ACQUIRING -> GAZING -> RELEASING -> IDLE
        WAITING,    // waiting to acquire a Palantir
        ACQUIRING,  // in the process of acquiring a reserved Palantir
        GAZING,     // busy gazing into a Palantir
        RELEASING,  // in the process of releasing a Palantir
        CANCELLED,  // in the process of being cancelled
        DONE,       // final state (cancelled or finished all gazing iterations)
        REMOVED,    // this component has been removed from the model
        ERROR;      // model has detected a fatal error with this component

        /**
         * @return The millisecond duration of the each state.
         */
        public long duration() {
            long duration;
            switch (this) {
                case HOLDING:
                    duration = HOLDING_DURATION;
                    break;
                case RELEASING:
                    duration = RELEASING_DURATION;
                    break;
                case ACQUIRING:
                    duration = ACQUIRING_DURATION;
                    break;
                case WAITING:
                    duration = WAITING_DURATION;
                    break;
                case GAZING:
                    duration = Controller.getRandomDelay();
                    break;
                default:
                    duration = NO_DURATION;
            }

            // Tune the state's duration if it hasn't been explicitly
            // defined to have a fixed duration. The speed range is
            // [0..1] and at .5 it should produce the default animation
            // speed, at 0 it should be 2 x slower, and at 1 it should
            // be 2 x faster.

            if (fixed()) {
                return duration;
            } else {
                if (Controller.getSimulationSpeed() == 0) {
                    return 0;
                } else {
                    float speed = 1 - Controller.getSimulationSpeed();
                    float delay = duration;
                    float factor;
                    if (speed < 0.5f) {
                        // Slow down to at most 1/4 default speed.
                        factor = 2f * speed; // map [0 to .5] to [0 to 1]
                        delay = (.25f + (factor * .75f)) * delay;
                    } else {
                        // Speed up by at most 4 times default speed.
                        factor = 2f * (speed - 0.5f); // map [.5 to 1] to [0 to 1]
                        delay = (1 + (factor * 3f)) * delay;
                    }
                    return (long) delay;
                }
            }
        }

        /**
         * @return {@code true} if a state has a fixed duration
         * that should not be scaled by simulation speed
         * scaling factor (settings "animation speed").
         */
        public boolean fixed() {
            switch (this) {
                case ACQUIRING:
                case RELEASING: {
                    // Scalable - app tunable.
                    return false;
                }

                default:
                    // Fixed - not app tunable.
                    return true;
            }
        }
    }
}

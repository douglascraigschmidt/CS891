package edu.vandy.simulator.model.implementation.components;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

import edu.vandy.simulator.managers.palantiri.Palantir;
import edu.vandy.simulator.model.base.BaseComponent;
import edu.vandy.simulator.model.implementation.snapshots.PalantirSnapshot;
import edu.vandy.simulator.model.interfaces.ModelProvider;

import static edu.vandy.simulator.model.implementation.components.PalantirComponent.State.AVAILABLE;
import static edu.vandy.simulator.model.implementation.components.SimulatorModel.Type.PALANTIR;

/**
 * Base class for Palantir to hide the complexity
 * of model state management.
 */
public abstract class PalantirComponent extends
        BaseComponent<SimulatorModel.Type, PalantirComponent.State> {
    /**
     * Thread-safe long value used to allocate unique 1 base
     * indexed being ids.
     */
    private static AtomicInteger sIdProvider = new AtomicInteger(0);

    /**
     * The id of the being currently gazing into this
     * palantir component, or -1 if no being is currently
     * gazing into this palantir.
     */
    private long mBeingId = -1;

    /**
     * Constructor for capturing a back reference to the
     * enclosing model class.
     *
     * @param modelProvider An ModelProvider implementation class
     *                      that provides access to the simulation
     *                      Model instance.
     */
    public PalantirComponent(ModelProvider modelProvider) {
        // Call super class constructor to set this component type
        // and to initialize all base class field values.
        super(PALANTIR, AVAILABLE, sIdProvider.incrementAndGet(), modelProvider);
    }

    /**
     * Called to reset static id generator back to 0.
     */
    public static void resetIds() {
        sIdProvider.set(0);
    }

    @Override
    public void setState(State state, @Nullable Throwable e, @Nullable String message) {
        throw new IllegalStateException("Palantiri should not call setState");
    }

    /**
     * Builds an immutable component snapshot that describes
     * the current state of this component suitable for pushing
     * to the presentation layer for rendering.
     */
    @Override
    public PalantirSnapshot buildSnapshot() {
        return new PalantirSnapshot((Palantir) this);
    }

    /**
     * @return The id of the being that is currently gazing into this
     * palantir or -1 if this palantir is not currently being gazed into
     * by any being.
     */
    public long getBeingId() {
        return mBeingId;
    }

    /**
     * Called by {@link edu.vandy.simulator.Simulator#gazeIntoPalantir}
     * to set the id of the being that is currently gazing into this
     * palantir. This value is included in any snapshots forwarded
     * to the presentation layer.
     *
     * @param beingId The id of the being currently gazing into this
     *                palantir component, or -1 if no being is currently
     *                gazing into this palantir.
     */
    public void setBeingId(long beingId) {
        mBeingId = beingId;
    }

    /**
     * Called to reset this component to it's initial state
     * suitable for restarting a simulation.
     */
    public void reset() {
        mBeingId = -1;
    }

    /**
     * @return Description of class fields excluding null fields.
     */
    @Override
    public String toString() {
        return getType() +
                "[" + getId() + "]" +
                " " + getState() +
                " beingId=" + getBeingId() +
                (getException() != null ? " e=" + getException() : "") +
                (getMessage() != null ? " msg='" + getMessage() + "'" : "");
    }

    /**
     * Palantir states.
     */
    public enum State {
        AVAILABLE,  // not being used by any being
        BUSY,       // being used by a single being
        REMOVED,    // this component has been removed from the model
        ERROR       // model has detected a fatal error with this component
    }
}

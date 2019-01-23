package edu.vandy.simulator.model.implementation.snapshots;

import org.jetbrains.annotations.NotNull;

import edu.vandy.simulator.managers.palantiri.Palantir;
import edu.vandy.simulator.model.base.BaseSnapshot;
import edu.vandy.simulator.model.implementation.components.PalantirComponent;
import edu.vandy.simulator.model.implementation.components.SimulatorModel;

/**
 * Immutable snapshot of a Palantir state which does not
 * need to add any model specific information above and
 * beyond what is already provided by the base class.
 */
public class PalantirSnapshot extends
        BaseSnapshot<SimulatorModel.Type, PalantirComponent.State> {
    /**
     * The id of the currently gazing being or -1 if no
     * being is currently gazing into this palantir.
     */
    private final long mBeingId;

    /**
     * The number of times beings have gazed at this palantir.
     */
    private final int mCount;

    /**
     * Special default constructor that
     * allows for an empty snapshot.
     */
    public PalantirSnapshot() {
        mBeingId = -1L;
        mCount = 0;
    }

    /**
     * Constructor to support cloning.
     *
     * @param snapshot The instance to clone.
     */
    public PalantirSnapshot(@NotNull PalantirSnapshot snapshot) {
        super(snapshot);
        mBeingId = snapshot.getBeingId();
        mCount = snapshot.getCount();
    }

    /**
     * Constructs a snapshot from the provided component.
     *
     * @param palantir The component to snapshot.
     */
    public PalantirSnapshot(@NotNull Palantir palantir) {
        // Call base class constructor to initialize ModelComponent fields.
        super(palantir);

        // Set model dependant attributes.
        mBeingId = palantir.getBeingId();
        mCount = palantir.getCount();
    }

    /**
     * @return The id of the currently gazing being or -1 if
     * no being is currently gazing into this palantir.
     */
    public Long getBeingId() {
        return mBeingId;
    }

    /**
     * @return The number times this palantir has been gazed at.
     */
    public int getCount() {
        return mCount;
    }

    /**
     * Called to determine if the associated component has
     * been removed from the model. If true, then the component
     * state should be considered to be not meaningful and should
     * not be used.
     *
     * @return Flag indicating if the associated component has
     * been removed from the model.
     */
    @Override
    public Boolean isRemoved() {
        return getState() == PalantirComponent.State.REMOVED;
    }
}

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
}

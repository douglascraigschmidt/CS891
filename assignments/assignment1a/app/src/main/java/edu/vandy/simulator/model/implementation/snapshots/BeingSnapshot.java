package edu.vandy.simulator.model.implementation.snapshots;

import org.jetbrains.annotations.NotNull;

import edu.vandy.simulator.managers.beings.Being;
import edu.vandy.simulator.model.base.BaseSnapshot;
import edu.vandy.simulator.model.implementation.components.BeingComponent;
import edu.vandy.simulator.model.implementation.components.SimulatorModel;

/**
 * Immutable snapshot of a Being state.
 */
public class BeingSnapshot extends
        BaseSnapshot<SimulatorModel.Type, BeingComponent.State> {
    private final long mPalantirId;
    private final long mDuration;
    private final int mIterations;
    private final int mCompleted;

    /**
     * Special default constructor that
     * allows for an empty snapshot.
     */
    public BeingSnapshot() {
        mPalantirId = -1;
        mDuration = 0;
        mCompleted = 0;
        mIterations = 0;
    }

    /**
     * Constructor for clone support.
     *
     * @param snapshot Instance to clone.
     */
    public BeingSnapshot(BeingSnapshot snapshot) {
        super(snapshot);
        mPalantirId = snapshot.mPalantirId;
        mDuration = snapshot.mDuration;
        mCompleted = snapshot.mCompleted;
        mIterations = snapshot.mIterations;
    }

    /**
     * Normal constructor.
     *
     * @param being Being whose state will be captured by this snapshot.
     */
    public BeingSnapshot(@NotNull Being being) {
        // Call base class constructor to initialize ModelComponent fields.
        super(being);

        // Model specific attributes.
        mDuration = being.getDuration();
        mCompleted = being.getCompleted();
        mIterations = being.getGazingIterations();

        switch (getState()) {
            case ACQUIRING:
            case GAZING:
            case RELEASING:
                // The being state is the ground-truth and not the mPalantir
                // for determining if a being should be linked to a palantir
                // in the presentation layer.
                mPalantirId = being.getPalantirId();
                if (mPalantirId == -1) {
                    throw new IllegalStateException("PalantirId should not be -1 " +
                            "when being is " + getState() + ": " + being);
                }
                break;
            default:
                // Don't care about a palantir if not in the right state.
                mPalantirId = -1;
        }
    }

    /**
     * Model specific attribute.
     *
     * @return The expected duration of the state (milliseconds).
     */
    public long getDuration() {
        return mDuration;
    }

    /**
     * @return The palantirId acquired by this being or -1
     * if the being is has not acquired a palantir.
     */
    public long getPalantirId() {
        return mPalantirId;
    }

    /**
     * @return The number of completed gazing iterations.
     */
    public int getCompleted() {
        return mCompleted;
    }

    /**
     * @return The number of gazing iterations.
     */
    public int getIterations() {
        return mIterations;
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
        return getState() == BeingComponent.State.REMOVED;
    }

    @Override
    public String toString() {
        return super.toString()
                + (mDuration > 0 ? " duration=" + mDuration : "")
                + " mPalantirId=" + mPalantirId
                + " mCompleted=" + mCompleted;
    }
}

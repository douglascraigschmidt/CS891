package edu.vandy.simulator.model.implementation.snapshots;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

import edu.vandy.simulator.Simulator;
import edu.vandy.simulator.model.base.BaseSnapshot;
import edu.vandy.simulator.model.implementation.components.SimulatorComponent;
import edu.vandy.simulator.model.implementation.components.SimulatorComponent.State;
import edu.vandy.simulator.model.implementation.components.SimulatorModel.Type;
import edu.vandy.simulator.model.interfaces.ModelComponent;

/**
 * Immutable snapshot of a Being state.
 */
public class SimulatorSnapshot extends BaseSnapshot<Type, State>
        implements Cloneable {
    /**
     * An snapshot id provider that is used to uniquely
     * identify each ModelSnapshot that is posted to the
     * presentation layer. This is useful for debugging
     * and for logging snapshot history.
     */
    private static final AtomicInteger sIdProvider = new AtomicInteger(0);
    private final int mSnapshotId = sIdProvider.getAndIncrement();

    /**
     * Model dependant attributes.
     */
    private final int mBeingCount;
    private final int mPalantirCount;
    private final int mGazingIterations;

    /**
     * Constructor that initializes all generic ComponentSnapshot fields
     * as well as the application dependent attribute fields (passed
     * snapshot parameters).
     */
    private SimulatorSnapshot() {
        super();
        mBeingCount = 0;
        mPalantirCount = 0;
        mGazingIterations = 0;
    }

    /**
     * Constructor for cloning support.
     *
     * @param snapshot Instance to clone.
     */
    public SimulatorSnapshot(SimulatorSnapshot snapshot) {
        super(snapshot);
        mBeingCount = snapshot.mBeingCount;
        mPalantirCount = snapshot.mPalantirCount;
        mGazingIterations = snapshot.mGazingIterations;
    }

    /**
     * Constructor that initializes all generic ComponentSnapshot fields
     * as well as the application dependent attribute fields (passed
     * snapshot parameters).
     */
    public SimulatorSnapshot(@NotNull Simulator component) {
        super(component);
        mBeingCount =
                component.getBeingManager().getBeingCount();
        mPalantirCount =
                component.getPalantirManager().getPalantirCount();
        mGazingIterations =
                component.getBeingManager().getGazingIterations();
    }

    /**
     * @return The model being count parameter.
     */
    public int getBeingCount() {
        return mBeingCount;
    }

    /**
     * @return The model palantir count parameter.
     */
    public int getPalantirCount() {
        return mPalantirCount;
    }

    /**
     * @return The model gazing iterations parameter.
     */
    public int getGazingIterations() {
        return mGazingIterations;
    }

    /**
     * @return Always {@code false}, a simulator component
     * can never be removed.
     */
    @Override
    public Boolean isRemoved() {
        return false;
    }

    /**
     * @return The unique id of this snapshot for tracking snapshot history.
     */
    public long getSnapshotId() {
        return mSnapshotId;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

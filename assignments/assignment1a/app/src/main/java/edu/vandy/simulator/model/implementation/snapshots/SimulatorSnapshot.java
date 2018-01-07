package edu.vandy.simulator.model.implementation.snapshots;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

import edu.vandy.simulator.model.base.BaseSnapshot;
import edu.vandy.simulator.model.implementation.components.SimulatorComponent.State;
import edu.vandy.simulator.model.implementation.components.SimulatorModel.Type;
import edu.vandy.simulator.model.interfaces.ModelComponent;

/**
 * Immutable snapshot of a Being state.
 */
public class SimulatorSnapshot extends BaseSnapshot<Type, State> {
    /**
     * An snapshot id provider that is used to uniquely
     * identify each ModelSnapshot that is posted to the
     * presentation layer. This is useful for debugging
     * and for logging snapshot history.
     */
    private static final AtomicInteger sIdProvider = new AtomicInteger(0);
    private final int mSnapshotId = sIdProvider.getAndIncrement();

    /**
     * Constructor that initializes all generic ComponentSnapshot fields
     * as well as the application dependent attribute fields (passed
     * snapshot parameters).
     */
    SimulatorSnapshot() {
        super();
        System.out.println("BaseModel: Sending snapshot# " + mSnapshotId);
    }

    /**
     * Constructor that initializes all generic ComponentSnapshot fields
     * as well as the application dependent attribute fields (passed
     * snapshot parameters).
     */
    public SimulatorSnapshot(@NotNull ModelComponent<Type, State> component) {
        super(component);
    }

    /**
     * @return The unique id of this snapshot for tracking snapshot history.
     */
    public long getSnapshotId() {
        return mSnapshotId;
    }
}

package edu.vandy.simulator.model.implementation.snapshots;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import edu.vandy.simulator.Simulator;
import edu.vandy.simulator.model.base.BaseSnapshot;
import edu.vandy.simulator.model.implementation.components.SimulatorModel;
import edu.vandy.simulator.model.interfaces.ComponentSnapshot;

/**
 * An immutable DTO object that captures the state of the
 * entire simulation model by including snapshots of all
 * model components.
 * <p>
 * This class hides the generics of SimulatorSnapshot from
 * the ModelObserver implementation.
 */
public class ModelSnapshot {
    /**
     * A static id generator.
     */
    private static final AtomicInteger sIdProvider = new AtomicInteger(0);

    /**
     * The unique id of this snapshot.
     */
    private final int mSnapshotId = sIdProvider.getAndIncrement();

    /**
     * Support for an empty snapshot to void nulls.
     */
    public static final ModelSnapshot NO_SNAPSHOT =
            new ModelSnapshot(new SimulatorSnapshot(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    -1L);

    /**
     * The component id that triggered the snapshot (for auditing).
     */
    private final long mTriggeredById;

    /**
     * Model dependant attribute fields. They have been given
     * short names so that they are less verbose to use in
     * the Kotlin presentation layer.
     */
    private final SimulatorSnapshot mSimulator;
    private final List<BeingSnapshot> mBeings;
    private final List<PalantirSnapshot> mPalantiri;

    /**
     * Constructor that saves all the passed snapshots in a wrapper
     * class that is then transmitted to the presentation layer to
     * render.
     *
     * @param simulatorSnapshot A list of being snapshots (or empty list).
     * @param beingSnapshots    A list of being snapshots (or empty list).
     * @param palantirSnapshots A list of palantir snapshots (or empty list).
     */
    public ModelSnapshot(@NotNull SimulatorSnapshot simulatorSnapshot,
                         @NotNull List<BeingSnapshot> beingSnapshots,
                         @NotNull List<PalantirSnapshot> palantirSnapshots,
                         long triggeredById) {
        // Model dependant attributes.
        mSimulator = simulatorSnapshot;
        mBeings = beingSnapshots;
        mPalantiri = palantirSnapshots;
        mTriggeredById = triggeredById;
    }

    /**
     * Model specific attribute.
     *
     * @return A list of being component snapshots.
     */
    public SimulatorSnapshot getSimulator() {
        return mSimulator;
    }

    /**
     * Model specific attribute.
     *
     * @return A list of being component snapshots.
     */
    public List<BeingSnapshot> getBeings() {
        return mBeings;
    }

    /**
     * Model specific attribute.
     *
     * @return A list of palantir component snapshots.
     */
    public List<PalantirSnapshot> getPalantiri() {
        return mPalantiri;
    }

    /**
     * @return The current unique snapshot id.
     */
    public long getSnapshotId() {
        return mSnapshotId;
    }

    @Override
    public String toString() {
        return "ModelSnapshot[" + getSnapshotId() + "]"
                + " triggered by Being[" + mTriggeredById + "]";
    }
}

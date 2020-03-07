package edu.vandy.simulator.model.implementation.snapshots;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import edu.vandy.simulator.model.interfaces.ModelComponent;

/**
 * An immutable DTO object that captures the state of the
 * entire simulation model by including snapshots of all
 * model components.
 * <p>
 * This class hides the generics of SimulatorSnapshot from
 * the ModelObserver implementation.
 */
public class ModelSnapshot implements Cloneable {
    /**
     * Support for an empty snapshot to void nulls.
     */
    public static final ModelSnapshot NO_SNAPSHOT = new ModelSnapshot();
    /**
     * A static id generator (must be declared before NO_SNAPSHOT).
     */
    private static final AtomicInteger sIdProvider = new AtomicInteger(0);
    /**
     * The unique id of this snapshot.
     */
    private final int mSnapshotId = sIdProvider != null ? sIdProvider.getAndIncrement() : 0;
    /**
     * The component id that triggered the snapshot (for auditing).
     */
    private final long mTriggeredById;
    private final Map<Long, BeingSnapshot> mBeings;
    private final Map<Long, PalantirSnapshot> mPalantiri;
    /**
     * Model dependant attribute fields. They have been given
     * short names so that they are less verbose to use in
     * the Kotlin presentation layer.
     */
    private SimulatorSnapshot mSimulator;

    /**
     * Constructs a new empty model snapshot and is used to
     * eliminate errors caused by null objects.
     */
    public ModelSnapshot() {
        mSimulator = null;
        mBeings = new HashMap<>();
        mPalantiri = new HashMap<>();
        mTriggeredById = -1L;
    }

    /**
     * Constructor that saves all the passed snapshots in a wrapper
     * class that is then transmitted to the presentation layer to
     * render.
     *
     * @param simulatorSnapshot A snapshot of the current simulator state.
     * @param beingSnapshots    A map of being snapshots.
     * @param palantirSnapshots A map of palantir snapshots.
     * @param component         The model component that is triggering the snapshot.
     */
    public ModelSnapshot(@NotNull SimulatorSnapshot simulatorSnapshot,
                         @NotNull Map<Long, BeingSnapshot> beingSnapshots,
                         @NotNull Map<Long, PalantirSnapshot> palantirSnapshots,
                         ModelComponent component) {
        // Model dependant attributes.
        mSimulator = simulatorSnapshot;
        mBeings = beingSnapshots;
        mPalantiri = palantirSnapshots;
        mTriggeredById = component.getId();
    }

    /**
     * Constructor for clone support.
     *
     * @param snapshot Instance to clone.
     */
    public ModelSnapshot(ModelSnapshot snapshot) {
        mSimulator = new SimulatorSnapshot(snapshot.mSimulator);

        mBeings = snapshot.mBeings.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        v -> new BeingSnapshot(v.getValue())));

        mPalantiri = snapshot.mPalantiri.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        v -> new PalantirSnapshot(v.getValue())));

        mTriggeredById = snapshot.mTriggeredById;
    }

    /**
     * Model specific attribute.
     *
     * @return A list of being component snapshots.
     */
    public SimulatorSnapshot getSimulator() {
        return mSimulator;
    }

    public void setSimulator(SimulatorSnapshot snapshot) {
        mSimulator = snapshot;
    }

    /**
     * Model specific attribute.
     *
     * @return A list of being component snapshots.
     */
    public Map<Long, BeingSnapshot> getBeings() {
        return mBeings;
    }

    /**
     * Model specific attribute.
     *
     * @return A list of palantir component snapshots.
     */
    public Map<Long, PalantirSnapshot> getPalantiri() {
        return mPalantiri;
    }

    /**
     * @return The current unique snapshot id.
     */
    public long getSnapshotId() {
        return mSnapshotId;
    }

    /**
     * @return The id of the componennt that triggered this snapshot.
     */
    public long getTriggeredById() {
        return mTriggeredById;
    }

    @Override
    @NotNull
    public String toString() {
        String triggeredBy;

        if (mTriggeredById == -1) {
            triggeredBy = "unspecified";
        } else if (mTriggeredById == this.mSimulator.getId()) {
            triggeredBy = "Simulator";
        } else if (mBeings.containsKey(mTriggeredById)) {
            triggeredBy = "Being[" + mTriggeredById + "]";
        } else if (mPalantiri.containsKey(mTriggeredById)) {
            triggeredBy = "Palantir[" + mTriggeredById + "]";
        } else {
            triggeredBy = "ERROR";
        }

        return "ModelSnapshot"
                + " triggered by " + triggeredBy
                + " beings=" + mBeings.size()
                + " palantiri=" + mPalantiri.size();
    }
}

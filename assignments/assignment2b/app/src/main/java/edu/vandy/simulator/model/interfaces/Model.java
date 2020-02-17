package edu.vandy.simulator.model.interfaces;

/**
 * Model interface that supports adding and removing
 * {@link ModelObserver}'s and a method for triggering
 * model snapshot generation that is pushed to all
 * registered observers.
 * <p>
 * Typically a Model implementation will itself be a
 * ModelComponent with its own overall state and act
 * as a centralizing {@link SnapshotProvider}. But
 * this in not enforced so that implementations can
 * have a model implementation that has not state but
 * simply acts as a transmitter of snapshots to the
 * presentation layer.
 */
public interface Model {
    /**
     * Adds an new {@link ModelObserver} to the list of
     * observers. The observer will be notified of all
     * model state changes.
     *
     * @param observer An implementation of the
     *                 {@link ModelObserver} interface.
     * @param notify   If true observer will immediately receive
     *                 a current snapshot of the model state.
     */
    void addObserver(ModelObserver observer, boolean notify);

    /**
     * Removes the specified {@link ModelObserver}
     * from the list of observers.
     *
     * @param observer An implementation of the
     *                 {@link ModelObserver} interface.
     */
    void removeObserver(ModelObserver observer);

    /**
     * Called when any model component changes state and
     * should create a model snapshot DTO than is pushed
     * to any registered model observers. The contents
     * of the snapshot is model dependant.
     *
     * @param component The component that is triggering
     *                  the snapshot.
     */
    void triggerSnapshot(ModelComponent component);
}

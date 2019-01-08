package edu.vandy.simulator.model.interfaces;

/**
 * Used by model components that provide a snapshot
 * of their current state suitable for transmitting
 * to the presentation layer for rendering.
 */
public interface SnapshotProvider<Type, State> {
    /**
     * Called by the Model framework to capture a snapshot
     * of the ModelComponent's state. This snapshot will
     * contain all the information supported by a StateProvider
     * as well as any additional information model dependant
     * information.
     *
     * @return An immutable snapshot DTO object.
     */
    ComponentSnapshot<Type, State> buildSnapshot();
}

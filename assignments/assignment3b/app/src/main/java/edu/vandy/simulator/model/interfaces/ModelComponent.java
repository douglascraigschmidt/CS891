package edu.vandy.simulator.model.interfaces;

/**
 * Basic model component that belongs to a specific
 * group of components identified by an enumerated
 * type <T>. A ModelComponent also
 * acts as a ModelProvider (routes model state changes
 * to the top Model implementation) and as a StateProvider
 * that maintains the current state information for the
 * component implementation.
 *
 * @param <Type>  An enumerated type representing all the
 *                components types.
 * @param <State> An enumerate type representing all the
 *                states this component supports.
 */
public interface ModelComponent<Type, State>
        extends ModelProvider,
        StateProvider<State>,
        SnapshotProvider<Type, State> {
    /**
     * @return The component type.
     */
    Type getType();

    /**
     * @return The component's unique id.
     */
    long getId();

    /**
     * Builds an immutable component snapshot that describes
     * the current state of this component suitable for pushing
     * to the presentation layer for rendering.
     */
    ComponentSnapshot<Type, State> buildSnapshot();
}

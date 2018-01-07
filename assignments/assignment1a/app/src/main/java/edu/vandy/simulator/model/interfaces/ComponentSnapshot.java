package edu.vandy.simulator.model.interfaces;

import org.jetbrains.annotations.Nullable;

/**
 * An immutable DTO wrapper interface that contains snapshot
 * of a model component's id and current state. Implementations
 * of this interface can add additional model specific data
 * required to describe the components state.
 * <p>
 * The model will call a ModelComponent's buildSnapshot() method
 * to build and return a ComponentSnapshot and that object will
 * be included in a Model snapshot that is pushed to the presentation
 * layer.
 *
 * @param <Type> The type of component.
 * @param <State> An enumerated list of all possible states for
 *            the component implementing this interface.
 */
public interface ComponentSnapshot<Type, State> {
    /**
     * @return A unique component id.
     */
    long getId();

    /**
     * @return Time when the snapshot was created.
     */
    long getTimestamp();

    /**
     * @return The component type.
     */
    Type getType();

    /**
     * @return Optional exception if state includes an
     * encountered an exception.
     */
    @Nullable
    Throwable getException();

    /**
     * @return Optional message that provides additional
     * information about the state.
     */
    @Nullable
    String getMessage();

    /**
     * @return The state of the component at the time of the
     * snapshot was captured.
     */
    State getState();

    /**
     * @return The previous state of the component at the time
     * of the snapshot was captured.
     */
    State getPrevState();
}

package edu.vandy.simulator.model.interfaces;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implemented by any model component that maintains
 * state information that should be included in an
 * Model Snapshot that is pushed to the presentation
 * layer.
 */
public interface StateProvider<T> {
    /**
     * @return The current component state.
     */
    T getState();

    /**
     * @return An optional previous state.
     */
    @Nullable
    T getPrevState();

    /**
     * Sets the components current state.
     *
     * @param state The component's current state.
     */
    void setState(T state);

    /**
     * Sets the components current state along with
     * an exception.
     *
     * @param state The component's current state.
     * @param message An information text message.
     */
    void setState(T state, @NotNull String message);

    /**
     * Sets the components current state along with
     * a message.
     *
     * @param state The component's current state.
     * @param e The component's encountered exception.
     */
    void setState(T state, @NotNull Throwable e);

    /**
     * Calls the Model instance to handle the Being state change
     * and to push a new model snapshot to the presentation layer.
     *
     * @param state BeingState to move to.
     * @param e     The component's encountered exception.
     * @param message A message relating to the state.
     */
    void setState(T state, @Nullable Throwable e, @Nullable String message);

    /**
     * @return Optional exception if state encountered
     * an exception.
     */
    @Nullable
    Throwable getException();

    /**
     * @return Optional informational message.
     */
    @Nullable
    String getMessage();
}

package edu.vandy.simulator.model.interfaces;

import org.jetbrains.annotations.NotNull;

/**
 * An interface that aggregates the high level
 * actions that can be performed on a runnable
 * model (e.g. a simulator model).
 */
public interface ModelController {
    /**
     * External entry point for starting a runnable model.
     */
    void start();

    /**
     * External entry point for stopping a model.
     */
    void stop();

    /**
     * External entry point for resetting a model so
     * that it can be rerun assuming that all model
     * parameters have not been changed.
     */
    void reset();

    /**
     * Internal shutdown handler that can be invoked
     * externally from a {@link #stop} request, or
     * internally be a call to either of the overloaded
     * {@link #error} methods.
     */
    void shutdown();

    /**
     * Internal handler called when any model component
     * encounters a non-recoverable error. This method
     * should always call {@link #shutdown} that will
     * then propagate the shutdown request to all model
     * components.
     *
     * @param e A throwable cause of the non-recoverable error.
     * @throws IllegalStateException Wraps the passed throwable e.
     */
    void error(@NotNull Throwable e) throws IllegalStateException;

    /**
     * Internal handler called when any model component
     * encounters a non-recoverable error. This method
     * should always call {@link #shutdown} that will
     * then propagate the shutdown request to all model
     * components.
     *
     * @param message A non null string explaining the
     *                cause of the non-recoverable error.
     * @param args
     * @throws IllegalStateException Wraps the passed message.
     */
    void error(@NotNull String message, Object... args)
            throws IllegalStateException;
}

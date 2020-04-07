package edu.vandy.simulator.model.interfaces;

public interface CancellableComponent {
    /**
     * Should be called periodically to determine if a
     * shutdown has been requested. This method should
     * be called at a relatively fine-grained interval
     * to ensure responsive shutdown requests.
     *
     * @return {@code true} if the component should shutdown.
     */
    boolean isCancelled();

    /**
     * Sets a cancelled flag that should periodically
     * monitored at a fine-grained interval to allow
     * responsive shutdown requests.
     */
    void shutdownNow();

    /**
     * @return {@code true} if the component is currently
     * running or active (has not been shutdown or has
     * completed).
     */
    boolean isRunning();

    /**
     * Called to reset this component so that it can be
     * reused in a new simulation.
     * <p>
     * //     * @return {@code true} if this component is resettable
     * //     * and was successfully reset, or {@code false} if this
     * //     * is not resettable or was not able to be reset.
     */
    void reset();
}

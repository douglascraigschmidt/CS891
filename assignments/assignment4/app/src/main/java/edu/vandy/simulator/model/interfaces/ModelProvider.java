package edu.vandy.simulator.model.interfaces;

import org.jetbrains.annotations.NotNull;

/**
 * Used by model components that hold a reference to
 * the Simulation's Model instance.
 */
public interface ModelProvider {
    /**
     * @return The component's model instance.
     */
    @NotNull
    Model getModel();
}

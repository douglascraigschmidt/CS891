package edu.vandy.simulator.model.implementation.components;

import org.jetbrains.annotations.NotNull;

import edu.vandy.simulator.Simulator;
import edu.vandy.simulator.model.base.BaseModel;
import edu.vandy.simulator.model.implementation.snapshots.SimulatorSnapshot;
import edu.vandy.simulator.model.interfaces.Model;
import edu.vandy.simulator.model.interfaces.ModelComponent;
import edu.vandy.simulator.model.interfaces.ModelProvider;

/**
 * The top level model simulator model that extends the
 * default BaseModel abstract class and adds the model
 * dependant list of ModelComponent enumerated types.
 * The Simulator class is the only concrete implementation
 * of this class but only sees itself as a ModelComponent.
 */
public abstract class SimulatorModel
        extends BaseModel<SimulatorModel.Type, SimulatorComponent.State> {
    /**
     * Constructor for capturing a back reference to the
     * enclosing model class.
     *
     * @param type     The component type.
     * @param state    Initial state.
     * @param provider An ModelProvider implementation class
     *                 that provides access to the simulation
     */
    public SimulatorModel(Type type,
                          SimulatorComponent.State state,
                          ModelProvider provider) {
        super(type, state, provider);
    }

    /**
     * @return This SimulatorModel since it is the top level Model.
     */
    @Override
    @NotNull
    public Model getModel() {
        return this;
    }

    /**
     * Builds an immutable component snapshot that describes
     * the current state of this component suitable for pushing
     * to the presentation layer for rendering.
     */
    @Override
    public SimulatorSnapshot buildSnapshot() {
        return new SimulatorSnapshot((Simulator)this);
    }

    /**
     * Creates a snapshot of the current model state that includes
     * the states of all beings (BeingState) and palantiri
     * (PalantiriState) along with the current overall state of the
     * simulator (ModelState). This snapshot is then pushed to the
     * presentation layer's Observer callback.
     * @param component
     */
    @Override
    public void triggerSnapshot(ModelComponent component) {
        super.triggerSnapshot(component);
    }

    /**
     * The list of all ModelComponent
     * types supported by this model.
     */
    public enum Type {
        BEING,
        PALANTIR,
        SIMULATOR
    }
}

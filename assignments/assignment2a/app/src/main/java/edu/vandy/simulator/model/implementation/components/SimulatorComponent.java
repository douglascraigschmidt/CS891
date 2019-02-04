package edu.vandy.simulator.model.implementation.components;

import edu.vandy.simulator.model.base.BaseComponent;
import edu.vandy.simulator.model.interfaces.ComponentSnapshot;
import edu.vandy.simulator.model.interfaces.ModelComponent;
import edu.vandy.simulator.model.interfaces.ModelProvider;

import static edu.vandy.simulator.model.implementation.components.SimulatorComponent.State.IDLE;
import static edu.vandy.simulator.model.implementation.components.SimulatorModel.Type.SIMULATOR;

/**
 * A {@link ModelComponent} abstract class that defines the
 * supported states for a simulator object as well as
 * the additional model dependant state management that is
 * not provided by the generic BaseComponent abstract class.
 * This class hides the complexity of state management from the
 * Simulator subclass.
 */
public class SimulatorComponent extends
        BaseComponent<SimulatorModel.Type, SimulatorComponent.State> {
    /**
     * Constructor for capturing a back reference to the
     * enclosing model class.
     *
     * @param modelProvider An ModelProvider implementation class
     *                      that provides access to the simulation
     *                      Model instance.
     */
    public SimulatorComponent(ModelProvider modelProvider) {
        // Call super class constructor to set this component type
        // and to initialize all base class field values.
        super(SIMULATOR, IDLE, 0, modelProvider);
    }

    /**
     * Not used in this model because the simulator manually
     * constructs a SimulatorSnapshot so that it can avoid
     * problems with generics becoming exposed to the presentation
     * layer.
     */
    @Override
    public ComponentSnapshot<SimulatorModel.Type, State> buildSnapshot() {
        throw new IllegalStateException("SimulatorSnapshot is created by Simulator.");
    }

    /**
     * Possible states of this simulation model.
     */
    public enum State {
        UNDEFINED,  // initial state before model has been build/defined
        IDLE,       // simulation has been built and is ready to be run
        RUNNING,    // simulation is running
        CANCELLING, // simulation is in the process of being cancelled
        CANCELLED,  // simulation has been successfully cancelled
        COMPLETED,  // simulation has successfully completed
        ERROR       // simulation has terminated due to an exception
    }
}

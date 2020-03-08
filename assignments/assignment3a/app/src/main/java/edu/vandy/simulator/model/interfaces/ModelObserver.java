package edu.vandy.simulator.model.interfaces;

import edu.vandy.simulator.model.implementation.snapshots.ModelSnapshot;

/**
 * An observer interface with a single method that is called
 * whenever the state of any model component changes.
 * The onModelChange method is called when the model state
 * has changed and those changes need to be reflected in the
 * presentation layer. The passed snapshot is a complete
 * description of the entire model and therefore may be
 * considered as the ground-truth of the model state.
 * The goal of this design is to centralize all state information
 * in the model layer so that the presentation layer can be
 * destroyed and recreated without having to concern itself
 * with saving and restoring state. This design ensures a
 * single directional flow of all state information from the
 * model to the presentation layer and this minimizes the
 * accidental complexities that result from state information
 * being managed by multiple application components.
 * <p>
 * When the presentation layer requires operations to be performed
 * on the model, it sends these requests as "intents". They are
 * called "intents" because they have the intention of updating the
 * model. Only the model should store the state (progress) of
 * pending intents which will be included in subsequent snapshots.
 * This allows the activity to be destroyed and recreated without
 * worrying about saving any information make orientation changes
 * a trivial concern.
 */
public interface ModelObserver {
    /**
     * Called when the model state has changed and those changes
     * need to be reflected in the presentation layer. The passed
     * snapshot is a complete description of the entire model and
     * therefore may be considered as the ground-truth of the model
     * state.
     *
     * @param snapshot An immutable snapshot of the model state.
     */
    void onModelChanged(ModelSnapshot snapshot);
}

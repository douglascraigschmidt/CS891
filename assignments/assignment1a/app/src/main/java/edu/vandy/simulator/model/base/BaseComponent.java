package edu.vandy.simulator.model.base;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import edu.vandy.simulator.Controller;
import edu.vandy.simulator.model.interfaces.Model;
import edu.vandy.simulator.model.interfaces.ModelComponent;
import edu.vandy.simulator.model.interfaces.ModelProvider;

/**
 * A base component implementation that hides all the component
 * interface boiler-plate code from concrete subclasses.
 *
 * @param <Type>  Enumerate type used to identity the supported
 *                components types.
 * @param <State> Enumerated state supported used to identify the
 *                supported component states.
 */
public abstract class BaseComponent<Type, State>
        implements ModelComponent<Type, State> {
    /**
     * The component type.
     */
    private final Type mType;

    /**
     * Back reference to enclosing model class.
     */
    private final ModelProvider mModelProvider;

    /**
     * The component's id. Concrete implementations are
     * required to provide an id generator to support
     * the component ids attribute.
     */
    private final int mId;

    /**
     * The current state of this component.
     */
    private volatile State mState;

    /**
     * Optional support for keep a record of the
     * previous state (useful for debug logging).
     */
    @Nullable
    private volatile State mPrevState = null;

    /**
     * Optional encountered exception.
     */
    @Nullable
    private volatile Throwable mThrowable = null;
    /**
     * Optional state message string.
     */
    @Nullable
    private volatile String mMessage = null;

    /**
     * Base constructor initializes the component type.
     * n*
     *
     * @param type     The enum type of component.
     * @param state    The initial state (to avoid nulls).
     * @param provider Back reference to the root model provider.
     * @param id       The component id.
     */
    public BaseComponent(Type type, State state, int id, ModelProvider provider) {
        mType = type;
        mState = state;
        mId = id;
        mModelProvider = provider;
    }

    /**
     * @return A unique component id.
     */
    @Override
    public int getId() {
        return mId;
    }

    /**
     * @return The component's type.
     */
    @Override
    public Type getType() {
        return mType;
    }

    /**
     * @return The component's model instance.
     */
    @Override
    @NotNull
    public Model getModel() {
        return mModelProvider.getModel();
    }

    /**
     * @return The current component state.
     */
    @Override
    public State getState() {
        return mState;
    }

    /**
     * Sets the components current state.
     *
     * @param state The component's current state.
     */
    @Override
    public void setState(State state) {
        setState(state, null, null);
    }

    /**
     * @return An optional previous state.
     */
    @Override
    @Nullable
    public State getPrevState() {
        return mPrevState;
    }

    /**
     * Sets the components current state along with
     * an exception.
     *
     * @param state   The component's current state.
     * @param message An information text message.
     */
    @Override
    public void setState(State state, @NotNull String message) {
        setState(state, null, message);
    }

    /**
     * Sets the components current state along with
     * a message.
     *
     * @param state The component's current state.
     * @param e     The component's encountered exception.
     */
    @Override
    public void setState(State state, @NotNull Throwable e) {
        setState(state, e, null);
    }

    /**
     * Calls the Model instance to handle the Being state change
     * and to push a new model snapshot to the presentation layer.
     *
     * @param state   BeingState to move to.
     * @param e       The component's encountered exception.
     * @param message A message relating to the state.
     */
    @Override
    public void setState(State state, @Nullable Throwable e, @Nullable String message) {
        mPrevState = mState;
        mState = state;
        mThrowable = e;
        mMessage = message;
        Controller.log("Snapshot triggered by " + this);
        getModel().triggerSnapshot(this.getId());
    }

    /**
     * @return Optional exception if state encountered
     * an exception.
     */
    @Nullable
    @Override
    public Throwable getException() {
        return mThrowable;
    }

    /**
     * @return Optional informational message.
     */
    @Nullable
    @Override
    public String getMessage() {
        return mMessage;
    }

    /**
     * @return Description of class fields excluding null fields.
     */
    @Override
    public String toString() {
        return "BaseComponent{" +
                getType() +
                ", id=" + getId() +
                (getPrevState() != null ? ", prevState=" + getPrevState() : "") +
                ", state=" + getState() +
                (getException() != null ? ", e=" + getException() : "") +
                (getMessage() != null ? ", msg='" + getMessage() + "'" : "") +
                '}';
    }
}

package edu.vandy.simulator.model.base;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

import edu.vandy.simulator.Controller;
import edu.vandy.simulator.model.interfaces.ComponentSnapshot;
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
    private final long mId;

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
     * Flag indicating if this component has been removed
     * from the model.
     */
    private boolean mRemoved = false;

    /**
     * Support for caching last snapshot.
     */
    private ComponentSnapshot<Type, State> mSnapshot = null;

    /**
     * A flag indicating of the component state has changed since
     * the last snapshot was generated. Initially, start off with
     * this flag set so that its first snapshot will be automatically
     * generated when the model is first constructed.
     */
    private AtomicBoolean mModified = new AtomicBoolean(true);

    /**
     * Base constructor initializes the component type.
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
    public long getId() {
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
        setStateHelper(state, e, message);
    }

    /**
     * This setState helper method.
     */
    public final void setStateHelper(State state, @Nullable Throwable e, @Nullable String message) {
        mPrevState = mState;
        mState = state;
        mThrowable = e;
        mMessage = message;
        Controller.log("Snapshot triggered by " + this);

        // Set the dirty flag before triggering the snapshot.
        setModified();

        // Trigger a snapshot generation event.
        getModel().triggerSnapshot(this);
    }

    /**
     * Wrapper method to support efficient snapshot generation.
     * If the modified flag is false, and a previous
     * snapshot exists, then the last generated snapshot is
     * returned (avoiding memory allocations). Otherwise, a new
     * snapshot is generated, cached, and then returned. Note that
     * a cached snapshot should be considered immutable and should
     * never be modified in any way.
     *
     * @return The component's new snapshot or an updated cached
     * snapshot.
     */
    final public ComponentSnapshot<Type, State> getSnapshot() {
        if (isModified() || mSnapshot == null) {
            mSnapshot = buildSnapshot();
            clearModified();
        }

        return mSnapshot;
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
     * @return {@code true} if component has been removed
     * from the model {@code false} if not.
     */
    public boolean isRemoved() {
        return mRemoved;
    }

    /**
     * Called to set the removed flag when this component
     * has been removed from the model.
     *
     * @param removed {@code true} if component has been removed
     *                from the model {@code false} if not.
     */

    public void setRemoved(boolean removed) {
        this.mRemoved = removed;
    }

    /**
     * Used to increment the dirty count each time setState is
     * called or to clear after a snapshot is generated.
     */
    public void setModified() {
        mModified.set(true);
    }

    /**
     * Used to clear the dirty count.
     */
    public void clearModified() {
        mModified.set(false);
    }

    /**
     * @return {@code true} if the palantir has been changed since
     * the last snapshot, {@code false} if not.
     */
    public boolean isModified() {
        return mModified.get();
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
                ", modified=" + isModified() +
                ", removed=" + isRemoved() +
                (getException() != null ? ", e=" + getException() : "") +
                (getMessage() != null ? ", msg='" + getMessage() + "'" : "") +
                '}';
    }
}

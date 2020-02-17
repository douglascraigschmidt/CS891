package edu.vandy.simulator.model.base;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

import edu.vandy.simulator.model.interfaces.ComponentSnapshot;
import edu.vandy.simulator.model.interfaces.ModelComponent;

/**
 * Base class for all component immutable snapshots that factor
 * out all the fields and their accessors. The primary purpose
 * of this base class is to reduce Java boiler-plate code.
 * <p>
 * An snapshot id provider is used to allocate a new unique
 * snapshot id for each snapshot that is created by an call
 * to a SnapshotProvider's {@link ModelComponent#buildSnapshot}
 * method. This is extremely useful for the following reasons:
 * <pre>
 * <ul>
 * <li>
 * Allows presentation layer to determine if a component snapshot
 * has changed since the last snapshot submission without having
 * to check individual snapshot fields.
 * </li>
 * <li>
 * Allows the model framework to avoid unnecessary memory
 * allocations for duplicated component snapshots when pushing
 * the model to the presentation layer. If the model implementation
 * chooses to keep the last snapshot of each component, if the
 * component state has not changed since the last push, the
 * previous snapshot can be resent (this is safe since all
 * snapshots are immutable).
 * </li>
 * <li>
 * The snapshot id's provide a history of model changes which
 * can be leveraged however any model implementation sees fit.
 * </li>
 * </ul>
 * </pre>
 */
abstract public class BaseSnapshot<Type, State>
        implements ComponentSnapshot<Type, State> {
    /**
     * A static id generator.
     */
    private static final AtomicInteger sIdProvider = new AtomicInteger(0);

    /**
     * The unique id of this snapshot.
     */
    private final int mSnapshotId = sIdProvider.getAndIncrement();

    /**
     * Component id. The components of each component type should
     * be allocated a unique identifying id (different from the
     * system wide snapshot id).
     */
    private final long mId;

    /**
     * The component type.
     */
    private final Type mType;

    /**
     * The state of the component when this snapshot was genereated.
     */
    private final State mState;

    /**
     * The state of the component when this snapshot was genereated.
     */
    private final State mPrevState;

    /**
     * Creation time set by calling System.currentTimeMillis();
     */
    private final long mTimestamp;

    /**
     * An optional throwable if the component has
     * encountered and exception.
     */
    private final Throwable mThrowable;

    /**
     * An optional message provides information about the state.
     */
    private final String mMessage;

    /**
     * To support a empty snapshot (avoids nulls).
     */
    public BaseSnapshot() {
        mId = -1;
        mType = null;
        mState = null;
        mPrevState = null;
        mThrowable = null;
        mMessage = null;
        mTimestamp = System.currentTimeMillis();
    }

    public BaseSnapshot(@NotNull ModelComponent<Type, State> component) {
        mId = component.getId();
        mType = component.getType();
        mState = component.getState();
        mPrevState = component.getPrevState();
        mThrowable = component.getException();
        mMessage = component.getMessage();
        mTimestamp = System.currentTimeMillis();
    }

    /**
     * Constructor for clone support.
     *
     * @param snapshot Instance to clone.
     */
    public BaseSnapshot(@NotNull BaseSnapshot<Type, State> snapshot) {
        mId = snapshot.getId();
        mType = snapshot.getType();
        mState = snapshot.getState();
        mPrevState = snapshot.getPrevState();
        mThrowable = snapshot.getException();
        mMessage = snapshot.getMessage();
        mTimestamp = System.currentTimeMillis();
    }

    /**
     * Resets the unique snapshot history id generator to 0.
     */
    static void resetIdProvider() {
        sIdProvider.set(0);
    }

    /**
     * @return A unique component id.
     */
    @Override
    public long getId() {
        return mId;
    }

    /**
     * @return Time when the snapshot was created.
     */
    @Override
    public long getTimestamp() {
        return mTimestamp;
    }

    /**
     * @return The component type.
     */
    @Override
    public Type getType() {
        return mType;
    }

    /**
     * @return Optional exception if state includes an
     * encountered an exception.
     */
    @Nullable
    @Override
    public Throwable getException() {
        return mThrowable;
    }

    /**
     * @return Optional message that provides additional
     * information about the state.
     */
    @Nullable
    @Override
    public String getMessage() {
        return mMessage;
    }

    /**
     * @return The state of the component at the time of the
     * snapshot was captured.
     */
    @Override
    public State getState() {
        return mState;
    }

    /**
     * @return The previous state of the component at the time
     * of the snapshot was captured.
     */
    @Override
    public State getPrevState() {
        return mPrevState;
    }

    /**
     * @return Returns the number of milliseconds that have elapsed from
     * when this snapshot was first created.
     */
    public long getElapsedTime() {
        return System.currentTimeMillis() - getTimestamp();
    }

    /**
     * @return The current unique snapshot id.
     */
    public long getSnapshotId() {
        return mSnapshotId;
    }

    @Override
    public String toString() {
        return "Snapshot[" + getSnapshotId() + "]: " + getType() +
                "[" + getId() + " : " + getTimestamp() + "]" +
                (getPrevState() != null ? (" " + getPrevState() + " -> ") : "") +
                getState() +
                (getException() != null ? " e=" + getException() : "") +
                (getMessage() != null ? " msg='" + getMessage() + "'" : "");
    }
}


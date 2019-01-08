package edu.vandy.simulator.model.base;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import edu.vandy.simulator.Controller;
import edu.vandy.simulator.model.implementation.snapshots.ModelSnapshot;
import edu.vandy.simulator.model.interfaces.ComponentSnapshot;
import edu.vandy.simulator.model.interfaces.Model;
import edu.vandy.simulator.model.interfaces.ModelComponent;
import edu.vandy.simulator.model.interfaces.ModelObserver;
import edu.vandy.simulator.model.interfaces.ModelProvider;

/**
 * A top level model base class that manages a list of model
 * observers and also provides the default implementation of
 * the {@link Model#triggerSnapshot} method which is invoked when
 * any model component calls {@link #setState}. When invoked,
 * this method calls each component's {@link #buildSnapshot}
 * method to obtain an immutable snapshot of that components
 * current state. These snapshots are then combined into a
 * single immutable {@link ComponentSnapshot} that is then
 * sent to all registered {@link ModelObserver}s
 * <p>
 * Note that this abstract class is not only a top level Model
 * implementation, but is also a
 * {@link edu.vandy.simulator.model.interfaces.ModelComponent}
 * that has state and will produce a top level snapshot when
 * it's {@link #buildSnapshot} method is invoked.
 */
public abstract class BaseModel<Type, State>
        extends BaseComponent<Type, State>
        implements Model {
    /**
     * Optional list of Model observers.
     */
    private final List<WeakReference<ModelObserver>> mObservers
            = new ArrayList<>();

    /**
     * Construct a fair re-entrant read/write lock to
     * control concurrent access to the list of model
     * observers.
     */
    private final ReentrantReadWriteLock mObserversLock
            = new ReentrantReadWriteLock(true);

    /**
     * Constructor for capturing a back reference to the
     * enclosing model class.
     *
     * @param type     The component type.
     * @param state    The initial state (to avoid nulls).
     * @param provider An ModelProvider implementation class
     *                 that provides access to the simulation
     *                 Model instance.
     */
    public BaseModel(Type type, State state, ModelProvider provider) {
        super(type, state, 0, provider);
        // Reset ModelSnapshot static history id provider to 0.
        BaseSnapshot.resetIdProvider();
    }

    /**
     * Adds an new {@link ModelObserver} to the list of
     * observers. The observer will be notified of all
     * model state changes.
     *
     * @param observer An implementation of the
     *                 {@link ModelObserver} interface.
     * @param notify   If true observer will immediately receive
     *                 a current snapshot of the model state.
     */
    @Override
    public void addObserver(ModelObserver observer, boolean notify) {

        // Acquire write lock for updating observers list.
        mObserversLock.writeLock().lock();

        try {
            // Remove this observer if already in the list.
            removeObserver(observer);

            // Add the observer to the list.
            mObservers.add(new WeakReference<>(observer));
        } finally {
            // Release the write lock.
            mObserversLock.writeLock().unlock();
        }

        // Immediately notify observer about all cache contents.
        if (notify) {
            triggerSnapshot(this);
        }
    }

    /**
     * Removes the specified {@link ModelObserver}
     * from the list of observers.
     *
     * @param observer An implementation of the
     *                 {@link ModelObserver} interface.
     */
    public void removeObserver(ModelObserver observer) {
        // Acquire write access to observers list.
        mObserversLock.writeLock().lock();

        try {
            // Remove observer from the list of observers.
            // We can't simply list.remove because the passed
            // observer is wrapped in an ObserverEntry which
            // contains a WeakReference to this observer.
            mObservers.stream()
                    .filter(entry -> entry == observer)
                    .findFirst()
                    .ifPresent(mObservers::remove);
        } finally {
            // Release write lock.
            mObserversLock.writeLock().unlock();
        }
    }

    /**
     * Updates the currently cached model snapshot with the
     * snapshot of the passed component and then broadcasts
     * this updated model snapshot to all registered model
     * observers.
     *
     * NOTE: the above comment is outdated because a new fresh
     * snapshot is created by updateModelSnapshot which no longer
     * updates a cached snapshot. The comment is being kept
     * until I'm sure that the underlying snapshot generation
     * algorithm does not have any concurrency side-effects.
     *
     * @param component Component that is triggering this snapshot.
     */
    @Override
    public void triggerSnapshot(ModelComponent component) {
        broadcastSnapshot(updateModelSnapshot(component));
    }

    /**
     * Broadcasts a snapshot of the current model state that
     * to all registered snapshot observers.
     *
     * @param snapshot Snapshot to broadcast.
     */
    protected void broadcastSnapshot(ModelSnapshot snapshot) {
        // If an observer is registered, then construct an immutable
        // snapshot of the model state and push it to presentation layer.
        if (!mObservers.isEmpty()) {
            // Acquire read access to the observers list.
            mObserversLock.readLock().lock();

            Controller.log("Sending snapshot: " + snapshot);

            try {
                // Notify observers. Since snapshots are immutable
                // a single snapshot can be safely sent to multiple
                // observers.
                mObservers.forEach(observerRef -> {
                    ModelObserver observer = observerRef.get();
                    if (observer != null) {
                        observer.onModelChanged(snapshot);
                    }
                });
            } finally {
                // Release the read lock.
                mObserversLock.readLock().unlock();
            }
        }
    }

    abstract public ModelSnapshot buildModelSnapshot(ModelComponent component);

    abstract public ModelSnapshot updateModelSnapshot(ModelComponent component);
}

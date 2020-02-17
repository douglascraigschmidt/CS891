package edu.vandy.simulator.managers.beings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import edu.vandy.simulator.Simulator;
import edu.vandy.simulator.managers.palantiri.Palantir;
import edu.vandy.simulator.model.interfaces.Model;
import edu.vandy.simulator.model.interfaces.ModelProvider;

import static java.util.stream.Collectors.toList;

/**
 * Abstract class that builds and the Model's list of Beings and
 * routes requests from the base class implementation to acquire and
 * release Palantiri to the simulator which will route those request
 * down to the PalantiriManager implementation.  this class also
 * contains a factory for generating different BeingManager
 * implementation strategies.
 */
public abstract class BeingManager<T extends Being>
        implements ModelProvider {
    /**
     * Logging tag.
     */
    private static final String TAG = "BeingManager";
    /**
     * A back reference to the controlling Simulator instance that
     * manages all request routing as well as error handling.
     */
    public Simulator mSimulator;
    /**
     * The maximum time to wait when asking beings to shutdown.
     */
    private int MAX_WAIT = 5000;
    /**
     * The number of threads to use in the simulation.
     */
    public int mThreadCount;
    /**
     * The model parameters.
     */
    private int mGazingIterations;

    /**
     * The list of Beings that will run concurrently in a
     * parallel stream.
     */
    @NotNull
    public List<T> mBeings = new ArrayList<>();

    /**
     * Zero parameter constructor required for Factory creation.
     */
    public BeingManager() {
    }

    /**
     * Called to create and return a new concrete Being instance.
     *
     * @return A new Being concrete instance.
     */
    public abstract T newBeing();

    /**
     * Called to run the simulation.
     */
    public abstract void runSimulation();

    /**
     * Called to run to error the simulation and
     * should only return after all threads have been
     * terminated and all resources cleaned up.
     */
    public abstract void shutdownNow();

    /**
     * Builds a new simulation model with the specified model parameters.
     *
     * @param simulator        The Simulator that routes requests between Model Managers.
     * @param beingCount       The number of Beings.
     * @param gazingIterations The number of Being gazing iterations.
     */
    final void buildModel(Simulator simulator, int beingCount, int threadCount, int gazingIterations) {
        // Save the controller simulator instance.
        mSimulator = simulator;

        // Save the model's gazing parameter.
        mGazingIterations = gazingIterations;

        // Initialize the Beings (no need to save being count
        // since it's implicit in the mBeings size).
        mBeings = makeBeings(beingCount);

        mThreadCount = threadCount;
    }

    /**
     * Returns the Being that matches has the specified id.  Note that
     * this call is required to succeed.
     *
     * @param id Being id to search for.
     * @return The Being that has the specified id.
     */
    final public T getBeing(long id) {
        return getBeings()
                .stream()
                .filter(being -> being.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * @return The number per being gazing iterations in this model.
     */
    final public int getGazingIterations() {
        return mGazingIterations;
    }

    /**
     * @return The number of beings in this model.
     */
    final public int getBeingCount() {
        return getBeings().size();
    }

    /**
     * @return The number of threads to use in the simulation.
     */
    final public int getThreadCount() {
        return mThreadCount > 0 ? mThreadCount : getBeingCount();
    }

    /**
     * @return The list of beings in this model.
     */
    @NotNull
    public List<T> getBeings() {
        return mBeings;
    }

    /**
     * @return Forwards the model request to the Simulator to handle.
     */
    @Override
    final public @NotNull
    Model getModel() {
        return mSimulator.getModel();
    }

    /**
     * Create the specified number of beings that will be used
     * in the simulation.
     *
     * @param count The number of Beings to create.
     */
    public List<T> makeBeings(int count) {
        // Reset Being static id generator.
        Being.resetIds();

        // Create a list of Beings of size beingCount that
        // will be run concurrently in a parallel stream.
        return Stream
                // Generate beingCount number of Beings.
                .generate(this::newBeing)

                // Limit the number of beings to
                // the specified count value.
                .limit(count)

                // Return a list of Beings.
                .collect(toList());
    }

    /**
     * Called when an unrecoverable error occurs. The error
     * is passed up the chain to the simulator to deal with.
     *
     * @param msg An optional message describing the error.
     */
    public void error(@Nullable String msg) {
        mSimulator.throwError(msg);
    }

    /**
     * Called when an unrecoverable error occurs. The error
     * is passed up the chain to the manager to deal with.
     *
     * @param throwable An exception that caused the error.
     */
    final public void error(Throwable throwable) throws IllegalStateException {
        mSimulator.throwError(throwable);
    }

    /**
     * Forwards error request to the controlling simulator.
     * BeingManager concrete subclasses should override this
     * method to cleanly error all being threads and release
     * any allocated resources.
     */
    public void shutdown() {
        log("shutdown: called.");

        // Call helper method to ask all beings to shutdown.  Passing
        // false to this method will ensure that beings are politely
        // requested to shutdown.  If not all beings are cancelled
        // within MAX_WAIT time this method will return false.
        if (!shutdownBeings(false)) {
            // Some stubborn beings are not being responsive to a
            // polite shutdown request, so we now bring out the big
            // guns and pass true which signals the methods to
            // forcefully interrupt all beings.  This call will also
            // wait for MAX_WAIT seconds to ensure that all beings are
            // shutdown.
            log("shutdown: Beings didn't respond to shutdown request!");
            shutdownBeings(true);
        }

        // Now that all beings have been shutdown, or, even if the two
        // calls failed, tell the BeingManager implementation to
        // shutdown. The manager should assume that it's beings are no
        // longer running and only need concern itself with freeing up
        // resources. A subsequent call to reset() may be issued at a
        // later time, in which case, the manager should reset fields
        // to a state that allows re-running the simulation using the
        // same model parameters and being instances that were used in
        // the previous run.
        shutdownNow();

        log("shutdown: completed.");
    }

    /**
     * Attempts to shutdown all beings. If interrupt is {@code false}
     * the beings are politely request to shutdown by calling their
     * shutdownNow method that simply sets a cancelled flag that they
     * should be monitoring at a relatively fine-grained interval.  If
     * interrupt is {@code true}, then each being is forcefully
     * interrupted. In either case, this method will wat for MAX_WAIT
     * milliseconds to ensure that all the beings were properly
     * shutdown.
     *
     * @param interrupt if {@code false} beings are only requested to
     *                  shutdown and if {@code true} beings are shutdown
     *                  by forcing an interrupt on their running thread.
     * @return {@code true} if all beings were shutdown and {@code false}
     * if not all beings were shutdown.
     */
    protected boolean shutdownBeings(boolean interrupt) {
        // Start off with the the number of running beings.
        if (getRunningBeingCount() == 0) {
            // Nothing to do if there are no beings running.
            return true;
        }

        try {
            if (interrupt) {
                // Force each being to shutdown.
                getBeings().forEach(being -> {
                    if (!being.isCancelled()) {
                        if (!being.interruptNow()) {
                            log("Forced interrupt " +
                                    "failed for " + being);
                        }
                    }
                });
            } else {
                // Request each being to shutdown.
                getBeings().forEach(being -> {
                    if (!being.isCancelled()) {
                        being.shutdownNow();
                    }
                });
            }

            // Now wait for MAX_WAIT milliseconds for all threads
            // to shutdown.
            int interval = 50;
            int waited = 0;
            while (getRunningBeingCount() > 0 && waited < MAX_WAIT) {
                waited += interval;
                Thread.sleep(interval);
            }

            // Return true if all beings have shutdown and false if not.
            return getRunningBeingCount() == 0;
        } catch (Exception e) {
            log("shutdownBeings encountered exception: " + e);
            e.printStackTrace();
            // Rethrow.
            throw new IllegalStateException(e);
        } finally {
            log("shutdownBeings: completed with "
                    + getRunningBeingCount()
                    + "/" + getBeingCount() + " running beings.");
        }
    }

    /**
     * Resets the fields to their initial values
     * and tells all beings to reset themselves.
     * <p>
     * Override this class if the being manager
     * implementation has it's own fields or
     * state to reset.
     */
    public void reset() {
        // Just reset beings (no fields to reset).
        mBeings.forEach(Being::reset);
    }

    /**
     * Called to ensure that beings are in a suitable state to be rerun.
     * Subclasses should handle their own sanity checks and then call this
     * super class methods for the default checks.
     */
    public void validateStartState() {
        for (T being : mBeings) {
            int running = 0;
            int palantir = 0;
            int cancelled = 0;

            if (being.isRunning()) {
                running++;
                log("validateStartState: " + being + " in cancelled state!");
            }
            if (being.getPalantirId() != -1L) {
                palantir++;
                log("validateStartState: " + being + " has an associated Palantitr!");
            }
            if (being.isCancelled()) {
                log("validateStartState: " + being + " in still in a cancelled state!");
                cancelled++;
            }

            if (running + palantir + cancelled != 0) {
                throw new IllegalStateException(TAG + ": invalid start state!!!");
            }
        }
    }

    /**
     * Called by a Being to acquire a Palantir resource.
     * The request is routed to the simulator which will,
     * in turn, route the request to the PalantiriManager.
     *
     * @param being The Being.
     * @return The next available Palantir.
     */
    public Palantir acquirePalantir(Being being) {
        return mSimulator.acquirePalantir(being);
    }

    /**
     * Called by a Being to release a previously acquired Palantir
     * resource. The request is routed to the simulator which will,
     * in turn, route the request to the PalantiriManager.
     *
     * @param being    The Being that is releasing the Palantir.
     * @param palantir The Palantir resource to release.
     */
    public void releasePalantir(Being being, Palantir palantir) {
        mSimulator.releasePalantir(being, palantir);
    }

    /**
     * @return Asks controlling Simulator if there is a pending
     * error request.
     */
    final public boolean isShutdown() {
        return mSimulator.isShutdown();
    }

    /**
     * @return The number of currently running beings.
     */
    final public int getRunningBeingCount() {
        return (int) getBeings()
                .stream()
                .filter(Being::isRunning)
                .count();
    }

    /**
     * Routes all non-fatal warning messages up the chain.
     *
     * @param message A non-fatal warning message.
     */
    final void warn(String message) {
        mSimulator.warn(message);
    }

    /**
     * Use println instead of Android Log class so that unit tests
     * will not require mocking Android Log class.
     *
     * @param msg Any string.
     */
    private void log(String msg) {
        System.out.println(TAG + " - " + msg);
    }

    /**
     * Java utility class used to create new instances of
     * BeingManagers.
     */
    final public static class Factory {
        /**
         * Disallow object creation.
         */
        private Factory() {
        }

        /**
         * Creates the specified {@code type} simulator.
         *
         * @param type        Type of simulator.
         * @param threadCount
         * @return A transform instance of the specified type.
         */
        public static BeingManager newManager(Type type,
                                              int beingCount,
                                              int threadCount,
                                              int gazingIterations,
                                              Simulator simulator) {
            try {
                BeingManager manager = type.newInstance();
                manager.buildModel(simulator, beingCount, threadCount, gazingIterations);
                return manager;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        /**
         * Supported being managers which are loaded via reflection.
         */
        public enum Type {
            RUNNABLE_THREADS("runnableThreads.RunnableThreadsMgr"),
            EXECUTOR_SERVICE("executorService.ExecutorServiceMgr"),
            COMPLETION_SERVICE("completionService.ExecutorCompletionServiceMgr"),
            ASYNC_TASK("asyncTask.AsyncTaskMgr"),
            PARALLEL_STREAMS("parallelStreams.ParallelStreamsMgr"),
            COMPLETABLE_FUTURES("completableFutures.CompletableFuturesMgr"),
            STRUCTURED_CONCURRENCY("structuredConcurrency.CoroutineMgr");

            public final String className;

            Type(String className) {
                this.className = className;
            }

            public boolean isSupported() {
                return getManagerClass() != null;
            }

            public String getCanonicalName() {
                return "edu.vandy.simulator.managers.beings." + className;
            }

            private Class<?> getManagerClass() {
                try {
                    // Create a new JavaClassLoader
                    Class<?> clazz = getClass();

                    // Load the target class using its binary name
                    return clazz.getClassLoader().loadClass(getCanonicalName());
                } catch (Exception e) {
                    return null;
                }
            }

            public BeingManager newInstance() {
                try {
                    // Load the target class using its binary name
                    Class clazz = getManagerClass();
                    if (clazz == null) {
                        throw new IllegalStateException(
                                "newInstance should only be called for a supported BeingManager type");
                    }

                    System.out.println("Loaded class name: " + clazz.getName());

                    // Create a new instance from the loaded class
                    Constructor constructor = clazz.getConstructor();
                    Object crawler = constructor.newInstance();
                    return (BeingManager) crawler;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

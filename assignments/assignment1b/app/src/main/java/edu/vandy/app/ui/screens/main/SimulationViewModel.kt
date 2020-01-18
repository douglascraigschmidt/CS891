package edu.vandy.app.ui.screens.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.annotation.MainThread
import edu.vandy.app.utils.Range
import edu.vandy.app.extensions.scale
import edu.vandy.app.ui.screens.settings.Settings
import edu.vandy.app.utils.KtLogger
import edu.vandy.app.utils.info
import edu.vandy.app.utils.warn
import edu.vandy.simulator.Controller
import edu.vandy.simulator.Simulator
import edu.vandy.simulator.managers.beings.BeingManager
import edu.vandy.simulator.managers.palantiri.PalantiriManager
import edu.vandy.simulator.model.implementation.snapshots.ModelSnapshot
import edu.vandy.simulator.model.interfaces.ModelObserver
import org.jetbrains.anko.doAsync

class SimulationViewModel : ViewModel(), ModelObserver, KtLogger {
    /** Simulation model state live data feed. */
    private val modelStateFeed = MutableLiveData<ModelSnapshot>()

    /** Simulator instance (exposed for instrumented tests). */
    var simulator: Simulator? = null

    /**
     * Prevents repeated start and stop calls.
     * Currently supports starting/stopping
     * from the main thread only.
     */
    private @Volatile
    var startingSimulation = false
    private @Volatile
    var stoppingSimulation = false

    /** Simulation start time. */
    private var startTime = 0L
    private var stopTime = 0L

    /** Keeps track of the number of simulation runs */
    var simulationCount = 0

    /**
     * Elapsed time for the currently running simulation
     * or the last run simulation if not currently running
     */
    val elapsedTime
        get() = if (startTime != 0L) {
            if (stopTime != 0L) {
                stopTime - startTime
            } else {
                System.currentTimeMillis() - startTime
            }
        } else {
            0L
        }

    /**
     * Directly updates Controller for real-time
     * animation speed adjustment.
     */
    var simulationSpeed: Int = 0
        set(value) {
            field = value
            Controller.setSimulationSpeed(value / 100f)
        }

    var setPerformanceMode: Boolean = false
        set(value) {
            field = value
            Controller.setPerformanceMode(value)
        }

    /**
     * Directly updates Controller for real-time
     * logging enabling/disabling.
     */
    var logging: Boolean = true
        set(value) {
            field = value
            Controller.setLogging(value)
        }

    /**
     * Directly updates Controller for real-time
     * gazing time adjustment.
     */
    var gazingTimeRange: Range<Int> = Range(1, 4) // Arbitrary value
        set(value) {
            field = value
            val scaledRange = value.scale(1000f)
            Controller.setGazingTimeRange(
                    scaledRange.lower, scaledRange.upper)
        }

    val simulationRunning: Boolean
        get() = simulator?.isRunning ?: false

    /**
     * Used to keep track of current model parameters.
     */
    data class ModelParameters(val beingManagerType: BeingManager.Factory.Type,
                               val palantirManagerType: PalantiriManager.Factory.Type,
                               val beings: Int,
                               val palantiri: Int,
                               val iterations: Int)

    /**
     * Initially set to assignment 1a strategies.
     */
    var modelParameters = ModelParameters(BeingManager.Factory.Type.RUNNABLE_THREADS,
                                          PalantiriManager.Factory.Type.ARRAY_BLOCKING_QUEUE,
                                          0,
                                          0,
                                          0)

    fun subscribe(lifecycleOwner: MainActivity,
                  modelStateObserver: Observer<ModelSnapshot>,
                  block: (() -> Unit)? = null) {
        modelStateFeed.observe(lifecycleOwner, modelStateObserver)
        block?.invoke()
    }

    /**
     * Asynchronously starts a simulation run using the specified
     * model parameters.
     *
     * This function is safe to call while a simulation is
     * already running because it uses a volatile flag to
     * prevent spawning multiple async start simulation tasks.
     */
    @MainThread
    fun startSimulationAsync(beingManagerType: BeingManager.Factory.Type,
                             palantirManagerType: PalantiriManager.Factory.Type,
                             beings: Int,
                             palantiri: Int,
                             iterations: Int) {
        if (startingSimulation) {
            // Ignore repeated clicks that cause stacked up start calls
            // This causes some kind of thread deadlock with the doAsync() call
            // below which won't allocate a new Async thread if enough clicks
            // occur in succession (probably 8).
            return
        }

        try {
            // Set guard flag.
            startingSimulation = true

            // Keep track of how many times start has been called.
            simulationCount++

            // Shouldn't be running, but sometimes the start stop
            // logic has edge cases so to be safe, force any running
            // simulation to stop.
            simulator?.stop()

            Controller.setLogging(Settings.logging)
            val gazingRange = Settings.gazingDuration.scale(1000f)
            Controller.setGazingTimeRange(gazingRange.lower, gazingRange.upper)
            Controller.setSimulationSpeed(Settings.animationSpeed / 100f)

            try {
                // For efficiency reasons, don't update the model if
                // it hasn't changed from the last time it was built.
                updateSimulationModel(beingManagerType,
                                      palantirManagerType,
                                      beings,
                                      palantiri,
                                      iterations)
            } catch (e: Exception) {
                error("SimulationViewModel: Unable to update simulation model: $e")
            }

            doAsync {
                try {
                    log("SimulationViewModel: Starting simulation ...")

                    // Reset timer values.
                    stopTime = 0
                    startTime = System.currentTimeMillis()

                    // Run the simulation.
                    simulator?.start()

                    warn("SimulationViewModel: Simulation completed normally.")
                } catch (e: Exception) {
                    warn("SimulationViewModel: Simulation completed with exception: $e")
                } finally {
                    // Set the simulation stop time.
                    stopTime = System.currentTimeMillis()

                    if (simulationRunning) {
                        error("SimulationViewModel: Simulation should not be running at this point.")
                    }
                }
            }
        } finally {
            startingSimulation = false
        }
    }

    /**
     * Asynchronous call so that if the simulation is slow
     * in stopping, it won't hang the UI layer.
     *
     * This function is safe to call before a previous call
     * has completed because it uses a volatile flag to
     * prevent spawning multiple async stop tasks.
     */
    @MainThread
    fun stopSimulationAsync() {
        if (!stoppingSimulation) {
            doAsync {
                stoppingSimulation = true
                simulator?.stop()
                stoppingSimulation = false
            }
        }
    }

    /**
     * Updates the current simulation model parameters.
     */
    @MainThread
    fun updateSimulationModel(beingManagerType: BeingManager.Factory.Type,
                              palantirManagerType: PalantiriManager.Factory.Type,
                              beingCount: Int,
                              palantirCount: Int,
                              gazingIterationCount: Int) {
        if (!simulationRunning) {
            if (simulator == null) {
                simulator = Simulator(this)
            }

            simulator?.apply {
                val parameters =
                        ModelParameters(beingManagerType,
                                        palantirManagerType,
                                        beingCount,
                                        palantirCount,
                                        gazingIterationCount)

                if (parameters != modelParameters) {
                    modelParameters = parameters
                    buildModel(beingManagerType,
                               palantirManagerType,
                               beingCount,
                               palantirCount,
                               gazingIterationCount)
                }
            }
        }
    }

    /**
     * Called when the model state has changed and those changes
     * need to be reflected in the presentation layer. The passed
     * snapshot is a complete description of the entire model and
     * therefore may be considered as the ground-truth of the model
     * state.
     *
     * @param snapshot An immutable snapshot of the model state.
     */
    override fun onModelChanged(snapshot: ModelSnapshot) {
        //info(snapshot.simulator)
        modelStateFeed.postValue(snapshot)
    }

    private fun log(msg: String) {
        if (Controller.getLogging()) {
            info(msg)
        }
    }
}

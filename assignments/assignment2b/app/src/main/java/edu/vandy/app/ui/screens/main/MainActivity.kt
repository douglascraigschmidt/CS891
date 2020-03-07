package edu.vandy.app.ui.screens.main

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import edu.vandy.R
import edu.vandy.app.extensions.*
import edu.vandy.app.preferences.CompositeUnsubscriber
import edu.vandy.app.preferences.Preference
import edu.vandy.app.preferences.PreferenceProvider
import edu.vandy.app.ui.screens.settings.Settings
import edu.vandy.app.ui.screens.settings.SettingsDialogFragment
import edu.vandy.app.ui.widgets.ToolbarManager
import edu.vandy.app.utils.KtLogger
import edu.vandy.app.utils.info
import edu.vandy.app.utils.warn
import edu.vandy.simulator.Controller
import edu.vandy.simulator.model.implementation.components.SimulatorComponent
import edu.vandy.simulator.model.implementation.snapshots.ModelSnapshot
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.simulation_view.*
import kotlinx.android.synthetic.main.toolbar.*
import org.jetbrains.anko.contentView
import org.jetbrains.anko.find
import org.jetbrains.anko.longToast
import java.util.*
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity(),
        ToolbarManager,
        KtLogger,
        SharedPreferences.OnSharedPreferenceChangeListener {

    /** toolbar is maintained in ToolbarManager. */
    override val toolbar by lazy { find<Toolbar>(R.id.toolbar) }

    /** View model manages LiveData (exposed for Instrumented tests). */
    lateinit var viewModel: SimulationViewModel

    /** Watch preference changes for simulation speed. */
    private val compositeUnsubscriber = CompositeUnsubscriber()

    /**
     * Preference used to ensure that settings drawer
     * is only peeked once when the app starts.
     */
    private var peekDrawer: Boolean by Preference(true)

    /**
     * The state of the underlying model when the last
     * model snapshot was received.
     */
    private var simulationState: SimulatorComponent.State? = null

    /** The total number of gazing iterations completed. */
    private var completedIterations: Int = 0

    /** Keeps track of simulation time. */
    private var simulationTimer: Timer? = null

    /**
     * Setup all widgets and initialize the simulation model.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Connect or reconnect to view model.
        viewModel = ViewModelProvider(this).get(SimulationViewModel::class.java)

        setContentView(R.layout.activity_main)
        initToolbar()
        setSupportActionBar(toolbar)

        // Hide FAB so that it can be animated into view.
        progressBar.visibility = View.INVISIBLE

        initializeViews()
        updateViewStates()

        // Only peek drawer when a session first starts.
        if (savedInstanceState == null) {
            peekDrawer = true
        }

        // First time in - setup model parameters.
        if (savedInstanceState == null) {
            updateSimulationModel()
        }

        PreferenceProvider.addListener(this)
    }

    /**
     * Initializes all views when the activity is created.
     */
    private fun initializeViews() {
        // Search FAB has 3 responsibilities
        // 1. Search: when local crawl setting and a root url has not been chosen.
        // 2. Start: when local mode is true or a root url has been chosen.
        // 3. Stop: when the crawler is running.
        progressFab.setOnClickListener {
            // These calls drill down into simulator so protect
            // main thread from abnormal termination because of
            // mistakes in assignment implementations.
            if (viewModel.simulationRunning) {
                try {
                    viewModel.stopSimulationAsync()
                } catch (e: Exception) {
                    logException("An exception occurred when " +
                            "trying to stop the simulation: $e", e)
                }
            } else {
                try {
                    viewModel.setPerformanceMode = Settings.performanceMode
                    viewModel.startSimulationAsync(
                            Settings.beingManagerType,
                            Settings.palantirManagerType,
                            Settings.beingCount,
                            Settings.palantirCount,
                            Settings.threadCount,
                            Settings.gazingIterations)

                    // Now that the simulation is running, start an
                    // asynchronous thread to periodically update
                    // the title bar performance timer.
                    startTimerAsync()

                    // Enable/disable model parameter settings if
                    // we've reconnected to a running simulation.
                    (settingsFragment as SettingsDialogFragment)
                            .simulationRunning(true)
                } catch (e: Exception) {
                    logException("An exception occurred when " +
                            "trying to start the simulation: $e", e)
                }
            }

            updateViewStates()
        }
    }

    /**
     * Simple background thread that periodically updates
     * the title bar to show the simulations performance timer.
     */
    private fun startTimerAsync() {
        simulationTimer = timer("PerformanceTimer", false, 100L, 100L) {
            if (viewModel.simulationRunning) {
                contentView?.post { updateTitle() }
            } else {
                contentView?.post { updateTitle() }
                cancel()
            }
        }
    }

    /**
     * Helper that updates a composite title containing crawl run information.
     */
    private fun updateTitle() {
        title = ""
        simulationStats.visible = true

        val beingCount = Settings.beingCount
        val iterations = Settings.gazingIterations
        val total = beingCount * iterations
        val threadCount: Int = viewModel.threadCount

        iterationsValue.text = iterations.toString()
        beingsValue.text = beingCount.toString()
        palantiriValue.text = Settings.palantirCount.toString()
        threadsValue.text = threadCount.toString()

        completedIterationsValue.text =
                String.format("%d/%d", completedIterations, total)

        val color =
                if (completedIterations == total) {
                    Color.GREEN
                } else if (viewModel.simulationCount == 0 ||
                        viewModel.simulationRunning) {
                    ContextCompat.getColor(
                            this, R.color.secondaryLightColor)
                } else {
                    Color.RED
                }

        beingsLabel.textColor = color
        palantiriLabel.textColor = color
        iterationsLabel.textColor = color
        completedIterationsLabel.textColor = color
        threadsLabel.textColor = color
    }

    /**
     * Called by Shared Preference observers to dynamically change
     * the model parameters when the user adjust those parameters
     * in the Settings drawer dialog.
     */
    private fun updateSimulationModel() {
        // Ensure that the current settings will be able to
        // display at the current resolution. If now this call
        // will reset the number of beings or palantiri in the
        // settings panel. The user will have to try again
        if (!viewModel.simulationRunning) {
            try {
                viewModel.updateSimulationModel(Settings.beingManagerType,
                        Settings.palantirManagerType,
                        Settings.beingCount,
                        Settings.palantirCount,
                        Settings.threadCount,
                        Settings.gazingIterations)
            } catch (e: Exception) {
                logException("An exception occurred while trying " +
                        "to initialize the simulation model: $e", e)
            }
        } else {
            shortSnack("Simulation is running. Your change will " +
                    "be applied when the simulation completes.") {
            }
        }
    }

    /**
     * Subscribe or resubscribe to the view model when
     * the activity is resumed.
     */
    override fun onResume() {
        super.onResume()

        // Start subscribing to live item.
        contentView?.runAfterLayout {
            return@runAfterLayout if (simulationView.realized) {
                subscribeViewModel()
                true
            } else {
                false
            }
        }

        // Animate Fab into view if not already visible.
        if (!progressFab.visible) {
            postDelayed(1000L) {
                showFab(true)
            }
        }

        if (peekDrawer && !drawerLayout.isDrawerOpen(GravityCompat.END)) {
            postDelayed(500) {
                drawerLayout.peekDrawer()
                peekDrawer = false
            }
        }
    }

    /**
     * Unsubscribe all shared preference Observers.
     */
    override fun onDestroy() {
        PreferenceProvider.removeListener(this)
        compositeUnsubscriber.invoke()
        super.onDestroy()
    }

    /**
     * Helper method to force showing or hiding the FAB.
     */
    private fun showFab(show: Boolean, run: (() -> Unit)? = null) {
        if (show != progressFab.isShown) {
            if (show) {
                progressFab.show(run)
            } else {
                progressFab.hide(run)
            }
        }
    }

    /**
     * Subscribes to the view model and sets up LiveData observer
     * to monitor all view state changes (snapshots).
     */
    private fun subscribeViewModel() {
        viewModel.subscribe(this, Observer { handleModelStateChange(it!!) })
    }

    /**
     * Process model snapshot received from a LiveData update event.
     */
    private fun handleModelStateChange(snapshot: ModelSnapshot) {
//        log("MainActivity: Received model"
//            + " snapshot id = " + snapshot.simulator.snapshotId
//            + " new state = " + snapshot.simulator.state)
        simulationState = snapshot.simulator.state
        completedIterations = snapshot.beings.values.map { it.completed }.sum()
        simulationView.updateModel(snapshot)
        updateViewStates()

        if (!viewModel.simulationRunning) {
            // Enable/disable model parameter settings if
            // we've reconnected to a running simulation.
            (settingsFragment as SettingsDialogFragment)
                    .simulationRunning(viewModel.simulationRunning)
        }
    }

    /**
     * Updates all views to reflect the current model state.
     */
    private fun updateViewStates() {
        mainActivityHintView.gone = true
        simulationView.show = true

        when (simulationState) {
            SimulatorComponent.State.RUNNING -> {
                progressBar.visible = true
                actionFab.setImageResource(R.drawable.ic_close_white_48dp)
                (settingsFragment as SettingsDialogFragment)
                        .simulationRunning(true)
            }
            SimulatorComponent.State.CANCELLING -> {
                progressBar.visible = true
                actionFab.setImageResource(R.drawable.ic_hourglass_empty_white_48dp)
            }
            SimulatorComponent.State.IDLE -> {
                progressBar.visible = false
                actionFab.setImageResource(android.R.drawable.ic_media_play)
                (settingsFragment as SettingsDialogFragment)
                        .simulationRunning(false)
            }
            SimulatorComponent.State.CANCELLED,
            SimulatorComponent.State.ERROR,
            SimulatorComponent.State.COMPLETED -> {
                progressBar.visible = false
                actionFab.setImageResource(android.R.drawable.ic_media_play)
                (settingsFragment as SettingsDialogFragment)
                        .simulationRunning(false)
            }
            SimulatorComponent.State.UNDEFINED -> {
                error("Received an UNDEFINED Model state")
            }
            null -> {
            }
        }

        updateTitle()
    }

    /**
     * The AppBar menu only has one entry: an icon for
     * opening the Settings panel.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * Open or close the Settings panel.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.actionSettings -> {
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END)
                } else {
                    drawerLayout.openDrawer(GravityCompat.END)
                }
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    /**
     * Only print out log statements if logging is enabled.
     */
    private fun log(msg: String) {
        if (Controller.getLogging()) {
            info(msg)
        }
    }

    /**
     * React to shared preference changes.
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?,
                                           key: String?) {
        with(Settings) {
            when (key) {
                SIMULATION_PALANTIR_COUNT_PREF -> updateSimulationModel()
                SIMULATION_BEING_COUNT_PREF -> updateSimulationModel()
                SIMULATION_GAZING_ITERATIONS_COUNT_PREF -> updateSimulationModel()
                SIMULATION_BEING_MANAGER_TYPE_PREF -> updateSimulationModel()
                SIMULATION_PALANTIRI_MANAGER_TYPE_PREF -> updateSimulationModel()
                SIMULATION_ANIMATION_SPEED_PREF -> {
                    viewModel.simulationSpeed = animationSpeed
                }
                SIMULATION_LOGGING_PREF -> {
                    viewModel.logging = logging
                }
                SIMULATION_GAZING_DURATION_PREF -> {
                    viewModel.gazingTimeRange = gazingDuration
                }
                SIMULATION_PERFORMANCE_MODE_PREF -> {
                    viewModel.setPerformanceMode = performanceMode
                }
            }
        }
    }

    /**
     * Displays a toast when an exception is encountered, but also
     * writes the exception to the log file for auditing.
     */
    private fun logException(msg: String, e: Exception) {
        longToast(msg)
        warn(msg)
        e.printStackTrace()
    }
}

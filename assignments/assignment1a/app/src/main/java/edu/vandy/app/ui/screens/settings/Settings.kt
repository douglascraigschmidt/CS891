package edu.vandy.app.ui.screens.settings

import edu.vandy.app.utils.Range
import edu.vandy.app.extensions.ImageDownloader
import edu.vandy.app.extensions.RangeAdapter
import edu.vandy.app.preferences.EnumAdapter
import edu.vandy.app.preferences.Preference
import edu.vandy.app.preferences.PreferenceProvider
import edu.vandy.app.ui.adapters.dpToPx
import edu.vandy.app.ui.screens.main.SimulationView
import edu.vandy.simulator.managers.beings.BeingManager
import edu.vandy.simulator.managers.palantiri.PalantiriManager

/**
 * All settings that are saved/restored from shared
 * preferences and changeable from the SettingsDialogFragment.
 */
object Settings {
    /**
     * Used to support shared preference versions so that this value
     * can be incremented to force the all shared preferences to be
     * cleared if the app loads and the restored VERSION less than
     * this VERSION. Bump up this version if a design change results
     * in any shared preference values may conflict with previously
     * saved values. Examples include changing a shared preference
     * type, or adding, removing, or reordering enumerated types
     * This is not an exhaustive list of examples, so use your own
     * discretion, but it's better to bump up the version than to
     * get unpredictable results or worse, crashes.
     */
    private val VERSION = 1.1f // Assignment 1a
//    private val VERSION = 1.2f // Assignment 1b
//    private val VERSION = 2.1f // Assignment 2a
//    private val VERSION = 2.2f // Assignment 2b
//    private val VERSION = 3.1f // Assignment 3a
//    private val VERSION = 3.2f // Assignment 3b
//    private val VERSION = 4.1f // Assignment 4a
//    private val VERSION = 4.2f // Assignment 4b

    /**
     * Preference keys defined for other classes to connect to.
     */
    val SIMULATION_ANIMATION_SPEED_PREF = "SimulationSpeedPreference"
    val SIMULATION_BEING_COUNT_PREF = "SimulationBeingCount"
    val SIMULATION_PALANTIR_COUNT_PREF = "SimulationPalantirCount"
    val SIMULATION_GAZING_ITERATIONS_COUNT_PREF = "SimulationGazingIterations"
    val SIMULATION_SHOW_SPRITES_PREF = "SimulationShowSprites"
    val SIMULATION_SHOW_PATHS_PREF = "SimulationShowPaths"
    val SIMULATION_SHOW_STATES_PREF = "SimulationShowStates"
    val SIMULATION_SPRITE_PREF = "SimulationSprite"
    val SIMULATION_LOGGING_PREF = "SimulationLogging"
    val SIMULATION_SHOW_WIRE_FRAMES_PREF = "SimulationShowWireFrames"
    val SIMULATION_GAZING_DURATION_PREF = "SimulationGazingDuration"
    val SIMULATION_AUTO_SCALE_PREF = "SimulationAutoScale"
    val SIMULATION_VIEW_TRANSPARENCY_PREF = "SimulationViewTransparency"
    val SIMULATION_BEING_MANAGER_TYPE_PREF = "SimulationBeingManagerType"
    val SIMULATION_PALANTIRI_MANAGER_TYPE_PREF = "SimulationPalantiriManagerType"
    val SIMULATION_BEING_SIZE_PREF = "SimulationBeingSize"
    val SIMULATION_BEING_SIZE_RANGE_PREF = "SimulationBeingSizeRangePref"
    val SIMULATION_BEING_COUNT_RANGE_PREF = "SimulationBeingCountRangePref"
    val SIMULATION_PALANTIRI_SIZE_PREF = "SimulationPalantiriSize"
    val SIMULATION_PALANTIR_SIZE_RANGE_PREF = "SimulationPalantirSizeRangePref"
    val SIMULATION_PALANTIR_COUNT_RANGE_PREF = "SimulationPalantirCountRangePref"
    val SIMULATION_STATE_SIZE_RANGE_PREF = "SimulationStateSizeRangePref"
    val SIMULATION_STATE_SIZE_PREF = "SimulationStateSize"
    val SIMULATION_SAVE_ON_EXIT_PREF = "SimulationSaveOnExit"
    val SIMULATION_PERFORMANCE_MODE_PREF = "SimulationPerformanceMode"
    val SIMULATION_STRICT_MODE_PREF = "SimulationStrictMode"
    val SIMULATION_MODEL_CHECKER_PREF = "SimulationModelChecker"
    val SIMULATION_SHOW_PROGRESS_PREF = "SimulationShowProgress"

    // Both not used in this simulation but an included extension needs them.
    val IMAGE_DOWNLOADER_PREF = "imageDownloaderPref" // Not used in this app
    val DEFAULT_IMAGE_DOWNLOADER = ImageDownloader.Type.PICASSO // not used in this app

    /**
     * Default preference values.
     */
    private val DEFAULT_BEING_COUNT = 5
    private val DEFAULT_PALANTIRI_COUNT = 3
    private val DEFAULT_GAZING_ITERATIONS = 5
    private val DEFAULT_ANIMATION_SPEED = 50
    private val DEFAULT_SHOW_SPRITES = true
    private val DEFAULT_SHOW_STATES = true
    private val DEFAULT_SHOW_PATHS = true
    private val DEFAULT_SPRITE = 0
    private val DEFAULT_LOGGING = true
    private val DEFAULT_SHOW_WIRE_FRAMES = false
    private val DEFAULT_GAZING_DURATION = Range(2, 5)
    private val DEFAULT_AUTO_SCALE = true
    private val DEFAULT_VIEW_TRANSPARENCY = 15
    private val DEFAULT_BEING_MANAGER_TYPE = BeingManager.Factory.Type.RUNNABLE_THREADS
    private val DEFAULT_PALANTIRI_MANAGER_TYPE = PalantiriManager.Factory.Type.ARRAY_BLOCKING_QUEUE
    private val DEFAULT_SAVE_ON_EXIT = true
    private val DEFAULT_PERFORMANCE_MODE = false
    private val DEFAULT_STRICT_MODE = false
    private val DEFAULT_MODEL_CHECKER = false
    private val DEFAULT_SHOW_PROGRESS = true

    /**
     * To avoid accumulating rounding errors by converting
     * between dp and px values, all sizes are saved in px
     * values.
     */
    private val DEFAULT_BEING_SIZE = 80.dpToPx.toInt()
    private val DEFAULT_PALANTIRI_SIZE = (DEFAULT_BEING_SIZE * 2f/ 3f).toInt()
    private val DEFAULT_STATE_SIZE = 10.dpToPx.toInt()

    /**
     * Default preference range values.
     *
     * Note that in the case of SeekBars, specifying a
     * max of 10 will create a seek range of 0 to 9, but
     * values will be incremented by 1 before being sent
     * to the UI.
     */
    val ITERATIONS_RANGE = Range(1, 10)
    val GAZING_DURATION_RANGE = Range(0, 9)
    val VIEW_TRANSPARENCY_RANGE = Range(5, 50)
    val ANIMATION_RANGE = Range(0, 100)

    /**
     * These default values are just reasonable guesses
     * and may not make sense for smaller displays, so
     * the actual min and max range values are adjusted
     * by SimulationView when it performs it initial
     * layout.
     */
    val BEING_COUNT_RANGE = Range(1, SimulationView.MAX_BEING_COUNT)
    val PALANTIR_COUNT_RANGE = Range(1, SimulationView.MAX_PALANTIR_COUNT)

    /**
     * To avoid accumulating rounding errors by converting
     * between dp and px values, all sizes are saved in px
     * values. The maximum values are really irrelevant here
     * because they will always be set dynamically based on
     * sprite counts, resolution, and display size.
     */
    val BEING_SIZE_RANGE =
            Range(SimulationView.MIN_BEING_SIZE,
                  150.dpToPx.toInt())
    val PALANTIR_SIZE_RANGE =
            Range(SimulationView.MIN_PALANTIR_SIZE,
                  150.dpToPx.toInt())
    val STATE_SIZE_RANGE =
            Range(SimulationView.MIN_STATE_TEXT_SIZE,
                  SimulationView.MAX_STATE_TEXT_SIZE)

    /**
     * Tunable range values set by SimulationView to account
     * for different display sizes and resolutions.
     */
    @JvmStatic
    var beingSizeRange: Range<Int>
            by Preference(BEING_SIZE_RANGE,
                          SIMULATION_BEING_SIZE_RANGE_PREF)
    @JvmStatic
    var beingCountRange: Range<Int>
            by Preference(BEING_COUNT_RANGE,
                          SIMULATION_BEING_COUNT_RANGE_PREF)
    @JvmStatic
    var palantirSizeRange: Range<Int>
            by Preference(PALANTIR_SIZE_RANGE,
                          SIMULATION_PALANTIR_SIZE_RANGE_PREF)
    @JvmStatic
    var palantirCountRange: Range<Int>
            by Preference(PALANTIR_COUNT_RANGE,
                          SIMULATION_PALANTIR_COUNT_RANGE_PREF)
    @JvmStatic
    var stateSizeRange: Range<Int>
            by Preference(STATE_SIZE_RANGE,
                          SIMULATION_STATE_SIZE_RANGE_PREF)

    /**
     * These preferences have fixed names so that other classes
     * can use the preference key name to observe changes on that key.
     */
    @JvmStatic
    var beingCount: Int
            by Preference(DEFAULT_BEING_COUNT,
                          SIMULATION_BEING_COUNT_PREF)
    @JvmStatic
    var palantirCount: Int
            by Preference(DEFAULT_PALANTIRI_COUNT,
                          SIMULATION_PALANTIR_COUNT_PREF)
    @JvmStatic
    var gazingIterations: Int
            by Preference(DEFAULT_GAZING_ITERATIONS,
                          SIMULATION_GAZING_ITERATIONS_COUNT_PREF)
    @JvmStatic
    var animationSpeed: Int
            by Preference(DEFAULT_ANIMATION_SPEED,
                          SIMULATION_ANIMATION_SPEED_PREF)
    @JvmStatic
    var showSprites: Boolean
            by Preference(DEFAULT_SHOW_SPRITES,
                          SIMULATION_SHOW_SPRITES_PREF)
    @JvmStatic
    var showPaths: Boolean
            by Preference(DEFAULT_SHOW_PATHS,
                          SIMULATION_SHOW_PATHS_PREF)
    @JvmStatic
    var showStates: Boolean
            by Preference(DEFAULT_SHOW_STATES,
                          SIMULATION_SHOW_STATES_PREF)
    @JvmStatic
    var sprite: Int
            by Preference(DEFAULT_SPRITE,
                          SIMULATION_SPRITE_PREF)
    @JvmStatic
    var logging: Boolean
            by Preference(DEFAULT_LOGGING,
                          SIMULATION_LOGGING_PREF)
    @JvmStatic
    var showWireFrames: Boolean
            by Preference(DEFAULT_SHOW_WIRE_FRAMES,
                          SIMULATION_SHOW_WIRE_FRAMES_PREF)
    @JvmStatic
    var gazingDuration: Range<Int>
            by Preference(DEFAULT_GAZING_DURATION,
                          SIMULATION_GAZING_DURATION_PREF,
                          RangeAdapter())
    @JvmStatic
    var autoScale: Boolean
            by Preference(DEFAULT_AUTO_SCALE,
                          SIMULATION_AUTO_SCALE_PREF)
    @JvmStatic
    var viewTransparency: Int
            by Preference(DEFAULT_VIEW_TRANSPARENCY,
                          SIMULATION_VIEW_TRANSPARENCY_PREF)
    @JvmStatic
    var beingManagerType: BeingManager.Factory.Type
            by Preference(DEFAULT_BEING_MANAGER_TYPE,
                          SIMULATION_BEING_MANAGER_TYPE_PREF,
                          EnumAdapter(BeingManager.Factory.Type::class.java))
    @JvmStatic
    var palantirManagerType: PalantiriManager.Factory.Type
            by Preference(DEFAULT_PALANTIRI_MANAGER_TYPE,
                          SIMULATION_PALANTIRI_MANAGER_TYPE_PREF,
                          EnumAdapter(PalantiriManager.Factory.Type::class.java))
    @JvmStatic
    var beingSize: Int
            by Preference(DEFAULT_BEING_SIZE,
                          SIMULATION_BEING_SIZE_PREF)
    @JvmStatic
    var palantirSize: Int
            by Preference(DEFAULT_PALANTIRI_SIZE,
                          SIMULATION_PALANTIRI_SIZE_PREF)
    @JvmStatic
    var stateSize: Int
            by Preference(DEFAULT_STATE_SIZE,
                          SIMULATION_STATE_SIZE_PREF)
    @JvmStatic
    var saveOnExit: Boolean
            by Preference(DEFAULT_SAVE_ON_EXIT,
                          SIMULATION_SAVE_ON_EXIT_PREF)
    @JvmStatic
    var performanceMode: Boolean
            by Preference(DEFAULT_PERFORMANCE_MODE,
                          SIMULATION_PERFORMANCE_MODE_PREF)
    @JvmStatic
    var strictMode: Boolean
            by Preference(DEFAULT_STRICT_MODE,
                          SIMULATION_STRICT_MODE_PREF)
    @JvmStatic
    var modelChecker: Boolean
            by Preference(DEFAULT_MODEL_CHECKER,
                          SIMULATION_MODEL_CHECKER_PREF)
    @JvmStatic
    var showProgress: Boolean
            by Preference(DEFAULT_SHOW_PROGRESS,
                          SIMULATION_SHOW_PROGRESS_PREF)

    /**
     * Settings version support ensures that only
     * version >= VERSION will be loaded.
     */
    private val VERSION_PREF = "SettingsVersion"
    private val DEFAULT_VERSION = 0f
    private var version: Float
            by Preference(DEFAULT_VERSION, VERSION_PREF)

    init {
        // Don't load obsolete versions or last version when
        // saveOnExit has been disabled.
        if (version < VERSION || !saveOnExit) {
            // The only shared pref that always get restored
            // is the saveOnExit, so reset it to it's saved
            // value after clearing out all shared prefs.
            val oldSaveOnExit = saveOnExit

            println("Settings VERSION change from $version -> " +
                    "$VERSION: clearing all shared preferences!")

            // Clear all shared preferences.
            PreferenceProvider.clear()

            // Now save the new shared preference version number.
            version = VERSION

            // Restore save on exit flag.
            saveOnExit = oldSaveOnExit
        } else if (version > VERSION) {
            error("The saved settings version ($version) should " +
                  "never be less than the compiled VERSION value $VERSION")
        }
    }

    fun resetToDefaults(simulationRunning: Boolean) {
        if (!simulationRunning) {
            beingCount = DEFAULT_BEING_COUNT
            palantirCount = DEFAULT_PALANTIRI_COUNT
            gazingIterations = DEFAULT_GAZING_ITERATIONS
            palantirManagerType = DEFAULT_PALANTIRI_MANAGER_TYPE
            beingManagerType = DEFAULT_BEING_MANAGER_TYPE
        }

        animationSpeed = DEFAULT_ANIMATION_SPEED
        showSprites = DEFAULT_SHOW_SPRITES
        showPaths = DEFAULT_SHOW_PATHS
        showStates = DEFAULT_SHOW_STATES
        sprite = DEFAULT_SPRITE
        logging = DEFAULT_LOGGING
        showWireFrames = DEFAULT_SHOW_WIRE_FRAMES
        gazingDuration = DEFAULT_GAZING_DURATION
        autoScale = DEFAULT_AUTO_SCALE
        viewTransparency = DEFAULT_VIEW_TRANSPARENCY
        beingSize = DEFAULT_BEING_SIZE
        palantirSize = DEFAULT_PALANTIRI_SIZE
        stateSize = DEFAULT_STATE_SIZE
        showProgress = DEFAULT_SHOW_PROGRESS
        performanceMode = DEFAULT_PERFORMANCE_MODE
        strictMode = DEFAULT_STRICT_MODE
        modelChecker = DEFAULT_MODEL_CHECKER
    }
}


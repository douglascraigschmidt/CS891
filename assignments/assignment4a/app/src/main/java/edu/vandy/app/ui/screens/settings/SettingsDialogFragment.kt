package edu.vandy.app.ui.screens.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.widget.RxAdapterView
import com.jakewharton.rxbinding2.widget.RxCompoundButton
import com.jakewharton.rxbinding2.widget.RxSeekBar
import edu.vandy.R
import edu.vandy.app.extensions.*
import edu.vandy.app.preferences.PreferenceProvider
import edu.vandy.app.ui.screens.settings.Settings.ANIMATION_RANGE
import edu.vandy.app.ui.screens.settings.Settings.ITERATIONS_RANGE
import edu.vandy.app.ui.screens.settings.Settings.VIEW_TRANSPARENCY_RANGE
import edu.vandy.app.ui.screens.settings.Settings.beingCountRange
import edu.vandy.app.ui.screens.settings.Settings.beingSizeRange
import edu.vandy.app.ui.screens.settings.Settings.palantirCountRange
import edu.vandy.app.ui.screens.settings.Settings.palantirSizeRange
import edu.vandy.app.ui.screens.settings.Settings.stateSizeRange
import edu.vandy.app.ui.screens.settings.adapters.ManagerTypeEnumSpinnerAdapter
import edu.vandy.app.ui.screens.settings.adapters.SpriteAdapter
import edu.vandy.app.ui.widgets.RxMultiSlider
import edu.vandy.app.utils.KtLogger
import edu.vandy.simulator.managers.beings.BeingManager
import edu.vandy.simulator.managers.palantiri.PalantiriManager
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.settings_dialog_fragment.*


/**
 * Application developer options fragment that shows a list of tunable
 * options in a modal bottom sheet.
 *
 * To show this bottom sheet:
 * <pre>
 * SettingsDialogFragment.newInstance().show(getSupportFragmentManager(), "dialog");
 */
class SettingsDialogFragment :
        Fragment(),
        SharedPreferences.OnSharedPreferenceChangeListener,
        KtLogger {

    /**
     * Temporary flag set when a user action triggers one of the
     * widget Observables. When this happens, the shared preference
     * listener will ignore any notification received and reset the
     * flag to false.
     */
    private var userAction = false

    /**
     * Observables should always be disposed. In particular, they
     * need to be disposed when the user clicks the reset button
     * which reconfigures all widgets.
     */
    private var compositeDisposable = CompositeDisposable()

    /**
     * Flag indicating if a simulation is running. Used to
     * prevent reset of critical model parameters while a
     * simulation is running.
     */
    private var simulationRunning = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // create ContextThemeWrapper from the original Activity Context with the custom theme
        val contextThemeWrapper = ContextThemeWrapper(activity, R.style.SettingsThemeDark)

        // clone the inflater using the ContextThemeWrapper
        val localInflater = inflater.cloneInContext(contextThemeWrapper)
        return localInflater!!.inflate(R.layout.settings_dialog_fragment, container, false)
    }

    /**
     * Configure all widgets and install a shared preference listener.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureWidgets()
        PreferenceProvider.addListener(this)
    }

    /**
     * Configures all widgets and install Rx Widget Observers.
     */
    private fun configureWidgets() {
        configureBeingManagerType()
        configurePalantiriManagerType()
        configurePalantirCount()
        configureBeingCount()
        configureIterations()
        configureAnimationSpeed()
        configureShowSprites()
        configureShowPaths()
        configureShowStates()
        configureSpriteSpinner()
        configureLogging()
        configureWireFrames()
        configureGazingDuration()
        configureAutoScale()
        configureViewTransparency()
        configureResetToDefaults()
        configurePalantirSize()
        configureBeingSize()
        configureStateSize()
        configureSaveOnExit()
        configurePerformanceMode()
        configureModelChecker()
        configureStrictMode()
        configureShowProgress()
    }

    /**
     * Called to disable model parameter settings when
     * the simulation is running and to re-enable them
     * once the simulation completes.
     */
    fun simulationRunning(running: Boolean) {
        simulationRunning = running
        settingsBeingManagerType.enable(!running)
        settingsBeingManagerTypeLabel.enable(!running)
        settingsPalantiriManagerType.enable(!running)
        settingsPalantirManagerTypeLabel.enable(!running)
        settingsBeingsLabel.enable(!running)
        settingsBeingsLayout.enable(!running)
        settingsPalantirLabel.enable(!running)
        settingsPalantirLayout.enable(!running)
        settingsIterationsLabel.enable(!running)
        settingsIterationsLayout.enable(!running)
        settingsPerformanceMode.enable(!running)
        settingsPerformanceModeLabel.enable(!running)
    }

    /**
     * Dispose of all Rx Observers and stop
     * listening for shared preference changes.
     */
    override fun onDestroyView() {
        compositeDisposable.dispose()
        PreferenceProvider.removeListener(this)
        super.onDestroyView()
    }

    /**
     * Keeps settings panel up to date if any shared preferences are changed
     * else where in the app or when the reset button is clicked. Note that
     * for efficiency reasons, we don't want to reconfigure an preference
     * widget if the preference changed was itself caused by user input in
     * this Settings panel.
     *
     * NOTE: This method no longer uses the userAction flag set by
     * updatePreference function to prevent the edge case issue
     * described in the following WARNING note.
     *
     * WARNING: There's and edge case where the app may internally update a
     * shared preference while userAction is true which will cause this
     * function to inadvertently process the internal notification in the
     * userAction block - this can cause unexpected results and should be
     * fixed. The reason for this is when the updatePreference sets the
     * shared preference, the onSharedPreference handler of another class
     * may be called and from that call, may set a shared preference before
     * returning from it's onSharedPreference handler. The framework will
     * then call this function for the apps shared preference change before
     * it calls this function for resulting from the local updatePreference
     * call.
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?,
                                           key: String?) {
        // React to preference changes made outside of this view.
        with(Settings) {
            when (key) {
                SIMULATION_BEING_MANAGER_TYPE_PREF -> configureBeingManagerType()
                SIMULATION_BEING_SIZE_PREF -> configureBeingSize()
                SIMULATION_BEING_SIZE_RANGE_PREF -> configureBeingSize()
                SIMULATION_BEING_COUNT_PREF -> configureBeingCount()
                SIMULATION_BEING_COUNT_RANGE_PREF -> configureBeingCount()
                SIMULATION_PALANTIRI_MANAGER_TYPE_PREF -> configurePalantiriManagerType()
                SIMULATION_PALANTIR_COUNT_PREF -> configurePalantirCount()
                SIMULATION_PALANTIR_COUNT_RANGE_PREF -> configurePalantirCount()
                SIMULATION_PALANTIRI_SIZE_PREF -> configurePalantirSize()
                SIMULATION_PALANTIR_SIZE_RANGE_PREF -> configurePalantirSize()
                SIMULATION_GAZING_ITERATIONS_COUNT_PREF -> configureIterations()
                SIMULATION_ANIMATION_SPEED_PREF -> configureAnimationSpeed()
                SIMULATION_SHOW_PATHS_PREF -> configureShowPaths()
                SIMULATION_SHOW_STATES_PREF -> configureShowStates()
                SIMULATION_SHOW_PROGRESS_PREF -> configureShowProgress()
                SIMULATION_SPRITE_PREF -> configureSpriteSpinner()
                SIMULATION_LOGGING_PREF -> configureLogging()
                SIMULATION_SHOW_SPRITES_PREF,
                SIMULATION_SHOW_WIRE_FRAMES_PREF -> {
                    // These widgets are co-dependant.
                    configureWireFrames()
                    configureShowSprites()
                }
                SIMULATION_GAZING_DURATION_PREF -> configureGazingDuration()
                SIMULATION_AUTO_SCALE_PREF -> configureAutoScale()
                SIMULATION_VIEW_TRANSPARENCY_PREF -> configureViewTransparency()
                SIMULATION_STATE_SIZE_PREF -> configureStateSize()
                SIMULATION_SAVE_ON_EXIT_PREF -> configureSaveOnExit()
                SIMULATION_PERFORMANCE_MODE_PREF -> configurePerformanceMode()
                SIMULATION_STRICT_MODE_PREF -> configureStrictMode()
                SIMULATION_MODEL_CHECKER_PREF -> {
                    // Model checker toggles strict mode enabled state.
                    configureModelChecker()
                    configureStrictMode()
                }
            }
        }
    }

    private fun configureBeingManagerType(update: Boolean = false) {
        if (update) {
            @Suppress("UNCHECKED_CAST")
            val adapter =
                    settingsBeingManagerType.adapter
                            as ManagerTypeEnumSpinnerAdapter<BeingManager.Factory.Type>
            settingsBeingManagerType.setSelection(
                    adapter.getPositionForValue(Settings.beingManagerType))
        } else {
            // Setup adapter and current selection.
            val adapter = ManagerTypeEnumSpinnerAdapter(
                    ContextThemeWrapper(activity, R.style.SettingsThemeDark),
                    BeingManager.Factory.Type::class.java)
            settingsBeingManagerType.adapter = adapter
            settingsBeingManagerType.setSelection(
                    adapter.getPositionForValue(Settings.beingManagerType))

            // Use Rx to filter item selections and save changed value to shared preference.
            compositeDisposable.add(
                    RxAdapterView.itemSelections(settingsBeingManagerType)
                            .skipInitialValue()
                            .map<BeingManager.Factory.Type> { adapter.getItem(it) }
                            .filter { it != Settings.beingManagerType }
                            .subscribe {
                                updatePreference { Settings.beingManagerType = it }
                            })
        }
    }

    private fun configurePalantiriManagerType() {
        // Setup adapter and current selection.
        val adapter = ManagerTypeEnumSpinnerAdapter(
                ContextThemeWrapper(activity, R.style.SettingsThemeDark),
                PalantiriManager.Factory.Type::class.java)
        settingsPalantiriManagerType.adapter = adapter
        settingsPalantiriManagerType.setSelection(
                adapter.getPositionForValue(Settings.palantirManagerType))

        // Use Rx to filter item selections and save changed value to shared preference.
        compositeDisposable.add(
                RxAdapterView.itemSelections(settingsPalantiriManagerType)
                        .skipInitialValue()
                        .map<PalantiriManager.Factory.Type> { adapter.getItem(it) }
                        .filter { it != Settings.palantirManagerType }
                        .subscribe {
                            updatePreference { Settings.palantirManagerType = it }
                        })
    }

    private fun configurePalantirCount() {
        settingsPalantirSeekBar.grabTouchEvents()
        settingsPalantirSeekBar.max = palantirCountRange.maxIndex
        settingsPalantirSeekBar.progress =
                palantirCountRange.toOffset(Settings.palantirCount)
        settingsPalantirValue.text =
                String.format("%3d", Settings.palantirCount)
        compositeDisposable.add(
                RxSeekBar.userChanges(settingsPalantirSeekBar)
                        .skipInitialValue()
                        .subscribe {
                            val value = palantirCountRange.fromOffset(it)
                            updatePreference { Settings.palantirCount = value }
                            settingsPalantirValue.text = String.format("%3d", value)
                        })
    }

    private fun configureBeingCount() {
        settingsBeingsSeekBar.grabTouchEvents()
        settingsBeingsSeekBar.progress =
                beingCountRange.toOffset(Settings.beingCount)
        settingsBeingsSeekBar.max = beingCountRange.maxIndex
        settingsBeingsValue.text =
                String.format("%3d", Settings.beingCount)
        compositeDisposable.add(
                RxSeekBar.userChanges(settingsBeingsSeekBar)
                        .skipInitialValue()
                        .subscribe {
                            val value = beingCountRange.fromOffset(it)
                            updatePreference { Settings.beingCount = value }
                            settingsBeingsValue.text = String.format("%3d", value)
                        })
    }

    private fun configureIterations() {
        settingsIterationsSeekBar.grabTouchEvents()
        settingsIterationsSeekBar.max = ITERATIONS_RANGE.maxIndex
        settingsIterationsSeekBar.progress =
                ITERATIONS_RANGE.toOffset(Settings.gazingIterations)
        settingsIterationsValue.text =
                String.format("%3d", Settings.gazingIterations)
        compositeDisposable.add(
                RxSeekBar.userChanges(settingsIterationsSeekBar)
                        .skipInitialValue()
                        .subscribe {
                            val value = ITERATIONS_RANGE.fromOffset(it)
                            updatePreference { Settings.gazingIterations = value }
                            settingsIterationsValue.text = String.format("%3d", value)
                        })
    }

    private fun configureAnimationSpeed() {
        settingsAnimationSpeedSeekBar.grabTouchEvents()
        settingsAnimationSpeedSeekBar.max = ANIMATION_RANGE.maxIndex
        settingsAnimationSpeedSeekBar.progress =
                ANIMATION_RANGE.toOffset(Settings.animationSpeed)
        settingsAnimationSpeedValue.text =
                String.format("%3d", Settings.animationSpeed)
        compositeDisposable.add(
                RxSeekBar.userChanges(settingsAnimationSpeedSeekBar)
                        .skipInitialValue()
                        .subscribe {
                            val value = ANIMATION_RANGE.fromOffset(it)
                            updatePreference { Settings.animationSpeed = value }
                            settingsAnimationSpeedValue.text = String.format("%3d", value)
                        })
    }

    private fun configureShowPaths() {
        settingsShowPaths.isChecked = Settings.showPaths
        compositeDisposable.add(
                RxCompoundButton.checkedChanges(settingsShowPaths)
                        .skipInitialValue()
                        .subscribe {
                            updatePreference { Settings.showPaths = it }
                        })
    }

    private fun configureShowStates() {
        settingsShowStates.isChecked = Settings.showStates
        compositeDisposable.add(
                RxCompoundButton.checkedChanges(settingsShowStates)
                        .skipInitialValue()
                        .subscribe {
                            updatePreference { Settings.showStates = it }
                        })
    }

    private fun configureShowProgress() {
        settingsShowProgress.isChecked = Settings.showProgress
        compositeDisposable.add(
                RxCompoundButton.checkedChanges(settingsShowProgress)
                        .skipInitialValue()
                        .subscribe {
                            updatePreference { Settings.showProgress = it }
                        })
    }

    private fun configureLogging() {
        settingsLogging.isChecked = Settings.logging
        compositeDisposable.add(
                RxCompoundButton.checkedChanges(settingsLogging)
                        .skipInitialValue()
                        .subscribe {
                            updatePreference { Settings.logging = it }
                        })
    }

    private fun configureModelChecker() {
        settingsModelChecker.isChecked = Settings.modelChecker
        compositeDisposable.add(
                RxCompoundButton.checkedChanges(settingsModelChecker)
                        .skipInitialValue()
                        .subscribe {
                            updatePreference { Settings.modelChecker = it }
                        })
    }

    private fun configureStrictMode() {
        settingsStrictMode.isChecked = Settings.strictMode
        settingsStrictMode.enable(Settings.modelChecker)
        settingsStrictModeLabel.enable(Settings.modelChecker)
        compositeDisposable.add(
                RxCompoundButton.checkedChanges(settingsStrictMode)
                        .skipInitialValue()
                        .subscribe {
                            updatePreference { Settings.strictMode = it }
                        })
    }

    private fun configureSpriteSpinner() {
        // Setup adapter and current selection.
        val adapter = SpriteAdapter(
                ContextThemeWrapper(activity, R.style.SettingsThemeDark))
        settingsBeingSprite.adapter = adapter
        settingsBeingSprite.setSelection(
                adapter.getPositionForValue(Settings.sprite))
        // Use Rx to filter item selections and save
        // changed value to shared preference.
        compositeDisposable.add(
                RxAdapterView.itemSelections(settingsBeingSprite)
                        .skipInitialValue()
                        .map<Int> { adapter.getItem(it) }
                        .filter { it != Settings.sprite }
                        .subscribe {
                            updatePreference { Settings.sprite = it }
                        })
    }

    private fun configureWireFrames() {
        settingsShowWireFrames.isChecked = Settings.showWireFrames || !Settings.showSprites
        compositeDisposable.add(
                RxCompoundButton.checkedChanges(settingsShowWireFrames)
                        .skipInitialValue()
                        .subscribe {
                            updatePreference { Settings.showWireFrames = it }
                        })
    }

    private fun configureShowSprites() {
        settingsShowSprites.isChecked = Settings.showSprites || !Settings.showWireFrames
        compositeDisposable.add(
                RxCompoundButton.checkedChanges(settingsShowSprites)
                        .skipInitialValue()
                        .subscribe {
                            updatePreference { Settings.showSprites = it }
                        })
    }

    var ignoreGazingEvent = false
    private fun configureGazingDuration() {
        // Can't use kotlinx xml object because the RangeSeekBar class is typed.
        settingsGazingDurationSeekBar.grabTouchEvents()
        settingsGazingDurationSeekBar.min = Settings.GAZING_DURATION_RANGE.min
        settingsGazingDurationSeekBar.max = Settings.GAZING_DURATION_RANGE.max

        val range = Settings.gazingDuration
        settingsGazingDurationValue.text =
                String.format("%3d-%d", range.min, range.max)

        // Since this widget has two values (min and max) set a
        // flag to prevent calls to update the preference when
        // the thumbs are being moved.
        ignoreGazingEvent = true

        // This multiple thumb seekbar does not handle calling
        // setNumberOfThumbs more than once per session so check
        // if the 2nd thumb has been set to determine to prevent
        // calling that method more than once.
        if (settingsGazingDurationSeekBar.getThumb(1) == null) {
            settingsGazingDurationSeekBar.setNumberOfThumbs(2, true)
        }

        settingsGazingDurationSeekBar.getThumb(0).value =
                Settings.gazingDuration.min
        settingsGazingDurationSeekBar.getThumb(1).value =
                Settings.gazingDuration.max

        compositeDisposable.add(
                RxMultiSlider.changes(settingsGazingDurationSeekBar)
                        .skipInitialValue()
                        .subscribe {
                            if (!ignoreGazingEvent) {
                                updatePreference { Settings.gazingDuration = it }
                            }
                            settingsGazingDurationValue.text =
                                    String.format("%3d-%d", it.min, it.max)
                        })

        // Re-enable processing of events.
        ignoreGazingEvent = false
    }


    private fun configurePerformanceMode(update: Boolean = false) {
        if (update) {
            settingsAutoScale.isChecked = Settings.performanceMode
        } else {
            val performanceMode = Settings.performanceMode

            settingsAnimationSpeedLabel.enable(!performanceMode)
            settingsAnimationSpeedLayout.enable(!performanceMode)
            settingsGazingDurationLabel.enable(!performanceMode)
            settingsGazingDurationLayout.enable(!performanceMode)
            settingsPerformanceMode.isChecked = performanceMode

            compositeDisposable.add(
                    RxCompoundButton.checkedChanges(settingsPerformanceMode)
                            .skipInitialValue()
                            .subscribe {
                                updatePreference { Settings.performanceMode = it }
                            })
        }
    }

    private fun configureAutoScale(update: Boolean = false) {
        if (update) {
            settingsAutoScale.isChecked = Settings.autoScale
        } else {
            val autoScale = Settings.autoScale

            settingsBeingSizeLabel.enable(!autoScale)
            settingsBeingSizeLayout.enable(!autoScale)
            settingsPalantirSizeLabel.enable(!autoScale)
            settingsPalantirSizeLayout.enable(!autoScale)
            settingsStateSizeLabel.enable(!autoScale)
            settingsStateSizeLayout.enable(!autoScale)
            settingsAutoScale.isChecked = autoScale

            compositeDisposable.add(
                    RxCompoundButton.checkedChanges(settingsAutoScale)
                            .skipInitialValue()
                            .subscribe {
                                //updatePreference { Settings.autoScale = it }
                                Settings.autoScale = it
                            })
        }
    }

    private fun configureViewTransparency() {
        settingsViewTransparencySeekBar.grabTouchEvents()
        settingsViewTransparencySeekBar.max = VIEW_TRANSPARENCY_RANGE.maxIndex
        settingsViewTransparencySeekBar.progress =
                VIEW_TRANSPARENCY_RANGE.toOffset(Settings.viewTransparency)
        compositeDisposable.add(
                RxSeekBar.changes(settingsViewTransparencySeekBar)
                        // SPECIAL CASE: don't skip initial value so that
                        // onNext will be immediately called and this will
                        // set the transparency for this view.
                        .subscribe {
                            val value = VIEW_TRANSPARENCY_RANGE.fromOffset(it)
                            updatePreference { Settings.viewTransparency = value }
                            settingsViewTransparencyValue.text = String.format("%3d", value)
                            gridLayout.alpha = 1 - (value * (1 / 100f))
                        })
    }

    private fun configureBeingSize() {
        settingsBeingSizeLayout.enable(!Settings.autoScale)
        settingsBeingSizeLabel.enable(!Settings.autoScale)

        settingsBeingSizeSeekBar.grabTouchEvents()
        settingsBeingSizeSeekBar.max = beingSizeRange.maxIndex
        settingsBeingSizeSeekBar.progress =
                beingSizeRange.toOffset(Settings.beingSize)
        compositeDisposable.add(
                RxSeekBar.userChanges(settingsBeingSizeSeekBar)
                        .skipInitialValue()
                        .subscribe {
                            val value = beingSizeRange.fromOffset(it)
                            updatePreference { Settings.beingSize = value }
                            settingsBeingSizeValue.text = String.format("%3d", value)
                        })
    }

    private fun configurePalantirSize() {
        settingsPalantirSizeLabel.enable(!Settings.autoScale)
        settingsPalantirSizeLayout.enable(!Settings.autoScale)

        settingsPalantirSizeSeekBar.grabTouchEvents()
        settingsPalantirSizeSeekBar.max = palantirSizeRange.maxIndex
        settingsPalantirSizeSeekBar.progress =
                palantirSizeRange.toOffset(Settings.palantirSize)
        compositeDisposable.add(
                RxSeekBar.userChanges(settingsPalantirSizeSeekBar)
                        .skipInitialValue()
                        .subscribe {
                            val value = palantirSizeRange.fromOffset(it)
                            updatePreference { Settings.palantirSize = value }
                            settingsPalantirSizeValue.text = String.format("%3d", value)
                        })
    }

    private fun configureStateSize() {
        settingsStateSizeLabel.enable(!Settings.autoScale)
        settingsStateSizeSeekBar.enable(!Settings.autoScale)

        settingsStateSizeSeekBar.grabTouchEvents()
        settingsStateSizeSeekBar.max = stateSizeRange.maxIndex
        settingsStateSizeSeekBar.progress =
                stateSizeRange.toOffset(Settings.stateSize)
        compositeDisposable.add(
                RxSeekBar.userChanges(settingsStateSizeSeekBar)
                        .skipInitialValue()
                        .subscribe {
                            val value = stateSizeRange.fromOffset(it)
                            updatePreference { Settings.stateSize = value }
                            settingsStateSizeValue.text = String.format("%3d", value)
                        })
    }

    private fun configureSaveOnExit() {
        settingsSaveOnExit.isChecked = Settings.saveOnExit
        compositeDisposable.add(
                RxCompoundButton.checkedChanges(settingsSaveOnExit)
                        .skipInitialValue()
                        .subscribe {
                            updatePreference { Settings.saveOnExit = it }
                        })
    }

    /**
     * Resets all Settings shared preferences to their default
     * values. This is tricky because the simplest code to handle
     * this is to call each widgets configure method. This requires
     * uninstalling all RxObservers which are then reinstalled
     * in each configuration function. The problem is that if a
     * shared preference hasn't changed, then the SharedPreference
     * manager will not send a shared preference change event.
     * Therefore, we can't rely on the installed shared preference
     * listener as the main mechanism to handle calling all the
     * configuration functions. Instead, the configureWidgets
     * function is called via the updatePreference wrapper function
     * which ensures that no shared preference processing is called
     * while the configuration is running.
     */
    @Suppress("UNUSED_PARAMETER")
    private fun configureResetToDefaults(unused: Boolean = true) {
        settingsResetToDefaults.setOnClickListener {
            updatePreference {
                Settings.resetToDefaults(simulationRunning)
                compositeDisposable.dispose()
                compositeDisposable = CompositeDisposable()
                configureWidgets()
            }
        }
    }

    /**
     * NOTE: This method is still used but the userAction property
     * is no longer used in onSharedPreferenceChanged because of the
     * edge case issues described by the WARNING note.
     *
     * All Observables call this method to do the preference update
     * so that the userAction flag can be set first which prevents
     * this class's shared preference listener from reacting to the
     * preference change event. The flag is reset to false after
     * the update so that the listener is no longer disabled.
     *
     * WARNING: There's and edge case where the app may update a
     * shared preference while this function has set userAction
     * to true, and therefore this class's onSharedPreference
     * handler will inadvertently process the notification in the
     * userAction block - this can cause unexpected results and
     * should be fixed.
     */
    private fun updatePreference(block: () -> Unit) {
        userAction = true
        block()
        userAction = false
    }
}

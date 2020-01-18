package edu.vandy.app.ui.screens.main

import android.animation.ValueAnimator
import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.MainThread
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import android.util.AttributeSet
import edu.vandy.app.utils.Range
import android.util.Size
import android.util.SizeF
import android.view.View
import android.view.animation.LinearInterpolator
import edu.vandy.R
import edu.vandy.app.extensions.*
import edu.vandy.app.preferences.PreferenceProvider
import edu.vandy.app.ui.adapters.dpToPx
import edu.vandy.app.ui.adapters.getDisplaySize
import edu.vandy.app.ui.adapters.getPortraitSystemBarsSize
import edu.vandy.app.ui.adapters.getSystemBarsSize
import edu.vandy.app.ui.screens.settings.Settings
import edu.vandy.app.ui.screens.settings.adapters.SpriteAdapter
import edu.vandy.app.utils.KtLogger
import edu.vandy.simulator.model.implementation.components.BeingComponent
import edu.vandy.simulator.model.implementation.components.BeingComponent.State
import edu.vandy.simulator.model.implementation.components.PalantirComponent
import edu.vandy.simulator.model.implementation.components.SimulatorComponent
import edu.vandy.simulator.model.implementation.snapshots.BeingSnapshot
import edu.vandy.simulator.model.implementation.snapshots.ModelSnapshot
import edu.vandy.simulator.model.implementation.snapshots.PalantirSnapshot
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.properties.Delegates

class SimulationView @JvmOverloads constructor(context: Context,
                                               attrs: AttributeSet? = null,
                                               defStyle: Int = 0,
                                               styleRes: Int = 0)
    : View(context, attrs, defStyle, styleRes),
        KtLogger/**/,
        SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        /** Maximum palantir size as a % of fixed dimension autosize. */
        private val MAX_AUTO_SIZE_BEING_SCALE = 2 / 3f // of fixed dimension
        private val MAX_AUTO_SIZE_PALANTIR_SCALE = 1 / 2f // of being size
        private val MAX_BEING_SCALE = 2 / 3f // of fixed dimension
        private val MAX_PALANTIR_SCALE = 1f // of being size
        private val PROGRESS_SIZE = Size(30.dpToPx.toInt(), 4.dpToPx.toInt())

        /** declared internal for use in Settings class */
        internal val MIN_STATE_TEXT_SIZE = 6.dpToPx

        internal val MAX_STATE_TEXT_SIZE = 10.dpToPx

        /** The palantir sprite is the smallest. */
        internal val MIN_PALANTIR_SIZE =
                (MAX_STATE_TEXT_SIZE + 20.dpToPx.toInt()).toInt()

        /** The minimum being size is scaled to the minimum palantir size. */
        internal val MIN_BEING_SIZE =
                ((1f / MAX_PALANTIR_SCALE) * MIN_PALANTIR_SIZE).toInt()

        /** Maximum number of beings */
        internal val MAX_BEING_COUNT = 10
        /** Maximum number of palantiri */
        internal val MAX_PALANTIR_COUNT = 10

        /** Maximum being size as a % of fixed dimension */
        /** Used to avoid memory allocations during layouts and draws. */
        val NO_SIZE = Size(0, 0)
    }

    /** Min/Max sprite margin sizes. */
    private var marginRange =
            Range(4.dpToPx.toInt() + PROGRESS_SIZE.height,
                  8.dpToPx.toInt() + PROGRESS_SIZE.height)

    /**
     * Current distance between adjacent sprites.
     * Currently always set to max, but this value should be
     * scaled to the actual current sprite size so that small
     * sprites don't look so far apart.
     */
    private var spriteMargin = marginRange.max

    /** Minimum pace between being and palantir during gazing. */
    private var gazingMargin = 8.dpToPx.toInt()

    /** Current maximum being dimensions. */
    private var maxBeingDim = Size(0, 0)
    /** Current maximum palantir dimensions. */
    private var maxPalantirDim = Size(0, 0)

    /** Switches between auto scale and manual scale values */
    private val beingScale
        get() = if (Settings.autoScale) {
            MAX_AUTO_SIZE_BEING_SCALE
        } else {
            MAX_BEING_SCALE
        }
    private val palantirScale
        get() = if (Settings.autoScale) {
            MAX_AUTO_SIZE_PALANTIR_SCALE
        } else {
            MAX_PALANTIR_SCALE
        }

    /** Initialized each time a new snapshot is received. */
    internal var maxStateSize = NO_SIZE

    /**
     * Initialized each time a new snapshot is received
     * This value is the same as maxStateSize but includes
     * the state margin.
     */
    internal var maxStateBounds = NO_SIZE
    /** Only returns the maximum state size if show states is enabled. */
    private val stateSize
        get() = if (Settings.showStates) {
            maxStateSize
        } else {
            NO_SIZE
        }
    /** Only returns the maximum state bounds if show states is enabled. */
    internal val stateBounds
        get() = if (Settings.showStates) {
            maxStateBounds
        } else {
            NO_SIZE
        }

    /** Palantir alpha animation parameters. */
    private val palantirAlpha = 0.8f
    /** For status text */
    private var stateMargin = 2.dpToPx.toInt()
    /** For path anchor circle */
    private val pathAnchorRadius = 10f
    /** Current snapshot. */
    private var snapshot: ModelSnapshot
    /** Previous snapshot. */
    private var prevSnapshot: ModelSnapshot
    /** Current being sprites (snapshot drawable wrapper class) */
    internal var beings = HashMap<Long, Sprite>()
    /** Current palantir sprites (snapshot drawable wrapper class) */
    internal var palantiri = HashMap<Long, Sprite>()

    /**
     * Pre-allocated draw objects for faster onDraw calls.
     */
    private var beingPaint: Paint
    private var palantiriPaint: Paint
    private var progressPaint: Paint
    private var pathPaint: Paint
    private var framePaint: Paint
    private var statePaint: Paint
    private var palantirId = R.drawable.palantir_bright
    private var palantirDrawable: BitmapDrawable
    private var beingDrawables = emptyList<BitmapDrawable>()
    private val beingResIds: List<Int>
    private val measureRect = Rect()
    private val layoutRect = Rect()
    private val spriteLayoutRect = Rect()

    /**
     * Flag to temporarily disable reacting to shared preference
     * changes made internally by this class.
     */
    private var userAction = true

    /** Minimum gazing margin adjusted to account for text labels. */
    internal val beingPalantirMargin
        get() = if (maxStateSize.width > width) {
            gazingMargin + ((maxStateSize.width - width) / 2f).toInt()
        } else {
            gazingMargin
        }

    /**
     * Flag indicating if require blocks should
     * throw an exception or just show a toast
     */
    private val strictMode
        get() = Settings.strictMode

    /** Flag indicating if a simulation is currently in progress. */
    private val simulationRunning
        get() = snapshot.simulator.state == SimulatorComponent.State.RUNNING

    /**
     * Allocate and initialize all memory objects use by onDraw.
     *
     * Note that this init block must be declared after all class
     * properties declarations so that they have all been initialized.
     */
    init {
        PreferenceProvider.addListener(this)

        // To avoid null values, setup empty snapshot and previous snapshot.
        snapshot = ModelSnapshot.NO_SNAPSHOT
        prevSnapshot = snapshot

        beingPaint = Paint()
        beingPaint.style = Paint.Style.FILL

        palantiriPaint = Paint()
        palantiriPaint.color = ContextCompat.getColor(context, R.color.palantir_in_use_color)
        palantiriPaint.style = Paint.Style.FILL
//        val glowColor = ContextCompat.getColor(context, android.R.color.black)
//        palantiriPaint.setShadowLayer(palantirSize.toFloat(), 0f, 0f, glowColor)
//        setLayerType(LAYER_TYPE_SOFTWARE, palantiriPaint)

        statePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        statePaint.color = ContextCompat.getColor(context, R.color.primaryDarkColor)
        statePaint.textSize = Settings.stateSize.toFloat()

        progressPaint = Paint()
        progressPaint.color = Color.GREEN
        progressPaint.style = Paint.Style.FILL
        //progressPaint.strokeWidth = 2f

        palantirDrawable =
                ContextCompat.getDrawable(context, palantirId)!!
                        as BitmapDrawable

        // Adapter owns the image resource ids of all being sprites.
        // Note that the Settings.sprite shared preference is the actual
        // id of the resource so we need a way of mapping the current
        // Settings.sprite value to it's associated image drawable. A
        // Settings.sprite value of 0, however, means to use all sprites
        // images returned from the adapter.
        beingResIds = SpriteAdapter.getSpriteResourceIds(context)
        beingDrawables = beingResIds
                .filter { it > 0 }
                .map {
                    ContextCompat.getDrawable(context, it)!!
                            as BitmapDrawable
                }
                .toMutableList()

        pathPaint = Paint()
        pathPaint.color = ContextCompat.getColor(context, R.color.path_color)
        pathPaint.style = Paint.Style.STROKE
        pathPaint.strokeWidth = 4f
        pathPaint.pathEffect =
                DashPathEffect(floatArrayOf(8f, 8f, 8f, 8f), 0f)

        framePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        framePaint.color = Color.RED
        framePaint.style = Paint.Style.STROKE
        framePaint.strokeWidth = 1f

        // Parse all state enum types from the model to determine the
        // the maximum enum name length which is required for
        // determining the min/max being and palantir counts and also
        // their min/max sizes.
        updateMaxStateSize()
    }

    /**
     * Called from the activity whenever the snapshot snapshot changes.
     * Saves the snapshot snapshot and calls invalidate to force it to
     * redraw the new snapshot.
     */
    @MainThread
    internal fun updateModel(snapshot: ModelSnapshot) {
        println("View received snapshot: " + snapshot)

        prevSnapshot = this.snapshot
        this.snapshot = snapshot

        if (Settings.modelChecker) {
            // First ensure that model snapshot is sound.
            try {
                validateModelState(prevSnapshot, snapshot)
            } catch (e: Exception) {
                context.toast("The currently Palantir model has not " +
                              "been properly implemented. Please fix " +
                              "your simulation solution and try again.")
            }
        }

        // Update hashMap first because calcMaxSpriteCounts
        // calls a method that indirectly needs the beings
        // map to be set.
        updateHashMaps(snapshot)

        // Only update count ranges if either being or palantir
        // counts have changed since last update.
        if (prevSnapshot.beings.count() != snapshot.beings.count() ||
            prevSnapshot.palantiri.count() != snapshot.palantiri.count()) {
            calcMaxSpriteCounts()
        }

        if (Settings.beingSizeRange.max < snapshot.beings.count()) {
            context.toast("Current display size and resolution " +
                          "will not support ${snapshot.beings.count()} " +
                          "beings")
            updateHashMaps(null)
            return
        }

        if (Settings.palantirSizeRange.max < snapshot.palantiri.count()) {
            context.toast("Current display size and resolution " +
                          "will not support ${snapshot.palantiri.count()} " +
                          "palantiri")
            updateHashMaps(null)
            return
        }

        // Now that all sprites are updated with the latest snapshot
        // values, run some model validation checks that will set
        // displayable error feedback messages that will be displayed
        // next to any sprites that are in an invalid state.
        if (Settings.modelChecker) {
            errors.clear()
            validateModel(snapshot)
            if (errors.count() > 0) {
                errors.forEach {
                    context.toast(it.value.msg)
                }
            }
        }

        // Only do a layout if the number of beings or palantiri have changed.
        if (prevSnapshot.beings.size != snapshot.beings.size ||
            prevSnapshot.palantiri.size != snapshot.palantiri.size) {
            setSizesAndRanges()
            requestLayout()
        } else {
            invalidate()
        }
    }

    var errors: MutableMap<String, Error> = mutableMapOf()

    class Error(val beingId: Long,
                val palantirId: Long,
                val msg: String,
                var count: Int)

    enum class ErrorType(val error: String) {
        // used
        BeingCountError(
                "Invalid being count: %d but should be %d."),
        BeingStarvationError(
                "Being starvation: being %d."),
        // used
        BeingIllegalStateTransitionError(
                "Illegal being state transition: being %d %s -> %s."),
        // used
        BeingPalantirIdInvalid(
                "Being %d acquired an unknown palantir %d."),
        // used
        BeingNoPalantirId(
                "Being %d is in state %s but has no associated palantir id"),
        BeingUnfairError(
                "Being %d has not had fair access to palantiri."),
        // used
        BeingMultipleUseError(
                "Being %d is using %d palantiri."),
        PalantirCountError(
                "Invalid palantir count: %d but should be %d."),
        PalantirUnfairError(
                "Palantir %d has not had fair gazing usage."),
        PalantirUnusedError(
                "Palantir %d has never been used."),
        PalantirIllegalTransitionStateError(
                "Illegal palantir state transition: palantir %d %s -> %s."),
        // used
        PalantirMultipleUseError(
                "Palantir %d has been assigned to more than %d beings."),
        // used
        PalantirUsedByWrongBeing(
                "Palantir %d is assigned to being %d but this being is gazing into palantir %d"),
        BeingPalantirDoesNotHaveCorrectBeingId(
                "Being %d owns palantir %d but palantir %d is owned by being %d."),
        BeingPalantirDoesNotHaveBeingIdSet(
                "Being %d owns palantir %d but palantir %d beingId is -1.");

        override fun toString(): String = error

        fun format(vararg args: Any) = error.format(*args)
    }

    private fun addError(beingId: Long, palantirId: Long, msg: String) {
        errors[msg]?.let {
            it.count++
        } ?: errors.put(msg, Error(beingId, palantirId, msg, 1))
    }

    /**
     * Parses all being and palantir sprites and checks
     * their snapshot values for modelling errors. If found,
     * each sprite that is associated with an error will have
     * it's error [String] property set to an appropriate
     * error feedback message that will then be displayed
     * along side of the sprite.
     */
    private fun validateModel(model: ModelSnapshot) {
        // A cancelling or cancelled model state will never be
        // in a valid state so there is nothing to check.
        if (model.simulator.state == SimulatorComponent.State.CANCELLING ||
            model.simulator.state == SimulatorComponent.State.CANCELLED) {
            return
        }

        val beings = model.beings
        val palantiri = model.palantiri

        // Being validation includes:
        // 1. Check that the expected number of beings exist.
        // 2. Check that valid previous -> current state is valid.
        // 3. Check that being palantirId references a valid palantir.
        // 4. Check that gazing beings have a valid palantir id.
        // 5. Check that only 1 being has any given palantir id.
        // 6. Check for starved beings.
        // 7. Check for unfair gazing bias.

        // Being count should match settings being count value.
        if (beings.count() != Settings.beingCount) {
            addError(-1, -1,
                     ErrorType.BeingCountError.format(
                             beings.count(), Settings.beingCount))
        }

        palantiri.values.forEach {
            if (it.beingId != -1L) {
                // This palantir should not be owned by any other beings.
                // i.e., this being should only own a single palantir.
                val count = beings.filterValues { b -> b.palantirId == it.id }.count()
                if (count > 1) {
                    addError(it.beingId,
                             it.id,
                             ErrorType.PalantirMultipleUseError.format(
                                     it.id.toInt(),
                                     count))
                }
            }
        }

        beings.values.forEach {
            // Being should only be moving to a valid new state.
            if (!validateBeingState(it)) {
                addError(it.id,
                         -1,
                         ErrorType.BeingIllegalStateTransitionError.format(
                                 it.id.toInt(), it.prevState?.name ?: "null", it.state.name))
            }

            // We only check for model consistency when a being
            // is in the BUSY state (which breaks down into the
            // 3 states ACQUIRING, GAZING, and RELEASING.
            when (it.state) {
                State.ACQUIRING,
                State.RELEASING,
                State.GAZING -> {
                    // Palantir id should be set.
                    if (it.palantirId == -1L) {
                        addError(it.id,
                                 -1,
                                 ErrorType.BeingNoPalantirId.format(it.id.toInt(), it.state))
                    } else {
                        // The palantir should have an owner.
                        val palantir = palantiri[it.palantirId]
                        if (palantir == null) {
                            addError(it.id,
                                     it.palantirId,
                                     ErrorType.BeingPalantirIdInvalid.format(
                                             it.id.toInt(), it.palantirId.toInt()))
                        } else {
                            // This is a framework error check and if it fails, then the
                            // framework needs to be fixed (not a student assignment error).
                            if (strictMode) {
                                require(palantir.beingId != -1L) {
                                    "Framework Error: Being ${it.id} has acquired palantir " +
                                    "${it.palantirId}, but this palantir beingId is set to -1."
                                }
                            }

                            // The being's palantir should have this being as an owner.
                            if (palantir.beingId == -1L) {
                                addError(it.id,
                                         it.palantirId,
                                         ErrorType.BeingPalantirDoesNotHaveBeingIdSet.format(
                                                 it.id.toInt(),
                                                 it.palantirId.toInt(),
                                                 palantir.id))
                            } else if (palantir.beingId != it.id) {
                                addError(it.id,
                                         it.palantirId,
                                         ErrorType.BeingPalantirDoesNotHaveCorrectBeingId.format(
                                                 it.id.toInt(),
                                                 it.palantirId.toInt(),
                                                 palantir.id,
                                                 palantir.beingId))
                            }

                            // No other palantir should have this being as an owner,
                            // i.e., this being should only own a single palantir.
                            val count = palantiri.filterValues { p -> p.beingId == it.id }.count()
                            if (count > 1) {
                                addError(it.id,
                                         it.palantirId,
                                         ErrorType.BeingMultipleUseError.format(
                                                 it.id.toInt(),
                                                 count))
                            }
                        }
                    }
                }
                else -> {
                }
            }

            // Palantir validation includes:
            // 1. Check that the expected number of palantiri exist.
            // 2. Check that valid previous -> current state is valid.
            // 3. Check that palantir beingId references a valid being.
            // 4. Check that acquired palantir is only allocated to 1 being.
            // 5. Check that palantir usage is fair.
            // 6. Check that all palantir are being used.

            // Palantir count should match settings palantir count value.
            if (palantiri.count() != Settings.palantirCount) {
                addError(-1, -1,
                         ErrorType.PalantirCountError.format(
                                 palantiri.count(), Settings.palantirCount))
            }
        }
    }

    private fun validateBeingState(snapshot: BeingSnapshot): Boolean {
        val s1 = snapshot.prevState
        val s2 = snapshot.state

        if (s1 == s2) {
            return true
        }

        return when (s2) {
            State.HOLDING -> s1 == null || s1 == State.DONE
            State.IDLE -> s1 == null ||
                          s1 == State.RELEASING ||
                          s1 == State.ACQUIRING ||
                          s1 == State.WAITING ||
                          s1 == State.GAZING ||
                          s1 == State.DONE
            State.WAITING -> s1 == State.IDLE || s1 == State.RELEASING
            State.ACQUIRING -> s1 == State.IDLE || s1 == State.WAITING
            State.GAZING -> s1 == State.ACQUIRING
            State.RELEASING -> s1 == State.GAZING
            State.DONE -> s1 == State.IDLE || s1 == State.WAITING || s1 == State.ERROR
            State.CANCELLED,
            State.ERROR -> {
                true
            }
            null -> {
                // Framework error.
                error("State should never be null")
            }
            else -> {
                // Framework error.
                error("State $s2 is not supported by simulatorView.")
            }
        }
    }

    /**
     * Validates a received model snapshot.
     */
    private fun validateModelState(m1: ModelSnapshot,
                                   m2: ModelSnapshot): Boolean {
        if (m2.simulator.state == SimulatorComponent.State.CANCELLING ||
            m2.simulator.state == SimulatorComponent.State.CANCELLED) {
            return true
        }

        if (m1.beings.size != m2.beings.size) {
            return true
        }

        m1.beings.values.forEach { b1 ->
            val b2 = m2.beings[b1.id]
            require(b2 != null) {
                "Being from a previous snapshot b1 was " +
                "not found in new snapshot: $b1"
            }
            b2!!

            validateBeing(b2)

            // Since LiveData does not buffer snapshots, snapshots
            // can be dropped when too many are sent too quickly.
            // Therefore, we can't compare the previous snapshot
            // state with the new snapshot state since this may
            // produce invalid transition errors when states are
            // skipped. The best we can do is to very the snapshot's
            // prevState to state transition for possible errors.
            val s1 = b2.prevState
            val s2 = b2.state

            // Handy scoped function for error checking
            fun requires(b: Boolean) {
                require(b) {
                    error("Invalid transition $s1 -> $s2: $b2 ")
                }
            }

            if (s1 != s2 && s1 != State.CANCELLED) {
                //println("Validate: Being[${b2.id}] NEW STATE = $s1 -> $s2")
                // Note that IDLE can occur after almost any state because
                // its used as a short-term interim state.
                when (s2) {
                    State.HOLDING -> requires(s1 == null || s1 == State.DONE)
                    State.IDLE -> requires(s1 == null ||
                                           s1 == State.RELEASING ||
                                           s1 == State.ACQUIRING ||
                                           s1 == State.WAITING ||
                                           s1 == State.GAZING ||
                                           s1 == State.DONE)
                    State.WAITING -> requires(s1 == State.IDLE || s1 == State.RELEASING)
                    State.ACQUIRING -> requires(s1 == State.IDLE || s1 == State.WAITING)
                    State.GAZING -> requires(s1 == State.ACQUIRING)
                    State.RELEASING -> requires(s1 == State.GAZING)
                    State.DONE -> requires(s1 == State.IDLE || s1 == State.WAITING || s1 == State.ERROR)
                    State.CANCELLED,
                    State.REMOVED,
                    State.ERROR -> {
                    }
                    null -> {
                        error("State should never be null")
                    }
                    else -> {
                        error("State $s2 is not supported by simulatorView.")
                    }
                }
            }
        }

        return true
    }

    private fun validateBeing(being: BeingSnapshot): Boolean {
        return when (being.state) {
            State.ACQUIRING,
            State.GAZING,
            State.RELEASING -> {
                being.palantirId != -1L
            }
            else -> being.palantirId == -1L
        }
    }

    /**
     * Use memory efficient update by promoting HashMap object reuse.
     */
    private fun updateHashMaps(modelSnapshot: ModelSnapshot?) {
        if (modelSnapshot == null) {
            beings.clear()
            palantiri.clear()
            return
        }

        modelSnapshot.beings
                .map { it.value }
                .forEach { snapshot ->
                    (beings[snapshot.id] as? BeingSprite)?.let {
                        if (snapshot.isRemoved) {
                            beings.remove(snapshot.id)
                        } else {
                            it.snapshot = snapshot
                        }
                    } ?: beings.put(snapshot.id, BeingSprite(this, snapshot))
                }

        modelSnapshot.palantiri
                .map { it.value }
                .forEach { snapshot ->
                    (palantiri[snapshot.id] as? PalantirSprite)?.let {
                        if (snapshot.isRemoved) {
                            palantiri.remove(snapshot.id)
                        } else {
                            it.snapshot = snapshot
                        }
                    } ?: palantiri.put(snapshot.id, PalantirSprite(this, snapshot))
                }

        // Ensure that no sprites exist that are no longer
        // represented in the current model snapshot.
        beings.filterKeys { !modelSnapshot.beings.containsKey(it) }
                .forEach { beings.remove(it.key) }

        palantiri.filterKeys { !modelSnapshot.palantiri.containsKey(it) }
                .forEach { palantiri.remove(it.key) }
    }

    /**
     * Release resources before shutting down.
     */
    override fun onDetachedFromWindow() {
        beings.forEach {
            //TODOx should call stopAnimator...
            if (it.value.animator.isRunning) {
                it.value.animator.cancel()
            }
        }
        palantiri.forEach {
            //TODOx should call stopAnimator...
            if (it.value.animator.isRunning) {
                it.value.animator.cancel()
            }
        }
        PreferenceProvider.removeListener(this)
        super.onDetachedFromWindow()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (!oRealized || (beings.count() == 0 && palantiri.count() == 0)) {
            return super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }

        val ancestor = findAncestor { oRealized } ?:
                       return super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val maxWidth = ancestor.width
        val maxHeight = ancestor.height

        if (maxWidth == 0 || maxHeight == 0) {
            return super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }

        val rect = measureRect
        rect.left = paddingLeft
        rect.top = paddingTop
        rect.right = maxWidth - paddingRight
        rect.bottom = maxHeight - paddingBottom

        debugPrintSizesAndRanges()

        // Layout all sprites in the specified bounds.
        doLayout(rect)

        // Declared as var for possible orientation swap later.
        var desiredWidth: Int
        var desiredHeight: Int

        val desiredBeingsHeight =
                if (beings.count() == 0) {
                    0
                } else {
                    beings.values.last().bounds.oBottom +
                    spriteMargin +
                    oPaddingBottom
                }

        val desiredPalantiriHeight =
                if (palantiri.count() == 0) {
                    0
                } else {
                    palantiri.values.last().bounds.oBottom +
                    spriteMargin +
                    oPaddingBottom
                }

        desiredWidth = maxWidth
        desiredHeight = max(maxHeight,
                            max(desiredBeingsHeight,
                                desiredPalantiriHeight))

        // If landscape, then need to swap reversed width/height
        // for getSize and setMeasureDimension calls below.
        if (landscape) {
            val swap: Int = desiredWidth
            desiredWidth = desiredHeight
            desiredHeight = swap
        }

        // Now desiredWidth and desiredHeight are no longer reversed.
        val resolvedWidth = View.resolveSizeAndState(
                View.MeasureSpec.getSize(desiredWidth), widthMeasureSpec, 0)
        val resolvedHeight = View.resolveSizeAndState(
                View.MeasureSpec.getSize(desiredHeight), heightMeasureSpec, 0)

        setMeasuredDimension(resolvedWidth, resolvedHeight)

        // TODOx: not working still...
        // For some reason, the view is not redrawn after a rotation
        // so force a redraw.
        postInvalidate()
    }

    /**
     * Determines the maximum number of beings and palantiri
     * that can be displayed at the current display size and
     * resolution.
     *
     * We want to determine the maximum sprite counts based on
     * the orientation that supports the fewest sprites at the
     * minimum size. This ensures that when the device is rotated
     * the maximum number of sprites will always be displayable
     * and will not force new maximums which may then require
     * stopping a running simulation.
     */
    private fun calcMaxSpriteCounts(): Pair<Int, Int> {
        val (portraitBeingCount, portraitPalantirCount) =
                calcMaxSpriteCounts(true)

        val (landscapeBeingCount, landscapePalantirCount) =
                calcMaxSpriteCounts(false)

        // Chose minimum value so rotations don't change
        // the maximum counts.
        val maxBeingCount =
                min(portraitBeingCount, landscapeBeingCount)
        val maxPalantirCount =
                min(portraitPalantirCount, landscapePalantirCount)

        // Run all shared pref updates in a protected block
        // that will prevent processing of shared pref update
        // notifications while we are in an inconsistent
        // state.
        internalSharedPrefUpdate {
            // Now set min/max shared pref values.
            Settings.beingCountRange = Range(1, maxBeingCount)
            if (Settings.beingCount > maxBeingCount) {
                Settings.beingCount = maxBeingCount
            }

            Settings.palantirCountRange = Range(1, maxPalantirCount)
            if (Settings.palantirCount > maxPalantirCount) {
                Settings.palantirCount = maxPalantirCount
            }
        }

        return Pair(maxBeingCount, maxPalantirCount)
    }

    /**
     * Determines the maximum number of beings and palantiri
     * that can be displayed in the specified orientation
     * (true for portrait and false for landscape).
     *
     * There is a problem with trying to get the portrait
     * displayable area when in landscape mode. The action
     * bar size returned in landscape is smaller than in
     * portrait which results in the availableSize being
     * about 20 pixels larger than the actual height of this
     * view in portrait mode. But since we the calling
     * function will choose the minimum count value from
     * both orientations, this miscalculation does not cause
     * and adverse effect.
     */
    private fun calcMaxSpriteCounts(portraitMode: Boolean)
            : Pair<Int, Int> {
        val minBeingSize = MIN_BEING_SIZE
        val minPalantirSize = (palantirScale * minBeingSize).toInt()
        val minMargin = spriteMargin
        val displaySize = context.getDisplaySize()

        val portraitSize =
                if (portrait) {
                    displaySize.height - context.getSystemBarsSize()
                } else {
                    displaySize.width - context.getPortraitSystemBarsSize()
                }

        val landscapeSize =
                if (landscape) {
                    displaySize.width
                } else {
                    displaySize.height
                }

        println("calcMaxSpriteCounts: portraitSize = $portraitSize " +
                "landscapeSize = $landscapeSize DIFF = " +
                "${abs(portraitSize - landscapeSize)}")

        val size =
                if (portraitMode) {
                    portraitSize
                } else {
                    landscapeSize
                }

        // Use maximum state text size so that text size can
        // be changed without altering the maximum count of
        // sprites.
        val stateSize =
                if (portraitMode) {
                    calcStateBounds(MAX_STATE_TEXT_SIZE).height
                } else {
                    calcStateBounds(MAX_STATE_TEXT_SIZE).width
                }

        // Include optional state text maximum size in the
        // calculation so that the text size can be maximized
        // without invoking a change in the maximum number of
        // sprites.
        var maxBeingCount = if (portraitMode) {
            val minSize = minBeingSize + minMargin + stateSize
            ((size - minMargin).toFloat() / minSize).toInt()
        } else {
            // Convert minBeingSize (height) to width.
            val spriteSize =
                    calcMaxSpriteSize(beings,
                                      height = minBeingSize.toFloat(),
                                      withState = true)
            val minSize = max(spriteSize.width.toInt(), stateSize) + minMargin
            ((size - minMargin).toFloat() / minSize).toInt()
        }

        // Restrict to a hard maximum.
        maxBeingCount = min(maxBeingCount, MAX_BEING_COUNT)

        require(maxBeingCount > 0) {
            "minimum being, margin, and state " +
            "sizes are too large to display a being"
        }

        // Currently, min palantir size should always be
        // less than or equal to min being size.
        require(minPalantirSize <= minBeingSize) {
            "minPalantirSize <= minBeingSize " +
            "$minPalantirSize <= $minBeingSize"
        }

        // If the minimum palantir size is <= minimum being size
        // then palantir count will be the same as the being count
        // since adjacently gazing beings can't overlap. If the
        // minimum palantir size is greater than the minimum being
        // size then the palantir count is simply bound by how many
        // will fit in the viewable area.
        var maxPalantirCount =
                if (minPalantirSize <= minBeingSize) {
                    maxBeingCount // should always be the case
                } else {
                    ((size - minMargin).toFloat() /
                     (minPalantirSize + minMargin + stateSize)).toInt()
                }

        // Restrict to a hard maximum.
        maxPalantirCount = min(maxPalantirCount, MAX_PALANTIR_COUNT)

        require(maxPalantirCount > 0) {
            "Minimum palantir , margin, and state " +
            "sizes are too large to display a palantir"
        }

        return Pair(maxBeingCount, maxPalantirCount)
    }

    /**
     * Sets sensible max range values and counts to guarantee that all
     * all beings and palantiri can be displayed within the current
     * view extents and resolution. Note that since beings height > width
     * and palantiri are square, the landscape ranges are the same as
     * the portrait to support rotation changes without changing the
     * sized and ranges which provides a more natural look and feel.
     */
    private fun setSizesAndRanges() {
        if (!realized) {
            return
        }

        // Update the maximum state text size from the latest snapshot.
        updateMaxStateSize()

        // Need a valid size in the fixed dimension.
        require(width != 0 && height != 0) {
            "setSharedPrefSizes() should only be " +
            "called once the view has been realized"
        }

        val size = Size(width, height)

        // Calculate sprite ranges for the current number
        // of displayed sprites. Returned ranges are in
        // pixels so they have to converted to dp before
        // saving as shared prefs
        //
        val (beingSizeRange, palantirSizeRange) =
                calcMaxSpriteSizes(size, spriteMargin)

        // All shared preference updates performed in this
        // block will prevent this class from reacting to
        // the change notification thereby avoiding unnecessary
        // layouts and inconsistent sizes and ranges.
        internalSharedPrefUpdate {
            if (Settings.beingSizeRange != beingSizeRange)
                Settings.beingSizeRange = beingSizeRange

            // Being max size check.
            if (Settings.autoScale) {
                // Auto scaling: being size is always set to the maximum.
                if (Settings.beingSize != Settings.beingSizeRange.max) {
                    Settings.beingSize = Settings.beingSizeRange.max
                }
            } else {
                // Resizeable being size check.
                Settings.beingSizeRange.fit(Settings.beingSize) {
                    // Called only if passed value was clipped to bounds.
                    Settings.beingSize = it
                }
            }

            if (Settings.palantirSizeRange != palantirSizeRange) {
                Settings.palantirSizeRange = palantirSizeRange
            }

            // Palantir max size check.
            if (Settings.autoScale) {
                // With auto scaling, palantir is always set to the maximum.
                if (Settings.palantirSize != Settings.palantirSizeRange.max) {
                    Settings.palantirSize = Settings.palantirSizeRange.max
                }
            } else {
                // Resizeable palantir size check.
                Settings.palantirSizeRange.fit(Settings.palantirSize) {
                    // Called only if passed value was clipped to bounds.
                    Settings.palantirSize = it
                }
            }
        }

        // The maximum sprite dimensions can be pre-calculated here
        // since they won't change until the next call to this
        // function; this speeds up layouts and draws.
        //TODOx: should this calc call use param withState = true ?
        maxBeingDim = calcMaxSpriteSize(beings, -1f).toSize()
        maxPalantirDim = calcMaxSpriteSize(palantiri, -1f).toSize()

        require(validateRanges(Size(width, height))) {
            "validateRanges failed!"
        }

        debugPrintSizesAndRanges()
    }

    /**
     * Requires a single assumption: a palantir can be a maximum of 1/2 the
     * being height, and since a being being's width is roughly 1/2 it's
     * height, and since the palantir is circular (square area) then
     * it must also be about 1/4 of the being width. This rule makes it
     * possible to determine the max being height in both portrait and
     * landscape modes.
     */
    private fun calcMaxSpriteSizes(layout: Size,
                                   minMargin: Int)
            : Pair<Range<Int>, Range<Int>> {
        // The maximum size (height) of all sprites are a factor
        // of the fixed dimension (width for portrait, height for
        // landscape). To produce the same maximum size in both
        // portrait and landscape, the title bar height is added
        // to the landscape layout height so that it will be the
        // same size as the portrait layout width which has no
        // system bars.
        val maxSize = (layout.oWidth * 0.5f).toInt()
        // Set maximum being height as a ratio of fixed dimension.
        val maxBeingHeight = (beingScale * maxSize).toInt()
        // Overwrite shared pref range upper bound.
        var beingRange = Range(MIN_BEING_SIZE, maxBeingHeight)

        if (landscape) {
            // All subsequent sizing calculations are done
            // in portrait mode, so convert range to portrait.
            beingRange = heightRangeToWidthRange(beings, beingRange)
        }

        // Call common sprite size helper to calculate the required
        // min/max range and margin required to view the current
        // number of sprites. The returned range min/max values
        // will always fit in the passed range.
        var (beingSizeRange, _) =
                calcSpriteSizeRange(layout.oHeight,
                                    beings,
                                    spriteMargin,
                                    beingRange)

        // Max palantir height is always scaled to max being height
        // so adjust range upper bound to be scaled to the possibly
        // new being upper bound.

        val palantirRange =
                calcScaledPalantirSizeRange(
                        beingSizeRange, palantirScale, portrait)

        var (palantirSizeRange, palantirMargin) =
                calcSpriteSizeRange(layout.oHeight,
                                    palantiri,
                                    spriteMargin,
                                    palantirRange)

        // Determine the maximum being size that will ensure that
        // adjacently gazing beings do not overlap. Don't remove
        // state size since it's included in the palantirSizeRange.max
        // value.
        val maxGazingSize = palantirSizeRange.max +
                            palantirMargin -
                            minMargin

        // Bound being size by the minimum of the maximum
        // gazing size and the maximum being size returned
        // from the calcSpriteSizeRange function.
        if (maxGazingSize < beingSizeRange.max) {
            beingSizeRange = Range(MIN_BEING_SIZE, maxGazingSize)


            if (Settings.autoScale) {
                // Scale palantir size range to
                // new being size range.
                palantirSizeRange =
                        calcScaledPalantirSizeRange(
                                beingSizeRange,
                                palantirScale,
                                portrait)
            }
        }

        // For landscape mode, convert ranges
        // back from widths to heights.
        if (landscape) {
            palantirSizeRange =
                    widthRangeToHeightRange(
                            palantiri, palantirSizeRange)
            beingSizeRange =
                    widthRangeToHeightRange(beings, beingSizeRange)
        }

        return Pair(beingSizeRange, palantirSizeRange)
    }


    /**
     * !!!!! GOOGLE DEVELOPER !!!!!!
     *
     * this is the function that never
     * shows any local variables, only the "this" is
     * available when you step through this function.
     * I tried inlining the whole function as a normal
     * block of code in the calling function, but even
     * as a local code block is still doesn't show local
     * variables.
     */
    private fun calcSpriteSizeRange(layoutSize: Int,
                                    sprites: Map<Long, Sprite>,
                                    minMargin: Int,
                                    range: Range<Int>)
            : Pair<Range<Int>, Int> {
        // Extract the number of sprites. To handle case where
        // the sprites map is empty, just use a count of 1 so
        // that we return reasonable range values and the view
        // will still function normally with the missing sprites.
        val count = max(1, sprites.count())

        // Divide available height by number of sprites
        // yielding the maximum area available for each
        // sprite with its margin.
        var spriteBounds = layoutSize / count.toFloat()

        // Sanity check.
        require(spriteBounds * count <= layoutSize) {
            val result = spriteBounds * count <= layoutSize
            "Algorithm incorrect: $spriteBounds * " +
            "$count == $result <= $layoutSize"
        }

        // This bounds value is a little bigger than the actual
        // target of a sprite + margin because the leading margin
        // required before the first sprite (n sprites requires
        // n + 1 margins) so we need to reduce this bound to
        // account for this missing margin value.
        val adjustedMargin = minMargin + (minMargin / count.toFloat())
        spriteBounds -= adjustedMargin

        // After removing the reduced margin, we finally have
        // the actual maximum allowable size for a sprite and
        // its optional label that will guarantee that all
        // sprites will be fully visible in the display area.
        val size = spriteBounds

        // Sanity check.
        require((size * count) + minMargin * (count + 1)
                <= layoutSize) {
            val result = size * count + minMargin * (count + 1)
            "Algorithm incorrect: $size * $count + " +
            "$minMargin * ($count + 1) == $result <= $layoutSize"
        }

        // Adjust this size to be no larger than a scaled
        // percent of the fixed dimension.
        // Use a 2nd val size2 as per Google issue tracker
        // https://issuetracker.google.com/issues/71029850
        // or else debugger will not show local variables in
        // this function!
        val size2 = min(size, range.max.toFloat())

        // Sanity check.
        require(size2 > 0) {
            "Maximum palantir size ${size2.toInt()} > 0."
        }

        // Adjust minimum size if it now exceeds the maximum size.
        val minSize =
                if (range.min > size2) {
                    size2
                } else {
                    range.min.toFloat()
                }

        // Safest way to determine the max margin is by
        // straight math (not using previous values).
        val margin = (layoutSize - (size2 * count)) / (count + 1)

        // Round down to ensure that total size is <= layoutSize
        val adjustedRange = Range(minSize.toInt(), size2.toInt())
        val requiredSize =
                calcRequiredLayoutSize(count, adjustedRange.max, margin.toInt())
        require(requiredSize <= layoutSize) {
            "requiredSize <= layoutSize -> " +
            "$requiredSize <= $layoutSize"
        }

        return Pair(adjustedRange, margin.toInt())
    }

    /**
     * Calculates the maximum palantir range based on a scale
     * factor applied to the passed [beingRange]. The returned
     * range orientation will match the passed [range] orientation.
     * For proper scaling based on image appearance, the optional
     * state text size must be removed from all sizes before
     * scaling and then added back after.
     */
    private fun calcScaledPalantirSizeRange(
            beingRange: Range<Int>,
            scale: Float, portraitMode:
            Boolean = true): Range<Int> {
        return if (portraitMode) {
            // Removing optional state height, scale, and then
            // restore the optional state height.
            beingRange.shift(-stateBounds.height)
                    .scale(scale)
                    .shift(stateBounds.height)
        } else {
            // Convert to portrait so that we have a being heights.
            val heightRange = widthRangeToHeightRange(beings, beingRange)
            // Removing optional state height, scale, and then
            // restore the optional state height.
            val palantirRange =
                    heightRange.shift(-stateBounds.height)
                            .scale(scale)
                            .shift(stateBounds.height)
            // Convert back to portrait width sizes.
            heightRangeToWidthRange(palantiri, palantirRange)
        }
    }

    /**
     * Helper that scales the palantir to the passed [beingSize].
     * This is tricky because the optional state size can't be
     * included in this calculation, i.e., we just want the
     * palantir image to look scaled to the being image, but the
     * passed beingSize includes the optional state height.
     */
    private fun scalePalantirToBeingSize(beingSize: Int, scale: Float): Int {
        return (scale * (beingSize - stateBounds.height)).toInt()
    }

    /**
     * Converts a height range to a width range. The passed
     * [heightRange] must be in px units and the return
     * value will also be in px units.
     *
     * @return A width range value converted from the passed
     * [heightRange].
     */
    private fun heightRangeToWidthRange(sprites: Map<Long, Sprite>,
                                        heightRange: Range<Int>): Range<Int> {
        val minSize = calcMaxSpriteSize(sprites,
                                        heightRange.lower.toFloat())
        val maxSize = calcMaxSpriteSize(sprites,
                                        heightRange.upper.toFloat())
        return Range(minSize.width.roundToInt(),
                     maxSize.width.roundToInt())
    }

    /**
     * Converts a width range to a width range. The passed
     * [widthRange] must be in px units and the return
     * value will also be in px units.
     *
     * @return A width range value converted from the passed
     * [widthRange].
     */
    private fun widthRangeToHeightRange(sprites: Map<Long, Sprite>,
                                        widthRange: Range<Int>): Range<Int> {
        // Calculate the maximum sprite dimensions at the current
        // height so that we have the width to height ratio of the
        // largest (widest) bounding box. The passed height is
        // irrelevant since we just want the resulting ratio.
        val size = calcMaxSpriteSize(sprites, 10000f)

        // Now apply the ratio to scale the width range values
        // to heights.
        val aspect = size.height / size.width
        return Range((widthRange.lower * aspect).roundToInt(),
                     (widthRange.upper * aspect).roundToInt())
    }

    /**
     * @return The dimensions of the widest sprite in the passed
     * [sprites] group where all sprites are scaled to the passed
     * [height] or the default height if height is -1. If [withState]
     * is specified, then, this passed flag determines if the state
     * label should be included in calculations. When [withState] is
     * not specified, it is set to the default Settings.showStates
     * value which then determines whether or not the state label
     * dimensions should be included in the calculation.
     */
    private fun calcMaxSpriteSize(sprites: Map<Long, Sprite>,
                                  height: Float = -1f,
                                  withState: Boolean = Settings.showStates): SizeF {
        // Return any non-zero values when the sprites map
        // is empty. This ensures that the view will still
        // function normally even when beings or palantiri
        // fail to be created by the model.
        if (sprites.isEmpty()) {
            // Size doesn't matter, just as
            // long as they are non-zero values.
            return SizeF(100f, 100f)
        }

        // If request is to use current height (height == -1)
        // then get the sprite height from the first sprite.
        val actualHeight =
                if (height == -1f) {
                    sprites.values.first().size.toFloat()
                } else {
                    height
                }

        val size = sprites.map {
            it.value.calcDimensions(actualHeight, withState)
        }.maxBy { it.width }

        return size ?: error("calcMaxSpriteSize - empty sprites parameter.")
    }

    /**
     * Updates the maximum state text width and height which
     * is based on the longest enumerated state name length.
     * For faster layout and draw times, this method should
     * be called each to a new model snapshot is received and
     * not from any layout or draw calls.
     */
    private fun updateMaxStateSize() {
        maxStateSize = calcStateSize(Settings.stateSize.toFloat())
        maxStateBounds = calcStateBounds(Settings.stateSize.toFloat())
    }

    /**
     * @return The state text sizing [Rect] for the
     * passed font [size].
     */
    private fun calcStateSize(size: Float): Size {
        // First update the paint object to the latest
        // text size shared preference value.
        statePaint.textSize = size

        // Now get the text size info for the longest
        // enumerated state name
        val sizeRect = State.values().map {
            val text = it.name.toLowerCase()
            val bounds = Rect()
            statePaint.getTextBounds(text, 0, text.length, bounds)
            bounds
        }.reduce { max, rect -> max.union(rect); max }

        return sizeRect.toSize()
    }

    /**
     * @return The state text bounding [Rect] for the
     * passed font [size]. The bounding rectangle is the
     * same as the stateSize but with a vertical margin
     * added.
     */
    private fun calcStateBounds(size: Float): Size {
        val stateSize = calcStateSize(size)
        return Size(stateSize.width, stateSize.height + stateMargin)
    }

    /**
     * Calculates all being and palantiri bounding rectangles.
     * If device is in landscape mode, the bounding rectangles
     * passed to the being and palantir calculation methods are
     * rotated by 90 degrees so that all calculations can be
     * performed in portrait mode.
     */
    private fun doLayout(bounds: Rect) {
        // If we have no beings and no palantiri then
        // there's nothing to layout so just return.
        if (beings.count() == 0 && palantiri.count() == 0) {
            return
        }

        // Do the sprite layouts now.
        layoutRect.set(bounds)

        // Set bounds for beings layout.
        layoutRect.oRight /= 2

        // First layout beings.
        if (beings.isNotEmpty()) {
            doSpritesLayout(layoutRect,
                            beings,
                            maxBeingDim,
                            spriteMargin)
        }

        // Assuming that being size should remain constant,
        // determine the minimum margin to use for spacing
        // palantiri so that adjacent gazing beings will not
        // overlap.
        val minPalantirMargin =
                if (maxBeingDim.oHeight < maxPalantirDim.oHeight) {
                    spriteMargin
                } else {
                    spriteMargin +
                    maxBeingDim.oHeight -
                    maxPalantirDim.oHeight
                }

        // Shift bounds for palantiri layout.
        layoutRect.oOffset(layoutRect.oRight, 0)

        // Now layout palantiri.
        if (palantiri.isNotEmpty()) {
            doSpritesLayout(layoutRect,
                            palantiri,
                            maxPalantirDim,
                            minPalantirMargin)
        }
    }

    /**
     * Agnostic sprite calculation of bounding boxes (for both beings and
     * palantiri). Each sprite group is aligned in a single row or column
     * in the center of the passed bounding box. The row or column is spaced
     * out to evenly distribute the sprites to use all the available space.
     */
    private fun doSpritesLayout(boundingBox: Rect,
                                sprites: Map<Long, Sprite>,
                                size: Size,
                                minMargin: Int) {
        if (sprites.isEmpty()) {
            return
        }

        // Determine the best margin to use to separate sprites
        // so that they are spread out evenly in the current orientation.

        val count = sprites.size
        val avail = boundingBox.oHeight
        val used = count * (minMargin + size.oHeight) + minMargin
        val margin =
                if (used < avail) {
                    minMargin + (avail - used) / (count + 1)
                } else {
                    minMargin
                }

        require(minMargin <= margin) {
            "Adjusted margin $margin exceeds minimum margin $minMargin"
        }

        spriteLayoutRect.set(boundingBox)
        spriteLayoutRect.oBottom = spriteLayoutRect.oTop + size.oHeight
        spriteLayoutRect.oOffset(0, margin)

        // For landscape, vertically center the the sprites.
        if (landscape) {
            spriteLayoutRect.top +=
                    ((spriteLayoutRect.height() - size.height) / 2f).toInt()
            spriteLayoutRect.bottom =
                    spriteLayoutRect.top + size.height
        }

        // Set bounds of first sprite.
        with(sprites.values.first()) {
            bounds.set(0, 0, width, height)
            require(bounds.oWidth <= spriteLayoutRect.oWidth) {
                error("First sprite too wide for display")
            }
            // Center the sprite's bounding rectangle
            // within the layout rectangle which will either scale
            // up or down the sprites size.
            require(bounds.oWidth == max(stateSize.oWidth, oWidth))
            require(bounds.oWidth <= spriteLayoutRect.oWidth)
            bounds.centerInside(spriteLayoutRect)

            // Since the state text size is a tunable shared pref
            // value, adjust the width of this scaled bounding
            // rectangle to fit the widest state label.
            if (bounds.width() < stateBounds.width) {
                val widthInset = (bounds.width() - stateBounds.width) / 2f
                bounds.inset(widthInset.toInt(), 0)
            }
        }

        // Set bounds for rest of the sprites.
        sprites.values.filterIndexed { i, _ -> i > 0 }
                .forEachIndexed { i, it ->
                    spriteLayoutRect.oOffset(0, size.oHeight + margin)
                    it.bounds.set(0, 0, it.width, it.height)
                    it.bounds.centerInside(spriteLayoutRect)

                    // Since the state text size is a tunable shared pref
                    // value, adjust the width of this scaled bounding
                    // rectangle to fit the widest state label.

                    if (it.bounds.width() < stateBounds.width) {
                        val widthInset = (it.bounds.width() - stateBounds.width) / 2f
                        it.bounds.inset(widthInset.toInt(), 0)
                    }
                    require(it.bounds.oBottom <= boundingBox.oBottom) {
                        "sprite $i height = ${it.bounds.oBottom} " +
                        "> ${boundingBox.oBottom}"
                    }
                }
    }

    /**
     * Handle framework draw request.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        palantiri.values.forEach {
            it.draw(canvas)
        }

        beings.values.forEach {
            it.draw(canvas)
        }

        if (Settings.showWireFrames) {
            canvas.drawRulers(width, height)
        }
    }

    /**
     * Used when internal updating shared preferences so that
     * change notifications can be ignored.
     */
    private fun internalSharedPrefUpdate(block: () -> Unit) {
        userAction = false
        block()
        userAction = true
    }

    /**
     * React to shared preference changes. Post is used
     * to request a layout or to invalidate so that this
     * class can change any of these settings from
     * onMeasure() or onDraw(). This is done a setting
     * value is invalid for the current view type or
     * resolution.
     *
     * Note that Being and Palantir count changes are not reacted
     * to via observing shared pref changes because this view should
     * only know about these counts via thie model update mechanism.
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?,
                                           key: String?) {
        // Temporarily set by setSharedPrefSizes().
        if (userAction) {
            with(Settings) {
                when (key) {
                    SIMULATION_PALANTIRI_SIZE_PREF,
                    SIMULATION_BEING_SIZE_PREF,
                    SIMULATION_STATE_SIZE_PREF,
                    SIMULATION_AUTO_SCALE_PREF,
                    SIMULATION_SPRITE_PREF,
                    SIMULATION_SHOW_STATES_PREF -> {
                        // Shared prefs that may affect layout sizing.
                        setSizesAndRanges()
                        post { requestLayout() }
                    }
                    SIMULATION_SHOW_SPRITES_PREF,
                    SIMULATION_SHOW_WIRE_FRAMES_PREF,
                    SIMULATION_SHOW_PROGRESS_PREF,
                    SIMULATION_SHOW_PATHS_PREF -> post { invalidate() }
                    else -> {
                    }
                }
            }
        }
    }

    private fun calcRequiredLayoutSize(
            count: Int, size: Int, margin: Int): Int {
        return count * size + margin * (count + 1)
    }

    /**
     * Validates all beings and palantiri ranges and sizes.
     * To support 0 beings and 0 palantiri, when either is
     * detected, it doesn't really matter what the sizes
     * and ranges are since they will never be used until
     * that sprite count is > 0. So don't validate 0 count
     * sprite groups.
     */
    private fun validateRanges(bounds: Size): Boolean {
        if (beings.count() > 0) {
            validateBeingLayout(bounds)
        }

        if (palantiri.count() > 0) {
            validatePalantirLayout(bounds)
        }
        return true
    }

    /**
     * Validate being sizes and range.
     */
    private fun validateBeingLayout(bounds: Size) {
        val curCount = beings.count()
        val curSize = Settings.beingSize
        val maxCount = Settings.beingCountRange.upper
        val sizeRange = Settings.beingSizeRange
        val minSize = sizeRange.lower
        val maxSize = sizeRange.upper

        require(maxCount <= MAX_BEING_COUNT) {
            "maxCount < MAX_BEING_COUNT [$maxCount < $MAX_BEING_COUNT]"
        }

        require(curCount <= maxCount) {
            "curCount < MAX_BEING_COUNT [$curCount < $MAX_BEING_COUNT]"
        }

        validateRanges("Being",
                       curCount,
                       curSize,
                       maxCount,
                       minSize,
                       maxSize)

        if (landscape) {
            val minWidth =
                    calcMaxSpriteSize(
                            beings, minSize.toFloat()).width.toInt()
            val curWidth =
                    calcMaxSpriteSize(
                            beings, curSize.toFloat()).width.toInt()

            validateMinMaxLayouts("Being",
                                  bounds,
                                  curCount,
                                  curWidth,
                                  spriteMargin,
                                  maxCount,
                                  minWidth)
        }
    }

    /**
     * Validate palantiri sizes and range.
     */
    private fun validatePalantirLayout(bounds: Size) {
        val curCount = palantiri.count()
        val curSize = Settings.palantirSize
        val maxCount = Settings.palantirCountRange.upper
        val sizeRange = Settings.palantirSizeRange
        val minSize = sizeRange.lower
        val maxSize = sizeRange.upper

        require(maxCount <= MAX_BEING_COUNT) {
            "maxCount < MAX_BEING_COUNT [$maxCount < $MAX_BEING_COUNT]"
        }

        require(curCount <= maxCount) {
            "curCount < MAX_BEING_COUNT [$curCount < $MAX_BEING_COUNT]"
        }

        validateRanges("Palantir",
                       curCount,
                       curSize,
                       maxCount,
                       minSize,
                       maxSize)

        if (landscape) {
            val minWidth =
                    calcMaxSpriteSize(
                            palantiri, minSize.toFloat()).width.toInt()
            val curWidth =
                    calcMaxSpriteSize(
                            palantiri, curSize.toFloat()).width.toInt()

            validateMinMaxLayouts("Palantir",
                                  bounds,
                                  curCount,
                                  curWidth,
                                  spriteMargin,
                                  maxCount,
                                  minWidth)
        }
    }

    /**
     * Common function to validate any type of sprite size and range.
     */
    private fun validateRanges(name: String,
                               curCount: Int,
                               curSize: Int,
                               maxCount: Int,
                               minSize: Int,
                               maxSize: Int): Boolean {
        require(curCount in 1..(maxCount)) {
            "$name: count in 1..(maxCount): $curCount in 1..($maxCount)"
        }

        // Check for valid palantir range.
        require(minSize <= maxSize) {
            "$name: min <= max: $minSize <= $maxSize"
        }

        // Check valid palantir current size.
        require(curSize in minSize..maxSize) {
            "$name: cur in min..max: $curSize in $minSize..$maxSize"
        }

        return true
    }

    private fun validateMinMaxLayouts(name: String,
                                      bounds: Size,
                                      curCount: Int,
                                      curSize: Int,
                                      margin: Int,
                                      maxCount: Int,
                                      minSize: Int): Boolean {

        // Check maximum layout.
        var size = calcRequiredLayoutSize(maxCount, minSize, margin)
        require(size <= bounds.oHeight) {
            "$name: size <= bounds.oHeight: $size <= ${bounds.oHeight}"
        }

        // Check current layout
        size = calcRequiredLayoutSize(curCount, curSize, margin)
        require(size <= bounds.oHeight) {
            "$name: curCount, curSize, margin -> size <= bounds.oHeight " +
            "$curCount, $curSize, $margin -> $size <= $bounds.oHeight"
        }

        return true
    }

    private fun debugPrintSizesAndRanges() {
        println("Being Stats: " +
                "beings.count() = ${beings.count()} " +
                "size=${Settings.beingSize} " +
                "range=[" +
                "${Settings.beingSizeRange.lower}, " +
                "${Settings.beingSizeRange.upper}]")
        println("Palantir Stats: " +
                "palantiri.count() = ${palantiri.count()} " +
                "size=${Settings.palantirSize} " +
                "range=[" +
                "${Settings.palantirSizeRange.lower}, " +
                "${Settings.palantirSizeRange.upper}]")
    }

    /**
     * Abstract base class for both Being and Palantir sprites.
     * The main reason for provide common layout properties
     * that can be accessed from the views onMeasure calls
     * and all beings and sprites can be processed using
     * the same layout logic.
     */
    abstract class Sprite(val view: SimulationView) : Drawable(), KtLogger {
        /** Pre-allocate for faster draws. */
        private val drawRect = Rect()
        private val _translatedBounds = Rect()

        abstract val drawable: BitmapDrawable
        abstract val animator: ValueAnimator
        abstract val rotation: Float
        abstract val translationX: Int
        abstract val translationY: Int
        abstract val stateName: String
        abstract val count: Int
        abstract val error: String
        abstract val progressColor: Int
        abstract val showProgressBar: Boolean
        abstract val showProgressCount: Boolean

        /** The sprite size in the orientation direction. */
        abstract val size: Int

        /** The current progress of the sprite as a value in [0..1]. */
        abstract val progress: Float

        /** width to height aspect ration. */
        private val aspect
            get() = drawable.bitmap.width / drawable.bitmap.height.toFloat()

        /** Image height is fixed to being size from shared prefs. */
        private val imageHeight
            get() = size - view.stateBounds.height

        /** Force image width to be relative to fixed height. */
        private val imageWidth
            get() = (imageHeight * aspect).toInt()

        /** Height of sprite including possible text label */
        val height
            get() = imageHeight + view.stateBounds.height

        /** Width of sprite including possible text label. */
        val width
            get() = max(imageWidth, view.stateBounds.width)

        /** The bounds with animator offset applied. */
        protected val boundsRect: Rect
            get() {
                _translatedBounds.set(bounds.left + translationX,
                                      bounds.top + translationY,
                                      bounds.right + translationX,
                                      bounds.bottom + translationY)
                return _translatedBounds
            }

        /**
         * @return the width of this sprite based on the passed [height]
         * value. This width is calculated to maintain the sprite's
         * bitmap aspect ratio. If [withState] is specified, then,
         * this passed flag determines if the state label should be
         * included in calculations. When [withState] is not specified,
         * it is set to the default Settings.showStates value which then
         * determines whether or not the state label dimensions
         * should be included in the calculation.
         */
        fun calcDimensions(height: Float,
                           withState: Boolean = Settings.showStates)
                : SizeF {
            // If show state is specified, then to get the proper
            // ratio of width to height, the state height has to be
            // removed prior to applying the aspect ratio.
            val h = if (withState) {
                height - view.maxStateBounds.height
            } else {
                height
            }

            val w = if (withState) {
                max((h * aspect), view.maxStateBounds.width.toFloat())
            } else {
                (h * aspect)
            }

            // Always return the passed height.
            return SizeF(w, height)
        }

        /** TRANSLUCENT drawable */
        override fun getOpacity(): Int {
            return PixelFormat.TRANSLUCENT
        }

        /** Unsupported */
        override fun setAlpha(alpha: Int) {
        }

        /** Unsupported */
        override fun setColorFilter(colorFilter: ColorFilter?) {
        }

        override fun draw(canvas: Canvas) {
            drawRect.set(0, 0, imageWidth, imageHeight)
            drawRect.centerHorizontally(boundsRect)

            if (Settings.showSprites) {
                val bitmap = drawable.bitmap
                val matrix = Matrix()
                matrix.postTranslate(-bitmap.width / 2f, -bitmap.height / 2f)
                matrix.postRotate(rotation)
                matrix.postScale(drawRect.width().toFloat() / bitmap.width,
                                 drawRect.height().toFloat() / bitmap.height)
                val left = drawRect.left + (drawRect.width() / 2f)
                val top = drawRect.top + (drawRect.height() / 2f)
                matrix.postTranslate(left, top)
                canvas.drawBitmap(bitmap, matrix, null)
                matrix.reset()
            }

            // Draw state if enabled.
            if (Settings.showStates) {
                drawState(canvas, stateName)
                drawProgress(canvas)
            }

            if (Settings.showWireFrames) {
                // Draw frame around image.
                drawFrame(canvas, drawRect, Color.RED, true)

                // Draw rect around total bounds.
                drawFrame(canvas, boundsRect, Color.BLUE, false)

                // Draw frame around margin area.
                drawRect.set(boundsRect)
                drawRect.oTop = drawRect.oBottom
                drawRect.oBottom = drawRect.oTop + view.spriteMargin
                drawFrame(canvas, drawRect, Color.RED)
            }
        }

        private val progressRect = Rect()
        private fun drawProgress(canvas: Canvas) {
            // -1 progress is used to prevent drawing.
            val oldColor = view.framePaint.color

            // Center progress bar horizontally over sprite
            progressRect.set(0, boundsRect.top, PROGRESS_SIZE)
            progressRect.centerHorizontally(boundsRect)

            // Shift progress bar above top of sprite
            val yOffset = PROGRESS_SIZE.height + 2.dpToPx.toInt()
            progressRect.offset(0, -yOffset)

            // Kludge for shorter Gollum sprite.
            val gollumIndex =
                    view.context.resources.getInteger(R.integer.gollumIndex)
            if (drawable.bitmap == view.beingDrawables[gollumIndex].bitmap) {
                progressRect.offset(0, (imageHeight / 4f).toInt())
            }

            if (showProgressBar) {
                // Draw the progress using a filled rectangle.
                view.progressPaint.color = progressColor
                val width = (PROGRESS_SIZE.width * progress).toInt()
                progressRect.right = progressRect.left + width
                canvas.drawRect(progressRect, view.progressPaint)

                // Draw the progress border.
                progressRect.right = progressRect.left + PROGRESS_SIZE.width
                view.framePaint.color = Color.BLACK
                canvas.drawRect(progressRect, view.framePaint)
            }

            if (showProgressCount) {
                // Draw the count as text to the right of the bar.
                val text = count.toString()
                view.statePaint.getTextBounds(text, 0, text.length, drawStateRect)

                var y = progressRect.top.toFloat() +
                        drawStateRect.height().toFloat() -
                        progressRect.height() / 2f

                val x = if (showProgressBar) {
                    // Draw to right of progress bar.
                    progressRect.right + 2.dpToPx
                } else {
                    // Shift up by a couple of pixels.
                    y -= 2.dpToPx
                    // Draw to in center of non-existent progress bar.
                    progressRect.left + (progressRect.width() - drawStateRect.width()) / 2f
                }

                canvas.drawText(text, x, y, view.statePaint)
            }

            // Cleanup.
            view.framePaint.color = oldColor
        }

        private val drawStateRect = Rect()
        private fun drawState(canvas: Canvas, text: String) {
            view.statePaint.textSize = Settings.stateSize.toFloat()
            view.statePaint.color =
                    ContextCompat.getColor(
                            view.context, R.color.primaryDarkColor)
            view.statePaint.getTextBounds(text, 0, text.length, drawStateRect)
            val ascent = drawStateRect.top

            val x: Float =
                    boundsRect.left +
                    (boundsRect.width() - drawStateRect.width()) / 2f
            val y: Float =
                    boundsRect.bottom -
                    (drawStateRect.height() + ascent).toFloat()

            if (Settings.showWireFrames) {
                drawStateRect.offsetTo(x.toInt(), y.toInt() + ascent)
                drawStateRect.left = boundsRect.left
                drawStateRect.right = boundsRect.right
                drawFrame(canvas, drawStateRect, Color.GREEN)
            }

            canvas.drawText(text, x, y, view.statePaint)
        }

        private val drawTextRect = Rect()
        private fun drawText(canvas: Canvas,
                             text: String,
                             xCenter: Float,
                             yCenter: Float,
                             color: Int = ContextCompat.getColor(
                                     view.context, R.color.primaryDarkColor),
                             size: Float = Settings.stateSize.toFloat()) {
            view.statePaint.textSize = size
            view.statePaint.color = color
            view.statePaint.getTextBounds(text, 0, text.length, drawTextRect)

            val x: Float = xCenter - drawTextRect.width() / 2f
            val y: Float = yCenter - drawTextRect.height() / 2f - drawTextRect.top

            canvas.drawText(text, x, y, view.statePaint)
        }

        private fun drawFrame(canvas: Canvas,
                              rect: Rect, color:
                              Int = Color.RED,
                              drawSizes: Boolean = false) {
            val oldColor = view.framePaint.color
            view.framePaint.color = color
            canvas.drawRect(rect, view.framePaint)
            view.framePaint.color = oldColor

            if (drawSizes) {
                var x = rect.right.toFloat()
                var y = rect.top + rect.height() / 2f
                drawText(canvas, rect.height().toString(), x, y, Color.BLACK, 8.dpToPx)

                x = rect.left + rect.width() / 2f
                y = rect.bottom.toFloat()

                drawText(canvas, rect.width().toString(), x, y, Color.BLACK, 8.dpToPx)
            }
        }

        /**
         * Returns a string representation of the object.
         */
        override fun toString(): String {
            return "size[$width,$height], " +
                   "rect[${boundsRect.left}, " +
                   "${boundsRect.top} - , " +
                   "${boundsRect.right}, " +
                   "${boundsRect.bottom}]"
        }
    }

    /**
     * Support extensions that allow doing all calculations
     * assuming portrait orientation.
     */
    private val Sprite.oWidth
        get() = if (portrait) width else height
    private val Sprite.oHeight
        get() = if (portrait) height else width

    /**
     * Recyclable being sprite drawable. All drawing properties
     * are calculated at draw time based on the sprites current
     * snapshot property which makes it unnecessary to wastefully
     * destroy and recreate sprites when the model changes. For
     * this to work, all drawing properties must be calculated
     * at draw time based on the current snapshot property.
     */
    class BeingSprite(view: SimulationView,
                      snapshot: BeingSnapshot) : Sprite(view) {
        /**
         * Sprite reuse is supported by simply updating the snapshot.
         * To ensure this reuse, all sprite drawing properties must
         * be calculated lazily based on the current snapshot value.
         */
        var snapshot: BeingSnapshot
                by Delegates.observable(snapshot) { _, old, new ->
                    if (new.state != old.state) {
                        // Clear all non-lazy properties here.
                        error = ""
                        updateAnimator(old.state, new.state)
                    }
                }

        /** Size is set as a shared pref from Settings panel. */
        override val size
            get() = Settings.beingSize

        /** Animation support */
        override val animator = ValueAnimator.ofFloat(1f, 0f)!!

        /** Being drawable is tunable via Shared Pref. */
        override val drawable
            get() = if (Settings.sprite == 0) {
                // Random sprite from a list.
                view.beingDrawables[id.toInt() % view.beingDrawables.size]
            } else {
                // Single sprite from the sprite list.
                val i = view.beingResIds.indices.first {
                    view.beingResIds[it] == Settings.sprite
                }
                view.beingDrawables[i]
            }

        /** Optional state label displayed below sprite. */
        override val stateName
            get() = state.name.toLowerCase()

        /** Rotation animation support. */
        override val rotation: Float
            get() = 0f

        /** Animation % complete. */
        override val progress: Float
            get() = snapshot.completed.toFloat() / snapshot.iterations

        /** Animated x offset. */
        override var translationX: Int = 0

        /** Animated y offset. */
        override var translationY: Int = 0

        /** Completed iterations. */
        override val count
            get() = snapshot.completed

        /** Only show bar if specified in settings. */
        override val showProgressBar
            get() = Settings.showProgress

        /** Always show progress count. */
        override val showProgressCount
            get() = true

        /** Progress bar fill color changes when complete. */
        override val progressColor
            get() = if (count == Settings.gazingIterations) {
                Color.GREEN
            } else {
                Color.RED
            }

        /**
         * An error string when simulation is in an illegal state.
         * Note that since this breaks the rule of all properities
         * being lazily based on the current snapshot value, (for
         * sprite reuse), it has to be updated by the snapshot
         * Observer property whenever the snapshot is updated.
         */
        override var error = ""

        /** Snapshot properties */
        val state get() = snapshot.state
        val type get() = snapshot.type
        val id get() = snapshot.id
        val prevState get(): BeingComponent.State? = snapshot.prevState
        val exception get() = snapshot.exception
        val message get() = snapshot.message
        val duration get() = snapshot.duration

        /** Being specific snapshot properties */
        internal val palantirId: Long
            get() = snapshot.palantirId

        private val animatedValue
            get() = when (state) {
                State.ACQUIRING -> animator.animatedValue as Float
                State.GAZING -> 1f
                State.RELEASING -> animator.animatedValue as Float
                else -> 0f
            }

        init {
            with(animator) {
                // Animator is toggled between [1..0] and [0..1]
                // so that both directions will run with a fast out
                // and slow in effect (don't use reverse here!).
                interpolator = FastOutSlowInInterpolator()
                duration = this@BeingSprite.duration
                addUpdateListener {
                    require(palantirId != -1L) {
                        "[palantirId=-1!!] snapshot = $snapshot"
                    }
                    val offset = MutableOffset()
                    calcOffset(palantirId, offset)
                    translationX = offset.x
                    translationY = offset.y
                    view.invalidate()
                }
            }
        }

        /**
         * Draw in its bounds (set via setBounds).
         *
         * @param canvas The canvas to draw into
         */
        override fun draw(canvas: Canvas) {
            // Draw path first so that default sprite draw handler will
            // draw over the portion that connects to the center of the
            // sprite.
            if (Settings.showPaths && translationX != 0 || translationY != 0) {
                drawPath(canvas,
                         bounds.exactCenterX(),
                         bounds.exactCenterY(),
                         boundsRect.exactCenterX(),
                         boundsRect.exactCenterY())
            }

            super.draw(canvas)
        }

        /**
         * Draw path dashed line from being resting spot to being current
         * location. For a better visual effect, the path should be drawn
         * before the being.
         */
        private val path = Path()

        private fun drawPath(canvas: Canvas, x1: Float, y1: Float, x2: Float, y2: Float) {
            path.reset()
            if (x1 != x2 || y1 != y2) {
                path.moveTo(x1, y1)
                path.lineTo(x2, y2)
                canvas.drawPath(path, view.pathPaint)
                canvas.drawCircle(x1, y1, view.pathAnchorRadius, view.statePaint)
            }
        }

        /** TRANSLUCENT drawable */
        override fun getOpacity(): Int {
            return PixelFormat.TRANSLUCENT
        }

        /** Unsupported */
        override fun setAlpha(alpha: Int) {
        }

        /** Unsupported */
        override fun setColorFilter(colorFilter: ColorFilter?) {
        }

        private fun calcOffset(palantirId: Long, offset: MutableOffset): MutableOffset {
            return view.palantiri[palantirId]?.let {
                it as PalantirSprite
                when (state) {
                    State.ACQUIRING,
                    State.RELEASING,
                    State.GAZING -> {
                        it.calcGazeOffset(bounds, offset).scale(animatedValue)
                    }
                    else -> {
                        offset.empty
                        offset
                    }
                }
            } ?: error("$snapshot - connected to an " +
                       "invalid palantiri id ($palantirId)")
        }

        /**
         * Called whenever a new snapshot is set. It starts
         * or stops the animator depending on the state change.
         */
        private fun updateAnimator(oldState: BeingComponent.State,
                                   newState: BeingComponent.State) {
            if (oldState != newState) {
                when (newState) {
                    State.ACQUIRING,
                    State.RELEASING,
                    State.GAZING -> {
                        //TODOx (fails sometimes): require(palantirId != -1L)
                        if (palantirId != -1L) {
                            startAnimator()
                        } else {
                            view.context.toast("Being $id has no palantir id!")
                            stopAnimator()
                        }
                    }
                    else -> {
                        stopAnimator()
                        //TODOx (fails sometimes): require(palantirId == -1L)
                    }
                }
            }
        }

        private fun startAnimator() {
            require(palantirId != -1L)
            stopAnimator()

            with(animator) {
                // To get the same animation effect in both directions
                // (fast-in slow-out) swap the start and end values
                // and restart the animation (reverse will produce a
                // slow-out fast-in which looks weird).
                when (state) {
                    State.RELEASING -> {
                        require(palantirId != -1L)
                        // To match the state duration accurately, the animation
                        // duration needs to be a little shorter to account for
                        // the snapshot delivery lag time.
                        duration = max(0, this@BeingSprite.duration -
                                          snapshot.elapsedTime)
                        stopPalantirAnimator()
                        setFloatValues(1f, 0f)
                        start()
                    }
                    State.ACQUIRING -> {
                        require(palantirId != -1L)
                        // To match the state duration accurately, the animation
                        // duration needs to be a little shorter to account for
                        // the snapshot delivery lag time.
                        duration = max(0, this@BeingSprite.duration -
                                          snapshot.elapsedTime)
                        setFloatValues(0f, 1f)
                        start()
                    }
                    State.GAZING -> {
                        require(palantirId != -1L)
                        // Stop any animation and fix the sprite translation
                        // to the gazing offset for the duration of the gazing
                        // operation.
                        cancel()
                        val offset = MutableOffset()
                        calcOffset(palantirId, offset)
                        translationX = offset.x
                        translationY = offset.y
                        startPalantirAnimator()
                    }
                    else -> {
                        error("startAnimator for invalid state $this")
                    }
                }
            }
        }

        private fun stopAnimator() {
            stopPalantirAnimator()
            if (animator.isRunning) {
                animator.cancel()
            }
            translationX = 0
            translationY = 0
        }

        private fun startPalantirAnimator() {
            require(state == State.GAZING)
            view.palantiri[palantirId]?.let {
                (it as PalantirSprite).startAnimator(duration)
            }
        }

        private fun stopPalantirAnimator() {
            view.palantiri[palantirId]?.let {
                (it as PalantirSprite).stopAnimator()
            }
        }

        /**
         * Returns a string representation of the object.
         */
        override fun toString(): String {
            return snapshot.toString() +
                   ", " +
                   super.toString() +
                   " animVal = $animatedValue"
        }
    }

    /**
     * Recyclable palantir sprite drawable. All drawing properties
     * are calculated at draw time based on the sprites current
     * snapshot property which makes it unnecessary to wastefully
     * destroy and recreate sprites when the model changes. For
     * this to work, all drawing properties must be calculated
     * at draw time based on the current snapshot property.
     */
    class PalantirSprite(view: SimulationView,
                         snapshot: PalantirSnapshot) : Sprite(view) {
        /**
         * Sprite reuse is supported by only updating the snapshot.
         * This means all sprite drawing properties must be calculated
         * lazily based on the current snapshot value.
         */
        var snapshot: PalantirSnapshot
                by Delegates.observable(snapshot) { _, _, _ ->
                    // Currently palantir do not have their own
                    // state, so always check to see if the animator
                    // needs to be stopped.
                    updateAnimator()
                }

        /** Size is set as a shared pref from Settings panel. */
        override val size
            get() = Settings.palantirSize

        /** Drawable (shared) is tunable via Shared Pref. */
        override val drawable: BitmapDrawable = view.palantirDrawable

        /** Animation support */
        override val animator = ValueAnimator.ofFloat(0f, 1f)!!

        var startRotation = 0f
        var currentRotation = 0f

        /** Rotation animation support. */
        override val rotation: Float
            get() {
                if (animator.isRunning) {
                    val duration = animator.duration
                    val rotations = duration / 3000f
                    val totalDegrees = rotations * 360f
                    val fraction = animator.animatedFraction
                    currentRotation = startRotation + totalDegrees * fraction
                }
                return currentRotation
            }

        /** Optional state label displayed below sprite. */
        override val stateName
            get() = when (state) {
                BeingComponent.State.ACQUIRING -> "acquired"
                BeingComponent.State.RELEASING -> "released"
                BeingComponent.State.GAZING -> "busy"
                else -> PalantirComponent.State.AVAILABLE.name.toLowerCase()
            }

        /** Not used. */
        override val translationX: Int = 0

        /** Not used. */
        override val translationY: Int = 0

        /** Animation % complete. */
        override val progress: Float
            get() = if (beingId == -1L) {
                0f
            } else {
                val being = view.beings[beingId] as BeingSprite
                when (being.state) {
                    State.GAZING -> {
                        //println("ANIMATOR: ${animator.animatedFraction}")
                        animator.animatedFraction
                    }
                    else -> 0f
                }
            }

        /**
         * Only show bar if specified in settings and simulation
         * is running.
         */
        override val showProgressBar
            get() = Settings.showProgress && view.simulationRunning

        /** Always show progress count. */
        override val showProgressCount
            get() = true

        /** Progress bar fill color changes when complete. */
        override val progressColor
            get() = Color.RED

        /**
         * Count of gazing occurrences. The model increments
         * the count as soon as the palantir has been acquired
         * by the being. However, to make the simulation look
         * more intuitive, return one less than the count for
         * the acquiring and gazing states, and then the real
         * count for the releasing state. This makes the count
         * look like it's updated when the progress bar reaches
         * 100%.
         */
        override val count
            get() = when (state) {
                BeingComponent.State.ACQUIRING,
                BeingComponent.State.GAZING -> snapshot.count - 1
                else -> snapshot.count
            }

        /** An error string when simulation is in an illegal state */
        override var error = ""

        /**
         * SPECIAL CASE: since a palantir does not currently have
         * an snapshot state (not currently set in the model layer)
         * the state is inferred from the palantir's associated
         * being state and not from the palantir snapshot.
         */
        private val state
            get() = if (beingId != -1L) {
                val being = view.beings[beingId] as BeingSprite
                when (being.state) {
                    BeingComponent.State.ACQUIRING,
                    BeingComponent.State.GAZING,
                    BeingComponent.State.RELEASING -> being.state
                    else -> PalantirComponent.State.AVAILABLE
                }
            } else {
                PalantirComponent.State.AVAILABLE
            }

        /** Snapshot properties */
        val type get() = snapshot.type
        val id get() = snapshot.id
        val prevState get() = snapshot.prevState
        val exception get() = snapshot.exception
        val message get() = snapshot.message
        /** Id of currently gazing being. */
        internal val beingId
            get() = snapshot.beingId

        init {
            with(animator) {
                interpolator = LinearInterpolator()
                addUpdateListener {
                    view.invalidate()
                }
            }
        }

        private fun updateAnimator() {
            if (animator.isRunning && beingId == -1L) {
                stopAnimator()
            }
        }

        fun startAnimator(milliseconds: Long) {
            stopAnimator()
            with(animator) {
                startRotation = currentRotation
                duration = milliseconds
                start()
            }
        }

        fun stopAnimator() {
            with(animator) {
                if (isRunning) {
                    cancel()
                }
            }
        }

        // Pre-allocate for faster draws.
        private val offset = MutableOffset()
        private val rect = Rect()

        /**
         * Draw in its bounds (set via setBounds).
         *
         * @param canvas The canvas to draw into
         */
        override fun draw(canvas: Canvas) {
            // For wire frames, show the palantir gazing
            // wire frame for a random being sprite size.
            if (Settings.showWireFrames && !view.beings.isEmpty()) {
                // Use as many beings as possible to help check
                // out visual alignment of all possible gazing beings.
                val index: Int =
                        view.palantiri.values
                                .takeWhile { it != this }
                                .count() % view.beings.count()

                val being = view.beings.values.filterIndexed { i, _ ->
                    i == index
                }.first()

                rect.set(being.bounds)
                calcGazeOffset(rect, offset)
                rect.offset(offset.x, offset.y)
                canvas.drawWireFrame(rect, Color.BLUE, true)

                // Draw frame around margin area.
                rect.oTop = rect.oBottom
                rect.oBottom = rect.oTop + view.spriteMargin
                canvas.drawWireFrame(rect, Color.RED)
            }

            super.draw(canvas)
        }

        /**
         * @returns Offset from center point of passed [fromRect]
         * to center point of that rectangle moved adjacent to
         * to the left side of this palantir rectangle with
         * bottoms aligned. A default spacing is enforced between
         * the two adjacent rectangles. This calculation accounts
         * for the width of any base line text label if the current
         * Settings.showStates shared pref value is true.
         */
        fun calcGazeOffset(fromRect: Rect,
                           offset: MutableOffset): MutableOffset {
            offset.empty

            // Determine the x and y offset required to translate
            // the being's idle position to it's gazing position.
            val x1 = fromRect.exactCenterX()
            val y1 = fromRect.exactCenterY()
            val y2: Float
            val x2: Float

            if (portrait) {
                x2 = bounds.exactCenterX() - (bounds.width() / 2f) -
                     view.beingPalantirMargin - (fromRect.width() / 2f)
                y2 = bounds.bottom - (fromRect.height() / 2f)
            } else {
                x2 = bounds.exactCenterX()
                y2 = bounds.exactCenterY() - (bounds.height() / 2f) -
                     view.beingPalantirMargin - (fromRect.height() / 2f)
            }

            return offset.set((x2 - x1).toInt(), (y2 - y1).toInt())
        }

        /**
         * Returns a string representation of the object.
         */
        override fun toString(): String {
            return snapshot.toString() +
                   ", " +
                   super.toString() +
                   " animVal = ${animator.animatedValue}"
        }
    }
}


fun Rect.immutable() = ImmutableRect(this)

fun Rect.set(rect: ImmutableRect) =
        this.set(rect.left, rect.top, rect.right, rect.bottom)

/**
 * Mutable offset class.
 */
class MutableOffset(var x: Int = 0, var y: Int = 0) {

    val empty get() = x == 0 && y == 0

    fun set(x: Int, y: Int): MutableOffset {
        this.x = x
        this.y = y
        return this
    }

    fun clear(): MutableOffset {
        x = 0
        y = 0
        return this
    }

    fun scale(fraction: Float) =
            set((fraction * x).toInt(), (fraction * y).toInt())

    val immutable
        get() = toOffset()

    fun toOffset() = Offset(x, y)

}

/**
 * Immutable offset class.
 */
data class Offset(val x: Int = 0, val y: Int = 0) {

    val empty
        get() = { x == 0 && y == 0 }

    val mutable
        get() = toMutableOffset()

    fun scale(fraction: Float) =
            Offset((fraction * x).toInt(), (fraction * y).toInt())

    fun toMutableOffset() = MutableOffset(x, y)

}

/**
 * Use class to make sure that rectangles aren't change.
 */
data class ImmutableRect(val left: Int, val top: Int, val right: Int, val bottom: Int) {

    constructor(rect: Rect) : this(rect.left, rect.top, rect.right, rect.bottom)

    val width
        get() = right - left

    val height
        get() = bottom - top

    fun exactCenterX(): Float {
        return (left + right) * 0.5f
    }

    fun exactCenterY(): Float {
        return (top + bottom) * 0.5f
    }

    fun toRect() = Rect(left, top, right, bottom)
}

private inline fun View.require(value: Boolean, lazyMessage: () -> Any) {
    if (Settings.modelChecker) {
        if (!value) {
            val message = lazyMessage()
            if (Settings.strictMode || message.toString().isEmpty()) {
                throw IllegalArgumentException(message.toString())
            } else {
                context.longToast(message.toString())
            }
        }
    }
}

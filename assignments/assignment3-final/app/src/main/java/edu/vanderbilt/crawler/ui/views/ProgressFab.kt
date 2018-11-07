package edu.vanderbilt.crawler.ui.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.isGone
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import edu.vanderbilt.crawler.extensions.LINEAR_OUT_SLOW_IN_INTERPOLATOR
import edu.vanderbilt.crawler.extensions.SHOW_HIDE_ANIM_DURATION
import edu.vanderbilt.crawler.ui.adapters.dpToPx
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.topPadding


/**
 * A circular loader is integrated with a floating action button.
 * It seems that if the anchor and anchor offset are set in XML
 * and this ProgressFab is a child of CoordinatorLayout, that for
 * some reason it assumes a default behvior adding/using the
 * custom one in this file is not necessary (and the default
 * performs much better), so it is no longer used. Should the need
 * arise it can be uncommented to be used.
 *
 * NOTE: This class is designed to look exactly like a normal 16dp
 * bottom|right (no anchoring) or top|right (anchored to bottom sheet)
 * FAB. It will not work if used in any other way.
 */
//@CoordinatorLayout.DefaultBehavior(ProgressFab.Behavior::class)
class ProgressFab : FrameLayout {

    companion object {
        val NAME: String = ProgressFab::class.java.name
        val FAST_OUT_LINEAR_IN_INTERPOLATOR = FastOutLinearInInterpolator()
        val ANIM_INTERPOLATOR = FAST_OUT_LINEAR_IN_INTERPOLATOR
        val PRESSED_ANIM_DURATION = 100L
        val PRESSED_ANIM_DELAY = 100L

        val ANIM_STATE_NONE = 0
        val ANIM_STATE_HIDING = 1
        val ANIM_STATE_SHOWING = 2
    }

    private var animState: Int = ANIM_STATE_NONE
    private var progressBar: ProgressBar? = null
    private var fab: FloatingActionButton? = null
    private var ringThickness: Int = 10.dpToPx
    private var progressBarPadding = 10.dpToPx
    private var viewSize = 0

    constructor(context: Context)
            : this(context, null)

    constructor(context: Context, attrs: AttributeSet?)
            : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int)
            : this(context, attrs, defStyle, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int, styleRes: Int)
            : super(context, attrs, defStyle, styleRes) {
        init(context, attrs, defStyle, styleRes)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyle: Int, styleRes: Int) {
        val a = context.obtainStyledAttributes(
                attrs,
                edu.vanderbilt.crawler.R.styleable.ProgressFab,
                defStyle,
                styleRes)

        ringThickness =
                a.getDimensionPixelOffset(
                        edu.vanderbilt.crawler.R.styleable.ProgressFab_ringThickness,
                        ringThickness)
        a.recycle()

        // The outside of a ProgressBar ring is actually inset about 10dp from
        // the progress view edges even with no padding or margins set. So to
        // get the requested thickness requires inflating by approximately 10dp.
        // A 10dp ring with the 10dp offset seems to work best.
        ringThickness += progressBarPadding
        translationY = ringThickness.toFloat() - progressBarPadding.toFloat()
        translationX = ringThickness.toFloat() - progressBarPadding.toFloat()
    }

    override fun onViewAdded(child: View?) {
        if (childCount > 2) {
            throw IllegalStateException(
                    "$NAME must only contain one FloatingActionButton and one ProgressBar")
        }

        when (child) {
            is ProgressBar ->
                if (progressBar == null) {
                    progressBar = child
                } else {
                    throw IllegalArgumentException(
                            "$NAME must only contain one ProgressBar child.")
                }
            is FloatingActionButton ->
                if (progressBar == null) {
                    fab = child
                } else {
                    throw IllegalArgumentException(
                            "$NAME must only contain one FloatingActionBar child.")
                }
            else -> throw IllegalStateException(
                    "$NAME must only contain a FloatingActionButton and a ProgressBar")
        }
    }

    override fun measureChildWithMargins(child: View?,
                                         parentWidthMeasureSpec: Int,
                                         widthUsed: Int,
                                         parentHeightMeasureSpec: Int,
                                         heightUsed: Int) {
        ensureChildrenExist()

        if (child == progressBar && !progressBar!!.isGone) {
            val lp = child!!.layoutParams as MarginLayoutParams
            val childWidthMeasureSpec =
                    getChildMeasureSpec(
                            parentWidthMeasureSpec,
                            leftPadding
                            + rightPadding
                            + lp.leftMargin
                            + lp.rightMargin
                            + widthUsed,
                            lp.width)
            val childHeightMeasureSpec =
                    getChildMeasureSpec(
                            parentHeightMeasureSpec,
                            topPadding
                            + bottomPadding
                            + lp.topMargin
                            + lp.bottomMargin
                            + heightUsed,
                            lp.height)

            fab!!.measure(childWidthMeasureSpec, childHeightMeasureSpec)

            val width = fab!!.measuredWidth
            val height = fab!!.measuredHeight
            progressBar!!.measure(
                    MeasureSpec.makeMeasureSpec(width + ringThickness, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height + ringThickness, MeasureSpec.EXACTLY))
        } else {
            super.measureChildWithMargins(child,
                                          parentWidthMeasureSpec,
                                          widthUsed,
                                          parentHeightMeasureSpec,
                                          heightUsed)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (fab != null && progressBar != null) {
            //resize()
        }
    }

    private fun ensureChildrenExist() {
        if (fab == null) {
            throw IllegalStateException("$NAME must contain a FloatingActionButton child.")
        } else if (progressBar == null) {
            throw IllegalStateException("$NAME must contain a ProgressBar child.")
        }
    }

    private fun shouldAnimateVisibilityChange(): Boolean {
        return ViewCompat.isLaidOut(this) && !isInEditMode
    }

    fun hide(runAfter: (() -> Unit)? = null) {
        animate().cancel()

        if (shouldAnimateVisibilityChange()) {
            animState = ANIM_STATE_HIDING

            animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .alpha(0f)
                    .setDuration(SHOW_HIDE_ANIM_DURATION)
                    .setInterpolator(FAST_OUT_LINEAR_IN_INTERPOLATOR)
                    .setListener(object : AnimatorListenerAdapter() {
                        var cancelled: Boolean = false

                        override fun onAnimationStart(animation: Animator?) {
                            this@ProgressFab.visibility = View.VISIBLE
                            cancelled = false
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            animState = ANIM_STATE_NONE
                            if (!cancelled) {
                                this@ProgressFab.visibility = View.INVISIBLE
                                runAfter?.invoke()
                            }
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                            cancelled = true
                        }
                    })
        } else {
            // If the view isn't laid out, or we're
            // in the editor, don't run the animation
            visibility = View.GONE
        }
    }

    fun show(runAfter: (() -> Unit)? = null) {
        animate().cancel()

        if (shouldAnimateVisibilityChange()) {
            animState = ANIM_STATE_SHOWING

            if (visibility != View.VISIBLE) {
                // If the view isn't visible currently,
                // we'll animate it from a single pixel
                alpha = 0f
                scaleY = 0f
                scaleX = 0f
            }

            animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(SHOW_HIDE_ANIM_DURATION)
                    .setInterpolator(LINEAR_OUT_SLOW_IN_INTERPOLATOR)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator) {
                            this@ProgressFab.visibility = View.VISIBLE
                        }

                        override fun onAnimationEnd(animation: Animator) {
                            animState = ANIM_STATE_NONE
                            runAfter?.invoke()
                        }
                    })
        } else {
            visibility = View.VISIBLE
            alpha = 1f
            scaleY = 1f
            scaleX = 1f
        }
    }

    class Behavior : CoordinatorLayout.Behavior<ProgressFab> {
        constructor() : super()
        constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

        override fun layoutDependsOn(parent: CoordinatorLayout,
                                     child: ProgressFab,
                                     dependency: View): Boolean {
            return isSnackBar(dependency) || isBottomSheet(dependency)
        }

        override fun onDependentViewChanged(parent: CoordinatorLayout,
                                            child: ProgressFab,
                                            dependency: View): Boolean {
            if (isSnackBar(dependency)) {
                onSnackbarChanged(child, dependency)
            } else if (isBottomSheet(dependency)) {
                onBottomSheetChanged(parent, child, dependency)
            }

            return true
        }

        /**
         * Bottom sheet calculation uses absolute position values to
         * determine the FAB offset. This calculation is NOT generic
         * and is intended to only work for bottom sheet used in this
         * ImageCrawler application.
         */
        private fun onBottomSheetChanged(parent: CoordinatorLayout,
                                         child: ProgressFab,
                                         dependency: View) {
            val offset = parent.bottom - dependency.top
            if (offset >= 0) {
                child.translationY = -offset.toFloat()
            }
        }

        /**
         * Snack bar calculation uses y translation value of Snackbar to
         * determine FAB offset.
         */
        private fun onSnackbarChanged(child: ProgressFab, dependency: View) {
            val transY = dependency.translationY
            println("translation Y = $transY")
            val translationY = Math.min(0f, dependency.translationY - dependency.height)
            if (child.bottom > dependency.top) {
                child.translationY = translationY
            }
        }

        private fun isBottomSheet(view: View?): Boolean {
            return if (view == null) {
                false
            } else {
                val lp = view.layoutParams
                if (lp is CoordinatorLayout.LayoutParams) {
                    lp.behavior is BottomSheetBehavior<*>
                } else {
                    false
                }
            }
        }

        private fun isSnackBar(view: View?): Boolean {
            return view is Snackbar.SnackbarLayout
        }
    }
}

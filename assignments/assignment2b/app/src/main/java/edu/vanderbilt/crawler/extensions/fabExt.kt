package edu.vanderbilt.crawler.extensions

import android.animation.Animator
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.google.android.material.floatingactionbutton.FloatingActionButton


fun FloatingActionButton.enableBehavior(behavior: FloatingActionButton.Behavior,
                                        enable: Boolean = true) {
    (layoutParams as CoordinatorLayout.LayoutParams).behavior = behavior
    requestLayout()
    isVisible = enable
}

fun FloatingActionButton.setAnchor(anchorId: Int = View.NO_ID) {
    (layoutParams as CoordinatorLayout.LayoutParams).anchorId = anchorId
    isGone = anchorId == View.NO_ID
}

var FloatingActionButton.behavior
    get() = {
        val params = (layoutParams as? CoordinatorLayout.LayoutParams)
                ?: throw IllegalArgumentException("The view is not a child " +
                        "of CoordinatorLayout")
        params.behavior as? FloatingActionButton.Behavior
                ?: throw IllegalArgumentException("The view is not associated " +
                        "with FloatingActionButton.Behavior")
    }
    set(behavior) {
        (layoutParams as CoordinatorLayout.LayoutParams).behavior =
                behavior as? FloatingActionButton.Behavior
        requestLayout()
    }

/**
 * Returns the typed behavior of the receiver view.
 */
inline fun <V : View, reified T : CoordinatorLayout.Behavior<V>> View.behavior(): T {
    val params = layoutParams as? CoordinatorLayout.LayoutParams
            ?: throw IllegalArgumentException("The view is not a " +
                    "child of CoordinatorLayout")
    return params.behavior as? T
            ?: throw IllegalArgumentException(
                    "The view does not support the specified behavior")
}

/**
 * FAB animation helper used for showing and hiding FAB that are
 * anchored to bottom sheets. This is necessary because the default
 * implementation of the BottomSheetBehaviour class always cancels
 * any FAB animation started by either the [show] or [hide] methods.
 *
 * The animation shows a resizing effect for the FAB receiver object
 * that adds resize animation to the normal FAB translation animation.
 */
val SHOW_HIDE_ANIM_DURATION = 300L
val LINEAR_OUT_SLOW_IN_INTERPOLATOR = LinearOutSlowInInterpolator()

fun FloatingActionButton.animateScale(show: Boolean,
                                      anchorId: Int = View.NO_ID,
                                      block: () -> Unit = {}) {
    // Can't rely on show and hide to determine FAB visibility
    // so use the scale to determine visibility instead.

    if (!show && scaleX != 0f) {
        animate().cancel()

        System.out.println("Hiding FAB")
        // Hide the fab with a nice double animation. Note DO NOT call
        // the fab.hide() here because it will not hide the FAB.
        animate().scaleX(0f)
                .scaleY(0f)
                //.setDuration(SHOW_HIDE_ANIM_DURATION)
                //.setInterpolator(LINEAR_OUT_SLOW_IN_INTERPOLATOR)
                .setInterpolator(AccelerateInterpolator(2f))
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {
                        block()
                    }

                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationStart(animation: Animator?) {}
                })
                .start()
    } else if (show && scaleX == 0f) {
        animate().cancel()

        System.out.println("Showing FAB")
        // Show the fab with a nice double animation. Note DO NOT call
        // the fab.show() here because it will not show the FAB.
        animate().scaleX(1f)
                .scaleY(1f)
                .setInterpolator(DecelerateInterpolator(2f))
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {
                        block()
                    }

                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationStart(animation: Animator?) {}
                })
                .start()
    }
}

fun FloatingActionButton.animateSlide(show: Boolean,
                                      anchorId: Int = View.NO_ID,
                                      block: () -> Unit = {}) {
    // Can't rely on show and hide to determine FAB visibility
    // so use the scale to determine visibility instead.

    if (!show && translationY == 0f) {
        animate().cancel()
        //setAnchor(View.NO_ID)

        System.out.println("Hiding FAB")
        // Hide the fab with a nice double animation. Note DO NOT call
        // the fab.hide() here because it will not hide the FAB.
        animate().scaleX(0f)
                .scaleY(0f)
                .translationY((height + 100).toFloat())
                .setDuration(SHOW_HIDE_ANIM_DURATION)
                .setInterpolator(LINEAR_OUT_SLOW_IN_INTERPOLATOR)
                //.setInterpolator(AccelerateInterpolator(2f))
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {
                        block()
                    }

                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationStart(animation: Animator?) {}
                })
                .start()
    } else if (show && scaleX == 0f) {
        animate().cancel()

        //setAnchor(anchorId)

        System.out.println("Showing FAB")
        // Show the fab with a nice double animation. Note DO NOT call
        // the fab.show() here because it will not show the FAB.
        animate().scaleX(1f)
                .scaleY(1f)
                .translationY(0f)
                //.setDuration(SHOW_HIDE_ANIM_DURATION)
                //.setInterpolator(LINEAR_OUT_SLOW_IN_INTERPOLATOR)
                .setInterpolator(DecelerateInterpolator(2f))
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {
                        block()
                    }

                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationStart(animation: Animator?) {}
                })
                .start()
    }
}
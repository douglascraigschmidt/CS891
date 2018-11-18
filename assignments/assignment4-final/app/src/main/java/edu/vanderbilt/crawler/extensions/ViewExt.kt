package edu.vanderbilt.crawler.extensions

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import org.jetbrains.anko.contentView
import java.util.ArrayList


/**
 * View extensions.
 */

val View.ctx: Context
    get() = context

var TextView.textColor: Int
    get() = currentTextColor
    set(v) = setTextColor(v)

fun View.slideExit() {
    if (translationY == 0f) animate().translationY(height.toFloat())
}

fun View.slideEnter() {
    if (translationY < 0f) animate().translationY(0f)
}

fun View.setViewHeight(height: Int) {
    val params = layoutParams
    params.height = height
    requestLayout()
}

var View.property: Int?
    get() = ExtensionBackingField["${this.javaClass.canonicalName}::property"]
    set(value: Int?) {
        ExtensionBackingField["${this.javaClass.canonicalName}::property"] = value
    }

/**
 * Same as View.postDelayed, but with reversed parameters so
 * that the call can be postDelay(1000) { .... }
 */
fun View.postDelayed(delay: Long, action: () -> Unit): Boolean {
    return postDelayed(action, delay)
}

/**
 * Same as View.postDelayed, but with reversed parameters so
 * that the call can be postDelay(1000) { .... }
 */
fun Activity.postDelayed(delay: Long, action: () -> Unit): Boolean {
    return contentView?.postDelayed(action, delay) ?: false
}

/**
 * Returns the passed [view] or the first descendant view
 * that matches the passed [predicate]
 */
fun View.findView(predicate: (view: View) -> Boolean): View? {
    if (predicate(this)) {
        return this
    } else if (this is ViewGroup) {
        // Search all descendants for a match.
        (0 until childCount)
                .map { getChildAt(it) }
                .forEach {
                    val view = it.findView(predicate)
                    if (view != null) {
                        return view
                    }
                }
    }

    return null
}

/**
 * Returns a list of all descendant views (that may include the
 * the receiver view) that match the supplied [predicate].
 */
fun View.findViews(predicate: (view: View) -> Boolean): List<View> {
    val views = ArrayList<View>()

    if (predicate(this)) {
        views.add(this)
    }

    if (this is ViewGroup) {
        (0 until this.childCount)
                .map { getChildAt(it) }
                .forEach {
                    views.addAll(it.findViews(predicate))
                }
    }

    return views
}

/**
 * Finds all descendant views with a specific tag
 * (including the receiver view).
 *
 * @param tag  The tag to match.
 * @return A list of views that have the specified tag set.
 */
fun View.findTaggedViews(tag: Any): List<View> {
    return findViews { it.tag == tag }
}

/**
 * Finds the first descendant ImageView that has a matching
 * transition name. If the receiver is an ImageView and has
 * a matching transition name, it will be returned as the
 * matching view.
 *
 * @param transitionName The transition name to match.
 * @return The first image view with the specified transition name.
 */
fun View.findImageViewWithTransitionName(transitionName: String): ImageView? {
    val view = findView {
        it is ImageView && ViewCompat.getTransitionName(it) == transitionName
    }

    return if (view != null) view as ImageView else null
}

/**
 * Not sure if this will work or if it has to be a method.
 * TODO: test this out to see what gets returned.
 */
var View.behavior
    get() = {
        val params = (layoutParams as? CoordinatorLayout.LayoutParams)
                ?: throw IllegalArgumentException("The view is not a child " +
                        "of CoordinatorLayout")
        params.behavior as? CoordinatorLayout.Behavior<View>
                ?: throw IllegalArgumentException("The view is not associated " +
                        "with FloatingActionButton.Behavior")
    }
    set(behavior) {
        (layoutParams as CoordinatorLayout.LayoutParams).behavior =
                behavior as? CoordinatorLayout.Behavior<*>
        requestLayout()
    }

fun View.setSingleClickListener(interval: Long = 500L, action: () -> Unit) {
    setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0

        override fun onClick(view: View) {
            val now = SystemClock.elapsedRealtime()
            if (now - lastClickTime >= interval) {
                lastClickTime = now
                action()
            }
        }
    })
}

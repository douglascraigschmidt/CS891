package edu.vandy.app.extensions

import android.app.Activity
import android.content.Context
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import org.jetbrains.anko.contentView


/**
 * View extensions.
 */

val View.ctx: Context
    get() = context

var TextView.textColor: Int
    get() = currentTextColor
    set(v) = setTextColor(v)

var View.visible: Boolean
    get() = visibility == View.VISIBLE
    set(b) {
        visibility = if (b) View.VISIBLE else View.INVISIBLE
    }

var View.gone: Boolean
    get() = visibility == View.GONE
    set(b) {
        visibility = if (b) View.GONE else View.VISIBLE
    }

var View.show: Boolean
    get() = visibility == View.VISIBLE
    set(b) {
        visibility = if (b) View.VISIBLE else View.GONE
    }

/**
 * Enables or disables the receiver view and all its descendants.
 */
fun View.enable(enable: Boolean) {
    isEnabled = enable
    (this as? ViewGroup)?.forAllDescendants {
        it.isEnabled = enable
        true
    }
}

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

/**
 * Search up the view receiver's ancestors looking for the
 * scrolling view and returns that view or null if not found.
 *
 * @return ScrollView or HorizontalScrollView (as ViewGroup)
 * if found, otherwise null.
 */
fun View.getScrollingAncestor(): ViewGroup? {
    // The layout of this view assumes a scrolling ancestor.
    var ancestor = parent
    while (ancestor != null
           && ancestor !is ScrollView
           && ancestor !is HorizontalScrollView) {
        ancestor = ancestor.parent
    }

    return if (ancestor is ScrollView ||
               ancestor is HorizontalScrollView) {
        ancestor as ViewGroup
    } else {
        null
    }
}

var View.property: Int?
    get() = ExtensionBackingField["${this.javaClass.canonicalName}::property"]
    set(value) {
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
 * Returns true if device height >= width. If height == width,
 * true is returned so that this edge case is handle by portrait
 * handlers.
 */
val View.measuredPortrait: Boolean
    get() =
        with(context.applicationContext) {
            with(getSystemService(Context.WINDOW_SERVICE) as WindowManager) {
                val metrics = DisplayMetrics()
                defaultDisplay.getMetrics(metrics)
                metrics.heightPixels >= metrics.widthPixels
            }
        }

/**
 * Returns true if device width > height.
 */
val View.measuredLandscape: Boolean
    get() = !measuredPortrait

/**
 * Runs the specified [action] on all descendant views until
 * either all views have been visited, or [action] returns
 * false.
 */
fun View.forAllDescendants(action: (view: View) -> Boolean): Boolean {
    if (this is ViewGroup) {
        // Search all descendants for a match.
        (0 until childCount)
                .map { getChildAt(it) }
                .forEach {
                    if (!action(it)) {
                        return false
                    }
                    if (!it.forAllDescendants(action)) {
                        return false
                    }
                }
    }

    return true
}

/**
 * Returns the receiver view [this] or the first descendant view
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
 * @return true if view has a valid width and height, false if not.
 */
val View.realized: Boolean
    get() = width != 0 && height != 0

/**
 * Returns the first ancestor of the [android.view.View] receiver
 * (not including the receiver) that matches the passed [predicate]
 * function.
 */
fun View.findAncestor(predicate: (view: View) -> Boolean): View? {
    if (parent is View) {
        val view = parent as View
        return if (predicate(view)) {
            view
        } else {
            view.findAncestor(predicate)
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

/**
 * For debugging only.
 */
fun View.onMeasurePrintParams(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec)
    val widthSpecSize = View.MeasureSpec.getSize(widthMeasureSpec)
    val heightSpecMode = View.MeasureSpec.getMode(heightMeasureSpec)
    val heightSpecSize = View.MeasureSpec.getSize(heightMeasureSpec)

    when (widthSpecMode) {
        View.MeasureSpec.UNSPECIFIED -> println("Width MeasureSpec mode = UNSPECIFIED")
        View.MeasureSpec.AT_MOST -> println("Width MeasureSpec mode = AT_MOST $widthSpecSize")
        View.MeasureSpec.EXACTLY -> println("Width MeasureSpec mode = EXACTLY $widthSpecSize")
        else -> println("Width MeasureSpec mode = NO SPEC MODE ($widthMeasureSpec)")
    }

    when (heightSpecMode) {
        View.MeasureSpec.UNSPECIFIED -> println("Height MeasureSpec mode = UNSPECIFIED")
        View.MeasureSpec.AT_MOST -> println("Height MeasureSpec mode = AT_MOST $heightSpecSize")
        View.MeasureSpec.EXACTLY -> println("Height MeasureSpec mode = EXACTLY $heightSpecSize")
        else -> println("Height MeasureSpec mode = NO SPEC MODE ($heightMeasureSpec)")
    }
}

/**
 * Runs the specified [action] after the receiver [View]
 * has been laid out.
 */
fun View.runAfterLayout(action: (view: View) -> Boolean) {
    val view = this
    viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    if (action(view)) {
                        viewTreeObserver.removeOnPreDrawListener(this)
                    }
                    return true
                }
            })
}

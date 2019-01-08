package edu.vandy.app.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.Size
import edu.vandy.app.extensions.landscape
import edu.vandy.app.extensions.portrait
import kotlin.math.ceil


/**
 * [Float] receiver value converted from px to dp.
 */
val Float.pxToDp: Float
    get() = (this / Resources.getSystem().displayMetrics.density)

/**
 * [Float] receiver value converted from dp to px.
 */
val Float.dpToPx: Float
    get() = (this * Resources.getSystem().displayMetrics.density)

/**
 * [Int] receiver value converted from px to dp.
 */
val Int.pxToDp: Float
    get() = (this / Resources.getSystem().displayMetrics.density)

/**
 * [Int] receiver value converted from dp to px.
 */
val Int.dpToPx: Float
    get() = (this * Resources.getSystem().displayMetrics.density)


/**
 * @return The action bar size in pixels.
 */
fun Context.getActionBarSize(): Int {
    val styledAttributes =
            theme.obtainStyledAttributes(
                    intArrayOf(android.R.attr.actionBarSize))
    val size = styledAttributes.getDimension(0, 0f).toInt()
    styledAttributes.recycle()
    return size
}

/**
 * @return The portrait action bar size in pixels.
 */
fun Context.getPortraitActionBarSize(): Int {
    return if (portrait) {
        getActionBarSize()
    } else {
        // In landscape mode, the action bar size attribute
        // returned is for the landscape bar which is 8dp
        // smaller than the portrait action bar.
        getActionBarSize() + 8.dpToPx.toInt()
    }
}

/**
 * @return The landscape action bar size in pixels.
 */
fun Context.getLandscapeActionBarSize(): Int {
    return if (landscape) {
        getActionBarSize()
    } else {
        // In portrait mode, the action bar size attribute
        // returned is for the portrait action bar which is
        // 8dp larger than the landscape action bar.
        getActionBarSize() - 8.dpToPx.toInt()
    }
}

/**
 * @return The navigation bar size in pixels. When this function
 * is called in landscape mode, the returned action bar size will
 * be 8dp smaller than in portrait mode.
 */
fun Context.getNavBarSize(): Int {
    // navigation bar height
    val resId =
            resources.getIdentifier(
                    "navigation_bar_height", "dimen", "android")
    return if (resId > 0) {
        resources.getDimensionPixelSize(resId)
    } else {
        0
    }
}

/**
 * @return The status size in pixels.
 */
@SuppressLint("ObsoleteSdkInt")
fun Context.getStatusBarSize(): Int {
    val resId =
            resources.getIdentifier("status_bar_size", "dimen", "android")
    return if (resId > 0) {
        resources.getDimensionPixelSize(resId)
    } else {
        return ceil((if (VERSION.SDK_INT >= VERSION_CODES.M) 24 else 25) *
                    resources.displayMetrics.density).toInt()
    }
}

/**
 * @return Android resource dimension in pixels or -1 if the
 * passed [resName] resource name does not exist.
 */
fun Context.getAndroidResPixelDimension(resName: String): Int {
    val resId = resources.getIdentifier(resName, "dimen", "android")
    return if (resId > 0) {
        resources.getDimensionPixelSize(resId)
    } else {
        // Return -1 because 0 may be the actual size.
        -1
    }
}

/**
 * @return Sum of status and actions bars (pixels).
 */
fun Context.getSystemBarsSize() =
        getActionBarSize() + getStatusBarSize()

/**
 * @return Sum of status and actions bars for portrait mode.
 */
fun Context.getPortraitSystemBarsSize() =
        getPortraitActionBarSize() + getStatusBarSize()

/**
 * @return Sum of status and actions bars for portrait mode.
 */
fun Context.getLandscapeSystemBarsSize() =
        getLandscapeActionBarSize() + getStatusBarSize()

/**
 * @return The portrait display dimensions (app drawable area)
 * in pixels.
 */
fun Context.getDisplaySize(): Size {
    val metrics = resources.displayMetrics
    return Size(metrics.widthPixels, metrics.heightPixels)
}

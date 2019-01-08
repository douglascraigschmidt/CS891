package edu.vandy.app.extensions

import android.graphics.Color
import android.os.Build
import android.view.Window
import android.view.WindowManager
import android.view.animation.Interpolator

/**
 * Blend `color1` and `color2` using the given ratio.
 *
 * @param ratio of which to blend. 0.0 will return `color1`, 0.5
 * will give an even blend, 1.0 will return `color2`.
 *
 * Source: android\support\design\widget\CollapsingTextHelper.java
 */
fun Color.blendColors(color1: Int, color2: Int, ratio: Float): Int {
    val inverseRatio = 1f - ratio
    val a = Color.alpha(color1) * inverseRatio + Color.alpha(color2) * ratio
    val r = Color.red(color1) * inverseRatio + Color.red(color2) * ratio
    val g = Color.green(color1) * inverseRatio + Color.green(color2) * ratio
    val b = Color.blue(color1) * inverseRatio + Color.blue(color2) * ratio
    return Color.argb(a.toInt(), r.toInt(), g.toInt(), b.toInt())
}

/**
 * Returns lighter/darker version of specified `color`.
 * Source http://stackoverflow.com/questions/4928772/android-color-darker
 */
fun Color.adjustColorBrightnessRGB(color: Int, factor: Float): Int {
    val a = Color.alpha(color)
    val r = Color.red(color)
    val g = Color.green(color)
    val b = Color.blue(color)

    return Color.argb(a,
            Math.max((r * factor).toInt(), 0),
            Math.max((g * factor).toInt(), 0),
            Math.max((b * factor).toInt(), 0))
}

fun Color.adjustColorBrightnessHSV(color: Int, factor: Float): Int {
    val hsv = FloatArray(3)
    Color.colorToHSV(color, hsv)
    hsv[2] *= factor
    return Color.HSVToColor(hsv)
}

/**
 * Sets the status bar color for API >= LOLLIPOP
 *
 * @param window
 * @param color
 */
fun Color.setStatusBarColor(window: Window, color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        // finally change the color
        window.statusBarColor = color
    }
}

/**
 * Linear interpolation from start to end value based on fraction
 * and interpolator's current interpolated value.
 *
 * Source: android\support\design\widget\CollapsingTextHelper.java
 */
private fun lerp(startValue: Float, endValue: Float, fraction: Float,
                 interpolator: Interpolator?): Float {
    var frac = if (interpolator != null) {
        interpolator.getInterpolation(fraction)
    } else {
        fraction
    }

    return lerp(startValue, endValue, frac)
}

/**
 * Linear float interpolation from start to end value based on fraction.
 *
 * Source: android\support\design\widget\AnimationUtils.java
 */
internal fun lerp(startValue: Float, endValue: Float, fraction: Float): Float {
    return startValue + fraction * (endValue - startValue)
}

/**
 * Linear int interpolation from start to end value based on fraction.
 *
 * Source: android\support\design\widget\AnimationUtils.java
 */
internal fun lerp(startValue: Int, endValue: Int, fraction: Float): Int {
    return startValue + Math.round(fraction * (endValue - startValue))
}
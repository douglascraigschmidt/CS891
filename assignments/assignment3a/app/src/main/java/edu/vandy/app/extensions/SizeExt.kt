package edu.vandy.app.extensions

import android.util.Size
import android.util.SizeF
import kotlin.math.roundToInt

/**
 * @return A new SizeF with width and height set to
 * receiver [Size] width and height values.
 */
fun Size.toSizeF(): SizeF {
    return SizeF(width.toFloat(), height.toFloat())
}

/**
 * @return A new Size with width and height set to
 * receiver [SizeF] width and height values. If
 * [round] is true, values are converted using
 * roundToInt().
 */
fun SizeF.toSize(round: Boolean = false): Size {
    return if (round) {
        Size(width.roundToInt(), height.roundToInt())
    } else {
        Size(width.toInt(), height.toInt())
    }
}

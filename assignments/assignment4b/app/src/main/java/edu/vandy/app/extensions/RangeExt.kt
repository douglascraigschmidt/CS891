package edu.vandy.app.extensions

import edu.vandy.app.utils.Range
import edu.vandy.app.preferences.Adapter
import edu.vandy.app.ui.adapters.dpToPx
import edu.vandy.app.ui.adapters.pxToDp
import kotlin.math.roundToInt

/**
 * For grouping static extensions.
 */
class RangeExt {
    companion object {
        /**
         * @return A [Range] with lower and upper bounds set from
         * the passed comma separated [encoded] [String].
         */
        fun decoder(encoded: String): Range<Int> {
            val split = encoded.split(",")
            if (split.size != 2) {
                error("Unable to decode shared preference Range<Int> value: $encoded")
            }
            return Range(split[0].toInt(), split[1].toInt())
        }
    }
}

/**
 * @return The [Range] receiver's upper bound.
 */
//TODO: get rid of warning
val Range<Int>.max get() = upper

/**
 * @return The [Range] receiver's lower bound.
 */
//TODO: get rid of warning
val Range<Int>.min get() = lower

/**
 * @return The number of values that fall between
 * the [Range] receiver's lower and upper bounds.
 */
val Range<Int>.size get() = upper - lower + 1

/**
 * @return The [Range] receiver's upper bound
 * translated to 0 based offset.
 */
val Range<Int>.maxIndex get() = upper - lower

/**
 * @return The [Range] receivers' 0 based offset for
 * the specified [value].
 */
fun Range<Int>.toOffset(value: Int) = value - lower

/**
 * @return The passed [Range] receiver's [value]
 * mapped to a 0 based offset.
 */
fun Range<Int>.fromOffset(value: Int) = lower + value

/**
 * @return A new [Range] with the [Range] receiver's lower
 * and upper values scaled by the passed [factor] scaling
 * factor.
 */
fun Range<Int>.scale(factor: Float): Range<Int> {
    return Range((factor * lower).toInt(), (factor * upper).toInt())
}

/**
 * @return An Int value that is calculated by first determining
 * the distance between the pass [value] and the passed [min]
 * and [max] values and then scales and maps that value into
 * the receiver [Range] resulting in a value the is at the same
 * relative offset in the receiver [Range].
 */
fun Range<Int>.transpose(min: Int, value: Int, max: Int): Int {
    check(min <= max) {
        "passed value min $min must be less than passed $max."
    }
    check(value in min..max) {
        "passed value $value is not in the range [$min..$max]"
    }

    // Determine the value % offset in the passed min/max range.
    val factor = (value - min).toFloat() / (max - min)

    // Return the equivalent % offset value in the receiver range.
    return fractionalValue(factor)
}

/**
 * @return An Int value that is the specified fractional
 * distance (must be in [0..1]) in the receiver [Range].
 */
fun Range<Int>.fractionalValue(fraction: Float): Int {
    check(fraction in 0f..1f) {
        "passed fraction must be in the range [0..1]"
    }

    // Return the value at the fractional offset
    return (lower + fraction * (upper - lower)).toInt()
}

/**
 * @return A encoded comma separated [String] representing
 * the [Range] receiver's lower and upper bounds.
 */
fun Range<Int>.encode(): String = "$lower,$upper"

/**
 * @return A new [Range] using the passed [value] value
 * the receiver's upper value. If [fit] is specified
 * then, if necessary, the upper bound will shifted to prevent
 * invalid argument exception caused when lower > upper.
 */
fun Range<Int>.setLower(value: Int,
                                            fit: Boolean = false)
        : Range<Int> {
    val newUpper = if (fit && upper < value) {
        value
    } else {
        upper
    }

    return Range(value, newUpper)
}

/**
 * @return A new [Range] using the receiver's lower value
 * and the passed [value] value. If [fit] is specified
 * then, if necessary, the lower bound will shifted to prevent
 * invalid argument exception caused when upper < lower.
 */
fun Range<Int>.setUpper(value: Int,
                                            fit: Boolean = false)
        : Range<Int> {
    val newLower = if (fit && lower > value) {
        value
    } else {
        lower
    }

    return Range(newLower, value)
}

/**
 * @return A new [Range] with bounds shifted by the
 * specified [offset].
 */
fun Range<Int>.shift(offset: Int): Range<Int> =
        Range(lower + offset, upper + offset)

/**
 * @return A new [Range] with lower and upper DP values
 * converted to pixel values.
 */
fun Range<Int>.dpToPx(): Range<Int> {
    return Range(lower.dpToPx.toInt(), upper.dpToPx.toInt())
}

/**
 * @return A new [Range] with lower and upper pixel values
 * converted to DP values.
 */
fun Range<Int>.pxToDp(): Range<Int> {
    return Range(lower.pxToDp.toInt(), upper.pxToDp.toInt())
}

/**
 * @return A new Range<Float> converted from receiver Range<Int>.
 */
fun Range<Int>.toFloat(): Range<Float> {
    return Range(lower.toFloat(), upper.toFloat())
}

/**
 * @return A new Range<Int> converted from receiver Range<Float>
 *     If [round] is true, bounds are rounded using roundToInt().
 */
fun Range<Float>.toInt(round: Boolean = false): Range<Int> {
    return if (round) {
        Range(lower.roundToInt(), upper.roundToInt())
    } else {
        Range(lower.toInt(), upper.toInt())
    }
}

/**
 * Checks if the passed [value] falls within the [Range] receiver's
 * bounds. If it does not fit, the optional [apply] consumer is
 * called passing in the closer of the lower or upper bound.
 *
 * @return true if the passed [value] fits in the range, false if not.
 */
fun Range<Int>.fit(value: Int, apply: (fitValue: Int) -> Unit = {}): Boolean {
    return when {
        value < lower -> {
            apply(lower)
            false
        }
        value > upper -> {
            apply(upper)
            false
        }
        else -> true
    }
}

/**
 * Preference adapter class for Range<Int> that can be used
 * for save and restoring from shared preferences.
 */
class RangeAdapter : Adapter<Range<Int>> {
    override fun encode(value: Range<Int>) = value.encode()

    override fun decode(string: String): Range<Int> {
        return RangeExt.decoder(string)
    }
}

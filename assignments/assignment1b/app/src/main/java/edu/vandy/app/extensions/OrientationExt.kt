package edu.vandy.app.extensions

import android.content.res.Configuration
import android.graphics.Point
import android.graphics.Rect
import android.util.Size
import android.view.View
import android.view.ViewGroup
import edu.vandy.app.App

/**
 * Orientation extensions automatically flip points and dimensions from
 * landscape to portrait so that all custom view calculations can be
 * done in portrait mode. Note that this assumes that the goal is to
 * have landscape mode look exactly like portrait mode rotated clockwise
 * by 90 degrees.
 */

val portrait: Boolean
    get() = App.instance.resources.configuration.orientation ==
            Configuration.ORIENTATION_PORTRAIT

val landscape: Boolean
    get() = App.instance.resources.configuration.orientation ==
            Configuration.ORIENTATION_LANDSCAPE

val Rect.oWidth
    get() = if (portrait) width() else height()
val Rect.oHeight
    get() = if (portrait) height() else width()

var Rect.oTop
    get() = if (portrait) top else left
    set(oValue) {
        if (portrait) {
            top = oValue
        } else {
            left = oValue
        }
    }

var Rect.oBottom
    get() = if (portrait) bottom else right
    set(oValue) {
        if (portrait) {
            bottom = oValue
        } else {
            right = oValue
        }
    }
var Rect.oLeft
    get() = if (portrait) left else top
    set(oValue) {
        if (portrait) {
            left = oValue
        } else {
            top = oValue
        }
    }
var Rect.oRight
    get() = if (portrait) right else bottom
    set(oValue) {
        if (portrait) {
            right = oValue
        } else {
            bottom = oValue
        }
    }

fun Rect.oOffset(oX: Int, oY: Int): Rect {
    if (portrait) {
        offset(oX, oY)
    } else {
        offset(oY, oX)
    }
    return this
}

fun Rect.oOffsetTo(oX: Int, oY: Int): Rect {
    if (portrait) {
        offsetTo(oX, oY)
    } else {
        offsetTo(oY, oX)
    }
    return this
}

fun Rect.oInset(oDx: Int, oDy: Int): Rect {
    if (portrait) {
        inset(oDx, oDy)
    } else {
        inset(oDy, oDx)
    }
    return this
}

val Size.oWidth
    get() = if (portrait) width else height
val Size.oHeight
    get() = if (portrait) height else width

fun Size.oScale(scaleX: Float, scaleY: Float): Size {
    return if (portrait) {
        Size((width * scaleX).toInt(), (height * scaleY).toInt())
    } else {
        Size((width * scaleY).toInt(), (height * scaleX).toInt())
    }
}

var Point.oX
    get() = if (portrait) x else y
    set(oValue) {
        if (portrait) {
            x = oValue
        } else {
            y = oValue
        }
    }
var Point.oY
    get() = if (portrait) y else x
    set(oValue) {
        if (portrait) {
            y = oValue
        } else {
            x = oValue
        }
    }

val View.oWidth
    get() = if (portrait) width else height

val View.oHeight
    get() = if (portrait) height else width

val View.oRealized
    get() = (portrait && width != 0) ||
            (landscape && height != 0)

val ViewGroup.oWidth
    get() = if (portrait) width else height

val ViewGroup.oHeight
    get() = if (portrait) height else width

val View.oPaddingStart
    get() = if (portrait) paddingStart else paddingTop

val View.oPaddingEnd
    get() = if (portrait) paddingEnd else paddingBottom

val View.oPaddingLeft
    get() = if (portrait) paddingLeft else paddingTop

val View.oPaddingRight
    get() = if (portrait) paddingRight else paddingBottom

val View.oPaddingTop
    get() = if (portrait) paddingTop else paddingLeft

val View.oPaddingBottom
    get() = if (portrait) paddingBottom else paddingRight


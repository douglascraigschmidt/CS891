package edu.vandy.app.extensions

import android.graphics.Point
import android.graphics.Rect
import android.util.Size

/**
 * @return Top left [Point].
 */
val Rect.topLeft
    get() = Point(left, top)

/**
 * @return Top right [Point].
 */
val Rect.topRight
    get() = Point(right, top)

/**
 * @return Bottom left [Point].
 */
val Rect.bottomLeft
    get() = Point(left, bottom)

/**
 * @return Bottom right [Point].
 */
val Rect.bottomRight
    get() = Point(right, bottom)

/**
 * Rect extension to center fit a rectangle in
 * the specified [bounds] rectangle.
 */
fun Rect.centerFit(bounds: Rect): Rect {
    val hInset = (bounds.width() - width()) / 2f
    val vInset = (bounds.height() - height()) / 2f
    inset(hInset.toInt(), vInset.toInt())
    return this
}

/**
 * Rect extension to center fit a rectangle in
 * the specified [bounds] rectangle.
 */
fun Rect.centerInside(bounds: Rect): Rect {
    offset(bounds.centerX() - centerX(), bounds.centerY() - centerY())
    val aspect = width().toFloat() / height()
    val vInset = (height() - bounds.height()) / 2f
    inset(0, vInset.toInt())
    inset((vInset * aspect).toInt(), 0)

    if (width() > bounds.width()) {
        val hInset = (width() - bounds.width()) / 2f
        //val hInset = (bounds.width() - width()) / 2f
        inset(hInset.toInt(), 0)
        inset(0, (hInset / aspect).toInt())
    }

    return this
}

/**
 * Rect extension to center fit a rectangle in
 * the specified [bounds] rectangle.
 */
fun Rect.centerHorizontally(bounds: Rect): Rect {
    val width = width()
    val height = height()
    top = bounds.top
    bottom = top + height
    left = bounds.left + ((bounds.width() - width) / 2f).toInt()
    right = left + width
    return this
}

/**
 * @return A [Size] with set to the width and height of the receiver [Rect].
 */
fun Rect.toSize() = Size(width(), height())

/**
 * @return A [Rect] with [Rect.right]set to the [Size] receiver
 * width and [Rect.bottom] set to [Size] receiver height.
 */
fun Size.toRect() = Rect(0, 0, width, height)

/**
 * Convenience function that initialize the receiver [Rect]
 * top left coordinates to the passed [point] and width and
 * height from the passed [size].
 */
fun Rect.set(point: Point, size: Size) {
    set(point.x, point.y, size)
}

/**
 * Convenience function that initialize the receiver [Rect]
 * using [top] [left] coordinate and a passed [size].
 */
fun Rect.set(left: Int, top: Int, size: Size) {
    set(left, top, left + size.width, top + size.height)
}

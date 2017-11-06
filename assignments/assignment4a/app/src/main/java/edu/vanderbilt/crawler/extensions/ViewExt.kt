package edu.vanderbilt.crawler.extensions

import android.app.Activity
import android.content.Context
import android.opengl.Visibility
import android.view.View
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

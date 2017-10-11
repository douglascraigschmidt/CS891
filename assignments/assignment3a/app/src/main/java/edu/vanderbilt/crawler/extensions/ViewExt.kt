package edu.vanderbilt.crawler.extensions

import android.content.Context
import android.view.View
import android.widget.TextView


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

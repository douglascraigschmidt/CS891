package edu.vanderbilt.crawler.extensions

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import org.jetbrains.anko.contentView
import org.jetbrains.anko.inputMethodManager

/**
 * Keyboard helpers.
 */

fun Activity.hideKeyboard() {
    contentView?.hideKeyboard(inputMethodManager)
}

fun View.hideKeyboard(inputMethodManager: InputMethodManager) {
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

package edu.vanderbilt.crawler.extensions

import android.app.Activity
import android.view.View
import com.google.android.material.snackbar.Snackbar
import org.jetbrains.anko.contentView

/**
 * Extensions for displaying Snackbar.
 */

fun View.snack(message: String,
               length: Int = Snackbar.LENGTH_LONG,
               action: Snackbar.() -> Unit) {
    val snackBar = Snackbar.make(this, message, length)
    snackBar.action()
    snackBar.show()
}

fun Snackbar.action(action: String,
                    color: Int? = null,
                    listener: (View) -> Unit) {
    setAction(action, listener)
    color?.let { setActionTextColor(it) }
}

fun Activity.shortSnack(message: String, action: Snackbar.() -> Unit)
        = snack(message, Snackbar.LENGTH_SHORT, action)

fun Activity.longSnack(message: String, action: Snackbar.() -> Unit)
        = snack(message, Snackbar.LENGTH_LONG, action)

fun Activity.snack(message: String,
                   length: Int = Snackbar.LENGTH_LONG,
                   action: Snackbar.() -> Unit)
        = contentView?.snack(message, length, action)
        ?: throw IllegalStateException("snack requites a valid activity content view.")

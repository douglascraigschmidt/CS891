package edu.vanderbilt.webcrawler.adapters

import android.content.res.Resources

/**
 * Created by monte on 2017-09-07.
 */

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

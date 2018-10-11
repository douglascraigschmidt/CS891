package edu.vanderbilt.crawler.ui.adapters

import android.content.res.Resources

/**
 * Created by monte on 2017-09-07.
 */

val Int.pxToDp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.dpToPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

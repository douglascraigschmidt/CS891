package edu.vandy.app.extensions

/**
 * Created by monte on 10/10/17.
 */
fun Int.minmax(min: Int, max: Int) = Math.max(min, Math.min(max, this))

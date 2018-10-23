package edu.vanderbilt.crawler.utils

/**
 * A utility class that provides some common data and state validation methods
 * that will throw exceptions if preconditions are not met. These methods are
 * a small subset of those provided in Google Guava.
 */
object Preconditions {
    fun checkArgument(expression: Boolean, message: String) {
        if (!expression) {
            throw IllegalArgumentException(message)
        }
    }

    fun checkState(expression: Boolean, message: String) {
        if (!expression) {
            throw IllegalStateException(message)
        }
    }

    fun <T> checkNotNull(arg: T): T {
        return checkNotNull(arg, "Argument must not be null")
    }

    fun <T> checkNotNull(arg: T?, message: String): T {
        if (arg == null) {
            throw NullPointerException(message)
        }
        return arg
    }
}

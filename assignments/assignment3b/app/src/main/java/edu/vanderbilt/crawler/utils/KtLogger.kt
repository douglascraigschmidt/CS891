package edu.vanderbilt.crawler.utils

import android.util.Log

interface KtLogger {
    /**
     * The logger tag used in extension functions for the [KtLogger].
     * Note that the tag length should not be more than 23 symbols.
     */
    val loggerTag: String
        get() = getTag(javaClass)
}

var globalLogLevel: Int? = null

@Suppress("NOTHING_TO_INLINE")
inline fun isLoggable(tag: String, level: Int): Boolean {
    if (globalLogLevel == null) {
        return Log.isLoggable(tag, level)
    } else {
        return level >= globalLogLevel!!
    }
}

fun KtLogger(clazz: Class<*>): KtLogger = object : KtLogger {
    override val loggerTag = getTag(clazz)
}

fun KtLogger(tag: String): KtLogger = object : KtLogger {
    init {
        assert(tag.length <= 23) { "The maximum tag length is 23, got $tag" }
    }

    override val loggerTag = tag
}

inline fun <reified T : Any> KtLogger(): KtLogger = KtLogger(T::class.java)

/**
 * Send a log message with the [Log.VERBOSE] severity.
 * Note that the log message will not be written if the current log level is above [Log.VERBOSE].
 * The default log level is [Log.INFO].
 *
 * @param message the message text to log. `null` value will be represent as "null", for any other value
 *   the [Any.toString] will be invoked.
 * @param thr an exception to log (optional).
 *
 * @see [Log.v].
 */
@Suppress("NAME_SHADOWING")
fun KtLogger.verbose(message: Any?, thr: Throwable? = null) {
    log(this, message, thr, Log.VERBOSE,
            { tag, msg -> Log.v(tag, msg) },
            { tag, msg, thr -> Log.v(tag, msg, thr) })
}

/**
 * Send a log message with the [Log.DEBUG] severity.
 * Note that the log message will not be written if the current log level is above [Log.DEBUG].
 * The default log level is [Log.INFO].
 *
 * @param message the message text to log. `null` value will be represent as "null", for any other value
 *   the [Any.toString] will be invoked.
 * @param thr an exception to log (optional).
 *
 * @see [Log.d].
 */
@Suppress("NAME_SHADOWING")
fun KtLogger.debug(message: Any?, thr: Throwable? = null) {
    log(this, message, thr, Log.DEBUG,
            { tag, msg -> Log.d(tag, msg) },
            { tag, msg, thr -> Log.d(tag, msg, thr) })
}

/**
 * Send a log message with the [Log.INFO] severity.
 * Note that the log message will not be written if the current log level is above [Log.INFO]
 *   (it is the default level).
 *
 * @param message the message text to log. `null` value will be represent as "null", for any other value
 *   the [Any.toString] will be invoked.
 * @param thr an exception to log (optional).
 *
 * @see [Log.i].
 */
@Suppress("NAME_SHADOWING")
fun KtLogger.info(message: Any?, thr: Throwable? = null) {
    log(this, message, thr, Log.INFO,
            { tag, msg -> Log.i(tag, msg) },
            { tag, msg, thr -> Log.i(tag, msg, thr) })
}

/**
 * Send a log message with the [Log.WARN] severity.
 * Note that the log message will not be written if the current log level is above [Log.WARN].
 * The default log level is [Log.INFO].
 *
 * @param message the message text to log. `null` value will be represent as "null", for any other value
 *   the [Any.toString] will be invoked.
 * @param thr an exception to log (optional).
 *
 * @see [Log.w].
 */
@Suppress("NAME_SHADOWING")
fun KtLogger.warn(message: Any?, thr: Throwable? = null) {
    log(this, message, thr, Log.WARN,
            { tag, msg -> Log.w(tag, msg) },
            { tag, msg, thr -> Log.w(tag, msg, thr) })
}

/**
 * Send a log message with the [Log.ERROR] severity.
 * Note that the log message will not be written if the current log level is above [Log.ERROR].
 * The default log level is [Log.INFO].
 *
 * @param message the message text to log. `null` value will be represent as "null", for any other value
 *   the [Any.toString] will be invoked.
 * @param thr an exception to log (optional).
 *
 * @see [Log.e].
 */
@Suppress("NAME_SHADOWING")
fun KtLogger.error(message: Any?, thr: Throwable? = null) {
    log(this, message, thr, Log.ERROR,
            { tag, msg -> Log.e(tag, msg) },
            { tag, msg, thr -> Log.e(tag, msg, thr) })
}

/**
 * Send a log message with the "What a Terrible Failure" severity.
 * Report an exception that should never happen.
 *
 * @param message the message text to log. `null` value will be represent as "null", for any other value
 *   the [Any.toString] will be invoked.
 * @param thr an exception to log (optional).
 *
 * @see [Log.wtf].
 */
fun KtLogger.wtf(message: Any?, thr: Throwable? = null) {
    if (thr != null) {
        Log.wtf(loggerTag, message?.toString() ?: "null", thr)
    } else {
        Log.wtf(loggerTag, message?.toString() ?: "null")
    }
}

/**
 * Send a log message with the [Log.VERBOSE] severity.
 * Note that the log message will not be written if the current log level is above [Log.VERBOSE].
 * The default log level is [Log.INFO].
 *
 * @param message the function that returns message text to log.
 *   `null` value will be represent as "null", for any other value the [Any.toString] will be invoked.
 *
 * @see [Log.v].
 */
inline fun KtLogger.verbose(message: () -> Any?) {
    val tag = loggerTag
    if (isLoggable(tag, Log.VERBOSE)) {
        Log.v(tag, message()?.toString() ?: "null")
    }
}

/**
 * Send a log message with the [Log.DEBUG] severity.
 * Note that the log message will not be written if the current log level is above [Log.DEBUG].
 * The default log level is [Log.INFO].
 *
 * @param message the function that returns message text to log.
 *   `null` value will be represent as "null", for any other value the [Any.toString] will be invoked.
 *
 * @see [Log.d].
 */
inline fun KtLogger.debug(message: () -> Any?) {
    val tag = loggerTag
    if (isLoggable(tag, Log.DEBUG)) {
        Log.d(tag, message()?.toString() ?: "null")
    }
}

/**
 * Send a log message with the [Log.INFO] severity.
 * Note that the log message will not be written if the current log level is above [Log.INFO].
 * The default log level is [Log.INFO].
 *
 * @param message the function that returns message text to log.
 *   `null` value will be represent as "null", for any other value the [Any.toString] will be invoked.
 *
 * @see [Log.i].
 */
inline fun KtLogger.info(message: () -> Any?) {
    val tag = loggerTag
    if (isLoggable(tag, Log.INFO)) {
        Log.i(tag, message()?.toString() ?: "null")
    }
}

/**
 * Send a log message with the [Log.WARN] severity.
 * Note that the log message will not be written if the current log level is above [Log.WARN].
 * The default log level is [Log.INFO].
 *
 * @param message the function that returns message text to log.
 *   `null` value will be represent as "null", for any other value the [Any.toString] will be invoked.
 *
 * @see [Log.w].
 */
inline fun KtLogger.warn(message: () -> Any?) {
    val tag = loggerTag
    if (isLoggable(tag, Log.WARN)) {
        Log.w(tag, message()?.toString() ?: "null")
    }
}

/**
 * Send a log message with the [Log.ERROR] severity.
 * Note that the log message will not be written if the current log level is above [Log.ERROR].
 * The default log level is [Log.INFO].
 *
 * @param message the function that returns message text to log.
 *   `null` value will be represent as "null", for any other value the [Any.toString] will be invoked.
 *
 * @see [Log.e].
 */
inline fun KtLogger.error(message: () -> Any?) {
    val tag = loggerTag
    if (isLoggable(tag, Log.ERROR)) {
        Log.e(tag, message()?.toString() ?: "null")
    }
}

/**
 * Return the stack trace [String] of a throwable.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Throwable.getStackTraceString(): String = Log.getStackTraceString(this)

private inline fun log(
        logger: KtLogger,
        message: Any?,
        thr: Throwable?,
        level: Int,
        f: (String, String) -> Unit,
        fThrowable: (String, String, Throwable) -> Unit) {
    val tag = logger.loggerTag
    if (isLoggable(tag, level)) {
        if (thr != null) {
            fThrowable(tag, message?.toString() ?: "null", thr)
        } else {
            f(tag, message?.toString() ?: "null")
        }
    }
}

private fun getTag(clazz: Class<*>): String {
    val tag = clazz.simpleName
    return if (tag.length <= 23) {
        tag
    } else {
        tag.substring(0, 23)
    }
}

package edu.vanderbilt.crawler.utils

import io.reactivex.exceptions.UndeliverableException
import java.io.IOException
import java.net.SocketException

/**
 * RxJavaPlugin error handler.
 */
val rxErrorHandler = { error: Throwable ->
    fun debug(msg: String) {
        println("rxErrorHandler: $msg")
    }

    val e = if (error is UndeliverableException) {
        debug("UndeliveredException")
        error.cause
    } else {
        error
    }

    when (e) {
        is SocketException -> {
            // Irrelevant network problem or API that throws on cancellation.
            debug("Socket exception ... ignoring")
        }
        is IOException -> {
            // Irrelevant network problem or API that throws on cancellation.
            debug("IO exception ... ignoring")
        }
        is InterruptedException -> {
            // Some blocking code was interrupted by a dispose call.
            debug("InterruptedException ... ignoring")
        }
        is NullPointerException -> {
            // Likely a bug in the application.
            debug("NullPointer ... calling uncaught handler and returning")
            Thread.currentThread().uncaughtExceptionHandler
                    .uncaughtException(Thread.currentThread(), e)
        }
        is IllegalArgumentException -> {
            // Likely a bug in the application.
            debug("IllegalArgument ... calling uncaught handler and returning")

            Thread.currentThread().uncaughtExceptionHandler
                    .uncaughtException(Thread.currentThread(), e)
        }
        is IllegalStateException -> {
            // Likely a bug in RxJava or in a custom operator.
            debug("IllegalState ... calling uncaught handler and returning")

            Thread.currentThread().uncaughtExceptionHandler
                    .uncaughtException(Thread.currentThread(), e)
        }
        else -> debug("Undeliverable exception received, not sure what to do: $e")
    }
}

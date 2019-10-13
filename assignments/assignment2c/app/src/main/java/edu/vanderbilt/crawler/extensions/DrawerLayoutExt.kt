package edu.vanderbilt.crawler.extensions

import android.os.SystemClock
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.drawerlayout.widget.DrawerLayout

/**
 * Tricky function that simulates a long-click near drawer
 * edge to force it to peek out for a short period of time.
 */
fun DrawerLayout.peekDrawer() {
    // Obtain MotionEvent object
    val downTime = SystemClock.uptimeMillis()
    val longPressTime = ViewConfiguration.getLongPressTimeout() + 100
    val eventTime =
            SystemClock.uptimeMillis() + longPressTime
    val x = width - 5
    val y = 5
    val metaState = 0
    val motionDownEvent = MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_DOWN,
            x.toFloat(),
            y.toFloat(),
            metaState
    )
    dispatchTouchEvent(motionDownEvent)

    postDelayed(longPressTime.toLong() + 500) {
        val motionUpEvent = MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_UP,
                x.toFloat(),
                y.toFloat(),
                metaState
        )
        dispatchTouchEvent(motionUpEvent)
    }
}


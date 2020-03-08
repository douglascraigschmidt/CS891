package edu.vandy.app.extensions

import android.os.SystemClock
import androidx.drawerlayout.widget.DrawerLayout
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration

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

/**
 * Just calls the passed [onChange] function passing true if an open
 * event is received and false if a close event is received.
 */
fun DrawerLayout.openCloseListener(onChange: (state: Boolean) -> Unit) {
    this.addDrawerListener(object: DrawerLayout.SimpleDrawerListener() {
        override fun onDrawerOpened(drawerView: View) {
            onChange(true)
        }
        override fun onDrawerClosed(drawerView: View) {
            onChange(false)
        }
    })
}


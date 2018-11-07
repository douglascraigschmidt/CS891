package edu.vanderbilt.crawler.extensions

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View

/**
 * Prevents parent DrawerLayout from horizontally
 * scrolling when user drags SeekBar.
 */
fun View.grabTouchEvents() {
    setOnTouchListener(object : View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            val action = event.action
            when (action) {
                MotionEvent.ACTION_DOWN ->
                    // Disallow Drawer to intercept touch events.
                    view.parent.requestDisallowInterceptTouchEvent(true)

                MotionEvent.ACTION_UP ->
                    // Allow Drawer to intercept touch events.
                    view.parent.requestDisallowInterceptTouchEvent(false)
            }

            // Handle seekbar touch events.
            view.onTouchEvent(event)
            return true
        }
    })
}

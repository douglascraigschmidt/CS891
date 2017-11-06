/*
 * Copyright 2016.  Dmitry Malkovich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.vanderbilt.crawler.ui.views

import android.content.Context
import android.os.Build
import android.support.design.widget.FloatingActionButton
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import edu.vanderbilt.crawler.R
import edu.vanderbilt.crawler.extensions.gone
import edu.vanderbilt.crawler.ui.adapters.px
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.topPadding

/**
 * ProgressFloatingActionButton.java
 * Created by: Dmitry Malkovich
 *
 *
 * A circular loader is integrated with a floating action button.
 */
class ProgressFab(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    companion object {
        val NAME: String = ProgressFab::class.java.name
    }

    private var mProgressBar: ProgressBar? = null
    private var mFab: FloatingActionButton? = null
    private var ringSize: Int = (20).px

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onViewAdded(child: View?) {
        if (childCount > 2) {
            throw IllegalStateException(
                    "$NAME must only contain one FloatingActionButton and one ProgressBar")
        }

        when (child) {
            is ProgressBar ->
                if (mProgressBar == null) {
                    mProgressBar = child
                } else {
                    throw IllegalArgumentException(
                            "$NAME must only contain one ProgressBar child.")
                }
            is FloatingActionButton ->
                if (mProgressBar == null) {
                    mFab = child
                } else {
                    throw IllegalArgumentException(
                            "$NAME must only contain one FloatingActionBar child.")
                }
            else -> throw IllegalStateException(
                    "$NAME must only contain a FloatingActionButton and a ProgressBar")
        }
    }

    override fun measureChildWithMargins(child: View?,
                                         parentWidthMeasureSpec: Int,
                                         widthUsed: Int,
                                         parentHeightMeasureSpec: Int,
                                         heightUsed: Int) {
        ensureChildrenExist()

        if (child == mProgressBar && !mProgressBar!!.gone) {
            val lp = child!!.layoutParams as MarginLayoutParams
            val childWidthMeasureSpec =
                    getChildMeasureSpec(
                            parentWidthMeasureSpec,
                            leftPadding + rightPadding + lp.leftMargin + lp.rightMargin + widthUsed, lp.width)
            val childHeightMeasureSpec =
                    getChildMeasureSpec(
                            parentHeightMeasureSpec,
                            topPadding + bottomPadding + lp.topMargin + lp.bottomMargin + heightUsed, lp.height)

            mFab!!.measure(childWidthMeasureSpec, childHeightMeasureSpec);

            val width = mFab!!.measuredWidth
            val height = mFab!!.measuredHeight
            mProgressBar!!.measure(
                    MeasureSpec.makeMeasureSpec(width + ringSize, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height + ringSize, MeasureSpec.EXACTLY))
        } else {
            super.measureChildWithMargins(child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun measureChild(child: View?, parentWidthMeasureSpec: Int, parentHeightMeasureSpec: Int) {
        super.measureChild(child, parentWidthMeasureSpec, parentHeightMeasureSpec)
    }

    override fun measureChildren(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        ensureChildrenExist()

        mFab!!.visibility = View.GONE

        if (!mFab!!.gone) {
            measureChild(mFab, widthMeasureSpec, heightMeasureSpec)
            if (mProgressBar!!.gone) {
                val width = mFab!!.measuredWidth
                val height = mFab!!.measuredHeight
                measureChild(mProgressBar,
                             MeasureSpec.makeMeasureSpec(
                                     width + ringSize, MeasureSpec.EXACTLY),
                             MeasureSpec.makeMeasureSpec(
                                     height + ringSize, MeasureSpec.EXACTLY))
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (mFab != null && mProgressBar != null) {
            //resize()
        }
    }

    private fun resize() {
        // 6 is needed for progress bar to be visible, 5 doesn't work
        val translationZpx = resources.getDisplayMetrics().density * 6
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP)
            mProgressBar!!.setTranslationZ(translationZpx)

        val mFabParams = mFab!!.layoutParams as FrameLayout.LayoutParams
        val mProgressParams = mProgressBar!!.layoutParams as FrameLayout.LayoutParams

        val additionSize = resources.getDimensionPixelSize(R.dimen.progress_bar_size)
        mProgressBar!!.layoutParams.height = mFab!!.height + additionSize
        mProgressBar!!.layoutParams.width = mFab!!.width + additionSize

        mFabParams.gravity = Gravity.CENTER
        mProgressParams.gravity = Gravity.CENTER
    }

    private fun ensureChildrenExist() {
        if (mFab == null) {
            throw IllegalStateException("$NAME must contain a FloatingActionButton child.")
        } else if (mProgressBar == null) {
            throw IllegalStateException("$NAME must contain a ProgressBar child.")
        }
    }

}

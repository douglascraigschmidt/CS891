package edu.vanderbilt.crawler.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ListView

class NoScrollListView : ListView {
    constructor(context: Context) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val newWidthSpec = View.MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE shr 2,
                                                            View.MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, newWidthSpec)
    }
}
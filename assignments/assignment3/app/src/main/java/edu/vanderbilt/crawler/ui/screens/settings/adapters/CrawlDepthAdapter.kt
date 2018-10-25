package edu.vanderbilt.crawler.ui.screens.settings.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

internal class CrawlDepthAdapter(context: Context) : BindingAdapter<Int>(context) {

    override fun getCount(): Int {
        return VALUES.size
    }

    override fun getItem(position: Int): Int? {
        return VALUES[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun newView(inflater: LayoutInflater,
                         position: Int,
                         container: ViewGroup): View {
        return inflater.inflate(android.R.layout.simple_spinner_item, container, false)
    }

    override fun bindView(item: Int?, position: Int, view: View) {
        val tv = view.findViewById<TextView>(android.R.id.text1)
        tv.text = item?.toString() ?: "0"
    }

    override fun newDropDownView(inflater: LayoutInflater,
                                 position: Int,
                                 container: ViewGroup): View {
        return inflater.inflate(android.R.layout.simple_spinner_dropdown_item, container, false)
    }

    companion object {
        private val DEFAULT_DEPTH = 3
        private val VALUES = arrayOf(1, 2, 3, 4, 5, 6)

        fun getPositionForValue(value: Int): Int {
            return VALUES.indices.firstOrNull { VALUES[it] == value } ?: DEFAULT_DEPTH
        }
    }
}

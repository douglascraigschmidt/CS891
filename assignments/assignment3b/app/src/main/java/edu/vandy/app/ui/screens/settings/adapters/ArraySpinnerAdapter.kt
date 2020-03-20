package edu.vanderbilt.crawler.ui.screens.settings.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

open class ArraySpinnerAdapter<T> @JvmOverloads constructor(
        context: Context,
        private val array: Array<T>,
        private val showNull: Boolean = false)
    : BindingAdapter<T>(context) {

    private val nullOffset: Int = if (showNull) 1 else 0

    override fun getCount() = array.size + nullOffset

    override fun getItem(position: Int): T? {
        if (showNull && position == 0) {
            return null
        }

        return array[position - nullOffset]
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun newView(inflater: LayoutInflater, position: Int, container: ViewGroup) =
            inflater.inflate(android.R.layout.simple_spinner_item, container, false)!!

    override fun bindView(item: T?, position: Int, view: View) {
        val tv = view.findViewById<TextView>(android.R.id.text1)
        tv.text = getName(item)
    }

    override fun newDropDownView(inflater: LayoutInflater, position: Int, container: ViewGroup) =
            inflater.inflate(android.R.layout.simple_spinner_dropdown_item, container, false)!!

    protected fun getName(item: T?): String = item.toString()

    fun getPositionForValue(value: T): Int? {
        return array.indices.firstOrNull { array[it] == value }
    }
}

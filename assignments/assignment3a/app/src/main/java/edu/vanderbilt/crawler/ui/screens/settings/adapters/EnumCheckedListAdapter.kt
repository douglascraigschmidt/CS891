package edu.vanderbilt.crawler.ui.screens.settings.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import edu.vanderbilt.crawler.R

open class EnumCheckedListAdapter<T : Enum<T>> constructor(
        context: Context,
        enumType: Class<T>,
        private val checkedList: List<Boolean>,
        private val showNull: Boolean = false)
    : BindingAdapter<T>(context) {
    // enumType must have at least one value
    private val enumConstants: Array<T> = enumType.enumConstants!!
    private val nullOffset: Int = if (showNull) 1 else 0

    override fun getCount() = enumConstants.size + nullOffset

    override fun getItem(position: Int): T? {
        if (showNull && position == 0) {
            return null
        }

        return enumConstants[position - nullOffset]
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun newView(inflater: LayoutInflater, position: Int, container: ViewGroup)
            = inflater.inflate(R.layout.settings_checked_item, container, false)!!

    override fun bindView(item: T?, position: Int, view: View) {
        val tv = view.findViewById<TextView>(R.id.textView)
        val cb = view.findViewById<CheckBox>(R.id.checkBox)
        tv.text = getName(item)
        cb.isChecked = isChecked(position)
    }

    override fun newDropDownView(inflater: LayoutInflater, position: Int, container: ViewGroup)
            = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, container, false)!!

    open protected fun getName(item: T?): String = item.toString()

    open protected fun isChecked(position: Int): Boolean = checkedList[position]
}
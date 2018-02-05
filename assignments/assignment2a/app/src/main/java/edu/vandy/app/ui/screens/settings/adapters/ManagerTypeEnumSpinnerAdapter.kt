package edu.vandy.app.ui.screens.settings.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * Special adapter that filters out all Factory enum
 * values* that are set to the static NoManager class.
 */
open class ManagerTypeEnumSpinnerAdapter<T : Enum<T>> @JvmOverloads constructor(
        context: Context,
        enumType: Class<T>,
        private val showNull: Boolean = false)
    : BindingAdapter<T>(context) {
    private val inputEnumConstants: Array<T> = enumType.enumConstants
    private val enumConstants: List<T>
    private val nullOffset: Int = if (showNull) 1 else 0

    override fun getCount() = enumConstants.size + nullOffset

    override fun getItem(position: Int): T? {
        if (showNull && position == 0) {
            return null
        }

        return enumConstants[position - nullOffset]
    }

    /**
     * Get rid of any enumerated values that
     * are set to the "NoManager" class.
     */
    init {
        enumConstants =
                inputEnumConstants
                        .filter { it.toString() != "NoManager" }
                        .toList()
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun newView(inflater: LayoutInflater, position: Int, container: ViewGroup)
            = inflater.inflate(android.R.layout.simple_spinner_item, container, false)!!

    override fun bindView(item: T?, position: Int, view: View) {
        val tv = view.findViewById<TextView>(android.R.id.text1)
        tv.text = getName(item)
    }

    override fun newDropDownView(inflater: LayoutInflater, position: Int, container: ViewGroup)
            = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, container, false)!!

    private fun getName(item: T?): String = item.toString()

    fun getPositionForValue(value: T): Int {
        return enumConstants.indices.first { enumConstants[it] == value }
    }
}

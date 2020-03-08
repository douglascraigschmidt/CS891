package edu.vanderbilt.crawler.ui.screens.settings.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

/**
 * An implementation of [BaseAdapter] which uses the new/forActivity pattern for its views.
 *
 * NOTE!!
 * Make sure that any implementation that uses this adapter that creates a set of widgets
 * of a given type, always assigns a unique ID to each widget. This will ensure that the
 * widget's default save/restore state handler will work as expected.
 */
abstract class BindingAdapter<T>(val context: Context) : BaseAdapter() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    /**
     * Returns the adapter item at [position].
     */
    abstract override fun getItem(position: Int): T?

    /**
     * Create a new instance of a view for the specified position.
     */
    abstract fun newView(inflater: LayoutInflater, position: Int, container: ViewGroup): View

    /**
     * Bind the data for the specified `position` to the view.
     */
    abstract fun bindView(item: T?, position: Int, view: View)

    /**
     * Creates and binds a view. If a [view] is not null, then the
     * passed view is bound.
     */
    override fun getView(position: Int, view: View?, container: ViewGroup): View {
        val resultView = view ?: newView(inflater, position, container)
        bindView(getItem(position), position, resultView)
        return resultView
    }

    override fun getDropDownView(position: Int, view: View?, container: ViewGroup): View {
        val resultView = view ?: newDropDownView(inflater, position, container)
        bindDropDownView(getItem(position), position, resultView)
        return resultView
    }

    /**
     * Create a new instance of a drop-down view for the specified position.
     */
    open fun newDropDownView(inflater: LayoutInflater, position: Int, container: ViewGroup): View
            = newView(inflater, position, container)

    /**
     * Bind the data for the specified `position` to the drop-down view.
     */
    open fun bindDropDownView(item: T?, position: Int, view: View) {
        bindView(item, position, view)
    }
}

package edu.vanderbilt.webcrawler.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.*
import kotlin.reflect.KProperty

abstract class DecoratedAdapter<T, VH : RecyclerView.ViewHolder>(
        private val context: Context,
        val noDups: Boolean = true,
        list: List<T> = arrayListOf()) : RecyclerView.Adapter<VH>(), AnkoLogger {

    private var wrappedItems: MutableList<WrappedItem>

    init {
        wrappedItems = list.map { WrappedItem(it) }.toMutableList()
    }

    /** Simple wrapper class that keeps track of activated items. */
    inner class WrappedItem(val data: T, var extraData: Any? = null) {
        var selected: Boolean = false
        operator fun component1() = data
        operator fun component2() = extraData
    }

    /** Immutable list of all items. */
    val items: List<T>
        get() = List(wrappedItems.size) { wrappedItems[it].data }

    /** Immutable list of activated items. */
    val selectedItems: List<T>
        get() = wrappedItems.filter { it.selected }.map { it.data }.toList()

    /** Immutable list of activated item positions. */
    val selectedItemPositions: List<Int>
        get() = (wrappedItems.indices).filter { wrappedItems[it].selected }.toList()

    /** Selected item count. */
    val selectedCount: Int
        get() = wrappedItems.filter { it.selected }.count()

    /** Support for delegation - returns the list of all inner item values. */
    operator fun getValue(thisRef: Any?,
                          property: KProperty<*>): List<T> = items

    /** Support for delegation - wraps all the passed items in a new WrappedItem list. */
    operator fun setValue(thisRef: Any?,
                          property: KProperty<*>,
                          value: MutableList<T>) = setItems(value)

    /** Support for val item = adapter[pos] convention. */
    operator fun get(pos: Int): T = wrappedItems[pos].data

    /** Support for adapter[pos] = item convention. */
    operator fun set(pos: Int, item: T) = replaceItem(pos, item)

    /** Support for adapter += item */
    operator fun plusAssign(item: T) = addItem(item)

    /**
     * Returns the number of items in this adapter.
     */
    override fun getItemCount(): Int = wrappedItems.size


    /**
     * Sets the adapter data to the contents of the passed list. It does not
     * keep a reference to the past list and stores the list contents in an
     * List<T>.
     */
    fun setItems(list: List<T>?) {
        clear()
        list?.forEach { addItem(it) }
        notifyDataSetChanged()
    }

    /**
     * Returns the specified adapter item at [pos].
     */
    fun getItem(pos: Int): T = wrappedItems[pos].data

    /**
     * Returns the specified adapter item at [pos].
     */
    fun getItemPos(item: T): Int = wrappedItems.indexOfFirst { it.data == item }

    /**
     * For efficiency, can be used to get the item and extra
     * with one call and no memory allocations.
     */
    fun getWrappedItem(item: T): WrappedItem? = wrappedItems.find { it.data == item }

    /**
     * Returns the specified adapter item at [pos].
     */
    fun getIndexOf(item: T): Int = items.indexOf(item)

    /**
     * Adds an [item] to the adapter list.
     */
    fun addItem(item: T, extra: Any? = null) {
        if (items.contains(item) && noDups) {
            val pos = items.indexOf(item)
            if (wrappedItems[pos].extraData != extra) {
                debug("Item already exists in this but extra data has changed adapter: $item")
                wrappedItems[pos].extraData = extra
                notifyItemChanged(pos)
            } else {
                warn("Item already exists in this adapter: $item")
            }
        } else {
            debug("Adapter adding item $item")
            wrappedItems.add(WrappedItem(item, extra))
            notifyItemInserted(wrappedItems.lastIndex)
        }
    }

    /**
     * Removes the item at [pos].
     */
    fun removeItem(item: T) {
        val index = items.indexOf(item)
        if (index != -1) {
            wrappedItems.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    /**
     * Removes the item at [pos].
     */
    fun removeItem(pos: Int) {
        wrappedItems.removeAt(pos)
        notifyItemRemoved(pos)
    }

    /**
     * Removes the items at the specified list of adapter item positions.
     */
    fun removeItems(list: List<Int>) {
        list.sorted().reversed().forEach {
            removeItem(it)
        }
    }

    /**
     * Adds item to the top of the adapter list.
     */
    fun pushItem(item: T, extra: Any? = null) {
        if (items.contains(item) and noDups) {
            val from = items.indexOf(item)
            wrappedItems.removeAt(from)
            wrappedItems.add(0, WrappedItem(item, extra))
            notifyItemMoved(from, 0)
            notifyItemChanged(0)
        } else {
            wrappedItems.add(0, WrappedItem(item, extra))
            notifyItemInserted(0)
        }
    }

    /**
     * Removes the last item in the adapter list.
     */
    fun popItem() {
        wrappedItems.dropLast(1)
        notifyItemRemoved(wrappedItems.count())
    }

    fun replaceItem(pos: Int, item: T) {
        when (pos) {
            in 0..wrappedItems.lastIndex -> {
                if (wrappedItems[pos] != item) {
                    wrappedItems[pos] = WrappedItem(item)
                    notifyItemChanged(pos)
                }
            }
            else -> throw IndexOutOfBoundsException(
                    "Adapter index $pos is out of bounds.")
        }
    }

    /**
     * Adds [list] of items to the end of the adapter list.
     */
    fun addItems(list: List<T>) {
        val start = wrappedItems.size
        list.forEach { wrappedItems.add(WrappedItem(it)) }
        notifyItemRangeInserted(start, wrappedItems.count())
    }

    /**
     * Removes all items.
     */
    fun clear() {
        wrappedItems.clear()
        notifyDataSetChanged()
    }

    /**
     * Returns extra data object that is currently stored with the item at [pos].
     */
    fun getExtraData(pos: Int): Any? {
        if (pos < 0 || pos >= wrappedItems.size) {
            throw IndexOutOfBoundsException("Invalid adapter item position $pos.")
        }

        return wrappedItems[pos].extraData
    }

    /**
     * Sets the [extraData] object for the time at the specified [pos] and
     * refreshes the item if [refresh] is true.
     */
    fun setExtraData(pos: Int, extraData: Any?, refresh: Boolean = true) {
        if (pos < 0 || pos >= wrappedItems.size) {
            throw IndexOutOfBoundsException("Invalid adapter item position $pos.")
        }

        wrappedItems[pos].extraData = extraData

        if (refresh) {
            notifyItemChanged(pos)
        }
    }
}

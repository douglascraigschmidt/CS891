package edu.vanderbilt.crawler.adapters

import androidx.recyclerview.widget.RecyclerView
import edu.vanderbilt.crawler.utils.KtLogger
import kotlin.reflect.KProperty

abstract class DecoratedAdapter<ItemType, VH : RecyclerView.ViewHolder>(
        val noDups: Boolean = true,
        list: List<ItemType> = arrayListOf()) : RecyclerView.Adapter<VH>(), KtLogger {

    private var wrappedItems: MutableList<WrappedItem>

    init {
        wrappedItems = list.map { WrappedItem(it) }.toMutableList()
    }

    /** Simple wrapper class that keeps track of activated items. */
    inner class WrappedItem(var item: ItemType) {
        var selected: Boolean = false
    }

    /** Immutable list of all items. */
    val items: List<ItemType>
        get() = List(wrappedItems.size) { wrappedItems[it].item }

    /** Immutable list of activated items. */
    val selectedItems: List<ItemType>
        get() = wrappedItems.filter { it.selected }.map { it.item }.toList()

    /** Immutable list of activated item positions. */
    val selectedItemPositions: List<Int>
        get() = (wrappedItems.indices).filter { wrappedItems[it].selected }.toList()

    /** Selected item count. */
    val selectedCount: Int
        get() = wrappedItems.filter { it.selected }.count()

    /** Support for delegation - returns the list of all inner item values. */
    operator fun getValue(thisRef: ItemType,
                          property: KProperty<*>): List<ItemType> = items

    /** Support for delegation - wraps all the passed items in a new WrappedItem list. */
    operator fun setValue(thisRef: ItemType,
                          property: KProperty<*>,
                          value: MutableList<ItemType>) = setItems(value)

    /** Support for val item = adapter[pos] convention. */
    operator fun get(pos: Int): ItemType = wrappedItems[pos].item

    /** Support for adapter[pos] = item convention. */
    operator fun set(pos: Int, item: ItemType) = replaceItem(pos, item)

    /** Support for adapter += item */
    operator fun plusAssign(item: ItemType) = addItem(item)

    /**
     * Returns the number of items in this adapter.
     */
    override fun getItemCount(): Int = wrappedItems.size

    /**
     * Sets the adapter item to the contents of the passed list. It does not
     * keep a reference to the past list and stores the list contents in an
     * List<T>.
     */
    fun setItems(list: List<ItemType>, notify: Boolean = true) {
        wrappedItems = list.map { WrappedItem(it) }.toMutableList()
        if (notify) {
            notifyDataSetChanged()
        }
    }

    /**
     * Returns the specified adapter item at [pos].
     */
    fun getItem(pos: Int): ItemType = wrappedItems[pos].item

    /**
     * Returns the specified adapter item at [pos].
     */
    fun getItemPos(item: ItemType): Int = wrappedItems.indexOfFirst { it.item == item }

    /**
     * For efficiency, can be used to get the item and extra
     * with one call and no memory allocations.
     */
    fun getWrappedItem(item: ItemType): WrappedItem? = wrappedItems.find { it.item == item }

    /**
     * Returns the specified adapter item at [pos].
     */
    fun getIndexOf(item: ItemType): Int = items.indexOf(item)

    /**
     * Adds an [item] to the adapter list.
     */
    fun addItem(item: ItemType) {
        wrappedItems.indexOfFirst { wrappedItem -> wrappedItem.item == item }
        wrappedItems.add(WrappedItem(item))
        notifyItemInserted(wrappedItems.lastIndex)
    }

    /**
     * Only updates the item data if it has changed.
     */
    open fun updateItem(pos: Int, item: ItemType) {
        if (wrappedItems[pos].item != item) {
            wrappedItems[pos].item = item
            notifyItemChanged(pos)
        }
    }

    /**
     * Removes the item at [pos].
     */
    fun removeItem(item: ItemType) {
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
    fun pushItem(item: ItemType) {
        if (items.contains(item) and noDups) {
            val from = items.indexOf(item)
            wrappedItems.removeAt(from)
            wrappedItems.add(0, WrappedItem(item))
            notifyItemMoved(from, 0)
            notifyItemChanged(0)
        } else {
            wrappedItems.add(0, WrappedItem(item))
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

    fun replaceItem(pos: Int, item: ItemType) {
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
    fun addItems(list: List<ItemType>) {
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
}

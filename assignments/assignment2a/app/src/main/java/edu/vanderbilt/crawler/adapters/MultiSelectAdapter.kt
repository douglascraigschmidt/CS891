package edu.vanderbilt.crawler.adapters

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.RecyclerView
import edu.vanderbilt.crawler.R
import java.util.*
import kotlin.reflect.KProperty

/**
 * This abstract adapter class manages the item selection state for any List of
 * objects. It uses an ArrayList wrapper that maintains an additional selections
 * state boolean for each item in the adapter's item list. Note that since this
 * implementation uses its own ArrayList wrapper, any changes the application
 * makes on the originally passed list will not be reflected in the adapter
 * list. This means that the adapter owns the item and any changes such as
 * adding, deleting, or sorting, must be done by the adapter itself through
 * provided methods.
 */
abstract class MultiSelectAdapter<T, VH : SelectableViewHolder>(
        private val context: Context,
        list: List<T> = arrayListOf(),
        var onSelectionListener: OnSelectionListener? = null,
        var selectionEnabled: Boolean = true) : RecyclerView.Adapter<VH>() {

    private var wrappedItems: MutableList<WrappedItem>

    var actionMode: ActionMode? = null

    init {
        wrappedItems = list.map { WrappedItem(it) }.toMutableList()
    }

    companion object {
        val KEY_SELECTION_POSITIONS = "selectedPositions"
    }

    protected var refreshCount: Int = 0

    /** Simple wrapper class that keeps track of activated items. */
    inner class WrappedItem(val data: T, var extraData: Any? = null) {
        var selected: Boolean = false
    }

    /** Support for delegation. */
    operator fun getValue(thisRef: Any?, property: KProperty<*>): List<T> {
        return items
    }

    /** Support for delegation. */
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: MutableList<T>) {
        setData(value)
    }

    /** Support for val item = adapter[position] convention. */
    operator fun get(position: Int): T = wrappedItems[position].data

    /** Support for adapter[position] = item convention. */
    operator fun set(position: Int, item: T) {
        replaceItem(position, item)
    }

    /** Support for adapter += item */
    operator fun plusAssign(item: T) {
        addItem(item)
    }

    /** Immutable list of all items. */
    val items: List<T>
        get() {
            return List(wrappedItems.size) { wrappedItems[it].data }
        }

    /** Immutable list of activated items. */
    val selectedItems: List<T>
        get() {
            return wrappedItems.filter { it.selected }.map { it.data }.toList()
        }

    /** Immutable list of activated item positions. */
    val selectedItemPositions: List<Int>
        get() {
            val list = (wrappedItems.indices).filter { wrappedItems[it].selected }.toList()
            return list.toList()
        }

    /** Selected item count. */
    val selectedCount: Int
        get() {
            return wrappedItems.filter { it.selected }.count()
        }

    /**
     * Sets the item view activated state to the adapter item's selected state
     * and installs a long-click listener for starting action mode.
     */
    override fun onBindViewHolder(holder: VH, position: Int) {
        with(holder) {
            assert(position == holder.adapterPosition)
            selectable = isItemSelected(position)
            activated = selectable
            itemView.setOnLongClickListener {
                if (selectionEnabled && actionMode == null) {
                    if (onSelectionListener?.onActionModeStarting() == true) {
                        actionMode = startActionMode()
                        onSelectionListener?.run { onActionModeStarted() }
                        toggleSelection(adapterPosition)
                        activated = true
                        selectable = true
                    }
                }

                // Don't swallow this long click event if the action mode
                // has a context menu. This will allow the context menu
                // to popup on all long-clicks including the first one that
                // starts the action mode.
                actionMode?.menu != null
            }

            itemView.setOnClickListener {
                if (selectionEnabled && actionMode != null) {
                    // Use holder.adapterPosition which may have changed since
                    // the initial binding of this holder
                    toggleSelection(adapterPosition)
                    activated = !activated
                    selectable = activated
                } else {
                    onSelectionListener?.onItemClick(itemView, adapterPosition)
                }
            }
        }
    }

    /**
     * Starts action mode an selects all items.
     */
    fun startActionModeAndSelectAll() {
        if (actionMode == null && onSelectionListener?.onActionModeStarting() == true) {
            actionMode = startActionMode()
            onSelectionListener?.onActionModeStarted()
        }

        if (actionMode != null) {
            selectItem(-1)
        }
    }

    /**
     * Returns an ActionMode handler to support multiple list selections and deletions.
     */
    private fun startActionMode(): ActionMode? {
        val dataObserver = object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                if (selectedCount == 0) {
                    actionMode?.finish()
                }
            }

            override fun onChanged() {
                if (selectedCount == 0) {
                    actionMode?.finish()
                }
            }
        }

        with(context as AppCompatActivity) {
            return startSupportActionMode(
                    object : ActionMode.Callback {
                        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                            registerAdapterDataObserver(dataObserver)
                            menuInflater.inflate(R.menu.menu_action_mode, menu)
                            return true
                        }

                        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                            return true
                        }

                        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                            when (item.itemId) {
                                R.id.action_delete -> {
                                    onSelectionListener?.run {
                                        val items = selectedItemPositions
                                        if (onActionModeCommand(items, item.itemId)) {
                                            return true
                                        }
                                    }

                                    removeItems(selectedItemPositions)
                                    finishActionMode()

                                    return true
                                }

                                R.id.action_select_all -> {
                                    selectItem(-1)
                                    return true
                                }

                                else -> {
                                    onSelectionListener?.run {
                                        if (!onActionModeCommand(
                                                        selectedItemPositions, item.itemId)) {
                                            finishActionMode()
                                        }
                                    }
                                    return false
                                }
                            }
                        }

                        override fun onDestroyActionMode(mode: ActionMode) {
                            unregisterAdapterDataObserver(dataObserver)
                            finishActionMode()
                        }
                    })
        }
    }

    /**
     * Finishes (ends) the currently running action mode handler.
     */
    fun finishActionMode() {
        selectItem(-1, false)
        actionMode?.finish()
        actionMode = null
        onSelectionListener?.run { onActionModeFinished() }
    }

    /**
     * Sets the adapter item to the contents of the passed list. It does not
     * keep a reference to the past list and stores the list contents in an
     * List<T>.
     */
    fun setData(list: List<T>?) {
        clear()
        list?.forEach { addItem(it) }
        notifyDataSetChanged()
    }

    /**
     * Sets the adapter item to the contents of the passed list. It does not
     * keep a reference to the past list and stores the list contents in an
     * List<T>.
     */
    fun setItems(list: List<T>, notify: Boolean = true) {
        wrappedItems = list.map { WrappedItem(it) }.toMutableList()
        if (notify) {
            notifyDataSetChanged()
        }
    }

    /**
     * Returns the specified adapter item at [position].
     */
    fun getItem(position: Int): T {
        return wrappedItems[position].data
    }

    /**
     * Adds an [item] to the adapter list.
     */
    fun addItem(item: T, extra: Any? = null) {
        wrappedItems.add(WrappedItem(item, extra))
        notifyItemInserted(wrappedItems.size - 1)
    }

    /**
     * Removes the item at [position].
     */
    fun remove(position: Int) {
        if (wrappedItems[position].selected) {
            selectItem(position, false)
        }
        wrappedItems.removeAt(position)
        notifyItemRemoved(position)
    }

    /**
     * Removes the items at the specified list of adapter item positions.
     */
    fun removeItems(list: List<Int>) {
        list.sorted().reversed().forEach {
            remove(it)
        }
    }

    /**
     * Adds item to the top of the adapter list.
     */
    fun push(item: T, extra: Any? = null) {
        wrappedItems.add(0, WrappedItem(item, extra))
        notifyItemInserted(0)
    }

    /**
     * Removes the last item in the adapter list.
     */
    fun pop() {
        wrappedItems.removeAt(wrappedItems.lastIndex)
        notifyItemRemoved(wrappedItems.count())
    }

    open fun replaceItem(position: Int, item: T) {
        when (position) {
            in 0..wrappedItems.lastIndex -> {
                if (wrappedItems[position] != item) {
                    wrappedItems[position] = WrappedItem(item)
                    notifyItemChanged(position)
                }
            }
            else -> throw IndexOutOfBoundsException("Adapter index $position is out of bounds.")
        }
    }

    /**
     * Adds [list] of items to the end of the adapter list.
     */
    fun addAll(list: List<T>) {
        val start = wrappedItems.size
        list.forEach { wrappedItems.add(WrappedItem(it)) }
        notifyItemRangeInserted(start, wrappedItems.count())
    }

    /**
     * Removes all items.
     */
    fun clear() {
        selectItem(-1, false)
        wrappedItems.clear()
        notifyDataSetChanged()
    }

    /**
     * Helper method for sub-classes. Redirects request to listener if a
     * listener has been set.
     */
    protected fun showRefresh(show: Boolean) {
        onSelectionListener?.onShowRefresh(show)
    }

    /**
     * Toggles the selection state of the item at the specified [position].
     */
    fun toggleSelection(position: Int): Boolean {
        selectItem(position, !wrappedItems[position].selected)
        return isItemSelected(position)
    }

    /**
     * Change the selection state of the item at the specified [position]
     * or all items if [position] is -1.
     */
    fun selectItem(position: Int, select: Boolean = true) {
        var changed = 0

        when (position) {
            in 0..wrappedItems.lastIndex -> with(wrappedItems[position]) {
                if (selected != select) {
                    selected = select
                    changed++
                }
                notifyItemChanged(position)
            }
            -1 -> {
                wrappedItems.forEach {
                    if (it.selected != select) {
                        it.selected = select
                        changed++
                    }
                }
            }
            else -> throw IndexOutOfBoundsException(
                    "Unable to select item $position: index out of bounds.")
        }

        if (changed > 0) {
            notifyDataSetChanged()
            onSelectionListener?.onSelectionChanged(selectedCount)
            if (selectedCount == 0) {
                actionMode?.finish()
            }
        }
    }

    fun selectItems(positions: List<Int>) {
        positions.forEach {
            if (it in 0..wrappedItems.lastIndex) {
                selectItem(it, true)
            } else {
                throw IndexOutOfBoundsException(
                        "Unable to select item $it: index out of bounds.")
            }
        }
    }

    /**
     * Checks if an item is activated at the specified [position].
     */
    fun isItemSelected(position: Int): Boolean {
        return wrappedItems[position].selected
    }

    /**
     * Returns the number of items in this adapter.
     */
    override fun getItemCount(): Int {
        return wrappedItems.size
    }

    /**
     * Returns extra item object that is currently stored with the item at [position].
     */
    fun getExtraData(position: Int): Any? {
        if (position < 0 || position >= wrappedItems.size) {
            throw IndexOutOfBoundsException("Invalid adapter item position $position.")
        }

        return wrappedItems[position].extraData
    }

    /**
     * Sets the [extraData] object for the time at the specified [position] and
     * refreshes the item if [refresh] is true.
     */
    fun setExtraData(position: Int, extraData: Any?, refresh: Boolean = true) {
        if (position < 0 || position >= wrappedItems.size) {
            throw IndexOutOfBoundsException("Invalid adapter item position $position.")
        }

        wrappedItems[position].extraData = extraData

        if (refresh) {
            notifyItemChanged(position)
        }
    }

    /**
     * Returns a Bundle containing the selection state of all items.
     */
    fun saveSelectionStates(): Bundle? {
        val positions = selectedItemPositions
        return if (positions.isNotEmpty()) {
            val bundle = Bundle()
            bundle.putIntegerArrayList(KEY_SELECTION_POSITIONS, ArrayList(positions))
            bundle
        } else {
            null
        }
    }

    /**
     * Restores item selection states from a the passed [savedStates] bundle.
     */
    fun restoreSelectionStates(savedStates: Bundle?) {
        if (savedStates != null) {
            actionMode = startActionMode()
            savedStates.getIntegerArrayList(KEY_SELECTION_POSITIONS)?.run {
                selectItems(toList())
            }
        }
    }

    /**
     * Listener interface used for notifying application about action mode events
     * and item click events.
     */
    interface OnSelectionListener {
        /**
         * Called when adapter is about to begin ActionMode. Return `true` to
         * allow action mode to startCrawlAsync, `false` to prevent action mode from
         * starting.
         */
        fun onActionModeStarting(): Boolean = true

        /**
         * Called when ActionMode has started; return false
         * to prevent action mode from starting or true to
         * allow it to start.
         */
        fun onActionModeStarted() {}

        /**
         * Called when ActionMode has ended or has been cancelled.
         */
        fun onActionModeFinished() {}

        /**
         * Called when a app bar menu item is clicked during action mode
         * passing the list of selected item positions [selections].
         * Delete and select all menu items are handled by this adapter
         * and onActionModeCommand will not be called for these actions.
         *
         * Return `true` if the command was handle, and `false` to let
         * the adapter handle the action mode command. If `false` is
         * returned, the listener is responsible for choosing to continue
         * or end the current action mode session.
         */
        fun onActionModeCommand(selections: List<Int>, menuActionId: Int): Boolean = false

        /**
         * Hook method called when an item [view] at [position] is clicked.
         */
        fun onItemClick(view: View, position: Int) {}

        /**
         * Hook method called when item [view] at [position] is long clicked.
         * Return `true` to startCrawlAsync action mode and `false` to prevent action
         * mode from starting.
         */
        fun onItemLongClick(view: View, position: Int): Boolean = true

        /**
         * Hook method called when any items are waiting for item. When [show] is true
         * a busy indicator is displayed, and when [show] is false, the indicator is
         * hidden.
         */
        fun onShowRefresh(show: Boolean) = {}

        /**
         * Called whenever the selection count changes. For efficiency, passes the
         * selection count instead of building and passing a seleected item list.
         */
        fun onSelectionChanged(count: Int) {}
    }
}

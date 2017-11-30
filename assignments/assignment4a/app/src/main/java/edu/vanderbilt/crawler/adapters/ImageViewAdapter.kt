package edu.vanderbilt.crawler.adapters

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import edu.vanderbilt.crawler.R
import edu.vanderbilt.crawler.extensions.*
import edu.vanderbilt.crawler.ui.adapters.MultiSelectAdapter
import edu.vanderbilt.crawler.ui.adapters.SelectableViewHolder
import edu.vanderbilt.crawler.utils.KtLogger
import edu.vanderbilt.crawler.viewmodels.Resource
import edu.vanderbilt.crawler.viewmodels.Resource.State.*
import org.jetbrains.anko.find
import java.io.File

/**
 * Adapter that displays an image URL and its associated image.
 */
class ImageViewAdapter(context: Context,
                       list: MutableList<Resource> = mutableListOf(),
                       val gridLayout: Boolean = false,
                       onSelectionListener: OnSelectionListener? = null)
    : MultiSelectAdapter<Resource, ImageViewAdapter.Holder>(
        context, list, onSelectionListener),
        KtLogger {

    companion object {
        val KEY_PROGRESS_VALUE = "key_progress_value"
        val KEY_SIZE = "key_size"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(
                if (gridLayout) R.layout.image_grid_item else R.layout.image_list_item,
                parent,
                false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
        super.onBindViewHolder(holder, position)
    }

    override fun onBindViewHolder(holder: Holder?,
                                  position: Int,
                                  payloads: MutableList<Any>?) {
        if (!payloads!!.isEmpty()) {
            // Local DiffUtils implementation puts a bundle at index 0.
            with(payloads[0] as Bundle) {
                val resource = items[position]
                keySet().forEach { key ->
                    when (key) {
                        KEY_PROGRESS_VALUE -> holder!!.setProgress(resource)
                        KEY_SIZE -> holder!!.imageSize.text = holder.getImageSize(resource)
                        else -> {
                            throw IllegalStateException("Unsupported DiffUtils key value: $key")
                        }
                    }
                }
            }
            // Assumption: at least 1 key will match so
            // don't call default handler.
            return
        }

        super.onBindViewHolder(holder, position, payloads)
    }

    /**
     * Updates the state of an existing item.
     */
    override fun replaceItem(position: Int, item: Resource) {
        val oldItem = getItem(position)
        validateStateChange(oldItem.state, item.state)

        if (!DiffUtilCallback.areItemsTheSame(oldItem, item)) {
            throw Exception("Replace items must be logically equal")
        }

        if (!DiffUtilCallback.areContentsTheSame(oldItem, item)) {
            super.replaceItem(position, item)
        }
    }

    fun updateItems(newItems: List<Resource>) {
        val oldItems = items
        val result = DiffUtil.calculateDiff(
                DiffUtilCallback(oldItems, newItems))
        setItems(newItems, false)
        result.dispatchUpdatesTo(this)
    }

    /**
     * State change verification. Expected transitions are:
     *   [CREATE|LOAD] -> [READ|WRITE|PROCESS|DOWNLOAD] -> CLOSE
     */
    private fun validateStateChange(oldState: Resource.State,
                                    newState: Resource.State): Boolean {
        when (newState) {
            CREATE, LOAD ->
                error("Illegal state change " +
                      "${oldState.name} -> ${newState.name}")
            PROCESS,
            READ,
            WRITE,
            DOWNLOAD ->
                if (oldState != CREATE) {
                    error("Illegal state change " +
                          "${oldState.name} -> ${newState.name}")
                }
            CLOSE -> {
            }
        }

        return true
    }

    /**
     * Sets all items to CLOSE state so that transient
     * gif animations are stopped.
     */
    fun crawlStopped(cancelled: Boolean = false) {
        items.forEachIndexed { pos, item ->
            when (item.state) {
                Resource.State.DOWNLOAD,
                Resource.State.CREATE,
                Resource.State.READ,
                Resource.State.WRITE,
                Resource.State.PROCESS,
                Resource.State.CANCEL -> {
                    val state = if (cancelled) CANCEL else CLOSE
                    super.replaceItem(pos, item.copy(state = state))
                }
                Resource.State.LOAD,
                Resource.State.CLOSE -> {
                }
            }
        }
    }

    inner class Holder(val view: View) : SelectableViewHolder(view) {
        private lateinit var url: String
        private val textView: TextView? = if (gridLayout) null else view.find(R.id.textView)
        private val threadView = view.find<TextView>(R.id.thread)
        private val imageView = view.find<ImageView>(R.id.imageView)
        internal val imageSize = view.find<TextView>(R.id.imageSize)
        private val progressBar = view.find<ProgressBar>(R.id.progressBar)
        private val indeterminateProgressBar = view.find<ProgressBar>(R.id.indeterminateProgressBar)
        private val state = view.find<TextView>(R.id.state)

        fun bind(resource: Resource) {
            url = resource.url
            textView?.text = url
            imageSize.text = getImageSize(resource)
            threadView.text = getThreadId(resource)
            imageSize.setTextColor(
                    if (resource.state == CANCEL)
                        Color.RED
                    else {
                        ContextCompat.getColor(
                                view.ctx, android.R.color.primary_text_light)
                    })
            state.text = resource.state.name
            setImage(resource)
            setProgress(resource)
        }

        fun setProgress(resource: Resource) {
            progressBar.max = 100
            when (resource.state) {
                DOWNLOAD -> setProgress(resource.progress, Color.RED)
                WRITE -> setProgress(resource.progress, Color.BLUE)
                READ -> setProgress(resource.progress, Color.GREEN)
                PROCESS -> setProgress(resource.progress, Color.CYAN)
                LOAD, CREATE, CLOSE, CANCEL -> hideProgressBar()
            }
        }

        internal fun getImageSize(resource: Resource): String {
            return if (resource.size != 0) {
                roundSizeToNearestK(view.context, resource.size)
            } else {
                when (resource.state) {
                    READ -> "reading"
                    DOWNLOAD -> "downloading"
                    PROCESS -> "transforming"
                    LOAD, CLOSE, CANCEL -> {
                        resource.filePath?.let {
                            roundSizeToNearestK(view.context, File(it).length().toInt())
                        } ?: roundSizeToNearestK(view.context, 0)
                    }
                    else -> roundSizeToNearestK(view.context, 0)
                }
            }
        }

        internal fun roundSizeToNearestK(context: Context, size: Int): String {
            return if (size == 0) {
                context.getString(R.string.image_size_format, 0f)
            } else {
                context.getString(
                        R.string.image_size_format,
                        Math.ceil(size.toFloat() / 1e3))
            }
        }

        private fun getThreadId(resource: Resource): String {
            return when (resource.state) {
                Resource.State.LOAD,
                Resource.State.CLOSE,
                Resource.State.CANCEL -> ""
                else -> "thread: ${resource.thread}"
            }
        }

        private fun setProgress(progress: Int, color: Int) {
            with(progressBar) {
                progressDrawable.setColorFilter(
                        color, android.graphics.PorterDuff.Mode.SRC_IN)
                this.progress = (progress * max / 100f).toInt().minmax(0, 100)
                visibility = View.VISIBLE
            }
        }

        private fun setImage(resource: Resource) {
            imageView.run {
                imageView.clear()
                when (resource.state) {
                    DOWNLOAD -> asyncLoadGif(R.drawable.down_arrow)
                    CREATE -> asyncLoad(R.drawable.placeholder)
                    READ,
                    WRITE -> asyncLoadGif(R.drawable.spinning_cdrom)
                    PROCESS -> asyncLoadGif(R.drawable.filter)
                    LOAD,
                    CANCEL,
                    CLOSE -> {
                        if (!resource.filePath.isNullOrEmpty()) {
                            val file = File(resource.filePath)
                            if (file.isFile && file.length() > 0) {
                                asyncLoad(resource.filePath!!)
                            } else {
                                asyncLoad(R.drawable.error)
                            }
                        } else {
                            asyncLoad(R.drawable.error)
                        }
                    }
                }
            }
        }

        private fun hideProgressBar() {
            indeterminateProgressBar.visibility = View.INVISIBLE
            progressBar.visibility = View.INVISIBLE
        }

        private fun showProgressBar() {
            indeterminateProgressBar.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
        }

        private fun showIndeterminateProgressBar() {
            indeterminateProgressBar.visibility = View.VISIBLE
            progressBar.visibility = View.INVISIBLE
            indeterminateProgressBar.progress = 100
        }

        override fun toString(): String {
            return super.toString() + " '$url'"
        }
    }

    class DiffUtilCallback(private val oldList: List<Resource>,
                           private val newList: List<Resource>)
        : DiffUtil.Callback() {

        companion object {
            fun areContentsTheSame(oldItem: Resource, newItem: Resource): Boolean {
                // We only care if the properties that map to
                // visual components are the same/different.
                return oldItem.state == newItem.state
                       && oldItem.size == newItem.size
                       && oldItem.progress == newItem.progress
            }

            fun areItemsTheSame(oldItem: Resource, newItem: Resource) =
                // Only care if items are the same not their contents.
                oldItem.url == newItem.url &&
                oldItem.filePath == newItem.filePath
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int,
                                     newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            // Only care at this point if the item is the same,
            // not if the contents are the same.
            return oldItem.url == newItem.url &&
                   oldItem.filePath == newItem.filePath
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
                = areContentsTheSame(oldList[oldItemPosition], newList[newItemPosition])

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]

            if (oldItem.state == newItem.state) {
                val bundle = Bundle()
                if (oldItem.progress != newItem.progress) {
                    bundle.putParcelable(KEY_PROGRESS_VALUE, newItem)
                }
                if (oldItem.size != newItem.size) {
                    bundle.putParcelable(KEY_SIZE, newItem)
                }

                // Only return bundle if something was added.
                return if (!bundle.isEmpty) bundle else null
            }

            return null
        }
    }
}

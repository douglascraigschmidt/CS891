package edu.vanderbilt.crawler.adapters

import android.content.Context
import android.graphics.Color
import android.os.Bundle
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
        context, list, onSelectionListener), KtLogger {

    companion object {
        val keyImage = "key_image"
        val keyProgressColor = "key_progress_color"
        val keyProgressValue = "key_progress_value"
        val keySize = "key_size"
        val keyThread = "key_thread"
        val keyFilePath = "key_file_path"
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

    fun onBindViewHolderNotUsed(holder: Holder?,
                                  position: Int,
                                  payloads: MutableList<Any>?) {
        if (payloads!!.isEmpty()) {
            return
        }

        with(payloads[0] as Bundle) {
            holder?.let { holder ->
                val resource = items[position]
                keySet().forEach { key ->
                    when (key) {
                        //keyImage -> holder.setImage(resource)
                        //keyProgressValue -> holder.setProgressValue(resource.progress)
                        //keyProgressColor -> holder.setProgressColor(resource)
                        //keySize -> holder.imageSize.text = holder.getSizeText(resource)
                        //keyThread -> holder.threadView.text = holder.getThreadText(resource)
                        keyFilePath -> {
                        }
                    }
                }
            }
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
    fun crawlStopped() {
        items.forEachIndexed { pos, item ->
            if (item.state != Resource.State.CLOSE) {
                super.replaceItem(pos, item.copy(state = Resource.State.CLOSE))
            }
        }
    }

    inner class Holder(val view: View) : SelectableViewHolder(view) {
        private lateinit var url: String
        private val textView: TextView? = if (gridLayout) null else view.find(R.id.textView)
        private val threadView = view.find<TextView>(R.id.thread)
        private val imageView = view.find<ImageView>(R.id.imageView)
        private val imageSize = view.find<TextView>(R.id.imageSize)
        private val progressBar = view.find<ProgressBar>(R.id.progressBar)
        private val indeterminateProgressBar = view.find<ProgressBar>(R.id.indeterminateProgressBar)
        private val state = view.find<TextView>(R.id.state)

        fun bind(resource: Resource) {
            url = resource.url
            textView?.text = url
            imageSize.text = getImageSize(resource)
            threadView.text = getThreadId(resource)
            state.text = resource.state.name
            setImage(resource)
            setProgress(resource)

        }

        private fun setProgress(resource: Resource) {
            progressBar.max = 100
            when (resource.state) {
                DOWNLOAD -> setProgress(resource.progress, Color.RED)
                WRITE -> setProgress(resource.progress, Color.BLUE)
                READ -> setProgress(resource.progress, Color.GREEN)
                PROCESS -> setProgress(resource.progress, Color.CYAN)
                LOAD,
                CREATE,
                CLOSE -> hideProgressBar()
            }
        }

        private fun getImageSize(resource: Resource): String {
            return if (resource.size == 0) {
                when (resource.state) {
                    READ -> "reading"
                    DOWNLOAD -> "downloading"
                    PROCESS -> "transforming"
                    CLOSE,
                    LOAD -> {
                        resource.filePath?.let {
                            val length = File(it).length()
                            view.ctx.getString(R.string.image_size_format, length / 1e3)
                        } ?: view.ctx.getString(R.string.image_size_format, 0f)
                    }
                    else -> view.ctx.getString(R.string.image_size_format, 0f)

                }
            } else {
                view.ctx.getString(
                        R.string.image_size_format, resource.size / 1e3)
            }
        }

        private fun getThreadId(resource: Resource): String {
            return if (resource.state != Resource.State.LOAD) {
                "thread: ${resource.thread}"
            } else {
                ""
            }
        }

        private fun setProgress(progress: Float, color: Int) {
            with(progressBar) {
                progressDrawable.setColorFilter(
                        color, android.graphics.PorterDuff.Mode.SRC_IN)
                this.progress = (progress * max).toInt().minmax(0, 100)
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
            fun areContentsTheSame(oldItem: Resource, newItem: Resource): Boolean
                    = oldItem.state == newItem.state
                      && oldItem.size == newItem.size
                      && oldItem.progress == newItem.progress

            fun areItemsTheSame(oldItem: Resource, newItem: Resource)
                    = oldItem == newItem
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int,
                                     newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
                = areContentsTheSame(oldList[oldItemPosition], newList[newItemPosition])

        fun getChangePayloadNotUsed(oldItemPosition: Int, newItemPosition: Int): Any? {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]

            val bundle = Bundle()

            if (oldItem.state != newItem.state) {
                bundle.putParcelable(keyImage, newItem)
                bundle.putBoolean(keyProgressColor, true)
            }
            if (oldItem.progress != newItem.progress) {
                bundle.putFloat(keyProgressValue, newItem.progress)
            }
            if (oldItem.size != newItem.size) {
                bundle.putInt(keySize, newItem.size)
            }
            if (oldItem.thread != newItem.thread) {
                bundle.putInt(keyThread, newItem.thread)
            }
            if (oldItem.filePath != newItem.filePath) {
                bundle.putString(keyFilePath, newItem.filePath)
            }

            return bundle
        }
    }
}

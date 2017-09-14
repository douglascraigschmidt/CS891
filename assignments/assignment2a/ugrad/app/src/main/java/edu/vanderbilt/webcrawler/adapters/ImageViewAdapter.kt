package edu.vanderbilt.webcrawler.adapters

import android.content.Context
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import edu.vanderbilt.utils.CacheUtils
import edu.vanderbilt.webcrawler.R
import edu.vanderbilt.webcrawler.extensions.File
import edu.vanderbilt.webcrawler.extensions.getResourceUri
import edu.vanderbilt.webcrawler.extensions.load
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.find
import java.net.URI

/**
 * Adapter that displays an image URL and its associated image.
 */
class ImageViewAdapter(context: Context,
                       noDups: Boolean = true,
                       var local: Boolean = false,
                       list: MutableList<String> = mutableListOf())
    : DecoratedAdapter<String, ImageViewAdapter.Holder>(context, noDups, list), AnkoLogger {

    enum class State(val size: Int) {
        PENDING(-4),
        DOWNLOADING(-3),
        DOWNLOADED(-2),
        ERROR(0),
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.image_list_item, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * Updates the state of an existing item.
     */
    fun updateItem(item: String, state: State, size: Int) {
        val pos = getItemPos(item)
        if (pos == -1) {
            Log.w("ImageViewAdapter", "updateItem called before item has been added!")
            addItem(item, size)
            return
        }

        when (getState(pos)) {
            State.PENDING -> {
                assert(state == State.DOWNLOADING
                        || state == State.DOWNLOADED
                        || state == State.ERROR)
            }
            State.DOWNLOADING -> {
                assert(state == State.DOWNLOADED
                        || state == State.DOWNLOADED
                        || state == State.ERROR)
                if (state == State.DOWNLOADING) {
                    val oldSize = getExtraData(pos)
                    // set the new size but don't refresh.
                    if (oldSize != size) {
                        setExtraData(pos, size, true)
                    }
                    return
                }
            }
            State.ERROR -> assert(false)
            State.DOWNLOADED -> assert(false)
        }

        // Set the new state and refresh.
        setExtraData(pos, state, true)
    }

    fun crawlStopped() {
        items.forEachIndexed { pos, item ->
            when (getState(pos)) {
                State.PENDING, State.DOWNLOADING, State.ERROR -> {
                    if (File(Uri.parse(item)).length() > 0) {
                        setExtraData(pos, State.DOWNLOADED, true)
                    } else {
                        setExtraData(pos, State.ERROR, true)
                    }
                }
                else -> {
                }
            }
        }
    }

    fun getState(pos: Int): State {
        val data = getExtraData(pos)
        return when (data) {
            is Int -> State.DOWNLOADED
            is State -> data
            else -> throw Exception("Invalid state for item a position $pos")
        }
    }

    inner class Holder(val view: View) : RecyclerView.ViewHolder(view) {
        private val textView = view.find<TextView>(R.id.textView)
        private val imageView = view.find<ImageView>(R.id.imageView)
        private val imageSize = view.find<TextView>(R.id.imageSize)

        fun bind(url: String) {
            with(Uri.parse(url)) {
                textView.text = mapFileUrlToWebUrl(url)
                imageSize.text = "${File(Uri.parse(url)).length().toString()} bytes"
                val pos = getIndexOf(url)
                // There is a bug where the extra data is null which
                // should not really happen. So for now, just try to
                // load the image.
                val state = getState(pos)
                when (state) {
                    State.PENDING -> {
                        val uri = view.context.getResourceUri(R.drawable.waiting)
                        imageView.load(uri.toString(), placeholder = 0, asGif = true)
                    }

                    State.DOWNLOADING -> {
                        val uri = view.context.getResourceUri(R.drawable.downloading)
                        imageView.load(uri.toString(), placeholder = 0, asGif = true)
                    }

                    State.DOWNLOADED -> {
                        imageView.load(url)
                    }

                    State.ERROR -> {
                        val uri = view.context.getResourceUri(R.drawable.error)
                        imageView.load(uri.toString())
                    }
                }
            }
        }

        /**
         * Converts "file://.../filter/path to either
         * "http://path" or "local://path" for more readable output.
         */
        private fun mapFileUrlToWebUrl(url: String): String {
            return URI(url)
                    .path
                    .removePrefix(CacheUtils.getCacheDirPath())
                    .replaceFirst(
                            "/[^/]+/".toRegex(),
                            if (local) "assets://" else "http://")
        }

        override fun toString(): String {
            return super.toString() + " '${textView.text}'"
        }
    }
}
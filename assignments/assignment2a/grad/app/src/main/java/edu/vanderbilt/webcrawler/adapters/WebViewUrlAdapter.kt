package edu.vanderbilt.webcrawler.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import edu.vanderbilt.webcrawler.R
import edu.vanderbilt.webcrawler.extensions.load
import org.jetbrains.anko.find

/**
 * Adapter that displays an image URL and its associated image.
 */
class WebViewUrlAdapter(context: Context,
                        items: MutableList<String> = mutableListOf(),
                        selectionListener: MultiSelectAdapter.OnSelectionListener? = null)
    : MultiSelectAdapter<String, WebViewUrlAdapter.Holder>(context, items, selectionListener) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.webview_url_item, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position])
        super.onBindViewHolder(holder, position)
    }

    inner class Holder(val view: View) : SelectableViewHolder(view) {
        private val textView = view.find<TextView>(R.id.textView)
        private val imageView = view.find<ImageView>(R.id.imageView)

        fun bind(url: String) {
            textView.text = url
            imageView.load(url)
        }

        override fun toString(): String {
            return super.toString() + " '${textView.text}'"
        }
    }
}

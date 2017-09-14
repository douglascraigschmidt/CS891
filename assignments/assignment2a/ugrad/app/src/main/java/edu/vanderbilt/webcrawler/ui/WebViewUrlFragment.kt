package edu.vanderbilt.webcrawler.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import edu.vanderbilt.webcrawler.R
import edu.vanderbilt.webcrawler.adapters.WebViewUrlAdapter

/**
 * Supports a maximum length item list using push and pop operations to keep
 * the list items withing the maxCount value passed to the push function
 */
class WebViewUrlFragment : Fragment() {
    companion object {
        fun newInstance(): WebViewUrlFragment = WebViewUrlFragment()
    }

    private lateinit var view: RecyclerView
    private val adapter: WebViewUrlAdapter by lazy { view.adapter as WebViewUrlAdapter }

    /**
     * Immutable URL list displayed by the adapter.
     */
    val urls: List<String>
        get() = adapter.items.toList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
            inflater: LayoutInflater?, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        view = inflater!!.inflate(
                R.layout.webview_url_list, container, false) as RecyclerView

        with(view) {
            adapter = WebViewUrlAdapter(context)
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        return view
    }

    /**
     * Pushes up the maxCount items. If the list size is equal to or greater than
     * the passed [maxCount] value, the oldest items will be popped off the list
     * to ensure that a single available spot will be available to make room for
     * the new item to be pushed to the top of the list.
     */
    fun push(url: String, maxCount: Int = Int.MAX_VALUE) {
        while (adapter.itemCount >= maxCount) {
            adapter.pop()
        }
        adapter.push(url)
    }
}

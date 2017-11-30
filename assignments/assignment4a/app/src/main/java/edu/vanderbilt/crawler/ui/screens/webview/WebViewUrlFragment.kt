package edu.vanderbilt.crawler.ui.screens.webview

import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import edu.vanderbilt.crawler.R
import edu.vanderbilt.crawler.extensions.setViewHeight
import edu.vanderbilt.crawler.ui.adapters.MultiSelectAdapter
import edu.vanderbilt.crawler.ui.adapters.WebViewUrlAdapter
import edu.vanderbilt.crawler.utils.KtLogger
import kotlinx.android.synthetic.main.activity_web_view.*
import org.jetbrains.anko.dimen

/**
 * Supports a maximum length item list using push and pop operations to keep
 * the list items withing the maxCount value passed to the push function
 */
class WebViewUrlFragment : Fragment(), MultiSelectAdapter.OnSelectionListener, KtLogger {

    companion object {
        fun newInstance(): WebViewUrlFragment = WebViewUrlFragment()
    }

    /** Initialized in onCreateVew() */
    private lateinit var view: RecyclerView

    /** Set internal to allow controlling activity to access adapter. */
    internal val adapter: WebViewUrlAdapter by lazy { view.adapter as WebViewUrlAdapter }

    /**
     * Immutable URL list displayed by the adapter.
     */
    val urls: List<String>
        get() = adapter.items.toList()

    override fun onCreateView(
            inflater: LayoutInflater?, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        view = inflater!!.inflate(
                R.layout.webview_url_list, container, false) as RecyclerView

        with(view) {
            adapter = WebViewUrlAdapter(
                    context,
                    imagePicker = (activity as WebViewActivity).imagePicker)
            // Custom layout manager to ensure that the activity bottom
            // sheet peek height adjust to the height of the first list item.
            // Note that this only works if the activity has a bottom sheet
            // with id "bottomSheet".
            layoutManager = object : LinearLayoutManager(
                    context, LinearLayoutManager.VERTICAL, false) {
                override fun onLayoutCompleted(state: RecyclerView.State?) {
                    super.onLayoutCompleted(state)
                    val pos: Int = findFirstVisibleItemPosition()
                    if (pos == 0) {
                        val view = findViewByPosition(pos)
                        with(BottomSheetBehavior.from(activity.bottomSheet)) {
                            peekHeight = view.height
                        }
                    }
                    val lastPos = findLastVisibleItemPosition()
                    if (pos >= 0) {
                        if ((lastPos - pos) + 1 < 5) {
                            view.setViewHeight(LinearLayout.LayoutParams.WRAP_CONTENT)
                        } else {
                            view.setViewHeight(dimen((R.dimen.web_view_list_height)))
                        }
                    }
                }
            }

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

        val layoutManager = view.layoutManager
        layoutManager.scrollToPosition(0)
    }
}

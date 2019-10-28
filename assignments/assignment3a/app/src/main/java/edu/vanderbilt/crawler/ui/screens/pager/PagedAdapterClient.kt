package edu.vanderbilt.crawler.ui.screens.pager

import android.net.Uri
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import edu.vanderbilt.crawler.extensions.findView

/**
 * The following is an example of a PagedAdapterClient implementation:
 *
 * ```
 * class RecyclerViewAdapter: RecyclerView.Adapter<T>, PagedAdapterClient {
 *
 *     /**
 *      * Override PagedActivityClient.pagedActivityClient
 *      * property and set to this activity.
 *      */
 *      override fun onBindViewHolder(holder: Holder, position: Int) {
 *          holder.bind(getItem(position))
 *
 *          ...
 *
 *          // Call PagedAdapterClient helper function to set the
 *          // transition name for the holders's ImageView.
 *          onBindPagedAdapterViewHolder(holder)
 *      }
 *
 *     /**
 *      * PagedAdapterClient callback is required to return a list
 *      * containing each item's image uri. Note that this list
 *      * is assumed to have the same ordering as the items appear
 *      * in the recycler view and the items ordering should not
 *      * be changed while the PagerActivity is the active.
 *      */
 *      override fun getImageUris(): List<Uri> {
 *          // Map all resources to uris for PagedActivity
 *          return items.map { it.uri }
 *      }
 * ```
 * See [PagedActivityClient] for an example of the PagedActivity
 * client activity implementation.
 */
interface PagedAdapterClient {
    companion object {
        /**
         * Helper method used to set an [ImageView]'s transition
         * name to the string value of it's adapter position.
         * This transition name is required for each image that
         * will be displayed in the PagedActivity.
         */
        fun setTransitionName(position: Int, view: ImageView) {
            ViewCompat.setTransitionName(view, position.toString())
        }
    }

    /**
     * Must be called from [RecyclerView.Adapter<T>.onBindView] to
     * assign a unique transition name to the passed [holder]'s
     * image view. The holder view must be an ImageView, or it must
     * contain a single ImageView descendant. If the holder contains
     * multiple ImageView descendants, the first one found will be
     * assumed to be the ImageView that will be used for shared
     * element transitions.
     */
    fun onBindPagedAdapterViewHolder(holder: RecyclerView.ViewHolder) {
        val imageView = holder.itemView.findView {it is ImageView}
                        ?: error("PagedAdapterClient: unable to find an ImageView")
        setTransitionName(holder.adapterPosition, imageView as ImageView)
    }

    /**
     * Implementations are required return to return a list
     * containing each item's image uri. Note that this list
     * is assumed to have the same ordering as the items appear
     * in the recycler view.
     */
    fun getImageUris(): List<Uri>
}
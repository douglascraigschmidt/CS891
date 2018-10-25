package edu.vanderbilt.crawler.ui.screens.pager

import android.annotation.TargetApi
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.SharedElementCallback
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import edu.vanderbilt.crawler.extensions.findImageViewWithTransitionName
import edu.vanderbilt.crawler.extensions.findView
import edu.vanderbilt.crawler.utils.KtLogger
import edu.vanderbilt.crawler.utils.warn
import org.jetbrains.anko.contentView

/**
 * Required interface for any RecyclerView activity that starts a
 * PagedActivity to display a selected adapter image.
 *
 * The following is an example of a PagedActivityClient implementation:
 *
 * ```
 * class RecyclerViewActivity: AppCompatActivity, PagedActivityClient {
 *
 *     /**
 *      * Override PagedActivityClient.pagedActivityClient
 *      * property and set to this activity.
 *      */
 *      override val pagedActivityClient: Activity
 *          get() = this
 *
 *     /**
 *      * Override Activity.onActivityReenter and forward
 *      * event to PagedActivityClient.onActivityReenterEvent.
 *      */
 *      override fun onActivityReenter(resultCode: Int, data: Intent) {
 *         super.onActivityReenter(resultCode, data)
 *         onActivityReenterHandler(resultCode, data)
 *      }
 * ```
 *
 * See [PagedAdapterClient] for an example of the PagedActivity client
 * adapter implementation.
 */
interface PagedActivityClient {
    /** Back pointer to Activity class implementing this interface. */
    val pagedActivityClient: Activity

    /**
     * To prevent rapid button clicking from starting
     * more than one paged activity at a time.
     */
    var pagedActivityStarted: Boolean

    /**
     * Starts a new PagedActivity using a shared element transition.
     *
     * @param view     The selected item view.
     * @param position The selected item position.
     * @param adapter  An adapter that implements the PagedAdapterClient interface.
     */
    fun startPagedActivity(view: View, position: Int, adapter: PagedAdapterClient) {
        // Use the adapter position in the shared element name so that
        // it will map to the correct image in the PagedActivity.
        // The installed custom PagedSharedElementCallback class will
        // handle replacing this shared element with whatever image is
        // displayed when the PagedActivity returns.
        val transitionName = position.toString()

        // Passed view might be an image view container, so drill down
        // to get at the image view.
        val imageView = view.findView { it is ImageView }!!

        // Now set the image view's transition name.
        ViewCompat.setTransitionName(imageView, transitionName)

        // Setup the scene transition which is passed to the
        // startActivity() call.
        val options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                        pagedActivityClient, imageView, transitionName)

        // Map all resources to uris for PagedActivity
        val uris = adapter.getImageUris()

        // Call the class factory method to create the intent to start the
        // activity.
        val intent = PagedActivity.makeIntent(pagedActivityClient, uris, position)

        // Start the activity. Note that the onActivityReenter() hook will
        // be called when returning from this started activity. When this
        // occurs, we save the passed bundle that is then used in the custom
        // PagedSharedElementCallback class.
        ActivityCompat.startActivity(pagedActivityClient, intent, options.toBundle())
    }

    /**
     * Called when an activity launched with transitions finishes and is returning
     * to the previous activity. This method is only called if the activity set
     * a result code other than RESULT_CANCELED and supports FEATURE_ACTIVITY_TRANSITIONS.
     *
     * @param resultCode The integer result code returned by the child activity
     * through its setResult().
     * @param data       An Intent, which can return result data to the caller
     * (various data can be attached to Intent "extras").
     */
    fun onActivityReenterHandler(resultCode: Int, data: Intent) {
        if (resultCode == RESULT_OK) {
            // Save the reentry extras so that they will be available when the
            // framework calls the onMapSharedElements() listen.
            installEnterTransitionHandler(pagedActivityClient, Bundle(data.extras))
        }
    }

    /**
     * Setup a custom shared element callback that will update the return shared
     * element to the current paged image displayed when returning from the
     * PagedActivity (see PagedSharedElementCallback for a more detailed
     * description).
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun installEnterTransitionHandler(activity: Activity, bundle: Bundle) {
        val recyclerView = activity.contentView?.findView { it is RecyclerView }
                ?: error("PageActivityReturnTransition: unable to find a RecyclerView.")
        ActivityCompat.setExitSharedElementCallback(
                activity,
                PagedSharedElementCallback(this, recyclerView as RecyclerView, bundle))
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun uninstallEnterTransitionHandler(activity: Activity) {
        ActivityCompat.setExitSharedElementCallback(activity, null)
    }

    /**
     * A shared element callback implementation that is installed in the
     * transition animation framework using setExitSharedElementCallback().
     * The reason for installing this handler is to handle the case where
     * the user clicks on image A to start the PagedActivity, and then swipes
     * to image B before returning to this parent activity. In this case,
     * the default Android shared element transition framework will attempt
     * (and fail) to animate image A back to its position in this activity's
     * grid.
     *
     * The onMapSharedElement() hook, handles this case and replaces the shared
     * element information for A with that of B so that the shared element
     * transition animation framework will perform the animation using the
     * updated image source (image view B in the ViewPager) and target (image
     * view B in the grid).
     *
     * Each image in the PagedActivity uses a transitionName property equal to
     * "n" where n is the adapter position of the gallery item. The transition
     * name can then be converted to an Int position value and then used to
     * retrieve the ImageView from the view holder that maps the this adapter
     * position. The PagedActivityClient's RecyclerView adapter must implement
     * the PagedAdapterClient interface which handle's setting each view holder's
     * ImageView transition name to the its adapter position's String value.
     *
     * Note that if the use pages to an image that is not currently visible
     * in the calling activity's RecyclerView, a return shared element
     * transition will not be performed.
     */
    class PagedSharedElementCallback(val client: PagedActivityClient,
                                     val recyclerView: RecyclerView,
                                     val reenterState: Bundle)
        : SharedElementCallback(), KtLogger {
        companion object {
            val TAG = "PagedSharedElementCallback"
        }

        override fun onMapSharedElements(
                names: MutableList<String>?,
                sharedElements: MutableMap<String, View>?) {
            // We are only interested in a re-enter event which is signaled
            // by the mReenterState bundle containing the information we need
            // to handle setting up a new shared element for the return.
            val position = reenterState.getInt(PagedActivity.EXTRA_POSITION, -1)
            if (position == -1) {
                warn("$TAG: No adapter position was received for return transition")
                names!!.clear()
                sharedElements!!.clear()
                return
            }

            // Get the view holder so that we can access the actual image view.
            // If we are unable to get the view holder then the image view we
            // are looking for must not be visible, so cancel transition.
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
            if (viewHolder == null) {
                warn("$TAG: Image view for return transition is not visible")
                names!!.clear()
                sharedElements!!.clear()
                return
            }

            // Since this gallery activity supports multiple adapters we need
            // to find the ImageView that matches return transition name.
            val transitionName = position.toString()
            val imageView = viewHolder.itemView.findImageViewWithTransitionName(transitionName)
            if (imageView != null) {
                val exitName = ViewCompat.getTransitionName(imageView)!!

                // Add the exiting shared element name if not already added.
                if (!names!!.contains(exitName)) {
                    names.add(exitName)
                }

                // Add or update the associated shared element view.
                sharedElements!!.put(exitName, imageView)
            } else {
                warn("$TAG: Unable to locate return shared element image view")
                names!!.clear()
                sharedElements!!.clear()
            }

            // Now that the return transition values have been set, remove this
            // handler so that it won't be run when the client starts a different
            // activity.
            client.uninstallEnterTransitionHandler(client as Activity)
        }
    }
}
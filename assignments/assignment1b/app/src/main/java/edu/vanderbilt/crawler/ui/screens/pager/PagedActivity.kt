package edu.vanderbilt.crawler.ui.screens.pager

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.app.SharedElementCallback
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import edu.vanderbilt.crawler.R
import edu.vanderbilt.crawler.extensions.findImageViewWithTransitionName
import edu.vanderbilt.crawler.ui.views.ToolbarManager
import edu.vanderbilt.crawler.ui.views.ZoomOutPageTransformer
import edu.vanderbilt.crawler.utils.KtLogger
import edu.vanderbilt.crawler.utils.Preconditions
import edu.vanderbilt.crawler.utils.debug
import edu.vanderbilt.crawler.utils.warn
import kotlinx.android.synthetic.main.activity_paged.*
import org.jetbrains.anko.find
import java.util.*

/**
 * A generic activity that contains a set of PagedFragment objects and
 * supports a shared element image transition with the parent activity.
 *
 * The PagedActivity and associated components can be added to any
 * Android application that has an activity that contains a
 * RecyclerView that displays items containing images that have
 * associated uri values. When the PageActivity is started by
 * clicking a RecyclerView item, the item's image will be used in
 * a shared element transition and that PagerActivity will initially
 * display a full view of the clicked image. The user can then swipe
 * to display any of the other images that existed in the RecyclerView
 * adapter when the PagedActivity was started.
 *
 * Note that that by design, the PagerActivity requires that the
 * RecyclerView adapter does not change the positions of items that
 * existed when the PagerActivity was started. If, however, the
 * RecyclerView adds new items to then end of its adapter list after
 * the PagerActivity has started, the PagerActivity will work as
 * expected although it won't show any of the added items.
 *
 * To install the PagerActivity in an application have the RecyclerView
 * activity implement the PagedActivityClient interface and the
 * RecyclerView adapter implement the PagedAdapterClient interface.
 * The AndroidManifest will also need to updated to include an entry
 * for this activity, and the application's resource will need to include
 * the two layout files [activity_paged.xml], [fragment_paged.xml], as well
 * as the two values files [dimens.xml] and [strings.xml].
 *
 * Here are the steps required to integrate the PagerActivity into an
 * exisiting Android project:
 *
 * STEP 1:
 *
 * Add the following entry to the application's manifest:
 *
 * ```
 *    <activity
 *         android:name=".ui.screens.pager.PagedActivity"
 *         android:label="@string/title_activity_details"
 *         android:theme="@style/AppTheme.NoActionBar"/>
 *
 * ```
 *
 * STEP 2:
 *
 * Implement the PagedActivityClient interface in your RecyclerView
 * Activity class. The following is an example of a PagedActivityClient
 * implementation:
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
 *
 *
 *     /**
 *      * OnSelectionLister hook called when an adapter item
 *      * has been clicked. Forward call the PagedActivityClient
 *      * helper function. If preferred, the startPagedActivity
 *      * helper method can be called from the PagedAdapterClient
 *      * as an alternative way to start the PagerActivity.
 *      */
 *     override fun onItemClick(view: View, position: Int) {
 *         // Start pager activity to view an image.
 *         startPagedActivity(view, position, imageAdapter)
 *     }

 * ```
 *
 * STEP 3:
 *
 * Implement the PagedAdapterClient interface in your RecyclerView
 * Adapter class. The following is an example of a PagedAdapterClient
 * implementation:
 *
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
 *
 * STEP 4:
 *
 * Add the PagedActivity resources to the project resource directory:
 *
 * ```
 *     a) Add activity_paged.xml and fragment_paged.xml to the res/layout directory.
 *     b) Add or merge dimens.xml and strings.xml into the res/values directory.
 * ```
 *
 * These 4 steps are all that are required to have a fully functional details activity
 * view with full transition animation support.
 */
class PagedActivity :
        AppCompatActivity(),
        PagedFragment.OnPagedFragmentCallback,
        ToolbarManager,
        KtLogger {
    companion object {
        /**
         * Debug logging tag.
         */
        private val TAG = "PagedActivity"
        /**
         * String key names used for intent extras.
         */
        internal val EXTRA_POSITION = "Position"
        private val EXTRA_ITEMS = "Items"
        private val EXTRA_FRAGMENT_CLASS = "FragmentClass"

        /**
         * Factory method that can be called to construct an intent to start this
         * activity.
         *
         * @param context  An activity context.
         * @param items    The list of items can be swiped/viewed.
         * @param position The index of the initial item to display.
         * @return An intent that can be used to start this activity.
         */
        fun makeIntent(
                context: Context,
                items: List<Uri>,
                position: Int,
                fragment: Class<out PagedFragment> = PagedFragment::class.java): Intent {
            val intent = Intent(context, PagedActivity::class.java)
            intent.putParcelableArrayListExtra(EXTRA_ITEMS, ArrayList(items))
            intent.putExtra(EXTRA_POSITION, position)
            intent.putExtra(EXTRA_FRAGMENT_CLASS, fragment)
            return intent
        }
    }

    /**
     * The adapter used for the view pager containing that contains fragments.
     */
    private var pagerAdapter: FragmentPagerAdapter? = null

    /**
     * The list urls that can be swiped to display.
     */
    private var uris: List<Uri>? = null

    /**
     * The adapter position of the initially displayed item.
     */
    private var position: Int = 0

    /**
     * The transition name of a shared element (image view) when this activity
     * first exits.
     */
    private var exitTransition: Boolean = false

    /**
     * Class to use in the view pager.
     */
    private var fragmentClass: Class<*>? = null

    /**
     * Required override for ToolbarManager interface.
     */
    override val toolbar by lazy { find<Toolbar>(R.id.toolbar) }

    var title: String
        get() = toolbar.title.toString()
        set(value) {
            toolbar.title = value
        }

    /**
     * Hook method called when a new instance of Activity is created. One time
     * initialization code goes here, e.g., UI layout initialization.
     *
     * @param savedInstanceState A Bundle object that contains saved state
     * information.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Always call the superclass onCreate().
        super.onCreate(savedInstanceState)

        // Load the view from the XML layout.
        setContentView(R.layout.activity_paged)

        // This activity uses a CoordinatorLayout with a custom Toolbar.
        initToolbar()
        setSupportActionBar(toolbar)

        // Initialize fields from the starting intent extras.
        val intent = intent
        uris = intent.getParcelableArrayListExtra(EXTRA_ITEMS)
        position = intent.getIntExtra(EXTRA_POSITION, 0)
        fragmentClass = intent.getSerializableExtra(EXTRA_FRAGMENT_CLASS) as Class<*>
        val serializableExtra = intent.getSerializableExtra(EXTRA_FRAGMENT_CLASS)
        fragmentClass = serializableExtra as Class<*>

        // Initialize all layout views.
        initializeViews()

        if (position >= 0) {
            // Postpone the shared element transition until the current
            // fragment has loaded the shared element target image view
            // and the view has been fully laid out.
            debug("$TAG: Postponing shared element transition...")
            supportPostponeEnterTransition()
            installExitTransitionHandler()
        }
    }

    /**
     * Initializes view pager adapter and view pager.
     */
    private fun initializeViews() {
        // Create the adapter.
        pagerAdapter = FragmentPagerAdapter(supportFragmentManager)

        // Create and initialize the view pager.
        assert(viewPager != null)
        viewPager!!.adapter = pagerAdapter
        viewPager.setPageTransformer(true, ZoomOutPageTransformer())

        // Always notify adapter when contents is changed.
        pagerAdapter!!.notifyDataSetChanged()

        // Won't set the title for the initially loaded fragment because
        // the pagerAdapter calls onPage selected before the pager adapter
        // has fully created the fragments. The only place to be sure that
        // the fragment has been created is when the shared element is
        // being started in the preDrawListener (so the title is set there).
        viewPager.addOnPageChangeListener(object: ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                title = pagerAdapter?.getPageTitle(position).toString()
            }
        })
    }

    /**
     * Activity lifecycle hook method called when an activity is being started.
     */
    override fun onStart() {
        super.onStart()

        // Set the current page to image position passed by the activity.
        viewPager!!.currentItem = position
    }

    /**
     * Hook method called when the user clicks the system back button. Sets a
     * result intent that will be received by the activity's the result intent
     * that will be received in private void onMapSharedElementsForExit(
     */
    override fun onBackPressed() {
        // Set the activity result to an intent that contains the transition
        // name of the currently displayed image view.
        val position = viewPager!!.currentItem
        val intent = Intent()
        intent.putExtra(EXTRA_POSITION, position)
        setResult(Activity.RESULT_OK, intent)

        // Set flag for shared element transition callback which
        // will update the return shared element to reflect which
        // ever image the user has paged to.
        exitTransition = true
        super.onBackPressed()
    }

    /**
     * Hook method called by adapter to notify that the background image loading
     * operation has completed. Now that the ImageView contains a valid image,
     * we schedule a postponed shared element transition using an
     * OnPreDrawListener that will wait until the image view has been fully laid
     * out before starting the postponed shared element transition.
     *
     * @param view    The Image view that was just loaded.
     * @param success true if the download image was set; `false` if not.
     */
    override fun onSharedElementReady(view: ImageView, success: Boolean) {
        // This method will be called by each fragment once its image has been
        // loaded. Fragments are required to set their image view transition
        // names.
        try {
            val position = Integer.valueOf(ViewCompat.getTransitionName(view)!!)
            if (position == this.position) {
                if (success) {
                    debug("$TAG: scheduling a startPostponedEnterTransition ...")
                    scheduleStartPostponedEnterTransition(view)
                } else {
                    debug("$TAG: Shared element load failed; starting transition")
                    supportStartPostponedEnterTransition()
                }
            }
        } catch (e: Exception) {
            warn("$TAG: Shared element view transition name not set properly")
        }

    }

    /**
     * Schedules a call to supportStartPostponedEnterTransition() to be run once
     * the target view has been fully laid out.
     *
     * @param observerView shared element target view
     */
    private fun scheduleStartPostponedEnterTransition(observerView: View?) {
        observerView?.doOnPreDraw {
            debug("$TAG: Starting the postponed transition ...")
            supportStartPostponedEnterTransition()
            val position = viewPager!!.currentItem
            title = pagerAdapter?.getPageTitle(position).toString()
        }
    }

    /*
     * Animation support helpers
     */

    /**
     * Install a shared element callback that will be used to update the shared
     * element map with the movie id and poster image view of whatever movie is
     * currently being displayed in the PagerActivity when this activity finishes.
     */
    private fun installExitTransitionHandler() {
        // This callback occurs for the the enter and exit transitions
        // for this activity. We are only interested in modifying the
        // mapped shared element to the the poster image of the currently
        // displayed movie in the ViewPager.
        ActivityCompat.setEnterSharedElementCallback(this, object : SharedElementCallback() {
            override fun onMapSharedElements(
                    names: MutableList<String>?,
                    sharedElements: MutableMap<String, View>?) {
                onMapSharedElementsForExit(names, sharedElements)
            }
        })
    }

    /**
     * Hook method called by the shared element support framework when the
     * current activity is being entered or exited. When the activity is being
     * exited, whe update the shared element name and view map to reflect the
     * view and name of the last displayed image view so that it will be used in
     * the shared element transition animation rather than the original item
     * that was used when this activity was first started.
     *
     * @param names          The list of shared element names.
     * @param sharedElements The list of shared element views.
     */
    private fun onMapSharedElementsForExit(
            names: MutableList<String>?, sharedElements: MutableMap<String, View>?) {
        // Only interested in handling an exit transition.
        if (!exitTransition) {
            return
        }

        // Can't really do anything when framework doesn't send
        // parameters to update. This happens occasionally for
        // some unknown reason.
        if (names == null || sharedElements == null) {
            error("$TAG: Framework passed null name list or " +
                  "sharedElement map to sharedElementCallback!")
        }

        val position = viewPager!!.currentItem
        val fragment = pagerAdapter!!.getItem(position)

        // The fragment view will unfortunately be null if an orientation change
        // occurred while viewing a paged image. When this occurs, we will just
        // abandon the attempt to do a return shared element transition.
        if (fragment == null || fragment.view == null) {
            names.clear()
            sharedElements.clear()
            return
        }

        // Shared element transition requires a layout with an image view with
        // id "R.id.image_view". This should be rethought...
        val transitionName = position.toString()
        val imageView = fragment.view!!.findImageViewWithTransitionName(transitionName)
        if (imageView == null) {
            warn("$TAG: Unable to locate image view for shared element transition")
            names.clear()
            sharedElements.clear()
            return
        }

        // Get enter and exit transition names.
        val enterName = this.position.toString()
        val exitName = ViewCompat.getTransitionName(imageView)

        if (exitName != null) {
            // Check if the exit shared element is different
            // from the original enter shared element.
            // If for some reason the names list is empty then
            // we want to add the exit name and view. Or, if
            // the enter and exit names differ then we want to
            // replace the replace the enter name and view with
            // the exit name and view.
            if (names.isEmpty() || enterName != exitName) {
                // The exit shared element not the same as the
                // enter shared element so update the name list
                // new name of the new shared element.
                val index = names.indexOf(enterName)
                if (index == -1) {
                    // Abnormal case: add the exit name
                    if (!names.contains(exitName)) {
                        names.add(exitName)
                    }
                    warn("$TAG: Enter name missing ... adding exit name $exitName anyway.")
                } else {
                    // Normal case: replace enter name with exit name.
                    names[index] = exitName
                }

                sharedElements.remove(enterName)
                sharedElements.put(exitName, imageView)
            }
        } else {
            warn("$TAG: Unable to locate return shared element image view")
            names.clear()
            sharedElements.clear()
        }
    }

    /**
     * Static subclass FragmentStatePagerAdapter implementation that is suitable
     * to support swiping between a potentially large number of dynamically
     * created fragment pages.
     */
    private inner class FragmentPagerAdapter(fm: FragmentManager)
        : FragmentStatePagerAdapter(fm) {

        /**
         * Maintain in local fragment map since the default implementation does
         * not handle this properly.
         */
        @SuppressLint("UseSparseArrays")
        internal val fragmentMap = HashMap<Int, PagedFragment>()

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentMap[position]?.title ?: ""
        }

        /**
         * Method called to either return an existing page instance or to
         * construct a new one if not already cached.
         *
         * @param position The adapter position of the page to get.
         * @return Returns a new or cached fragment.
         */
        override fun getItem(position: Int): Fragment? {
            var fragment: PagedFragment? = fragmentMap[position]
            if (fragment != null) {
                return fragment
            }

            try {
                fragment = Preconditions.checkNotNull(
                        fragmentClass!!.newInstance() as PagedFragment)
                val args = Bundle()
                args.putParcelable(
                        PagedFragment.ARG_RESOURCE_URI, uris!![position])
                args.putInt(PagedFragment.ARG_POSITION, position)
                fragment.arguments = args
            } catch (e: InstantiationException) {
                e.printStackTrace()
                return null
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
                return null
            }

            // Set the url for this fragment to display.
            fragment.setUrl(uris!![position])

            // Keep track of fragments.
            fragmentMap.put(position, fragment)

            return fragment
        }

        override fun destroyItem(
                container: ViewGroup,
                position: Int,
                `object`: Any) {
            super.destroyItem(container, position, `object`)
            fragmentMap.remove(position)
        }

        /**
         * Returns the count of items in this pager adapter.
         *
         * @return Number of pageable fragments.
         */
        override fun getCount(): Int {
            return uris!!.size
        }
    }
}

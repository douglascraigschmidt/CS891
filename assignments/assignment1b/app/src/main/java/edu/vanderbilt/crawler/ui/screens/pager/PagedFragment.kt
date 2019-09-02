package edu.vanderbilt.crawler.ui.screens.pager

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import edu.vanderbilt.crawler.R
import edu.vanderbilt.crawler.extensions.asyncLoad
import edu.vanderbilt.crawler.utils.UriUtils
import kotlinx.android.synthetic.main.fragment_paged.*
import java.io.File
import java.net.URLDecoder

/**
 * A generic details fragment whose main data element is a resource uri. This
 * class along with the parent PagedActivity provide shared element transition
 * animation for a single image.
 *
 * [Fragment] subclass. Use the [PagedFragment.newInstance] factory
 * method to create an instance of this fragment.
 */
/**
 * Required empty public constructor for FragmentManager reconstruction.
 */
class PagedFragment : Fragment() {
    companion object {
        /**
         * Key string names used by newInstance() to store parameters in a bundle
         * that can be then accessed from the fragment once it has been created.
         */
        val ARG_RESOURCE_URI = "resourceUri"
        val ARG_POSITION = "position"

        /**
         * Use this factory method to create a new instance of this fragment using
         * the provided parameters.
         *
         * @param uri URL of image to display
         * @return A new instance of fragment PagedFragment.
         */
        fun newInstance(uri: Uri, position: Int): PagedFragment {
            val fragment = PagedFragment()
            val args = Bundle()
            args.putParcelable(ARG_RESOURCE_URI, uri)
            args.putInt(ARG_POSITION, position)
            fragment.arguments = args
            return fragment
        }
    }

    /**
     * The data source uri to display in this fragment. This uri will depend on
     * the application context. The default implementation assumes that this uri
     * is to an image resource that needs to be downloaded.
     */
    private lateinit var uri: Uri

    /**
     * The position of this paged fragment in the parent activities adapter.
     */
    private var position: Int = 0

    /**
     * A listen set by the calling activity so that it will be notified when the
     * initial background thread load image operation has completed and the
     * target image view has been updated with the resulting bitmap. Once this
     * has happened any postponed shared element transition can be started now
     * that the view is ready to animate.
     */
    lateinit private var mPagedFragmentListener: OnPagedFragmentCallback

    /**
     * Returns the default title which is the last path component of the source uri.
     *
     * Default implementation forwards the set title to request to the activity
     * to handle. Custom fragments can override to display the title in some
     * other way.
     *
     * NOTE: The title implementation has been customized for the Crawler application
     * that has uri's with a "Transform" prefix.
     *
     * @return The fragment title.
     */
    var title: String
        get() {
            // CRAWLER APP CUSTOM CODE:
            val decodedUri = URLDecoder.decode(UriUtils.getLastPathSegmentBaseName(uri), "UTF-8")
            val transform = decodedUri.substringBefore('-')
            val suffix =
                    if (transform.compareTo("__notag__", ignoreCase = true) == 0) {
                        "original"
                    } else {
                        transform.replace("Transform", "", ignoreCase = true)
                    }

            val prefix = File(decodedUri).name
            val string = "$prefix ($suffix)"
            return if (!string.isBlank()) string else getString(R.string.no_title)

            // ORIGINAL GENERIC CODE:
            // val title = UriUtils.getLastPathSegmentBaseName(uri)
            // return if (!title.isBlank()) title else getString(R.string.no_title)
        }

        set(string) {
            activity?.title = string
        }

    /**
     * Called when a fragment is first attached to its context. [ ][.onCreate] will be called after this.
     *
     * @param context The Activity context.
     */
    override fun onAttach(context: Context?) {
        if (context is OnPagedFragmentCallback) {
            mPagedFragmentListener = context
        }
        super.onAttach(context)
    }

    /**
     * Hook method called when a new instance of Activity is created. One time
     * initialization code goes here, e.g., UI layout initialization.
     *
     * @param savedInstanceState A Bundle object that contains saved state
     * information.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Un-bundle arguments and save as members.
        arguments?.apply {
            uri = getParcelable(ARG_RESOURCE_URI)!!
            position = getInt(ARG_POSITION, -1)
        }
    }

    /**
     * Called after onCreate() to create the fragment view.
     * NOTE: Koltinx fragment widgets are null until until onViewCreated()!
     *
     *
     * @param inflater           Inflater used to inflate the
     * fragment XML layout.
     * @param container          The parent container.
     * @param savedInstanceState A previously saved state that can be used to
     * retrieve and restore the fragment state.
     * @return The inflated top level view.
     */
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_paged, container, false)

        // Set the default title.
        //title = title

        return view
    }

    /**
     * Since Kotlinx widgets aren't available in onCreateView(),
     * the shared element transition is started here now that
     * the Kotlinx ImageView has been realized.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (pagedImageView == null) {
            throw AssertionError()
        }

        // Load the default image.
        loadImage(uri)
    }

    /**
     * Loads the specified [uri] in to the image view.
     *
     * @param uri The image URL to load (may be local or remote).
     */
    private fun loadImage(uri: Uri) {
        // Asynchronously load the bitmap.
        pagedImageView?.apply {
            ViewCompat.setTransitionName(this, position.toString())
            asyncLoad(uri.toString()) {
                if (it) {
                    mPagedFragmentListener.onSharedElementReady(this, true)
                } else {
                    mPagedFragmentListener.onSharedElementReady(this, false)
                }
            }
        }
    }

    /**
     * Image load callbacks are optional and can be installed using this
     * method.
     *
     * @param listener An OnPagedFragmentCallback implementation.
     */
    fun setOnPagedFragmentCallback(listener: OnPagedFragmentCallback) {
        mPagedFragmentListener = listener
    }

    /**
     * Called by pager adapter once this fragment has been constructed to set
     * the uri to display within this fragment.
     *
     * @param uri A uri string.
     */
    fun setUrl(uri: Uri) {
        this.uri = uri
    }

    /**
     * Listener callback interface to inform when an image load operation was
     * successful or failed.
     */
    interface OnPagedFragmentCallback {
        fun onSharedElementReady(view: ImageView, success: Boolean)
    }
}


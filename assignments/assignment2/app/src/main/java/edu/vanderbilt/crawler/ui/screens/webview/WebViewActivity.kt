package edu.vanderbilt.crawler.ui.screens.webview

import android.annotation.SuppressLint
import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Patterns
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NavUtils
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import edu.vanderbilt.crawler.R
import edu.vanderbilt.crawler.adapters.MultiSelectAdapter
import edu.vanderbilt.crawler.extensions.animateScale
import edu.vanderbilt.crawler.extensions.asyncFetchImage
import edu.vanderbilt.crawler.extensions.hideKeyboard
import edu.vanderbilt.crawler.extensions.postDelayed
import edu.vanderbilt.crawler.utils.KtLogger
import kotlinx.android.synthetic.main.activity_web_view.*
import org.jetbrains.anko.contentView
import org.jetbrains.anko.dimen
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import java.net.URI
import java.net.URLEncoder

/**
 * A generic visual URL picker that uses a web view and a recycler
 * view bottom sheet to allow the user to pick a set of urls.
 */
class WebViewActivity : AppCompatActivity(), KtLogger {
    companion object {
        val KEY_URL = "currentUrl"
        val KEY_PICK_URLS = "pickUrls"
        val KEY_MAX_URLS = "maxUrls"
        private val KEY_IMAGE_PICKER = "image_picker"
        private val KEY_SEARCH_VIEW_ICONIFIED = "search_view_iconified"

        private val GOOGLE_SEARCH_URL = "https://www.google.com/search?q="

        fun startUrlPickerForResult(activity: Activity,
                                    startUrl: String? = activity.getString(R.string.default_web_view),
                                    pickUrls: List<String>? = null,
                                    imagePicker: Boolean = false,
                                    maxUrls: Int = -1, // unlimited
                                    resultCode: Int) {
            with(Intent(activity, WebViewActivity::class.java)) {
                putExtra(KEY_URL, startUrl)
                putStringArrayListExtra(KEY_PICK_URLS, ArrayList<String>(pickUrls ?: arrayListOf()))
                /* pickUrls?.let { putStringArrayListExtra(KEY_PICK_URLS, ArrayList<String>(pickUrls)) } */
                putExtra(KEY_IMAGE_PICKER, imagePicker)
                putExtra(KEY_MAX_URLS, maxUrls)

                activity.startActivityForResult(this, resultCode)
            }
        }
    }

    // Framework resources can't be accessed by Anko bindings.
    private val searchEditText: EditText by lazy {
        searchView.find<EditText>(R.id.search_src_text)
    }

    // Mutable properties first initialized from, but also
    // loaded from savedInstanceState (config change).
    private lateinit var url: String
    private lateinit var urls: MutableList<String>

    // Immutable properties that are only initialized
    // from passed intent.
    internal val imagePicker: Boolean by lazy {
        intent.getBooleanExtra(KEY_IMAGE_PICKER, false)
    }
    private val maxUrls: Int by lazy {
        intent.getIntExtra(KEY_MAX_URLS, -1)
    }

    val webViewFragment: WebViewUrlFragment by lazy {
        fragment as WebViewUrlFragment
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        setSupportActionBar(webViewToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        // @Doug: is this the coolest thing ever?
        with(savedInstanceState ?: intent.extras) {
            url = getString(KEY_URL) ?: getString(R.string.default_web_view)
            if (imagePicker) {
                urls = getStringArrayList(KEY_PICK_URLS)
            }
        }

        initializeSearchView(searchView)

        with(webView) {
            settings.javaScriptEnabled = true
            webViewClient = WebViewCallback()
        }

        if (imagePicker) {
            initializeImagePicker()
        } else {
            initializeUrlPicker()
        }

        copyFab.setOnClickListener {
            copyFab.animateScale(false) {
                finishPicker()
            }
        }

        // Set FAB scaled size to 0 so that anmateFab extension
        // will grow it into view in onResume().
        copyFab.isVisible = true
        copyFab.scaleX = 0f
        copyFab.scaleY = 0f

        webView.loadUrl(url)

        updateViews()
    }

    override fun onResume() {
        super.onResume()
        // Animate Fab into view if not already visible.
        updateFabPosition()
        if (!copyFab.isShown) {
            postDelayed(1000) {
                copyFab.animateScale(true)
            }
        }
    }

    /**
     * Sets up listeners for the search view to handle coordinating
     * between the normal toolbar title view and the special search
     * view input widget. Also forwards any user entered query to the
     * web view.
     */
    private fun initializeSearchView(searchView: SearchView) {
        searchEditText.setSelectAllOnFocus(true)
        webViewFragment.adapter.onSelectionListener =
                object : MultiSelectAdapter.OnSelectionListener {
                    override fun onActionModeStarting(): Boolean {
                        // Only allow selection if this is an image picker.
                        return imagePicker
                    }

                    override fun onActionModeFinished() {
                        updateViews()
                    }
                }

        with(searchView) {
            // Force search icon to the right and as wide as possible.
            layoutParams = Toolbar.LayoutParams(Gravity.END)
            maxWidth = Integer.MAX_VALUE

            // Show the app bar title and home button when the search view closes.
            setOnCloseListener {
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.setDisplayShowTitleEnabled(true)
                false
            }

            // Listen for search button click to detect when view is being
            // expanded. When it is, set URL, select it and hide home button.
            setOnSearchClickListener {
                setQuery(url, false)
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                supportActionBar?.setDisplayShowTitleEnabled(false)
                searchEditText.setSelection(searchEditText.length(), 0)
            }

            val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            setIconifiedByDefault(true)
            queryHint = getString(R.string.search_view_hint)

            // Listen for search view query submit to forward the url to the WebView.
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    if (!query.isBlank()) {
                        webView.loadUrl(queryToUrl(query))
                    }

                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    return false
                }
            })
        }
    }

    /**
     * Image picker configuration supports long-click to add and image
     * to a bottom sheet recycler view.
     */
    private fun initializeImagePicker() {
        throw AssertionError("Not currently setup to handle new bottom sheet peek logic.")

        @Suppress("UNREACHABLE_CODE")
        webView.setOnLongClickListener {
            val hitTestResult = (it as WebView).hitTestResult
            // Asynchronously checks that the url points to an image
            // before adding it to the image list.
            addImageUrl(hitTestResult.extra)
            false
        }

        @Suppress("UNREACHABLE_CODE")
        with(BottomSheetBehavior.from(bottomSheet)) {
            isHideable = true
            peekHeight = dimen(R.dimen.url_list_peek_height)
            state = STATE_COLLAPSED
            //TODO(monte) handle automatic sizing of image picker list
            setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == STATE_HIDDEN) {
                        state = STATE_COLLAPSED
                    }
                    updateFabPosition()
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    updateFabPosition()
                }
            })
        }
    }

    /**
     * Image picker configuration supports long-click to add and image
     * to a bottom sheet recycler view.
     */
    private fun initializeUrlPicker() {
        with(BottomSheetBehavior.from(bottomSheet)) {
            isHideable = true
            state = STATE_HIDDEN
            setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == STATE_HIDDEN) {
//                        state = STATE_COLLAPSED
//                        peekHeight = dimen(R.dimen.min_peek_height)
                    }

                    updateFabPosition()
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    updateFabPosition()
//                    peekHeight = if (slideOffset < 0) {
//                        dimen(R.dimen.min_peek_height)
//                    } else {
//                        dimen(R.dimen.url_list_peek_height)
//                    }
                }
            })
        }
    }

    private fun updateFabPosition() {
        contentView?.apply {
            val dist = bottom - bottomSheet.top
            copyFab?.translationY = Math.min(-dist.toFloat(), 0f)
        }
    }

    /**
     * Asynchronously adds an image url to the url list only
     * if the url actually points to a valid web image object.
     */
    private fun addImageUrl(url: String?) {
        if (url == null) {
            toast(R.string.selected_item_not_image_url)
        } else with(webViewFragment) {
            // Async fetch call: only add the URL if really is an image.
            asyncFetchImage(url) { isImage ->
                if (isImage) {
                    push(url, maxUrls)
                    updateViews()
                } else {
                    toast(R.string.selected_item_not_image_url)
                }
            }
        }
    }

    private fun updateViews() {
        webActivityHintView.visibility =
                if (webViewFragment.adapter.itemCount > 0)
                    View.GONE
                else
                    View.VISIBLE

        updateFabPosition()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_web_view, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (!webView.canGoBack()) {
                    NavUtils.navigateUpFromSameTask(this)
                } else {
                    webView.goBack()
                }
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * Save and restore the current url when a configuration change occurs.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        with(outState) {
            putString(KEY_URL, webView.url)
            putStringArrayList(KEY_PICK_URLS, ArrayList<String>(webViewFragment.urls))
            putBoolean(KEY_SEARCH_VIEW_ICONIFIED, searchView.isIconified)
        }
        super.onSaveInstanceState(outState)
    }

    /**
     * Formats the passed query string to a url for the WebView to load.
     * Web url strings without an authority will have it prepended
     * (eg "amazon.com" will be changed to "https://amazon.com") and search
     * words or phrases will be changed to a google search query
     * (eg cats will be changed to "https://www.google.com/search?q=cats".
     */
    private fun queryToUrl(query: String): String {
        return if (Patterns.WEB_URL.matcher(query).matches()) {
            // Search query is likely a url, so add authority if missing.
            with(URI.create(query)) {
                if (isAbsolute && authority.isNullOrBlank()) {
                    "https://$query"
                } else {
                    query
                }
            }
        } else {
            // Search query is likely a query so convert to a google search url.
            try {
                GOOGLE_SEARCH_URL + URLEncoder.encode(query, "UTF-8")
            } catch (e: Exception) {
                toast("Unable to encode search string")
                return query
            }
        }
    }

    /**
     * Updates the current url property and displays it as the AppBar title.
     * If the search view is expanded, it's iconified and the keyboards is
     * hidden.
     */
    private fun setUrlAndShowTitle(newUrl: String) {
        url = newUrl
        supportActionBar?.title = url

        if (!searchView.isIconified) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowTitleEnabled(true)
            searchView.setQuery(null, false)
            searchView.isIconified = true
        }

        hideKeyboard()

        if (!imagePicker) {
            webViewFragment.push(url, 1)
            updateViews()
        }
    }

    /**
     * Sets the return intent and finishes this activity.
     */
    private fun finishPicker() {
        with(Intent()) {
            if (imagePicker) {
                putStringArrayListExtra(
                        KEY_PICK_URLS,
                        ArrayList<String>(webViewFragment.urls))
            }
            putExtra(KEY_URL, url)
            setResult(Activity.RESULT_OK, this)
        }
        finish()
    }

    /**
     * Standard WebView client used to intercept page loading actions.
     */
    private inner class WebViewCallback : WebViewClient() {
        /**
         * Capture the new url and show it as the app bar title.
         */
        override fun onPageStarted(view: WebView, newUrl: String, favicon: Bitmap?) {
            setUrlAndShowTitle(newUrl)
            super.onPageStarted(view, url, favicon)
        }
    }
}

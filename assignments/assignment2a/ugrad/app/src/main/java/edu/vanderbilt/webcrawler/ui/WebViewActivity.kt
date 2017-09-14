package edu.vanderbilt.webcrawler.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.util.Patterns
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import edu.vanderbilt.webcrawler.R
import edu.vanderbilt.webcrawler.extensions.fetchImage
import edu.vanderbilt.webcrawler.extensions.hideKeyboard
import kotlinx.android.synthetic.main.activity_web_view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.dimen
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import java.net.URI
import java.net.URLEncoder

/**
 * A generic visual URL picker that uses a web view and a recycler
 * view bottom sheet to allow the user to pick a set of urls.
 */
class WebViewActivity : AppCompatActivity(), AnkoLogger {
    companion object {
        val KEY_URL_LIST = "startUrl"
        val KEY_PICK_URLS = "pickUrls"
        val KEY_MAX_URLS = "maxUrls"
        private val KEY_IMAGE_PICKER = "image_picker"
        private val KEY_SEARCH_VIEW_ICONIFIED = "search_view_iconified"

        private val GOOGLE_SEARCH_URL = "https://www.google.com/search?q="

        fun startUrlPickerForResult(activity: Activity,
                                    startUrl: String? = activity.getString(R.string.default_web_view),
                                    pickUrls: List<String>? = null,
                                    imagePicker: Boolean = false,
                                    maxUrls: Int = 1,
                                    resultCode: Int) {
            with(Intent(activity, WebViewActivity::class.java)) {
                putExtra(KEY_URL_LIST, startUrl)
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
    private val imagePicker: Boolean by lazy {
        intent.getBooleanExtra(KEY_IMAGE_PICKER, false)
    }
    private val maxUrls: Int by lazy {
        intent.getIntExtra(KEY_MAX_URLS, 1)
    }

    val webViewFragment: WebViewUrlFragment by lazy {
        fragment as WebViewUrlFragment
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        // @Doug: is this the coolest thing ever?
        with(savedInstanceState ?: intent.extras) {
            url = getString(KEY_URL_LIST)
            urls = getStringArrayList(KEY_PICK_URLS)
        }

        initializeSearchView(searchView)

        with(webView) {
            settings.javaScriptEnabled = true
            setWebViewClient(WebViewCallback())
        }

        if (imagePicker) {
            initializeImagePicker()
        }

        copyFab.setOnClickListener { finishPicker() }

        webView.loadUrl(url)
    }

    /**
     * Sets up listeners for the search view to handle coordinating
     * between the normal toolbar title view and the special search
     * view input widget. Also forwards any user entered query to the
     * web view.
     */
    private fun initializeSearchView(searchView: SearchView) {
        searchEditText.setSelectAllOnFocus(true)
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
        webView.setOnLongClickListener {
            val hitTestResult = (it as WebView).hitTestResult
            // Asynchronously checks that the url points to an image
            // before adding it to the image list.
            addImageUrl(hitTestResult.extra)

            false
        }

        with(BottomSheetBehavior.from(bottomSheet)) {
            isHideable = true
            peekHeight = dimen(R.dimen.url_list_peek_height)
            state = BottomSheetBehavior.STATE_COLLAPSED
            //TODO
            setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    if (slideOffset < 0) {
                    }
                }
            })
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
            // @Doug: This one's amazing right? It looks like it's
            // synchronous but its not! Check out ImageExt.kt
            // to see how it works.

            // Async fetch call: only add the URL if really is an image.
            fetchImage(url) { isImage ->
                if (isImage) {
                    push(url)
                } else {
                    toast(R.string.selected_item_not_image_url)
                }
            }
        }
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
        outState.putString(KEY_URL_LIST, webView.url)
        outState.putStringArrayList(KEY_PICK_URLS, ArrayList<String>(webViewFragment.urls))
        outState.putBoolean(KEY_SEARCH_VIEW_ICONIFIED, searchView.isIconified)
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
        }
    }

    /**
     * Sets the return intent and finishes this activity.
     */
    private fun finishPicker() {
        val intent = Intent()
        with(intent) {
            if (imagePicker) {
                putStringArrayListExtra(
                        KEY_PICK_URLS,
                        ArrayList<String>((webViewFragment).urls))
            } else {
                val arrayList = arrayListOf(url)
                putStringArrayListExtra(KEY_PICK_URLS, arrayList)
            }
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

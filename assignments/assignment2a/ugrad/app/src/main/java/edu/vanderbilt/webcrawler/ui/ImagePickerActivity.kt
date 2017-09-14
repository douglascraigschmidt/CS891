package edu.vanderbilt.webcrawler.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import edu.vanderbilt.webcrawler.R
import kotlinx.android.synthetic.main.activity_image_picker.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import java.io.UnsupportedEncodingException
import java.net.URI
import java.net.URLEncoder

class ImagePickerActivity : AppCompatActivity(), AnkoLogger {
    companion object {
        val KEY_URL = "url"
        val KEY_IMAGE_PICKER = "image_picker"
        private val TAG = "WebViewActivity"
        private val GOOGLE_SEARCH_URL = "https://www.google.com/search?q="

        fun startImagePickerForResult(activity: Activity, url: String, resultCode: Int) {
            val intent = Intent(activity, WebViewActivity::class.java)

            intent.putExtra(KEY_IMAGE_PICKER, true)
            intent.putExtra(KEY_URL, if (!TextUtils.isEmpty(url))
                url
            else
                activity.getString(R.string.default_web_view))

            activity.startActivityForResult(intent, resultCode)
        }
    }

    private var searchEditText: EditText =
            searchView.find<EditText>(R.id.search_src_text)
    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    private var url: String? = null

    private var imagePicker: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_picker)

        setSupportActionBar(toolbar)
        initializeSearchView(searchView)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        initializeViews()

        if (savedInstanceState == null) {
            url = intent.getStringExtra(KEY_URL)
            imagePicker = intent.getBooleanExtra(KEY_IMAGE_PICKER, false)
        } else {
            url = savedInstanceState.getString(KEY_URL)
        }

        webView.loadUrl(url)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeViews() {
        with(BottomSheetBehavior.from(webView)) {
            isHideable = true
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        fab.setOnClickListener { finishWithUrlResult() }

        webView.settings.javaScriptEnabled = true
        webView.setWebViewClient(WebViewCallback())

        if (imagePicker) {
            webView.setOnLongClickListener { v ->
                val hr = (v as WebView).hitTestResult
                Log.d(TAG, "Clicked On: " + hr.extra)
                false
            }
        }
    }

    private fun initializeSearchView(searchView: SearchView) {
        with (searchView) {
            searchEditText = searchView.findViewById(R.id.search_src_text) as EditText

            val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            setIconifiedByDefault(false)
            queryHint = getString(R.string.search_view_hint)
            searchEditText.setSelection(0)

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    debug("onQueryTextSubmit: $query")
                    if (query.isBlank()) {
                        return false
                    }

                    var finalQuery = query
                    if (Patterns.WEB_URL.matcher(query).matches()) {
                        // Search query is likely a url, so make sure
                        // that it's formatted as an absolute url before
                        // loading into the WebView.
                        with(URI.create(query)) {
                            if (isAbsolute && authority.isNullOrBlank()) {
                                finalQuery = "https://$query"
                            }
                        }
                    } else {
                        // Search query is likely a query so reformat the query
                        // to a Google search url before loading into WebView.
                        try {
                            finalQuery = GOOGLE_SEARCH_URL + URLEncoder.encode(query, "UTF-8")
                        } catch (e: UnsupportedEncodingException) {
                            toast("Unable to format search string")
                            return false
                        }
                    }

                    webView.loadUrl(finalQuery)
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    return false
                }
            })
        }
    }

    private fun finishWithUrlResult() {
        with (Intent()) {
            putExtra(KEY_URL, webView.url)
            setResult(Activity.RESULT_OK, this)
        }
        finish()
    }

    /**
     * Standard WebView client used to intercept page loading actions.
     */
    private inner class WebViewCallback : WebViewClient() {
        /**
         * Update the search view to display the current url.
         */
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {
            this@ImagePickerActivity.url = url
            with (searchView) {
                setQuery(url, false)
                isIconified = false
                setIconifiedByDefault(false)
            }
            searchEditText.setSelection(0)
            super.onPageStarted(view, url, favicon)
        }
    }
}

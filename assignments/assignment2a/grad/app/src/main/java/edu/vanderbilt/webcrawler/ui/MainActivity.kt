package edu.vanderbilt.webcrawler.ui

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.FileObserver
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import edu.vanderbilt.crawlers.framework.CrawlerFactory
import edu.vanderbilt.crawlers.framework.ImageCrawlerBase
import edu.vanderbilt.filters.FilterFactory
import edu.vanderbilt.platform.Device
import edu.vanderbilt.utils.CacheUtils
import edu.vanderbilt.utils.LocalPageCrawler
import edu.vanderbilt.utils.Options
import edu.vanderbilt.utils.WebPageCrawler
import edu.vanderbilt.webcrawler.R
import edu.vanderbilt.webcrawler.adapters.ImageViewAdapter
import edu.vanderbilt.webcrawler.extensions.set
import edu.vanderbilt.webcrawler.platform.AndroidPlatform
import edu.vanderbilt.webcrawler.utils.RecursiveFileObserver
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import java.io.File

class MainActivity : AppCompatActivity(), ToolbarManager, AnkoLogger {
    /**
     * Enumerated type that lists all the image crawling
     * implementation strategies to test.
     */

    companion object {
        val DEFAULT_CRAWL_DEPTH = 3
        val DEFAULT_LOCAL_CRAWL = false
        val DEFAULT_MAX_URLS = 1

        val KEY_URL = "rootUrl"
        val KEY_CRAWL_STRATEGY = "crawlStrategy"
        val KEY_FILTER_TYPE = "filterType"
        val KEY_IMAGE_URLS = "image_urls"
        val DEFAULT_PICKER_TYPE = PickerType.URL_PICKER

        enum class PickerType {
            IMAGE_PICKER,
            URL_PICKER
        }
    }

    override val toolbar by lazy { find<Toolbar>(R.id.toolbar) }
    private val adapter: ImageViewAdapter by lazy {
        recyclerView.adapter as ImageViewAdapter
    }

    /**
     * Options.
     */
    private var rootUrl: String? = null
    private var urls: List<String> = mutableListOf()
    private var crawlerRunning = false
    private var searchDrawable: Drawable? = null
    private var maxUrls = DEFAULT_MAX_URLS
    private var pickerType = DEFAULT_PICKER_TYPE

    /**
     * Must be accessible to MenuHandler.
     */
    internal var crawlStrategy: CrawlerFactory.Type = CrawlerFactory.Type.SEQUENTIAL_LOOPS
    internal var filterTypes = FilterFactory.Type.values().toMutableList()
    internal var localCrawl = DEFAULT_LOCAL_CRAWL
    internal var crawlDepth: Int = DEFAULT_CRAWL_DEPTH

    /**
     * Crawler support properties.
     */
    private var fileObserver: RecursiveFileObserver? = null
    private var crawler: ImageCrawlerBase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initToolbar()
        setSupportActionBar(toolbar)

        toolbarTitle = getString(R.string.app_name)
        rootUrl = if (savedInstanceState == null) {
            intent.getStringExtra(KEY_URL)
        } else {
            urls = savedInstanceState.getStringArrayList(KEY_IMAGE_URLS).toList()
            savedInstanceState.getString(KEY_URL)
        }

        // Setup all contained views.
        initializeViews()

        // Update all views to reflect the current state.
        updateViewStates()
    }

    private fun initializeViews() {
        // Since I can't figure out how to load the nice search icon dynamically
        // (it's an R.attr type of drawable), just save the original drawable
        // from the XML file and use it when necessary.
        searchDrawable = searchFab.drawable

        searchFab.setOnClickListener {
            when {
                crawlerRunning || crawler != null ->
                    stopCrawler()

                localCrawl -> crawlerRunning = runCrawler()

                else -> WebViewActivity.startUrlPickerForResult(this,
                        pickUrls = null,
                        imagePicker = pickerType == PickerType.IMAGE_PICKER,
                        maxUrls = maxUrls,
                        resultCode = pickerType.ordinal)
            }
        }

        with(recyclerView) {
            adapter = ImageViewAdapter(context,
                    noDups = true,
                    local = localCrawl,
                    list = urls.toMutableList())
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    /**
     * Sets up the Device singleton and options required
     * to run the crawler.
     */
    private fun initializeCrawler(): Boolean {
        stopCrawler()

        // FIX: Added as a temporary solution for recreating singleton.
        Device.setPrivateInstanceFieldToNullForUnitTestingOnly()

        // The Options class is effectively a platform unaware
        // immutable class and therefore we need to set the rootUrl
        // local crawls use the apps assets.
        if (localCrawl) {
            val webUri = Uri.parse(Options.DEFAULT_WEB_URL)
            rootUrl = Uri.parse(AndroidPlatform.ASSETS_URI_PREFIX
                    .plus("/")
                    .plus(webUri.host)
                    .plus(webUri.path)
                    .plus("/index.html"))
                    .toString()
        } else if (rootUrl.isNullOrBlank()) {
            toast("Please click the search button to pick a root URL to crawl.")
            return false
        }

        Device.newBuilder()
                .platform(AndroidPlatform())
                .options(Options.newBuilder()
                        .local(localCrawl)
                        .rootUrl(rootUrl)
                        .maxDepth(crawlDepth)
                        .diagnosticsEnabled(true)
                        .build())
                .crawler(if (localCrawl) LocalPageCrawler() else WebPageCrawler())
                .build()

        return true
    }

    /**
     * Called when the crawler finishes to resets states and updates views.
     */
    private fun crawlerFinished() {
        crawler = null
        crawlerRunning = false
        fileObserver?.stopWatching()
        fileObserver = null

        // Tell adapter to clear all downloading animations.
        adapter.crawlStopped()

        updateViewStates()
    }

    /**
     * Sets a singleton flag to stop the crawler and terminates
     * the recursive file cache directory observer.
     */
    private fun stopCrawler() {
        // Stop watching the cache.
        fileObserver?.stopWatching()

        if (crawler != null) {
            Device.stopCrawl(true)
            // Tell adapter to clear all downloading animations.
            adapter.crawlStopped()
        }
    }

    /**
     * Cancels and running crawl and starts a new one.
     */
    private fun runCrawler(): Boolean {
        // Stop watching the cache and clear recycler view.
        fileObserver?.stopWatching()
        adapter.clear()

        // Stops current crawl and prepares for a new one.
        if (!initializeCrawler()) {
            return false
        }

        // Delete any the filtered images from the previous run.
        if (Device.instance() != null) {
            CacheUtils.clearCache()
        }

        // Make sure the cache directory exists or else the file tree
        // observer won't work.
        CacheUtils.getCacheDir()?.mkdirs()

        fileObserver = RecursiveFileObserver(CacheUtils.getCacheDirPath(),
                RecursiveFileObserver.EventListener { event, file ->
                    onCacheChangedEvent(event, file)
                })
        fileObserver?.startWatching()

        // Create list of new Filter instances matching the chosen list.

        // Pick the appropriate crawl strategy.
        crawler = CrawlerFactory.newCrawler(crawlStrategy, filterTypes, rootUrl)

        // Make an ImageCrawlerAsync object via the factory method.
        // Start running the test.
        crawlerRunning = true
        doAsync {
            crawler?.run()
            uiThread {
                crawlerFinished()
                if (adapter.itemCount == 0) {
                    warn("No images were added to the image adapter!")
                }
            }
        }

        updateViewStates()

        return true
    }

    //
    // data/user/0/edu.vanderbilt.webcrawler/files/downloaded-images/GrayScaleFilter/www.dre.vanderbilt.edu/~schmidt/imgs/dougs_small.jpg
    //
    /**
     * Updates recycler view to reflect any cache changes.
     */
    private fun onCacheChangedEvent(event: Int, file: File) {
        if (file.name.endsWith(".png") || file.name.endsWith(".jpg") || file.name.endsWith(".jpeg")) {
            // TODO: monte printDebug(event, file)
            when (event) {
//                FileObserver.CREATE ->
//                    runOnUiThread {
//                        adapter.addItem(
//                                file.toURI().toString(),
//                                ImageViewAdapter.State.PENDING)
//                    }
                FileObserver.OPEN ->
                    runOnUiThread {
                        adapter.addItem(
                                file.toURI().toString(),
                                ImageViewAdapter.State.PENDING)
                        adapter.notifyDataSetChanged()
                    }
                FileObserver.MODIFY ->
                    runOnUiThread {
                        adapter.updateItem(
                                file.toURI().toString(),
                                ImageViewAdapter.State.DOWNLOADING,
                                file.length().toInt())
                        adapter.notifyDataSetChanged()
                    }
                FileObserver.CLOSE_WRITE ->
                    runOnUiThread {
                        adapter.updateItem(
                                file.toURI().toString(),
                                ImageViewAdapter.State.DOWNLOADED,
                                file.length().toInt())
                        adapter.notifyDataSetChanged()
                    }
                FileObserver.DELETE or FileObserver.DELETE_SELF ->
                    runOnUiThread {
                        adapter.removeItem(file.toURI().toString())
                    }
            }
        }
    }

    fun printDebug(event: Int, file: File) {
        val title = when (event) {
            FileObserver.CREATE -> "CREATE"
        //FileObserver.MODIFY -> "MODIFY"
            FileObserver.OPEN -> "OPEN"
            FileObserver.CLOSE_WRITE -> "CLOSE"
            FileObserver.DELETE or FileObserver.DELETE_SELF -> "DELETE"
            else -> return
        }

        System.out.println(">************ $title *************")
        System.out.println("FileObserver        FILE: ${file.path}")
        System.out.println("FileObserver         URI: ${Uri.parse(file.path)}")
        System.out.println("FileObserver        Size: ${file.length()}")
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        rootUrl?.let { outState?.set(KEY_URL, it) }
        adapter.let {
            outState?.putStringArrayList(
                    KEY_IMAGE_URLS, ArrayList<String>(adapter.items))
        }
        super.onSaveInstanceState(outState)
    }

    /**
     * Delegates command handling to MenuHandler.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return handleCreateOptionsMenu(menu)
    }

    /**
     * Delegates command handling to MenuHandler.
     */
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val result = handlePrepareOptionsMenu(menu)
        updateViewStates()
        return result
    }

    /**
     * Delegates command handling to MenuHandler.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val handled = handleOptionsItemSelected(item)
        updateViewStates()
        alert {
            customView {
                val task = editText {
                    hint = "CrawlDepth"
                    padding = dip(20)
                }

                positiveButton("Ok") {
                    if (task.text.toString().isEmpty()) {
                        toast("Please specify a depth value.")
                    } else {
                        crawlDepth = task.text.toString().toInt()
                    }
                }
            }
        }
        return handled || super.onOptionsItemSelected(item)
    }

    /**
     * Updates the all views to reflect the current option settings
     * and the current crawl state.
     */
    fun updateViewStates() {
        when {
            crawlerRunning -> {
                searchFab.setImageDrawable(ContextCompat.getDrawable(
                        this, android.R.drawable.ic_menu_close_clear_cancel))
            }
            localCrawl -> {
                searchFab.setImageDrawable(ContextCompat.getDrawable(
                        this, android.R.drawable.stat_sys_download))
            }
            else -> {
                searchFab.setImageDrawable(searchDrawable)
            }
        }

        if (crawlerRunning || adapter.itemCount > 0) {
            hintView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        } else {
            hintView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        }

        toolbarTitle = getString(R.string.app_name) +
                if (localCrawl) " [LOCAL]" else ""
    }

    /**
     * Starts a WEB crawl based on the url returned from the Web View URL picker.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            val urls = when (PickerType.values()[requestCode]) {
                PickerType.URL_PICKER -> data?.getStringArrayListExtra(WebViewActivity.KEY_PICK_URLS)
                PickerType.IMAGE_PICKER -> data?.getStringArrayListExtra(WebViewActivity.KEY_PICK_URLS)
            }

            if (urls?.isNotEmpty() == true) {
                rootUrl = urls[0]
                crawlerRunning = runCrawler()
            } else {
                longToast("No URL was selected.")
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
}

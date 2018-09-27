package edu.vanderbilt.crawler.ui.activities

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.annotation.MainThread
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetBehavior.*
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.*
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import edu.vanderbilt.crawler.R
import edu.vanderbilt.crawler.adapters.ImageViewAdapter
import edu.vanderbilt.crawler.extensions.asyncLoadGif
import edu.vanderbilt.crawler.extensions.behaviour
import edu.vanderbilt.crawler.extensions.getResourceUri
import edu.vanderbilt.crawler.extensions.postDelayed
import edu.vanderbilt.crawler.platform.AndroidPlatform
import edu.vanderbilt.crawler.preferences.Preference
import edu.vanderbilt.crawler.ui.adapters.MultiSelectAdapter
import edu.vanderbilt.crawler.ui.adapters.WebViewUrlAdapter
import edu.vanderbilt.crawler.ui.views.ToolbarManager
import edu.vanderbilt.crawler.utils.KtLogger
import edu.vanderbilt.crawler.utils.warn
import edu.vanderbilt.crawler.viewmodels.MainViewModel
import edu.vanderbilt.crawler.viewmodels.MainViewModel.CrawlState.*
import edu.vanderbilt.crawler.viewmodels.Resource
import edu.vanderbilt.imagecrawler.crawlers.framework.ImageCrawler
import edu.vanderbilt.imagecrawler.platform.Controller
import edu.vanderbilt.imagecrawler.platform.Platform
import edu.vanderbilt.imagecrawler.transforms.Transform
import edu.vanderbilt.imagecrawler.utils.Options
import edu.vanderbilt.imagecrawler.utils.UriUtils
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import java.util.concurrent.TimeUnit

class MainActivity :
        AppCompatActivity(),
        ToolbarManager,
        MultiSelectAdapter.OnSelectionListener,
        KtLogger {

    /**
     * Enumerated type that lists all the image crawling
     * implementation strategies to test.
     */

    companion object {
        private val DEFAULT_CRAWL_DEPTH = 3
        private val DEFAULT_MAX_URLS = 1
        private val DEFAULT_LOCAL_CRAWL = false
        private val DEFAULT_CRAWL_SPEED = 100 // [0..100]%

        private val KEY_SELECTIONS = "MainActivity.key_selections"

        enum class PickerType {
            IMAGE_PICKER,
            URL_PICKER
        }
    }

    private val localUrl: String by lazy {
        Platform.ASSETS_URI_PREFIX +
        "/" +
        UriUtils.mapUriToRelativePath(Options.DEFAULT_WEB_URL)
    }

    override val toolbar by lazy { find<Toolbar>(R.id.toolbar) }
//    private val imageAdapter: ImageViewAdapter by lazy {
//        recyclerView.imageAdapter as ImageViewAdapter
//    }

    private var resourceList: List<Resource> = mutableListOf()
    private var crawlProgress: MainViewModel.CrawlProgress =
            MainViewModel.CrawlProgress(MainViewModel.CrawlState.IDLE)
    private var searchDrawable: Drawable? = null
    private var maxUrls = DEFAULT_MAX_URLS
    private var gridView = true

    /**
     * Persistent preferences.
     */
    private var webUrl: String by Preference(Options.DEFAULT_WEB_URL)
    internal var crawlStrategy: ImageCrawler.Type by Preference(ImageCrawler.Type.SEQUENTIAL_LOOPS)
    internal var localCrawl: Boolean by Preference(DEFAULT_LOCAL_CRAWL)
    internal var crawlDepth: Int by Preference(DEFAULT_CRAWL_DEPTH)
    internal var transformTypes: List<Transform.Type> by Preference(listOf())
    internal var bottomSheetState: Int by Preference(STATE_COLLAPSED)
    internal var crawlSpeed: Int by Preference(DEFAULT_CRAWL_SPEED)

    /**
     * All these properties must be internal so that
     * they can be accessible to MenuHandler.
     */
    internal lateinit var viewModel: MainViewModel
    private var selectionBundle: Bundle? = null
    private var showFab = true
    lateinit private var imageAdapter: ImageViewAdapter

    lateinit var originalToolbarTitle: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initToolbar()
        setSupportActionBar(toolbar)

        toolbarTitle = getString(R.string.app_name)

        // Connect or reconnect to view model.
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        // Setup all contained views.
        initializeViews()

        // Update all views to reflect the current state.
        updateViewStates()

        // Keep around original title for later restoring.
        originalToolbarTitle = toolbarTitle
    }

    /**
     * Subscribe or resubscribe to the view model when
     * the activity is resumed.
     */
    override fun onResume() {
        super.onResume()
        // Start subscribing to live item.
        subscribeViewModel()
    }

    /**
     * Subscribes to the view model's two cache and crawl live streams
     * passing in observers that call the appropriate live data result
     * handler. Note that the ViewModel framework will only keep a weak
     * reference to any live data Observers.
     *
     * Subscribing will automatically trigger cache events for each item
     * that currently exists in the cache at the time of this subscription.
     * When this occurs, the adapter will have received the latest list of
     * cached items and any pending list selection state saved before a
     * configuration change can then restored by the list adapter.
     *
     * The Android ViewModel framework handles the life cycles of this
     * activity and will automatically unhook these observers when the
     * activity is being destroyed.
     */
    private fun subscribeViewModel() {
        viewModel.subscribe(
                lifecycleOwner = this,
                cacheObserver = Observer { it?.let(this::handleCacheEvent) },
                crawlObserver = Observer { it?.let(this::handleProgressEvent) }) {
            if (selectionBundle != null) {
                imageAdapter.restoreSelectionStates(selectionBundle)
            }
        }
    }

    /**
     * Handles a new cache list received from live data feed.
     * Call the adapter to replace all existing items with this
     * new up to date list of items. The adapter uses DiffUtils
     * to ensure that the minimum amount of updating is performed.
     */
    private fun handleCacheEvent(items: List<Resource>) {
        if (viewModel.crawlCancelled || crawlProgress.status == CANCELLED) {
            warn {
                "MainActivity: Ignoring cache events " +
                "received after crawl cancelled..."
            }
            return
        }
        imageAdapter.updateItems(items)
    }

    /**
     * Live data cacheEvent handler for crawl time events as well
     * as crawl completion cacheEvent ([milliSeconds] == -1).
     */
    @MainThread
    private fun handleProgressEvent(progress: MainViewModel.CrawlProgress) {
        // Ingore all RUNNING events sent after a CANCELLED event.
        if (crawlProgress.status == CANCELLED && progress.status == RUNNING) {
            return
        }
        val oldCrawlProgress = crawlProgress
        crawlProgress = progress
        when (progress.status) {
            IDLE,
            CANCELLED,
            COMPLETED,
            FAILED -> {
                if (oldCrawlProgress.status != crawlProgress.status) {
                    imageAdapter.crawlStopped()
                    updateViewStates()
                    showFab(true)
                }
            }
            RUNNING -> {
                if (oldCrawlProgress.status != RUNNING) {
                    // If this is the first RUNNING status received, then
                    // update the view states to show the cancel FAB button.
                    updateViewStates()
                }
                val msecs = progress.millisecs
                val timer = String.format(
                        "%02d:%02d.%d",
                        TimeUnit.MILLISECONDS.toMinutes(msecs) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(msecs) % TimeUnit.MINUTES.toSeconds(1),
                        (msecs - ((msecs / 10000) * 10000)) % 10)
                title = "$originalToolbarTitle - $timer"
            }
        }
    }

    /**
     * Initialize all layout views when content view is created.
     */
    private fun initializeViews() {
        // Save search drawable to restore after crawl has stopped.
        searchDrawable = actionFab.drawable

        // Search FAB has 3 responsibilities
        // 1. Search: when local crawl setting and a root url has not been chosen.
        // 2. Start: when local mode is true or a root url has been chosen.
        // 3. Stop: when the crawler is running.
        actionFab.setOnClickListener {
            when (crawlProgress.status) {
                CANCELLED -> toast("Waiting for the crawler to terminate ...")
                RUNNING -> viewModel.cancelCrawl()
                IDLE,
                COMPLETED,
                FAILED -> {
                    if (localCrawl) {
                        startCrawl()
                    } else {
                        startUrlPicker(PickerType.URL_PICKER)
                    }
                }
            }

            updateViewStates()
        }

        // Restore seek bar progress from shared preferences.
        viewModel.crawlSpeed = crawlSpeed
        speedSeekBar.progress = crawlSpeed
        speedSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                seekBar?.let { crawlSpeed = it.progress; viewModel.crawlSpeed = it.progress }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Restore bottomSheet visibility state from shared preferences.
        setSpeedBottomSheetState(bottomSheetState)

        BottomSheetBehavior.from(speedBottomSheet).setBottomSheetCallback(
                object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        bottomSheetState = newState
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {}
                })

        // Setup recycler view, layout, and imageAdapter.
        with(recyclerView) {
            imageAdapter = ImageViewAdapter(
                    context = context,
                    list = resourceList.toMutableList(),
                    gridLayout = true,
                    onSelectionListener = this@MainActivity)
            recyclerView.adapter = imageAdapter

            if (gridView) {
                layoutManager = GridLayoutManager(context, 6)
                itemAnimator = null
            } else {
                layoutManager = LinearLayoutManager(context)
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            }

            // Custom scroll listener to show/hide FAB when user scrolls
            // so that they can see obscured images. The FAB is hidden
            // when scrolling starts and is shown when the user lifts
            // their finger from the display.
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    with(actionFab) {
                        when (newState) {
                            RecyclerView.SCROLL_STATE_IDLE -> {
                                contentView?.postDelayed(300) {
                                    behaviour().isAutoHideEnabled = true
                                    show()
                                }
                            }
                            RecyclerView.SCROLL_STATE_DRAGGING -> {
                                behaviour().isAutoHideEnabled = false
                                hide()
                            }
                            else -> {
                            }
                        }
                    }

                    super.onScrollStateChanged(recyclerView, newState)
                }
            })
        }
    }

    /**
     * Starts the url picker activity.
     */
    private fun startUrlPicker(type: PickerType) {
        when (type) {
            PickerType.URL_PICKER ->
                WebViewActivity.startUrlPickerForResult(
                        this,
                        startUrl = if (localCrawl) localUrl else webUrl,
                        pickUrls = null,
                        imagePicker = false,
                        maxUrls = maxUrls,
                        resultCode = type.ordinal)
            PickerType.IMAGE_PICKER -> {
                WebViewActivity.startUrlPickerForResult(
                        this,
                        startUrl = "https://www.google.ca/search?q=Dogs&newwindow" +
                                   "=1&source=lnms&tbm=isch&sa=X&ved=0ahUKEwjFzvip0" +
                                   "_7WAhUm6oMKHZyvBMoQ_AUICygC&biw=1292&bih=780",
                        pickUrls = null,
                        imagePicker = true,
                        maxUrls = 10,
                        resultCode = type.ordinal)
                contentView?.postDelayed(2000) {
                    longToast("Doug: You puppys right? Long-click on a puppy then! " +
                              "Pull up the bottom sheet and delete items too!")
                }
            }

        }
    }

    /**
     * Cancels and running crawl and starts a new one.
     */
    private fun startCrawl() {
        if (!localCrawl && webUrl.isBlank()) {
            toast("Please click the search button to pick a root URL to crawl.")
            return
        }

        // Restore normal crawl imageAdapter if image picker demo imageAdapter
        // was being used.
        if (recyclerView.adapter !is ImageViewAdapter) {
            recyclerView.adapter = imageAdapter
        }

        // For local crawl, set the root url to the root web-pages
        // directory in the application asses. The Options class
        // Options.DEFAULT_WEB_URL defines the default url used
        // to build the locally crawled web pages from the JUnit
        // test assignmentBuilder scripts.
        val rootUrl = if (localCrawl) {
            Platform.ASSETS_URI_PREFIX + "/" +
            UriUtils.mapUriToRelativePath(Options.DEFAULT_WEB_URL)
        } else {
            webUrl
        }

        // Build controller that is passed to the image-crawler library.
        val controller = Controller
                .newBuilder()
                .platform(AndroidPlatform)
                .transforms(transformTypes)
                .rootUrl(rootUrl)
                .maxDepth(crawlDepth)
                .diagnosticsEnabled(false)
                .build()

        /*
        // Start timer.
        startTime = System.currentTimeMillis()
        var runTimer = true

        val asyncTimer = doAsync {
            while (runTimer) {
                try {
                    val msecs = System.currentTimeMillis() - startTime
                    val timer = String.format(
                            "%02d:%02d:%d",
                            TimeUnit.MILLISECONDS.toMinutes(msecs) % TimeUnit.HOURS.toMinutes(1),
                            TimeUnit.MILLISECONDS.toSeconds(msecs) % TimeUnit.MINUTES.toSeconds(1),
                            (msecs - ((msecs / 10000) * 10000)) % 10)

                    uiThread {
                        title = "$originalToolbarTitle - $timer"
                    }

                    //Thread.sleep(10)
                } catch (e: Exception) {
                }
            }
        }
        */

        // Start an asynchronous crawl.
        viewModel.startCrawlAsync(crawlStrategy, controller)

        updateViewStates()
    }

    /**
     * Saves all properties so that they can be restored when
     * the activity is recreated.
     */
    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.run {
            putBundle(KEY_SELECTIONS, imageAdapter.saveSelectionStates())
        }
        super.onSaveInstanceState(outState)
    }

    /**
     * Restores all properties after activity is recreated.
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState?.run {
            selectionBundle = getBundle(KEY_SELECTIONS)
        }
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
    private fun updateViewStates() {
        when (crawlProgress.status) {
            RUNNING -> {
                actionFab.setImageDrawable(null)
                actionFab.setImageResource(R.drawable.ic_close_white_48dp)
            }

            CANCELLED -> {
                val uri = ctx.getResourceUri(R.drawable.waiting)
                actionFab.asyncLoadGif(uri.toString())
                //actionFab.setImageResource(R.drawable.ic_hourglass_empty_white_48dp)
            }
            IDLE,
            COMPLETED,
            FAILED -> {
                actionFab.setImageDrawable(null)
                if (localCrawl) {
                    actionFab.setImageResource(R.drawable.ic_file_download_white_48dp)
                } else {
                    actionFab.setImageResource(R.drawable.ic_search_white_48dp)
                }
            }
        }

        showHintView(imageAdapter.itemCount > 0
                     || crawlProgress.status == RUNNING)
    }

    /**
     * Broken ... always show the main view.
     */
    private fun showHintView(show: Boolean) {
        if (false) {
            mainActivityHintView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            mainActivityHintView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    /**
     * Helper method to force showing or hiding the FAB.
     */
    private fun showFab(show: Boolean) {
        if (show != actionFab.isShown) {
            showFab = show
            if (showFab) {
                actionFab.show()
            } else {
                actionFab.hide()
            }
        }
    }

    /**
     * Starts a WEB crawl based on the url returned from the Web View URL picker.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            data?.let {
                when (PickerType.values()[requestCode]) {
                    PickerType.URL_PICKER -> {
                        webUrl = it.getStringExtra(WebViewActivity.KEY_URL)
                        startCrawl()
                    }
                    PickerType.IMAGE_PICKER -> {
                        val urls = it.getStringArrayListExtra(WebViewActivity.KEY_PICK_URLS)
                        recyclerView.adapter = WebViewUrlAdapter(
                                this, urls, true,
                                object : MultiSelectAdapter.OnSelectionListener {
                                    override fun onActionModeFinished() {
                                        if (recyclerView.adapter.itemCount == 0) {
                                            updateViewStates()
                                        }
                                    }
                                })
                    }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Callback from the imageAdapter when an action mode command is executed.
     */
    override fun onActionModeCommand(selections: List<Int>, menuActionId: Int): Boolean {
        if (menuActionId == R.id.action_delete) {
            val items = imageAdapter.selectedItems
            items.forEach {
                // The url is currently the item's encoded key.
                AndroidPlatform.cache.remove(it.url)
            }
        }

        // Returning false indicates that we didn't handle deleting the
        // items from the imageAdapter, so the imageAdapter will do that for us.
        return false
    }

    /**
     * Sets the current speed bottom sheet visibility state.
     */
    private fun setSpeedBottomSheetState(state: Int) {
        // Update shared preference and set the bottom sheet state.
        bottomSheetState = state
        BottomSheetBehavior.from(speedBottomSheet).state = state
    }

    /**
     * Toggles the speed bottom visibility state.
     */
    fun toggleSpeedBottomSheet() {
        setSpeedBottomSheetState(
                with(BottomSheetBehavior.from(speedBottomSheet)) {
                    when (state) {
                        STATE_HIDDEN,
                        STATE_COLLAPSED ->
                            STATE_EXPANDED
                        else ->
                            STATE_HIDDEN
                    }
                })
    }

    /**
     * Runs the image picker for demo purposes.
     */
    fun runImagePickerDemo() {
        startUrlPicker(PickerType.IMAGE_PICKER)
    }
}


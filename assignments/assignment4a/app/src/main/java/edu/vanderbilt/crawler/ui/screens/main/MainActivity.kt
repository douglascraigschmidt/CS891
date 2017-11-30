package edu.vanderbilt.crawler.ui.screens.main

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.annotation.MainThread
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetBehavior.*
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.*
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import edu.vanderbilt.crawler.R
import edu.vanderbilt.crawler.adapters.ImageViewAdapter
import edu.vanderbilt.crawler.extensions.*
import edu.vanderbilt.crawler.platform.AndroidPlatform
import edu.vanderbilt.crawler.preferences.CompositeUnsubscriber
import edu.vanderbilt.crawler.preferences.ObservablePreference
import edu.vanderbilt.crawler.preferences.Preference
import edu.vanderbilt.crawler.preferences.Subscriber
import edu.vanderbilt.crawler.ui.adapters.MultiSelectAdapter
import edu.vanderbilt.crawler.ui.adapters.WebViewUrlAdapter
import edu.vanderbilt.crawler.ui.screens.settings.Settings
import edu.vanderbilt.crawler.ui.screens.webview.WebViewActivity
import edu.vanderbilt.crawler.ui.views.ToolbarManager
import edu.vanderbilt.crawler.utils.KtLogger
import edu.vanderbilt.crawler.utils.warn
import edu.vanderbilt.crawler.viewmodels.MainViewModel
import edu.vanderbilt.crawler.viewmodels.MainViewModel.CrawlState.*
import edu.vanderbilt.crawler.viewmodels.Resource
import edu.vanderbilt.imagecrawler.platform.Controller
import edu.vanderbilt.imagecrawler.platform.Platform
import edu.vanderbilt.imagecrawler.utils.Options
import edu.vanderbilt.imagecrawler.utils.UriUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.toolbar.*
import org.jetbrains.anko.contentView
import org.jetbrains.anko.find
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
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
        private val KEY_TIME = "MainActivity.time"
        private val KEY_THREADS = "MainActivity.threads"

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
    private var maxUrls = DEFAULT_MAX_URLS
    private var gridView = true
    private var threads = 0
    private var elapsedTime = -1L
    private var itemCount = 0

    /**
     * Persistent preferences.
     */
    private var webUrl: String by Preference(Options.DEFAULT_WEB_URL)
    private var bottomSheetState: Int by Preference(STATE_COLLAPSED)

    private lateinit var viewModel: MainViewModel
    private var selectionBundle: Bundle? = null
    private var showFab = true
    private var crawlCancelling: Boolean = false
    private lateinit var imageAdapter: ImageViewAdapter

    /** Observe all  crawl speed preference changes. */
    private val compositeUnsubscriber = CompositeUnsubscriber()
    private var crawlSpeed: Int by ObservablePreference(
            default = 100,
            name = "CrawlSpeedPreference",
            subscriber = object : Subscriber<Int> {
                override val subscriber: (Int) -> Unit
                    get() = {
                        speedSeekBar.progress = it
                        viewModel.crawlSpeed = it
                    }

                override fun unsubscribe(callback: () -> Unit) {
                    compositeUnsubscriber.add(callback)
                }
            })

    /** Ensures that settings drawer is only peeked out once. */
    private var peekDrawer: Boolean by Preference(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Connect or reconnect to view model before calling setContent
        // which indirectly will cause the crawlSpeed ObservablePreference
        // callback has a reference to the lateinit viewModel.
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        setContentView(R.layout.activity_main)
        initToolbar()
        setSupportActionBar(toolbar)

        // Setup all contained views.
        initializeViews()

        // Update all views to reflect the current state.
        updateViewStates()

        // Keep around original title for later restoring.
        updateTitle()

        // Hide FAB so that it can be animated into view.
        progressFab.visibility = View.INVISIBLE

        // Only peek drawer when a session first starts.
        if (savedInstanceState == null) {
            peekDrawer = true
        }
    }

    /**
     * Subscribe or resubscribe to the view model when
     * the activity is resumed.
     */
    override fun onResume() {
        super.onResume()
        // Start subscribing to live item.
        subscribeViewModel()

        // Animate Fab into view if not already visible.
        if (!progressFab.visible) {
            postDelayed(1000L) {
                showFab(true)
            }
        }

        if (peekDrawer && !drawerLayout.isDrawerOpen(GravityCompat.END)) {
            postDelayed(500) {
                drawerLayout.peekDrawer()
                peekDrawer = false
            }
        }
    }

    override fun onDestroy() {
        compositeUnsubscriber.invoke()
        super.onDestroy()
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
        if (crawlCancelling || viewModel.crawlCancelled) {
            warn {
                "MainActivity: Ignoring cache events " +
                "received after crawl cancelled..."
            }
            return
        }
        imageAdapter.updateItems(items)
        showHintView(imageAdapter.itemCount == 0)
        itemCount = imageAdapter.itemCount
        updateTitle()
    }

    /**
     * Live data cacheEvent handler for crawl time events as well
     * as crawl completion cacheEvent ([milliSeconds] == -1).
     */
    @MainThread
    private fun handleProgressEvent(progress: MainViewModel.CrawlProgress) {
        val oldCrawlProgress = crawlProgress
        crawlProgress = progress
        when (progress.state) {
            CANCELLING -> {
                showFab(true)
                updateViewStates()
            }
            IDLE,
            CANCELLED,
            COMPLETED,
            FAILED -> {
                if (oldCrawlProgress.state != crawlProgress.state) {
                    imageAdapter.crawlStopped(progress.state == CANCELLED)
                    updateViewStates()
                    showFab(true)
                }
            }
            RUNNING -> {
                if (oldCrawlProgress.state != RUNNING) {
                    // If this is the first RUNNING state received, then
                    // update the view states to show the cancel FAB button.
                    updateViewStates()
                }
                elapsedTime = progress.millisecs
                threads = progress.threads
                updateTitle()
            }
        }
    }

    /**
     * Helper that updates a composite title containing crawl run information.
     */
    internal fun updateTitle() {
        if (elapsedTime == -1L) {
            crawlTracker.gone = true

            var titleString = getString(R.string.app_name)
            if (itemCount > 0) {
                titleString += " - Images: $itemCount"
            }
            title = titleString
        } else {
            title = ""
            crawlTracker.visible = true
            imagesValue.text = itemCount.toString()
            threadsValue.text = threads.toString()

            timeLabel.visible = true
            timeValue.visible = true
            timeValue.text = String.format(
                    "%01d:%02d.%d",
                    TimeUnit.MILLISECONDS.toMinutes(elapsedTime) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % TimeUnit.MINUTES.toSeconds(1),
                    (elapsedTime - ((elapsedTime / 10000) * 10000)) % 10)
            strategyValue.text = Settings.crawlStrategy.toString()
        }
    }

    /**
     * Initialize all layout views when content view is created.
     */
    private fun initializeViews() {
        // Search FAB has 3 responsibilities
        // 1. Search: when local crawl setting and a root url has not been chosen.
        // 2. Start: when local mode is true or a root url has been chosen.
        // 3. Stop: when the crawler is running.
        actionFab.setOnClickListener {
            when (crawlProgress.state) {
                RUNNING -> {
                    // Keep track of cancelling state here instead
                    // of waiting for a CANCELLING progress event
                    // so that the FAB is immediately changed to the
                    // waiting image and will disable all clicks until
                    // the cancel operation completes.
                    crawlCancelling = true
                    viewModel.cancelCrawl()
                }
                IDLE,
                COMPLETED,
                CANCELLED,
                FAILED -> {
                    if (Settings.localCrawl) {
                        startCrawl()
                    } else {
                        showFab(false) {
                            startUrlPicker(PickerType.URL_PICKER)
                        }
                    }
                }
                else -> {
                    if (crawlCancelling) {
                        toast("Waiting for the crawler to terminate ...")
                    }
                }
            }

            updateViewStates()
        }

        // Restore seek bar progress from shared preferences.
        viewModel.crawlSpeed = Settings.crawlSpeed
        speedSeekBar.progress = Settings.crawlSpeed
        speedSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                seekBar?.let { Settings.crawlSpeed = it.progress; viewModel.crawlSpeed = it.progress }
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
                    with(progressFab) {
                        //var behaviour: ProgressFab.Behavior = behaviour()
                        when (newState) {
                            RecyclerView.SCROLL_STATE_IDLE -> {
                                contentView?.postDelayed(300) {
                                    //behaviour.isAutoHideEnabled = true
                                    show()
                                }
                            }
                            RecyclerView.SCROLL_STATE_DRAGGING -> {
                                //behaviour.isAutoHideEnabled = false
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
                        startUrl = if (Settings.localCrawl) localUrl else webUrl,
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
        if (!Settings.localCrawl && webUrl.isBlank()) {
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
        val rootUrl = if (Settings.localCrawl) {
            Platform.ASSETS_URI_PREFIX + "/" +
            UriUtils.mapUriToRelativePath(Options.DEFAULT_WEB_URL)
        } else {
            webUrl
        }

        // Build controller that is passed to the image-crawler library.
        val controller = Controller
                .newBuilder()
                .platform(AndroidPlatform)
                .transforms(Settings.transformTypes)
                .rootUrl(rootUrl)
                .maxDepth(Settings.crawlDepth)
                .diagnosticsEnabled(false)
                .build()

        // Cancel possibly set cancel flag from a previous crawl.
        crawlCancelling = false

        // Start an asynchronous crawl.
        viewModel.startCrawlAsync(Settings.crawlStrategy, controller)

        updateViewStates()
    }

    /**
     * Saves all properties so that they can be restored when
     * the activity is recreated.
     */
    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.run {
            putBundle(KEY_SELECTIONS, imageAdapter.saveSelectionStates())
            putLong(KEY_TIME, elapsedTime)
            putInt(KEY_THREADS, threads)
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
            elapsedTime = getLong(KEY_TIME)
            threads = getInt(KEY_THREADS)
        }
    }

    /**
     * Delegates command handling to MenuHandler.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * Delegates command handling to MenuHandler.
     */
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        //TODO(monte): ??? updateViewStates()
        return true
    }

    /**
     * Delegates command handling to MenuHandler.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.actionSettings -> {
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END)
                } else {
                    drawerLayout.openDrawer(GravityCompat.END)
                }
                //SettingsDialogFragment.newInstance().show(supportFragmentManager, "dialog")
                true
            }

            R.id.action_select_all -> {
                with(recyclerView.adapter as ImageViewAdapter) {
                    startActionModeAndSelectAll()
                }
                true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    /**
     * Updates the all views to reflect the current option settings
     * and the current crawl state.
     */
    private fun updateViewStates() {
        when (crawlProgress.state) {
            RUNNING -> {
                progressBar.visible = true
                actionFab.setImageResource(R.drawable.ic_close_white_48dp)
            }
            CANCELLING -> {
                actionFab.setImageResource(R.drawable.ic_hourglass_empty_white_48dp)
            }
            IDLE,
            COMPLETED,
            CANCELLED,
            FAILED -> {
                progressBar.visible = false
                if (Settings.localCrawl) {
                    actionFab.setImageResource(R.drawable.ic_file_download_white_48dp)
                } else {
                    actionFab.setImageResource(R.drawable.ic_search_white_48dp)
                }
            }
        }

        showHintView(imageAdapter.itemCount == 0 && crawlProgress.state == RUNNING)
    }

    /**
     * Broken ... always show the main view.
     */
    private fun showHintView(show: Boolean) {
        postDelayed(200) {
            if (!show || imageAdapter.itemCount == 0) {
                mainActivityHintView.show = show
                recyclerView.gone = show
            }
        }
    }

    /**
     * Helper method to force showing or hiding the FAB.
     */
    private fun showFab(show: Boolean, run: (() -> Unit)? = null) {
        if (show != progressFab.isShown) {
            showFab = show
            if (showFab) {
                progressFab.show(run)
            } else {
                progressFab.hide(run)
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
}


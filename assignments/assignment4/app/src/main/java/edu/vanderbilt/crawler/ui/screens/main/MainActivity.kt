package edu.vanderbilt.crawler.ui.screens.main

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import android.widget.SeekBar
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import edu.vanderbilt.crawler.R
import edu.vanderbilt.crawler.adapters.ImageViewAdapter
import edu.vanderbilt.crawler.adapters.MultiSelectAdapter
import edu.vanderbilt.crawler.adapters.WebViewUrlAdapter
import edu.vanderbilt.crawler.extensions.bottomSheetState
import edu.vanderbilt.crawler.extensions.peekDrawer
import edu.vanderbilt.crawler.extensions.postDelayed
import edu.vanderbilt.crawler.platform.AndroidCache
import edu.vanderbilt.crawler.platform.AndroidPlatform
import edu.vanderbilt.crawler.preferences.Preference
import edu.vanderbilt.crawler.preferences.PreferenceProvider
import edu.vanderbilt.crawler.ui.screens.pager.PagedActivityClient
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
        PagedActivityClient,
        SharedPreferences.OnSharedPreferenceChangeListener,
        KtLogger {

    /**
     * Override PagedActivityClient.pagedActivityClient
     * property and set to this activity.
     */
    override val pagedActivityClient: Activity
        get() = this

    /**
     * To prevent rapid button clicking from starting more
     * than one paged activity at a time.
     */
    override var pagedActivityStarted = false

    /**
     * Enumerated type that lists all the image crawling
     * implementation strategies to test.
     */
    companion object {
        private val DEFAULT_MAX_URLS = 1

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

    private var resourceList: List<Resource> = mutableListOf()
    private var crawlProgress: MainViewModel.CrawlProgress =
            MainViewModel.CrawlProgress(IDLE)
    private var maxUrls = DEFAULT_MAX_URLS
    private var gridView = true
    private var threads = 0
    private var elapsedTime = -1L
    private var itemCount = 0

    private var spanCount: Int
        get() = (recyclerView.layoutManager as GridLayoutManager).spanCount
        set(value) {
            (recyclerView.layoutManager as GridLayoutManager).spanCount = value
        }

    lateinit var viewModel: MainViewModel
    private var selectionBundle: Bundle? = null
    private var showFab = true
    private var crawlCancelling: Boolean = false
    private lateinit var imageAdapter: ImageViewAdapter

    /**
     * Persistent preferences and preference observers.
     * (See Settings and SettingsDialogFragment).
     */

    /** Ensures that settings drawer is only peeked out once. */
    private var peekDrawer: Boolean by Preference(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        initToolbar()
        setSupportActionBar(toolbar)

        // Connect or reconnect to view model before calling setContent
        // which indirectly will cause the crawlSpeed ObservablePreference
        // callback has a reference to the lateinit viewModel.
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

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

        // Register this class to observe shared preference changes.
        PreferenceProvider.prefs.registerOnSharedPreferenceChangeListener(this)
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
        if (!progressFab.isVisible) {
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
        PreferenceProvider.prefs.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    /**
     * Override Activity.onActivityReenter and forward
     * event to PagedActivityClient.onActivityReenterEvent.
     */
    override fun onActivityReenter(resultCode: Int, data: Intent) {
        super.onActivityReenter(resultCode, data)
        onActivityReenterHandler(resultCode, data)
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
        if (viewModel.state == CANCELLING || viewModel.state == CANCELLED) {
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
     * as crawl completion cacheEvent.
     */
    @MainThread
    private fun handleProgressEvent(progress: MainViewModel.CrawlProgress) {
        val oldCrawlProgress = crawlProgress
        crawlProgress = progress
        when (progress.state) {
            RUNNING -> {
                // Show cancel FAB
                updateViewStates()
                elapsedTime = progress.millisecs
                threads = progress.threads
                updateTitle()
            }
            CANCELLING -> {
                crawlCancelling = true
                showFab(true)
                updateViewStates()
            }
            IDLE,
            CANCELLED,
            COMPLETED,
            FAILED -> {
                crawlCancelling = false
                if (oldCrawlProgress.state != crawlProgress.state) {
                    imageAdapter.crawlStopped(progress.state == CANCELLED)
                    updateViewStates()
                    showFab(true)
                }
            }
        }
    }

    /**
     * Helper that updates a composite title containing crawl run information.
     */
    private fun updateTitle() {
        if (!isCrawlRunning()) {
            crawlTracker.isGone = true

            var titleString = getString(R.string.app_name)
            if (itemCount > 0) {
                titleString += " - Images: $itemCount"
            }
            title = titleString
        } else {
            title = ""
            crawlTracker.isVisible = true
            imagesValue.text = itemCount.toString()
            threadsValue.text = threads.toString()

            timeLabel.isVisible = true
            timeValue.isVisible = true
            strategyValue.text = Settings.crawlStrategy.toString()
        }
    }

    /**
     * Helper that clears previously set title bar statistic views.
     */
    private fun clearTitleBar() {
        timeValue.stop()
        timeValue.base = SystemClock.elapsedRealtime()
        itemCount = 0
        threads = 0
        updateTitle()
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
                    stopCrawl()
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

        BottomSheetBehavior.from(speedBottomSheet).setBottomSheetCallback(
                object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        when (newState) {
                            STATE_EXPANDED,
                            STATE_COLLAPSED,
                            STATE_HIDDEN -> {
                                Settings.speedBarState = newState
                            }
                            else -> Unit
                        }
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {}
                })

        // Restore bottomSheet visibility state from shared preferences.
        speedBottomSheet.bottomSheetState = Settings.speedBarState

        imageAdapter = ImageViewAdapter(
                context = this,
                list = resourceList.toMutableList(),
                gridLayout = true,
                onSelectionListener = this@MainActivity)

        // Setup recycler view, layout, and imageAdapter.
        recyclerView.initialize(imageAdapter)
    }

    private fun RecyclerView.initialize(adapter: ImageViewAdapter) {
        // Setup recycler view, layout, and imageAdapter.
        this.adapter = adapter

        if (gridView) {
            layoutManager = GridLayoutManager(context, Settings.viewScale)
            itemAnimator = null
        } else {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        registerForContextMenu(this)

        // A gesture detector combined with an OnItemTouchListener is
        // the only way to detect long-clicks on recycler view background.
        val gestureDetector = GestureDetector(context,
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onLongPress(e: MotionEvent) {
                        println("***************** showContextMenu called")
                        showContextMenu(e.x, e.y)
                    }
                })

        addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                gestureDetector.onTouchEvent(e)
                return false
            }
        })

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

    /**
     * Starts the url picker activity.
     */
    private fun startUrlPicker(type: PickerType) {
        when (type) {
            PickerType.URL_PICKER ->
                WebViewActivity.startUrlPickerForResult(
                        this,
                        startUrl = if (Settings.localCrawl) localUrl else Settings.webUrl,
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
    fun startCrawl() {
        if (!Settings.localCrawl && Settings.webUrl.isBlank()) {
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
            Settings.webUrl
        }

        // Build controller that is passed to the image-crawler library.
        val controller = Controller
                .newBuilder()
                .platform(AndroidPlatform)
                .transforms(Settings.transformTypes.filterNotNull())
                .rootUrl(rootUrl)
                .maxDepth(Settings.crawlDepth)
                .diagnosticsEnabled(false)
                .build()

        // Cancel possibly set cancel flag from a previous crawl.
        crawlCancelling = false

        // Clear previous title bar statistic views.
        clearTitleBar()

        // Start an asynchronous crawl.
        viewModel.startCrawl(Settings.crawlStrategy, controller)

        updateViewStates()
    }

    fun stopCrawl() {
        if (crawlCancelling) {
            warn("stopCrawl() called when crawl is already cancelling")
            return
        }

        // Keep track of cancelling state here instead
        // of waiting for a CANCELLING progress event
        // so that the FAB is immediately changed to the
        // waiting image and will disable all clicks until
        // the cancel operation completes.
        crawlCancelling = viewModel.cancelCrawl() == CANCELLING
        updateViewStates()
    }

    /**
     * Saves all properties so that they can be restored when
     * the activity is recreated.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        outState.run {
            putBundle(KEY_SELECTIONS, imageAdapter.saveSelectionStates())
            putLong(KEY_TIME, elapsedTime)
            putInt(KEY_THREADS, threads)
        }
        super.onSaveInstanceState(outState)
    }

    /**
     * Restores all properties after activity is recreated.
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.run {
            selectionBundle = getBundle(KEY_SELECTIONS)
            elapsedTime = getLong(KEY_TIME)
            threads = getInt(KEY_THREADS)
        }
    }

    /**
     * Creates a context menu.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_delete)?.isEnabled =
                imageAdapter.selectedCount > 0

        menu.findItem(R.id.action_select_all)?.isEnabled =
                imageAdapter.selectedCount < imageAdapter.itemCount

        return super.onPrepareOptionsMenu(menu)
    }

    /**
     * Creates a context menu whose entries depend on whether action
     * mode is enabled or not and the current item selection state.
     */
    override fun onCreateContextMenu(menu: ContextMenu, v: View?,
                                     menuInfo: ContextMenu.ContextMenuInfo?) {
        if (imageAdapter.actionMode != null) {
            menuInflater.inflate(R.menu.menu_action_mode, menu)
            if (imageAdapter.selectedCount == imageAdapter.itemCount) {
                menu.removeItem(R.id.action_select_all)
            }
            if (imageAdapter.selectedCount == 0) {
                menu.removeItem(R.id.action_delete)
            }
        } else {
            if (imageAdapter.itemCount > 0) {
                menuInflater.inflate(R.menu.menu_no_action_mode, menu)
            }
        }

        super.onCreateContextMenu(menu, v, menuInfo)
    }

    /**
     * Delegates options menu commands to generic action helper or super class.
     */
    override fun onOptionsItemSelected(item: MenuItem) =
            performAction(item.itemId) or super.onOptionsItemSelected(item)

    /**
     * Delegates context menu commands to generic action helper or super class.
     */
    override fun onContextItemSelected(item: MenuItem) =
            performAction(item.itemId) or super.onContextItemSelected(item)

    /**
     * Helper that executes all supported action commands for all menu types.
     */
    private fun performAction(actionId: Int) =
            when (actionId) {
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
                R.id.action_clear -> {
                    imageAdapter.items.forEach {
                        AndroidPlatform.cache.remove(it.url)
                    }
                    true
                }
                R.id.action_delete -> {
                    imageAdapter.selectedItems.forEach {
                        AndroidPlatform.cache.remove(it.url)
                    }
                    true
                }
                else -> false
            }

    /**
     * Catch back press and if the settings panel is visible, close the panel.
     */
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }

    /**
     * Updates the all views to reflect the current option settings
     * and the current crawl state.
     */
    private fun updateViewStates() {
        when (crawlProgress.state) {
            RUNNING -> {
                progressBar.isVisible = true
                actionFab.setImageResource(R.drawable.ic_close_white_48dp)
                timeValue.start()
            }
            CANCELLING -> {
                timeValue.stop()
                actionFab.setImageResource(R.drawable.ic_hourglass_empty_white_48dp)
            }
            IDLE,
            COMPLETED,
            CANCELLED,
            FAILED -> {
                timeValue.stop()
                progressBar.isVisible = false
                if (Settings.localCrawl) {
                    actionFab.setImageResource(R.drawable.ic_file_download_white_48dp)
                } else {
                    actionFab.setImageResource(R.drawable.ic_search_white_48dp)
                }
                // This is only necessary to call if DiffUtils is being used
                // to make UI updates more efficient. Doing so prevents the
                // view from doing layouts when toggling state, size, thread,
                // and progress widgets so a final layout used to be forced
                // here so that when the crawl finished and all the transient
                // widgets were set to "gone" the layout would reduce each
                // row size to reflect the gone states. Now that DiffUtils
                // always returns false for content being the same, a new
                // layout for each item is performed on every state update
                // sent by the crawler and the grid items properly resize
                // during the crawl.
                // imageAdapter.notifyDataSetChanged()
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
                mainActivityHintView.isVisible = show
                recyclerView.isGone = show
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
                        Settings.webUrl = it.getStringExtra(WebViewActivity.KEY_URL)!!
                        startCrawl()
                    }
                    PickerType.IMAGE_PICKER -> {
                        val urls = it.getStringArrayListExtra(WebViewActivity.KEY_PICK_URLS)
                        recyclerView.adapter = WebViewUrlAdapter(
                                this, urls!!, true,
                                object : MultiSelectAdapter.OnSelectionListener {
                                    override fun onActionModeFinished() {
                                        if (recyclerView.adapter?.itemCount == 0) {
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
     * Returns true if crawl is running, false if it's not running.
     */
    fun isCrawlRunning(): Boolean {
        return when (crawlProgress.state) {
            RUNNING, CANCELLING -> true
            else -> false
        }
    }

    /**
     * OnSelectionListener hook called to check if action
     * mode should be started when the user long-clicks
     * on an item. Action mode is not allowed when a crawl
     * is running.
     */
    override fun onActionModeStarting(): Boolean {
        return if (!isCrawlRunning()) {
            true
        } else {
            toast("Item selection is disabled while a crawl is running.")
            false
        }
    }

    /**
     * OnSelectionListener hook called when action
     * mode has been successfully started.
     */
    override fun onActionModeStarted() {
        actionFab.hide()
    }

    /**
     * OnSelectionListener hook called when action
     * mode has finished.
     */
    override fun onActionModeFinished() {
        actionFab.show()
    }

    /**
     * OnSelectionListener hook called when an
     * action mode command is has been selected.
     */
    override fun onActionModeCommand(selections: List<Int>, menuActionId: Int): Boolean {
        // Returning false indicates that we didn't handle deleting the
        // items from the imageAdapter, so the imageAdapter will do that for us.
        return performAction(menuActionId)
    }

    /**
     * OnSelectionLister hook called when an adapter item
     * has been clicked.
     */
    override fun onItemClick(view: View, position: Int) {
        // Start pager activity to view an image.
        startPagedActivity(view, position, imageAdapter)
    }

    /**
     * OnSelectionLister hook called when an adapter item
     * has been double-clicked.
     */
    override fun onItemLongClick(view: View, position: Int): Boolean {
        // Always allow long-clicks so that on onActionModeStarting
        // hook will be called and it can decide if the activity
        // is in a state that allows or disallows entering action mode.
        view.showContextMenu()
        return true
    }

    /**
     * Called whenever the number of selected items changes.
     */
    override fun onSelectionChanged(count: Int) {
        invalidateOptionsMenu()
    }

    /**
     * React to shared preference (Settings) changes.
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            Settings.CRAWL_SPEED_PREF -> {
                speedSeekBar?.progress = Settings.crawlSpeed
                viewModel.crawlSpeed = Settings.crawlSpeed
            }
            Settings.SPEED_BAR_STATE_PREF -> {
                speedBottomSheet?.bottomSheetState = Settings.speedBarState
            }
            Settings.GRID_SCALE_PREF -> {
                AndroidCache.clearDownloaderCache()
                recyclerView.initialize(imageAdapter)
            }
            Settings.SHOW_PROGRESS_PREF,
            Settings.SHOW_STATE_PREF,
            Settings.SHOW_SIZE_PREF,
            Settings.SHOW_THREAD_PREF -> {
                imageAdapter.notifyDataSetChanged()
            }
            Settings.LOCAL_CRAWL_PREF -> {
                updateViewStates()
            }
        }
    }
}


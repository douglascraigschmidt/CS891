package edu.vanderbilt.crawler.ui.activities

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetBehavior.*
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.*
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import edu.vanderbilt.crawler.R
import edu.vanderbilt.crawler.adapters.ImageViewAdapter
import edu.vanderbilt.crawler.app.Prefs
import edu.vanderbilt.crawler.extensions.*
import edu.vanderbilt.crawler.platform.AndroidPlatform
import edu.vanderbilt.crawler.ui.adapters.MultiSelectAdapter
import edu.vanderbilt.crawler.ui.views.ToolbarManager
import edu.vanderbilt.crawler.utils.KtLogger
import edu.vanderbilt.crawler.viewmodels.MainViewModel
import edu.vanderbilt.crawler.viewmodels.Resource
import edu.vanderbilt.imagecrawler.crawlers.framework.ImageCrawler
import edu.vanderbilt.imagecrawler.platform.Controller
import edu.vanderbilt.imagecrawler.platform.Platform
import edu.vanderbilt.imagecrawler.transforms.Transform
import edu.vanderbilt.imagecrawler.utils.Options
import edu.vanderbilt.imagecrawler.utils.UriUtils
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*

class MainActivity :
        AppCompatActivity(),
        ToolbarManager,
        MultiSelectAdapter.OnSelectionListener,
        MainViewModel.CrawlCompletedListener,
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
        private val KEY_SPEED_BAR_STATE = "MainActivity.speed_bar_state"
        private val KEY_CRAWL_URL = "MainActivity.crawl_url"
        private val KEY_CRAWL_SPEED = "MainActivity.key_crawl_speed"
        private val KEY_CRAWL_STRATEGY = "MainActivity.crawl_strategy"
        private val KEY_CRAWL_TRANSFORMS = "MainActivity.crawl_transforms"
        private val KEY_CRAWL_LOCAL = "MainActivity.crawl_local"
        private val KEY_CRAWL_DEPTH = "MainActivity.crawl_depth"

        private val DEFAULT_PICKER_TYPE = PickerType.URL_PICKER

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
    private val adapter: ImageViewAdapter by lazy {
        recyclerView.adapter as ImageViewAdapter
    }

    /**
     * Options.
     */
    private var resourceList: List<Resource> = mutableListOf()
    private var searchDrawable: Drawable? = null
    private var maxUrls = DEFAULT_MAX_URLS
    private var pickerType = DEFAULT_PICKER_TYPE
    private var gridView = true

    /**
     * All these properties must be internal so that
     * they can be accessible to MenuHandler.
     */
    internal var webUrl: String? = null
    internal var crawlStrategy: ImageCrawler.Type = ImageCrawler.Type.SEQUENTIAL_LOOPS
    internal var transformTypes: MutableList<Transform.Type> = mutableListOf()
    internal var localCrawl = DEFAULT_LOCAL_CRAWL
    internal var crawlDepth: Int = DEFAULT_CRAWL_DEPTH
    private lateinit var observer: Observer<List<Resource>>
    private lateinit var viewModel: MainViewModel
    private var selectionBundle: Bundle? = null
    private var showFab = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initToolbar()
        setSupportActionBar(toolbar)

        toolbarTitle = getString(R.string.app_name)

        // Connect or reconnect to view model.
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        if (savedInstanceState == null) {
            // Restore state (must be called after viewModel is set)
            restorePersistentState()
        }

        // Setup all contained views.
        initializeViews()

        // Update all views to reflect the current state.
        updateViewStates()
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
     * Always save all properties to shared preferences when
     * the activity is stopped in case onDestroy is not called.
     */
    override fun onStop() {
        savePersistentState()
        super.onStop()
    }

    /**
     * Creates a new Observer that adds or updates the adapter
     * with received resource values, and the adds the observer
     * to the live data managed by the ViewModel.
     */
    private fun subscribeViewModel() {
        observer = Observer { it?.let { adapter.updateItems(it) } }
        viewModel.subscribe(this, observer) {
            if (selectionBundle != null) {
                adapter.restoreSelectionStates(selectionBundle)
            }
        }
    }

    /**
     * Initialize all layout views when content view is created.
     */
    private fun initializeViews() {
        // Setup recycler view, layout, and adapter.
        with(recyclerView) {
            adapter = ImageViewAdapter(
                    context = context,
                    list = resourceList.toMutableList(),
                    gridLayout = true,
                    onSelectionListener = this@MainActivity)

            if (gridView) {
                layoutManager = GridLayoutManager(context, 6)
                itemAnimator = null
            } else {
                layoutManager = LinearLayoutManager(context)
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            }
        }

        // Save search drawable to restore after crawl has stopped.
        searchDrawable = actionFab.drawable

        // Search FAB has 3 responsibilities
        // 1. Search: when local crawl setting and a root url has not been chosen.
        // 2. Start: when local mode is true or a root url has been chosen.
        // 3. Stop: when the crawler is running.
        actionFab.setOnClickListener {
            when {
                viewModel.isCrawlRunning -> {
                    viewModel.cancelCrawl()
                }
                localCrawl -> {
                    startCrawl()
                }
                else -> {
                    startUrlPicker()
                }
            }

            updateViewStates()
        }

        speedSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                seekBar?.let { viewModel.crawlSpeed = it.progress }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Custom scroll listener required for showing and hiding FAB
        // when recycler view is scrolled in different directions.
        // This is necessary since the FAB is anchored to a bottom sheet
        // and the default bottom sheet behaviour will override an
        // attempt to hide the FAB forcing it to be permanently visible.
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val behavior: FloatingActionButton.Behavior = actionFab.behaviour()

                if (dy < 0 && !actionFab.isShown) {
                    // Wait until idle to show FAB when scrolling up.
                    showFab = true
                } else if (dy > 0 && actionFab.isShown) {
                    // Immediately hide the FAB when scrolled down.
                    showFab = false
                    behavior.isAutoHideEnabled = false
                    actionFab.hide()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val behavior: FloatingActionButton.Behavior = actionFab.behaviour()

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (showFab && !actionFab.isShown) {
                        // Show FAB now that upward scroll has become idle.
                        actionFab.show()
                        behavior.isAutoHideEnabled = true
                    }
                }

                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }

    /**
     * Starts the url picker activity.
     */
    private fun startUrlPicker() {
        WebViewActivity.startUrlPickerForResult(
                this,
                startUrl = if (localCrawl) localUrl else webUrl,
                pickUrls = null,
                imagePicker = pickerType == PickerType.IMAGE_PICKER,
                maxUrls = maxUrls,
                resultCode = pickerType.ordinal)
    }

    /**
     * Cancels and running crawl and starts a new one.
     */
    private fun startCrawl() {
        if (!localCrawl && webUrl.isNullOrBlank()) {
            toast("Please click the search button to pick a root URL to crawl.")
            return
        }

        // Build controller that is passed to the image-crawler library.
        val controller = Controller
                .newBuilder()
                .platform(AndroidPlatform)
                .transforms(transformTypes)
                .rootUrl(webUrl)
                .maxDepth(crawlDepth)
                .diagnosticsEnabled(false)
                .build()

        // Start an asynchronous crawl.
        viewModel.startCrawlAsync(crawlStrategy, controller, this)

        // This call will switch the FAB icon from download to stop.
        updateViewStates()
    }

    /**
     * Called when the crawler finishes; resets states and updates views.
     */
    private fun crawlFinished() {
        // Tell adapter that the crawl has stopped
        // so that it can handle any items that are
        // in an incomplete state from a cancel action.
        adapter.crawlStopped()

        // Force the FAB to be shown.
        showFab(true)

        // Update all views to reflect stopped state.
        updateViewStates()
    }

    /**
     * Hook function called by view model when crawl has stopped.
     * The passed [status] indicates if the crawl completed normally
     * or with an exception. A cancelled crawl is considered to
     * have completed normally ([status] will be true).
     */
    override fun onCrawlCompleted(status: Boolean) {
        crawlFinished()
    }

    /**
     * Saves all properties so that they can be restored when
     * the activity is recreated.
     */
    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.let {
            it[KEY_CRAWL_URL] = webUrl
            it[KEY_CRAWL_STRATEGY] = crawlStrategy
            it[KEY_CRAWL_TRANSFORMS] = transformTypes
            it[KEY_CRAWL_LOCAL] = localCrawl
            it[KEY_CRAWL_DEPTH] = crawlDepth
            it[KEY_CRAWL_SPEED] = speedSeekBar.progress
            it[KEY_SPEED_BAR_STATE] = getSpeedBottomSheetState()
            it.putBundle(KEY_SELECTIONS, adapter.saveSelectionStates())
        }
        super.onSaveInstanceState(outState)
    }

    /**
     * Restores all properties after activity is recreated.
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState?.let {
            webUrl = it[KEY_CRAWL_URL, getString(R.string.default_web_view)]
            crawlStrategy = it[KEY_CRAWL_STRATEGY, ImageCrawler.Type.SEQUENTIAL_LOOPS]!!
            transformTypes = it[KEY_CRAWL_TRANSFORMS, mutableListOf()]!!
            localCrawl = it[KEY_CRAWL_LOCAL, false]!!
            crawlDepth = it[KEY_CRAWL_DEPTH, DEFAULT_CRAWL_DEPTH]!!
            speedSeekBar.progress = it[KEY_CRAWL_SPEED, DEFAULT_CRAWL_SPEED]!!
            setSpeedBottomSheetState(it[KEY_SPEED_BAR_STATE, STATE_COLLAPSED]!!)
            selectionBundle = it.getBundle(KEY_SELECTIONS)
        }
    }

    /**
     * Restores all persistent properties from shared preferences.
     */
    private fun restorePersistentState() {
        webUrl = Prefs[KEY_CRAWL_URL, getString(R.string.default_web_view)]
        crawlStrategy = Prefs[KEY_CRAWL_STRATEGY, ImageCrawler.Type.SEQUENTIAL_LOOPS]!!
        transformTypes = Prefs[KEY_CRAWL_TRANSFORMS, mutableListOf()]!!
        localCrawl = Prefs[KEY_CRAWL_LOCAL, false]!!
        crawlDepth = Prefs[KEY_CRAWL_DEPTH, DEFAULT_CRAWL_DEPTH]!!
        speedSeekBar.progress = Prefs[KEY_CRAWL_SPEED, DEFAULT_CRAWL_SPEED]!!
        setSpeedBottomSheetState(Prefs[KEY_SPEED_BAR_STATE, STATE_COLLAPSED]!!)
    }

    /**
     * Saves all persistent properties to shared preferences.
     */
    private fun savePersistentState() {
        Prefs[KEY_CRAWL_URL] = webUrl
        Prefs[KEY_CRAWL_STRATEGY] = crawlStrategy
        Prefs[KEY_CRAWL_TRANSFORMS] = transformTypes
        Prefs[KEY_CRAWL_LOCAL] = localCrawl
        Prefs[KEY_CRAWL_DEPTH] = crawlDepth
        Prefs[KEY_CRAWL_SPEED] = speedSeekBar.progress
        Prefs[KEY_SPEED_BAR_STATE] = getSpeedBottomSheetState()
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
        when {
            viewModel.isCrawlRunning -> {
                if (viewModel.crawlCancelled) {
                    val uri = ctx.getResourceUri(R.drawable.waiting)
                    actionFab.asyncLoad(url = uri.toString(), asGif = true)
                } else {
                    actionFab.setImageDrawable(ContextCompat.getDrawable(
                            this, android.R.drawable.ic_menu_close_clear_cancel))
                }
            }
            localCrawl -> {
                actionFab.setImageDrawable(ContextCompat.getDrawable(
                        this, android.R.drawable.stat_sys_download))
            }
            else -> {
                actionFab.setImageDrawable(searchDrawable)
            }
        }

        if (viewModel.isCrawlRunning || adapter.itemCount > 0) {
            mainActivityHintView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        } else {
            mainActivityHintView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        }

        toolbarTitle = getString(R.string.app_name) + if (localCrawl) " [LOCAL]" else ""
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
                        adapter.addAll(urls.map { Resource.fromUrl(it) }.toList())
                    }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Callback from the adapter when an action mode command is executed.
     */
    override fun onActionModeCommand(selections: List<Int>, menuActionId: Int): Boolean {
        if (menuActionId == R.id.action_delete) {
            val items = adapter.selectedItems
            items.forEach {
                AndroidPlatform.cache.remove(it.url, it.tag)
            }
        }

        // Returning false indicates that we didn't handle deleting the
        // items from the adapter, so the adapter will do that for us.
        return false
    }

    /**
     * Sets the current speed bottom sheet visibility state.
     */
    fun setSpeedBottomSheetState(state: Int) {
        BottomSheetBehavior.from(speedBottomSheet).state = state
    }

    /**
     * Returns the current speed bottom sheet visibility state.
     */
    fun getSpeedBottomSheetState(): Int {
        return BottomSheetBehavior.from(speedBottomSheet).state
    }

    /**
     * Toggles the speed bottom visibility state.
     */
    fun toggleSpeedBottomSheet() {
        with(BottomSheetBehavior.from(speedBottomSheet)) {
            state = when (state) {
                STATE_HIDDEN,
                STATE_COLLAPSED ->
                    STATE_EXPANDED
                else ->
                    STATE_HIDDEN
            }
        }
    }
}


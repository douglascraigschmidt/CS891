package edu.vanderbilt.crawler.ui.activities

import android.graphics.Color
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED
import android.support.design.widget.BottomSheetBehavior.STATE_HIDDEN
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import edu.vanderbilt.crawler.R
import edu.vanderbilt.crawler.adapters.ImageViewAdapter
import edu.vanderbilt.crawler.extensions.classNameToTitle
import edu.vanderbilt.imagecrawler.crawlers.framework.ImageCrawler
import edu.vanderbilt.imagecrawler.transforms.Transform
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*

/**
 * Creates the dynamic crawl and transform submenus.
 */

/**
 * Menu IDs to support dynamic creation of transform
 * and crawler sub-menus.
 */
private val CRAWLER_SUBMENU_GROUP_ID = 100
private val FILTER_SUBMENU_GROUP_ID = 200

/**
 * Creates the dynamic crawl strategy and transition submenus.
 */
fun MainActivity.handleCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_main, menu)

    // Add all supported crawlers to the crawler submenu.
    with(menu.findItem(R.id.crawlers).subMenu) {
        setGroupCheckable(CRAWLER_SUBMENU_GROUP_ID, true, false)
        setHeaderTitle("Crawler Strategy")

        ImageCrawler.Type.values().forEachIndexed { i, crawler ->
            val itemId = CRAWLER_SUBMENU_GROUP_ID + crawler.ordinal
            val title = crawler.toString().classNameToTitle()
            add(CRAWLER_SUBMENU_GROUP_ID, itemId, i, title).isCheckable = true
        }
    }

    return true
}

/**
 * Updates the all menu items to reflect the current settings.
 */
fun MainActivity.handlePrepareOptionsMenu(menu: Menu?): Boolean {
    menu?.findItem(R.id.actionLocalCrawl)?.isChecked = localCrawl

    // Set crawler menu title to show the currently selected crawler.
    var menuItem = menu?.findItem(R.id.crawlers)
    menuItem?.title = crawlStrategy.toString().classNameToTitle()

    // Update checked state of the selected crawler.
    menuItem?.subMenu?.run {
        setGroupCheckable(CRAWLER_SUBMENU_GROUP_ID, true, true)
        setHeaderTitle("Crawler Strategy")
        val crawlerItemId = CRAWLER_SUBMENU_GROUP_ID + crawlStrategy.ordinal
        findItem(crawlerItemId)?.isChecked = true
    }

    menu?.let { prepareTransformMenu(crawlStrategy, transformTypes, it) }

    // Display the current depth in the depth menu item title.
    menu?.findItem(R.id.actionCrawlDepth)?.title = "Crawl Depth: $crawlDepth"

    // If the adapter has any items, show the select all menu item.
    with(recyclerView.adapter as ImageViewAdapter) {
        menu?.findItem(R.id.action_select_all)?.isVisible = itemCount > 0
    }

    menu?.findItem(R.id.actionAdjustSpeed)?.let {
        when (BottomSheetBehavior.from(speedBottomSheet).state) {
            STATE_HIDDEN,
            STATE_COLLAPSED ->
                it.title = getString(R.string.show_speed_controller)
            else ->
                it.title = getString(R.string.hide_speed_controller)
        }
    }

    menuItem = menu?.findItem(R.id.actonImagePickerDemo)
    menuItem?.isVisible = false
    //menuItem?.isEnabled = !viewModel.isCrawlRunning

    return true
}

/**
 * Show transform menu and submenu items only if
 * the current crawl strategy supports at least
 * one transform.
 */
private fun prepareTransformMenu(crawlerType: ImageCrawler.Type,
                                 transformTypes: List<Transform.Type>,
                                 menu: Menu) {
    val menuItem = menu.findItem(R.id.transforms)
    val transforms = getSupportedTransforms(crawlerType)

    // Set visibility based on transforms being present.
    menuItem.isVisible = !transforms.isEmpty()

    if (menuItem.isVisible) {
        // Transforms exists, so add submenu and submenu items.
        with(menu.findItem(R.id.transforms).subMenu) {
            // First delete any existing submenu items.
            this.clear()

            // Add all supported transforms to the transforms submenu.
            setHeaderTitle("Transforms")
            setGroupCheckable(FILTER_SUBMENU_GROUP_ID, true, true)
            Transform.Type.values().forEachIndexed { i, transform ->
                val itemId = FILTER_SUBMENU_GROUP_ID + transform.ordinal
                val title = transform.toString().classNameToTitle()
                add(FILTER_SUBMENU_GROUP_ID, itemId, i, title).isCheckable = true
                findItem(itemId)?.isChecked = transformTypes.contains(transform)
            }
            true
        }

        // Set transform menu to show the number of selected transforms.
        menu.findItem(R.id.transforms)?.run {
            val count =
                    if (transformTypes.count() == 0)
                        "None"
                    else
                        transformTypes.count().toString()
            title = "Transforms ($count)"
        }
    }
}

/**
 * Handles all UI command input.
 */
fun MainActivity.handleOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
        R.id.actionLocalCrawl -> {
            item.isChecked = !item.isChecked
            localCrawl = item.isChecked
            toolbarTitle = getString(R.string.app_name) + if (localCrawl) " [LOCAL]" else ""
            true
        }
        R.id.actionCrawlDepth -> {
            crawlDepthDialog { depth ->
                crawlDepth = depth
            }
            true
        }
        R.id.crawlers, R.id.transforms ->
            true

        R.id.action_select_all -> {
            with(recyclerView.adapter as ImageViewAdapter) {
                startActionModeAndSelectAll()
            }
            true
        }
        R.id.actionAdjustSpeed -> {
            toggleSpeedBottomSheet()
            true
        }

        R.id.actonImagePickerDemo -> {
            if (viewModel.isCrawlRunning) {
                toast("Please cancel the current crawl operation and try again.")
            } else {
                runImagePickerDemo()
            }
            true
        }

        else -> when (item.groupId) {
            CRAWLER_SUBMENU_GROUP_ID -> {
                val ordinal = item.itemId - CRAWLER_SUBMENU_GROUP_ID
                crawlStrategy = ImageCrawler.Type.values()[ordinal]

                // A new crawler has been selected, so the current
                // transform list will be out of date. Update it.
                // Only added those transforms that were checked
                // in the previous transform list.
                val supportedList = getSupportedTransforms(crawlStrategy).toMutableList()
                transformTypes = supportedList
                        .filter { transformTypes.contains(it) }
                        .toList()

                true
            }
            FILTER_SUBMENU_GROUP_ID -> {
                val transforms = getSupportedTransforms(crawlStrategy)
                val ordinal = item.itemId - FILTER_SUBMENU_GROUP_ID
                val transform = transforms[ordinal]
                item.isChecked = !transformTypes.contains(transform)
                val mutableList = transformTypes.toMutableList()
                if (item.isChecked) {
                    mutableList.add(transform)
                } else {
                    mutableList.remove(transform)
                }
                // Force this shared preference to immediately update
                transformTypes = mutableList
                true
            }
            else -> {
                false
            }
        }
    }
}

/**
 * Helper method that uses reflection to call the [crawlStrategy]
 * getSupportedTransforms static method using reflection. The
 * strategy's list of supported transforms is returned or an empty
 * list if the crawler does not support transforms.
 */
fun getSupportedTransforms(crawlStrategy: ImageCrawler.Type): List<Transform.Type> {
    return try {
        val crawler = crawlStrategy.clazz.newInstance()
        val method = crawler.javaClass.getMethod("getSupportedTransforms")
        val list = method.invoke(crawler)
        @Suppress("UNCHECKED_CAST")
        list as List<Transform.Type>
    } catch (e: Exception) {
        mutableListOf()
    }
}

/**
 * Dialog invoked by the depth menu item that
 * allows user to input a crawl depth int value.
 */
fun MainActivity.crawlDepthDialog(block: (depth: Int) -> Unit) {
    alert {
        title = "Choose the maximum crawl depth"
        customView {
            val picker = numberPicker {
                layoutParams = ViewGroup.LayoutParams(200, 200)
                minValue = 1
                maxValue = 10
                padding = dip(20)
                value = crawlDepth
            }

            positiveButton("Ok") {
                block(picker.value)
            }
        }
    }.show()
}

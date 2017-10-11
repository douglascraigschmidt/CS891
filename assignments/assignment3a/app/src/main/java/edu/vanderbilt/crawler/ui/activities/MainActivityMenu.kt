package edu.vanderbilt.crawler.ui.activities

import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED
import android.support.design.widget.BottomSheetBehavior.STATE_HIDDEN
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
 * Creates the dynamic crawl and filter strategy submenus.
 */

// Support for dynamic creation of filter sub-menu.
private val CRAWLER_SUBMENU_GROUP_ID = 100
private val FILTER_SUBMENU_GROUP_ID = 200

/**
 * Creates the dynamic crawl strategy and transtion submenus.
 */
fun MainActivity.handleCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_main, menu)

    // Add all supported crawlers to the crawler submenu.
    var subMenu = menu.findItem(R.id.crawlers).subMenu
    subMenu.setGroupCheckable(CRAWLER_SUBMENU_GROUP_ID, true, false)
    subMenu.setHeaderTitle("Crawler Strategy")

    ImageCrawler.Type.values().forEachIndexed { i, crawler ->
        val itemId = CRAWLER_SUBMENU_GROUP_ID + crawler.ordinal
        val title = crawler.toString().classNameToTitle()
        subMenu.add(CRAWLER_SUBMENU_GROUP_ID, itemId, i, title).isCheckable = true
    }

    // Show transform menu and submenu items only if
    // the current crawl strategy supports at least
    // one transform.
    val menuItem = menu.findItem(R.id.transforms)
    val transforms = getSupportedTransforms(crawlStrategy)
    menuItem.isVisible = !transforms.isEmpty()
    if (menuItem.isVisible) {
        // Add all supported transforms to the transforms submenu.
        subMenu = menu.findItem(R.id.transforms).subMenu
        subMenu.setHeaderTitle("Transforms")
        subMenu.setGroupCheckable(FILTER_SUBMENU_GROUP_ID, true, true)

        Transform.Type.values().forEachIndexed { i, filter ->
            val itemId = FILTER_SUBMENU_GROUP_ID + filter.ordinal
            val title = filter.toString().classNameToTitle()
            subMenu.add(FILTER_SUBMENU_GROUP_ID, itemId, i, title).isCheckable = true
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
    var subMenu = menuItem?.subMenu
    subMenu?.setGroupCheckable(CRAWLER_SUBMENU_GROUP_ID, true, true)
    subMenu?.setHeaderTitle("Crawler Strategy")
    val crawlerItemId = CRAWLER_SUBMENU_GROUP_ID + crawlStrategy.ordinal
    subMenu?.findItem(crawlerItemId)?.isChecked = true

    // Set filter menu to show the number of selected filters.
    menuItem = menu?.findItem(R.id.transforms)
    val count =
            if (transformTypes.count() == 0)
                "None"
            else
                transformTypes.count().toString()
    menuItem?.title = "Transforms ($count)"

    // Update checked state of all filters.
    subMenu = menuItem?.subMenu
    subMenu?.setHeaderTitle("Filters")
    Transform.Type.values().forEach { filter ->
        val itemId = FILTER_SUBMENU_GROUP_ID + filter.ordinal
        subMenu?.findItem(itemId)?.isChecked = transformTypes.contains(filter)
    }

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

    return true
}

/**
 * Handles all UI command input.
 */
fun MainActivity.handleOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
        R.id.actionLocalCrawl -> {
            item.isChecked = !item.isChecked
            localCrawl = item.isChecked
            true
        }
        R.id.actionCrawlDepth -> {
            crawlDepthDialog { depth ->
                crawlDepth = depth
            }
            true
        }
        R.id.crawlers, R.id.transforms -> {
            true
        }
        R.id.action_select_all -> {
            with (recyclerView.adapter as ImageViewAdapter) {
                startActionModeAndSelectAll()
            }
            true
        }
        R.id.actionAdjustSpeed -> {
            toggleSpeedBottomSheet()
            true
        }

        else -> when (item.groupId) {
            CRAWLER_SUBMENU_GROUP_ID -> {
                val ordinal = item.itemId - CRAWLER_SUBMENU_GROUP_ID
                crawlStrategy = ImageCrawler.Type.values()[ordinal]
                true
            }
            FILTER_SUBMENU_GROUP_ID -> {
                val transforms = getSupportedTransforms(crawlStrategy)
                val ordinal = item.itemId - FILTER_SUBMENU_GROUP_ID
                val filter = transforms[ordinal]
                item.isChecked = !transformTypes.contains(filter)
                if (item.isChecked) {
                    transformTypes.add(filter)
                } else {
                    transformTypes.remove(filter)
                }
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
fun getSupportedTransforms(crawlStrategy: ImageCrawler.Type):
        List<Transform.Type> {
    return try {
        val crawler = crawlStrategy.clazz.newInstance()
        val method = crawler.javaClass.getMethod("getSupportedTransforms")
        val list = method.invoke(null)
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

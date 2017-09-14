package edu.vanderbilt.webcrawler.ui

import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import edu.vanderbilt.crawlers.framework.CrawlerFactory
import edu.vanderbilt.filters.FilterFactory
import edu.vanderbilt.webcrawler.R
import edu.vanderbilt.webcrawler.extensions.classNameToTitle
import org.jetbrains.anko.*

/**
 * Creates the dynamic crawl and filter strategy submenus.
 */

// Support for dynamic creation of filter sub-menu.
private val CRAWLER_SUBMENU_GROUP_ID = 100
private val FILTER_SUBMENU_GROUP_ID = 200

fun MainActivity.handleCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_main, menu)

    // Add all supported crawlers to the crawler submenu.
    var subMenu = menu.findItem(R.id.crawlers).subMenu
    subMenu.setGroupCheckable(CRAWLER_SUBMENU_GROUP_ID, true, false)
    subMenu.setHeaderTitle("Crawler Strategy")

    CrawlerFactory.Type.values().forEachIndexed { i, crawler ->
        val itemId = CRAWLER_SUBMENU_GROUP_ID + crawler.ordinal
        val title = crawler.toString().classNameToTitle()
        subMenu.add(CRAWLER_SUBMENU_GROUP_ID, itemId, i, title).isCheckable = true
    }

    // Add all supported filters to the filters submenu.
    subMenu = menu.findItem(R.id.filters).subMenu
    subMenu.setHeaderTitle("Filters")
    subMenu.setGroupCheckable(FILTER_SUBMENU_GROUP_ID, true, true)

    FilterFactory.Type.values().forEachIndexed { i, filter ->
        val itemId = FILTER_SUBMENU_GROUP_ID + filter.ordinal
        val title = filter.toString().classNameToTitle()
        subMenu.add(FILTER_SUBMENU_GROUP_ID, itemId, i, title).isCheckable = true
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
    menuItem = menu?.findItem(R.id.filters)
    val count =
            if (filterTypes.count() == 0)
                "None"
            else
                filterTypes.count().toString()
    menuItem?.title = "Filters ($count)"

    // Update checked state of all filters.
    subMenu = menuItem?.subMenu
    subMenu?.setHeaderTitle("Filters")
    FilterFactory.Type.values().forEach { filter ->
        val itemId = FILTER_SUBMENU_GROUP_ID + filter.ordinal
        subMenu?.findItem(itemId)?.isChecked = filterTypes.contains(filter)
    }

    // Display the current depth in the depth menu item title.
    menu?.findItem(R.id.actionCrawlDepth)?.title = "Crawl Depth: $crawlDepth"

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
        R.id.crawlers, R.id.filters -> {
            true
        }
        else -> when (item.groupId) {
            CRAWLER_SUBMENU_GROUP_ID -> {
                val ordinal = item.itemId - CRAWLER_SUBMENU_GROUP_ID
                crawlStrategy = CrawlerFactory.Type.values()[ordinal]
                true
            }
            FILTER_SUBMENU_GROUP_ID -> {
                val ordinal = item.itemId - FILTER_SUBMENU_GROUP_ID
                val filter = FilterFactory.Type.values()[ordinal]
                item.isChecked = !filterTypes.contains(filter)
                if (item.isChecked) {
                    filterTypes.add(filter)
                } else {
                    filterTypes.remove(filter)
                }
                true
            }
            else -> {
                false
            }
        }
    }
}

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


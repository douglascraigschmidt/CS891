package edu.vanderbilt.crawler.ui.screens.settings

import edu.vanderbilt.crawler.preferences.Preference
import edu.vanderbilt.crawler.preferences.Subscriber
import edu.vanderbilt.imagecrawler.crawlers.framework.ImageCrawler
import edu.vanderbilt.imagecrawler.transforms.Transform

/**
 * All settings that are saved/restored from shared
 * preferences and changeable from the SettingsDialogFragment.
 */
internal object Settings {
    private val DEFAULT_CRAWL_DEPTH = 3
    private val DEFAULT_LOCAL_CRAWL = false
    private val DEFAULT_CRAWL_SPEED = 100 // [0..100]%

    var crawlStrategy: ImageCrawler.Type by Preference(ImageCrawler.Type.SEQUENTIAL_LOOPS)
    var localCrawl: Boolean by Preference(DEFAULT_LOCAL_CRAWL)
    var crawlDepth: Int by Preference(DEFAULT_CRAWL_DEPTH)
    var transformTypes: List<Transform.Type> by Preference(listOf())
    var crawlSpeed: Int by Preference(DEFAULT_CRAWL_SPEED, "CrawlSpeedPreference")
    var debugLogging: Boolean by Preference(false)
}
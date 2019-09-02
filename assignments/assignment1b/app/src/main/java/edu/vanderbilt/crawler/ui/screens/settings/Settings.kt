package edu.vanderbilt.crawler.ui.screens.settings

import android.util.Range
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import edu.vanderbilt.crawler.preferences.Adapter
import edu.vanderbilt.crawler.preferences.Preference
import edu.vanderbilt.crawler.preferences.PreferenceProvider.prefs
import edu.vanderbilt.imagecrawler.crawlers.CrawlerType
import edu.vanderbilt.imagecrawler.transforms.Transform
import edu.vanderbilt.imagecrawler.utils.Options

/**
 * All settings that are saved/restored from shared
 * preferences and changeable from the SettingsDialogFragment.
 */
internal object Settings {
    /** Default pref values */
    val DEFAULT_CRAWL_STRATEGY = CrawlerType.SEQUENTIAL_LOOPS
    val DEFAULT_LOCAL_CRAWL = false
    val DEFAULT_CRAWL_DEPTH = 3
    val DEFAULT_TRANSFORM_TYPES = Transform.Type.values().toList()
    val DEFAULT_CRAWL_SPEED = 100 // [0..100]%
    val DEFAULT_DEBUG_LOGGING = false
    val DEFAULT_SPEED_BAR_STATE = STATE_COLLAPSED
    val DEFAULT_WEB_URL = Options.DEFAULT_WEB_URL
    val DEFAULT_GRID_SCALE = 6
    val DEFAULT_TRASPARANCEY = 10
    val DEFAULT_SHOW_PROGRESS = false
    val DEFAULT_SHOW_STATE = false
    val DEFAULT_SHOW_SIZE = false
    val DEFAULT_SHOW_THREAD = false

    /** Pref keys */
    val CRAWL_STRATEGY_PREF = "crawlStrategyPref"
    val LOCAL_CRAWL_PREF = "localCrawlPref"
    val CRAWL_DEPTH_PREF = "crawlDepthPref"
    val TRANSFORM_TYPES_PREF = "transformTypesPref"
    val CRAWL_SPEED_PREF = "crawlSpeedPref"
    val DEBUG_LOGGING_PREF = "debugLoggingPref"
    val SPEED_BAR_STATE_PREF = "speedBarStatePref"
    val WEB_URL_PREF = "webUrlPref"
    val GRID_SCALE_PREF = "gridViewScalePref"
    val TRANSPARENCY_PREF = "transparencyPref"
    val SHOW_PROGRESS_PREF = "showProgressPref"
    val SHOW_STATE_PREF = "showStatePref"
    val SHOW_SIZE_PREF = "showSizePref"
    val SHOW_THREAD_PREF = "showThreadPref"

    /** SeekBar min/max range. */
    val TRANSPARENCY_RANGE = Range(5, 50)
    val GRID_SCALE_RANGE = Range(2, 10)

    /** Pref values */
    var crawlStrategy: CrawlerType by Preference(DEFAULT_CRAWL_STRATEGY, CRAWL_STRATEGY_PREF)
    var localCrawl: Boolean by Preference(DEFAULT_LOCAL_CRAWL, LOCAL_CRAWL_PREF)
    var crawlDepth: Int by Preference(DEFAULT_CRAWL_DEPTH, CRAWL_DEPTH_PREF)
    var transformTypes: List<Transform.Type?> by Preference(DEFAULT_TRANSFORM_TYPES, TRANSFORM_TYPES_PREF)
    var crawlSpeed: Int by Preference(DEFAULT_CRAWL_SPEED, CRAWL_SPEED_PREF)
    var debugLogging: Boolean by Preference(DEFAULT_DEBUG_LOGGING, DEBUG_LOGGING_PREF)
    var speedBarState: Int by Preference(DEFAULT_SPEED_BAR_STATE, SPEED_BAR_STATE_PREF)
    var webUrl: String by Preference(DEFAULT_WEB_URL, WEB_URL_PREF)
    var viewTransparency: Int by Preference(DEFAULT_TRASPARANCEY, TRANSPARENCY_PREF)
    var viewScale: Int by Preference(DEFAULT_GRID_SCALE, GRID_SCALE_PREF)
    var showProgress: Boolean by Preference(DEFAULT_SHOW_PROGRESS, SHOW_PROGRESS_PREF)
    var showState: Boolean by Preference(DEFAULT_SHOW_STATE, SHOW_STATE_PREF)
    var showSize: Boolean by Preference(DEFAULT_SHOW_SIZE, SHOW_SIZE_PREF)
    var showThread: Boolean by Preference(DEFAULT_SHOW_THREAD, SHOW_THREAD_PREF)

    fun reset() {
        with(prefs.edit()) {
            clear()
            apply()
        }
    }
}

/**
 * All Range extensions shift the upper value to be relative to 0.
 */
val Range<Int>.progressMax get() = upper - lower
val Range<Int>.max get() = upper
val Range<Int>.min get() = lower
val Range<Int>.size get() = upper - lower + 1
fun Range<Int>.toProgress(value: Int) = value - lower
fun Range<Int>.fromProgress(value: Int) = lower + value
fun Range<Int>.scale(t: Int): Range<Int> {
    return Range(t * lower, t * upper)
}

fun Range<Int>.encode(): String = "$lower,$upper"

/**
 * Preference adapter class for Range<Int> required since
 * PreferenceObserver can't automatically handle complex objects.
 */
class RangeAdapter : Adapter<Range<Int>> {
    override fun encode(value: Range<Int>) = value.encode()

    override fun decode(string: String): Range<Int> {
        val split = string.split(",")
        if (split.size != 2) {
            error("Unable to decode shared preference Range<Int> value: $string")
        }
        return Range(split[0].toInt(), split[1].toInt())
    }
}

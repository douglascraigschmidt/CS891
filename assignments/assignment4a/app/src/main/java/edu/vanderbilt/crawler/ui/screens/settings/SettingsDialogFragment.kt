package edu.vanderbilt.crawler.ui.screens.settings

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.widget.RxAdapterView
import com.jakewharton.rxbinding2.widget.RxRadioGroup
import com.jakewharton.rxbinding2.widget.RxSeekBar
import edu.vanderbilt.crawler.R
import edu.vanderbilt.crawler.preferences.CompositeUnsubscriber
import edu.vanderbilt.crawler.preferences.ObservablePreference
import edu.vanderbilt.crawler.preferences.Subscriber
import edu.vanderbilt.crawler.ui.screens.settings.adapters.CrawlDepthAdapter
import edu.vanderbilt.crawler.ui.screens.settings.adapters.EnumSpinnerAdapter
import edu.vanderbilt.crawler.ui.screens.settings.adapters.TransformsAdapter
import edu.vanderbilt.imagecrawler.crawlers.framework.ImageCrawler
import kotlinx.android.synthetic.main.settings_dialog_fragment.*

/**
 * Application developer options fragment that shows a list of tunable
 * options in a modal bottom sheet.
 *
 * To show this bottom sheet:
 * <pre>
 * SettingsDialogFragment.newInstance().show(getSupportFragmentManager(), "dialog");
 * </pre>
 * You activity (or fragment) needs to implement [SettingsDialogFragment.Listener].
 */
class SettingsDialogFragment : BottomSheetDialogFragment() {
    companion object {
        fun newInstance(): SettingsDialogFragment {
            return SettingsDialogFragment()
        }
    }

    /** Observe all  crawl speed preference changes. */
    private val compositeUnsubscriber = CompositeUnsubscriber()
    private var crawlSpeed: Int by ObservablePreference(
            default = 100,
            name = "CrawlSpeedPreference",
            subscriber = object : Subscriber<Int> {
                override val subscriber: (Int) -> Unit
                    get() = { speedSeekBar.progress = it }

                override fun unsubscribe(callback: () -> Unit) {
                    compositeUnsubscriber.add(callback)
                }
            })

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // create ContextThemeWrapper from the original Activity Context with the custom theme
        val contextThemeWrapper = ContextThemeWrapper(activity, R.style.SettingsThemeDark)

        // clone the inflater using the ContextThemeWrapper
        val localInflater = inflater!!.cloneInContext(contextThemeWrapper)
        return localInflater!!.inflate(R.layout.settings_dialog_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        configureTransforms()
        configureCrawlerStrategy()
        configureCrawlerMaxDepth()
        configureCrawlerLocation()
        configureDebugLogging()
        configureThreadSpeed()
    }

    private fun configureTransforms() {
        // Let the adapter handle adding child views.
        TransformsAdapter.buildAdapter(transformsLayoutView)
    }

    private fun configureCrawlerMaxDepth() {
        // Setup adapter and current selection.
        val adapter = CrawlDepthAdapter(
                ContextThemeWrapper(activity, R.style.SettingsThemeDark))
        settingsCrawlDepth.adapter = adapter
        settingsCrawlDepth.setSelection(
                CrawlDepthAdapter.getPositionForValue(Settings.crawlDepth))

        // Use Rx to filter item selections and save changed value to shared preference.
        RxAdapterView.itemSelections(settingsCrawlDepth)
                .map<Int> { adapter.getItem(it) }
                .filter { it != Settings.crawlDepth }
                .subscribe { Settings.crawlDepth = it }
    }

    private fun configureCrawlerStrategy() {
        // Setup adapter and current selection.
        val adapter = EnumSpinnerAdapter(
                ContextThemeWrapper(activity, R.style.SettingsThemeDark),
                ImageCrawler.Type::class.java)
        settingsCrawlStrategy.adapter = adapter
        settingsCrawlStrategy.setSelection(
                adapter.getPositionForValue(Settings.crawlStrategy))

        // Use Rx to filter item selections and save changed value to shared preference.
        RxAdapterView.itemSelections(settingsCrawlStrategy)
                .map<ImageCrawler.Type> { adapter.getItem(it) }
                .filter { it != Settings.crawlStrategy }
                .subscribe { Settings.crawlStrategy = it }
    }

    private fun configureCrawlerLocation() {
        val checkedId = if (Settings.localCrawl) {
            settingsLocalCrawl.id
        } else {
            settingsRemoteCrawl.id
        }
        settingsImageSourceRadioGroup.check(checkedId)

        RxRadioGroup.checkedChanges(settingsImageSourceRadioGroup)
                .subscribe { Settings.localCrawl = (it == settingsLocalCrawl.id) }
    }

    private fun configureDebugLogging() {
        settingsDebugOutput.isChecked = Settings.debugLogging
        settingsDebugOutput.setOnCheckedChangeListener { buttonView, isChecked ->
            Settings.debugLogging = isChecked
        }
    }

    private fun configureThreadSpeed() {
        speedSeekBar.progress = Settings.crawlSpeed
        RxSeekBar.userChanges(speedSeekBar)
                .subscribe {
                    Settings.crawlSpeed = it
                }
    }
}

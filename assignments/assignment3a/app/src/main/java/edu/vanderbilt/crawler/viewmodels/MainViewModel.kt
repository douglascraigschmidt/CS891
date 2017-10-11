package edu.vanderbilt.crawler.viewmodels

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.support.annotation.MainThread
import edu.vanderbilt.crawler.extensions.minmax
import edu.vanderbilt.crawler.platform.AndroidPlatform.cache
import edu.vanderbilt.crawler.utils.KtLogger
import edu.vanderbilt.crawler.utils.info
import edu.vanderbilt.imagecrawler.crawlers.framework.ImageCrawler
import edu.vanderbilt.imagecrawler.platform.Cache
import edu.vanderbilt.imagecrawler.platform.Controller
import edu.vanderbilt.imagecrawler.platform.FileCache
import edu.vanderbilt.imagecrawler.utils.Options
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

class MainViewModel : ViewModel(), FileCache.Observer, KtLogger {
    /**
     * Crawler support properties.
     */
    private var crawler: ImageCrawler? = null
    private var hashMap: ConcurrentHashMap<Cache.Item, Resource> = ConcurrentHashMap()
    private val cachedResources = MutableLiveData<List<Resource>>()
    val isCrawlRunning: Boolean
        get() = crawler != null
    var crawlCancelled: Boolean = false
    var crawlSpeed: Int
        get() = (cache.delay + 100).minmax(0, 100)
        set(speed) {
            cache.delay = (100 - speed).minmax(0, 100)
        }

    /**
     * Memory safe callback used for when crawl completes.
     */
    interface CrawlCompletedListener {
        fun onCrawlCompleted(status: Boolean)
    }

    var crawlCompletedListener: WeakReference<CrawlCompletedListener>? = null

    fun setCrawlCompletedListener(listener: CrawlCompletedListener) {
        crawlCompletedListener = WeakReference(listener)
    }

    /**
     * Called to subscribe to live data. Sets the live data observer and
     * also adds this calls as a cache watcher. Since the call to startWatching
     * immediately calls back the observer with each of the existing cached
     * items, the call is performed in a background thread so that the call
     * will not block the UI thread.
     */
    fun subscribe(lifecycleOwner: LifecycleOwner,
                  observer: Observer<List<Resource>>,
                  block: () -> Unit) {
        cachedResources.observe(lifecycleOwner, observer)
        cache.startWatching(this@MainViewModel)
        block()
    }

    /**
     * Call to unsubscribe a previously subscribed [observer].
     */
    fun unsubscribe(observer: Observer<List<Resource>>) {
        cachedResources.removeObserver(observer)
    }

    /**
     * Starts an image crawl.
     */
    fun startCrawlAsync(strategy: ImageCrawler.Type,
                        controller: Controller,
                        listener: CrawlCompletedListener) {

        crawlCompletedListener = WeakReference(listener)

        // First ensure that if this crawler is already
        // running that it's stopped before restarting.
        stopCrawl()
        crawlCancelled = false

        // Clear the mutable live data instance, cache, and
        // local hashMap.
        clearAll()

        // Build a new crawler using the specified strategy.
        Options.diagnosticsEnabled = true
        crawler = ImageCrawler.Factory.newCrawler(strategy, controller)

        // Make an ImageCrawlerAsync object via the factory method.
        // Start running the test.
        var status: Boolean

        doAsync {
            try {
                // Clear out all values from the cache.
                // and from the mutable live data.
                cache.startWatching(this@MainViewModel)

                crawler?.run() ?: info("Crawler was not started.")
                info("Crawler finished normally.")
                status = true
            } catch (e: Exception) {
                error("Crawler failed with exception $e")
            }

            crawler = null
            crawlCancelled = false

            uiThread {
                if (crawler != null || crawlCancelled) {
                    throw Exception("Crawler properties not clear!!! This should never happen!!!")
                }
                crawlCompletedListener?.get()?.onCrawlCompleted(status)
            }
        }
    }

    /**
     * Mutable live data must be cleared on the main thread
     * when using synchronous value property. The call to
     * clear the mutable live data will be propagated to the
     * app layer which will clear the dependent UI elements
     * so there is no need to rely on event callbacks from
     * the cache as items are deleted.
     */
    @MainThread
    private fun clearAll() {
        cachedResources.value = null
        hashMap.clear()
        cache.clear()
        cache.startWatching(this@MainViewModel)
    }

    /**
     * Forces the crawler to stop.
     */
    private fun stopCrawl() {
        if (isCrawlRunning) {
            cache.stopWatching(this)
            crawler?.stopCrawl()
        }
    }

    /**
     * Sets the cancelled flag and forces the
     * crawl to stop (if it was running.
     */
    fun cancelCrawl() {
        if (isCrawlRunning && !crawlCancelled) {
            crawlCancelled = true
            stopCrawl()
        }
    }

    /**
     * Updates the resource list to reflect this event change and
     * posts the updated list to application via the Android Architecture
     * LiveData mechanism.
     */
    override fun event(operation: Cache.Operation, item: Cache.Item, progress: Float) {
        // Map the operation and item to an application resource.
        if (operation == Cache.Operation.DELETE) {
            hashMap.remove(item)
        } else {
            val resource = Resource.fromFileObserver(item, operation, progress)

            val oldResource = hashMap.putIfAbsent(item, resource)
            oldResource?.let {
                hashMap.replace(item, it, resource)
            }
        }

        // Unfortunately, there is no was to copy a concurrent hash map
        // to an immutable list without protecting the operation with a
        // synchronize guard.
        synchronized(hashMap) {
            val mutableList = hashMap.values.toMutableList()
            mutableList.sortBy { it.timestamp }
            cachedResources.postValue(mutableList)
        }
    }
}

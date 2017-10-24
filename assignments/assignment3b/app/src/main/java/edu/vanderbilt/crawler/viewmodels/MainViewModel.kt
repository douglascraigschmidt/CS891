package edu.vanderbilt.crawler.viewmodels

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.support.annotation.MainThread
import edu.vanderbilt.crawler.platform.AndroidCache
import edu.vanderbilt.crawler.utils.KtLogger
import edu.vanderbilt.crawler.utils.info
import edu.vanderbilt.crawler.utils.warn
import edu.vanderbilt.imagecrawler.crawlers.framework.ImageCrawler
import edu.vanderbilt.imagecrawler.platform.Cache
import edu.vanderbilt.imagecrawler.platform.Controller
import edu.vanderbilt.imagecrawler.utils.Options
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Future

class MainViewModel : ViewModel(), Cache.Observer, KtLogger {
    // Crawl state posted as CrawlProgress
    enum class CrawlState {
        IDLE,
        RUNNING,
        CANCELLED,
        COMPLETED,
        FAILED
    }

    data class CrawlProgress(val status: CrawlState, val millisecs: Long = 0L)

    /**
     * Crawler support properties.
     */
    private var crawler: ImageCrawler? = null

    /** Cache events live data feed. */
    private val cacheContentsFeed = MutableLiveData<List<Resource>>()

    /** Crawl status live data feed. */
    private var crawlProgressFeed = MutableLiveData<CrawlProgress>()

    /** Support collections for cached resources live data feed. */
    private var threads = ConcurrentHashMap<Long, Int>()
    private var hashMap = ConcurrentHashMap<Cache.Item, Resource>()

    // Publicly available properties.
    val isCrawlRunning: Boolean
        get() = crawler != null
    var crawlCancelled = false
    // Delegate crawl speed to cache field.
    var crawlSpeed
        get() = AndroidCache.crawlSpeed
        set(speed) {
            AndroidCache.crawlSpeed = speed
        }
    var maxDelay = 500 // milliseconds

    /** Kill thread that send cache events after a cancel has been issued. */
    val toughLove = true

    /**
     * Called to subscribe to live data. Sets the live data observer and
     * also adds this calls as a cache watcher. Since the call to startWatching
     * immediately calls back the observer with each of the existing cached
     * items, the call is performed in a background thread so that the call
     * will not block the UI thread.
     */
    fun subscribe(lifecycleOwner: LifecycleOwner,
                  cacheObserver: Observer<List<Resource>>,
                  crawlObserver: Observer<CrawlProgress>,
                  block: () -> Unit) {
        cacheContentsFeed.observe(lifecycleOwner, cacheObserver)
        crawlProgressFeed.observe(lifecycleOwner, crawlObserver)
        AndroidCache.startWatching(this@MainViewModel, true)
        block()
    }

    /**
     * Call to unsubscribe a previously subscribed
     * crawl progress [observer].
     */
    fun unsubscribeCacheObserver(observer: Observer<List<Resource>>) {
        cacheContentsFeed.removeObserver(observer)
    }

    /**
     * Call to unsubscribe a previously subscribed cache [observer].
     */
    fun unsubscribeCrawlObserver(observer: Observer<CrawlProgress>) {
        crawlProgressFeed.removeObserver(observer)
    }

    /**
     * Starts an image crawl.
     */
    fun startCrawlAsync(strategy: ImageCrawler.Type,
                        controller: Controller) {
        // First ensure that if this crawler is already
        // running that it's stopped before restarting.
        stopCrawl()
        crawlCancelled = false

        // Clear the mutable live data instance, cache, and
        // local hashMap.
        clearAll()

        // Build a new crawler using the specified strategy.
        Options.debug = true
        crawler = ImageCrawler.Factory.newCrawler(strategy, controller)

        val asyncProgress = startProgressAsync(true)

        doAsync {
            try {
                crawler?.run() ?: info("Crawler was not started.")
                info("Crawler finished normally.")
            } catch (e: Exception) {
                warn("Crawler finished with exception: $e")
            } finally {
                // Always clear properties and notify UI
                // that crawl has finished.
                asyncProgress.cancel(true)
                crawler = null
                crawlCancelled = false

                // Update all hashMap entries to have a CLOSED state to signal
                // that UI that they should consider these values all finished
                synchronized(hashMap) {
                    hashMap.forEach { (key, value) ->
                        hashMap.put(key, value.copy(state = Resource.State.CLOSE))
                    }
                    val mutableList = hashMap.values.toMutableList()
                    mutableList.sortBy { it.timestamp }
                    cacheContentsFeed.postValue(mutableList)
                }
            }
            crawlProgressFeed.postValue(CrawlProgress(CrawlState.COMPLETED))
        }
    }

    fun startProgressAsync(usePost: Boolean): Future<Unit> {
        // Start asynchronous timer. All progress events are sent in the
        // UI thread ensure accurate timing and to also to preserve event
        // ordering.
        return doAsync {
            val startTime = System.currentTimeMillis()
            try {
                while (crawler != null && !crawlCancelled) {
                    // Calculate and set the elapsed time in the UI thread
                    // to avoid inaccuracies caused by the inherent delay
                    // that would result from posting the elapsed time from
                    // this background thread.
                    if (usePost) {
                        crawlProgressFeed.postValue(
                                MainViewModel.CrawlProgress(MainViewModel.CrawlState.RUNNING,
                                                            System.currentTimeMillis() - startTime))
                    } else {
                        uiThread {
                            crawlProgressFeed.value =
                                    MainViewModel.CrawlProgress(MainViewModel.CrawlState.RUNNING,
                                                                System.currentTimeMillis() - startTime)
                        }
                    }
                    // Keep this sleep relatively short so that the status
                    // check will be responsive when a crawl
                    Thread.sleep(50)
                }
            } catch (e: Exception) {
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
        AndroidCache.stopWatching(this)
        cacheContentsFeed.value = mutableListOf()
        hashMap.clear()
        threads.clear()
        AndroidCache.clear()
        AndroidCache.startWatching(this, false)
    }

    /**
     * Forces the crawler to stop.
     */
    private fun stopCrawl() {
        if (isCrawlRunning) {
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
            crawlProgressFeed.postValue(CrawlProgress(CrawlState.CANCELLED, 0L))
        }
    }

    /**
     * Adds an artificial delay to all computationally
     * intense operations. Currently moved to cache
     * to make it possible to synchronization to observer
     * list without drastically degrading the performance
     * of threads. This is a kludge until I refactor the
     * cancel logic.
     */
    fun addSimulatedDelay(operation: Cache.Operation) {
        // Only delay computationally intense operations.
        when (operation) {
            Cache.Operation.READ,
            Cache.Operation.DOWNLOAD,
            Cache.Operation.WRITE,
            Cache.Operation.TRANSFORM -> {
                // Convert speed to a delay between 0 and MAX_DELAY_MS.
                var duration = ((100 - crawlSpeed) / 100f * maxDelay).toLong()
                // Sleep in intervals so that cancelling is responsive.
                val interval = Math.min(10, duration)
                do {
                    ImageCrawler.throwExceptionIfCancelled()
                    Thread.sleep(interval)
                    duration -= interval
                } while (duration > 0)
            }
            else -> {/* No delay */
            }
        }
    }

    /**
     * Updates the resource list to reflect this event change and
     * posts the updated list to application via the Android Architecture
     * LiveData mechanism.
     */
    override fun event(operation: Cache.Operation, item: Cache.Item, progress: Float) {
        // Map the operation and item to an application resource.
        if (crawlCancelled && toughLove) {
            //Thread.currentThread().interrupt()
            throw CancellationException("Tough Love!")
        }
        if (operation == Cache.Operation.DELETE) {
            hashMap.remove(item)
        } else {
            // Slow down processing if processing speed is below 100%.
            // Temporarily delegated to cache to allow cache to wrap
            // all access to observer list with synchronized block.
            // This allows a cancel operation to remove this viewModel
            // as a cache watcher so that it won't receive events from
            // threads that are hard to cancel.
//            if (crawlSpeed < 100) {
//                addSimulatedDelay(operation)
//            }

            val threadId = threads.getOrPut(Thread.currentThread().id, { threads.size + 1 })
            val resource = Resource.fromFileObserver(item,
                                                     operation,
                                                     progress,
                                                     threadId)

            val oldResource = hashMap.putIfAbsent(item, resource)
            oldResource?.let {
                hashMap.replace(item, it, resource)
            }
        }

        // Unfortunately, there is no way to copy a concurrent hash map
        // to an immutable list without protecting the operation with a
        // synchronize guard.
        synchronized(hashMap) {
            val mutableList = hashMap.values.toMutableList()
            mutableList.sortBy { it.timestamp }
            cacheContentsFeed.postValue(mutableList)
        }
    }
}

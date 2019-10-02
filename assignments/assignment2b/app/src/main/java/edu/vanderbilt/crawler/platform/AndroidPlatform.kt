package edu.vanderbilt.crawler.platform

import edu.vanderbilt.crawler.app.App
import edu.vanderbilt.crawler.utils.KtLogger
import edu.vanderbilt.crawler.utils.debug
import edu.vanderbilt.imagecrawler.platform.Cache
import edu.vanderbilt.imagecrawler.platform.Platform
import edu.vanderbilt.imagecrawler.platform.Platform.*
import edu.vanderbilt.imagecrawler.platform.PlatformImage
import java.io.InputStream
import java.net.URL

/**
 * Java Platform helper methods.
 */
object AndroidPlatform : Platform, KtLogger {
    /**
     * Assets URL prefix string for comparisons.
     * NOTE: Android requires 3 /// characters.
     */
    @JvmField
    val ASSETS_URI_PREFIX = "file:///android_assets"

    /**
     * Returns the platform dependant [cache].
     */
    override fun getCache(): Cache = AndroidCache

    /**
     * Returns a new Java platform bitmap created from [inputStream]
     */
    override fun newImage(inputStream: InputStream, item: Cache.Item):
            PlatformImage = AndroidImage(inputStream, item)

    /**
     * All debug logging is consolidated through this method.
     */
    override fun log(msg: String, vararg args: Any?) = debug("DIAGNOSTICS: ${if (args.isEmpty()) msg else msg.format(*args)}")

    /**
     * Creates an input stream for the passed [uri]. This method supports
     * both normal URLs and any URL located in the application assets.
     */
    override fun mapUriToInputStream(uri: String): InputStream? {
        val assetsPrefix = AndroidPlatform.ASSETS_URI_PREFIX + "/"

        // Check if this input stream is for an image asset.
        val item = cache.getItem(uri, null)

        // Get the input stream.
        return when {
            uri.startsWith(assetsPrefix) -> {
                val path = "$LOCAL_WEB_PAGES_DIR_NAME/${uri.removePrefix(assetsPrefix)}"
                val inputStream = App.instance.assets.open(path)

                // If the stream is for a cached image item, then wrap the
                // stream in an ObserverInputStream which will provide feedback
                // to the app UI.
                if (item == null) {
                    // Must be a web page uri, just return a normal input stream.
                    inputStream
                } else {
                    // Must be an image stream, return the wrapped observer stream.
                    cache.ObserverInputStream(
                            inputStream,
                            Cache.Operation.DOWNLOAD,
                            item,
                            inputStream.available())
                }
            }
            else -> {
                if (item == null) {
                    // Must be a web page URL so return a normal input stream.
                    URL(uri).openStream()
                } else {
                    // Must be an image URL so return the wrapped observer stream.
                    cache.ObserverInputStream(
                            URL(uri).openStream(),
                            Cache.Operation.DOWNLOAD,
                            item,
                            URL(uri).openConnection().contentLength)
                }
            }
        }
    }
}

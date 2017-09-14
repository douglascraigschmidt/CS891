package edu.vanderbilt.webcrawler.platform

import edu.vanderbilt.platform.Device
import edu.vanderbilt.platform.Platform
import edu.vanderbilt.platform.PlatformImage
import edu.vanderbilt.webcrawler.App
import edu.vanderbilt.webcrawler.extensions.getImageBytes
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.URL

/**
 * Java Platform helper methods.
 */
class AndroidPlatform : Platform {
    companion object {
        /** The path to the image directory. */
        val DOWNLOAD_DIR_NAME = "downloaded-images"

        /** Assets URL prefix string for comparisons. */
        val ASSETS_URI_PREFIX = "file::///android_assets"
    }

    /**
     * Returns a new Java platform bitmap created from [imageData] and
     * with it's source URL set to [url].
     */
    override fun newImage(url: URL, imageData: ByteArray): PlatformImage {
        return AndroidImage(url, imageData, this)
    }

    /**
     * Return the path to the external storage directory.
     */
    override fun getCacheDirPath(): String {
        return "${App.instance.filesDir.absolutePath}/$DOWNLOAD_DIR_NAME"
    }

    override fun getUrlList(): MutableList<URL> {
        TODO("not implemented")
    }

    /**
     * Creates an input stream for the passed [uri]. This method supports
     * both normal URLs and any URL located in the application assets.
     *
     * @param uri Any URL including an asset url (file::///android_assets/...)
     * @return An input stream.
     * @throws IOException
     */
    override fun getInputStream(uri: String): InputStream? {
        val assetsPrefix = AndroidPlatform.ASSETS_URI_PREFIX + "/"
        return when {
            uri.startsWith(assetsPrefix) ->
                App.instance.assets.open(uri.removePrefix(assetsPrefix))
            else -> URL(uri).openStream()
        }
    }

    /**
     * Download the contents found at the given URL and return them as
     * a raw byte array. This method never because the URL extension
     * getImageBytes will return a placeholder's bytes if the image
     * can't be downloaded.
     */
    override fun downloadContent(url: URL): ByteArray {
        //printDiagostics("DOWNLOADING content: $url")

        try {
            val out = ByteArrayOutputStream()
            var bytesCopied: Long = 0
            getInputStream(url.toString())?.use {
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var bytes = it.read(buffer)
                while (bytes >= 0) {
                    out.write(buffer, 0, bytes)
                    bytesCopied += bytes
                    bytes = it.read(buffer)
                    out.flush()
                }
                //it.copyTo(outputStream)
            }
            out.close()
            val bytes = out.toByteArray()
            printDiagostics("DOWNLOADED ${bytes?.count() ?: "0"} bytes for URL: $url")
            if (bytes == null) {
                System.out.println("downloadContent return empty ByteArray!")
                return ByteArray(1){0}
            }
            return bytes
        } catch (e: Exception) {
            printDiagostics("AndroidPlatform:downloadContent() - Error downloading url $url: $e")
            throw throw(e)
            // TODO(monte): Maybe return empty bytes so that the app can continue
            // loading other images ...
            // System.out.prinln("downloadContent return empty ByteArray!")
            //return ByteArray(1){0}
        }
    }

    /**
     * Helper to only output debug if diagnostics is enabled.
     * or if the options class has not yet been created.
     */
    fun printDiagostics(msg: String) {
        if (Device.options().diagnosticsEnabled) {
            System.out.println(msg)
        }
    }

    /**
     * Returns a relative file path form of the @a url.
     */
    override fun mapUrlToRelativeFilePath(url: URL): String {
        return when {
            url.toString().startsWith(ASSETS_URI_PREFIX) ->
                    url.toString().removePrefix(ASSETS_URI_PREFIX)
            else -> url.host + url.path
        }
    }

    /**
     * Returns the URI authority for a local web crawl.
     */
    override fun getAuthority(): String {
        return getLocalRootUri().authority
    }

    /**
     * @returns the URI scheme for a local web crawl.
     */
    override fun getScheme(): String {
        return getLocalRootUri().scheme
    }

    /**
     * Returns the platform root URI for a local web crawl.
     */
    override fun getLocalRootUri(): URI {
        try {
            return URI("file:///android_assets/")
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}

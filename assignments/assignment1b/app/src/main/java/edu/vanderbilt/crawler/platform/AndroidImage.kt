package edu.vanderbilt.crawler.platform

import android.graphics.Bitmap
import edu.vanderbilt.crawler.R
import edu.vanderbilt.crawler.app.App
import edu.vanderbilt.crawler.utils.BitmapUtils
import edu.vanderbilt.crawler.utils.KtLogger
import edu.vanderbilt.imagecrawler.crawlers.ImageCrawler
import edu.vanderbilt.imagecrawler.platform.Cache.Item
import edu.vanderbilt.imagecrawler.platform.Cache.Operation.TRANSFORM
import edu.vanderbilt.imagecrawler.platform.PlatformImage
import edu.vanderbilt.imagecrawler.transforms.Transform
import edu.vanderbilt.imagecrawler.utils.Filters
import org.jetbrains.anko.dimen
import java.io.InputStream
import java.io.OutputStream

/**
 * Stores platform-specific meta-data about an Image and also provides
 * methods for common image- and file-related tasks.  This
 * implementation is specific to the Android platform.
 */
class AndroidImage : PlatformImage, KtLogger {
    companion object {
        /** Dimensions representing how large the scaled image should be. */
        private val IMAGE_WIDTH = App.instance.dimen(R.dimen.url_grid_image_size)
        private val IMAGE_HEIGHT = IMAGE_WIDTH

    }

    lateinit var item: Item

    var image: Bitmap? = null
    var size = 0

    constructor(inputStream: InputStream, item: Item) {
        setImage(inputStream, item)
    }

    constructor(bitmap: Bitmap) {
        image = bitmap
        size = bitmap.byteCount
    }

    override fun setImage(inputStream: InputStream, item: Item) {
        this.item = item
        size = inputStream.available()
        image = BitmapUtils.decodeSampledBitmapFromStream(
                inputStream, IMAGE_WIDTH, IMAGE_HEIGHT)
        size = image?.byteCount ?: 0
    }

    /**
     * Returns size of image.
     */
    override fun size(): Int = size

    /**
     * Write the image to the [outputStream].
     */
    override fun writeImage(outputStream: OutputStream) {
        image?.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
    }

    /**
     * Apply a grayscale filter to the @a imageEntity and return it.
     */
    override fun applyTransform(type: Transform.Type,
                                newItem: Item): PlatformImage? {

        ImageCrawler.throwExceptionIfCancelled()

        return when (type) {
            Transform.Type.GRAY_SCALE_TRANSFORM -> grayScale(newItem)
            Transform.Type.SEPIA_TRANSFORM -> sepiaImage(newItem)
            Transform.Type.TINT_TRANSFORM -> tint(newItem)
        }
    }

    private fun grayScale(newItem: Item): PlatformImage? {
        // Bail out if something is wrong with the image.
        if (image == null) {
            return null
        }

        val image = image!!

        val height = image.height
        val width = image.width
        val pixels = IntArray(width * height)
        image.getPixels(pixels, 0, width, 0, 0, width, height)

        var lastProgress = 0
        Filters.grayScale(pixels, image.hasAlpha()) {
            lastProgress = updateProgress(newItem, it, lastProgress)
        }

        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).let { bitmap ->
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            AndroidImage(bitmap)
        }
    }

    private fun tint(newItem: Item): PlatformImage? {
        // Bail out if something is wrong with the image.
        if (image == null) {
            return null
        }

        val image = image!!

        val height = image.height
        val width = image.width
        val pixels = IntArray(width * height)
        image.getPixels(pixels, 0, width, 0, 0, width, height)

        var lastProgress = 0
        Filters.tint(pixels, image.hasAlpha(), .9f, 0f, 0f) {
            lastProgress = updateProgress(newItem, it, lastProgress)
        }

        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).let { bitmap ->
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            AndroidImage(bitmap)
        }
    }

    private fun updateProgress(newItem: Item, progress: Float, lastProgress: Int): Int {
        val percent = (progress * 100).toInt()
        if (percent > lastProgress) {
            newItem.progress(TRANSFORM, progress, 0)
        }
        return percent
    }

    private fun sepiaImage(newItem: Item): PlatformImage? {
        // Bail out if something is wrong with the image.
        if (image == null) {
            return null
        }

        val image = image!!

        val height = image.height
        val width = image.width

        val pixels = IntArray(width * height)
        image.getPixels(pixels, 0, width, 0, 0, width, height)

        var lastProgress = 0
        Filters.sepia(pixels, image.hasAlpha()) {
            lastProgress = updateProgress(newItem, it, lastProgress)
        }

        //val sepiaBitmap = image.copy(image.config, true)

        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).let { bitmap ->
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            AndroidImage(bitmap)
        }
    }
}

package edu.vanderbilt.crawler.platform

import android.graphics.Bitmap
import android.graphics.Color
import edu.vanderbilt.crawler.R
import edu.vanderbilt.crawler.app.App
import edu.vanderbilt.crawler.utils.BitmapUtils
import edu.vanderbilt.crawler.utils.KtLogger
import edu.vanderbilt.imagecrawler.crawlers.ImageCrawler
import edu.vanderbilt.imagecrawler.platform.Cache
import edu.vanderbilt.imagecrawler.platform.PlatformImage
import edu.vanderbilt.imagecrawler.transforms.Transform
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

    lateinit var item: Cache.Item

    var image: Bitmap? = null
    var size = 0

    constructor(inputStream: InputStream, item: Cache.Item) {
        setImage(inputStream, item)
    }

    constructor(bitmap: Bitmap) {
        image = bitmap
        size = bitmap.byteCount
    }

    override fun setImage(inputStream: InputStream, item: Cache.Item) {
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
                                newItem: Cache.Item): PlatformImage? {

        ImageCrawler.throwExceptionIfCancelled();

        return when (type) {
            Transform.Type.GRAY_SCALE_TRANSFORM -> grayScale(newItem)
            Transform.Type.NULL_TRANSFORM -> this
        }
    }

    private fun grayScale(newItem: Cache.Item): PlatformImage? {
        // Bail out if something is wrong with the image.
        if (image == null) {
            return null
        }

        val originalImage = image
        val grayScaleImage = originalImage!!.copy(originalImage.config, true)

        val hasTransparent = grayScaleImage.hasAlpha()
        val width = grayScaleImage.width
        val height = grayScaleImage.height
        val total = width * height
        var bytes = 0

        // A common pixel-by-pixel grayscale conversion algorithm
        // using values obtained from en.wikipedia.org/wiki/Grayscale.
        for (i in 0 until height) {
            for (j in 0 until width) {
                ImageCrawler.throwExceptionIfCancelled();

                bytes++

                // Check if the pixel is transparent in the original
                // by checking if the alpha is 0
                if (hasTransparent && grayScaleImage.getPixel(j, i) and 0xff000000.toInt() shr 24 == 0) {
                    continue
                }

                // Convert the pixel to grayscale.
                val pixel = grayScaleImage.getPixel(j, i)
                val grayScale = (Color.red(pixel) * .299
                        + Color.green(pixel) * .587
                        + Color.blue(pixel) * .114).toInt()
                grayScaleImage.setPixel(j, i, Color.rgb(grayScale,
                        grayScale,
                        grayScale))
            }

            newItem.progress(Cache.Operation.TRANSFORM, bytes.toFloat() / total, bytes)
        }

        return AndroidImage(grayScaleImage)
    }
}

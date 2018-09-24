package edu.vanderbilt.crawler.utils

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.IOException
import java.io.InputStream

/**
 * Created by monte on 05/10/17.
 */
/**
 * BitmapUtils
 *
 *
 * This helper class encapsulates Bitmap-specific processing methods.
 */
object BitmapUtils : KtLogger {
    /**
     * This returns the sample size that should be used when down-sampling the
     * image. This ensures that the image is scaled appropriately with respect
     * to it's final display size.
     */
    fun calculateInSampleSize(
            options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2
            // and keeps both height and width larger than the requested
            // height and width.
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * This will return a bitmap that is loaded and appropriately scaled from
     * the filePath parameter.
     */
    fun decodeSampledBitmapFromFile(
            pathName: String, width: Int, height: Int): Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions.
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(pathName, options)

        // If either width or height is passed in as 0, then use the actual
        // stored image dimension.
        val sampleWidth = if (width == 0) options.outWidth else width
        val sampleHeight = if (height == 0) options.outHeight else height

        // Calculate inSampleSize
        options.inSampleSize =
                calculateInSampleSize(options, sampleWidth, sampleHeight)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(pathName, options)
    }

    /**
     * Decodes and scales a bitmap from a byte array.  Adapted from
     * developer.android.com/training/displaying-bitmaps/load-bitmap.html
     */
    fun decodeSampledBitmapFromByteArray(
            imageData: ByteArray,
            width: Int,
            height: Int): Bitmap {

        // First decode with inJustDecodeBounds=true to check
        // dimensions.
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(imageData,
                0,
                imageData.size,
                options)

        // If either width or height is passed in as 0, then use the actual
        // stored image dimension.
        val sampleWidth = if (width == 0) options.outWidth else width
        val sampleHeight = if (height == 0) options.outHeight else height

        // Calculate inSampleSize.
        options.inSampleSize = calculateInSampleSize(options, sampleWidth, sampleHeight)

        // Decode bitmap with inSampleSize set.
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeByteArray(imageData,
                0,
                imageData.size,
                options)
    }

    /**
     * Decodes from an input stream that optimally supports mark and reset
     * operations. If a maximum width and/or height are specified then the
     * passed stream must support mark and reset so that the bitmap can be down
     * sampled properly. If the width and/or height are specified and the input
     * stream does not support mark and reset, then an IllegalArgumentException
     * will be throw.
     */
    fun decodeSampledBitmapFromStream(
            inputStream: InputStream, width: Int, height: Int): Bitmap? {
        val options = BitmapFactory.Options()

        if ((width != 0 || height != 0) && !inputStream.markSupported()) {
            debug("Input stream does not support mark and reset so sample size must be set to 1.")
            options.inSampleSize = 1
        } else {

            // Set a mark for reset. Since we have no idea of the size of this
            // image, just set the maximum value possible.
            inputStream.mark(Integer.MAX_VALUE)

            // First decode with inJustDecodeBounds=true to check dimensions.
            options.inJustDecodeBounds = true

            BitmapFactory.decodeStream(inputStream, null, options)

            // Reset the stream for the actual decoding phase.
            try {
                inputStream.reset()
            } catch (e: IOException) {
                error("Failed to reset input stream during bitmap decoding")
                return null
            }

            // If either width or height is passed in as 0, then use the actual
            // stored image dimension.
            val sampleWidth = if (width == 0) options.outWidth else width
            val sampleHeight = if (height == 0) options.outHeight else height

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, sampleWidth, sampleHeight)

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
        }

        return BitmapFactory.decodeStream(inputStream, null, options)
    }

    /**
     * This will return a bitmap that is loaded and appropriately scaled from
     * the application resources.
     */
    fun decodeSampledBitmapFromResource(
            res: Resources, resId: Int, width: Int, height: Int): Bitmap? {
        if (resId == 0) {
            return null
        }

        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(res, resId, options)

        // If either width or height is passed in as 0, then use the actual
        // stored image dimension.
        val sampleWidth = if (width == 0) options.outWidth else width
        val sampleHeight = if (height == 0) options.outHeight else height

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, sampleWidth, sampleHeight)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeResource(res, resId, options)
    }

    /**
     * Determines if the file contains a valid image.
     *
     * @param pathName A file path.
     * @return `true` if the stream contains a valid image; `false` if not.
     */
    fun hasImageContent(pathName: String): Boolean {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(pathName, options)
        return options.outMimeType != null
    }

    /**
     * Determines if the stream contains a valid image.
     *
     * @param inputStream An input stream.
     * @return `true` if the stream contains a valid image; `false` if not.
     */
    fun hasImageContent(inputStream: InputStream): Boolean {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(inputStream, null, options)
        return options.outMimeType != null
    }


}

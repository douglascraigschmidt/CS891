package edu.vanderbilt.imagecrawler.utils

import edu.vanderbilt.imagecrawler.crawlers.ImageCrawler
import kotlin.math.min

/**
 * Platform independent filter algorithms.
 */
object Filters {
    @JvmStatic
    fun grayScale(pixels: IntArray, hasAlpha: Boolean, progress: (Float) -> Unit): IntArray {
        // A common pixel-by-pixel grayscale conversion algorithm
        // using values obtained from en.wikipedia.org/wiki/Grayscale.
        for (i in pixels.indices) {
            val pixel = pixels[i]

            ImageCrawler.throwExceptionIfCancelled()

            // Skip transparent pixels
            if (hasAlpha && alpha(pixel) == 0) {
                continue
            }

            // Convert the pixel to grayscale.
            val grayScale = (red(pixel) * .299
                    + green(pixel) * .587
                    + blue(pixel) * .114).toInt()
            pixels[i] = rgb(grayScale, grayScale, grayScale)

            progress.invoke(i.toFloat() / pixels.size)
        }

        return pixels
    }

    @JvmStatic
    fun sepia(pixels: IntArray, hasAlpha: Boolean, progress: (Float) -> Unit): IntArray {
        var red: Int
        var green: Int
        var blue: Int
        var alpha: Int
        var pixel: Int
        val depth = 20

        for (i in pixels.indices) {
            pixel = pixels[i]

            // Skip transparent pixels
            if (hasAlpha && alpha(pixel) == 0) {
                continue
            }

            red = red(pixel)
            green = green(pixel)
            blue = blue(pixel)
            alpha = alpha(pixel)

            blue = (red + green + blue) / 3
            green = blue
            red = green

            red += depth * 2
            green += depth

            red = min(red, 255)
            green = min(green, 255)

            pixels[i] = rgba(red, blue, green, alpha)

            progress.invoke(i.toFloat() / pixels.size)
        }

        return pixels
    }

    @JvmStatic
    fun tint(pixels: IntArray,
             hasAlpha: Boolean,
             redTint: Float,
             greenTint: Float,
             blueTint: Float,
             progress: (Float) -> Unit): IntArray {

        for (i in pixels.indices) {
            val pixel = pixels[i]

            // Skip transparent pixels
            if (hasAlpha && alpha(pixel) == 0) {
                continue
            }

            val alpha = alpha(pixel)
            val red = (red(pixel) + (255 - red(pixel)) * redTint).toInt()
            val green = (green(pixel) + (255 - green(pixel)) * greenTint).toInt()
            val blue = (blue(pixel) + (255 - blue(pixel)) * blueTint).toInt()

            pixels[i] = rgba(red, green, blue, alpha)

            progress.invoke(i.toFloat() / pixels.size)
        }

        return pixels
    }

    private fun alpha(color: Int) = (color shr 24 and 0xFF)

    private fun red(color: Int) = (color shr 16 and 0xFF)

    private fun green(color: Int) = (color shr 8 and 0xFF)

    private fun blue(color: Int) = (color and 0xFF)

    private fun rgb(red: Byte, green: Byte, blue: Byte) =
            rgb(red.toInt(), green.toInt(), blue.toInt())

    private fun rgba(red: Byte, green: Byte, blue: Byte, alpha: Byte) =
            rgba(red.toInt(), green.toInt(), blue.toInt(), alpha.toInt())

    private fun rgb(red: Int, green: Int, blue: Int) =
            rgba(red, green, blue)

    private fun rgba(red: Int, green: Int, blue: Int, alpha: Int = 0xFF) =
            (alpha shl 24) or (red shl 16) or (green shl 8) or blue

    private fun Int.clip() = min(this, 255).toByte()
}
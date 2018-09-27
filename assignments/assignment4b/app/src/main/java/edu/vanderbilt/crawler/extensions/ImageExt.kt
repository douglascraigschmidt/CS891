package edu.vanderbilt.crawler.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import edu.vanderbilt.crawler.R
import edu.vanderbilt.crawler.app.App
import edu.vanderbilt.crawler.modules.GlideApp
import java.io.ByteArrayOutputStream
import java.net.URL

/**
 * Collection of image helper extensions.
 */

val IMAGE_VIEW_PLACEHOLDER = R.drawable.placeholder

/**
 * Download managers.
 */
enum class ImageDownloader {
    GLIDE,
    PICASSO
}

/**
 * Current strategy used for downloading images.
 */
var imageDownloader = ImageDownloader.GLIDE

/**
 * Download manager property.
 */
var ImageView.downloader: ImageDownloader
    get() = imageDownloader
    set(downloader) {
        imageDownloader = downloader
    }

/**
 * Check for common image extensions.
 */
fun hasImageExtension(url: String): Boolean {
    return url.endsWith(".png")
           || url.endsWith(".jpg")
           || url.endsWith(".jpeg")
}

/**
 * Loads the gif resource identified by the resource id [imageId].
 * A gif placeholder [placeholder] can be specified. The download
 * requires the imageDownloader to be set to ImageDownloader.GLIDE.
 */
fun ImageView.asyncLoadGif(imageId: Int,
                           placeholder: Int? = null,
                           block: (status: Boolean) -> Unit = {}) {
    asyncLoadGif(context.getResourceUri(imageId).toString(), placeholder, block)
}

/**
 * Loads the gif resource identified by the resource id [imageId].
 * A gif placeholder [placeholder] can be specified. The download
 * requires the imageDownloader to be set to ImageDownloader.GLIDE.
 */
fun ImageView.asyncLoadGif(url: String,
                           placeholder: Int? = null,
                           block: (status: Boolean) -> Unit = {}) {
    (!url.isBlank()).let {
        when (downloader) {
            ImageDownloader.GLIDE -> {
                GlideApp.with(this).clear(this)
                val builder = GlideApp.with(this).asGif().load(url)
                if (placeholder != null) {
                    builder.placeholder(placeholder)
                }
                builder.listener(object : RequestListener<GifDrawable> {
                    override fun onResourceReady(resource: GifDrawable?,
                                                 model: Any?,
                                                 target: Target<GifDrawable>?,
                                                 dataSource: DataSource?,
                                                 isFirstResource: Boolean): Boolean {
                        block(true)
                        return false // Let Glide update the target.
                    }

                    override fun onLoadFailed(e: GlideException?,
                                              model: Any?,
                                              target: Target<GifDrawable>?,
                                              isFirstResource: Boolean): Boolean {
                        block(false)
                        return false // Let Glide update the target.
                    }
                }).into(this)
            }
            else -> throw IllegalStateException(
                    "GIF loading requires GLIDE downloader to be set")
        }
    }
}

/**
 * Loads the image or gif (if [asGif] is true) resource identified by
 * the resource id [imageId]. A default placeholder [IMAGE_VIEW_PLACEHOLDER]
 * is used unless a custom [placeholder] value is specified. The download
 * is performed by the current [imageDownloader].
 */
fun ImageView.asyncLoad(imageId: Int,
                        placeholder: Int? = IMAGE_VIEW_PLACEHOLDER,
                        block: (status: Boolean) -> Unit = {}) {
    asyncLoad(url = context.getResourceUri(imageId).toString(),
              placeholder = placeholder,
              block = block)
}

internal fun asyncLoadTest(context: Context, url: String, view: ImageView) {
    GlideApp.with(context)
            .load(url)
}

/**
 * Loads the image or gif (if [asGif] is true) from the specified [url].
 * A default placeholder [IMAGE_VIEW_PLACEHOLDER] is used unless a custom
 * [placeholder] value is specified. The download is performed by the
 * current [imageDownloader].
 */
fun ImageView.asyncLoad(url: String,
                        placeholder: Int? = IMAGE_VIEW_PLACEHOLDER,
                        block: (status: Boolean) -> Unit = {}) {
    (!url.isBlank()).let {
        when (downloader) {
            ImageDownloader.GLIDE -> {
                GlideApp.with(this).clear(this)
                val builder = GlideApp.with(this).load(url)
                if (placeholder != null) {
                    builder.placeholder(placeholder)
                }
                builder.listener(object : RequestListener<Drawable> {
                    override fun onResourceReady(resource: Drawable,
                                                 model: Any,
                                                 target: Target<Drawable>,
                                                 dataSource: DataSource,
                                                 isFirstResource: Boolean): Boolean {
                        block(true)
                        return false // Let Glide update the target.
                    }

                    override fun onLoadFailed(e: GlideException?,
                                              model: Any,
                                              target: Target<Drawable>,
                                              isFirstResource: Boolean): Boolean {
                        block(false)
                        return false // Let Glide update the target.
                    }
                }).into(this)
            }

            ImageDownloader.PICASSO -> {
                Picasso.with(this.context).cancelRequest(this)
                val builder = Picasso.with(context).load(url)
                if (placeholder != null) {
                    builder.placeholder(placeholder)
                }
                builder.into(this,
                             object : Callback {
                                 override fun onSuccess() = block(true)
                                 override fun onError() = block(false)
                             })
            }
        }
    }
}

/**
 * Clears the image from the downloader cache.
 */
fun ImageView.clear() {
    when (downloader) {
        ImageDownloader.GLIDE -> GlideApp.with(this).clear(this)
        ImageDownloader.PICASSO -> Picasso.with(this.context).cancelRequest(this)
    }
}

/**
 * Asynchronously attempts to fetch an image and then calls [block]
 * in the the calling thread passing in the state of the fetch operation.
 */
fun Context.asyncFetchImage(url: String,
                            width: Int = Target.SIZE_ORIGINAL,
                            height: Int = Target.SIZE_ORIGINAL,
                            block: (isImage: Boolean) -> Unit) {
    when (imageDownloader) {
        ImageDownloader.GLIDE -> {
            GlideApp.with(applicationContext)
                    .asBitmap()
                    .load(url)
                    .downsample(DownsampleStrategy.AT_MOST)
                    .into(object : SimpleTarget<Bitmap>(width, height) {
                        override fun onResourceReady(resource: Bitmap?,
                                                     transition: Transition<in Bitmap>?)
                                = block(true)

                        override fun onLoadFailed(errorDrawable: Drawable?)
                                = block(false)
                    })
        }

        ImageDownloader.PICASSO -> {
            Picasso.with(this)
                    .load(url)
                    .fetch(object : Callback {
                        override fun onSuccess() = block(true)
                        override fun onError() = block(false)
                    })
        }
    }
}


fun URL.getImageBytes(caching: Boolean = true): ByteArray {
    //try {
    val builder = Picasso.with(App.instance)
            .load(IMAGE_VIEW_PLACEHOLDER)
    /*
        val builder = Picasso.with(App.instance)
                .load(this.toString())
                .placeholder(IMAGE_VIEW_PLACEHOLDER)
                */
    if (!caching) {
        builder.memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
    }
    val bitmap = builder.get()
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
    //} catch (e: Exception) {
    //    println("Picasso unable to fetch image bytes for url $this: $e")
    //    return null
    //}
}


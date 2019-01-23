package edu.vandy.app.extensions

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
import edu.vandy.app.App
import edu.vandy.app.modules.GlideApp
import edu.vandy.app.preferences.EnumAdapter
import edu.vandy.app.preferences.ObservablePreference
import edu.vandy.app.preferences.Subscriber
import edu.vandy.app.ui.screens.settings.Settings
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URL

/**
 * Custom Picasso singleton so that the rest of the app
 * can use Picasso.with(context) as if using the normal
 * Picasso singleton. Files should only import this class
 * and not the real com.squareup.picasso.Picasso otherwise
 * two picasso instances will exist and possibly produce
 * unstable results.
 */
object Picasso {
    fun with(@Suppress("UNUSED_PARAMETER") context: Context): com.squareup.picasso.Picasso {
        return App.instance.picasso
    }

    fun clearCache() {
        App.instance.picasso.shutdown()
        App.instance.picasso = com.squareup.picasso.Picasso.Builder(App.instance).build()
    }
}

/**
 * Image downloader singleton that manages Picasso and Glide
 * cache access. It also uses a shared preference that can
 * be exposed in the UI to choose between either downloader.
 * View extensions are included that provide asynchronous
 * download extension functions.
 */
object ImageDownloader {
    fun installPicasso(context: Context? = App.instance) {
        // Install custom Picasso instance to make it easy
        // to clear it's cache.
        if (ImageDownloader.type == ImageDownloader.Type.PICASSO) {
            val builder = com.squareup.picasso.Picasso.Builder(context)
            App.instance.picasso = builder.build()
        }
    }

    /**
     * Download managers.
     */
    enum class Type {
        PICASSO,
        GLIDE
    }

    /**
     * Current strategy used for downloading images.
     */
    var type: Type by ObservablePreference(
            default = Settings.DEFAULT_IMAGE_DOWNLOADER,
            name = Settings.IMAGE_DOWNLOADER_PREF,
            adapter = EnumAdapter(Type::class.java),
            subscriber = object : Subscriber<Type> {
                override val subscriber: (Type) -> Unit
                    get() = {
                        println("DOWNLOADER changed to $it")
                        if (it == Type.PICASSO) {
                            installPicasso()
                        }
                    }

                override fun unsubscribe(callback: () -> Unit) {
                    App.instance.compositeUnsubscriber.add(callback)
                }
            })

    init {
        println("ImageDownloader: Init called, type = $type")
    }

    /**
     * Clears the current downloader cache.
     */
    fun clearCache() {
        when (type) {
            Type.GLIDE -> {
                GlideApp.getPhotoCacheDir(App.instance)?.let {
                    deleteContents(it)
                }
            }
            Type.PICASSO -> {
                Picasso.clearCache()
            }
        }
    }

    /**
     * Recursively delete contents of [dir].
     */
    private fun deleteContents(dir: File) {
        if (dir.isDirectory) {
            dir.listFiles().forEach {
                deleteRecursive(it)
            }
        }
    }

    /**
     * Recursively delete [dir].
     */
    private fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) {
            fileOrDirectory.listFiles().forEach {
                deleteRecursive(it)
            }
        }

        fileOrDirectory.delete()
    }
}

/**
 * Can be used to specify that no placeholder by used
 * by load functions. This is only for readability
 * because any value <= 0 will be interpreted as a
 * request to not use a placeholder.
 */
val IMAGE_VIEW_NO_PLACEHOLDER = -1
val IMAGE_VIEW_PLACEHOLDER = IMAGE_VIEW_NO_PLACEHOLDER

/**
 * Collection of image helper extensions.
 */

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
                           placeholder: Int = IMAGE_VIEW_PLACEHOLDER,
                           block: (status: Boolean) -> Unit = {}) {
    checkPlacholder(placeholder)
    asyncLoadGif(context.getResourceUri(imageId).toString(), placeholder, block)
}

/**
 * Loads the gif resource identified by the resource id [imageId].
 * A gif placeholder [placeholder] can be specified. The download
 * requires the imageDownloader to be set to ImageDownloader.GLIDE.
 */
fun ImageView.asyncLoadGif(url: String,
                           placeholder: Int = IMAGE_VIEW_PLACEHOLDER,
                           block: (status: Boolean) -> Unit = {}) {
    checkPlacholder(placeholder)
    (!url.isBlank()).let {
        when (ImageDownloader.type) {
            ImageDownloader.Type.GLIDE -> {
                clear()
                val builder = GlideApp.with(this).asGif().load(url)
                if (placeholder > 0) {
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
            ImageDownloader.Type.PICASSO -> {
                clear()
                asyncLoad(url, placeholder, block)
            }
        }
    }
}

fun checkPlacholder(@Suppress("UNUSED_PARAMETER") placeholder: Int) {
    return
//    if (placeholder == R.drawable.placeholder || placeholder == R.drawable.error) {
//        throw AssertionError("should not happen")
//    }
}

/**
 * Loads the image or gif (if [asGif] is true) resource identified by
 * the resource id [imageId]. A default placeholder [IMAGE_VIEW_PLACEHOLDER]
 * is used unless a custom [placeholder] value is specified. The download
 * is performed by the current [imageDownloader].
 */
fun ImageView.asyncLoad(imageId: Int,
                        placeholder: Int = IMAGE_VIEW_PLACEHOLDER,
                        block: (status: Boolean) -> Unit = {}) {
    checkPlacholder(placeholder)
    asyncLoad(url = context.getResourceUri(imageId).toString(),
              placeholder = placeholder,
              block = block)
}

internal fun asyncLoadTest(context: Context, url: String, @Suppress("UNUSED_PARAMETER") view: ImageView) {
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
                        placeholder: Int = IMAGE_VIEW_PLACEHOLDER,
                        block: (status: Boolean) -> Unit = {}) {
    checkPlacholder(placeholder)
    (!url.isBlank()).let {
        when (ImageDownloader.type) {
            ImageDownloader.Type.GLIDE -> {
                clear()
                val builder = GlideApp.with(this).load(url).centerInside()
                if (placeholder > 0) {
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

            ImageDownloader.Type.PICASSO -> {
                clear()
                val builder = Picasso.with(context).load(url)
                if (placeholder > 0) {
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
    when (ImageDownloader.type) {
        ImageDownloader.Type.GLIDE -> GlideApp.with(this).clear(this)
        ImageDownloader.Type.PICASSO -> Picasso.with(this.context).cancelRequest(this)
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
    when (ImageDownloader.type) {
        ImageDownloader.Type.GLIDE -> {
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

        ImageDownloader.Type.PICASSO -> {
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


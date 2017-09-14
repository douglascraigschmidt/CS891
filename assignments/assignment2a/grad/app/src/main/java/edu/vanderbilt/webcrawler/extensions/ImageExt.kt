package edu.vanderbilt.webcrawler.extensions

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import edu.vanderbilt.webcrawler.App
import edu.vanderbilt.webcrawler.R
import edu.vanderbilt.webcrawler.modules.GlideApp
import edu.vanderbilt.webcrawler.modules.GlideRequest
import java.io.ByteArrayOutputStream
import java.net.URL

/**
 * Created by monte on 2017-09-07.
 */

val IMAGE_VIEW_PLACEHOLDER = R.mipmap.ic_launcher_round

//val IMAGE_VIEW_PLACEHOLDER = R.drawable.placeholder
enum class DownloadStrategy {
    GLIDE,
    PICASSO
}

fun ImageView.load(url: String,
                   placeholder: Int = IMAGE_VIEW_PLACEHOLDER,
                   asGif: Boolean = false,
                   strategy: DownloadStrategy = DownloadStrategy.GLIDE,
                   block: (status: Boolean) -> Unit = {}) {
    (!url.isBlank()).let {
        // Cancel any pending request...
        GlideApp.with(this).clear(this)

        when (strategy) {
            DownloadStrategy.GLIDE -> {
                val builder = if (asGif)
                    GlideApp.with(this).asGif().load(url)
                else
                    GlideApp.with(this).load(url)

                if (placeholder > 0) {
                    builder.placeholder(placeholder)
                }

                builder.into(this)
            }

            DownloadStrategy.PICASSO -> {
                val builder = Picasso.with(context).load(url)

                if (placeholder > 0) {
                    builder.placeholder(placeholder)
                }

                builder.into(this,
                        object : Callback {
                            override fun onSuccess() {
                                block(true)
                            }

                            override fun onError() {
                                block(false)
                            }
                        })
            }
        }
    }
}

/**
 * Attempts to asynchronously load an image into receiver ImageView
 * and also returns status of load in the calling thread.
fun ImageView.load(url: String,
placeholder: Int = IMAGE_VIEW_PLACEHOLDER,
block: (status: Boolean) -> Unit) {
(!url.isBlank()).let {
Picasso.with(context)
.load(url)
.placeholder(placeholder)
.into(this,
object : Callback {
override fun onSuccess() {
block(true)
}

override fun onError() {
block(false)
}
})
}
}
 */

/**
 * Asynchronously attempts to fetch an image and then calls [block]
 * in the the calling thread passing in the status of the fetch operation.
 */
fun Context.fetchImage(url: String, block: (isImage: Boolean) -> Unit) {
    Picasso.with(this)
            .load(url)
            .fetch(object : Callback {
                override fun onSuccess() {
                    block(true)
                }

                override fun onError() {
                    block(false)
                }
            })
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


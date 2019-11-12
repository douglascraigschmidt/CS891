package edu.vanderbilt.crawler.extensions

import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.RequestOptions.decodeTypeOf

/**
 * Handy Glide extensions.
 */

fun RequestOptions.minThumbSize() : RequestOptions = fitCenter().override(64)

fun RequestBuilder<GifDrawable>.asGif() : RequestBuilder<GifDrawable> =
    transition(DrawableTransitionOptions()).apply(decodeTypeOf(GifDrawable::class.java).lock())

fun <T> RequestBuilder<T>.withRoundIcon() = apply(RequestOptions().transform(CircleCrop()))
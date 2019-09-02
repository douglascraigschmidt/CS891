package edu.vanderbilt.crawler.modules

import android.content.Context

import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.module.AppGlideModule

/**
 * The name doesn't matter since Glide annotation processor
 * will generate GlideApp singleton.
 */
@GlideModule
class GlideAppModuleInst: AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setMemoryCache(LruResourceCache(10 * 1024 * 1024))
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        //registry.append(Api.GifResult.class, InputStream.class, new GiphyModelLoader.Factory());
    }
}
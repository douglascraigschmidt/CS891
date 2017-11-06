package edu.vanderbilt.crawler.platform

import edu.vanderbilt.crawler.app.App
import edu.vanderbilt.crawler.modules.GlideApp
import edu.vanderbilt.imagecrawler.platform.Cache
import java.io.File

object AndroidCache : Cache(File(App.instance.cacheDir, "crawler-cache").canonicalFile) {
    override fun clear() {
        super.clear()
        GlideApp.getPhotoCacheDir(App.instance)?.let { deleteContents(it) }
    }
}

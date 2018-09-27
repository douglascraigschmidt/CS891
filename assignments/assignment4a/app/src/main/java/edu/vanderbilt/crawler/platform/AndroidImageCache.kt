package edu.vanderbilt.crawler.platform

import edu.vanderbilt.crawler.app.App
import edu.vanderbilt.imagecrawler.platform.FileCache
import java.io.File

object AndroidImageCache : FileCache(File(App.instance.cacheDir, "crawler-cache").canonicalFile) {
    override fun clear() {
        super.clear()
        val deleted = deleteContents(App.instance.cacheDir)
        debug("Deleted $deleted files from app cache directory")
        cacheDir.mkdirs()
    }
}

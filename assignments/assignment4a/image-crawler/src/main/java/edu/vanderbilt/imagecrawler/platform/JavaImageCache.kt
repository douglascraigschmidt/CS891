package edu.vanderbilt.imagecrawler.platform

import java.io.File

/**
 * A Kotlin singleton Java image cache object that specifies the
 * root cache directory for running the pure Java version of the
 * crawler application.
 */
object JavaImageCache: FileCache(File("./image-cache").canonicalFile)
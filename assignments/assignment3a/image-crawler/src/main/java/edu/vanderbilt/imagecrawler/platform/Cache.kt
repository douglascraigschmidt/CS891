package edu.vanderbilt.imagecrawler.platform

import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * A Kotlin interface defining the basic required cache operations.
 */
interface Cache {
    /**
     * Root cache directory (platform dependent)
     */
    val cacheDir: File

    /**
     * Artificial delay in milliseconds.
     */
    var delay: Int

    /** Operation changes sent to registered observers */
    enum class Operation {
        LOAD,
        DOWNLOAD,
        CREATE,
        DELETE,
        READ,
        WRITE,
        TRANSFORM,
        CLOSE,
    }

    enum class State {
        START,
        PROCESS,
        END,
        NONE
    }

    /**
     * All cache items require a prefix tag to avoid edge
     * cases where tagged items match untagged items.
     * For example put("foo", "bar") produced file "bar-foo"
     * and put("bar-foo") (no tag) would also produces "bar-foo".
     * Requiring all items to have tags fixes this by making the
     * latter case produce the file "notag-bar-foo". An exception
     * is thrown if key is passed with the prefix "notag-".
     */
    companion object {
        val NOTAG = "__notag__"
    }

    fun get(key: String, tag: String? = NOTAG): Cache.Item?
    fun put(key: String, tag: String? = NOTAG): Cache.Item?
    fun putIfAbsent(key: String, tag: String? = NOTAG): Cache.Item?
    fun contains(key: String, tag: String? = NOTAG): Boolean
    fun remove(key: String, tag: String? = NOTAG): Cache.Item?

    /**
     * Removes all cache items (and their associated file objects)
     * that were create with the specified [tag].
     *
     */
    fun removeTagged(tag: String)

    /**
     * Removes all cached items and their associated files.
     */
    fun clear()

    /**
     * Returns the number of cached items.
     */
    fun getCacheSize(): Int

    /**
     * Exposed for assignment building only.
     */
    fun traverseCache(dir: File = cacheDir, visit: (file: File) -> Unit)
    fun newItemFromFile(file: File): Cache.Item

    /**
     * Cache item that hides underlying storage mechanism.
     */
    interface Item {
        val key: String
        val tag: String
        val file: File
        val size: Int
        val timestamp: Long

        fun getInputStream(operation: Operation = Operation.READ): InputStream?
        fun getOutputStream(operation: Operation = Operation.WRITE,
                            size: Int = 0): OutputStream
        fun progress(operation: Operation, progress: Float, bytes: Int)
    }
}
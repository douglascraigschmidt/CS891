package edu.vanderbilt.imagecrawler.platform

import edu.vanderbilt.imagecrawler.crawlers.framework.ImageCrawler
import edu.vanderbilt.imagecrawler.platform.Cache.Companion.NOTAG
import java.io.*
import java.lang.ref.WeakReference
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap

/**
 * A Kotlin singleton file cache implementation that uses a
 * ConcurrentHashMap to provide thread-safe access.
 * An optional tag property can be specified for each
 * added cache item which provides support for grouping
 * items. A number of operations provide filtering based
 * on an optional grouping tag.
 */
open class FileCache(final override val cacheDir: File) : Cache {

    /** Default states parameter for startWatching function. */
    private val allStates = Cache.Operation.values()

    /** Map for handling concurrent access. */
    private val hashMap = ConcurrentHashMap<String, Cache.Item>()

    /** Optional list of [State] observers. */
    private val observers = mutableListOf<ObserverEntry>()

    /** Artificial processing delay */
    @Volatile override var delay: Int = 0

    /**
     * Immutable Entry used in observers list property. A null
     * [filter] list is used to watch all [State] values.
     */
    private data class ObserverEntry(val filter: Array<out Cache.Operation>,
                                     val observer: WeakReference<Observer>)

    /**
     * An observer interface with a single event function that
     * can be notified about any desired cache [State] changes.
     */
    interface Observer {
        fun event(operation: Cache.Operation, item: Cache.Item, progress: Float = -1f)
    }

    init {
        // Ensure that the cache directory exists.
        cacheDir.mkdirs()
        loadFromDisk()
    }

    /**
     * Returns a new immutable cache item.
     */
    private fun newItem(key: String, tag: String): Cache.Item
            = Item(File(cacheDir, getCachePath(key, tag)),
                   key,
                   tag,
                   System.nanoTime(),
                   this::notifyObservers)

    /**
     * Adds an [observer] to the list of observers. The [observer]
     * will be notified when of any state in the specified
     * list of [operations] changes. To prevent memory leaks, the [observer]
     * is kept as a WeakReference.
     */
    fun startWatching(observer: Observer, vararg operations: Cache.Operation = allStates) {
        synchronized(observers) {
            stopWatching(observer)

            val entry = ObserverEntry(operations, WeakReference(observer))
            observers.add(entry)

            // Immediately notify observer about all cache contents.
            notifyCurrentContents(listOf(entry))
        }
    }

    /**
     * Removes the specified [observer] from the observer list.
     */
    fun stopWatching(observer: Observer) {
        synchronized(observers) {
            observers.filter { it.observer.get() == observer }
                    .firstOrNull { observers.remove(it) }
        }
    }

    /**
     * Gets a cache item.
     */
    override fun get(key: String, tag: String?): Cache.Item? {
        return hashMap.get(getKey(key, tag ?: NOTAG))
    }

    /**
     * Adds or replaces an item. The replaced item is returned.
     */
    override fun put(key: String, tag: String?): Cache.Item? {
        if (key.startsWith(NOTAG)) {
            error("Invalid argument: key cannot begin with $NOTAG")
        }

        return hashMap.put(getKey(key, tag ?: NOTAG), newItem(key, tag ?: NOTAG))
    }

    /**
     * Adds a new item or returns the existing item. The existing
     * item is returned.
     */
    override fun putIfAbsent(key: String, tag: String?): Cache.Item? {
        if (key.startsWith(NOTAG)) {
            error("Invalid argument: key cannot begin with $NOTAG")
        }

        val item = newItem(key, tag ?: NOTAG)
        val oldItem = hashMap.putIfAbsent(getKey(key, tag ?: NOTAG), item)

        // If item not already in cache, then create an empty file
        // so that the file timestamp can be used for sorting by
        // ascending order in the application recycler view.
        if (oldItem == null) {
            if (item.file.exists()) {
                item.file.delete()
            }
            item.file.createNewFile()
            notifyObservers(item, Cache.Operation.CREATE)
        }

        debug("Thread [${Thread.currentThread().name}]: " +
              "$item - CACHE " + if (oldItem != null) "HIT" else "MISS")

        return oldItem
    }

    override fun contains(key: String, tag: String?): Boolean {
        return hashMap.contains(getKey(key, tag ?: NOTAG))
    }

    override fun remove(key: String, tag: String?): Cache.Item? {
        val item = hashMap.remove(getKey(key, tag ?: NOTAG))
        item?.let {
            it.file.delete()
            notifyObservers(it, Cache.Operation.DELETE)
        }

        return item
    }

    /**
     * Removes all cache items (and their associated file objects)
     * that were create with the specified [tag].
     *
     */
    override fun removeTagged(tag: String) {
        hashMap.filter { it.value.tag == tag }
                .forEach { remove(it.key, it.value.tag) }
    }

    /**
     * Removes all cached items and their associated files.
     */
    override fun clear() {
        synchronized(hashMap) {
            hashMap.forEach {
                val item = hashMap.remove(it.key)
                item?.let {
                    it.file.delete()
                    notifyObservers(it, Cache.Operation.DELETE)
                }
            }
            // Sanity check.
            val files = cacheDir.listFiles()
            if (files != null && files.isNotEmpty()) {
                warn("Cache cleared, but ${files.size} files still " +
                     "exist in cache directory $cacheDir")
            }
        }
    }

    /**
     * Recursively delete files in directory [dir]
     * and return count of deleted files/directories.
     */
    fun deleteContents(dir: File): Int {
        return dir.listFiles()?.map {
            when {
                it.isDirectory ->
                    deleteContents(it) + if (it.delete()) 1 else 0
                it.delete() -> 1
                else -> 0
            }
        }?.toIntArray()?.sum() ?: 0
    }

    /**
     * Returns the number of cached items.
     */
    override fun getCacheSize(): Int {
        return hashMap.size
    }

    /**
     * Maps a [uri] and [tag] pair to an encoded relative cache path
     * suitable for file operations.
     */
    fun getCachePath(uri: String, tag: String = NOTAG): String {
        try {
            // Urls may be either
            //      "http://<path>",
            //      "file://java_resources/<path>,
            //      "file://android_assets/<path>
            //      "file://locat_project/<path>
            // To facilitate testing which compares the downloaded cache
            // with a ground-truth directory, strip off the locator prefix
            // so that cache files will have the same names no matter where
            // they originated from.
            val fixedUri: String =
                    with(uri) {
                        when {
                            startsWith(Platform.PROJECT_URI_PREFIX) ->
                                replace("${Platform.PROJECT_URI_PREFIX}/", "")
                            startsWith("${Platform.ASSETS_URI_PREFIX}/") ->
                                replace(Platform.PROJECT_URI_PREFIX, "")
                            startsWith("${Platform.RESOURCES_URI_PREFIX}/") ->
                                replace(Platform.PROJECT_URI_PREFIX, "")
                            startsWith("http://") -> replace("http://", "")
                            startsWith("https://") -> replace("https://", "")
                            else -> uri
                        }
                    }

            // Encode the adjusted uri.
            val fileName = URLEncoder.encode(fixedUri, "UTF-8")

            // Sanity check: make sure that decoding the encoded
            // filename can be converted back to the original uri.
            val decodedUri = URLDecoder.decode(fileName, "UTF-8")
            val uriTest = URI(decodedUri)
            if (uriTest.toString() != fixedUri) {
                throw UnsupportedEncodingException("Unable to encode/decode uri: $uri")
            }

            val filePath = File(cacheDir, getKey(fileName, tag))
            return filePath.name
        } catch (e: UnsupportedEncodingException) {
            error(e)
        }
    }

    /**
     * Creates a nwe item instance matching the specified cache file.
     * Note that this method will either return an item or throw an
     * exception. As a precaution, sweepCache is available to clean
     * rogue and empty files when the cache is first loaded from disk
     * making an exception very unlikely.
     */
    override fun newItemFromFile(file: File): Cache.Item {
        if (!file.exists()) {
            error("Invalid argument: file does not exist.")
        }

        if (!file.isFile) {
            error("Invalid argument: file is not a file.")
        }

        // Split into tag and encoded key name.
        val list = file.name.split(delimiters = '-', limit = 2)
        if (list.size != 2) {
            error("Detected invalid file in cache: $file")
        }

        val key = URLDecoder.decode(list[1], "UTF-8")
        return newItem(key, list[0])
    }

    /**
     * Tags the [key] with the specified tag or the default [NOTAG] tag
     * value if no [tag] is specified.
     */
    internal fun getKey(key: String, tag: String = NOTAG): String {
        if (tag.contains(' ')) {
            error("tag must not contain spaces")
        }

        return tag + "-" + key
    }

    /**
     * Deletes invalid or 0 length files from the cache.
     */
    internal fun sweepCache(): Int {
        var swept = 0

        traverseCache {
            // Delete any empty files that may have been orphaned
            // if a previous application invocation terminated
            // abnormally.
            if (it.length() == 0L) {
                info("Removing orphaned empty file from the cache: $it")
                if (!it.delete() || it.exists()) {
                    error("Unable to delete cache file: $it")
                }
                swept++
            }

            // Split into tag and encoded key name.
            val list = it.name.split(delimiters = '-', limit = 2)
            if (list.size != 2) {
                warn("Removing unknown file from cache: $it")
                if (!it.delete() || it.exists()) {
                    error("Unable to delete cache file: $it")
                }
                swept++
            }
        }

        return swept
    }

    /**
     * General purpose cache traversal function that begins at the
     * specified [dir] directory and calls the provided [visit]
     * function passing in each discovered file. Directories are
     * not passed to the [visit] function.
     */
    override fun traverseCache(dir: File, visit: (file: File) -> Unit) {
        dir.listFiles()?.forEach {
            when {
                it.isDirectory -> traverseCache(it, visit)
                else -> visit(it)
            }
        }
    }

    /**
     * Called once when the application starts to create cache entries
     * in the hashMap matching all files in the default cache directory.
     * A sweep is performed first to remove any rogue or empty files.
     */
    fun loadFromDisk(): Int {
        hashMap.clear()

        val swept = sweepCache()
        if (swept > 0) {
            debug("Swept $swept files from cache.")
        }

        traverseCache {
            val item = newItemFromFile(it)
            val key = getKey(item.key, item.tag)
            hashMap.put(key, item)
            notifyObservers(item, Cache.Operation.LOAD, 1f)
        }

        debug("Loaded ${hashMap.size} cache items from disk")
        return hashMap.size
    }

    /**
     * Notifies all interested observers of an [item] [operation] change.
     */
    fun notifyObservers(item: Cache.Item, operation: Cache.Operation, progress: Float = -1f) {
        if (delay > 0) {
            Thread.sleep(delay.toLong())
        }

        observers.filter { it.filter.contains(operation) }
                .forEach { it.observer.get()?.event(operation, item, progress) }
    }

    private fun notifyCurrentContents(observers: List<ObserverEntry>) {
        hashMap.forEach { item ->
            observers.filter { it.filter.contains(Cache.Operation.LOAD) }
                    .forEach { it.observer.get()?.event(Cache.Operation.LOAD, item.value) }
        }
    }

    /**
     * Debug output helper. Avoids using Kotlin Anko log helper
     * which references the Android logging classes preventing
     * this class from being unit tested.
     */
    fun debug(msg: String) {
        System.out.println("DEBUG: $msg")
    }

    /**
     * Info output helper (see [debug] comment).
     */
    fun info(msg: String) {
        System.out.println("INFO: $msg")
    }

    /**
     * Warning output helper (see [debug] comment).
     */
    fun warn(msg: String) {
        System.out.println("WARNING: $msg")
    }

    /**
     * Immutable item class provides input and output streams, keeping
     * the underlying storage implementation details hidden from
     * the application.
     */
    data class Item(override val file: File,
                    override val key: String,
                    override val tag: String = NOTAG,
                    override val timestamp: Long,
                    private val notify: (item: Cache.Item,
                                         operation: Cache.Operation,
                                         progress: Float) -> Unit)
        : Cache.Item {

        override val size: Int
            get() = if (file.exists()) file.length().toInt() else 0

        override fun getInputStream(operation: Cache.Operation): InputStream? {
            return try {
                ObserverInputStream(FileInputStream(file), operation, this, size)
            } catch (e: Exception) {
                null
            }
        }

        override fun getOutputStream(operation: Cache.Operation,
                                     size: Int): OutputStream {
            return ObserverOutputStream(FileOutputStream(file), operation, this, size)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Item

            if (key != other.key) return false
            if (tag != other.tag) return false
            if (file != other.file) return false

            return true
        }

        override fun hashCode(): Int {
            var result = key.hashCode()
            result = 31 * result + tag.hashCode()
            result = 31 * result + file.hashCode()
            return result
        }

        override fun toString(): String {
            return "Item(key='$key', tag='$tag')"
        }

        override fun progress(operation: Cache.Operation, progress: Float, bytes: Int) {
            notify(this, operation, progress)
        }
    }

    class ObserverInputStream(inputStream: InputStream,
                              private val operation: Cache.Operation,
                              private val item: Cache.Item,
                              internal val size: Int)
        : FilterInputStream(inputStream) {

        // The number of bytes that have been read from the InputStream
        private var bytesRead = 0

        override fun available(): Int = size - bytesRead

        override fun close() {
            super.close()
            notify(Cache.Operation.CLOSE, 1f)
        }

        override fun read(): Int {
            val bytes = super.read()
            if (bytes != -1) {
                bytesRead++
                notify(operation, bytesRead.toFloat() / size)
            } else {
                log("EOF total = $size read = $bytesRead")
            }
            return bytes
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            ImageCrawler.throwExceptionIfCancelled()

            val read = super.read(b, off, len)
            if (read != -1) {
                bytesRead += read
                notify(Cache.Operation.READ, bytesRead.toFloat() / size)
            } else {
                log("EOF total = $size read = $bytesRead")
            }
            return read
        }

        private fun notify(operation: Cache.Operation, progress: Float) {
            if (Thread.interrupted()) {
                throw InterruptedException("ObserverInputStream interrupted")
            }

            item.progress(operation, progress, size)
        }

        fun log(msg: String) {
            if (false) {
                val func = "ObserverInputStream"
                val name = File(item.key).name
                System.out.println("$func[$name|${item.tag}]: $msg")
            }
        }
    }

    /**
     * Immutable filter stream class used to monitor write
     * events and report them to registered observers.
     */
    class ObserverOutputStream(outputStream: OutputStream,
                               private val operation: Cache.Operation,
                               private val item: Item,
                               internal val size: Int) :
            FilterOutputStream(outputStream) {

        private var bytesWritten = 0

        override fun write(b: Int) {
            super.write(b)
            bytesWritten++
        }

        override fun close() {
            super.close()
            notify(Cache.Operation.CLOSE, 1f)
        }

        override fun write(b: ByteArray?, off: Int, len: Int) {
            ImageCrawler.throwExceptionIfCancelled()
            super.write(b, off, len)
            notify(operation, bytesWritten.toFloat() / size)
        }

        private fun notify(operation: Cache.Operation, progress: Float) {
            if (Thread.interrupted()) {
                throw InterruptedException("ObserverOutputStream interrupted")
            }

            item.progress(operation, progress, size)
        }

        fun log(msg: String) {
            if (false) {
                val func = "ObserverOutputStream"
                val name = File(item.key).name
                System.out.println("$func[$name|${item.tag}]: $msg")
            }
        }
    }
}

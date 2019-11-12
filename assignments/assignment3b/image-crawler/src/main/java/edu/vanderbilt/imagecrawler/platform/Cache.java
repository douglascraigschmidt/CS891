package edu.vanderbilt.imagecrawler.platform;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

import edu.vanderbilt.imagecrawler.crawlers.ImageCrawler;
import edu.vanderbilt.imagecrawler.utils.Options;
import io.reactivex.annotations.NonNull;

/**
 * A safe file based cache implementation that uses a ConcurrentHashMap
 * to provide thread-safe cache operations. Supports observers that
 * are be notified when the state of any cached item changes.
 */
public class Cache {
    /**
     * The Default tag when no tag is specified.
     */
    public static final String NOTAG = "__notag__";
    /**
     * Logging tag.
     */
    private static final String TAG = "Cache";
    /**
     * Default states parameter for startWatching function.
     */
    private static final List<Operation> mAllStates =
            Arrays.asList(Operation.values());

    /**
     * Boolean flag in thread local storage which is used to
     * determine when a thread's call to addItem actually adds
     * a new item rather than accessing a previously created
     * item. This boolean is set in newItem() and checked in
     * addItem().
     */
    private static final ThreadLocal<Boolean> wasJustCreated =
            ThreadLocal.withInitial(() -> false);

    /**
     * Static guard and synchronization lock object to ensure
     * that sub-classes are properly declared as singletons.
     */
    private static boolean mCreated;

    /**
     * Map for handling concurrent access (fast version).
     */
    private final ConcurrentHashMap<String, Item> mCacheMap
            = new ConcurrentHashMap<>();

    /**
     * Map for handling concurrent access (slow version).
     */
//    private final CacheMap<String, Cache.Item> mCacheMap
//            = new SynchronizedCacheMap();

    /**
     * Optional list of state change observers.
     */
    private final List<ObserverEntry> mObservers
            = new ArrayList<>();

    /**
     * Readers-writer support for observers list.
     */
    //private final StampedLock mObserversLock =
    // new StampedLock();
    private final ReentrantReadWriteLock mObserversLock
            = new ReentrantReadWriteLock();

    /**
     * Used to adjust the speed of the crawl using a sleep call.
     */
    private int mCrawlSpeed = 100;

    /**
     * The platform dependent root cache directory.
     */
    private File mCacheDir;

    /**
     * Constructor that binds the cache implementation to a
     * platform specific root directory, and ensures that this
     * class is a singleton.
     *
     * @param cacheDir The platform dependent root cache directory.
     */
    public Cache(File cacheDir) {
        // Ensure that this class remains a singleton.
        synchronized (this) {
            if (mCreated) {
                throw new RuntimeException(
                        "The cache must be implemented as a singleton.");
            }
            mCreated = true;
        }

        mCacheDir = cacheDir;

        // Ensure that the cache directory exits and
        // immediately load all previously cached files
        // from storage.
        //noinspection ResultOfMethodCallIgnored
        cacheDir.mkdirs();
        loadFromDisk();
    }

    /**
     * Recursively delete files in directory [dir]
     * and return count of deleted files/directories.
     */
    public static int deleteContents(File dir) {
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return 0;
        }

        return Arrays
                .stream(files)
                .map(file -> {
                    int deleted = 0;
                    if (file.isDirectory()) {
                        deleted = deleteContents(file);
                    }
                    if (file.delete()) {
                        deleted++;
                    }
                    return deleted;
                })
                .reduce((total, deleted) -> total + deleted)
                .orElse(0);
    }

    /**
     * General purpose cache traversal function that begins at the
     * specified [dir] directory and calls the provided [visit]
     * function passing in each discovered file. Directories are
     * not passed to the [visit] function.
     */
    public static int traverseCache(File dir, Function<File, Integer> function) {
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return 0;
        }

        return Arrays.stream(files).map(file -> {
            if (file.isDirectory()) {
                return traverseCache(file, function);
            } else {
                return function.apply(file);
            }
        }).reduce(0, (total, visited) -> total + visited);
    }

    /**
     * Returns the original web URL that was used to download the passed
     * cache file. No check is made to see if the passed file actually
     * currently exists in the cache. If, however, that passed cache file
     * has been passed through a transform, then it doesn't make sense
     * passing back the original image because it will be a different color.
     *
     * @param cacheFilePath A cache file path that originated from this class.
     * @return The web URL that was originally used to download the passed
     * cache file.
     */
    @NonNull
    public static String getSourceUriFromCacheUri(@NonNull String cacheFilePath) {
        try {
            String fileName = new File(cacheFilePath).getName();
            String[] split = fileName.split("-", 2);
            if (split.length != 2) {
                throw new IllegalArgumentException("Invalid cacheFileName argument.");
            }

            String transform = split[1];
            if (!NOTAG.equals(transform)) {
                // This is the original downloaded image, so return the original web image.
                String decoded = URLDecoder.decode(fileName.split("-", 2)[1], "UTF-8");
                if (decoded.startsWith("file:///android_assets/")) {
                    return "https://" + decoded.replace("file:///android_assets/", "");
                } else if (decoded.startsWith("file://")) {
                    return "https://" + decoded.replace("file://", "");
                } else {
                    throw new IllegalArgumentException("Invalid cacheFileName argument.");
                }
            } else {
                // Just return the passed cacheFile back as a proper uri since this
                // is a gray scale image.
                return new File(cacheFilePath).toURI().toString();
            }

        } catch (Exception e) {
            System.out.println("Cache: passed file path is not " +
                    "recognized as a file originating from this Cache class.");
            return "";
        }
    }

    /**
     * Maps a {@code uri} and {@code tag} pair to an relative cache path
     * suitable for file operations.
     *
     * @param uri The source uri of data object {@link String}.
     * @param tag A grouping tag {@link String} or null for the default group.
     * @return A constructed relative file path for the key/tag pair.
     */
    public String getRelativeCachePath(String uri, String tag) {
        // Get the file associated with the passed uri and tag pair.
        File file = getCacheFile(getEncodedKey(uri, tag));

        // Return the relative path of this file object.
        return file.getPath().replace(getCacheDir().getPath() + "/", "");
    }

    /**
     * Maps a {@code uri} and {@code tag} pair to an absolute cache path
     * suitable for file operations.
     *
     * @param key The item's cache key value.
     * @return A constructed absolute file path for the key.
     */
    public File getCacheFile(String key) {
        // Encode the uri and tag pair into a key and map that key to
        // it's associated File object.
        return mapKeyToFile(key);
    }

    /**
     * Maps an item's encoded cache key to its associated File object.
     * The current cache design uses the encoded key as the item's file
     * name.
     *
     * @param key Encoded cache key.
     * @return The item's associated File object on disk.
     */
    public File mapKeyToFile(String key) {
        return new File(getCacheDir(), key);
    }

    /**
     * Maps a cache file to it's associated encoded cache key.
     * The current cache design uses the encoded key as the item's file
     * name.
     *
     * @param file A cache file.
     * @return The item's associated encoded cache key.
     */
    public String mapFileToKey(File file) {
        // Make sure that the file has the required name format.
        String[] parts = file.getName().split("-", 2);
        if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
            fatal("Detected invalid cache file: " + file);
        }

        return file.getName();
    }

    /**
     * Adds an new {@link Observer} to the list of observers. The
     * observer will be notified when the {@code progress} of a
     * {@link Operation} has changed. To prevent memory leaks,
     * the specified {@link Observer} is stored as a {@link WeakReference}.
     *
     * @param observer              An implementation of {@link Observer} interface.
     * @param notifyCurrentContents if true observer will be notified about
     *                              the current cache contents.
     * @param operations            The operations to watch for or null for all operations.
     */
    public void startWatching(Observer observer,
                              boolean notifyCurrentContents,
                              Operation... operations) {
        // Construct filter list. An empty operations array defaults to
        // filter on all states.
        List<Operation> list = operations.length > 0
                ? Arrays.asList(operations)
                : mAllStates;

        // Acquire write lock for updating observers list.
        mObserversLock.writeLock().lock();

        try {
            // Remove this observer if already in the list.
            stopWatching(observer);

            // Add the observer to the list.
            mObservers.add(new ObserverEntry(observer, list));
        } finally {
            // Release the write lock.
            mObserversLock.writeLock().unlock();
        }

        // Immediately notify observer about all cache contents.
        if (notifyCurrentContents) {
            mCacheMap.forEach((key, item) ->
                    observer.event(Operation.LOAD, item, -1f));
        }
    }

    /**
     * Removes the specified {@link Observer} from the list of observers.
     *
     * @param observer An implementation of {@link Cache.Observer} interface.
     */
    public void stopWatching(Observer observer) {
        // Acquire write access to observers list.
        mObserversLock.readLock().lock();

        try {
            // Remove observer from the list of observers.
            // We can't simply list.remove because the passed
            // observer is wrapped in an ObserverEntry which
            // contains a WeakReference to this observer.
            mObservers.stream()
                    .filter(entry -> entry.getObserver() == observer)
                    .findFirst()
                    .ifPresent(mObservers::remove);
        } finally {
            // Release write lock.
            mObserversLock.readLock().unlock();
        }
    }

    /**
     * Gets a previously cached item.
     *
     * @param uri A uri where the image originates from {@link String}.
     * @param tag A grouping tag {@link String} or null for the default group.
     * @return The matching {@link Item} or null if no match is found.
     */
    public Item getItem(@NotNull String uri, @Nullable String tag) {
        String cacheKey = getEncodedKey(uri, tag);
        return mCacheMap.get(cacheKey);
    }

    /**
     * Attempts to add a new item to the cache. If the item is added {@code true}
     * is returned, if the item already exists, {@code false} is returned.
     *
     * @param uri A uri where the image originates from {@link String}.
     * @param tag A grouping tag {@link String} or null for the default group.
     * @param tag A Consumer lambda used to process the newly created item.
     * @return {@code true} if a new item was added, {@code false} if an item
     * with a matching uri and tag already exists.
     */
    public boolean addItem(@NotNull String uri,
                           @Nullable String tag,
                           Consumer<Item> consumer) {
        return internalAddOrGet(uri, tag, consumer).mWasAdded;
    }

    /**
     * Adds a new item or returns the existing item. The existing
     * item is returned.
     *
     * @param uri A uri where the image originates from {@link String}.
     * @param tag A grouping tag {@link String} or null for the default group.
     * @param tag A Consumer lambda used to process the newly created item.
     * @return A new Item if a matching item did not exist in the cache and was
     * created, and an existing item if the item already existed and was not
     * created.
     */
    public Item addOrGetItem(@NotNull String uri,
                             @Nullable String tag,
                             Consumer<Item> consumer) {
        return internalAddOrGet(uri, tag, consumer).mItem;
    }

    /**
     * Private implementation for adding a new item to the cache
     * that is called by addOrGetItem() and addItem(). The item
     * is created calling the cache map's computeIfAbsent which,
     * in turn, calls the passed lambda to create a new item.
     * In terms of concurrency, the computeIfAbsent call behave
     * as a single atomic operation. If two threads call this
     * method at roughly the same time, only one will actually
     * create an item and the other will receive that item as
     * the return value.
     * <p>
     * To support the different return values required by the
     * calling addOrGetItem method (requires and item return value)
     * and the addItem method (requires a boolean return value),
     * this method returns an AddOrGetResult wrapper class
     * that contains an {@code wasAdded} boolean and an {@code Item}
     * value.
     *
     * @param uri      The uri identifying the new item to create.
     * @param tag      A string tag to associate with the new item.
     * @param consumer An optional Java 8 Consumer lambda will
     *                 called once the item has been created.
     * @return An AddOrGetResult wrapper class instance containing
     * a wasAdded flag to indicate if the item was added,  and an
     * Item value which will be either the new item that was added
     * or an existing item if one already exists for this uri and
     * tag pair.
     */
    private AddOrGetResult internalAddOrGet(@NotNull String uri,
                                            @Nullable String tag,
                                            @Nullable Consumer<Item> consumer) {
        // Restriction: item uri can't begin with the default tag.
        if (uri.startsWith(NOTAG)) {
            fatal("Invalid argument: Item uri cannot begin with " + NOTAG);
        }

        // Restriction: Tags can't have spaces.
        if (tag != null && tag.contains(" ")) {
            throw new IllegalArgumentException("Item tag must not contain spaces");
        }

        // Build the unique encoded key from the uri and tag pair.
        String key = getEncodedKey(uri, tag);

        // Add the item to the hash map if it doesn't already exist.
        // The ConcurrentHashMap implementation will only call the
        // compute method to create a new item if there is no
        // item in the cache matching the given key.
        Item item =
                mCacheMap.computeIfAbsent(
                        key, string -> newItem(string, consumer));

        if (item == null) {
            fatal("computeIfAbsent returned null");
        }

        // Construct result value before clearing the wasJustCreated
        // thread local storage boolean.
        AddOrGetResult result = new AddOrGetResult(item, wasJustCreated.get());

        // Check the thread local storage boolean to see if this is the
        // thread actually created this item (set in newItem()). If so,
        // then this thread is responsible for notifying observers about
        // the item creation operation.
        if (wasJustCreated.get()) {
            notifyObservers(item, Operation.CREATE, -1f);

            // Unset the boolean to prevent this thread for sending
            // more than one create notification to observers.
            wasJustCreated.set(false);
        }

        // Return the aggregated result.
        return result;
    }

    /**
     * Removes the item associated with the passed key, and deletes
     * the item's file object. This method does nothing if the cache
     * does not contain a matching item.
     *
     * @param key The cache key of this item.
     * @return The removed item, or null if no matching item was found.
     */
    public Item remove(@NotNull String key) {
        Item item = mCacheMap.remove(key);
        if (item != null) {
            //noinspection ResultOfMethodCallIgnored
            item.mFile.delete();
            notifyObservers(item, Operation.DELETE, -1f);
        }

        return item;
    }

    /**
     * Removes all cache items (and their associated File objects)
     * that were create with the specified group [tag].
     *
     * @param tag A grouping tag {@link String} or null for the default group.
     */
    public void removeTagged(String tag) {
        mCacheMap.forEach((key, item) -> {
            if (tag.equals(item.getTag())) {
                remove(key);
            }
        });
    }

    /**
     * Removes all cached items and their associated files.
     */
    public void clear() {
        // @@Doug: please read this comment.

        // Can't remove cache entries within the forEach() block
        // unless the underlying CacheMap implementation is a
        // ConcurrentHashMap. As a workaround, delete all the cache
        // files first and then clear the map outside of forEach().
        // Ideally, a ReentrantReadWrite lock should be used here.
        mCacheMap.forEach((key, value) -> {
            Item item = mCacheMap.get(key);
            if (item != null) {
                //noinspection ResultOfMethodCallIgnored
                if ((item.mFile.exists())) {
                    if (!item.mFile.delete()) {
                        fatal("Unable to delete file: " + item.mFile);
                    } else {
                        System.out.println("Deleted file: " + item.mFile);
                    }
                    notifyObservers(item, Operation.DELETE, -1f);
                }
            }
        });
        mCacheMap.clear();

        // Sanity check.
        File[] files = mCacheDir.listFiles();
        if (files != null && files.length > 0) {
            warn("Cache cleared, but " + files.length + " files still " +
                    "exist in cache directory $cacheDir");
        }
    }

    /**
     * @return The root cache directory set by the platform subclass.
     */
    public File getCacheDir() {
        return mCacheDir;
    }

    /**
     * Returns the number of cached items.
     */
    public int getCacheSize() {
        return mCacheMap.size();
    }

    /**
     * Called once when the application starts to create cache entries
     * in the cacheMap matching all files in the default cache directory.
     * A sweep is performed first to remove any rogue or empty files.
     */
    public int loadFromDisk() {
        mCacheMap.clear();

        int swept = sweepCache();
        if (swept > 0) {
            info("Swept " + swept + " files from cache.");
        }

        int loaded = traverseCache(mCacheDir, file -> {
            Item item = newItemFromFile(file);
            mCacheMap.put(item.mKey, item);
            notifyObservers(item, Operation.LOAD, 1f);
            return 1;
        });

        info("Loaded " + loaded + " cache items from disk.");
        return loaded;
    }

    /**
     * Notifies all interested {@link Observer}s when the {@code progress}
     * of the current {@link Operation} being performed on a {@link Item}
     * has changed.
     *
     * @param item      Item undergoing a change.
     * @param operation The operation being performed on the item.
     * @param progress  The current progress of the operation or -1
     *                  to indicate that the operation is atomic.
     */
    private void notifyObservers(Item item,
                                 Operation operation,
                                 Float progress) {
        if (mCrawlSpeed < 100) {
            addSimulatedDelay(operation);
        }

        // Acquire read access to the observers list.
        mObserversLock.readLock().lock();

        try {
            // Notify all interested observers.
            mObservers.stream()
                    .filter(o -> o.getFilter().contains(operation))
                    .forEach(o -> {
                        Observer observer = o.getObserver();
                        if (observer != null) {
                            observer.event(operation, item, progress);
                        }
                    });
        } finally {
            // Release the read lock.
            mObserversLock.readLock().unlock();
        }
    }

    /**
     * Adds an artificial delay to all computationally
     * intense operations.
     */
    private void addSimulatedDelay(Operation operation) {
        // Only delay computationally intense operations.
        switch (operation) {
            case READ:
            case DOWNLOAD:
            case WRITE:
            case TRANSFORM: {
                // Convert speed to a delay between 0 and 500.
                int duration = (int) ((100 - mCrawlSpeed) / 100f * 500);
                // Sleep in intervals so that cancelling is responsive.
                int interval = Math.min(10, duration);
                do {
                    ImageCrawler.throwExceptionIfCancelled();
                    try {
                        Thread.sleep(interval);
                    } catch (Exception e) {
                        break;
                    }
                    duration -= interval;
                } while (duration > 0);
            }
        }
    }

    /**
     * @return The current crawl speed.
     */
    public int getCrawlSpeed() {
        return mCrawlSpeed;
    }

    /**
     * Sets the crawl speed.
     *
     * @param speed Value from 0 to 100%
     */
    public void setCrawlSpeed(int speed) {
        mCrawlSpeed = speed;
    }

    /**
     * Returns a new cache Item object for the specified unique cache key.
     * This method should only be called indirectly by the addItem()
     * method.
     *
     * @param key A unique cache key value for this new item.
     */
    private Item newItem(String key, @Nullable Consumer<Item> consumer) {
        // Construct a new item passing in the encoded key, associated
        // file object, and the current creation time.
        Item item = new Item(key, getCacheFile(key), System.nanoTime());

        // Since it's impossible to guarantee the integrity of the
        // underlying externally accessible file system, ensure that
        // if an orphaned file matching the the uri/tag pair already
        // exists in the cache directory, delete it.
        if (item.mFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            item.mFile.delete();
            warn("Orphaned file matching a new item found and deleted: "
                    + item.mFile);
        }

        // Create a new empty file for this item.
        try {
            if (!item.mFile.createNewFile()) {
                throw new IOException(
                        "Unable to create new cache item file: " + item.mFile);
            }

            // Now call the consumer if one was specified.
            if (consumer != null) {
                consumer.accept(item);
            }
        } catch (Exception e) {
            // Wrap IOException and throw.
            throw new RuntimeException(e);
        }

        info("Thread [" + Thread.currentThread().getId() +
                "]: " + item + " ADDED.");

        // Set the thread local storage boolean to true so that the calling
        // routine can determine if that the item was created.
        wasJustCreated.set(true);

        // Return the item.
        return item;
    }

    /**
     * Creates a new item instance for the specified cache file.
     * This method should only be called from the loadCache method
     * and from unit tests.
     *
     * @param file The cache file on disk.
     * @return A new item instance for the cached file.
     */
    public Item newItemFromFile(File file) {
        if (!file.exists()) {
            fatal("Invalid argument: file does not exist.");
        }

        if (!file.isFile()) {
            fatal("Invalid argument: file is not a file.");
        }

        if (!file.getPath().startsWith(getCacheDir().getPath())) {
            fatal("Invalid argument: the specified file is " +
                    "not in the cache directory: " + file);
        }

        // Deconstruct file path into it's associated encoded cache key
        // and create a new item using that key, the file, and the
        // current time of creation..
        return new Item(mapFileToKey(file), file, System.nanoTime());
    }

    /**
     * Centralizes the mapping of a url and tag to a cache lookup key.
     * To avoid potential key conflicts, this method is the only method
     * that constructs the cache key from the url and tag.
     */
    private String getEncodedKey(@NotNull String uri, @Nullable String tag) {
        // Urls may be either
        //      "http://<path>",
        //      "https://<path>",
        //      "file://java_resources/<path>,
        //      "file:///android_assets/<path> - Android requires 3 /// chars.
        //      "file://project_root/<path>
        // To facilitate testing which compares the downloaded cache
        // with a ground-truth directory, strip off the locator prefix
        // so that cache files will have the same names no matter where
        // they originated.
        String key;

        if (uri.startsWith(Platform.PROJECT_URI_PREFIX)) {
            key = uri.replace(Platform.PROJECT_URI_PREFIX, "");
        } else if (uri.startsWith(Platform.ASSETS_URI_PREFIX)) {
            key = uri.replace(Platform.ASSETS_URI_PREFIX, "");
        } else if (uri.startsWith(Platform.RESOURCES_URI_PREFIX)) {
            key = uri.replace(Platform.RESOURCES_URI_PREFIX, "");
        } else if (uri.startsWith(Platform.HTTP_URI_PREFIX)) {
            key = uri.replace(Platform.HTTP_URI_PREFIX, "");
        } else if (uri.startsWith(Platform.HTTPS_URI_PREFIX)) {
            key = uri.replace(Platform.HTTPS_URI_PREFIX, "");
        } else if (uri.startsWith("https://")) {
            key = uri.replace("https://", "");
        } else {
            key = uri;
        }

        if (key.startsWith("/")) {
            key = key.substring(1);
        }

        // The cache key is <tag>-<encoded key>
        try {
            return (tag != null ? tag : NOTAG) +
                    "-" +
                    URLEncoder.encode(key, "UTF-8");
        } catch (Exception e) {
            // Wrap and resend the exception.
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes invalid or 0 length files from the cache.
     */
    private int sweepCache() {
        return traverseCache(mCacheDir, file -> {
            // Delete any empty files that may have been orphaned
            // if a previous application invocation terminated
            // abnormally.
            if (file.length() == 0L) {
                info("Removing orphaned empty file from the cache: " + file);
                if (!file.delete() || file.exists()) {
                    fatal("Unable to delete cache file: " + file);
                }

                return 1;
            }

            // Call mapFileToKey which will throw an exception if this
            // file is not a valid cache file.
            try {
                mapFileToKey(file);
            } catch (Exception e) {
                warn("Removing unknown file from cache: " + file);

                // When deleting files, it's always safest recheck that
                // the file, is in fact, safe to delete.
                if (!file.getPath().startsWith(getCacheDir().getPath())) {
                    fatal("Only cache files can be swept.");
                }

                if (!file.delete() || file.exists()) {
                    fatal("Unable to delete cache file: " + file);
                }

                return 1;
            }

            return 0;
        });
    }

    /**
     * Throws a runtime exceptions with the specified message.
     *
     * @param msg Exception message.
     */
    private void fatal(String msg) {
        throw new RuntimeException(msg);
    }

    /**
     * Outputs a warning message.
     *
     * @param msg The message
     */
    private void warn(String msg) {
        System.out.println(TAG + "[WARNING]: " + msg);
    }

    /**
     * Outputs an informational message.
     *
     * @param msg The message.
     */
    private void info(String msg) {
        System.out.println(TAG + "[DEBUG]: " + msg);
    }

    /**
     * Outputs a debug message if the global debug options flat is set.
     *
     * @param msg The message
     */
    private void debug(String msg) {
        if (Options.mDebug) {
            System.out.println(TAG + "[DEBUG]: " + msg);
        }
    }

    /**
     * Operations that are used when notifying observers of an item change.
     */
    public enum Operation {
        LOAD,
        DOWNLOAD,
        CREATE,
        DELETE,
        READ,
        WRITE,
        TRANSFORM,
        CLOSE
    }

    /**
     * An observer interface with a single event function that
     * can be notified about any desired cache [State] changes.
     */
    public interface Observer {
        void event(Operation operation, Cache.Item item, Float progress);
    }

    /**
     * Used as a return value for internal addOrGet
     * method so support 2 return values (yuck).
     */
    class AddOrGetResult {
        boolean mWasAdded;
        Item mItem;

        AddOrGetResult(Item item, boolean wasAdded) {
            mItem = item;
            mWasAdded = wasAdded;
        }
    }

    /**
     * Immutable entry used in observers list. The contained
     * Observer is stored as a weak reference.
     */
    private class ObserverEntry {
        private final List<Operation> filter;
        private final WeakReference<Observer> observerRef;

        ObserverEntry(Observer observer,
                      List<Operation> filter) {
            this.filter = filter;
            this.observerRef = new WeakReference<>(observer);
        }

        Observer getObserver() {
            return observerRef.get();
        }

        List<Operation> getFilter() {
            return filter;
        }
    }

    /**
     * Immutable item class provides input and output streams, keeping
     * the underlying storage implementation details hidden from
     * the application.
     */
    public class Item {
        final String mKey;
        final File mFile;
        final int mSize = 0;
        long mTimeStamp = 0L;

        public Item(String key, File file, long timeStamp) {
            this.mKey = key;
            mFile = file;
            mTimeStamp = timeStamp;
        }

        /**
         * Builds a cache key from the item key and tag. The cache key is the
         * file name of the File object associated with this item.
         *
         * @return The cache key. Currently the file name of the cached item.
         */
        public String getCacheKey() {
            return mFile.getName();
        }

        /**
         * Reconstructs the web uri used to create this item. Https is
         * prepended to the decoded path which is really on an educated
         * guess about the authority of this item (since that information
         * is not saved once the item has been created).
         */
        public String getSourceUri() {
            try {
                String decoded = URLDecoder.decode(mKey.split("-", 2)[1], "UTF-8");

                // The Options class maintains a static String URL resource locator
                // value based on whether the crawl is local or remote, and whether
                // it is an Android or Java based crawl. Each combination will yeild
                // a different resource locator value indicating the location of
                // the image files being crawled.
                return Options.getRootUrlLocator() + "/" + decoded;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * @return The original item key used when the item was first created.
         */
        public String getKey() {
            return mKey;
        }

        /**
         * @return The item group tag used when the time was created.
         */
        public String getTag() {
            try {
                return URLDecoder.decode(mKey.split("-", 2)[0], "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * @return The item File object.
         */
        public File getFile() {
            return mFile;
        }

        /**
         * @return The item creation time.
         */
        public long getTimestamp() {
            return mTimeStamp;
        }

        /**
         * @return The current size of the item file or 0 if no file exists.
         */
        public int getSize() {
            return mFile.exists() ? (int) mFile.length() : 0;
        }

        /**
         * Returns an observable input stream for this item.
         *
         * @param operation The expected operation to be performed with this stream.
         * @return An observable input stream.
         */
        public InputStream getInputStream(Operation operation) {
            try {
                return new ObserverInputStream(
                        new FileInputStream(mFile), operation, this, mSize);
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * Returns an observable output stream for this item.
         *
         * @param operation The expected operation to be performed with this stream.
         * @return An observable output stream.
         */
        public OutputStream getOutputStream(Operation operation, int size)
                throws FileNotFoundException {
            return new ObserverOutputStream(
                    new FileOutputStream(mFile), operation, this, size);
        }

        @SuppressWarnings("unused") // bytes might be useful at some point
        public void progress(Operation operation, Float progress, int bytes) {
            notifyObservers(this, operation, progress);
        }

        /**
         * For item comparisons.
         *
         * @param o The item to compare to.
         * @return true if this time equals the passed one.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Item item = (Item) o;

            return getKey().equals(item.getKey())
                    && getFile().equals(item.getFile());
        }

        /**
         * Hash code generator for efficient comparisons.
         *
         * @return A hash code.
         */
        @Override
        public int hashCode() {
            int result = getKey().hashCode();
            result = 31 * result + getFile().hashCode();
            return result;
        }

        /**
         * @return Provides more readable string output.
         */
        @Override
        public String toString() {
            return "Item(url='" + getSourceUri() + "', tag='" + getTag() + "')";
        }
    }

    /**
     * A filtered input stream implementation that notifies observers
     * when the item's file is written to.
     */
    public class ObserverInputStream extends FilterInputStream {
        final InputStream mInputStream;
        final Operation mOperation;
        final Item mItem;
        final int mSize;

        /**
         * The number of bytes that have been read from the InputStream
         */
        private int bytesRead;

        /**
         * Creates a <code>FilterInputStream</code>
         * by assigning the  argument <code>in</code>
         * to the field <code>this.in</code> so as
         * to remember it for later use.
         *
         * @param in        the underlying input stream, or <code>null</code> if
         *                  this instance is to be created without an underlying stream.
         * @param operation
         * @param item
         * @param size
         */
        public ObserverInputStream(InputStream in,
                                   Operation operation,
                                   Item item,
                                   int size) {
            super(in);
            mInputStream = in;
            mOperation = operation;
            mItem = item;
            mSize = size;
        }

        @Override
        public int available() {
            return mSize - bytesRead;
        }

        @Override
        public void close() throws IOException {
            super.close();
            Cache.notify(mItem, Operation.CLOSE, 1f, mSize);
        }

        @Override
        public int read() throws IOException {
            int bytes = super.read();
            if (bytes != -1) {
                bytesRead++;
                Cache.notify(mItem, mOperation, (float) bytesRead / mSize, mSize);
            } else {
                log("EOF total = " + mSize + " read = " + bytesRead);
            }
            return bytes;
        }

        @Override
        public int read(@NotNull byte[] b, int off, int len) throws IOException {
            ImageCrawler.throwExceptionIfCancelled();

            int read = super.read(b, off, len);
            if (read != -1) {
                bytesRead += read;
                Cache.notify(mItem, mOperation, (float) bytesRead / mSize, mSize);
            } else {
                log("EOF total = " + mSize + " read = " + bytesRead);
            }

            return read;
        }

        private void log(String msg) {
            if (false) {
                String func = "ObserverInputStream";
                String name = new File(mItem.mKey).getName();
                debug(func + "[" + name + "|" + mItem.getTag() + "]: " + msg);
            }
        }
    }

    /**
     * A filtered input stream implementation that notifies observers
     * when the item's file is being read from.
     */
    public class ObserverOutputStream extends FilterOutputStream {
        final OutputStream mOutputStream;
        final Operation mOperation;
        final Item mItem;
        final int mSize;
        int mBytesWritten = 0;

        /**
         * Creates a <code>FilterOutputStream</code>
         * by assigning the  argument <code>out</code>
         * to the field <code>this.out</code> so as
         * to remember it for later use.
         *
         * @param out       the underlying output stream, or <code>null</code> if
         *                  this instance is to be created without an underlying stream.
         * @param operation
         * @param item
         * @param size
         */
        public ObserverOutputStream(OutputStream out,
                                    Operation operation,
                                    Item item,
                                    int size) {
            super(out);
            mOutputStream = out;
            mOperation = operation;
            mItem = item;
            mSize = size;
        }

        @Override
        public void write(int b) throws IOException {
            super.write(b);
            mBytesWritten++;
        }

        @Override
        public void close() throws IOException {
            super.close();
            Cache.notify(mItem, Operation.CLOSE, 1f, mSize);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            ImageCrawler.throwExceptionIfCancelled();
            super.write(b, off, len);
            // Image saving will always be about 1/3 the original
            // size, so adjust the total size here so that the
            // entire operation will complete at roughly 100%.
            Cache.notify(mItem, mOperation, mBytesWritten / (mSize / 3f), mSize);
        }

        private void log(String msg) {
            String func = "ObserverOutputStream";
            String name = new File(mItem.mKey).getName();
            debug(func + "[" + name + "|" + mItem.getTag() + "]: " + msg);
        }
    }

    /**
     * Common helper used by ObserverInputStream and ObserverOutputStream to ensure
     * that cancellation is handled consistently by both classes.
     *
     * @throws IOException
     */
    private static void notify(
            Item item, Operation operation, Float progress, int size) throws IOException {
        if (Thread.interrupted()) {
            // Clear interrupted flag and throw an IO based exception.
            throw new ClosedByInterruptException();
        }

        item.progress(operation, progress, size / 5);
    }
}

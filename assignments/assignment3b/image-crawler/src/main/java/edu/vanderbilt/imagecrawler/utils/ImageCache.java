package edu.vanderbilt.imagecrawler.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * An abstract File implementation of the StreamCache interface that
 * can be used by different platforms subclasses to provide the root
 * cache directory. The only abstract method is getCacheDir().
 */
public class ImageCache implements StreamCache<String> {
    /**
     * The length of time before any cached item is considered stale.
     */
    private Duration duration = Duration.ofDays(1);

    /**
     * A platform dependant Supplier lambda used to access the platform
     * cache's root directory.
     */
    private Supplier<File> cacheDirSupplier;

    /**
     * Constructor that receives a supplier lambda used to
     * provide the platform dependant cache root directory.
     *
     * @param cacheDirSupplier A Java8 Supplier.
     */
    public ImageCache(Supplier<File> cacheDirSupplier) {
        this.cacheDirSupplier = cacheDirSupplier;
    }

    /**
     * Recursively delete files in a specified directory.
     */
    public static int deleteContents(File dir) {
        File[] files = dir.listFiles();
        if (files == null) {
            return 0;
        } else {
            return Stream.of(files)
                         .mapToInt(file -> {
                             if (file.isDirectory()) {
                                 int count = deleteContents(file);
                                 file.delete();
                                 return count;
                             } else {
                                 return file.delete() ? 1 : 0;
                             }
                         })
                         .sum();
        }
    }

    /**
     * @return Platform specific cache location.
     */
    public File getCacheDir() {
        return cacheDirSupplier.get();
    }

    /**
     * Checks if the item exists in the cache. If the item exists but
     * is stale, it will be deleted and false will be returned.
     *
     * @param groupId The parent group.
     * @param itemId  The item id.
     * @return true if item is in the cache, false if not.
     */
    @Override
    public boolean isItemCached(String groupId, String itemId) {
        File file = new Item(groupId, itemId).getFile();
        deleteIfState(file);
        return file.exists();
    }

    /**
     * Creates a new item. If an item already exists in the specified
     * group, then an RuntimeException is thrown. To avoid this exception,
     * call the isItemCached method first.
     *
     * @param groupId The parent group.
     * @param itemId  The item id.
     * @return The new item.
     */
    @Override
    public Item newItem(String groupId, String itemId) {
        try {
            Item item = new Item(groupId, itemId);
            File file = item.getFile();
            deleteIfState(file);
            file.getParentFile().mkdirs();
            if (!item.getFile().createNewFile()) {
                throw new RuntimeException(
                        "ImageCache: unable to create new item " +
                                "(" + groupId + ", " + itemId + ")"
                                + " - item already exists.");
            }
            return item;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method that deletes a file if it's stale.
     *
     * @param file The file to check and delete if stale.
     * @return True if the file existed, was stale, and was deleted.
     */
    private boolean deleteIfState(File file) {
        if (file.exists()) {
            Duration itemAge = Duration.ofMillis(file.lastModified());
            if (!itemAge.plus(duration).minusMillis(System.currentTimeMillis()).isNegative()) {
                return file.delete();
            }
        }

        return false;
    }

    /**
     * Returns an existing item or null if it doesn't exist. This method
     * will not delete the item if it's stale because it should only be
     * called to get an existing item, and not to check if an item exists.
     *
     * @param groupId The parent group.
     * @param itemId  The item id.
     * @return The item, or null if the item doesn't exist in the cache.
     */
    @Override
    public Item getItem(String groupId, String itemId) {
        Item item = new Item(groupId, itemId);
        return item.getFile().exists() ? item : null;
    }

    /**
     * Deletes a previously cached item.
     *
     * @param groupId The parent group.
     * @param itemId  The item id.
     * @return true if the item was deleted, false if it didn't exist
     * or could not be deleted.
     */
    @Override
    public boolean deleteItem(String groupId, String itemId) {
        return new Item(groupId, itemId).getFile().delete();
    }

    /**
     * Deletes an entire group of items.
     *
     * @param groupId The group to delete.
     * @return The number of items that were deleted.
     */
    @Override
    public int deleteGroup(String groupId) {
        return deleteContents(new File(getCacheDir(), groupId));
    }

    /**
     * Clears the entire cache.
     *
     * @return The number of deleted items.
     */
    @Override
    public int clear() {
        return deleteContents(getCacheDir());
    }

    /**
     * Returns the amount of time that an item is allowed to exist
     * in the cache.
     *
     * @return The lifespan of cache items.
     */
    @Override
    public Duration getItemDuration() {
        return null;
    }

    /**
     * Sets the lifespan of all cached items. Items that exceed this
     * duration will be considered stale and will be removed when
     * newItem or isItemCached methods are called using the same group id
     * and item id of the stale item.
     *
     * @param duration Any duration.
     */
    @Override
    public void setItemDuration(Duration duration) {
        this.duration = duration;
    }

    /**
     * Cached Item class.
     */
    public class Item implements StreamCache.Item<String> {
        private final String groupId;
        private final String itemId;

        protected Item(String groupId, String itemId) {
            this.groupId = groupId;
            this.itemId = itemId;
        }

        /**
         * @return The item id.
         */
        @Override
        public String getId() {
            return itemId;
        }

        /**
         * @return The item parent group id.
         */
        @Override
        public String getGroupId() {
            return groupId;
        }

        /**
         * @return A relative item path string with the groupId has its top parent.
         */
        public String getItemPath() {
            return groupId
                    + File.separator
                    + UriUtils.mapUriToRelativePath(itemId);
        }

        /**
         * Returns the actual file path associated with the item.
         *
         * @return The
         */
        public String getFileSystemPath() {
            return getCacheDir().getAbsolutePath()
                    + File.separator
                    + getItemPath();
        }

        /**
         * @return The item's File object.
         */
        public File getFile() {
            return new File(getFileSystemPath());
        }

        /**
         * Copies the item contents into the passed stream.
         *
         * @param out The destination stream.
         * @return The number of bytes copied.
         */
        @Override
        public long readData(OutputStream out) {
            try {
                return IOUtils.copy(new FileInputStream(getFile()), out);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Copies the contents of the passed stream into the item's File object.
         * Any existing contents will be overwritten.
         *
         * @param in The source stream.
         * @return The number of bytes copied.
         */
        @Override
        public long writeData(InputStream in) {
            try {
                return IOUtils.copy(in, new FileOutputStream(getFile()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * @return An output stream that can be used to read the item's contents.
         */
        @Override
        public OutputStream getOutputStream() {
            try {
                return new FileOutputStream(getFile());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * @return A input stream that can be used to modify the item's contents.
         */
        @Override
        public InputStream getInputStream() {
            try {
                return new FileInputStream(getFile());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}

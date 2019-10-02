package edu.vanderbilt.imagecrawler.platform;

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A simple CacheMap implementation that wraps a HashMap collection and
 * uses synchronized functions to provide thread-safe access to the
 * wrapped HashMap.
 *
 * There are two other ways that synchronization could be handle in
 * this class. Each method could be tagged synchronize keyword which
 * will synchronize on the class object, or the current synchronize
 * statements could synchronize on 'this' instead of 'mMap'.
 */
class SynchronizedCacheMap
        implements CacheMap<String, Cache.Item> {
    /**
     * The standard Java HashMap.
     */
    private final HashMap<String, Cache.Item> mMap = new HashMap<>();

    /**
     * Clears all entries in the map.
     */
    @Override
    public void clear() {
        synchronized (mMap) {
            mMap.clear();
        }
    }

    /**
     * Associates the specified value with the specified mKey in this
     * map.  If the map previously contained a mapping for the mKey,
     * the old value is replaced.
     *
     * @param key   mKey with which the specified value is to be associated
     * @param value value to be associated with the specified mKey
     * @return the previous value associated with <tt>mKey</tt>, or
     * <tt>null</tt> if there was no mapping for <tt>mKey</tt>.
     * (A <tt>null</tt> return can also indicate that the map
     * previously associated <tt>null</tt> with <tt>mKey</tt>.)
     */
    public Cache.Item put(String key, Cache.Item value) {
        synchronized (mMap) {
            return mMap.put(key, value);
        }
    }

    /**
     * Attempts to add a new entry to the map identified by the passed
     * {@code mKey}. If the the map doesn't already contain an entry
     * with a matching mKey, the a new entry is added and its data
     * value is set to {@code value} and true is returned. If an entry
     * with the specified {@code mKey} already exists, the entry's
     * value is returned.
     *
     * @param key    The mKey for the new entry.
     * @param mapper A lambda that maps the provided mKey to an
     *               entry value to be added to the map.
     * @return The added value if it was added, or an existing value if a
     * entry with the specified mKey already existed.
     */
    @Override
    public Cache.Item computeIfAbsent(String key,
            Function<? super String, ? extends Cache.Item> mapper) {
        synchronized (mMap) {
            // Perform "check-then-act" operations with the lock held.
            if (mMap.containsKey(key)) {
                return mMap.get(key);
            } else {
                Cache.Item item = mapper.apply(key);
                mMap.put(key, item);
                return item;
            }
        }
    }

    /**
     * Returns a data value from the map that matches
     * the specified {@code mKey} or null if no matching
     * mKey was found.
     *
     * @param key The mKey to lookup.
     */
    @Override
    public Cache.Item get(String key) {
        synchronized (mMap) {
            return mMap.get(key);
        }
    }

    /**
     * Removes the entry that matches the specified {@code mKey}
     * and returns the removed entries data object.
     *
     * @param key The entry's mKey.
     * @return The removed entry data value, or null if the cache
     * does not contain an entry with a matching mKey.
     */
    @Override
    public Cache.Item remove(String key) {
        synchronized (mMap) {
            return mMap.remove(key);
        }
    }

    /**
     * @return Number of entries in the map.
     */
    @Override
    public int size() {
        synchronized (mMap) {
            return mMap.size();
        }
    }

    /**
     * Enumerates all entries in the map and calls the {@code action}
     * BiConsumer passing in each entry's mKey and value as parameters.
     *
     * @param action A BiConsumer that receives a the each entry's
     *               mKey and value.
     */
    @Override
    public void forEach(BiConsumer<? super String, ? super Cache.Item> action) {
        synchronized (mMap) {
            mMap.forEach(action);
        }
    }
}

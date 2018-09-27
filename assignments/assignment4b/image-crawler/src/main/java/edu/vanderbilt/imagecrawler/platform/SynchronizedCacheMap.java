package edu.vanderbilt.imagecrawler.platform;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A simple CacheMap implementation that wraps a HashMap collection and
 * uses synchronized functions to provide thread-safe access to the
 * wrapped HashMap.
 */
class SynchronizedCacheMap 
      implements CacheMap<String, Cache.Item> {
    /**
     * The standard Java HashMap.
     */
    private final HashMap<String, Cache.Item> map = new HashMap<>();

    /**
     * Clears all entries in the map.
     */
    @Override
    public void clear() {
        synchronized(this) {
            map.clear();
        }
    }

    /**
     * Associates the specified value with the specified key in this
     * map.  If the map previously contained a mapping for the key,
     * the old value is replaced.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     * <tt>null</tt> if there was no mapping for <tt>key</tt>.
     * (A <tt>null</tt> return can also indicate that the map
     * previously associated <tt>null</tt> with <tt>key</tt>.)
     */
    public Cache.Item put(String key, Cache.Item value) {
        synchronized(this) {
            return map.put(key, value);
        }
    }

    /**
     * Attempts to add a new entry to the map identified by the passed
     * {@code key}. If the the map doesn't already contain an entry
     * with a matching key, the a new entry is added and its data
     * value is set to {@code value} and true is returned. If an entry
     * with the specified {@code key} already exists, the entry's
     * value is returned.
     *
     * @param key    The key for the new entry.
     * @param mapper A lambda that maps the provided key to an
     *               entry value to be added to the map.
     * @return The added value if it was added, or an existing value if a
     * entry with the specified key already existed.
     */
    @Override
    public Cache.Item computeIfAbsent(String key,
                                      Function<? super String, ? extends Cache.Item> mapper) {
        synchronized(this) {
            if (map.containsKey(key)) {
                return map.get(key);
            } else {
                Cache.Item item = mapper.apply(key);
                map.put(key, item);
                return item;
            }
        }
    }

    /**
     * Returns a data value from the map that matches
     * the specified {@code key} or null if no matching
     * key was found.
     *
     * @param key The key to lookup.
     */
    @Override
    public Cache.Item get(String key) {
        synchronized(this) {
            return map.get(key);
        }
    }

    /**
     * Removes the entry that matches the specified {@code key}
     * and returns the removed entries data object.
     *
     * @param key The entry's key.
     * @return The removed entry data value, or null if the cache
     * does not contain an entry with a matching key.
     */
    @Override
    public Cache.Item remove(String key) {
        synchronized(this) {
            return map.remove(key);
        }
    }

    /**
     * Checks if the map contains the specified key.
     *
     * @param key The key to lookup.
     * @return {@code true} if a matching key is found,
     * {@code false} if not found.
     */
    @Override
    public boolean containsKey(String key) {
        synchronized(this) {
            return map.containsKey(key);
        }
    }

    /**
     * @return Number of entries in the map.
     */
    @Override
    public int size() {
        synchronized(this) {
            return map.size();
        }
    }

    /**
     * Enumerates all entries in the map and calls the {@code action}
     * BiConsumer passing in each entry's key and value as parameters.
     *
     * @param action A BiConsumer that receives a the each entry's
     *               key and value.
     */
    @Override
    public void forEach(BiConsumer<? super String, ? super Cache.Item> action) {
        synchronized(this) {
            try {
                map.forEach(action);
            } catch (ConcurrentModificationException e) {
                System.out.println("Foobar!");
            }
        }
    }
}

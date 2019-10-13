package edu.vanderbilt.imagecrawler.platform;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Defines an interface for a CacheMap that's used by the Cache class
 * in Cache.java.
 */
public interface CacheMap<K, V> {
    /**
     * Clears all entries in the map.
     */
    void clear();

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
    V put(K key, V value);

    /**
     * Attempts to add a new entry to the map identified by the passed
     * {@code mKey}. If the the map doesn't already contain an entry with
     * a matching mKey, the a new entry is added and its data value is set
     * to {@code value} and true is returned. If an entry with the specified
     * {@code mKey} already exists, the entry's value is returned.
     *
     * @param key      The mKey for the new entry.
     * @param mapper   A lambda that maps the provided mKey to an
     *                 entry value to be added to the map.
     * @return The added value if it was added, or an existing value if a
     * entry with the specified mKey already existed.
     */
    V computeIfAbsent(K key, Function<? super K, ? extends V> mapper);

    /**
     * Returns a data value from the map that matches
     * the specified {@code mKey} or null if no matching
     * mKey was found.
     *
     * @param key The mKey to lookup.
     */
    V get(K key);

    /**
     * Removes the entry that matches the specified {@code mKey}
     * and returns the removed entries data object.
     *
     * @param key The entry's mKey.
     * @return The removed entry data value, or null if the cache
     * does not contain an entry with a matching mKey.
     */
    V remove(K key);

    /**
     * @return Number of entries in the map.
     */
    int size();

    /**
     * Enumerates all entries in the map and calls the {@code action}
     * BiConsumer passing in each entry's mKey and value.
     *
     * @param action A BiConsumer that receives a the each entry's
     *               mKey and value.
     */
    void forEach(BiConsumer<? super K, ? super V> action);
}

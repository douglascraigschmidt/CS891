package edu.vanderbilt.imagecrawler.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;

/**
 * An implementation of a Cache that uses strings as identifiers
 * and files as the cache storage method. This cache is designed
 * to be thread-safe through the use of lock files.
 */
interface StreamCache<IdType> {
    boolean isItemCached(IdType groupId, IdType itemId);

    Item<IdType> newItem(IdType groupId, IdType itemId);

    Item<IdType> getItem(IdType groupId, IdType itemId);

    void setItemDuration(Duration duration);

    boolean deleteItem(IdType groupId, IdType itemId);

    int deleteGroup(IdType groupId);

    int clear();

    Duration getItemDuration();

    interface Item<IdType> {
        IdType getId();

        String getGroupId();

        long readData(OutputStream outputStream);

        long writeData(InputStream inputStream);

        OutputStream getOutputStream();

        InputStream getInputStream();
    }
}

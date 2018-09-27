package edu.vanderbilt.imagecrawler.utils;

import java.time.Duration;

/**
 * Created by monte on 2017-09-17.
 */

public interface Cache<IdType, DataType> {
    /**
     * @param itemId
     */
    boolean isItemCached(IdType itemId);

    /**
     * @param itemId
     */
    Item<IdType, DataType> getOrReserve(IdType itemId);

    /**
     *
     * @param itemId
     */
    Item<IdType, DataType> getCacheItem(IdType itemId);

    /**
     *
     * @param duration
     */
    void setItemDuration(Duration duration);

    /**
     * Remove cached item.
     */
    Item<IdType, DataType> removeItem(IdType itemId);

    /**
     *
     */
    interface Item<IdType, DataType> {
        /**
         *
         * @return
         */
        IdType getId();

        /**
         *
         * @return
         */
        DataType getData();

        /**
         *
         * @param data
         */
        void setData(DataType data);
    }
}

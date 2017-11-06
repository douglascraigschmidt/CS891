package edu.vanderbilt.imagecrawler.crawlers;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

import edu.vanderbilt.imagecrawler.crawlers.framework.ImageCrawler;
import edu.vanderbilt.imagecrawler.transforms.Transform;
import edu.vanderbilt.imagecrawler.utils.Crawler;
import edu.vanderbilt.imagecrawler.utils.Image;
import edu.vanderbilt.imagecrawler.utils.WebPageElement;

import static edu.vanderbilt.imagecrawler.utils.Crawler.Type.IMAGE;
import static edu.vanderbilt.imagecrawler.utils.Crawler.Type.PAGE;

/**
 * This implementation strategy uses Java 8 functional programming
 * features, the Java 8 fork-join pool framework to perform an "image
 * crawl" starting from a root Uri.  Images from HTML page reachable
 * from the root Uri are downloaded from a remote web server or the
 * local file system and the results are stored in files that can be
 * displayed to the user.
 */
public class ForkJoinCrawler
       extends ImageCrawler {
    /**
     * Perform the web crawl.
     *
     * @param pageUri The URL that we're crawling at this point
     * @param depth The current depth of the recursive processing
     * @return The number of images downloaded/stored/transformed
     */
    @Override
    protected int performCrawl(String pageUri, int depth) {
        // Return the result of performing the URLCrawlerTask
        // computations in the common pool of the ForkJoinPool.
        // TODO -- you fill in here (replace null with proper code).
        return null;
    }

    /**
     * Perform a web crawl from a particular starting point.  By
     * extending RecursiveTask this class can be forked/joined in
     * parallel by the fork-join pool.
     */
    class URLCrawlerTask
          extends RecursiveTask<Integer> {
        /**
         * The URI that's being crawled at this point.
         */
        final String mPageUri;

        /**
         * The current depth of the recursive processing.
         */
        final int mDepth;

        /**
         * Constructor initializes the fields.
         */
        URLCrawlerTask(String pageUri, int depth) {
            mPageUri = pageUri;
            mDepth = depth;
        }

        /**
         * Perform the web crawl at this url.
         *
         * @return The number of images downloaded/stored/transformed
         *         at this url
         */
        @Override
        protected Integer compute() {
            log(">> Depth: " + mDepth + " [" + mPageUri + "]");

            // Return 0 if we've reached the depth limit of the web
            // crawling.
            if (mDepth > mMaxDepth) {
                log("Exceeded max depth of " + mMaxDepth);
                return 0;
            }

            // Atomically check to see if we've already visited this
            // Uri and add the new uri to the hash set so we don't try
            // to revisit it again unnecessarily.
            else if (!mUniqueUris.putIfAbsent(mPageUri)) {
                log("Already processed " + mPageUri);
                // Return 0 if we've already examined this uri.
                return 0;
            }

            else {
                // Use the Java fork-join framework to (1) download
                // and process images on this page and (2) crawl other
                // hyperlinks accessible via this page.
                // TODO -- you fill in here.
            }
        }
    }
        
    /**
     * Download, process, and store an image.  By extending
     * RecursiveTask this class can be forked/joined in parallel by
     * the fork-join pool.
     */
    class ProcessImageTask
        extends RecursiveTask<Integer> {
        /**
         * Image to process.
         */
        final String mImageUri;

        /**
         * Constructor initializes the fields.
         * @param imageUri The URL to process
         */
        ProcessImageTask(String imageUri) {
            mImageUri = imageUri;
        }

        /**
         * Download, transform, and store an image.
         *
         * @return A count of the number of images processed
         */
        @Override
        protected Integer compute() {
            try {
                // Use the Java fork-join framework to get the Image
                // for this URL via the ProcessTransformTask.
                // TODO -- you fill in here.
            } catch (Exception e) {
                // Wrap and rethrow.
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Concurrently process transform operations.  By extending
     * RecursiveTask this class can be forked/joined in parallel by
     * the fork-join pool.
     */
    class ProcessTransformTask
          extends RecursiveTask<Image> {
        /**
         * Image to process.
         */
        final Image mImage;

        /**
         * Transform to apply.
         */
        final Transform mTransform;

        /**
         * Constructor initializes the fields.
         * @param image An image that's been downloaded and stored
         * @param transform The transform type.
         */
        ProcessTransformTask(Image image, Transform transform) {
            mImage = image;
            mTransform = transform;
        }

        /**
         * Download, transform, and store an image.
         *
         * @return A count of the number of images processed
         */
        @Override
        protected Image compute() {
            // Attempt to create a new cache item for this transform
            // and only apply the transform if a new cache item was
            // actually created (i.e., was not already in the cache).
            // TODO -- you fill in here.
        }
    }
}

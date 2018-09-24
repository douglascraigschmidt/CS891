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
        // Return the result of performing the computations in the
        // fork-join task.
        return ForkJoinPool
            // Use the common fork-join pool.
            .commonPool()
            // Invoke a URLCrawlerTask, which is the starting point of
            // the recursive traversal.
            .invoke(new URLCrawlerTask(pageUri,
                                       depth));
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

            // Use the fork-join framework to (1) download and process
            // images on this page and (2) crawl other hyperlinks
            // accessible via this page.
            else {
                try {
                    // Get the HTML page associated with mPageUri.
                    Crawler.Page page =
                        mWebPageCrawler.getPage(mPageUri);

                    // Create an (initially empty) list of
                    // ForkJoinTasks that will process all the images
                    // and hyperlinks on this page.
                    // TODO -- you fill in here.

                    // Iterate through all images and hyperlinks on
                    // this page.
                    for (WebPageElement pageElement : page.getPageElements(IMAGE, PAGE))
                        if (pageElement.mType == IMAGE) {
                            // If page element is an image create the
                            // appropriate task object that processes
                            // the image, fork it, and add it to the
                            // list of forked tasks.
                            // TODO -- you fill in here.

                        } else {
                            // If page element is a page create the
                            // appropriate task object that crawls
                            // hyperlinks recursively, fork it, and
                            // add it to the list of forked tasks.
                            // TODO -- you fill in here.

                        }

                    // Count the number of images on the page.
                    int imagesOnPage = 0;

                    // Join all the tasks in the list of forked tasks.
                    // TODO -- you fill in here.

                    // Return the result.
                    return imagesOnPage;
                } catch (Exception e) {
                    System.err.println("Exception For '"
                                       + mPageUri
                                       + "': "
                                       + e.getMessage());
                    return 0;
                }
            }
        }
    }
        
    /**
     * A RecursiveTask that downloads, processes, and stores an image.
     * By extending RecursiveTask this class can be forked/joined in
     * parallel by the fork-join pool.
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
                // Create the results array.
                int transformedImages = 0;

                // Get the Image for this URL either (1) returning the
                // image from a local cache if it's been downloaded
                // already or (2) downloading the image via its URL
                // and storing it in the local cache.
                Image rawImage = getOrDownloadImage(new URL(mImageUri));

                // Ensure we actually got a new image successfully.
                if (rawImage != null) {

                    // Create an (initially empty) list of
                    // ForkJoinTask that will transform Images in
                    // parallel.
                    // TODO -- you fill in here.

                    // Iterate through all the transforms and create
                    // the appropriate task object that transforms the
                    // image, fork it, and add it to the list of
                    // forked tasks.
                    // TODO -- you fill in here.

                    // Now join all the forked transform tasks and count the
                    // number of images that are returned from each task.
                    // TODO -- you fill in here.
                }

                // Return the number of images processed.
                return transformedImages;

            } catch (Exception e) {
                // Wrap and rethrow.
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * A RecursiveTask that performs transform operations.  By
     * extending RecursiveTask this class can be forked/joined in
     * parallel by the fork-join pool.
     */
    class PerformTransformTask
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
        PerformTransformTask(Image image, Transform transform) {
            mImage = image;
            mTransform = transform;
        }

        /**
         * Download, transform, and store an image.
         *
         * @return A processed image
         */
        @Override
        protected Image compute() {
            // Attempt to create a new cache item for this transform
            // and only apply the transform if a new cache item was
            // actually created (i.e., it wasn't already in cached).

            // TODO -- you fill in here.
            return null;
        }
    }
}

package edu.vanderbilt.imagecrawler.crawlers;

import java.net.URL;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

import edu.vanderbilt.imagecrawler.transforms.Transform;
import edu.vanderbilt.imagecrawler.utils.Array;
import edu.vanderbilt.imagecrawler.utils.Assignment;
import edu.vanderbilt.imagecrawler.utils.Crawler;
import edu.vanderbilt.imagecrawler.utils.Image;
import edu.vanderbilt.imagecrawler.utils.UnsynchronizedArray;
import edu.vanderbilt.imagecrawler.utils.WebPageElement;

import static edu.vanderbilt.imagecrawler.utils.ArrayCollector.toArray;
import static edu.vanderbilt.imagecrawler.utils.Crawler.Type.IMAGE;
import static edu.vanderbilt.imagecrawler.utils.Crawler.Type.PAGE;

/**
 * This ImageCrawler implementation uses the Java fork-join pool
 * framework to perform an "image crawl" starting from a root Uri.
 * Images from HTML page reachable from the root Uri are downloaded
 * from a remote web server or the local file system and the results
 * are stored in files that can be displayed to the user.
 *
 * Graduate students should use Java sequential streams to implement
 * the fork-join logic, whereas streams are optional for undergraduate
 * students (who can simply use Java 7 for-each loops).
 */
public class ForkJoinCrawler1 // Loaded via reflection
        extends ImageCrawler {
    /**
     * A factory method that creates an unsynchronized array of
     * ForkJoinTask objects to facilitate unit test mocking.
     *
     * @return a new empty array containing ForkJoinTask objects
     */
    protected <T> Array<ForkJoinTask<T>> makeForkJoinArray() {
        // TODO -- undergraduate students must replace 'null' with the
        // appropriate call, whereas graduate students can ignore this
        // method in their implementation.
        return null;
    }

    /**
     * Perform the web crawl.
     *
     * @param pageUri The URL that we're crawling at this point
     * @param depth   The current depth of the recursive processing
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
            .invoke(makeURLCrawlerTask(pageUri,
                                       depth));
    }

    /**
     * Helper method to create a new PerformTransformTask.
     *
     * @param image     Image to transform.
     * @param transform Transform to perform
     * @return A new ForkJoinTask instance.
     */
    protected ForkJoinTask<Image> makePerformTransformTask(Image image,
                                                           Transform transform) {
        // TODO -- you fill in here (replace null with the implementation).
        return null;
    }

    /**
     * Helper method to create a new ProcessImageTask.
     *
     * @param url The image url to process.
     * @return A new ProcessImageTask that will transform the image.
     */
    protected ProcessImageTask makeProcessImageTask(String url) {
        // TODO -- you fill in here (replace null with the implementation).
        return null;
    }

    /**
     * Helper method to create a new URLCrawlerTask.
     *
     * @param pageUri url page string
     * @param depth   the maximum crawl depth
     * @return A new URLCrawlTask instance
     */
    protected URLCrawlerTask makeURLCrawlerTask(String pageUri, int depth) {
        // TODO -- you fill in here (replace null with the implementation).
        return null;
    }

    /**
     * Perform a web crawl from a particular starting point.  By
     * extending RecursiveTask this class can be forked/joined in
     * parallel by the fork-join pool.
     */
    public class URLCrawlerTask
        extends RecursiveTask<Integer> {
        /**
         * The URI that's being crawled at this point.
         */
        public String mPageUri;

        /**
         * The current depth of the recursive processing.
         */
        public int mDepth;

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
         * at this url
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
                // Get the HTML page associated with mPageUri.
                Crawler.Page page =
                    mWebPageCrawler.getPage(mPageUri);

                if (Assignment.isGraduateTodo()) {
                    // Use a Java sequential stream to create the
                    // forks array with the appropriate fork()'d
                    // ForkJoinTask objects corresponding to each type
                    // of the IMAGE or PAGE element on the page.

                    // TODO -- you fill in here, replacing 'null' with
                    // the appropriate Java sequential stream.
                    Array<ForkJoinTask<Integer>> forks = null;

                    // Use a Java sequential stream to join/sum all
                    // the forked tasks and return a count of the # of
                    // images on the page.

                    // TODO -- you fill in here, replacing '0' with
                    // the appropriate Java sequential stream.
                    return 0;

                } else if (Assignment.isUndergraduateTodo()) {
                    // Use the makeForkJoinArray() factory method to
                    // create an empty array of ForkJoinTasks used to
                    // process all images and hyperlinks on this page.

                    // TODO -- you fill in here (replace null with the
                    // implementation).
                    Array<ForkJoinTask<Integer>> forks = null;

                    // Iterate through all images and hyperlinks on
                    // this page.
                    for (WebPageElement pageElement : page.getPageElements(IMAGE, PAGE))
                        if (pageElement.getType() == IMAGE) {
                            // If page element is an image create the
                            // appropriate task object that processes
                            // the image, fork it, and add it to the
                            // array of forked tasks.

                            // TODO -- you fill in here.
                        } else {
                            // If page element is a page create the
                            // appropriate task object that crawls
                            // hyperlinks recursively, fork it, and
                            // add it to the array of forked tasks.

                            // TODO -- you fill in here.
                        }

                    // Count the number of images on the page.
                    int imagesOnPage = 0;

                    // A barrier that joins all the tasks in the array
                    // of forked tasks.

                    // TODO -- you fill in here.

                    // Return the result.
                    return imagesOnPage;
                }
            }

            return 0;
        }
    }

    /**
     * Download, process, and store an image.  By extending
     * RecursiveTask this class can be forked/joined in parallel by
     * the fork-join pool.
     */
    public class ProcessImageTask
        extends RecursiveTask<Integer> {
        /**
         * Image to process.
         */
        final String mImageUri;

        /**
         * Constructor initializes the fields.
         *
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
            // Keep track of the number of transformed images.
            int transformedImages = 0;

            // Get the Image for this URL either (1) returning the
            // image from a local cache if it's been downloaded
            // already or (2) downloading the image via its URL and
            // storing it in the local cache.
            Image rawImage;

            try {
                rawImage = getOrDownloadImage(new URL(mImageUri));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // Ensure we actually got a new image successfully.
            if (rawImage != null) {
                if (Assignment.isGraduateTodo()) {
                    // Use a Java sequential stream to create the
                    // forks array with the appropriate fork()'d
                    // ForkJoinTask objects corresponding to
                    // PerformTransformTask.

                    // TODO -- you fill in here, replacing 'null' with
                    // the appropriate Java sequential stream.
                    Array<ForkJoinTask<Image>> forks = null;

                    // Use a Java sequential stream to join all the
                    // forked tasks and count the number of non-null
                    // images returned from each task.

                    // TODO -- you fill in here, replacing '0' with
                    // the appropriate Java sequential stream.
                    transformedImages += 0;
                } else if (Assignment.isUndergraduateTodo()) {
                    // Use the makeForkJoinArray() factory method to
                    // create an array of ForkJoinTasks that will
                    // transform Images in parallel.

                    // TODO -- you fill in here (replace null with the
                    // implementation).
                    Array<ForkJoinTask<Image>> forks = null;

                    // Iterate through all the transforms and create
                    // the appropriate task object that transforms the
                    // image, fork it, and add it to the array of
                    // forked tasks.

                    // TODO -- you fill in here.

                    // Now join all the forked transform tasks and
                    // count the number of images that are returned
                    // from each task.

                    // TODO -- you fill in here.
                }
            }

            // Return the number of images processed.
            return transformedImages;
        }
    }

    /**
     * Perform transform operations.  By extending RecursiveTask this
     * class can be forked/joined in parallel by the fork-join pool.
     */
    public class PerformTransformTask
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
         *
         * @param image     An image that's been downloaded and stored
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

            // TODO -- you fill in here (replace null with the
            // implementation).
            return null;
        }
    }
}

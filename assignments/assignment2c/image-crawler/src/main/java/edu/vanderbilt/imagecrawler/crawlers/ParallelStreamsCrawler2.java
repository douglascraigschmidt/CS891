package edu.vanderbilt.imagecrawler.crawlers;

import java.net.URL;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import edu.vanderbilt.imagecrawler.utils.Array;
import edu.vanderbilt.imagecrawler.utils.Crawler;
import edu.vanderbilt.imagecrawler.utils.Image;

import static edu.vanderbilt.imagecrawler.utils.Crawler.Type.PAGE;

/**
 * This ImageCrawler implementation uses Java parallel streams to
 * perform an "image crawl" starting from a root Uri.  Images from
 * HTML page reachable from the root Uri are downloaded from a remote
 * web server or the local file system and the results are stored in
 * files that can be displayed to the user.
 * <p>
 * This class is a variant of the ParallelStreamsCrawler1.java
 * implementation that uses parallel streams in a more sophisticated
 * manner.
 */
public class ParallelStreamsCrawler2 // Loaded via reflection
        extends ImageCrawler {
    /**
     * Perform the web crawl.
     *
     * @param pageUri The URI that's being crawled at this point
     * @param depth   The current depth of the recursive processing
     * @return A future to the number of images processed
     */
    @Override
    protected int performCrawl(String pageUri, int depth) {
        // Throw an exception if the the stop crawl flag has been set.
        throwExceptionIfCancelled();

        log(">> Depth: " + depth + " [" + pageUri + "]");

        // Return 0 if we've reached the depth limit of the web
        // crawling.
        if (depth > mMaxDepth) {
            log("Exceeded max depth of " + mMaxDepth);
            return 0;
        }

        // Atomically check to see if we've already visited this Uri
        // and add the new uri to the hash set so we don't try to
        // revisit it again unnecessarily.
        else if (!mUniqueUris.putIfAbsent(pageUri)) {
            log("SKIPPING page %s (already processed)", pageUri);
            // Return 0 if we've already examined this uri.
            return 0;
        }

        return crawlPage(pageUri, depth);
    }

    /**
     * Use a Java parallel streams features to (1) download and
     * process images on this page via processImage(), (2) recursively
     * crawl other hyperlinks accessible from this page via
     * crawlPage(), and (3) return the number all successfully
     * processed images.
     *
     * @param pageUri The page uri to crawl.
     * @param depth   The current depth of the recursive processing
     * @return The number of processed images.
     */
    protected int crawlPage(String pageUri, int depth) {
        log("[" + Thread.currentThread().getName()
            + "] Crawling " + pageUri + " (depth " + depth + ")");

        // Get the HTML page associated with pageUri (properly handle
        // a null return value).
        // TODO -- you fill in here.

        // Use a Java parallel stream to return the number of
        // processed images by the following steps:
        //
        // 1. Create a stream of 2 function lambdas: one to process
        //    images on a page and the other to process hyperlinks
        //    on each page.
        // 2. Run the stream elements (lambdas) in parallel.
        // 3. Invoke each function lambda mapping the result to an Integer.
        // 4. Return the total number of processed images.
        // 
        // TODO -- you fill in here (replace '0' with your implementation).
        return 0;
    }

    /**
     * Factory method that constructs a stream containing 2 function
     * lambdas: one for processing images on a page and the other for
     * processing links on a page.
     *
     * @return Return a stream of 2 function lambdas: one that processes
     * images and the other that processes hyperlinks.
     */
    protected Stream<Function<Crawler.Page, Integer>> streamOfTasks(int depth) {
        // Return a stream of 2 function lambdas: one that processes
        // images and the other that processes hyperlinks.
        // TODO -- you fill in here (replace null with your implementation).
        return null;
    }

    /**
     * Factory method that constructs a function lambda to process
     * images on the input page.
     *
     * @return A function lambda that receives a page and gets and processes
     * all images on that page and returns the count of processed images.
     */
    protected Function<Crawler.Page, Integer> makeImagesOnPageFunction() {
        // A function lambda that receives a page and gets and
        // processes all images on a page and returns the count of
        // processed images.
        // TODO -- you fill in here (replacing null with your implementation).
        return null;
    }

    /**
     * Factory method that constructs a function lambda to crawl
     * hyperlinks on the input page.
     *
     * @param depth The depth of the hyper link pages.
     * @return A function lambda that receives a page and calls helper method
     * to crawl hyperlinks on that page at the specified depth.
     */
    protected Function<Crawler.Page, Integer> makeHyperLinksOnPageFunction(int depth) {
        // A function lambda that receives a page and recursively
        // crawls hyperlinks that are on this page and counts the
        // number of processed images from each hyperlink. You will
        // pass an adjust depth value to reflect the depth of the
        // children hyperlinks on this page.
        // TODO -- you fill in here (replacing null with your implementation).
        return null;
    }

    /**
     * Recursively crawl hyperlinks that are on the {@code page}.
     *
     * @param page  The page to crawl.
     * @param depth The depth of the passed page.
     * @return The number of successfully processed images.
     */
    protected Integer crawlHyperLinksOnPage(Crawler.Page page, int depth) {
        log("Performing parallel crawl of hyperlinks on page ...");

        // Use a Java stream that invokes performCrawl() to crawl
        // through hyperlinks on this page and returns the total
        // number of processed images.

        // Use a Java parallel stream to return the number of
        // processed images by the following steps:
        //
        // 1. Get all hyperlinks on this page as an array of Strings.
        // 2. Convert the array to parallel streams.
        // 3. Call helper method to crawl each hyperlink
        //    and map the result to an Integer.
        // 4. Return the total number of processed images.
        //
        // TODO -- you fill in here (replace '0' with your implementation).
        return 0;
    }

    /**
     * Download, process, and store each image in the {@code urls} array.
     *
     * @param urls An array of URLs to images to process
     * @return A count of the number of images that were processed
     */
    protected Integer processImages(Array<URL> urls) {
        log("Performing parallel transforms of %d urls ...", urls.size());

        // Use a Java parallel stream to do the following steps:
        //
        // 1. Convert the urls array into a parallel stream.
        // 2. Map each URL to a downloaded raw image.
        // 3. Ignore any null images.
        // 4. Convert each downloaded raw image to a stream of
        //    transformed images.
        // 5. Ignore any null transformed images.
        // 6. Return the number of successfully transformed images.
        //
        // TODO -- you fill in here (replace '0' with your implementation).
        return 0;
    }

    /**
     * Download, process, and store an {@code image} by applying all
     * transforms to it.
     *
     * @param image An image that's been downloaded and stored
     * @return A stream of transformed images
     */
    protected Stream<Image> transformImage(Image image) {
        log("Performing %d parallel transforms of image %s ...",
            mTransforms.size(),
            image.getSourceUrl());

        // Use a Java parallel stream to perform the following steps:
        //
        // 1. Convert the transforms array into a parallel stream.
        // 2. Attempt to create a new cache item for each transform
        //    image filtering out any transform image that has already
        //    been locally cached.
        // 3. Apply the transform to original image producing to produce
        //    a transformed image.
        // 4. Return a stream of all non-null transformed images.
        //
        // TODO -- you fill in here (replace null with your implementation).
        return null;
    }
}

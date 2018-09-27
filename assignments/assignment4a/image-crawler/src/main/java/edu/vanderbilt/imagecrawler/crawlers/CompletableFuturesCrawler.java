package edu.vanderbilt.imagecrawler.crawlers;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import edu.vanderbilt.imagecrawler.crawlers.framework.ImageCrawler;
import edu.vanderbilt.imagecrawler.transforms.Transform;
import edu.vanderbilt.imagecrawler.utils.Array;
import edu.vanderbilt.imagecrawler.utils.Crawler;
import edu.vanderbilt.imagecrawler.utils.FuturesCollector;

import static edu.vanderbilt.imagecrawler.utils.Crawler.Type.PAGE;

/**
 * This implementation strategy uses Java 8 functional programming
 * features, the Java 8 completable futures framework, and the Java 8
 * sequential streams framework to perform an "image crawl" starting
 * from a root Uri.  Images from HTML page reachable from the root Uri
 * are downloaded from a remote web server or the local file system
 * and the results are stored in files that can be displayed to the
 * user.
 */
public class CompletableFuturesCrawler
       extends ImageCrawler {
    /**
     * Perform the web crawl.
     *
     * @param pageUri The URL that we're crawling at this point
     * @param depth The current depth of the recursive processing
     * @return The number of images downloaded/stored.
     */
    protected int performCrawl(String pageUri,
                               int depth) {
        log(">> Depth: " + depth + " [" + pageUri + "]" + " (" + Thread.currentThread().getId() + ")");

        // Return 0 if we've reached the depth limit of the web
        // crawling.
        if (depth > mMaxDepth) {
            log("Exceeded max depth of " + mMaxDepth);
            return 0;
        }

        // Atomically check to see if we've already visited this URL
        // and add the new url to the hashset so we don't try to
        // revisit it again unnecessarily.
        else if (!mUniqueUris.putIfAbsent(pageUri)) {
            log("Already processed " + pageUri);

            // Return 0 if we've already examined this url.
            return 0;
        }

        else {
            // Perform the crawl asynchronously, wait until all the
            // processing is done, return the result.
            // TODO -- you fill in here (replace null with the right code).
            return null;
        }
    }

    /**
     * Perform the web crawl by using completable futures to
     * asynchronously (1) download/store images on this page and (2)
     * crawl other hyperlinks accessible via this page.
     *
     * @param pageUri The URL that we're crawling at this point
     * @param depth The current depth of the recursive processing
     * @return A future to the the number of images downloaded/stored
     */
    private CompletableFuture<Integer> performCrawlAsync(String pageUri,
                                                         int depth) {
        try {
            // Get a future to the HTML page associated with pageUri.
            CompletableFuture<Crawler.Page> pageFuture =
                getPageAsync(pageUri);

            // The following two asynchronous method calls run
            // concurrently in the fork-join thread pool.

            // After contents of the HTML page are obtained get a
            // future to the # of images processed on this page.
            CompletableFuture<Integer> imagesOnPageFuture =
                getImagesOnPageAsync(pageFuture);

            // After contents of the HTML page are obtained get a
            // future to the number of images processed on pages
            // linked from this page.
            CompletableFuture<Integer> imagesOnPageLinksFuture =
                crawlHyperLinksOnPageAsync(pageFuture,
                                           // Increment depth.
                                           depth + 1);

            // Return a completable future to the combined results of
            // the two futures params whenever they complete.
            return combineResultsAsync(imagesOnPageFuture,
                                       imagesOnPageLinksFuture);
        } catch (Exception e) {
            log("For '" + pageUri + "': " + e.getMessage());
            // Return completed future with value 0 if an exception
            // happens.  
            // TODO -- you fill in here (replace null with the right code).
        }
    }

    /**
     * Asynchronously get the contents of the HTML page at {@code
     * pageUri}.
     *
     * @param pageUri The URL that we're crawling at this point
     * @return A completable future to the HTML page.
     */
    private CompletableFuture<Crawler.Page> getPageAsync(String pageUri) {
        // Load the HTML page asynchronously and return a completable
        // future to that page.
        // TODO -- you fill in here (replace null with the right code).
    }

    /**
     * Asynchronously download/store all the images in the page
     * associated with {@code pageFuture}.
     *
     * @param pageFuture A completable future to the page that's being
     *                   downloaded
     * @return A completable future to an integer containing the
     *         number of images downloaded/stored on this page
     */
    private CompletableFuture<Integer> getImagesOnPageAsync
        (CompletableFuture<Crawler.Page> pageFuture) {
        // Return a completable future to an integer containing the
        // number of images processed on this page.  This method
        // should call getImagesOnPage() and processImages()
        // asynchronously via a fluent chain of completion stage
        // methods.
        // TODO -- you fill in here (replace null with the right code).
    }

    /**
     * Asynchronously obtain a future to the number of images
     * processed on pages linked from this page.
     *
     * @param pageFuture A completable future to the page that's being
     *                   downloaded
     * @param depth The current depth of the recursive processing
     * @return A future to an integer containing # of images
     *         downloaded/stored on pages linked from this page
     */
    private CompletableFuture<Integer> 
        crawlHyperLinksOnPageAsync(CompletableFuture<Crawler.Page> pageFuture,
                                   int depth) {
        // Return a future to an integer containing the # of images
        // processed on pages linked from this page.  This method
        // should call crawlHyperLinksOnPage() asynchronously.
        // TODO -- you fill in here (replace null with the right code).
    }

    /**
     * Combines the results of two completable future parameters.
     *
     * @param imagesOnPageFuture A count of the number of images on a page
     * @param imagesOnPageLinksFuture an array of the counts of the
     *                                number of images on all pages
     *                                linked from a page
     * @return A completable future to the combined results of the two
     *         futures params after they complete
     */
    private CompletableFuture<Integer> combineResultsAsync
        (CompletableFuture<Integer> imagesOnPageFuture,
         CompletableFuture<Integer> imagesOnPageLinksFuture) {
        // Returns a completable future to the combined results of the
        // two futures params after they complete.
        // TODO -- you fill in here (replace null with the right code).
    }

    /**
     * Recursively crawl through hyperlinks that are in a {@code
     * page}.
     *
     * @return An integers that counts how many images were
     * downloaded/stored for each hyperlink on the page  
     */
    private Integer crawlHyperLinksOnPage(Crawler.Page page,
                                          int depth) {
        // Return a completable future to an integer that counts the #
        // of nested hyperlinks accessible from the page.  This method
        // should contain a stream that uses map(), performCrawl(),
        // and reduce(), among other methods.
        // TODO -- you fill in here (replace null with the right code).
    }

    /**
     * Use Java 8 CompletableFutures and a sequential stream to
     * download and store images asynchronously.
     *
     * @return A completable future to an integer that counts how many
     * images were downloaded/stored for each of the urls {@code urls}
     * on the page 
     */
    private CompletableFuture<Integer> processImages(Array<URL> urls) {
        // Create a sequential stream that returns a completable
        // future containing the # of images that were
        // downloaded/stored.
        return urls
            // Convert the URLs in the input array into a sequential
            // stream.
            // TODO -- you fill in here.

            // Map each URL to a completable future to an image (i.e.,
            // asynchronously download/store each image via its URL).
            // TODO -- you fill in here.

            // Terminate the stream and collect results into a single
            // completable future to an array of images.
            // TODO -- you fill in here.

            // When all futures complete return a count of the # of
            // images that were downloaded/stored.
            // TODO -- you fill in here.
    }

    /**
     * @return The supported transforms for this crawler strategy.
     */
    public static List<Transform.Type> getSupportedTransforms() {
        // Current assignment does not support transforms.
        return new ArrayList<>();
    }
}

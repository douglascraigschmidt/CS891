package edu.vanderbilt.imagecrawler.crawlers;

import java.net.URL;
import java.util.stream.Stream;

import edu.vanderbilt.imagecrawler.crawlers.framework.ImageCrawlerBase;
import edu.vanderbilt.imagecrawler.utils.Array;
import edu.vanderbilt.imagecrawler.utils.ArrayCollector;
import edu.vanderbilt.imagecrawler.utils.Crawler;
import edu.vanderbilt.imagecrawler.utils.Image;

import static edu.vanderbilt.imagecrawler.utils.Crawler.Type.PAGE;

/**
 * This class uses Java 8 features to perform an "image crawl"
 * starting from a root Uri.  Images from HTML page reachable from the
 * root Uri are either downloaded from a remote web server or read
 * from the local file system, image processing transforms are then
 * applied to each image, and the results are stored in files that can
 * be displayed to the user.
 * <p>
 * This implementation strategy customizes ImageCrawlerBase and uses
 * the Java 8 streams framework to download, transform, and store images
 * in parallel.
 */
public class ParallelStreamsCrawler
       extends ImageCrawlerBase {
    /**
     * Perform the web crawl.
     *
     * @param pageUri The URI that we're crawling at this point
     * @param depth   The current depth of the recursive processing
     * @return A future to the number of images processed
     */
    @Override
    protected int performCrawl(String pageUri, int depth) {
        printDiagnostics(TAG + ":>> Depth: "
                         + depth
                         + " ["
                         + pageUri
                         + "]"
                         + " ("
                         + Thread.currentThread().getId()
                         + ")");

        // Return 0 if we've reached the depth limit of the web
        // crawling.
        if (depth > mMaxDepth) {
            printDiagnostics(TAG + ": Exceeded max depth of "
                             + mMaxDepth);
            return 0;
        }

        // Atomically check to see if we've already visited this Uri
        // and add the new uri to the hash set so we don't try to
        // revisit it again unnecessarily.
        else if (!mUniqueUris.putIfAbsent(pageUri)) {
            printDiagnostics(TAG +
                             ": Already processed "
                             + pageUri);
            // Return 0 if we've already examined this uri.
            return 0;
        }

        // Use Java 8 streams to (1) download and process images on
        // this page and (2) crawl other hyperlinks accessible via
        // this page.
        else {
            try {
                // Get the HTML page associated with pageUri.
                Crawler.Page page = mWebPageCrawler.getPage(pageUri);

                // Get the # of images processed on this page.
                int imagesOnPage = processImages(getImagesOnPage(page));

                // Recursively crawl through hyperlinks that are in
                // this page and get an array of counts of the number
                // of images in each hyperlink.
                Array<Integer> imagesOnPageLinks =
                    crawlHyperLinksOnPage(page, depth + 1);

                // Use a Java 8 parallel stream to return a reduced
                // result containing a count of all images that were
                // downloaded and processed.
                // TODO -- you fill in here.
            } catch (Exception e) {
                System.err.println("Exception For '"
                                   + pageUri
                                   + "': "
                                   + e.getMessage());
                return 0;
            }
        }
    }

    /**
     * Recursively crawl through hyperlinks that are in a {@code page}.
     *
     * @return An array of integers, each of which counts how many
     *         images were processed for each hyperlink on the page.
     */
    private Array<Integer> crawlHyperLinksOnPage(Crawler.Page page,
                                                 int depth) {
        // Use a Java 8 parallel stream that recursively invokes
        // performCrawl() to crawl through hyperlinks on this page and
        // returns an array of integers, each of which counts how many
        // images were processed for each hyperlink on the page.

        // TODO -- you fill in here.  All students must use their
        // implementation of the ArrayCollector class here.
    }

    /**
     * Download, process, and store each image in the {@code urls} array.
     *
     * @param urls An array of URLs to images to process
     * @return A count of the number of images that were processed
     */
    private int processImages(Array<URL> urls) {
        // Use a Java 8 parallel stream to do the following:
        // 1. Convert the urls array into a parallel stream.
        // 2. Convert each URL to a raw image (via map()).
        // 3. Convert each raw image to a stream of transformed images
        //    (via flatMap())  
        // 4. Return the number of transformed images

        // TODO -- you fill in here.
    }

    /**
     * Download, process, and store an {@code image}
     * by applying the transform to it.
     *
     * @param image An image that's been downloaded and stored
     * @return A stream of transformed images
     */
    private Stream<Image> transformImage(Image image) {
        // Use a Java 8 parallel stream to do the following:
        // 1. Convert the urls array into a parallel stream.
        // 2. Ignore any images that's already been transformed and
        //    cached locally (via filter()).
        // 3. Transform/store any non-cached image (via map()).
        // 4. Return a stream of the transformed/stored images.

        // TODO -- you fill in here.
    }
}

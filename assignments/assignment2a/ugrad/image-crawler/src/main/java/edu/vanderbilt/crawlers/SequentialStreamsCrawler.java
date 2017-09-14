package edu.vanderbilt.crawlers;

import java.net.URL;
import java.util.List;
import java.util.stream.Stream;

import edu.vanderbilt.crawlers.framework.ImageCrawlerBase;
import edu.vanderbilt.filters.Filter;
import edu.vanderbilt.platform.Device;
import edu.vanderbilt.utils.Array;
import edu.vanderbilt.utils.ArrayCollector;
import edu.vanderbilt.utils.Crawler;
import edu.vanderbilt.utils.Image;
import edu.vanderbilt.utils.StreamsUtils;

import static edu.vanderbilt.utils.Crawler.Type.CONTAINER;

/**
 * This class uses Java 8 features to perform an "image crawl"
 * starting from a root Uri.  Images from HTML page reachable from the
 * root Uri are either downloaded from a remote web server or read
 * from the local file system, image processing filters are then
 * applied to each image, and the results are stored in files that can
 * be displayed to the user.
 *
 * This implementation strategy customizes ImageCrawlerBase and uses
 * the Java 8 streams framework to download, process, and store images
 * sequentially.
 */
public class SequentialStreamsCrawler 
       extends ImageCrawlerBase {
    /**
     * Default constructor is only used by the CrawlerFactory.
     */
    public SequentialStreamsCrawler() {
    }

    /**
     * Constructor initializes the superclass.
     */
    public SequentialStreamsCrawler(List<Filter> filters,
                                    String rootUri) {
        super(filters, rootUri);
    }

    /**
     * A hook method (also a template method) that does bookkeeping
     * operations and dispatches the subclass's performCrawl() hook
     * method to start implementation strategy processing.
     */
    @Override
    public void run() {
        // Start timing the test run.
        startTiming();

        // Perform the image crawling starting at the root Uri, given
        // an initial depth count of 1.
        int totalImages = performCrawl(mRootUri, 1);

        // Stop timing the test run.
        stopTiming();

        printDiagnostics(TAG
                           + ": downloaded and processed "
                           + totalImages
                           + " total image(s)");
    }

    /**
     * Perform the web crawl.
     *
     * @param pageUri The URI that we're crawling at this point
     * @param depth The current depth of the recursive processing
     * @return A future to the number of images processed
     */
    private int performCrawl(String pageUri,
                             int depth) {
        printDiagnostics(TAG
                           + ":>> Depth: " 
                           + depth 
                           + " [" 
                           + pageUri
                           + "]" 
                           + " (" 
                           + Thread.currentThread().getId() 
                           + ")");

        // Return 0 if we've reached the depth limit of the web
        // crawling.
        if (depth > Device.options().getMaxDepth()) {
            printDiagnostics(TAG 
                               + ": Exceeded max depth of "
                               + Device.options().getMaxDepth());
            return 0;
        }

        // Atomically check to see if we've already visited this Uri
        // and add the new uri to the hashset so we don't try to
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
                Crawler.Container page =
                        Device.parser().getContainer(pageUri);

                // Get the # of images processed on this page.
               int imagesOnPage = 
                    processImages(getImagesOnPage(page));

               // Recursively crawl through hyperlinks that are in
               // this page and get an array of counts of the number
               // of images in each hyperlink.
               Array<Integer> imagesOnPageLinks =
                   crawlHyperLinksOnPage(page,
                                         depth + 1);

               // Use a Java 8 sequential stream to return a
               // combined result.
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
     * Recursively crawl through hyperlinks that are in a @a page.
     *
     * @return An array of integers, each of which counts how many
     * images were processed for each hyperlink on the page.
     */
    private Array<Integer> crawlHyperLinksOnPage(Crawler.Container page,
                                                 int depth) {
        // Use a Java 8 stream that recursively invokes performCrawl()
        // to crawl through hyperlinks on this page and returns an
        // array of integers, each of which counts how many images
        // were processed for each hyperlink on the page.

        // TODO -- you fill in here.  Grad students must use their
        // implementation of the ArrayCollector class here, whereas
        // ugrads are free to implement this as they see fit, i.e.,
        // they can implement the ArrayCollector or some other
        // solution (e.g., a forEach() loop).
    }

    /**
     * Download, process, and store each image in the @a urls array.
     *
     * @param urls An array of URLs to images to process
     * @return A count of the number of images processed
     */
    private int processImages(Array<URL> urls) {
        // Use a Java 8 sequential stream to do the following:
        // 1. Ignore URLs that are already cached locally (via filter()).
        // 2. Download images (via map())
        // 3. Filter and store images (via flatMap())
        // 4. Return the number of images processed.

        // TODO -- you fill in here.  Grad students must use their
        // implementation of the ArrayCollector class here, whereas
        // ugrads are free to implement this as they see fit, i.e.,
        // they can implement the ArrayCollector or some other
        // solution (e.g., a forEach() loop).

        printDiagnostics(TAG
                           + ": processing of "
                           + filteredImages.size()
                           + " image(s) from "
                           + urls.size() 
                           + " urls is complete");

        // Return the number of images processed.
        return filteredImages.size();
    }

    /**
     * Apply each image filter to the @a image.
     *
     * @param image A downloaded image
     * @return A stream of processed and stored images.
     */
    private Stream<Image> applyFilters(Image image) {
        // Return a Java 8 sequential stream that processes and store
        // images.  You'll need to create an OutputFilterDecorator for
        // each image and run it to process each image and store it in
        // an output file.

        // TODO -- you fill in here.
    }

    /**
     * @return Returns true if the @a url is in cache, else false.
     */
    private boolean urlCached(URL url) {
        // Use a Java 8 sequential stream to search the list of
        // filters (via filter()) to see which images already exist in
        // the cache.  Returns true if the url is in cache, else
        // false.

        // TODO -- you fill in here.
    }
}

package edu.vanderbilt.crawlers;

import java.net.URL;
import java.util.List;
import java.util.concurrent.CancellationException;

import edu.vanderbilt.crawlers.framework.ImageCrawlerBase;
import edu.vanderbilt.filters.Filter;
import edu.vanderbilt.platform.Device;
import edu.vanderbilt.utils.Array;
import edu.vanderbilt.utils.Crawler;
import edu.vanderbilt.utils.ExceptionUtils;
import edu.vanderbilt.utils.Image;

import static edu.vanderbilt.utils.Crawler.Type.CONTAINER;

/**
 * This uses Java 7 object-oriented features to perform an "image
 * crawl" starting from a root Uri.  Images from HTML page reachable
 * from the root Uri are downloaded from a remote web server or the
 * local file system, image processing are then filters to each image,
 * and the results are stored in files that can be displayed to the
 * user.
 *
 * This implementation is entirely sequential and uses no Java 8
 * features at all.  It therefore serves as a baseline to compare all
 * the other implementation strategies.
 *
 * See https://www.mkyong.com/java/jsoup-basic-web-crawler-example for
 * an overview of how to write a web crawler using jsoup.
 */
public class SequentialLoopsCrawler
		extends ImageCrawlerBase {
    /**
     * Default constructor is only used by the CrawlerFactory.
     */
    public SequentialLoopsCrawler() {
    }

    /**
     * Constructor initializes the superclass.
     */
    public SequentialLoopsCrawler(List<Filter> filters,
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
        try {
            // Start timing the test run.
            startTiming();

            // Perform the web crawling starting at the root Uri, given an
            // initial depth count of 1.
            int totalImages = performCrawl(mRootUri, 1);

            // Stop timing the test run.
            stopTiming();

            printDiagnostics(TAG
                             + ": downloaded and processed "
                             + totalImages
                             + " total image(s)");
        } catch (Exception e) {
            if (e.getCause() instanceof CancellationException) {
                System.out.println("Crawl was cancelled.");
            } else {
                System.out.println("Crawl was abnormally terminated: " 
                                   + e.getMessage());
                throw e;
            }
        }
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

        // Throw an exception if the the stop crawl flag has been set.
        Device.throwExceptionIfCancelled();

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

        // Use completable futures to asynchronously (1) download and
        // process images on this page and (2) crawl other hyperlinks
        // accessible via this page.
        else {
            try {
                // Get the HTML page associated with pageUri.
                Crawler.Container page =
                    Device.instance().parser().getContainer(pageUri);

                // Process all images on this page and save the count
                // images that were successfully processed.
                int imagesOnPage = processImages(getImagesOnPage(page));

                // Recursively crawl through hyperlinks that are in
                // this page and get an array of counts of the number
                // of images in each hyperlink.
                Array<Integer> imagesOnPageLinks =
                    crawlHyperLinksOnPage(page, depth + 1);

                // Return a combined result.
                for (Integer imagesOnPageLink : imagesOnPageLinks)
                    imagesOnPage += imagesOnPageLink;

                return imagesOnPage;
            } catch (Exception e) {
                // If cancelled just rethrow the exception.
                ExceptionUtils.rethrowIfCancelled(e);

                e.printStackTrace();
                System.err.println("Exception for '"
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
     * images were processed for each hyperlink on the page
     */
    private Array<Integer> crawlHyperLinksOnPage(Crawler.Container page,
												 int depth) {
        // Create the results array.
        Array<Integer> resultsArray = new Array<>();

        // Iterate through all hyperlinks on this page.
        for (String url: page.getObjectsAsStrings(CONTAINER)) {
            // Recursively visit all the hyperlinks on this page and
            // add the result to the array.
            resultsArray.add(performCrawl(url, depth));
        }

        // Return the results array.
        return resultsArray;
    }

    /**
     * Download, process, and store each image in the @a urls array.
     *
     * @param urls An array of URLs to images to process
     * @return A count of the number of images processed
     */
    private int processImages(Array<URL> urls) {
        // Create the results array.
        Array<Image> filteredImages =
            new Array<>();

        // Iterate through all the URLs and perform the designated
        // processing.
        for (URL url : urls) {
            // Ignore URLs that are already cached locally, i.e., only
            // download non-cached images.
            if (!urlCached(url)) {
                // Transform URL to an Image by downloading each image via
                // its URL.
                Image image = downloadImage(url);

                // Create multiple filtered versions of each image and
                // add them to the array.
                filteredImages.addAll(applyFilters(image));
            }
        }

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
    private Array<Image> applyFilters(Image image) {
        // Create the results array.
        Array<Image> filteredArray =
            new Array<>();

        // Create an OutputFilterDecorator for each image and run it
        // to filter each image and store it in an output file.
        for (Filter filter : mFilters)
            // Add the filtered/stored image to the array.
            filteredArray.add(makeFilterDecoratorWithImage(filter,
                                                           image).run());

        // Return the results array.
        return filteredArray;
    }

    /**
     * @return Returns true if the @a url is in cache, else false.
     */
    private boolean urlCached(URL url) {
        // Initialize the count to 0.
        int count = 0;

        // Iterate through the list of filters and check to see which
        // images already exist in the cache.
        for (Filter filter : mFilters)
            // Count the number of files that already exist.
            if (urlCached(url, filter.getName()))
                count++;
            
        // A count > 0 means the url has already been cached.
        return count > 0;
    }
}

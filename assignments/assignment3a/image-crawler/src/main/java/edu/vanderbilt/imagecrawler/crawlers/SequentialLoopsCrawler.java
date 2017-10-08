package edu.vanderbilt.imagecrawler.crawlers;

import java.net.URL;

import edu.vanderbilt.imagecrawler.crawlers.framework.ImageCrawlerBase;
import edu.vanderbilt.imagecrawler.utils.Array;
import edu.vanderbilt.imagecrawler.utils.Crawler;
import edu.vanderbilt.imagecrawler.utils.ExceptionUtils;
import edu.vanderbilt.imagecrawler.utils.Image;

import static edu.vanderbilt.imagecrawler.utils.Crawler.Type.PAGE;

/**
 * This uses Java 7 object-oriented features to perform an "image
 * crawl" starting from a root Uri.  Images from HTML page reachable
 * from the root Uri are downloaded from a remote web server or the
 * local file system and stored in files that can be displayed to the
 * user.
 * <p>
 * This implementation is entirely sequential and uses no Java 8
 * features at all.  It therefore serves as a baseline to compare all
 * the other implementation strategies.
 * <p>
 * See https://www.mkyong.com/java/jsoup-basic-web-crawler-example for
 * an overview of how to write a web crawler using jsoup.
 */
public class SequentialLoopsCrawler
        extends ImageCrawlerBase {
    /**
     * Perform the web crawl.
     *
     * @param pageUri The URI that we're crawling at this point
     * @param depth   The current depth of the recursive processing
     * @return A future to the number of images downloaded/stored
     */
    @Override
    protected int performCrawl(String pageUri,
                               int depth) {

        // Throw an exception if the the stop crawl flag has been set.
        throwExceptionIfCancelled();

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
        // and add the new uri to the hashset so we don't try to
        // revisit it again unnecessarily.
        else if (!mUniqueUris.putIfAbsent(pageUri)) {
            printDiagnostics(TAG + ": Already processed " + pageUri);
            // Return 0 if we've already examined this uri.
            return 0;
        }

        // Use completable futures to asynchronously (1) download and
        // store images on this page and (2) crawl other hyperlinks
        // accessible via this page.
        else {
            try {
                // Get the HTML page associated with pageUri.
                Crawler.Page page = mWebPageCrawler.getPage(pageUri);

                // Download/store all images on this page and save the
                // count images that were successfully processed.
                int imagesOnPage = processImages(getImagesOnPage(page));

                // Recursively crawl through hyperlinks that are in
                // this page and get an array of counts of the number
                // of images in each hyperlink.
                Array<Integer> imagesOnPageLinks =
                        crawlHyperLinksOnPage(page, depth + 1);

                // Use a loop to compute the number of images
                // downloaded and stored.
                for (Integer imagesOnPageLink : imagesOnPageLinks)
                    imagesOnPage += imagesOnPageLink;

                // Return the combined result.
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
     * Recursively crawl through hyperlinks that are in a {@code
     * page}.
     *
     * @return An array of integers, each of which counts how many
     * images were downloaded/stored for each hyperlink on the page
     */
    private Array<Integer> crawlHyperLinksOnPage(Crawler.Page page,
                                                 int depth) {
        // Create the results array.
        Array<Integer> resultsArray = new Array<>();

        // Iterate through all hyperlinks on this page.
        for (String url : page.getObjectsAsStrings(PAGE)) 
            // Recursively visit all the hyperlinks on this page and
            // add the result to the array.
            resultsArray.add(performCrawl(url, depth));

        // Return the results array.
        return resultsArray;
    }

    /**
     * Download and store each image in the {@code urls} array.
     *
     * @param urls An array of URLs to images to process
     * @return A count of the number of images downloaded/stored
     */
    private int processImages(Array<URL> urls) {
        // Count of the number of images downloaded/stored.
        int downloadedAndStoredImages = 0;

        for (URL url : urls) {
            // Get the Image for this URL either (1) returning the
            // image from a local cache if it's been downloaded
            // already or (2) downloading the image via its URL and
            // storing it in the local cache.
            Image image = getImage(url);

            if (image != null) 
                // Increment count.
                downloadedAndStoredImages++;
        }

        // Return the number of images downloaded/stored.
        return downloadedAndStoredImages;
    }
}

package edu.vanderbilt.imagecrawler.crawlers;

import java.net.URL;

import edu.vanderbilt.imagecrawler.transforms.Transform;
import edu.vanderbilt.imagecrawler.utils.Array;
import edu.vanderbilt.imagecrawler.utils.UnsynchronizedArray;
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
       extends ImageCrawler {
    /**
     * Perform the web crawl.
     *
     * @param pageUri The URI that's being crawled at this point
     * @param depth   The current depth of the recursive processing
     * @return A future to the number of images downloaded/stored
     */
    @Override
    protected int performCrawl(String pageUri,
                               int depth) {

        // Throw an exception if the the stop crawl flag has been set.
        throwExceptionIfCancelled();

        log(">> Depth: " + depth + " [" + pageUri + "]" + " (" + Thread.currentThread().getId() + ")");

        // Return 0 if we've reached the depth limit of the web
        // crawling.
        if (depth > mMaxDepth) {
            log("Exceeded max depth of " + mMaxDepth);
            return 0;
        }

        // Check to see if we've already visited this Uri
        // and add the new uri to the hashset so we don't try to
        // revisit it again unnecessarily.
        else if (!mUniqueUris.putIfAbsent(pageUri)) {
            log("Already processed " + pageUri);
            // Return 0 if we've already examined this uri.
            return 0;
        }

        // Synchronously (1) download and store images on this page
        // and (2) crawl other hyperlinks accessible via this page.
        else {
            try {
                // Get the HTML page associated with pageUri.
                Crawler.Page page = mWebPageCrawler.getPage(pageUri);

                // Download/store all images on this page and save the
                // count images that were successfully processed.
                int imagesOnPage = processImages(getImagesOnPage(page));

                // Recursively crawl through hyperlinks that are in
                // this page and get a count of the number of images
                // in each hyperlink.
                int imagesOnPageLinks =
                    crawlHyperLinksOnPage(page, depth + 1);

                // Return the combined result.
                return imagesOnPage + imagesOnPageLinks;
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
     * @return An integer that counts how many images were
     * downloaded/stored for each hyperlink on the page
     */
    private int crawlHyperLinksOnPage(Crawler.Page page,
                                      int depth) {
        // Create the results.
        int results = 0;

        // Iterate through all hyperlinks on this page.
        for (String url : page.getPageElementsAsStrings(PAGE))
            // Recursively visit all the hyperlinks on this page and
            // add the result to the array.
            results += performCrawl(url, depth);

        // Return the results.
        return results;
    }

    /**
     * Download, process, and store each image in the {@code urls}
     * array.
     *
     * @param urls An array of URLs to images to process
     * @return A count of the number of images processed
     */
    private int processImages(Array<URL> urls) {
        // Create the results array.
        int transformedImages = 0;

        for (URL url : urls) {
            // Get the Image for this URL either (1) returning the
            // image from a local cache if it's been downloaded
            // already or (2) downloading the image via its URL and
            // storing it in the local cache.
            Image rawImage = getOrDownloadImage(url);

            if (rawImage != null)
                // Apply each transform to this image and cache the
                // resulting images and update the transformed image
                // count with the number of successful transform
                // operations.
                for (Image transformedImage : transformImage(rawImage))
                    if (transformedImage != null)
                        transformedImages++;
        }

        // Return the number of images processed.
        return transformedImages;
    }

    /**
     * Download, process, and store an {@code image} by applying all
     * transforms to it.
     *
     * @param image An image that's been downloaded and stored.
     * @return An array of images where a non-null entry indicates the
     * success of an image's transform operations and a null entry
     * indicates failure
     */
    private Array<Image> transformImage(Image image) {
        log("Performing transforms for image %s", image.getSourceUrl());

        // Array holding the results of each transform operation.
        Array<Image> transformArray = new UnsynchronizedArray<>();

        // Process all the URLs for this transform.
        for (Transform transform : mTransforms) {
            // Attempt to create a new cache item for this transform
            // and only apply the transform if a new cache item was
            // actually created (i.e., was not already in the cache).
            if (createNewCacheItem(image, transform)) {
                log("Applying transform %s (image not in cache)",
                    transform.getName());
                // Apply the transform to the image add the result to
                // the transformArray.
                transformArray.add(applyTransform(transform, image));
            } else
                log("SKIPPING transform %s (image already in cache)",
                    transform.getName());
        }

        // Return the results array.
        return transformArray;
    }
}

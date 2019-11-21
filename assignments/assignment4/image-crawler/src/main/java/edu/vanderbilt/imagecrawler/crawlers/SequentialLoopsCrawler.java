package edu.vanderbilt.imagecrawler.crawlers;

import edu.vanderbilt.imagecrawler.transforms.Transform;
import edu.vanderbilt.imagecrawler.utils.Crawler;
import edu.vanderbilt.imagecrawler.utils.Image;
import edu.vanderbilt.imagecrawler.utils.WebPageElement;

import static edu.vanderbilt.imagecrawler.utils.Crawler.Type.IMAGE;
import static edu.vanderbilt.imagecrawler.utils.Crawler.Type.PAGE;

/**
 * This class uses Java 8 features to perform an "image crawl"
 * starting from a root Uri. Images from HTML page reachable from
 * the root Uri are downloaded from a remote web server or the
 * local file system and the results are stored in files that are
 * displayed to the user.
 * <p>
 * This implementation is entirely sequential and uses no Java 8
 * features at all.  It therefore serves as a baseline to compare all
 * the other implementation strategies.
 * <p>
 * See https://www.mkyong.com/java/jsoup-basic-web-crawler-example for
 * an overview of how to write a web crawler using jsoup.
 */
public class SequentialLoopsCrawler // Loaded via reflection
        extends ImageCrawler {
    /**
     * Recursively crawls the passed page at the returing the total
     * number of processed images.
     *
     * @param pageUri The URI that's being crawled at this point
     * @param depth   The current depth of the recursive processing
     * @return The number of images processed at this depth
     */
    @Override
    protected int performCrawl(String pageUri, int depth) {
        // Throw an exception if the the stop crawl flag has been set.
        throwExceptionIfCancelled();

        log(">> Depth: " + depth + " [" + pageUri + "]");

        // Return 0 if we've reached the depth limit of the crawl.
        if (depth > mMaxDepth) {
            log("Exceeded max depth of " + mMaxDepth);
            return 0;
        }

        // Atomically check to see if we've already visited this Uri
        // and add the new uri to the hashset so we don't try to
        // revisit it again unnecessarily.
        if (!mUniqueUris.putIfAbsent(pageUri)) {
            log("Already processed " + pageUri);
            // Return 0 if we've already examined this uri.
            return 0;
        }

        // Recursively crawl all images and hyperlinks on this
        // page returning the total number of processed images.
        return crawlPage(pageUri, depth);
    }

    /**
     * Uses a Java 7 features to (1) download and
     * process images on this page via processImage(), (2)
     * recursively crawl other hyperlinks accessible from this
     * page via crawlPage(), and (3) return a sum of all the
     * image counts.
     *
     * @param pageUri The page uri to crawl.
     * @param depth   The current depth of the recursive processing
     * @return The number of processed images.
     */
    protected int crawlPage(String pageUri, int depth) {
        // Get the HTML page associated with pageUri.
        Crawler.Page page = mWebPageCrawler.getPage(pageUri);

        // The number of images processed at this crawl depth.
        int imageCount = 0;

        // Iterate through all hyperlinks on this page.
        for (WebPageElement e : page.getPageElements(IMAGE, PAGE)) {
            // Recursively visit all the hyperlinks on this page and
            // add the result to the array.
            if (e.getType() == IMAGE) {
                imageCount += processImage(getOrDownloadImage(e.getURL()));
            } else {
                imageCount += performCrawl(e.getUrl(), depth + 1);
            }
        }

        // Return the number of processed images.
        return imageCount;
    }

    /**
     * Process an image by applying any transformations that have
     * not already been applied and cached.
     *
     * @param image A downloaded image.
     * @return The count of transformed images.
     */
    protected int processImage(Image image) {
        // Uses a Java 7 features to:
        // 1. Loop through all transforms
        // 2. Try to create a new cached image item for each
        //    transform skipping any that already cached.
        // 3. Transform and store each non-cached image.
        // 4. Return the count of transformed images.

        // The resulting number of processed images.
        int imageCount = 0;

        // Apply any transforms to this image that have not already
        // been previously applied and cached.
        for (Transform transform : mTransforms) {
            // Attempt to create a new cache item for this transform
            // and only apply the transform if a new cache item was
            // actually created (i.e., was not already in the cache).
            if (createNewCacheItem(image, transform)) {
                // Apply the transformation to the image.
                applyTransform(transform, image);
                // Update the transformed images count.
                imageCount++;
            }
        }

        // Return the number of processed images.
        return imageCount;
    }
}

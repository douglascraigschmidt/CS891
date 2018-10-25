package edu.vanderbilt.imagecrawler.crawlers;

import java.net.URL;
import java.util.Objects;
import java.util.stream.Stream;

import edu.vanderbilt.imagecrawler.utils.Crawler;
import edu.vanderbilt.imagecrawler.utils.Image;

import static edu.vanderbilt.imagecrawler.utils.Crawler.Type.IMAGE;
import static edu.vanderbilt.imagecrawler.utils.Crawler.Type.PAGE;

/**
 * This class uses Java 8 features to perform an "image crawl"
 * starting from a root Uri.  Images from HTML page reachable from the
 * root Uri are either downloaded from a remote web server or read
 * from the local file system, image processing transforms are then
 * applied to each image, and the results are stored in files that can
 * be displayed to the user.
 * <p>
 * This implementation strategy customizes ImageCrawler and uses the
 * Java 8 streams framework to download, transform, and store images
 * in parallel.
 */
public class ParallelStreamsCrawler1
       extends ImageCrawler {
    /**
     * Perform the web crawl.
     *
     * @param pageUri The URI that's being crawled at this point
     * @param depth   The maximum depth for this crawl operation.
     * @return The number of successfully transformed images.
     */
    @Override
    protected int performCrawl(String pageUri, int depth) {
        try {
            // Call the crawlPage method to recursively crawl pages
            // and download/transform any discovered images links.
            // Return the total number of transformed images.
            return crawlPage(pageUri, depth);

        } catch (Exception e) {
            System.err.println("Exception For '"
                               + pageUri
                               + "': "
                               + e.getMessage());
            return 0;
        }
    }

    /**
     * Perform the web crawl.
     *
     * @param pageUri The URI that we're crawling at this point
     * @param depth The current depth of the recursive processing
     * @return A stream containing the number of images processed at this level
     */
    protected Integer crawlPage(String pageUri, int depth) {
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
        } else {
            log("Processing page %s", pageUri);

            // Use a Java 8 parallel stream to (1) download and
            // process images on this page via processImage(), (2)
            // recursively crawl other hyperlinks accessible from this
            // page via crawlPage(), and (3) return a sum of all the
            // image counts.

            // TODO -- you fill in here, replacing 'return 0' with the
            // appropriate code.
            return 0;
        }
    }

    /**
     * Download, process, and store the image associated with the
     * {@code url}.
     *
     * @param url A image url to process
     * @return A stream of booleans indicating the success or failure of
     * each transform operation performed on the downloaded image
     */
    protected Integer processImage(String url) {
        log("Downloading image: ", url);

        try {
            // Get a cached image or else download it.
            // TODO -- you fill in here, replacing null with
            // appropriate code.
            Image image = null;

            if (image != null) {
                // Call transformImage() to apply all the transforms
                // to the image and then return a count of those that
                // succeeded.

                // TODO -- you fill in here, replacing 0 with the
                // appropriate calls.
                return 0;
            } else {
                return 0;
            }
        } catch (Exception e) {
            // Wrap and rethrow.
            throw new RuntimeException(e);
        }
    }

    /**
     * Download, process, and store an {@code image} by applying all
     * transforms to it.
     *
     * @param image An image that's been downloaded and stored
     * @return A stream of successfully transformed images
     */
    protected Stream<Image> transformImage(Image image) {
        log("Performing %d parallel transforms of image %s ...",
            mTransforms.size(), image.getSourceUrl());

        // Use a Java 8 parallel stream to do the following:
        // 1. Convert the list of transforms into a parallel stream.
        // 2. Ignore any images that have already been transformed and
        //    cached locally.
        // 3. Transform/store any non-cached images.
        // 4. Ignore any unsuccessful transformations.
        // 5. Return a stream of successfully transformed/stored images. 

        // TODO -- you fill in here, replacing null with the
        // appropriate parallel stream operations.
        return null;
    }
}

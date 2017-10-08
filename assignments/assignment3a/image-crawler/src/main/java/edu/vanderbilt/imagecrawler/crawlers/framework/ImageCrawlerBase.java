package edu.vanderbilt.imagecrawler.crawlers.framework;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.vanderbilt.imagecrawler.crawlers.CompletableFutureCrawler1;
import edu.vanderbilt.imagecrawler.crawlers.SequentialLoopsCrawler;
import edu.vanderbilt.imagecrawler.platform.Controller;
import edu.vanderbilt.imagecrawler.platform.PlatformImage;
import edu.vanderbilt.imagecrawler.utils.Array;
import edu.vanderbilt.imagecrawler.utils.ArrayCollector;
import edu.vanderbilt.imagecrawler.utils.BlockingTask;
import edu.vanderbilt.imagecrawler.utils.ConcurrentHashSet;
import edu.vanderbilt.imagecrawler.utils.Crawler;
import edu.vanderbilt.imagecrawler.utils.IOUtils;
import edu.vanderbilt.imagecrawler.utils.Image;
import edu.vanderbilt.imagecrawler.utils.ImageCache;
import edu.vanderbilt.imagecrawler.utils.WebPageCrawler;

/**
 * This abstract class factors out methods and fields that are common
 * to all image crawler implementation strategies.
 */
public abstract class ImageCrawlerBase
        implements Runnable {
    /**
     * Debugging tag.
     */
    protected final String TAG = this.getClass().getName();

    /**
     * A cache of unique URIs that have already been processed.
     */
    protected ConcurrentHashSet<String> mUniqueUris;

    /**
     * A web page crawler that parses web pages.
     */
    protected WebPageCrawler mWebPageCrawler;

    /**
     * The maximum crawl depth (from options).
     */
    protected int mMaxDepth;

    /**
     * The root URL or pathname to start the search (from options).
     */
    private String mRootUri;

    /**
     * The cache where images are stored after they have been
     * downloaded.
     */
    private ImageCache mImageCache;

    /**
     * A boolean supplier lambda provided by the controller to
     * be called periodically to check if the application has
     * decided to cancel the crawl.
     */
    private BooleanSupplier mIsCancelledSupplier;

    /**
     * A Function lambda provided by the controller to create
     * a new platform dependent image object.
     */
    private Function<byte[], PlatformImage> mNewImageFunction;
    /**
     * A function lambda that maps a uri to a platform dependant
     * input stream.
     */
    private Function<String, InputStream> mMapUriToInputStream;

    /**
     * Keeps track of how long a given test has run and is also
     * used to check if the crawler is currently running.
     */
    private long mStartTime;

    /**
     * Keeps track of all the execution times.
     */
    private List<Long> mExecutionTimes = new ArrayList<>();

    /**
     * Flag used to stop/cancel a crawl.
     */
    private boolean mCancelled;

    /**
     * Constructor that is only available to inner Factory class to
     * support construction using newInstance().
     */
    protected ImageCrawlerBase() {
    }

    /**
     * Called from Factory after creating a new crawler instance and
     * therefore is declared as "package private". Transfers required
     * values from platform Controller to local fields to show
     * explicitly what this and sub-classes require from the
     * controller instance.
     */
    void initialize(Controller controller) {
        // A function lambda that will map a uri to a platform
        // dependant input stream.
        mMapUriToInputStream = controller::mapUriToInputStream;

        // Store the root Uri provided by the controller.
        mRootUri = controller.options.rootUrl;

        // The maximum depth for this crawl.
        mMaxDepth = controller.options.maxDepth;

        // A Function lambda the constructs a new platform
        // dependant image.
        mNewImageFunction = controller::newImage;

        // Setup a new WebPageCrawler passing it the platform
        // dependant url to input stream mapping function (used for
        // access local web pages in app resources or assets).
        mWebPageCrawler =
            new WebPageCrawler(controller::mapUriToInputStream);

        // Create a new image cache passing in the getCacheDir lambda
        // provided by the controller.
        mImageCache = new ImageCache(controller::getCacheDir);

        // Initialize the cache of processed Uris.
        mUniqueUris = new ConcurrentHashSet<>();
    }

    /**
     * A hook method (also a template method) that does bookkeeping
     * operations and dispatches the subclass's performCrawl() hook
     * method to start implementation strategy processing.
     */
    @Override
    public void run() {
        try {
            if (mStartTime != 0) {
                throw new IllegalStateException("The crawler is already " +
                                                "running.");
            }

            // Start timing the test run.
            startTiming();

            // Perform the web crawling starting at the root Uri, given an
            // initial depth count of 1.
            int totalImages = performCrawl(mRootUri, 1);

            // Stop timing the test run.
            stopTiming();

            printDiagnostics(TAG + ": downloaded and processed "
                             + totalImages
                             + " total image(s)");
        } catch (Exception e) {
            if (e.getCause() instanceof CancellationException) {
                printDiagnostics("Crawl was cancelled.");
            } else {
                System.err.println("Crawl was abnormally terminated: "
                                   + e.getMessage());
                throw e;
            }
        } finally {
            // Always reset the start time to 0 so that it can be
            // used as a flag to see if the crawler is currently
            // running. This allows a single crawler instance to
            // be reusable avoiding unnecessary memory allocations.
            mStartTime = 0;

            // Reset cancelled flag
            mCancelled = false;
        }
    }

    /**
     * Sets a flag that is periodically check at strategic
     * locations to determine if all processing should be
     * cancelled.
     */
    public void stopCrawl() {
        mCancelled = true;
    }

    /**
     * Abstract method required to be implemented by any sub class
     * implementation.
     *
     * @param pageUri The URI that we're crawling at this point
     * @param depth   The current depth of the recursive processing
     * @return A future to the number of images processed
     */
    protected abstract int performCrawl(String pageUri, int depth);

    /**
     * Return an array of all the IMG SRC URLs in this document.
     */
    protected Array<URL> getImagesOnPage(Crawler.Page page) {
        // Return an array of all the IMG SRC URLs in this page.
        return page
            // Select all the image elements in the page.
            .getObjectsAsUrls(Crawler.Type.IMAGE)

            // Convert the elements to a stream.
            .stream()

            // Remove duplicate image URL strings.
            .distinct()

            // Trigger intermediate operations and return an array.
            .collect(ArrayCollector.toArray());
    }

    /**
     * Factory method that retrieves the image associated with the
     * {@code url} and creates an image to encapsulate it.
     */
    private Image downloadImage(URL url) {
        // Before downloading the next image, check for cancellation
        // and throw and exception if cancelled.
        throwExceptionIfCancelled();

        // Creates a new ByteArrayOutputStream to write the downloaded
        // contents to a byte array, which is a generic form of the
        // image.
        ByteArrayOutputStream ostream =
            new ByteArrayOutputStream();

        // Creates an InputStream from the inputUrl from which to read
        // the image data. The input stream is platform dependant, so
        // we call the controller to provide the platform dependant
        // mapping of the url to an input stream.
        try (InputStream istream =
             mMapUriToInputStream.apply(url.toString())) {

            // Copy the input stream into a byte array.
            byte[] bytes = IOUtils.toBytes(istream);

            // Call platform dependant lambda function to create
            // a new platform image from the bytes, and then construct
            // and return an Image object that wraps this platform
            // image and its source url.
            return new Image(url, mNewImageFunction.apply(bytes));
        } catch (IOException e) {
            // "Try-with-resources" will clean up the istream
            // automatically.

            System.out.println("Error downloading url " + url + ": " + e);
            e.printStackTrace();

            // Return null so that the crawler can continue on to
            // process other images, hopefully with more luck than
            // this one!
            return null;
        }
    }

    /**
     * Asynchronously download an image from the {@code url} parameter
     * and return a CompletableFuture that completes when the image
     * finishes being downloaded and stored in the cache.
     */
    protected CompletableFuture<Image> downloadAndStoreImageAsync(URL url) {
        // Asynchronously download/store an Image from the url
        // parameter.
        return CompletableFuture.supplyAsync(() -> getImage(url));
    }

    /**
     * Convert URL to an Image by downloading each image via its URL.
     * This call ensures the common fork/join thread pool is expanded
     * to handle the blocking image download.
     */
    protected Image blockingDownload(URL url) {
        return BlockingTask.callInManagedBlock(() -> downloadImage(url));
    }

    /**
     * Checks to see if the {@code image} already exists in the
     * cache. If not, the cache isItemCached() method will atomically
     * creates a new file based on the image url.
     *
     * @return true if the image already exists in file system, else
     *         false.
     */
    protected boolean imageCached(Image image) {
        // Check if the image has already been cached.
        boolean isCached =
                mImageCache.isItemCached(null,
                                         image.getSourceUrl().toString());

        /*
        printDiagnostics("URL " + image.getSourceUrl() + " is " +
                (isCached ? "already" : "not") + " in the " +
                "cache.");
        */

        return isCached;
    }

    /**
     * Checks to see if the {@code url} is already exists in the file
     * system.  If not, it atomically creates a new file based on the
     * {@code url} and returns false, else true.
     *
     * @return true if the {@code url} already exists in file system, else false.
     */
    private boolean isUrlCached(URL url,
                                String cacheGroupId) {
        // Check if the image has already been cached.
        boolean isCached = mImageCache.isItemCached(cacheGroupId,
                                                    url.toString());

        /*
        printDiagnostics("URL " + url.toString() + " is " +
                        (isCached ? "already" : "not") + " in the " +
                        "cache.");
        */

        return isCached;
    }

    /**
     * This method first checks the cache for an image that matches
     * the {@code url} and if found returns that; otherwise, it calls
     * the {@code downloadImage} helper method to download the image
     * and then stores the image in the cache and returns the result.
     *
     * @return The url to get from cache or by downloading.
     */
    protected Image getImage(URL url) {
        // If the image isn't cached, the download it.  Note that this
        // isUrlCached call will automatically create a new cache item
        // if the image was not cached.
        if (!isUrlCached(url, null)) {
            return blockingDownload(url);
        } else {
            // The image is not cached, so get instance of the new
            // cache item created by the isUrlCached call above.
            ImageCache.Item item =
                mImageCache.getItem(null,
                                    url.toString());

            // Does the following:
            // 1. Get the cached item's input stream.
            // 2. Convert the input stream to a byte array.
            // 3. Creates a platform dependant image from the byte array.
            // 4. Decorates the the platform dependant image in an Image object.
            // 5. The try block automatically will close the input stream.
            // 6. Returns the Image decorator object or null if an exception occurred.
            try (InputStream inputStream = item.getInputStream()) {
                return new Image(mNewImageFunction.apply(IOUtils.toBytes(inputStream)));
            } catch (IOException e) {
                System.err.println("Download image failed: " + url);
                return null;
            }
        }
    }

    /**
     * Return the time needed to execute the test.
     */
    public List<Long> executionTimes() {
        return mExecutionTimes;
    }

    /**
     * Start timing the test run.
     */
    private void startTiming() {
        // Note the start time.
        mStartTime = System.nanoTime();
    }

    /**
     * Stop timing the test run.
     */
    private void stopTiming() {
        mExecutionTimes.add((System.nanoTime() - mStartTime) / 1_000_000);
    }

    /**
     * Conditionally prints the {@code string} depending on the current
     * setting of the Options singleton.
     */
    protected void printDiagnostics(String string) {
        if (Controller.isDiagnosticsEnabled()) {
            System.out.println("DIAGNOSTICS: " + string);
        }
    }

   /**
     * @return {@code true} if crawl should be cancelled, {@code false} if not.
     */
    public boolean isCancelled() {
        return mCancelled;
    }

    /**
     * Throws a CancellationException if the application has
     * decided to cancelled the crawl.
     */
    protected void throwExceptionIfCancelled() {
        if (isCancelled()) {
            throw new CancellationException("The crawl has been cancelled.");
        }
    }

    /**
     * Supported crawlers that can be applied to downloaded
     * images. The enum values are set to class types so that they can
     * be used to create crawler objects using newInstance().
     */
    public enum Type {
        SEQUENTIAL_LOOPS(SequentialLoopsCrawler.class),
        COMPLETABLE_FUTURE_1(CompletableFutureCrawler1.class);

        private final Class<? extends ImageCrawlerBase> clazz;

        Type(Class<? extends ImageCrawlerBase> clazz) {
            this.clazz = clazz;
        }

        @Override
        public String toString() {
            return clazz.getSimpleName();
        }
    }

    /**
     * Java Utility class used to create new instances of supported
     * crawlers.
     */
    public static class Factory {
        /**
         * Disallow object creation.
         */
        private Factory() {
        }

        /**
         * Creates the specified {@code type} crawler. Any images
         * downloaded this crawler will be saved in a folder that has
         * the same name as the class.
         *
         * @param crawlerType Type of transform.
         * @param controller  A controller that contains all crawler options and
         *                    platform dependent support methods.
         * @return A transform instance of the specified type.
         */
        public static ImageCrawlerBase newCrawler(Type crawlerType,
                                                  Controller controller) {
            try {
                ImageCrawlerBase crawler = crawlerType.clazz.newInstance();
                crawler.initialize(controller);
                return crawler;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Creates a list of new crawler instances matching the past crawler
         * types.
         *
         * @param crawlerTypes List of crawler types to create.
         * @param controller   A controller that contains all crawler options
         *                     and platform dependent support methods.
         * @return A list of new crawler instances.
         */
        public static List<ImageCrawlerBase> newCrawlers(List<Type> crawlerTypes,
                                                         Controller controller) {
            return crawlerTypes
                .stream()
                .map(type -> newCrawler(type, controller))
                .collect(Collectors.toList());
        }
    }
}

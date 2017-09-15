package edu.vanderbilt.crawlers.framework;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import edu.vanderbilt.filters.Filter;
import edu.vanderbilt.filters.FilterDecoratorWithImage;
import edu.vanderbilt.filters.OutputFilterDecorator;
import edu.vanderbilt.platform.Device;
import edu.vanderbilt.utils.Array;
import edu.vanderbilt.utils.ArrayCollector;
import edu.vanderbilt.utils.BlockingTask;
import edu.vanderbilt.utils.CacheUtils;
import edu.vanderbilt.utils.ConcurrentHashSet;
import edu.vanderbilt.utils.Crawler;
import edu.vanderbilt.utils.Image;

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
     * The List of filters to apply to the images.
     */
    protected List<Filter> mFilters;

    /**
     * The root URL or pathname to start the search.
     */
    protected String mRootUri;

    /**
     * A cache of unique URIs that have already been processed.
     */
    protected ConcurrentHashSet<String> mUniqueUris;

    /**
     * Flag used to cancel the current crawl.
     */
    private boolean mStopCrawl;

    /**
     * Constructor that is only used by CrawlerFactory.
     */
    protected ImageCrawlerBase() {
    }

    /**
     * Constructor that initializes filters and rootUri.
     */
    public ImageCrawlerBase(List<Filter> filters,
                            String rootUri) {
    	initialize(filters, rootUri);
    }

    /**
     * Called from CrawlerFactory after creating a new crawler instance.
     */
    protected void initialize(List<Filter> filters,
                              String rootUri) {
        // Should only ever be called once.
        if (mFilters != null || mRootUri != null || mUniqueUris != null) {
            throw new IllegalStateException("A crawler should onlly be initialized once.");
        }

        // Store the Filters to apply as a list.
        mFilters = filters;

        // Store the root Uri.
        mRootUri = rootUri;

        // Initialize the cache of processed Uris.
        mUniqueUris = new ConcurrentHashSet<>();
    }

    /**
     * Return an array of all the IMG SRC URLs in this document.
     */
    protected Array<URL> getImagesOnPage(Crawler.Container page) {
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
     * Factory method that retrieves the image associated with the @a
     * url and creates an Image to encapsulate it.
     */
    protected Image downloadImage(URL url) {
        // Before downloading the next image, check for cancellation
        // and throw and exception if cancelled.
        Device.throwExceptionIfCancelled();

        // Obtain byte for the image at the URL.
        byte[] imageData = Device.platform().downloadContent(url);

        // Return a new image object.
        return new Image(Device.platform().newImage(url, imageData));
    }

    /**
     * Transform URL to an Image by downloading each image via its
     * URL.  This call ensures the common fork/join thread pool is
     * expanded to handle the blocking image download.
     */
    protected Image blockingDownload(URL url) {
        return BlockingTask.callInManagedBlock(()
                                               -> downloadImage(url));
    }

    /**
     * Factory method that makes a new @a FilterDecoratorWithImage.
     */
    protected FilterDecoratorWithImage makeFilterDecoratorWithImage(Filter filter,
                                                                    Image image) {
        return new FilterDecoratorWithImage(new OutputFilterDecorator(filter),
                                            image);
    }

    /**
     * Checks to see if the @a url is already exists in the file
     * system.  If not, it atomically creates a new file based on
     * combining the @a url with the @a filterName and returns false,
     * else true.

     * @return true if the @a url already exists in file system, else
     * false.
     */
    protected boolean urlCached(URL url, String filterName) {
        // Construct a new cache file based on the filename for the URL.
        // This call will automatically create any required directories.
        File imageFile = CacheUtils.mapUrlToCacheFile(filterName, url);

        try {
            // The URL is already cached if imageFile exists so we
            // negate the return value from createNewFile().
            return !imageFile.createNewFile();
        } catch (IOException e) {
            // e.printStackTrace();
            printDiagnostics("file " + imageFile.toString() + e);
            return true;
        }
    }

    /**
     * Keeps track of how long a given test has run.
     */
    private long mStartTime;

    /**
     * Keeps track of all the execution times.
     */
    private List<Long> mExecutionTimes = new ArrayList<>();

    /**
     * Return the time needed to execute the test.
     */
    public List<Long> executionTimes() {
        return mExecutionTimes;
    }

    /**
     * Start timing the test run.
     */
    protected void startTiming() {
        // Note the start time.
        mStartTime = System.nanoTime();
    }

    /**
     * Stop timing the test run.
     */
    protected void stopTiming() {
        mExecutionTimes.add((System.nanoTime() - mStartTime) / 1_000_000);
    }

    /**
     * Conditionally prints the @a string depending on the current
     * setting of the Options singleton.
     */
    protected void printDiagnostics(String string) {
        if (Device.options().getDiagnosticsEnabled())
            System.out.println("DIAGNOSTICS: " + string);
    }

    /**
     * Sets a flag that will stop the current crawl.
     */
    public void stopCrawl() {
        mStopCrawl = true;
    }

    /**
     * @return {@code true} if crawl should be cancelled, {@code false} if not.
     */
    public boolean isCancelled() {
        return mStopCrawl;
    }
}

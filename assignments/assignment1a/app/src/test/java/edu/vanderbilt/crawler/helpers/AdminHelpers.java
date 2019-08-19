package edu.vanderbilt.crawler.helpers;

import java.io.File;
import java.net.URI;

import edu.vanderbilt.imagecrawler.crawlers.ImageCrawler;
import edu.vanderbilt.imagecrawler.platform.Controller;
import edu.vanderbilt.imagecrawler.platform.JavaCache;
import edu.vanderbilt.imagecrawler.platform.Platform;
import edu.vanderbilt.imagecrawler.utils.AdminUtils;
import edu.vanderbilt.imagecrawler.utils.Options;

/**
 * Helper methods only used for assignment testing and administration.
 */
public class AdminHelpers {
    /**
     * Helper method that centralizes all crawl building test methods so
     * that they predictably use the same parametrized crawler/downloader.
     *
     * @param crawlType  The crawler type to use.
     * @param controller Controller with options and transforms.
     * @param index      boolean flag indicating if indexing should be performed
     *                   on the downloaded directory.
     */
    public static void downloadIntoDirectory(
            ImageCrawler.Type crawlType,
            Controller controller,
            boolean index) {

        info("----------------------------------------------------");
        info("Downloading from:   " + controller.mOptions.mRootUrl);
        info("Downloading into:   " + controller.mOptions.mDownloadDirName);
        System.out.print("      Transforms:   ");

        for (int i = 0; i < controller.mTransforms.size(); i++) {
            String name = controller.mTransforms.get(i).toString();
            if (i == 0) {
                info(name);
            } else {
                info("                    " + name);
            }
        }

        info("----------------------------------------------------");

        File cacheDir = controller.getCacheDir();

        // Leverage the handy static delete contents cache
        // method to clear the cache before starting the crawl.
        info("Clearing directory: " + cacheDir);
        JavaCache.instance().clear();

        info("Running with crawl strategy " + crawlType.name());

        // Create and run the crawler.
        ImageCrawler.Factory.newCrawler(crawlType, controller).run();

        info("----------------------------------------------------");
        if (index) {
            info("Indexing directory: " + cacheDir);
            controller.mTransforms.forEach(it ->
                    AdminUtils.indexDirectory(
                            cacheDir.getAbsolutePath(),
                            it.getName()));
        }
        info("----------------------------------------------------");
    }

    public static String getDefaultWebRootUrl() throws Exception {
        return Options.DEFAULT_WEB_URL;
    }

    /**
     * @return Local web pages directory for Java testing.
     */
    public static String getDefaultJavaLocalRootUrl() throws Exception {
        // Just swaps the http scheme for the custom project scheme + authority.
        URI uri = new URI(Options.DEFAULT_WEB_URL);
        return Platform.PROJECT_URI_PREFIX + "/" + uri.getHost() + uri.getPath();
    }

    /**
     * Helper to make printing output less verbose.
     */
    public static void info(String msg) {
        System.out.println(msg);
    }

    /**
     * Helper to make printing output less verbose.
     */
    public static void error(String msg) {
        System.err.println(msg);
    }
}

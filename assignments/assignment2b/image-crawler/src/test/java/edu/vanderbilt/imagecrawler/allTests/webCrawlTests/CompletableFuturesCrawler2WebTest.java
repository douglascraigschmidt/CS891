package edu.vanderbilt.imagecrawler.allTests.webCrawlTests;

import org.junit.Test;

import edu.vanderbilt.imagecrawler.crawlers.ImageCrawler;
import edu.vanderbilt.imagecrawler.helpers.AdminHelpers;
import edu.vanderbilt.imagecrawler.helpers.DefaultController;
import edu.vanderbilt.imagecrawler.platform.Controller;

import static edu.vanderbilt.imagecrawler.helpers.AdminHelpers.recursivelyCompareDirectories;
import static edu.vanderbilt.imagecrawler.helpers.Directories.getJavaGroundTruthDir;

/**
 * Web crawl test (internet connection required).
 */
public class CompletableFuturesCrawler2WebTest {
    /**
     * Test web crawl which requires an internet connection.
     * The downloaded images are compared to the images in the project's
     * ground-truth directory.
     */
    @Test
    public void completableFutures2CrawlerWebTest() throws Exception {
        Controller controller = DefaultController.build(true);

        // Perform the local crawl using the sequential streams crawler.
        AdminHelpers.downloadIntoDirectory(
                ImageCrawler.Type.COMPLETABLE_FUTURES2,
                controller,
                false);

        // Compare the download cache with the contents of ground-truth directory.
        recursivelyCompareDirectories(
                getJavaGroundTruthDir(),
                controller.getCacheDir()
        );
    }
}

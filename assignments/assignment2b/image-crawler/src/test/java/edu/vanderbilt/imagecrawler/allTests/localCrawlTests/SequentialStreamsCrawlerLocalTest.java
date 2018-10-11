package edu.vanderbilt.imagecrawler.allTests.localCrawlTests;

import org.junit.Test;

import edu.vanderbilt.imagecrawler.crawlers.ImageCrawler;
import edu.vanderbilt.imagecrawler.helpers.AdminHelpers;
import edu.vanderbilt.imagecrawler.helpers.DefaultController;
import edu.vanderbilt.imagecrawler.platform.Controller;

import static edu.vanderbilt.imagecrawler.helpers.AdminHelpers.recursivelyCompareDirectories;
import static edu.vanderbilt.imagecrawler.helpers.Directories.getJavaGroundTruthDir;

/**
 * Local crawl test (no internet connection required).
 */
public class SequentialStreamsCrawlerLocalTest {
    /**
     * Test local crawl which does not require an internet connection.
     * The downloaded images are compared to the images in the project's
     * ground-truth directory.
     */
    @Test
    public void sequentialStreamsCrawlerLocalTest() throws Exception {
        Controller controller = DefaultController.build(true);

        // Perform the local crawl using the completable futures crawler.
        AdminHelpers.downloadIntoDirectory(
                ImageCrawler.Type.SEQUENTIAL_STREAMS,
                controller,
                false);

        // Compare the download cache with the contents of ground-truth directory.
        recursivelyCompareDirectories(
                getJavaGroundTruthDir(),
                controller.getCacheDir()
        );
    }
}

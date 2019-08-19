package edu.vanderbilt.imagecrawler.crawler;

import org.junit.Test;

import edu.vanderbilt.imagecrawler.crawlers.ImageCrawler;
import edu.vanderbilt.imagecrawler.helpers.AdminHelpers;
import edu.vanderbilt.imagecrawler.helpers.BuildController;
import edu.vanderbilt.imagecrawler.platform.Controller;

import static edu.vanderbilt.imagecrawler.crawlers.ImageCrawler.Type.SEQUENTIAL_LOOPS;
import static edu.vanderbilt.imagecrawler.helpers.AdminHelpers.recursivelyCompareDirectories;
import static edu.vanderbilt.imagecrawler.helpers.Directories.getJavaGroundTruthDir;

/**
 * OPTIONAL Local crawl test (no internet connection required).
 */
public class Assignment1aCrawlerTest {
    private static ImageCrawler.Type crawlerType = SEQUENTIAL_LOOPS;

    /**
     * Test local crawl which does not require an internet connection.
     * The downloaded images are compared to the images in the project's
     * ground-truth directory.
     */
    @Test
    public void localCrawlTest() throws Exception {
        Controller controller = BuildController.build(true);

        // Perform the local crawl using the completable futures crawler.
        AdminHelpers.downloadIntoDirectory(
                crawlerType,
                controller,
                false);

        // Compare the download cache with the contents of ground-truth directory.
        recursivelyCompareDirectories(
                getJavaGroundTruthDir(),
                controller.getCacheDir()
        );
    }
}

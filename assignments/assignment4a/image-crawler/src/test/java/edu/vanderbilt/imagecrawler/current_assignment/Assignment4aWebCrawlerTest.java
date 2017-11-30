package edu.vanderbilt.imagecrawler.current_assignment;

import org.junit.Test;

import edu.vanderbilt.imagecrawler.crawlers.framework.ImageCrawler;
import edu.vanderbilt.imagecrawler.helpers.AdminHelpers;
import edu.vanderbilt.imagecrawler.platform.Controller;

import static edu.vanderbilt.imagecrawler.helpers.AdminHelpers.recursivelyCompareDirectories;
import static edu.vanderbilt.imagecrawler.helpers.Controllers.buildAssignment4aController;
import static edu.vanderbilt.imagecrawler.helpers.Directories.getJavaGroundTruthDir;


/**
 * Web crawl test (internet connection required).
 */
public class Assignment4aWebCrawlerTest {
    private static ImageCrawler.Type crawlerType =
        ImageCrawler.Type.FORK_JOIN1;

    /**
     * Test web crawl which requires an internet connection.
     * The downloaded images are compared to the images in the project's
     * ground-truth directory.
     */
    @Test
    public void webImageCrawlerTest() throws Exception {
        Controller controller = buildAssignment4aController(false);

        // Perform the local crawl using the sequential streams crawler.
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

package edu.vanderbilt.imagecrawler;

import org.junit.Test;

import edu.vanderbilt.imagecrawler.crawlers.framework.ImageCrawlerBase;
import edu.vanderbilt.imagecrawler.helpers.AdminHelpers;
import edu.vanderbilt.imagecrawler.platform.Controller;

import static edu.vanderbilt.imagecrawler.helpers.AdminHelpers.recursivelyCompareDirectories;
import static edu.vanderbilt.imagecrawler.helpers.Controllers.buildAssignment2bController;
import static edu.vanderbilt.imagecrawler.helpers.Directories.getJavaGroundTruthDir;


/**
 * Web crawl test (internet connection required).
 */
public class Assignment2bWebCrawlerTest {
    private static ImageCrawlerBase.Type crawlerType =
            ImageCrawlerBase.Type.PARALLEL_STREAMS;

    /**
     * Test web crawl which requires an internet connection.
     * The downloaded images are compared to the images in the project's
     * ground-truth directory.
     */
    @Test
    public void webImageCrawlerTest() throws Exception {
        Controller controller = buildAssignment2bController(false);

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

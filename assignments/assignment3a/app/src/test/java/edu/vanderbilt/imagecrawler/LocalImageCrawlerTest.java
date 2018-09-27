package edu.vanderbilt.imagecrawler;

import org.junit.Test;

import static edu.vanderbilt.imagecrawler.helpers.TestHelpers.testDownloadCacheMatchesGroundTruthFiles;


/**
 * Tests local and remove web crawling.
 */
public class LocalImageCrawlerTest {
    private static final String GROUND_TRUTH_DIR_NAME = "ground-truth";

    /**
     * Runs image crawler on the local files from the resources using a
     * NULL_FILTER and then tests the resulting download cache to see if
     * all the downloaded images match the files in the ground-truth
     * directory. The actual testing is performed by the helper class
     * helper/testDownloadCacheMatchesGroundTruthFiles.
     */
    @Test
    public void localImageCrawlerTest() throws Exception {
        testDownloadCacheMatchesGroundTruthFiles(
                GROUND_TRUTH_DIR_NAME, true);
    }
}


package edu.vanderbilt.imagecrawler.crawlers

import edu.vanderbilt.imagecrawler.helpers.AdminHelpers
import edu.vanderbilt.imagecrawler.helpers.DefaultController
import edu.vanderbilt.imagecrawler.helpers.Directories
import org.junit.Test

class Assignment3TestsParallelStreamsCrawler1Local {
    /**
     * Test local crawl which does not require an internet connection.
     * The downloaded images are compared to the images in the project's
     * ground-truth directory.
     */
    @Test
    @Throws(Exception::class)
    fun parallelStreamsCrawler1LocalTest() {
        val controller = DefaultController.build(true)

        // Perform the local crawl using the completable futures crawler.
        AdminHelpers.downloadIntoDirectory(
                ImageCrawler.Type.COMPLETABLE_FUTURES1,
                controller,
                false)

        // Compare the download cache with the contents of ground-truth directory.
        AdminHelpers.recursivelyCompareDirectories(
                Directories.getJavaGroundTruthDir(),
                controller.cacheDir
        )
    }
}
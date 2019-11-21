package admin

import admin.AdminHelpers.recursivelyCompareDirectories
import admin.Directories.getJavaGroundTruthDir
import edu.vanderbilt.imagecrawler.crawlers.CrawlerType

/**
 * OPTIONAL Local crawl test (no internet connection required).
 */
object CrawlTest {
    /**
     * Test local crawl which does not require an internet connection.
     * The downloaded images are compared to the images in the project's
     * ground-truth directory.
     */
    @Throws(Exception::class)
    fun localCrawlTest(crawlerType: CrawlerType) {
        val controller = BuildController.build(true)

        // Perform the local crawl using the completable futures crawler.
        AdminHelpers.downloadIntoDirectory(
                crawlerType,
                controller,
                false)

        // Compare the download cache with the contents of ground-truth directory.
        recursivelyCompareDirectories(
                getJavaGroundTruthDir(),
                controller.cacheDir
        )
    }
}

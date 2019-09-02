package assignment1bTests.optionalTests

import admin.CrawlTest
import edu.vanderbilt.imagecrawler.crawlers.CrawlerType
import org.junit.Test

/**
 * OPTIONAL test for this assignment.
 */
class OptionalTests {
    @Test
    fun optionalTest() {
        CrawlTest.localCrawlTest(CrawlerType.SEQUENTIAL_LOOPS)
    }
}

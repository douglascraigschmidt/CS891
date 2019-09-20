package assignment2aTests.optionalTests

import admin.CrawlTest
import edu.vanderbilt.imagecrawler.crawlers.CrawlerType
import org.junit.Ignore
import org.junit.Test

/**
 * OPTIONAL test for this assignment.
 */
@Ignore
class OptionalTests {
    @Test
    fun optionalTest() {
        CrawlTest.localCrawlTest(CrawlerType.SEQUENTIAL_STREAMS)
    }
}

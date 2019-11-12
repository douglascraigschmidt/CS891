package assignmentTests.optionalTests

import admin.CrawlTest
import edu.vanderbilt.imagecrawler.crawlers.CrawlerType
import org.junit.Ignore
import org.junit.Test

/**
 * OPTIONAL test for this assignment.
 */
@Ignore
class ForkJoin2LocalCrawlTest {
    @Test
    fun optionalTest() {
        CrawlTest.localCrawlTest(CrawlerType.FORK_JOIN2)
    }
}

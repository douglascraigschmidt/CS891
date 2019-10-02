package assignment2bTests.requiredTests

import edu.vanderbilt.imagecrawler.crawlers.ParallelStreamsCrawler1Tests
import edu.vanderbilt.imagecrawler.crawlers.SequentialStreamsCrawlerTests
import edu.vanderbilt.imagecrawler.utils.*
import kotlinx.coroutines.InternalCoroutinesApi
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses

/**
 * REQUIRED test suite for this assignment.
 */
@InternalCoroutinesApi
@RunWith(Suite::class)
@SuiteClasses(
        /** NEW: 2b tests for all students */
        ParallelStreamsCrawler1Tests::class,
        /** NEW: grads use SynchronizedArray, undergrads use UnsynchronizedArray) */
        ArrayCollectorTests::class)
/** NEW: grads only */
class RequiredTests
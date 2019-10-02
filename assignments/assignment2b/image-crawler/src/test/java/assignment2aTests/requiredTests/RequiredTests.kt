package assignment2aTests.requiredTests

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
        /** CHANGED: all students (was grads only) */
        UnsynchronizedArraySpliteratorTests::class,
        /** NEW: all students */
        SequentialStreamsCrawlerTests::class,
        /** NEW: grads only */
        SynchronizedArrayTestsForGradsOnly::class)
class RequiredTests

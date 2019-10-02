package edu.vanderbilt.imagecrawler.crawlers

import admin.AssignmentTestRule
import edu.vanderbilt.imagecrawler.transforms.Transform
import edu.vanderbilt.imagecrawler.utils.*
import edu.vanderbilt.imagecrawler.utils.Crawler.Type.IMAGE
import edu.vanderbilt.imagecrawler.utils.Crawler.Type.PAGE
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit
import java.net.URL
import java.util.*
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ParallelStreamsCrawler1Tests {
    // Required to allow any() to work with mockito calls
    // to prevent any() can't be null Kotlin error messages.
    private fun <T> any(): T = Mockito.any<T>()

    @Rule
    @JvmField
    var assignmentTestRule = AssignmentTestRule()

    @Mock
    lateinit var mockElements: edu.vanderbilt.imagecrawler.utils.Array<WebPageElement>

    @Mock
    lateinit var mockUniqueUris: ConcurrentHashSet<String>

    @Mock
    lateinit var mockImageStream: Stream<Image>

    @Mock
    lateinit var mockTransforms: List<Transform>

    @Mock
    lateinit var mockCrawler: ParallelStreamsCrawler1

    @Rule
    @JvmField
    var mockitoRule = MockitoJUnit.rule()!!

    @Test
    fun `crawPage() with 0 pages and 10 images and no failures`() {
        crawlPage(0, 10)
    }

    @Test
    fun `crawPage() with 10 pages and 0 images and no failures`() {
        crawlPage(10, 0)
    }

    @Test
    fun `crawPage() with 10 to 100 pages and 10 to 100 images with no failures`() {
        val random = Random()
        repeat(10) {
            crawlPage(10 + random.nextInt(90), 10 + random.nextInt(90))
            resetAll()
        }
    }

    @Test
    fun `crawPage() with 10 to 100 pages and 10 to 100 images with random failures`() {
        val random = Random()
        repeat(10) {
            crawlPage(10 + random.nextInt(90),
                    10 + random.nextInt(90),
                    failures = true)
            resetAll()
        }
    }

    @Test
    fun `processImages() with 1 to 10 images and 0 failures`() {
        val random = Random()
        repeat(5) {
            processImageTest(random.nextInt(10), 0)
            resetAll()
        }
    }

    @Test
    fun `processImages() with 1 to 10 images and 1 to 10 failures`() {
        val random = Random()
        repeat(10) {
            processImageTest(random.nextInt(10), random.nextInt(10))
            resetAll()
        }
    }

    private fun resetAll() {
        reset(mockCrawler)
        reset(mockImageStream)
        reset(mockElements)
        reset(mockTransforms)
        reset(mockUniqueUris)
    }

    private fun crawlPage(pages: Int, images: Int, failures: Boolean = false) {
        /******* TEST SETUP ************/
        val rootUrl = "/root"
        val imageRet = 1
        val startDepth = 777
        val maxDepth = Int.MAX_VALUE
        var processImageCount = 0
        var expected = 0

        val mockPage = mock(Crawler.Page::class.java)
        val mockWebPageCrawler = mock(WebPageCrawler::class.java)

        val pageRoot = "http://www/PAGE/"
        val pageElements = (1..pages).map {
            WebPageElement("$pageRoot$it", PAGE)
        }.shuffled()

        val expectedPageUrls = pageElements.map { it.getUrl() }.toMutableList()

        val imageRoot = "http://www/IMAGE/"
        val imageElements = (1..images).map {
            WebPageElement("$imageRoot$it", IMAGE)
        }.shuffled()

        val elements = (pageElements + imageElements).shuffled()

        doNothing().`when`(mockCrawler).log(anyString())
        `when`(mockPage.getPageElements(any(), any())).thenAnswer {
            assertNotEquals(it.arguments[0], it.arguments[1])
            mockElements
        }
        `when`(mockWebPageCrawler.getPage(rootUrl)).thenReturn(mockPage)
        `when`(mockElements.parallelStream()).thenReturn(elements.parallelStream())
        `when`(mockCrawler.crawlPage(rootUrl, startDepth)).thenCallRealMethod()
        if (pages > 0) {
            `when`(mockCrawler.performCrawl(anyString(), anyInt())).thenAnswer {
                assertEquals(startDepth + 1, it.getArgument(1))
                synchronized(expectedPageUrls) {
                    assertTrue(expectedPageUrls.remove(it.getArgument<String>(0)))
                }
                0
            }
        }

        if (images > 0) {
            `when`(mockCrawler.processImage(any())).thenAnswer {
                ++processImageCount
                if (!failures || processImageCount.rem(2) == 0) {
                    expected++
                    1
                } else {
                    0
                }
            }
        }

        mockCrawler.mWebPageCrawler = mockWebPageCrawler
        mockCrawler.mMaxDepth = maxDepth
        mockCrawler.mUniqueUris = mockUniqueUris

        /******* TEST CALL ************/

        val count = mockCrawler.crawlPage(rootUrl, startDepth)

        /******* TEST EVALUATION ************/

        assertEquals(expected, count)
        assertTrue(expectedPageUrls.isEmpty())
        verify(mockPage, times(1)).getPageElements(any(), any())
        verify(mockWebPageCrawler, times(1)).getPage(anyString())
        verify(mockCrawler, times(images * imageRet)).processImage(any())
        verify(mockCrawler, times(pages)).performCrawl(anyString(), anyInt())
    }

    private fun processImageTest(transforms: Int, failures: Int) {
        val imageUrl = "http://www.mock.com/image"
        val url = URL(imageUrl)
        val mockImage = mock(Image::class.java)
        val mockTransform = mock(Transform::class.java)
        var transformCount = 0

        val transformList = mutableListOf<Transform>()
        repeat(transforms + failures) {
            transformList.add(mockTransform)
        }

        mockCrawler.mTransforms = mockTransforms

        doCallRealMethod().`when`(mockCrawler).processImage(url)
        `when`(mockTransforms.parallelStream()).thenReturn(transformList.stream())
        `when`(mockCrawler.getOrDownloadImage(url)).thenReturn(mockImage)
        `when`(mockCrawler.createNewCacheItem(any(), any()))
                .thenReturn(true)
        `when`(mockCrawler.applyTransform(any(), any()))
                .thenAnswer {
                    if (++transformCount <= transforms) {
                        mockImage
                    } else {
                        null
                    }
                }
        doNothing().`when`(mockCrawler).log(anyString())

        /******* TEST CALL ************/

        val count = mockCrawler.processImage(url)

        /******* TEST EVALUATION ************/

        assertEquals(transforms, count)

        verify(mockTransforms, times(1)).parallelStream()
        verify(mockCrawler, times(1)).getOrDownloadImage(url)
        verify(mockCrawler, times(transformList.size)).createNewCacheItem(any(), any())
        verify(mockCrawler, times(transformList.size)).applyTransform(any(), any())
    }
}
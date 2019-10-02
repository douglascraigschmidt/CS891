package edu.vanderbilt.imagecrawler.crawlers

import admin.AssignmentTests
import admin.setField
import edu.vanderbilt.imagecrawler.transforms.Transform
import edu.vanderbilt.imagecrawler.utils.*
import edu.vanderbilt.imagecrawler.utils.Crawler.Type.IMAGE
import edu.vanderbilt.imagecrawler.utils.Crawler.Type.PAGE
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit
import java.net.URL
import java.util.*
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

class SequentialStreamsCrawlerTests : AssignmentTests() {
    @Mock
    lateinit var mockElements: edu.vanderbilt.imagecrawler.utils.Array<WebPageElement>

    @Mock
    lateinit var mockUniqueUris: ConcurrentHashSet<String>

    @Mock
    lateinit var mockImageStream: Stream<Image>

    @Mock
    lateinit var mockTransforms: List<Transform>

    @Mock
    lateinit var mockWebPageCrawler: WebPageCrawler

    @Mock
    lateinit var mockCrawler: SequentialStreamsCrawler

    @Mock
    lateinit var mockPage: Crawler.Page

    @Rule
    @JvmField
    var mockitoRule = MockitoJUnit.rule()!!

    @Test
    fun crawPageTest1() {
        crawlPage(0, 10)
    }

    @Test
    fun crawPageTest2() {
        crawlPage(10, 0)
    }

    @Test
    fun crawPageTest3() {
        crawlPage(10, 10)
    }

    @Test
    fun crawPageWithFailuresTest4() {
        val random = Random()
        repeat(10) {
            crawlPage(10 + random.nextInt(90),
                    10 + random.nextInt(90),
                    failures = true)
            resetAll()
        }
    }

    @Test
    fun processImageTest5() {
        val random = Random()
        repeat(5) {
            processImageTest(random.nextInt(10), 0)
            resetAll()
        }
    }

    @Test
    fun transformImageTest6() {
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
        reset(mockPage)
        reset(mockUniqueUris)
        reset(mockWebPageCrawler)
    }

    private fun crawlPage(pages: Int, images: Int, failures: Boolean = false) {
        /******* TEST SETUP ************/
        val rootUrl = "/root"
        val imageRet = 1
        val startDepth = 777
        val maxDepth = Int.MAX_VALUE
        var processImageCount = 0
        var expected = 0

        val pageElements = (1..pages).map {
            WebPageElement("http://www/PAGE/$it", PAGE)
        }.shuffled()

        val imageElements = (1..images).map {
            WebPageElement("http://www/IMAGE/$it", IMAGE)
        }.shuffled()

        val elements = (pageElements + imageElements).shuffled()

        mockCrawler.setField("mWebPageCrawler", mockWebPageCrawler)
        assertSame(mockWebPageCrawler, mockCrawler.mWebPageCrawler)

        doNothing().`when`(mockCrawler).log(anyString())
        `when`(mockCrawler.crawlPage(anyString(), anyInt())).thenCallRealMethod()
        `when`(mockWebPageCrawler.getPage(anyString())).thenReturn(mockPage)
        `when`(mockPage.getPageElements(any(), any())).thenAnswer {
            assertNotEquals(it.arguments[0], it.arguments[1])
            mockElements
        }
        `when`(mockElements.stream()).thenReturn(elements.stream())

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
        `when`(mockTransforms.stream()).thenReturn(transformList.stream())
        `when`(mockCrawler.getOrDownloadImage(url)).thenReturn(mockImage)
        `when`(mockCrawler.createNewCacheItem(any(), any())).thenReturn(true)
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

        verify(mockTransforms, times(1)).stream()
        verify(mockCrawler, times(1)).getOrDownloadImage(url)
        verify(mockCrawler, times(transformList.size)).createNewCacheItem(any(), any())
        verify(mockCrawler, times(transformList.size)).applyTransform(any(), any())
    }
}
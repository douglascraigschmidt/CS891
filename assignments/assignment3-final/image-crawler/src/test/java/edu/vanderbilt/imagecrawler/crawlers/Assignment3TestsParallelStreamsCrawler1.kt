package edu.vanderbilt.imagecrawler.crawlers

import edu.vanderbilt.imagecrawler.utils.*
import edu.vanderbilt.imagecrawler.transforms.Transform
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit
import java.net.URL
import java.util.*
import java.util.stream.Stream
import kotlin.test.assertEquals

class Assignment3TestsParallelStreamsCrawler1 {
    @Mock
    lateinit var mockPageElements: edu.vanderbilt.imagecrawler.utils.Array<WebPageElement>

    @Mock
    lateinit var mockUniqueUris: ConcurrentHashSet<String>

    @Mock
    lateinit var mockImageStream: Stream<Image>

    @Mock
    lateinit var mockTransforms: List<Transform>

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
            reset(mockUniqueUris)
            reset(mockPageElements)
            crawlPageWithFailures(10 + random.nextInt(90), 10 + random.nextInt(90))
        }
    }

    @Test
    fun processImageTest5() {
        val random = Random()
        repeat(5) {
            reset(mockImageStream)
            processImageTest(random.nextInt(10))
        }
    }

    private fun crawlPage(pages: Int, images: Int) {
        /******* TEST SETUP ************/
        val rootUrl = "/root"
        val imageRet = 1
        val startDepth = 777
        val maxDepth = Int.MAX_VALUE

        val pageElements = (1..pages).map {
            WebPageElement("http://www/PAGE/$it", Crawler.Type.PAGE)
        }.toMutableList()
        val imageElements = (1..images).map {
            WebPageElement("http://www/IMAGE/$it", Crawler.Type.IMAGE)
        }.toMutableList()
        val elements = (imageElements + pageElements).shuffled().toMutableList()
        `when`(mockPageElements.parallelStream()).thenReturn(elements.parallelStream())

        val mockPage = mock(Crawler.Page::class.java)
        val mockWebPageCrawler = mock(WebPageCrawler::class.java)

        `when`(mockWebPageCrawler.getPage(rootUrl)).thenReturn(mockPage)
        `when`(mockPage.getPageElements(Crawler.Type.IMAGE, Crawler.Type.PAGE))
                .thenReturn(mockPageElements)

        val mockCrawler = mock(ParallelStreamsCrawler1::class.java)
        `when`(mockCrawler.crawlPage(rootUrl, startDepth)).thenCallRealMethod()
        if (images > 0) {
            `when`(mockCrawler.processImage(anyString())).thenReturn(imageRet)
        }
        doNothing().`when`(mockCrawler).log(anyString())

        mockCrawler.mWebPageCrawler = mockWebPageCrawler
        mockCrawler.mMaxDepth = maxDepth
        mockCrawler.mUniqueUris = mockUniqueUris
        `when`(mockUniqueUris.putIfAbsent(anyString())).thenReturn(true)

        /******* TEST CALL ************/

        val count = mockCrawler.crawlPage(rootUrl, startDepth)

        /******* TEST EVALUATION ************/

        assertEquals(imageRet * images, count)

        verify(mockWebPageCrawler, times(1)).getPage(anyString())
        verify(mockPage, times(1)).getPageElements(Crawler.Type.IMAGE, Crawler.Type.PAGE)
        verify(mockCrawler, times(images * imageRet)).processImage(anyString())
        verify(mockCrawler, times(pages + 1)).crawlPage(anyString(), anyInt())
    }

    private fun crawlPageWithFailures(pages: Int, images: Int) {
        /******* TEST SETUP ************/
        val rootUrl = "/root"
        val imageRet = 1
        val startDepth = 777
        val maxDepth = Int.MAX_VALUE
        var processImageCount = 0
        var expected = 0

        val pageElements = (1..pages).map {
            WebPageElement("http://www/PAGE/$it", Crawler.Type.PAGE)
        }.toMutableList()
        val imageElements = (1..images).map {
            WebPageElement("http://www/IMAGE/$it", Crawler.Type.IMAGE)
        }.toMutableList()
        val elements = (imageElements + pageElements).shuffled().toMutableList()
        `when`(mockPageElements.parallelStream()).thenReturn(elements.parallelStream())

        val mockPage = mock(Crawler.Page::class.java)
        val mockWebPageCrawler = mock(WebPageCrawler::class.java)

        `when`(mockWebPageCrawler.getPage(rootUrl)).thenReturn(mockPage)
        `when`(mockPage.getPageElements(Crawler.Type.IMAGE, Crawler.Type.PAGE))
                .thenReturn(mockPageElements)

        val mockCrawler = mock(ParallelStreamsCrawler1::class.java)
        `when`(mockCrawler.crawlPage(rootUrl, startDepth)).thenCallRealMethod()
        `when`(mockCrawler.processImage(anyString())).thenAnswer {
            ++processImageCount
            if (processImageCount.rem(2) == 0) {
                expected++
                1
            } else {
                0
            }
        }
        doNothing().`when`(mockCrawler).log(anyString())

        mockCrawler.mWebPageCrawler = mockWebPageCrawler
        mockCrawler.mMaxDepth = maxDepth
        mockCrawler.mUniqueUris = mockUniqueUris
        `when`(mockUniqueUris.putIfAbsent(anyString())).thenReturn(true)

        /******* TEST CALL ************/

        val count = mockCrawler.crawlPage(rootUrl, startDepth)

        /******* TEST EVALUATION ************/

        assertEquals(expected, count)

        verify(mockWebPageCrawler, times(1)).getPage(anyString())
        verify(mockPage, times(1)).getPageElements(Crawler.Type.IMAGE, Crawler.Type.PAGE)
        verify(mockCrawler, times(images * imageRet)).processImage(anyString())
        verify(mockCrawler, times(pages + 1)).crawlPage(anyString(), anyInt())
    }

    private fun processImageTest(transforms: Int) {
        val imageUrl = "http://www.mock.com/image"
        val url = URL(imageUrl)
        val mockImage = mock(Image::class.java)

        val mockCrawler = mock(ParallelStreamsCrawler1::class.java)
        `when`(mockCrawler.processImage(imageUrl)).thenCallRealMethod()
        `when`(mockCrawler.getOrDownloadImage(url)).thenReturn(mockImage)
        `when`(mockCrawler.transformImage(mockImage)).thenReturn(mockImageStream)
        `when`(mockImageStream.count()).thenReturn(transforms.toLong())

        /******* TEST CALL ************/

        val count = mockCrawler.processImage(imageUrl)

        /******* TEST EVALUATION ************/

        assertEquals(transforms, count)

        verify(mockCrawler, times(1)).getOrDownloadImage(url)
        verify(mockCrawler, times(1)).transformImage(mockImage)
        verify(mockImageStream, times(1)).count()
    }

    @Test
    fun transformImageTest6() {
        val random = Random()
        repeat(10) {
            transformImageTest(random.nextInt(10), random.nextInt(10))
        }
    }

    private fun transformImageTest(transforms: Int, failures: Int) {
        val mockImage = mock(Image::class.java)
        val mockCrawler = mock(ParallelStreamsCrawler1::class.java)
        val mockTransform = mock(Transform::class.java)
        var transformCount = 0

        val transformList = mutableListOf<Transform>()
        repeat(transforms + failures) {
            transformList.add(mockTransform)
        }
        mockCrawler.mTransforms = transformList

        doCallRealMethod().`when`(mockCrawler).transformImage(mockImage)
        `when`(mockCrawler.createNewCacheItem(any(Image::class.java), any(Transform::class.java)))
                .thenReturn(true)
        `when`(mockCrawler.applyTransform(any(Transform::class.java), any(Image::class.java)))
                .thenAnswer {
                    if (++transformCount <= transforms) {
                        mockImage
                    } else {
                        null
                    }
                }

        /******* TEST CALL ************/

        val imageStream = mockCrawler.transformImage(mockImage)

        /******* TEST EVALUATION ************/

        assertEquals(transforms.toLong(), imageStream.count())

        verify(mockCrawler, times(transformList.size)).createNewCacheItem(any(Image::class.java), any(Transform::class.java))
        verify(mockCrawler, times(transformList.size)).applyTransform(any(Transform::class.java),any(Image::class.java))
    }
}
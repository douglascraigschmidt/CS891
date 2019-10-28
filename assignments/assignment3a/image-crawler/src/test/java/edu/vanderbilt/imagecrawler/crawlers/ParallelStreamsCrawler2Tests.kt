package edu.vanderbilt.imagecrawler.crawlers

import admin.AssignmentTests
import edu.vanderbilt.imagecrawler.transforms.Transform
import edu.vanderbilt.imagecrawler.utils.*
import edu.vanderbilt.imagecrawler.utils.Crawler.Type.IMAGE
import edu.vanderbilt.imagecrawler.utils.Crawler.Type.PAGE
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.*
import java.net.URL
import java.util.*
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.IntStream
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.fail

class ParallelStreamsCrawler2Tests : AssignmentTests() {
    @Mock
    lateinit var mockElementsAsStrings: edu.vanderbilt.imagecrawler.utils.Array<String>

    @Mock
    lateinit var mockImagesOnPage: edu.vanderbilt.imagecrawler.utils.Array<URL>

    @Mock
    lateinit var mockUrlArray: edu.vanderbilt.imagecrawler.utils.Array<URL>

    @Mock
    lateinit var mockElements: edu.vanderbilt.imagecrawler.utils.Array<WebPageElement>

    @Mock
    lateinit var mockUniqueUris: ConcurrentHashSet<String>

    @Mock
    lateinit var mockStringStream: Stream<String>

    @Mock
    lateinit var mockImageStream: Stream<Image?>

    @Mock
    lateinit var mockURLStream: Stream<URL>

    @Mock
    lateinit var mockIntStream: IntStream

    @Mock
    lateinit var mockTransforms: List<Transform>

    @Mock
    lateinit var mockCrawler: ParallelStreamsCrawler2

    @Mock
    lateinit var mockWebPageCrawler: WebPageCrawler

    @Mock
    lateinit var mockPage: Crawler.Page

    @Mock
    lateinit var mockProcessImageFunction: Function<Crawler.Page, Int>

    @Mock
    lateinit var mockProcessHyperLinkFunction: Function<Crawler.Page, Int>

    @Mock
    lateinit var mockFunctionStream: Stream<Function<Crawler.Page, Int>>

    @Mock
    lateinit var mockStreamOfTasks: Stream<Function<Crawler.Page, Int>>

    @Mock
    lateinit var mockTransformStream: Stream<Transform>

    private fun resetAll() {
        reset(mockElementsAsStrings)
        reset(mockImagesOnPage)
        reset(mockUrlArray)
        reset(mockElements)
        reset(mockUniqueUris)
        reset(mockStringStream)
        reset(mockImageStream)
        reset(mockURLStream)
        reset(mockIntStream)
        reset(mockTransforms)
        reset(mockCrawler)
        reset(mockWebPageCrawler)
        reset(mockPage)
        reset(mockProcessImageFunction)
        reset(mockProcessHyperLinkFunction)
        reset(mockFunctionStream)
        reset(mockStreamOfTasks)
        reset(mockTransformStream)
    }

    @Test
    fun `processImagesOnPage should get and process images on input page`() {
        `when`(mockCrawler.makeImagesOnPageFunction()).thenCallRealMethod()
        `when`(mockCrawler.getImagesOnPage(mockPage)).thenReturn(mockImagesOnPage)

        /******* TEST CALL ************/
        val processImagesOnPage = mockCrawler.makeImagesOnPageFunction()
        assertNotNull(processImagesOnPage)

        /******* TEST EVALUATION ************/
        val count = processImagesOnPage.apply(mockPage)
        verify(mockCrawler, times(1)).getImagesOnPage(mockPage)
        verify(mockCrawler, times(1)).processImages(mockImagesOnPage)
        assertEquals(0, count)
    }

    @Test
    fun `processHyperLinksOnPage should crawl hyperlinks on input page`() {
        val expected = Random().nextInt(933992)
        val depth = Random().nextInt(38394)
        `when`(mockCrawler.makeHyperLinksOnPageFunction(anyInt())).thenCallRealMethod()
        `when`(mockCrawler.crawlHyperLinksOnPage(mockPage, depth)).thenReturn(expected)

        /******* TEST CALL ************/
        val processHyperLinksOnPage = mockCrawler.makeHyperLinksOnPageFunction(depth)

        /******* TEST EVALUATION ************/
        assertNotNull(processHyperLinksOnPage)
        val count = processHyperLinksOnPage.apply(mockPage)
        verify(mockCrawler, times(1)).crawlHyperLinksOnPage(mockPage, depth)
        assertEquals(expected, count)
    }

    @Test
    fun `crawlHyperLinksOnPage() should implement expected Java method chain`() {
        val expected = Random().nextInt(933992)
        val depth = Random().nextInt(38394)

        `when`(mockCrawler.crawlHyperLinksOnPage(mockPage, depth)).thenCallRealMethod()
        `when`(mockPage.getPageElementsAsStrings(PAGE)).thenReturn(mockElementsAsStrings)
        `when`(mockElementsAsStrings.parallelStream()).thenReturn(mockStringStream)
        `when`(mockStringStream.mapToInt(any())).thenReturn(mockIntStream)
        `when`(mockIntStream.sum()).thenReturn(expected)

        /******* TEST CALL ************/
        val count = mockCrawler.crawlHyperLinksOnPage(mockPage, depth)

        /******* TEST EVALUATION ************/
        verify(mockPage, times(1)).getPageElementsAsStrings(PAGE)
        verify(mockElementsAsStrings, times(1)).parallelStream()
        verify(mockStringStream, times(1)).mapToInt(any())
        verify(mockIntStream, times(1)).sum()
        assertEquals(expected, count)
    }

    @Test
    fun `CrawlPage should implement expected Java method chain`() {
        val depth = 10
        val mockHyperlink = "mock url"
        val processedImages = 999

        mockCrawler.mWebPageCrawler = mockWebPageCrawler

        `when`(mockCrawler.crawlPage(eq(mockHyperlink), eq(depth))).thenCallRealMethod()
        `when`(mockWebPageCrawler.getPage(eq(mockHyperlink))).thenReturn(mockPage)
        `when`(mockCrawler.streamOfTasks(anyInt())).thenAnswer {
            assertTrue(it.arguments[0] == depth || it.arguments[0] == depth + 1)
            mockStreamOfTasks
        }
        `when`(mockStreamOfTasks.parallel()).thenReturn(mockFunctionStream)
        `when`(mockFunctionStream.mapToInt(any())).thenReturn(mockIntStream)
        `when`(mockIntStream.sum()).thenReturn(processedImages)

        /******* TEST CALL ************/
        val result = mockCrawler.crawlPage(mockHyperlink, depth)

        /******* TEST EVALUATION ************/
        verify(mockWebPageCrawler, times(1)).getPage(mockHyperlink)
        verify(mockCrawler, times(1)).streamOfTasks(anyInt())
        verify(mockStreamOfTasks, times(1)).parallel()
        verify(mockFunctionStream, times(1)).mapToInt(any())
        verify(mockIntStream, times(1)).sum()
        assertEquals(processedImages, result)
    }

    @Test
    fun `CrawlPage should call the expected two lambda functions`() {
        val depth = 10
        val mockHyperlink = "mock url"
        val processedImages = 999

        mockCrawler.mWebPageCrawler = mockWebPageCrawler

        `when`(mockCrawler.crawlPage(eq(mockHyperlink), anyInt())).thenCallRealMethod()
        `when`(mockCrawler.streamOfTasks(anyInt())).thenCallRealMethod()
        `when`(mockWebPageCrawler.getPage(eq(mockHyperlink))).thenReturn(mockPage)
        `when`(mockCrawler.makeImagesOnPageFunction()).thenCallRealMethod()
        `when`(mockCrawler.makeHyperLinksOnPageFunction(anyInt())).thenCallRealMethod()

        `when`(mockCrawler.getImagesOnPage(any())).thenReturn(mockUrlArray)
        `when`(mockCrawler.processImages(eq(mockUrlArray))).thenReturn(processedImages)
        `when`(mockCrawler.crawlHyperLinksOnPage(any(Crawler.Page::class.java), eq(depth + 1)))
                .thenReturn(processedImages)

        /******* TEST CALL ************/
        val result = mockCrawler.crawlPage(mockHyperlink, depth)

        /******* TEST EVALUATION ************/
        verify(mockCrawler, times(1)).crawlPage(eq(mockHyperlink), anyInt())
        verify(mockWebPageCrawler, times(1)).getPage(mockHyperlink)
        verify(mockCrawler, times(1)).streamOfTasks(anyInt())
        verify(mockCrawler, times(1)).makeImagesOnPageFunction()
        verify(mockCrawler, times(1)).makeHyperLinksOnPageFunction(eq(depth + 1))
        verify(mockCrawler, times(1)).processImages(mockUrlArray)
        verify(mockCrawler, times(1))
                .crawlHyperLinksOnPage(any(Crawler.Page::class.java), eq(depth + 1))
        assertEquals(processedImages * 2, result)
    }

    @Test
    fun `processImages() should implement expected Java method chain`() {
        val expected = 234 + Random().nextInt()

        `when`(mockCrawler.processImages(mockUrlArray)).thenCallRealMethod()
        `when`(mockUrlArray.parallelStream()).thenReturn(mockURLStream)
        `when`(mockURLStream.map(any<Function<URL, Image>>()))
                .thenReturn(mockImageStream)
        `when`(mockImageStream.filter(any<Predicate<Image?>>()))
                .thenReturn(mockImageStream)
        `when`(mockImageStream.flatMap(any<Function<Image?, Stream<Image>>>()))
                .thenReturn(mockImageStream)
        `when`(mockImageStream.count()).thenReturn(expected.toLong())

        /******* TEST CALL ************/
        val result = mockCrawler.processImages(mockUrlArray)

        /******* TEST EVALUATION ************/
        verify(mockUrlArray, times(1)).parallelStream()
        verify(mockURLStream, times(1))
                .map(any<Function<URL, Image>>())
        verify(mockImageStream, times(2))
                .filter(any<Predicate<Image?>>())
        verify(mockImageStream, times(1))
                .flatMap(any<Function<Image?, Stream<Image>>>())
        verify(mockImageStream, times(1)).count()
        assertEquals(expected, result)
    }

    @Test
    fun `processImages() should only process and count non-null images`() {
        val mockImage = mock(Image::class.java)
        val urls = 7
        val nullUrls = 3
        val transforms = 8
        val nullTransforms = 4
        val expected = urls * transforms

        val imageRoot = "http://www/IMAGE/"
        val imageElements = (1..(urls + nullUrls)).map {
            WebPageElement("$imageRoot$it", IMAGE)
        }.shuffled()
        val urlArray = UnsynchronizedArray(imageElements.map { it.getURL() })
        val urlCount = urlArray.size()

        `when`(mockCrawler.processImages(urlArray)).thenCallRealMethod()
        `when`(mockCrawler.transformImage(any())).thenAnswer {
            val mockArray = mutableListOf<Image?>()
            repeat(transforms) { mockArray.add(mockImage) }
            repeat(nullTransforms) { mockArray.add(null) }
            val stream = with(mockArray) {
                shuffle()
                stream()
            }
            stream
        }

        val mockImages = mutableListOf<Image?>()
        repeat(urls) { mockImages.add(mockImage) }
        repeat(nullUrls) { mockImages.add(null) }
        val maxDownloadCalls = mockImages.size
        mockImages.shuffle()
        `when`(mockCrawler.getOrDownloadImage(any())).thenAnswer {
            if (mockImages.isEmpty()) {
                fail("getOrDownloadImages() should only been called $maxDownloadCalls times.")
            }
            mockImages.removeAt(0)
        }

        /******* TEST CALL ************/
        val result = mockCrawler.processImages(urlArray)

        /******* TEST EVALUATION ************/
        verify(mockCrawler, times(urlCount)).getOrDownloadImage(any())
        verify(mockCrawler, times(urls)).transformImage(any())
        assertEquals(expected, result)
    }

    @Test
    fun `transformImage() should implement expected Java method chain`() {
        val mockImage = mock(Image::class.java)

        mockCrawler.mTransforms = mockTransforms

        `when`(mockCrawler.transformImage(mockImage)).thenCallRealMethod()
        `when`(mockTransforms.parallelStream()).thenReturn(mockTransformStream)
        `when`(mockTransformStream.filter(any<Predicate<Transform>>()))
                .thenReturn(mockTransformStream)
        `when`(mockTransformStream.map(any<Function<Transform, Image>>()))
                .thenReturn(mockImageStream)
        `when`(mockImageStream.filter(any<Predicate<Image?>>()))
                .thenReturn(mockImageStream)

        /******* TEST CALL ************/
        val result = mockCrawler.transformImage(mockImage)

        /******* TEST EVALUATION ************/
        verify(mockTransforms, times(1)).parallelStream()
        verify(mockTransformStream, times(1))
                .filter(any<Predicate<Transform>>())
        verify(mockTransformStream, times(1))
                .map(any<Function<Transform, Image>>())
        verify(mockImageStream, times(1))
                .filter(any<Predicate<Image?>>())

        assertSame(mockImageStream, result)
    }

    @Test
    fun `CrawlPage must handle when getPage() returns a null value`() {
        val url = "null page"
        val depth = 100
        mockCrawler.mWebPageCrawler = mockWebPageCrawler
        `when`(mockCrawler.crawlPage(url, 100)).thenCallRealMethod()
        `when`(mockWebPageCrawler.getPage(url)).thenReturn(null)

        /******* TEST CALL ************/
        assertEquals(0, mockCrawler.crawlPage(url, depth))

        verify(mockWebPageCrawler, times(1)).getPage(url)
        verify(mockCrawler, times(0)).streamOfTasks(anyInt())
    }

    @Test
    fun `crawlPage() should call function lambdas`() {
        val mockUrl = "http://www.mock.url/PAGE"
        val depth = 9
        val random = Random()
        val expectedProcessedImages = 10 + random.nextInt(100)
        val expectedCrawledImages = 120 + random.nextInt(200)
        val expected = expectedProcessedImages + expectedCrawledImages

        mockCrawler.mWebPageCrawler = mockWebPageCrawler
        doNothing().`when`(mockCrawler).log(anyString())
        `when`(mockWebPageCrawler.getPage(eq(mockUrl))).thenReturn(mockPage)
        `when`(mockCrawler.streamOfTasks(anyInt())).thenCallRealMethod()
        `when`(mockCrawler.crawlPage(eq(mockUrl), eq(depth))).thenCallRealMethod()
        `when`(mockCrawler.makeImagesOnPageFunction()).thenCallRealMethod()
        `when`(mockCrawler.makeHyperLinksOnPageFunction(anyInt())).thenCallRealMethod()
        `when`(mockCrawler
                .processImages(any<edu.vanderbilt.imagecrawler.utils.Array<URL>>()))
                .thenAnswer {
                    expectedProcessedImages
                }
        `when`(mockCrawler
                .crawlHyperLinksOnPage(any(Crawler.Page::class.java), anyInt()))
                .thenAnswer {
                    expectedCrawledImages
                }

        /******* TEST CALL ************/
        val result = mockCrawler.crawlPage(mockUrl, depth)

        /******* TEST EVALUATION ************/
        verify(mockCrawler, times(1)).makeImagesOnPageFunction()
        verify(mockCrawler, times(1)).makeHyperLinksOnPageFunction(anyInt())
        verify(mockWebPageCrawler, times(1)).getPage(mockUrl)
        verify(mockCrawler, times(1))
                .processImages(any<edu.vanderbilt.imagecrawler.utils.Array<URL>>())
        verify(mockCrawler, times(1)).processImages(any())
        verify(mockCrawler, times(1))
                .crawlHyperLinksOnPage(any(Crawler.Page::class.java), anyInt())
        assertEquals(expected, result)
    }

    @Test
    fun `streamOfTasks() should return a stream of the 2 expected lambdas`() {
        val depth = 10
        val expectedImagesOnPage = 13234
        val expectedImagesOnHyperlinks = 89232
        val expectedResult: Int = expectedImagesOnPage + expectedImagesOnHyperlinks

        `when`(mockCrawler.streamOfTasks(depth)).thenCallRealMethod()
        `when`(mockCrawler.makeHyperLinksOnPageFunction(anyInt())).thenCallRealMethod()
        `when`(mockCrawler.makeImagesOnPageFunction()).thenCallRealMethod()
        `when`(mockCrawler.getImagesOnPage(mockPage)).thenReturn(mockUrlArray)
        `when`(mockCrawler.processImages(mockUrlArray)).thenReturn(expectedImagesOnPage)
        // 2nd parm could be called with either depth or depth + 1 ... this choice is
        // implementation specific so it can't be tested for here (it's tested for in
        // other tests).
        `when`(mockCrawler.crawlHyperLinksOnPage(eq(mockPage), anyInt()))
                .thenReturn(expectedImagesOnHyperlinks)

        /******* TEST CALL ************/
        val streamResult = mockCrawler.streamOfTasks(depth)

        var processResult = 0
        streamResult.forEach {
            processResult += it.apply(mockPage)
        }

        /******* TEST EVALUATION ************/
        assertThat(processResult, `is`(expectedResult))
        verify(mockCrawler, times(1)).makeHyperLinksOnPageFunction(anyInt())
        verify(mockCrawler, times(1)).makeImagesOnPageFunction()
        verify(mockCrawler, times(1)).processImages(mockUrlArray)
        verify(mockCrawler, times(1)).crawlHyperLinksOnPage(eq(mockPage), anyInt())
    }

    @Test
    fun `CrawlPage must call streamOfTasks`() {
        val depth = 10
        val url = "test url"
        `when`(mockCrawler.crawlPage(anyString(), anyInt())).thenCallRealMethod()
        `when`(mockWebPageCrawler.getPage(anyString())).thenReturn(mockPage)

        `when`(mockCrawler.streamOfTasks(anyInt())).thenAnswer {
            assertTrue(it.arguments[0] == depth || it.arguments[0] == depth + 1)
            mockStreamOfTasks
        }
        `when`(mockStreamOfTasks.parallel()).thenReturn(mockStreamOfTasks)
        mockCrawler.mWebPageCrawler = mockWebPageCrawler

        /******* TEST CALL ************/
        assertEquals(0, mockCrawler.crawlPage(url, depth))

        /******* TEST EVALUATION ************/
        verify(mockCrawler, times(1)).streamOfTasks(anyInt())
        verify(mockWebPageCrawler, times(1)).getPage(url)
    }
}

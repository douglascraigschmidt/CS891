package edu.vanderbilt.imagecrawler.crawlers

import admin.AssignmentTests
import admin.getField
import edu.vanderbilt.imagecrawler.transforms.Transform
import edu.vanderbilt.imagecrawler.utils.Crawler
import edu.vanderbilt.imagecrawler.utils.Image
import edu.vanderbilt.imagecrawler.utils.WebPageCrawler
import org.junit.Assert.*
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.*
import java.net.URL
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.Function
import java.util.stream.Stream

class CompletableFuturesCrawler1Tests : AssignmentTests() {
    @Mock
    lateinit var mockCrawler: CompletableFuturesCrawler1

    @Mock
    lateinit var mockWebPageCrawler: WebPageCrawler

    @Mock
    lateinit var mockPageFuture: CompletableFuture<Crawler.Page>

    @Mock
    lateinit var mockURLArrayFuture: CompletableFuture<edu.vanderbilt.imagecrawler.utils.Array<URL>>

    @Mock
    lateinit var mockIntFuture: CompletableFuture<Int>

    @Mock
    lateinit var mockPage: Crawler.Page

    @Test
    fun testMembersWhiteBox() {
        val crawler = CompletableFuturesCrawler1()
        val member: CompletableFuture<Int>? = crawler.getField("mZero")
        assertNotNull(member)
    }

    @Test
    fun performCrawlWhiteBox() {
        val uri = "https://www.no.where"
        val depth = Int.MAX_VALUE
        val imageCount = 10

        /******* TEST SETUP ************/

        `when`(mockCrawler.performCrawlAsync(anyString(), anyInt())).thenReturn(mockIntFuture)
        `when`(mockIntFuture.join()).thenReturn(imageCount)

        /******* TEST CALL ************/

        `when`(mockCrawler.performCrawl(anyString(), anyInt())).thenCallRealMethod()
        val result = mockCrawler.performCrawl(uri, depth)

        /******* TEST EVALUATION ************/

        assertEquals(imageCount, result)

        verify(mockCrawler, times(1)).performCrawlAsync(uri, depth)
        verify(mockIntFuture, times(1)).join()
    }

    @Test
    fun getPageAsyncWhiteBox() {
        val uri = "https://www.no.where"

        /******* TEST SETUP ************/

        mockCrawler.mWebPageCrawler = mockWebPageCrawler
        `when`(mockWebPageCrawler.getPage(anyString())).thenReturn(mockPage)

        /******* TEST CALL ************/

        `when`(mockCrawler.getPageAsync(anyString())).thenCallRealMethod()
        val resultFuture = mockCrawler.getPageAsync(uri)

        /******* TEST EVALUATION ************/

        assertNotNull(resultFuture)

        val page = resultFuture.get(10, TimeUnit.SECONDS)
        assertNotNull(page)
        assertSame(mockPage, page)

        verify(mockWebPageCrawler, times(1)).getPage(uri)
    }

    @Test
    fun getImagesOnPageAsyncWhiteBox() {

        /******* TEST SETUP ************/

        mockCrawler.mWebPageCrawler = mockWebPageCrawler

        `when`(mockPageFuture
                .thenApplyAsync(
                        ArgumentMatchers.any<Function<Crawler.Page,
                                edu.vanderbilt.imagecrawler.utils.Array<URL>>>()))
                .thenReturn(mockURLArrayFuture)

        `when`(mockURLArrayFuture
                .thenComposeAsync(
                        ArgumentMatchers.any<Function<edu.vanderbilt.imagecrawler.utils.Array<URL>,
                                CompletableFuture<Int>>>()))
                .thenReturn(mockIntFuture)


        /******* TEST CALL ************/

        `when`(mockCrawler.getImagesOnPageAsync(any())).thenCallRealMethod()
        val result = mockCrawler.getImagesOnPageAsync(mockPageFuture)

        /******* TEST EVALUATION ************/

        assertNotNull(result)
        assertSame(mockIntFuture, result)

        verify(mockPageFuture, times(1)).thenApplyAsync(
                ArgumentMatchers.any<Function<Crawler.Page,
                        edu.vanderbilt.imagecrawler.utils.Array<URL>>>())

        verify(mockURLArrayFuture, times(1)).thenComposeAsync(
                ArgumentMatchers.any<
                        Function<edu.vanderbilt.imagecrawler.utils.Array<URL>,
                                CompletableFuture<Int>>>())
    }

    @Test
    fun getImagesOnPageAsyncBlackBox() {
        val expectedIntResult = 10

        /******* TEST SETUP ************/

        mockCrawler.mWebPageCrawler = mockWebPageCrawler

        val pageFuture = CompletableFuture.completedFuture(mockPage)

        `when`(mockCrawler.getImagesOnPage(any())).thenAnswer {
            edu.vanderbilt.imagecrawler.utils.UnsynchronizedArray<URL>()
        }

        `when`(mockCrawler.processImages(any())).thenAnswer {
            val result = CompletableFuture<Int>()
            result.complete(expectedIntResult)
            result
        }

        /******* TEST CALL ************/

        `when`(mockCrawler.getImagesOnPageAsync(any())).thenCallRealMethod()
        val futureResult = mockCrawler.getImagesOnPageAsync(pageFuture)

        /******* TEST EVALUATION ************/

        assertNotNull(futureResult)

        val getResult = futureResult.get(100, TimeUnit.MILLISECONDS)
        assertEquals(expectedIntResult, getResult)

        verify(mockCrawler, times(1)).getImagesOnPage(any(Crawler.Page::class.java))
        verify(mockCrawler, times(1))
                .processImages(ArgumentMatchers.any<
                        edu.vanderbilt.imagecrawler.utils.Array<URL>>())
    }

    @Test
    fun crawlHyperLinksOnPageAsyncWhiteBox() {
        val depth = Int.MAX_VALUE

        /******* TEST SETUP ************/

        `when`(mockPageFuture
                .thenComposeAsync(
                        ArgumentMatchers.any<
                                Function<Crawler.Page, CompletableFuture<Int>>>()))
                .thenReturn(mockIntFuture)

        /******* TEST CALL ************/

        `when`(mockCrawler
                .crawlHyperLinksOnPageAsync(mockPageFuture, depth))
                .thenCallRealMethod()

        val result = mockCrawler.crawlHyperLinksOnPageAsync(mockPageFuture, depth)

        /******* TEST EVALUATION ************/

        assertNotNull(result)
        assertSame(result, mockIntFuture)

        verify(mockPageFuture, times(1)).thenComposeAsync(
                ArgumentMatchers.any<Function<Crawler.Page, CompletableFuture<Int>>>())
    }

    @Test
    fun crawlHyperLinksOnPageAsyncBlackBox() {
        val depth = Int.MAX_VALUE
        val expectedIntResult = 10

        /******* TEST SETUP ************/

        val pageFuture = CompletableFuture.completedFuture(mockPage)

        `when`(mockCrawler.crawlHyperLinksOnPage(any(Crawler.Page::class.java), anyInt()))
                .thenAnswer {
                    val result = CompletableFuture<Int>()
                    result.complete(expectedIntResult)
                    result
                }

        /******* TEST CALL ************/

        `when`(mockCrawler
                .crawlHyperLinksOnPageAsync(pageFuture, depth))
                .thenCallRealMethod()

        val futureResult = mockCrawler.crawlHyperLinksOnPageAsync(pageFuture, depth)

        /******* TEST EVALUATION ************/

        assertNotNull(futureResult)

        val getResult = futureResult.get(100, TimeUnit.MILLISECONDS)
        assertEquals(expectedIntResult, getResult)

        verify(mockCrawler, times(1)).crawlHyperLinksOnPage(
                any(Crawler.Page::class.java), anyInt())
    }

    @Test
    fun combineResultsAsyncBlackBox() {
        val random = Random()

        /******* TEST SETUP ************/

        val imagesOnPage = 10 + random.nextInt(10)
        val imagesOnPageLinks = 10 + random.nextInt(10)
        val pageFuture = CompletableFuture<Int>()
        pageFuture.complete(imagesOnPage)
        val pageLinksFuture = CompletableFuture<Int>()
        pageLinksFuture.complete(imagesOnPageLinks)

        /******* TEST CALL ************/

        `when`(mockCrawler
                .combineResultsAsync(pageFuture, pageLinksFuture))
                .thenCallRealMethod()

        val futureResult = mockCrawler.combineResultsAsync(pageFuture, pageLinksFuture)

        /******* TEST EVALUATION ************/

        assertNotNull(futureResult)

        val intResult = futureResult.get(100, TimeUnit.MILLISECONDS)

        assertEquals(imagesOnPage + imagesOnPageLinks, intResult)
    }

    @Test
    fun crawlHyperLinksOnPageBlackBox() {
        val depth = Int.MAX_VALUE
        val hyperLinkCount = 10

        /******* TEST SETUP ************/

        // Calculate number of images separately, not in thenAnswer() callback below
        // because this calculated value is an invariant that should not depend on
        // the user code.
        var expectedImages = 0
        for (i in 1..hyperLinkCount) {
            expectedImages += i
        }

        `when`(mockPage.getPageElementsAsStrings(Crawler.Type.PAGE)).thenAnswer {
            val array = edu.vanderbilt.imagecrawler.utils.UnsynchronizedArray<String>()
            for (i in 1..hyperLinkCount) {
                array.add("$i")
            }
            array
        }

        `when`(mockCrawler.performCrawlAsync(anyString(), anyInt())).thenAnswer {
            val hyperLink = it.arguments[0] as String
            val intValue = hyperLink.toInt()
            val intFuture = CompletableFuture<Int>()
            intFuture.complete(intValue)
            intFuture
        }

        /******* TEST CALL ************/

        `when`(mockCrawler.crawlHyperLinksOnPage(any(Crawler.Page::class.java), anyInt()))
                .thenCallRealMethod()

        val futureResult = mockCrawler.crawlHyperLinksOnPage(mockPage, depth)

        /******* TEST EVALUATION ************/

        assertNotNull(futureResult)

        verify(mockCrawler, times(hyperLinkCount)).performCrawlAsync(anyString(), anyInt())

        val intResult = futureResult.get(100, TimeUnit.MILLISECONDS)
        assertEquals(expectedImages, intResult)
    }

    @Mock
    lateinit var mockImageFuture: CompletableFuture<Image>

    @Mock
    lateinit var mockImageStreamFuture: CompletableFuture<Stream<Image>>

    @Test
    fun processImagesBlackBox() {
        val urlCount = 10
        var urls = edu.vanderbilt.imagecrawler.utils.UnsynchronizedArray<URL>()

        /******* TEST SETUP ************/

        for (i in 1..urlCount) {
            urls.add(URL("https://test.com/$i"))
        }

        var images = mutableListOf<Image>()
        for (i in 1..urlCount) {
            images.add(mock(Image::class.java))
        }

        val mockImage = mock(Image::class.java)
        val imageFuture = CompletableFuture<Image>()
        imageFuture.complete(mockImage)

        `when`(mockCrawler.downloadAndStoreImageAsync(any(URL::class.java)))
                .thenReturn(imageFuture)

        `when`(mockCrawler.transformImageAsync(ArgumentMatchers.any<CompletableFuture<Image>>()))
                .thenAnswer {
                    val imageStreamFuture = CompletableFuture<Stream<Image>>()
                    imageStreamFuture.complete(images.stream())
                    imageStreamFuture
                }

        /******* TEST CALL ************/

        `when`(mockCrawler.processImages(ArgumentMatchers.any<
                edu.vanderbilt.imagecrawler.utils.Array<URL>>()))
                .thenCallRealMethod()

        val futureResult = mockCrawler.processImages(urls)

        /******* TEST EVALUATION ************/

        assertNotNull(futureResult)

        verify(mockCrawler, times(urlCount))
                .downloadAndStoreImageAsync(any(URL::class.java))

        verify(mockCrawler, times(urlCount))
                .transformImageAsync(ArgumentMatchers.any<CompletableFuture<Image>>())

        val intResult = futureResult.get(100, TimeUnit.MILLISECONDS)
        assertEquals(urlCount * urlCount, intResult)
    }

    @Test
    fun transformImageAsyncWhiteBox() {
        val transforms = 10

        /******* TEST SETUP ************/

        mockCrawler.mTransforms = buildMockTransformList(transforms)

        /******* TEST CALL ************/

        `when`(mockCrawler.transformImageAsync(ArgumentMatchers.any<CompletableFuture<Image>>()))
                .thenCallRealMethod()

        val futureResult = mockCrawler.transformImageAsync(mockImageFuture)

        /******* TEST EVALUATION ************/

        assertNull(futureResult)

        verify(mockImageFuture, times(1))
                .thenCompose(ArgumentMatchers.any<Function<Image, CompletableFuture<Image>>>())
    }

    @Test
    fun transformImageAsyncBlackBox() {
        val random = Random()
        val transforms = 10 + random.nextInt(10)

        /******* TEST SETUP ************/

        val transformArray = mutableListOf<Boolean>()

        (1..transforms).forEach {
            transformArray.add(random.nextBoolean())
        }

        val imageCount = transformArray.filter { it }.count()

        mockCrawler.mTransforms = buildMockTransformList(transforms)

        val mockImage = mock(Image::class.java)
        val imageFuture = CompletableFuture<Image>()
        imageFuture.complete(mockImage)

        var index = 0
        `when`(mockCrawler.createNewCacheItem(any(Image::class.java), any(Transform::class.java)))
                .thenAnswer {
                    transformArray[index++]
                }

        `when`(mockCrawler.applyTransformAsync(any(Transform::class.java), any(Image::class.java)))
                .thenAnswer {
                    val transformedImageFuture = CompletableFuture<Image>()
                    transformedImageFuture.complete(mockImage)
                    transformedImageFuture
                }

        /******* TEST CALL ************/

        `when`(mockCrawler.transformImageAsync(ArgumentMatchers.any<CompletableFuture<Image>>()))
                .thenCallRealMethod()

        val futureResult = mockCrawler.transformImageAsync(imageFuture)

        /******* TEST EVALUATION ************/

        assertNotNull(futureResult)

        val imageStream = futureResult.get(100, TimeUnit.MILLISECONDS)

        val intResult = imageStream.count()
        assertEquals(imageCount.toLong(), intResult)

        verify(mockCrawler, times(transforms))
                .createNewCacheItem(any(Image::class.java), any(Transform::class.java))

        verify(mockCrawler, times(imageCount))
                .applyTransformAsync(any(Transform::class.java), any(Image::class.java))
    }

    private fun buildMockTransformList(transforms: Int): MutableList<Transform> {
        val mockTransform = mock(Transform::class.java)
        val transformList = mutableListOf<Transform>()

        repeat(transforms) {
            transformList.add(mockTransform)
        }

        mockCrawler.mTransforms = transformList

        return transformList
    }
}
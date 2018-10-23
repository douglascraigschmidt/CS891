package edu.vanderbilt.imagecrawler.crawlers

import edu.vanderbilt.imagecrawler.helpers.ReflectionHelper
import edu.vanderbilt.imagecrawler.helpers.setField
import edu.vanderbilt.imagecrawler.transforms.Transform
import edu.vanderbilt.imagecrawler.utils.*
import edu.vanderbilt.imagecrawler.utils.Assignment.GRADUATE
import edu.vanderbilt.imagecrawler.utils.Assignment.UNDERGRADUATE
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit
import java.net.URL
import java.util.*
import java.util.concurrent.ForkJoinTask
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.fail


class Assignment2bTestsForkJoinCrawler2 {
    // Required to allow any() to work with mockito calls
    // to prevent any() can't be null Kotlin error messages.
    private fun <T> any(): T = Mockito.any<T>()

    @Mock
    lateinit var mockImage: Image

    @Mock
    lateinit var mockTransform: Transform

    @Mock
    lateinit var mockForkJoinImageTask: ForkJoinTask<Image>

    @Mock
    lateinit var mockPageElements: edu.vanderbilt.imagecrawler.utils.Array<WebPageElement>

    @Mock
    lateinit var mockHashSet: ConcurrentHashSet<String>

    @InjectMocks
    val mockCrawler: ForkJoinCrawler2 = mock(ForkJoinCrawler2::class.java)

    @Rule
    @JvmField
    var mockitoRule = MockitoJUnit.rule()!!

    //--------------------------------------------------------------

    @Test
    fun testURLCrawlerTaskNoPagesOrImages() {
        testURLCrawlerTask(GRADUATE, 0, 0)
    }

    //--------------------------------------------------------------

    @Test
    fun testURLCrawlerTaskNoPages() {
        testURLCrawlerTask(GRADUATE, 0, 100 + Random().nextInt(100))
    }

    //--------------------------------------------------------------

    @Test
    fun testURLCrawlerTaskNoImages() {
        testURLCrawlerTask(GRADUATE, 100 + Random().nextInt(100), 0)
    }

    //--------------------------------------------------------------

    @Test
    fun testURLCrawlerTaskGradMany() {
        val random = Random()
        testURLCrawlerTask(GRADUATE, 100 + random.nextInt(100), 100 + random.nextInt(100))
    }

    //--------------------------------------------------------------

    @Test
    fun testProcessImageTask() {
        testProcessImageTask(GRADUATE)
    }

    //-------------------------------------------------------------
    @Test
    fun testProcessImageTaskWhenDownFails() {
        val url = "http://www.foo.com/bar.jpg"
        val task = ForkJoinCrawler2().ProcessImageTask(url)

        ReflectionHelper.injectOuterClass(mockCrawler, task)
        `when`(mockCrawler.getOrDownloadImage(any(URL::class.java))).thenReturn(null)

        val result = task.compute()
        assertSame(0, result)

        verify(mockCrawler, times(1)).getOrDownloadImage(any(URL::class.java))
    }

    @Test
    fun testPerformTransformTaskNotInCache() {
        val task = ForkJoinCrawler2().PerformTransformTask(mockImage, mockTransform)
        val mockReturnImage = mock(Image::class.java)

        ReflectionHelper.injectOuterClass(mockCrawler, task)

        `when`(mockTransform.name).thenReturn("foobar")
        `when`(mockCrawler.createNewCacheItem(mockImage, mockTransform)).thenReturn(true)
        `when`(mockCrawler.applyTransform(mockTransform, mockImage)).thenReturn(mockReturnImage)

        val image = task.compute()
        assertSame(mockReturnImage, image)

        verify(mockCrawler, times(1)).createNewCacheItem(mockImage, mockTransform)
        verify(mockCrawler, times(1)).applyTransform(mockTransform, mockImage)
    }

    @Test
    fun testPerformTransformTaskAlreadyInCache() {
        val task = ForkJoinCrawler2().PerformTransformTask(mockImage, mockTransform)

        ReflectionHelper.injectOuterClass(mockCrawler, task)
        `when`(mockTransform.name).thenReturn("foobar")
        `when`(mockCrawler.createNewCacheItem(mockImage, mockTransform)).thenReturn(false)

        val image = task.compute()
        assertSame(null, image)

        verify(mockCrawler, times(1)).createNewCacheItem(mockImage, mockTransform)
        verify(mockCrawler, never()).applyTransform(mockTransform, mockImage)
    }

    /**
     * Test helper that handles both undergraduate and graduate cases.
     */
    private fun testURLCrawlerTask(type: Int, pages: Int, images: Int) {
        Assignment.types = type

        /******* TEST SETUP ************/

        val rootUrl = "/root"
        val imageRet = 1
        val pageRet = 1
        val startDepth = 777
        val maxDepth = Int.MAX_VALUE

        val pageElements = (1..pages).map {
            WebPageElement("http://www/PAGE/$it", Crawler.Type.PAGE)
        }.toMutableList()
        val imageElements = (1..images).map {
            WebPageElement("http://www/IMAGE/$it", Crawler.Type.IMAGE)
        }.toMutableList()
        val elements = (imageElements + pageElements).shuffled().toMutableList()

        val mockPageTasks = mutableListOf<ForkJoinCrawler2.URLCrawlerTask>()
        `when`(mockCrawler.makeURLCrawlerTask(anyString(), anyInt())).thenAnswer {
            val task = mock(ForkJoinCrawler2.URLCrawlerTask::class.java)
            task.mPageUri = it.arguments[0] as String
            task.mDepth = it.arguments[1] as Int
            assertEquals(startDepth + 1, task.mDepth)
            mockPageTasks.add(task)
            `when`(task.fork()).thenReturn(task) //****************** ERROR
            `when`(task.join()).thenReturn(pageRet) //****************** ERROR
            task
        }

        val mockImageTasks = mutableListOf<ForkJoinCrawler2.ProcessImageTask>()
        `when`(mockCrawler.makeProcessImageTask(anyString())).thenAnswer {
            val task = mock(ForkJoinCrawler2.ProcessImageTask::class.java)
            mockImageTasks.add(task)
            `when`(task.fork()).thenReturn(task) //****************** ERROR
            `when`(task.join()).thenReturn(imageRet) //****************** ERROR
            task
        }

        mockCrawler.mMaxDepth = maxDepth
        mockCrawler.mUniqueUris = mockHashSet
        `when`(mockHashSet.putIfAbsent(anyString())).thenReturn(true)

        val mockWebPageCrawler = mock(WebPageCrawler::class.java)
        mockCrawler.mWebPageCrawler = mockWebPageCrawler

        val mockPage = mock(Crawler.Page::class.java)
        `when`(mockWebPageCrawler.getPage(rootUrl)).thenReturn(mockPage)
        `when`(mockPage.getPageElements(Crawler.Type.IMAGE, Crawler.Type.PAGE)).thenReturn(mockPageElements)

        when (type) {
            // Graduates must use streams.
            GRADUATE -> `when`(mockPageElements.stream()).thenReturn(elements.stream())
            else -> fail("Assignment.type has not been set")
        }

        val task = mockCrawler.URLCrawlerTask(rootUrl, 1)
        task.mDepth = startDepth

        /******* TEST CALL ************/

        val count = task.compute()

        /******* TEST EVALUATION ************/

        assertEquals(imageRet * images + pageRet * pages, count)

        when (type) {
            GRADUATE -> {
                verify(mockCrawler, never()).makeForkJoinArray<Image>()
                verify(mockPageElements, times(1)).stream()
            }
            else -> fail("Assignment.type has not been set")
        }

        verify(mockWebPageCrawler, times(1)).getPage(rootUrl)
        verify(mockPage, times(1)).getPageElements(Crawler.Type.IMAGE, Crawler.Type.PAGE)

        // Check for the proper number of fork/join calls.
        mockPageTasks.forEach {
            verify(it, times(1)).fork()
            verify(it, times(1)).join()
        }

        mockImageTasks.forEach {
            verify(it, times(1)).fork()
            verify(it, times(1)).join()
        }
    }

    /**
     * Test helper that handles both undergraduate and graduate cases.
     */
    private fun testProcessImageTask(type: Int) {
        Assignment.types = type

        /******* TEST SETUP ************/

        val url = "http://www.foo.com/bar.jpg"

        // Use mock crawler as parent to ProcessImageTask
        // to capture all outer class method calls.
        val imageTask = mockCrawler.ProcessImageTask(url)

        // A real list of transforms
        val transforms = Transform.Factory.newTransforms(
                listOf(Transform.Type.NULL_TRANSFORM,
                        Transform.Type.GRAY_SCALE_TRANSFORM))

        // The total number of transforms that will be processed.
        val total = transforms.size

        // Set the mTransforms field of the ImageCrawler super class
        // to this real transform list.
        mockCrawler.setField("mTransforms", transforms)

        when (type) {
            GRADUATE -> Unit
            else -> fail("Assignment.type has not been set")
        }

        `when`(mockCrawler.getOrDownloadImage(any(URL::class.java))).thenReturn(mockImage)
        `when`(mockCrawler.makePerformTransformTask(any(), any())).thenReturn(mockForkJoinImageTask)

        `when`(mockForkJoinImageTask.fork()).thenReturn(mockForkJoinImageTask) //****************** ERROR
        `when`(mockForkJoinImageTask.join()).thenReturn(mockImage)

        /******* TEST CALL ************/

        val result = imageTask.compute()

        /******* TEST EVALUATION ************/

        // Should have processed all the transforms.
        assertSame(total, result)

        // Check for first call.
        verify(mockCrawler, times(1)).getOrDownloadImage(URL(url))

        when (type) {
            // No way to check if undergraduates decided to use streams.
            GRADUATE -> verify(mockCrawler, never()).makeForkJoinArray<Image>()
            else -> fail("Assignment.type has not been set")
        }

        // Check for the correct number of calls to makeProcessTransformTask
        // and also ensure that all calls were passed the correct arguments.
        val arg1 = ArgumentCaptor.forClass(Image::class.java)
        val arg2 = ArgumentCaptor.forClass(Transform::class.java)
        verify(mockCrawler, times(total)).makePerformTransformTask(arg1.capture(), arg2.capture())
        // Same image should be used in each new task.
        arg1.allValues.forEach {
            assertEquals(mockImage, it)
        }
        // Each task should use a different transform.
        arg2.allValues.forEachIndexed { i, transform ->
            assertEquals(transforms[i], transform)
        }

        // Check for the proper number of fork/join calls.
        verify(mockForkJoinImageTask, times(total)).fork()
        verify(mockForkJoinImageTask, times(total)).join()
    }
}

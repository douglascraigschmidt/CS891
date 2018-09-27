package edu.vanderbilt.imagecrawler.utils

import edu.vanderbilt.imagecrawler.platform.Cache
import edu.vanderbilt.imagecrawler.platform.FileCache
import org.junit.*
import org.junit.Assert.*
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileOutputStream

/**
 * All tests for ImageCache Kotlin object.
 */
class FileCacheTest {
    // If we want the singleton to be able to access the temporary
    // folder, then it must also be declared in a static class.
    companion object {
        @ClassRule
        @JvmField
        val tempFolder = TemporaryFolder()
    }

    // Create a singleton implementation of abstract FileCache class.
    object TestCache : FileCache(tempFolder.root)

    init {
        tempFolder.create()
    }

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun cacheItemCompareTest() {
        TestCache.clear()
        assertTrue(TestCache.cacheDir.list().isEmpty())

        for (i in 1 until 10) {
            val uri = "https://www.foo.bar/dir1/image$i"
            TestCache.put(uri)
            val item = TestCache.get(uri)
            assertNotNull(item)
            assertTrue(item?.key == uri)
        }

        for (i in 1 until 10) {
            val uri = "https://www.foo.bar/dir1/image$i"
            val item1 = TestCache.get(uri)
            assertNotNull(item1)
            val item2 = TestCache.get(uri)
            assertNotNull(item2)
            assertEquals(item1, item2)
        }
    }

    @Test
    fun createTestCache() {
        TestCache.clear()
        assertTrue(TestCache.cacheDir.list().isEmpty())

        for (i in 1 until 10) {
            val uri = "https://www.foo.bar/dir1/image$i"
            TestCache.put(uri)
            val item = TestCache.get(uri)
            assertNotNull(item)
            assertTrue(item?.key == uri)
        }
    }

    @Ignore
    @Test
    fun cacheSweepAndLoadTest() {
        TestCache.clear()
        assertTrue(TestCache.cacheDir.list().isEmpty())

        var i = 0

        // Valid files.
        do {
            val uri = "https://www.foo.bar/dir1/image$i"
            val path = TestCache.getCachePath(uri)
            val file = File(TestCache.cacheDir, path)
            assertTrue(file.createNewFile())
            FileOutputStream(file).use {
                it.write(path.toByteArray())
            }
        } while (++i % 10 != 0)

        // Empty files to be swept.
        do {
            val uri = "https://www.foo.bar/dir1/image$i"
            val path = TestCache.getCachePath(uri)
            val file = File(TestCache.cacheDir, path)
            assertTrue(file.createNewFile())
        } while (++i % 10 != 0)

        // Invalid files to be swept.
        do {
            val path = "image$i"
            val file = File(TestCache.cacheDir, path)
            assertTrue(file.createNewFile())
            FileOutputStream(file).use {
                it.write(path.toByteArray())
            }
        } while (++i % 10 != 0)

        TestCache.loadFromDisk()

        assertEquals(10, TestCache.getCacheSize())

        for (j in 1 until 10) {
            val uri = "https://www.foo.bar/dir1/image$j"
            val item = TestCache.get(uri)
            assertNotNull(item)
            assertTrue(item?.key == uri)
        }
    }

    @Ignore
    @Test
    fun cacheLoadTest() {
        TestCache.clear()
        assertTrue(TestCache.cacheDir.list().isEmpty())

        for (i in 1 until 10) {
            val uri = "https://www.foo.bar/dir1/image$i"
            val path = TestCache.getCachePath(uri)
            val file = File(TestCache.cacheDir, path)
            assertTrue(file.createNewFile())
            FileOutputStream(file).write(uri.toByteArray())
        }

        TestCache.loadFromDisk()

        for (i in 1 until 10) {
            val uri = "https://www.foo.bar/dir1/image$i"
            val item = TestCache.get(uri)
            assertNotNull(item)
            assertTrue(item?.key == uri)
        }
    }

    @Ignore
    @Test
    fun testObserver() {
        TestCache.clear()
        assertTrue(TestCache.cacheDir.list().isEmpty())

        buildDiskCache(10)
        TestCache.loadFromDisk()
        assertEquals(10, TestCache.getCacheSize())

        val events = mutableListOf<String>()

        val observer: FileCache.Observer = object : FileCache.Observer {
            override fun event(operation: Cache.Operation, item: Cache.Item, progress: Float) {
                val msg = "item = ${item.key} operation = $operation"
                System.out.println(msg)
                events.add(msg)
            }
        }

        TestCache.startWatching(observer)

        events.sorted().forEachIndexed { i, msg ->
            val uri = "https://www.foo.bar/dir1/image${"%03d".format(i + 1)}"
            val expected = "item = $uri state = ${Cache.Operation.LOAD}"
            assertEquals(expected, msg)
        }
    }

    @Ignore
    @Test
    fun testObserverFileStates() {
        TestCache.clear()
        assertTrue(TestCache.cacheDir.list().isEmpty())

        val events = mutableListOf<String>()

        val observer: FileCache.Observer = object : FileCache.Observer {
            override fun event(operation: Cache.Operation, item: Cache.Item, progress: Float) {
                val msg = "item = ${item.key} operation = $operation"
                System.out.println(msg)
                events.add(msg)
            }
        }

        val key = "https://www.foo.bar/image"
        TestCache.startWatching(observer)

        TestCache.put(key)
        val item = TestCache.get(key)
        assertNotNull(item)
        item?.getOutputStream(Cache.Operation.WRITE).use { stream ->
            (1..10).forEach {
                stream?.write(it)
            }
        }

        val expected =
                (1..10).map { "item = $key state = ${Cache.Operation.WRITE}" }
                .toMutableList()
        expected.add("item = $key state = ${Cache.Operation.CLOSE}")

        assertEquals(expected, events)
    }

    @Ignore
    @Test
    fun testConcurrentCacheAccess() {
        TestCache.clear()
        assertTrue(TestCache.cacheDir.list().isEmpty())

        val events = mutableListOf<String>()

        val observer: FileCache.Observer = object : FileCache.Observer {
            override fun event(operation: Cache.Operation, item: Cache.Item, progress: Float) {
                val msg = "item = ${item.key} operation = $operation"
                System.out.println(msg)
                events.add(msg)
            }
        }

        val key = "https://www.foo.bar/image"
        TestCache.startWatching(observer)

        TestCache.put(key)
        val item = TestCache.get(key)
        assertNotNull(item)
        item?.getOutputStream().use { stream ->
            (1..10).forEach {
                stream?.write(it)
            }
        }

        val expected =
                (1..10).map { "item = $key state = ${Cache.Operation.WRITE}" }
                        .toMutableList()
        expected.add("item = $key state = ${Cache.Operation.CLOSE}")

        assertEquals(expected, events)
    }

    private fun buildDiskCache(size: Int) {
        for (i in 1..size) {
            val uri = "https://www.foo.bar/dir1/image${"%03d".format(i)}"
            val path = TestCache.getCachePath(uri)
            val file = File(TestCache.cacheDir, path)
            assertTrue(file.createNewFile())
            FileOutputStream(file).write(uri.toByteArray())
        }
    }
}
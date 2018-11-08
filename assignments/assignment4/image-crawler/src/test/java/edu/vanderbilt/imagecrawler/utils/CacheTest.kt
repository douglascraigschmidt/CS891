package edu.vanderbilt.imagecrawler.utils

import edu.vanderbilt.imagecrawler.platform.Cache
import org.junit.*
import org.junit.Assert.*
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileOutputStream

/**
 * All tests for ImageCache Kotlin object.
 */
@Ignore
class CacheTest {
    // If we want the singleton to be able to access the temporary
    // folder, then it must also be declared in a static class.
    companion object {
        @ClassRule
        @JvmField
        val tempFolder = TemporaryFolder()
    }

    object TestCache : Cache(tempFolder.root)

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

        val baseUri = "https://www.foo.bar/dir1/image"

        for (i in 1 until 10) {
            val uri = "$baseUri$i"
            val item = TestCache.addOrGetItem(uri, null, null)
            assertNotNull(item)
            val name = TestCache.getRelativeCachePath("$baseUri$i", null)
            assertTrue(item!!.file.name == name)
        }

        for (i in 1 until 10) {
            val uri = "$baseUri$i"
            val item1 = TestCache.getItem(uri, null)
            assertNotNull(item1)
            val item2 = TestCache.getItem(uri, null)
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
            val item = TestCache.addOrGetItem(uri, null, null)
            assertNotNull(item)
            val name = TestCache.getRelativeCachePath(uri, null)
            assertTrue(item!!.file.name == name)
        }
    }

    @Test
    fun cacheSweepAndLoadTest() {
        TestCache.clear()
        assertTrue(TestCache.cacheDir.list().isEmpty())

        var i = 0
        val baseUri = "https://www.foo.bar/dir1/image"

        var expectedLoadedCount = 0

        // Valid files.
        do {
            val path = TestCache.getRelativeCachePath("$baseUri$i", null)
            val file = File(TestCache.cacheDir, path)
            assertTrue(file.createNewFile())
            FileOutputStream(file).use {
                it.write(path.toByteArray())
            }
            expectedLoadedCount++
        } while (++i % 10 != 0)

        var expectedSweptCount = 0

        // Empty files should be swept.
        do {
            val path = TestCache.getRelativeCachePath("$baseUri$i", null)
            val file = File(TestCache.cacheDir, path)
            assertTrue(file.createNewFile())
            expectedSweptCount++
        } while (++i % 10 != 0)

        // Invalid files with no tag should be swept.
        do {
            val path = "image$i"
            val file = File(TestCache.cacheDir, path)
            assertTrue(file.createNewFile())
            FileOutputStream(file).use {
                it.write(path.toByteArray())
            }
            expectedSweptCount++
        } while (++i % 10 != 0)

        // Invalid files with missing LHS of '-' should be swept.
        do {
            val path = "-image$i"
            val file = File(TestCache.cacheDir, path)
            assertTrue(file.createNewFile())
            FileOutputStream(file).use {
                it.write(path.toByteArray())
            }
            expectedSweptCount++
        } while (++i % 10 != 0)

        // Invalid files with missing RHS of '-' should be swept.
        do {
            val path = "NOTAG$i-"
            val file = File(TestCache.cacheDir, path)
            assertTrue(file.createNewFile())
            FileOutputStream(file).use {
                it.write(path.toByteArray())
            }
            expectedSweptCount++
        } while (++i % 10 != 0)

        val loaded = TestCache.loadFromDisk()
        assertEquals(loaded, TestCache.cacheSize)
        assertEquals(expectedLoadedCount, loaded)
        assertEquals(expectedSweptCount, i - loaded)

        i = 0
        do {
            val uri = "$baseUri$i"
            val item = TestCache.getItem(uri, null)
            assertNotNull(item)
            assertTrue(item!!.file.name == TestCache.getRelativeCachePath(uri, null))
        } while (++i % 10 != 0)
    }

    @Test
    fun cacheLoadTest() {
        TestCache.clear()
        assertTrue(TestCache.cacheDir.list().isEmpty())

        val uri = "https://www.foo.bar/dir1/image"

        for (i in 1 until 10) {
            val path = TestCache.getRelativeCachePath("$uri$i", null)
            val file = File(TestCache.cacheDir, path)
            assertTrue(file.createNewFile())
            FileOutputStream(file).write(uri.toByteArray())
        }

        TestCache.loadFromDisk()

        for (i in 1 until 10) {
            val item = TestCache.getItem("$uri$i", null)
            assertNotNull(item)
            val path = TestCache.getRelativeCachePath("$uri$i", null)
            assertTrue(item?.file == File(TestCache.cacheDir, path))
        }
    }

    @Test
    fun testObserver() {
        TestCache.clear()
        assertTrue(TestCache.cacheDir.list().isEmpty())

        buildDiskCache(10)
        TestCache.loadFromDisk()
        assertEquals(10, TestCache.cacheSize)

        val events = mutableListOf<String>()

        val observer: Cache.Observer = Cache.Observer { operation, item, _ ->
            val msg = "item = ${item.key} op = $operation"
            System.out.println(msg)
            events.add(msg)
        }

        TestCache.startWatching(observer, true)

        val list = events.sorted().toList();
        list.forEachIndexed { i, msg ->
            val uri = "https://www.foo.bar/dir1/image${"%03d".format(i + 1)}"
            val path = TestCache.getRelativeCachePath(uri, null)
            val expected = "item = $path op = ${Cache.Operation.LOAD}"
            assertEquals(expected, msg)
        }
    }

    @Test
    fun testObserverFileStates() {
        TestCache.clear()
        assertTrue(TestCache.cacheDir.list().isEmpty())

        val events = mutableListOf<String>()

        val observer: Cache.Observer = Cache.Observer { operation, item, _ ->
            val msg = "item = ${item.key} op = $operation"
            System.out.println(msg)
            events.add(msg)
        }

        val key = "https://www.foo.bar/image"
        TestCache.startWatching(observer, true)

        val item = TestCache.addOrGetItem(key, null, null)
        assertNotNull(item)
        item?.getOutputStream(Cache.Operation.WRITE, 0).use { stream ->
            (1..10).forEach {
                stream?.write(it.toString().toByteArray())
            }
        }

        val path = TestCache.getRelativeCachePath(key, null)
        val expected = mutableListOf("item = $path op = ${Cache.Operation.CREATE}")
        (1..10).map { "item = $path op = ${Cache.Operation.WRITE}" }
                .forEach { expected.add(it) }
        expected.add("item = $path op = ${Cache.Operation.CLOSE}")

        assertEquals(expected, events)
    }

    @Test
    fun testConcurrentCacheAccess() {
        TestCache.clear()
        assertTrue(TestCache.cacheDir.list().isEmpty())

        val events = mutableListOf<String>()

        val observer: Cache.Observer = Cache.Observer { operation, item, _ ->
            val msg = "item = ${item.key} op = $operation"
            System.out.println(msg)
            events.add(msg)
        }

        val key = "https://www.foo.bar/image"
        TestCache.startWatching(observer, true)

        val item = TestCache.addOrGetItem(key, null, null)
        assertNotNull(item)
        item?.getOutputStream(Cache.Operation.WRITE, 0).use { stream ->
            (1..10).forEach {
                stream?.write(it.toString().toByteArray())
            }
        }

        val path = TestCache.getRelativeCachePath(key, null)
        val expected = mutableListOf("item = $path op = ${Cache.Operation.CREATE}")
        (1..10).map { "item = $path op = ${Cache.Operation.WRITE}" }
                .forEach { expected.add(it) }
        expected.add("item = $path op = ${Cache.Operation.CLOSE}")

        assertEquals(expected, events)
    }

    private fun buildDiskCache(size: Int) {
        for (i in 1..size) {
            val uri = "https://www.foo.bar/dir1/image${"%03d".format(i)}"
            val path = TestCache.getRelativeCachePath(uri, null)
            val file = File(TestCache.cacheDir, path)
            assertTrue(file.createNewFile())
            FileOutputStream(file).write(uri.toByteArray())
        }
    }
}
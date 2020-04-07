package edu.vandy.simulator.managers.palantiri.reentrantLockHashMapSimpleSemaphore

import admin.AssignmentTests
import admin.firstField
import admin.injectInto
import admin.value
import com.nhaarman.mockitokotlin2.*
import edu.vandy.simulator.managers.palantiri.Palantir
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.rules.Timeout
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.lenient
import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collector
import java.util.stream.Stream
import kotlin.collections.HashMap

class Assignment_2B_ReentrantLockHashMapSimpleSemaphoreMgrTest : AssignmentTests() {
    companion object {
        private const val PALANTIRI_COUNT = 5
    }

    override var timeout: Timeout
        get() = Timeout.seconds(100000)
        set(value) {}
    @Mock
    lateinit var streamPalantirMock: Stream<Palantir>
    @Mock
    lateinit var optionalPalantirMock: Optional<Palantir>
    @Mock
    lateinit var palantirMock: Palantir
    @Mock
    lateinit var semaphoreMock: SimpleSemaphore
    @Mock
    lateinit var palantiriMapMock: MutableMap<Palantir, Boolean>
    @Mock
    lateinit var entrySetMock: MutableSet<MutableMap.MutableEntry<Palantir, Boolean>>
    @Mock
    lateinit var streamEntrySetMock: Stream<MutableMap.MutableEntry<Palantir, Boolean>>
    @Mock
    lateinit var palantiriListMock: MutableList<Palantir>
    @Mock
    lateinit var streamPalantiriListMock: Stream<Palantir>
    @Mock
    lateinit var managerMock: ReentrantLockHashMapSimpleSemaphoreMgr

    @InjectMocks
    lateinit var manager: ReentrantLockHashMapSimpleSemaphoreMgr

    // In order to put mock entries in this map, it can't be a mock.
    private val palantiriMap = HashMap<Palantir, Boolean>(PALANTIRI_COUNT)

    // In order to put mock entries in this list, it can't be a mock.
    private var palantiri = mutableListOf<Palantir>()
    private lateinit var lockMock: ReentrantLock

    private class SimulatedException : RuntimeException("Simulated exception")

    // In order to put mock entries in this list, it can't be a mock.

    @Before
    fun before() {
        repeat(PALANTIRI_COUNT) {
            mock<Palantir>().let { mockPalantir ->
                palantiri.add(mockPalantir)
                palantiriMap[mockPalantir] = true
            }
        }

        // mPalantiriMap and mPalantiri can't be mocked themselves,
        // only their contents can be mocked.
        manager.mPalantiriMap = palantiriMap
        manager.mPalantiri = palantiri

        // Handles the case where the user has declared the lock field as
        // either Lock or ReentrantLock.
        lockMock = mock()
        injectLockField(lockMock, manager)
    }

    private fun injectLockField(lockMock: ReentrantLock, manager: Any) {
        val firstField = manager.firstField<ReentrantLock>()
        var field = firstField
        if (field != null) {
            field[manager] = lockMock
        } else {
            field = manager.firstField<Lock>()!!
            field[manager] = lockMock
        }
    }

    @Test
    fun `build model undergraduate and graduate test`() {
        // Note that the buildModel method does not use the
        // mManager created in the @Before setup
        // method because it needs to test the real Semaphore,
        // ReentrantLock, and HashMap fields for proper initialization.
        val mockPalantiri = (1..PALANTIRI_COUNT).map { mock<Palantir>() }

        lenient().`when`(managerMock.palantiri).thenReturn(mockPalantiri)
        lenient().`when`(managerMock.palantirCount).thenReturn(mockPalantiri.size)
        doCallRealMethod().whenever(managerMock).buildModel()

        // Call SUT method.
        managerMock.buildModel()

        val semaphore = managerMock.value<SimpleSemaphore>()
        assertEquals(PALANTIRI_COUNT, semaphore?.availablePermits())

        val lock = try {
            managerMock.value<ReentrantLock>()
        } catch (t: Throwable) {
            managerMock.value<Lock>()
        }

        assertNotNull("Lock field should exist and not be null.", lock)

        val availablePalantiri = managerMock.firstField<SimpleSemaphore>()

        assertNotNull("SimpleSemaphore field should exist and not be null", availablePalantiri)
        assertNotNull("mPalantiriMap should not be null.", managerMock.mPalantiriMap)
        assertEquals("getPalantiriMap() should contain $PALANTIRI_COUNT entries.",
                PALANTIRI_COUNT, managerMock.mPalantiriMap.size)
    }

    /**
     * Tests GRADUATE students use of Java 8 streams.
     */
    @Test
    fun `build model graduate only test`() {
        graduateTest()

        // Note that the buildModel method does not use the
        // managerMock created in the @Before setup
        // method because it needs to test the real Semaphore,
        // ReentrantLock, and HashMap fields for proper initialization.
        val inOrder = inOrder(managerMock, palantiriListMock, streamPalantiriListMock)

        lenient().`when`(managerMock.palantiri).thenReturn(palantiriListMock)
        lenient().`when`(palantiriListMock.stream()).thenReturn(streamPalantiriListMock)
        lenient().`when`(palantiriListMock.size).thenReturn(PALANTIRI_COUNT)
        whenever(streamPalantiriListMock.collect(any<Collector<in Palantir, Any, Any>>())).thenReturn(palantiriMapMock)
        whenever(managerMock.palantiri).thenReturn(palantiriListMock)
        doCallRealMethod().whenever(managerMock).buildModel()

        // Call SUT method.
        managerMock.buildModel()

        verify(managerMock).palantiri
        verify(palantiriListMock).stream()
        verify(streamPalantiriListMock).collect(any<Collector<in Palantir, Any, Any>>())
        inOrder.verify(managerMock).palantiri
        inOrder.verify(palantiriListMock).stream()
        inOrder.verify(streamPalantiriListMock).collect(any<Collector<in Palantir, Any, Any>>())
    }

    /**
     * Uses mManager instance created in the @Before setup method.
     */
    @Test
    fun `acquire returns locks the first available palantir`() {
        // Call SUT.
        val palantir = manager.acquire()
        assertNotNull("Acquire should return a non-null Palantir", palantir)

        val locked = palantiriMap.values.count { !it }
        assertEquals("Only 1 palantir should be locked", 1, locked)

        verify(semaphoreMock).acquire()
        try {
            verify(lockMock).lock()
        } catch (t: Throwable) {
            verify(lockMock).lockInterruptibly()
        }

        verify(lockMock).unlock()
    }

    /**
     * Uses mManager instance created in the @Before setup method.
     */
    @Test
    fun `acquire lock the only available palantir`() {
        lockAllPalantiri()
        val unlockedPalantir = palantiri[PALANTIRI_COUNT - 1]
        unlockPalantir(unlockedPalantir)
        val inOrder = inOrder(lockMock, semaphoreMock)

        // Call SUT.
        val palantir = manager.acquire()

        assertNotNull("Acquire should return a non-null Palantir", palantir)

        val locked = palantiriMap.values.count { !it }
        assertEquals("All $PALANTIRI_COUNT palantiri should be locked", PALANTIRI_COUNT, locked)
        assertSame("The only available Palantir should be returned", unlockedPalantir, palantir)

        verify(semaphoreMock).acquire()
        try {
            verify(lockMock).lock()
        } catch (t: Throwable) {
            verify(lockMock).lockInterruptibly()
        }

        verify(lockMock).unlock()
        inOrder.verify(semaphoreMock).acquire()

        try {
            inOrder.verify(lockMock).lock()
        } catch (t: Throwable) {
            inOrder.verify(lockMock).lockInterruptibly()
        }

        inOrder.verify(lockMock).unlock()
    }

    /**
     * Uses mManager instance created in the @Before setup method.
     */
    @Test
    fun `acquire all available palantiri`() {
        val inOrder = inOrder(lockMock, semaphoreMock)
        for (i in 1..PALANTIRI_COUNT) {
            // Call SUT.
            val palantir = manager.acquire()

            assertNotNull("Acquire should return a non-null Palantir", palantir)
            val locked = palantiriMap.values.count { !it }
            assertEquals("$i palantiri should be acquired (locked).", i, locked)
        }

        verify(semaphoreMock, times(PALANTIRI_COUNT)).acquire()
        verify(lockMock, times(PALANTIRI_COUNT)).lockInterruptibly()
        verify(lockMock, never()).lock()
        verify(lockMock, times(PALANTIRI_COUNT)).unlock()

        inOrder.verify(semaphoreMock).acquire()
        inOrder.verify(lockMock).lockInterruptibly()
        inOrder.verify(lockMock).unlock()
    }

    /**
     * Tests GRADUATE students use of Java 8 streams.
     */
    @Test
    fun `acquire all available palantiri graduate version`() {
        graduateTest()

        val inOrder = inOrder(palantiriMapMock,
                entrySetMock,
                streamEntrySetMock,
                streamPalantirMock,
                optionalPalantirMock)

        whenever(palantiriMapMock.entries).thenReturn(entrySetMock)
        whenever(entrySetMock.stream()).thenReturn(streamEntrySetMock)

        val anyFunction = any<Predicate<Map.Entry<Palantir, Boolean>>>()
        whenever(streamEntrySetMock.filter(anyFunction)).thenReturn(streamEntrySetMock)

        val mapFunction = any<Function<Map.Entry<Palantir, Boolean>, Palantir>>()
        whenever(streamEntrySetMock.map(mapFunction)).thenReturn(streamPalantirMock)

        lenient().`when`(streamPalantirMock.findFirst()).thenReturn(optionalPalantirMock)
        lenient().`when`(streamPalantirMock.findAny()).thenReturn(optionalPalantirMock)
        lenient().`when`(optionalPalantirMock.orElse(null)).thenReturn(palantirMock)

        manager.mPalantiriMap = palantiriMapMock

        // Call SUT.
        val palantir = manager.acquire()

        assertEquals(palantirMock, palantir)
        verify(palantiriMapMock).entries
        verify(entrySetMock).stream()
        verify(streamEntrySetMock).filter(any())
        verify(streamEntrySetMock).map(any<Function<Map.Entry<Palantir, Boolean>, Palantir>>())

        try {
            verify(streamPalantirMock).findFirst()
        } catch (e: Exception) {
            verify(streamPalantirMock).findAny()
        }

        inOrder.verify(palantiriMapMock).entries
        inOrder.verify(entrySetMock).stream()
        inOrder.verify(streamEntrySetMock).filter(any())

        inOrder.verify(streamEntrySetMock).map(any<Function<Map.Entry<Palantir, Boolean>, Palantir>>())

        try {
            inOrder.verify(streamPalantirMock).findFirst()
        } catch (e: Exception) {
            inOrder.verify(streamPalantirMock).findAny()
        }
    }

    @Test
    fun `acquire does not call unlock if semaphore acquire fails`() {
        doThrow(SimulatedException()).whenever(semaphoreMock).acquire()

        // SUT
        assertThrows(SimulatedException::class.java) {
            manager.acquire()
        }

        verify(semaphoreMock).acquire()
        verifyNoMoreInteractions(lockMock)
    }

    @Test
    fun `acquire does not call unlock if lock fails`() {
        doNothing().whenever(semaphoreMock).acquire()
        lenient().doThrow(SimulatedException()).whenever(lockMock).lock()
        lenient().doThrow(SimulatedException()).whenever(lockMock).lockInterruptibly()

        // SUT
        assertThrows(SimulatedException::class.java) {
            manager.acquire()
        }

        verify(semaphoreMock).acquire()

        try {
            verify(lockMock).lock()
        } catch (t: Throwable) {
            verify(lockMock).lockInterruptibly()
        }

        verifyNoMoreInteractions(lockMock)
    }

    @Test
    fun `release handles a null palantir`() {
        try {
            // Call SUT.
            manager.release(null)
        } catch (e: Exception) {
            fail("Release should not throw an exception if a " +
                    "null Palantir is passed as a parameter.")
        }
    }

    @Test
    fun `release an acquired palantir`() {
        val lockedPalantir = palantiri[PALANTIRI_COUNT - 1]
        lockPalantir(lockedPalantir)

        // Call SUT.
        manager.release(lockedPalantir)

        try {
            verify(lockMock).lock()
        } catch (t: Throwable) {
            verify(lockMock).lockInterruptibly()
        }
        assertTrue(
                "Released Palantir should not be locked in HashMap.",
                palantiriMap[lockedPalantir]!!)
    }

    @Test
    fun `release all acqquired palantiri`() {
        lockAllPalantiri()

        // Call SUT.
        palantiri.forEach { manager.release(it) }

        val inOrder = inOrder(lockMock)
        try {
            verify(lockMock, times(PALANTIRI_COUNT)).lock()
        } catch (t: Throwable) {
            verify(lockMock, times(PALANTIRI_COUNT)).lockInterruptibly()
        }

        verify(lockMock, times(PALANTIRI_COUNT)).unlock()

        try {
            inOrder.verify(lockMock).lock()
        } catch (t: Throwable) {
            inOrder.verify(lockMock).lockInterruptibly()
        }

        inOrder.verify(lockMock).unlock()

        val count = palantiriMap.values.count { it }
        assertEquals("All $PALANTIRI_COUNT Palantiri should be unlocked.", PALANTIRI_COUNT, count)
    }

    @Test
    fun `release only releases semaphore that was previously held`() {
        injectLockField(lockMock, managerMock)
        palantiriMapMock.injectInto(managerMock)
        semaphoreMock.injectInto(managerMock)
        lenient().doReturn(false).whenever(palantiriMapMock).put(any(), any())
        lenient().doReturn(false).whenever(palantiriMapMock).replace(any(), any())

        doCallRealMethod().whenever(managerMock).release(palantirMock)
        managerMock.release(palantirMock)

        verify(managerMock).release(palantirMock)
        verify(lockMock).lockInterruptibly()
        verify(lockMock).unlock()
        try {
            verify(palantiriMapMock).put(any(), any())
        } catch (t: Throwable) {
            verify(palantiriMapMock).replace(any(), any())
        }

        verify(semaphoreMock).release()
        verifyNoMoreInteractions(managerMock, lockMock, palantiriMapMock, semaphoreMock)
    }

    @Test
    fun `release does not unlock the lock if the lock call is interrupted`() {
        doThrow(SimulatedException()).whenever(lockMock).lockInterruptibly()

        val palantir = Palantir(manager)

        // SUT
        assertThrows(SimulatedException::class.java) {
            manager.release(palantir)
        }

        verify(lockMock).lockInterruptibly()
        verifyNoMoreInteractions(lockMock)
    }

    private fun lockAllPalantiri() {
        repeat(PALANTIRI_COUNT) {
            val palantir = palantiri[it]
            palantiriMap[palantir] = false
        }
    }

    private fun unlockPalantir(palantir: Palantir) {
        palantiriMap[palantir] = true
    }

    private fun lockPalantir(palantir: Palantir) {
        palantiriMap[palantir] = false
    }
}
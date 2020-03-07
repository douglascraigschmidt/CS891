package edu.vandy.simulator.managers.palantiri.spinLockHashMap

import admin.AssignmentTests
import com.nhaarman.mockitokotlin2.*
import edu.vandy.simulator.managers.palantiri.Palantir
import edu.vandy.simulator.utils.Assignment.isGraduateTodo
import edu.vandy.simulator.utils.Assignment.isUndergraduateTodo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.inOrder
import java.lang.RuntimeException
import java.util.*
import java.util.concurrent.Semaphore

@ExperimentalCoroutinesApi
class Assignment_1B_SpinLockHashMapMgrTest : AssignmentTests() {
    @Mock
    internal lateinit var cancellableLockMock: CancellableLock

    @Mock
    lateinit var semaphoreMock: Semaphore

    @InjectMocks
    lateinit var manager: SpinLockHashMapMgr

    @InjectMocks
    private val managerMock = mock<SpinLockHashMapMgr>()

    // In order to put mock entries in this map, it can't be a mock.
    private val palantiriMap = HashMap<Palantir, Boolean>(PALANTIRI_COUNT)

    // In order to put mock entries in this list, it can't be a mock.
    private var palantiri = mutableListOf<Palantir>()

    class SimulatedException: RuntimeException("Simulated exception")

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
    }

    @Test
    fun buildModelUndergraduate() {
        undergraduateTest()
        buildModel()
    }

    @Test
    fun buildModelGraduate() {
        graduateTest()
        buildModel()
    }

    private fun buildModel() {
        // Note that the buildModel method does not use the
        // SpinLockHashMapMgr created in the @Before setup
        // method because it needs to test the real Semaphore,
        // SpinLock, and Map fields for proper initialization.
        val manager: SpinLockHashMapMgr = mock()
        val mockPalantiri = (1..PALANTIRI_COUNT).map { mock<Palantir>() }

        whenever(manager.palantiri).thenReturn(mockPalantiri)
        doCallRealMethod().whenever(manager).buildModel()
        doCallRealMethod().whenever(manager).availablePalantiri
        doCallRealMethod().whenever(manager).spinLock
        doCallRealMethod().whenever(manager).palantiriMap

        // Call SUT method.
        manager.buildModel()

        if (isUndergraduateTodo()) {
            assertTrue(manager.spinLock is SpinLock)
        } else if (isGraduateTodo()) {
            assertTrue(manager.spinLock is ReentrantSpinLock)
        }

        assertNotNull(
                "getAvailablePalantiri() should not return null.",
                manager.availablePalantiri)
        assertEquals(
                "The available palantiri semaphore should 0 permits.",
                0,
                manager.availablePalantiri.queueLength.toLong())
        assertNotNull(
                "getSpinLock() accessor should not return null.",
                manager.spinLock)
        assertNotNull(
                "getPalantiriMap() should not return null.",
                manager.palantiriMap)
        assertEquals(
                "getPalantiriMap() should contain $PALANTIRI_COUNT entries.",
                PALANTIRI_COUNT.toLong(),
                manager.palantiriMap.size.toLong())
    }

    @Test
    fun `available permits returns expected value`() {
        val expected = 999
        whenever(semaphoreMock.availablePermits()).thenReturn(expected)
        doCallRealMethod().whenever(managerMock).availablePermits()

        // SUT
        assertEquals(expected.toLong(), managerMock.availablePermits().toLong())

        verify(managerMock).availablePermits()
        verifyNoMoreInteractions(managerMock)
        verify(semaphoreMock).availablePermits()
        verifyNoMoreInteractions(semaphoreMock)
    }

    @Test
    fun `acquire a palantir when all palantiri are available`() {
        doNothing().whenever(cancellableLockMock).lock(any())
        doNothing().whenever(cancellableLockMock).unlock()
        doNothing().whenever(semaphoreMock).acquire()

        // SUT
        val palantir = manager.acquire()

        assertNotNull("Acquire should return a non-null Palantir", palantir)
        val locked = palantiriMap.values.count { !it }
        assertEquals("Only 1 palantir should be locked", 1, locked)
        verify(semaphoreMock).acquire()
        verify(cancellableLockMock).lock(any())
        verify(cancellableLockMock).lock(any())
    }

    /**
     * Uses mManager instance created in the @Before setup method.
     */
    @Test
    fun `acquire a palantir when only one palantir is available`() {
        lockAllPalantiri()
        val unlockedPalantir = palantiri[PALANTIRI_COUNT - 1]
        unlockPalantir(unlockedPalantir)
        doNothing().whenever(cancellableLockMock).lock(any())
        doNothing().whenever(cancellableLockMock).unlock()
        doNothing().whenever(semaphoreMock).acquire()
        val inOrder = inOrder(cancellableLockMock, semaphoreMock)

        // SUT
        val palantir = manager.acquire()

        assertNotNull("Acquire should return a non-null Palantir", palantir)
        val locked = palantiriMap.values.count { !it }
        assertEquals("All $PALANTIRI_COUNT palantiri should be locked", PALANTIRI_COUNT, locked)
        assertSame("The only available Palantir should be returned", unlockedPalantir, palantir)
        verify(semaphoreMock).acquire()
        verify(cancellableLockMock).lock(any())
        verify(cancellableLockMock).unlock()
        inOrder.verify(semaphoreMock).acquire()
        inOrder.verify(cancellableLockMock).lock(any())
        inOrder.verify(cancellableLockMock).unlock()
    }

    /**
     * Uses mManager instance created in the @Before setup method.
     */
    @Test
    fun `acquire all available palantiri`() {
        doNothing().whenever(cancellableLockMock).lock(any())
        doNothing().whenever(cancellableLockMock).unlock()
        doNothing().whenever(semaphoreMock).acquire()
        val inOrder = inOrder(cancellableLockMock, semaphoreMock)
        for (i in 1..PALANTIRI_COUNT) {
            // SUT
            val palantir = manager.acquire()

            assertNotNull("Acquire should return a non-null Palantir", palantir)
            val locked = palantiriMap.values.count { !it }
            assertEquals("$i palantiri should be acquired (locked).", i, locked)
        }
        verify(semaphoreMock, times(PALANTIRI_COUNT)).acquire()
        verify(cancellableLockMock, times(PALANTIRI_COUNT)).lock(any())
        verify(cancellableLockMock, times(PALANTIRI_COUNT)).unlock()
        inOrder.verify(semaphoreMock).acquire()
        inOrder.verify(cancellableLockMock).lock(any())
        inOrder.verify(cancellableLockMock).unlock()
    }

    @Test
    fun `acquire does not call unlock if semaphore acquire fails`() {
        doThrow(SimulatedException()).whenever(semaphoreMock).acquire()
        doCallRealMethod().whenever(managerMock).acquire()

        // SUT
        assertThrows<SimulatedException>("Exception should have been thrown") {
            managerMock.acquire()
        }

        verify(managerMock).acquire()
        verifyNoMoreInteractions(managerMock, cancellableLockMock)
    }

    @Test
    fun `acquire does not call unlock if lock fails`() {
        doThrow(SimulatedException()).whenever(cancellableLockMock).lock(any())
        doCallRealMethod().whenever(managerMock).acquire()

        // SUT
        assertThrows<SimulatedException>("Exception should have been thrown") {
            managerMock.acquire()
        }

        verify(managerMock).acquire()
        verify(semaphoreMock).acquire()
        verify(cancellableLockMock).lock(any())
        verifyNoMoreInteractions(managerMock, cancellableLockMock)
    }

    @Test
    fun `release a null palantir`() {
        try {
            // SUT
            manager.release(null)
        } catch (e: Exception) {
            fail("Release should not throw an exception if a " +
                    "null Palantir is passed as a parameter.")
        }
    }


    @Test
    fun `release a locked palantir`() {
        val mockPalantiriMap: HashMap<Palantir, Boolean> = mock(lenient = true)
        val mockPalantir: Palantir = mock()
        manager.mPalantiriMap = mockPalantiriMap

        doReturn(false).whenever(mockPalantiriMap).replace(eq(mockPalantir), eq(true))
        doReturn(false).whenever(mockPalantiriMap).put(eq(mockPalantir), eq(true))
        val inOrder = inOrder(cancellableLockMock, mockPalantiriMap, semaphoreMock)

        // SUT
        manager.release(mockPalantir)

        inOrder.verify(cancellableLockMock).lock(any())
        try {
            inOrder.verify(mockPalantiriMap).replace(eq(mockPalantir), eq(true))
        } catch (t: Throwable) {
            inOrder.verify(mockPalantiriMap).put(eq(mockPalantir), eq(true))
        }
        inOrder.verify(cancellableLockMock).unlock()
        inOrder.verify(semaphoreMock).release()
    }

    @Test
    fun `release an unlocked palantir`() {
        val mockPalantiriMap: HashMap<Palantir, Boolean> = mock(lenient = true)
        val mockPalantir: Palantir = mock()
        manager.mPalantiriMap = mockPalantiriMap

        doReturn(true).whenever(mockPalantiriMap).replace(eq(mockPalantir), eq(true))
        doReturn(true).whenever(mockPalantiriMap).put(eq(mockPalantir), eq(true))
        val inOrder = inOrder(cancellableLockMock, mockPalantiriMap, semaphoreMock)

        // SUT
        manager.release(mockPalantir)

        inOrder.verify(cancellableLockMock).lock(any())
        try {
            inOrder.verify(mockPalantiriMap).replace(eq(mockPalantir), eq(true))
        } catch (t: Throwable) {
            inOrder.verify(mockPalantiriMap).put(eq(mockPalantir), eq(true))
        }
        inOrder.verify(cancellableLockMock).unlock()
        verify(semaphoreMock, never()).release()
    }

    @Test
    fun `release all acquired palantiri`() {
        lockAllPalantiri()
        doNothing().whenever(cancellableLockMock).lock(any())
        doNothing().whenever(cancellableLockMock).unlock()

        // SUT
        palantiri.forEach { manager.release(it) }

        val inOrder = inOrder(cancellableLockMock)
        verify(cancellableLockMock, times(PALANTIRI_COUNT)).lock(any())
        verify(cancellableLockMock, times(PALANTIRI_COUNT)).unlock()
        inOrder.verify(cancellableLockMock).lock(any())
        inOrder.verify(cancellableLockMock).unlock()
        val unlocked = palantiriMap.values.count { it }
        assertEquals("All $PALANTIRI_COUNT Palantiri should be unlocked.",
                PALANTIRI_COUNT, unlocked)
    }

    @Test
    fun `release does not call unlock if lock fails`() {
        doThrow(SimulatedException()).whenever(cancellableLockMock).lock(any())
        doCallRealMethod().whenever(managerMock).release(any())

        val palantir = Palantir(managerMock)

        // SUT
        assertThrows<SimulatedException>("Exception should have been thrown") {
            managerMock.release(palantir)
        }

        verify(managerMock).release(palantir)
        verify(cancellableLockMock).lock(any())
        verifyNoMoreInteractions(managerMock, cancellableLockMock)
    }

    private fun lockAllPalantiri() {
        // Lock all but the last Palantir in the Map.
        for (i in 0 until PALANTIRI_COUNT) {
            val palantir = palantiri[i]
            palantiriMap[palantir] = false
        }
    }

    private fun unlockPalantir(palantir: Palantir) {
        palantiriMap[palantir] = true
    }

    private fun lockPalantir(palantir: Palantir) {
        palantiriMap[palantir] = false
    }

    companion object {
        private const val PALANTIRI_COUNT = 5
    }
}
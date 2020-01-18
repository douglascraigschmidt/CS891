package edu.vandy.simulator.managers.palantiri.spinLockHashMap;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import admin.AssignmentTestRule;
import edu.vandy.simulator.managers.palantiri.Palantir;
import edu.vandy.simulator.utils.Assignment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class Assignment_1B_SpinLockHashMapMgrTest {
    // Model parameters.
    private final static int PALANTIRI_COUNT = 5;

    // The tests in this file only test the spin lock manager and not the
    // spin lock implementations (SpinLock and ReentrantSpinLock), so we can
    // just use an empty mockable CancellableLock interface since no student
    // implementation methods should ever be called.
    @Mock
    private CancellableLock mCancellableLockMock;

    @Mock
    private Semaphore mSemaphoreMock;

    @InjectMocks
    private SpinLockHashMapMgr mManager;

    // In order to put mock entries in this map, it can't be a mock.
    private HashMap<Palantir, Boolean> mPalantiriMap = new HashMap<>(PALANTIRI_COUNT);

    // In order to put mock entries in this list, it can't be a mock.
    private List<Palantir> mPalantiri;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public AssignmentTestRule assignmentTestRule = new AssignmentTestRule();

    @Before
    public void setup() {
        mPalantiri =
                IntStream.rangeClosed(1, PALANTIRI_COUNT)
                        .mapToObj(unused -> mock(Palantir.class))
                        .collect(Collectors.toList());
        mPalantiri.forEach(palantir -> mPalantiriMap.put(palantir, true));

        // mPalantiriMap and mPalantiri can't be mocked themselves,
        // only their contents can be mocked.
        mManager.mPalantiriMap = mPalantiriMap;
        mManager.mPalantiri = mPalantiri;
    }

    @Test
    public void buildModelUndergraduate() {
        if (!Assignment.testType(Assignment.UNDERGRADUATE)) {
            return;
        }

        buildModel();
    }

    @Test
    public void buildModelGraduate() {
        if (!Assignment.testType(Assignment.GRADUATE)) {
            return;
        }

        buildModel();
    }

    private void buildModel() {
        // Note that the buildModel method does not use the
        // SpinLockHashMapMgr created in the @Before setup
        // method because it needs to test the real Semaphore,
        // SpinLock, and Map fields for proper initialization.
        SpinLockHashMapMgr manager = mock(SpinLockHashMapMgr.class);
        List<Palantir> mockPalantiri =
                IntStream.rangeClosed(1, PALANTIRI_COUNT)
                        .mapToObj(unused -> mock(Palantir.class))
                        .collect(Collectors.toList());

        when(manager.getPalantiri()).thenReturn(mockPalantiri);

        doCallRealMethod().when(manager).buildModel();
        doCallRealMethod().when(manager).getAvailablePalantiri();
        doCallRealMethod().when(manager).getSpinLock();
        doCallRealMethod().when(manager).getPalantiriMap();

        // Call SUT method.
        manager.buildModel();

        if (Assignment.isUndergraduateTodo()) {
            assertTrue(manager.getSpinLock() instanceof SpinLock);
        } else if (Assignment.isGraduateTodo()) {
            assertTrue(manager.getSpinLock() instanceof ReentrantSpinLock);
        }

        assertNotNull(
                "getAvailablePalantiri() should not return null.",
                manager.getAvailablePalantiri());

        assertEquals(
                "The available palantiri semaphore should 0 permits.",
                0,
                manager.getAvailablePalantiri().getQueueLength());

        assertNotNull(
                "getSpinLock() accessor should not return null.",
                manager.getSpinLock());

        assertNotNull(
                "getPalantiriMap() should not return null.",
                manager.getPalantiriMap());

        assertEquals(
                "getPalantiriMap() should contain " + PALANTIRI_COUNT + " entries.",
                PALANTIRI_COUNT,
                manager.getPalantiriMap().size());
    }

    /**
     * Uses mManager instance created in the @Before setup method.
     */
    @Test
    public void testAcquireWithAllPalantiriAvailable() throws InterruptedException {
        doNothing().when(mCancellableLockMock).lock(any());
        doNothing().when(mCancellableLockMock).unlock();
        doNothing().when(mSemaphoreMock).acquire();

        Palantir palantir = mManager.acquire();

        assertNotNull("Acquire should return a non-null Palantir", palantir);
        long locked =
                mPalantiriMap.values()
                        .stream()
                        .filter(b -> !b)
                        .count();
        assertEquals("Only 1 palantir should be locked", 1, locked);

        verify(mSemaphoreMock).acquire();
        verify(mCancellableLockMock).lock(any());
        verify(mCancellableLockMock).lock(any());
    }

    /**
     * Uses mManager instance created in the @Before setup method.
     */
    @Test
    public void testAcquireWithOnlyOnePalantiriAvailable() throws InterruptedException {
        // Lock all but on Palantir.
        lockAllPalantiri();
        Palantir unlockedPalantir = mPalantiri.get(PALANTIRI_COUNT - 1);
        unlockPalantir(unlockedPalantir);

        doNothing().when(mCancellableLockMock).lock(any());
        doNothing().when(mCancellableLockMock).unlock();
        doNothing().when(mSemaphoreMock).acquire();

        InOrder inOrder = inOrder(mCancellableLockMock, mSemaphoreMock);

        Palantir palantir = mManager.acquire();

        assertNotNull("Acquire should return a non-null Palantir", palantir);
        long lockedCount =
                mPalantiriMap.values()
                        .stream()
                        .filter(b -> !b)
                        .count();
        assertEquals(
                "All " + PALANTIRI_COUNT + " palantiri should be locked",
                PALANTIRI_COUNT,
                lockedCount);

        assertSame(
                "The only available Palantir should be returned",
                unlockedPalantir,
                palantir);

        verify(mSemaphoreMock).acquire();
        verify(mCancellableLockMock).lock(any());
        verify(mCancellableLockMock).unlock();

        inOrder.verify(mSemaphoreMock).acquire();
        inOrder.verify(mCancellableLockMock).lock(any());
        inOrder.verify(mCancellableLockMock).unlock();
    }

    /**
     * Uses mManager instance created in the @Before setup method.
     */
    @Test
    public void testAcquireAllAvailablePalantiri() throws InterruptedException {
        doNothing().when(mCancellableLockMock).lock(any());
        doNothing().when(mCancellableLockMock).unlock();
        doNothing().when(mSemaphoreMock).acquire();

        InOrder inOrder = inOrder(mCancellableLockMock, mSemaphoreMock);

        for (int i = 1; i <= PALANTIRI_COUNT; i++) {
            Palantir palantir = mManager.acquire();
            assertNotNull("Acquire should return a non-null Palantir", palantir);

            long lockedCount =
                    mPalantiriMap.values()
                            .stream()
                            .filter(b -> !b)
                            .count();
            assertEquals(
                    i + " palantiri should be acquired (locked).",
                    i,
                    lockedCount);
        }

        verify(mSemaphoreMock, times(PALANTIRI_COUNT)).acquire();
        verify(mCancellableLockMock, times(PALANTIRI_COUNT)).lock(any());
        verify(mCancellableLockMock, times(PALANTIRI_COUNT)).unlock();

        inOrder.verify(mSemaphoreMock).acquire();
        inOrder.verify(mCancellableLockMock).lock(any());
        inOrder.verify(mCancellableLockMock).unlock();
    }

    @Test
    public void testReleaseNullPalantir() {
        try {
            mManager.release(null);
        } catch (Exception e) {
            fail("Release should not throw an exception if a " +
                    "null Palantir is passed as a parameter.");
        }
    }

    @Test
    public void testReleaseOneAcquiredPalantir() {
        Palantir lockedPalantir = mPalantiri.get(PALANTIRI_COUNT - 1);
        lockPalantir(lockedPalantir);

        doNothing().when(mCancellableLockMock).lock(any());
        doNothing().when(mCancellableLockMock).unlock();

        mManager.release(lockedPalantir);

        verify(mCancellableLockMock).lock(any());
        assertTrue(
                "Released Palantir should not be locked in HashMap.",
                mPalantiriMap.get(lockedPalantir));
    }

    @Test
    public void testReleaseAllAcquiredPalantiri() {
        lockAllPalantiri();

        doNothing().when(mCancellableLockMock).lock(any());
        doNothing().when(mCancellableLockMock).unlock();

        mPalantiri.forEach(palantir -> mManager.release(palantir));

        InOrder inOrder = inOrder(mCancellableLockMock);

        verify(mCancellableLockMock, times(PALANTIRI_COUNT)).lock(any());
        verify(mCancellableLockMock, times(PALANTIRI_COUNT)).unlock();

        inOrder.verify(mCancellableLockMock).lock(any());
        inOrder.verify(mCancellableLockMock).unlock();

        long count = mPalantiriMap.values().stream()
                .filter(b -> b)
                .count();
        assertEquals(
                "All " + PALANTIRI_COUNT + " Palantiri should be unlocked.",
                PALANTIRI_COUNT,
                count);
    }

    private void lockAllPalantiri() {
        // Lock all but the last Palantir in the Map.
        for (int i = 0; i < PALANTIRI_COUNT; i++) {
            Palantir palantir = mPalantiri.get(i);
            mPalantiriMap.put(palantir, false);
        }
    }

    private void unlockPalantir(Palantir palantir) {
        mPalantiriMap.put(palantir, true);
    }

    private void lockPalantir(Palantir palantir) {
        mPalantiriMap.put(palantir, false);
    }
}
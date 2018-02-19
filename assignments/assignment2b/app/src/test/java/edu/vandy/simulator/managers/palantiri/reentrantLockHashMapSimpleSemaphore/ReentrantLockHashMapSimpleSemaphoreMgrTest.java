package edu.vandy.simulator.managers.palantiri.reentrantLockHashMapSimpleSemaphore;

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
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import edu.vandy.simulator.managers.palantiri.Palantir;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReentrantLockHashMapSimpleSemaphoreMgrTest {
    // Model parameters.
    private final static int PALANTIRI_COUNT = 5;

    @Mock
    private SimpleSemaphore mSemaphoreMock;

    @Mock
    private ReentrantLock mLockMock;

    @InjectMocks
    private ReentrantLockHashMapSimpleSemaphoreMgr mManager;

    // In order to put mock entries in this map, it can't be a mock.
    private HashMap<Palantir, Boolean> mPalantiriMap = new HashMap<>(PALANTIRI_COUNT);

    // In order to put mock entries in this list, it can't be a mock.
    private List<Palantir> mPalantiri;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

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
    public void buildModel() {
        // Note that the buildModel method does not use the
        // mManager created in the @Before setup
        // method because it needs to test the real Semaphore,
        // ReentrantLock, and HashMap fields for proper initialization.
        ReentrantLockHashMapSimpleSemaphoreMgr manager =
                mock(ReentrantLockHashMapSimpleSemaphoreMgr.class);
        List<Palantir> mockPalantiri =
                IntStream.rangeClosed(1, PALANTIRI_COUNT)
                        .mapToObj(unused -> mock(Palantir.class))
                        .collect(Collectors.toList());

        when(manager.getPalantiri()).thenReturn(mockPalantiri);

        doCallRealMethod().when(manager).buildModel();

        // Call SUT method.
        manager.buildModel();

        assertNotNull(
                "getAvailablePalantiri() should not return null.",
                manager.mAvailablePalantiri);

        assertNotNull(
                "mLock should not be null.",
                manager.mLock);

        assertNotNull(
                "mPalantiriMap should not be null.",
                manager.mPalantiriMap);

        assertEquals(
                "getPalantiriMap() should contain " + PALANTIRI_COUNT + " entries.",
                PALANTIRI_COUNT,
                manager.mPalantiriMap.size());
    }

    /**
     * Uses mManager instance created in the @Before setup method.
     */
    @Test
    public void testAcquireWithAllPalantiriAvailable() throws InterruptedException {
        doNothing().when(mLockMock).lock();
        doNothing().when(mLockMock).unlock();
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
        verify(mLockMock).lock();
        verify(mLockMock).lock();
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

        doNothing().when(mLockMock).lock();
        doNothing().when(mLockMock).unlock();
        doNothing().when(mSemaphoreMock).acquire();

        InOrder inOrder = inOrder(mLockMock, mSemaphoreMock);

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
        verify(mLockMock).lock();
        verify(mLockMock).unlock();

        inOrder.verify(mSemaphoreMock).acquire();
        inOrder.verify(mLockMock).lock();
        inOrder.verify(mLockMock).unlock();
    }

    /**
     * Uses mManager instance created in the @Before setup method.
     */
    @Test
    public void testAcquireAllAvailablePalantiri() throws InterruptedException {
        doNothing().when(mLockMock).lock();
        doNothing().when(mLockMock).unlock();
        doNothing().when(mSemaphoreMock).acquire();

        InOrder inOrder = inOrder(mLockMock, mSemaphoreMock);

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
        verify(mLockMock, times(PALANTIRI_COUNT)).lock();
        verify(mLockMock, times(PALANTIRI_COUNT)).unlock();

        inOrder.verify(mSemaphoreMock).acquire();
        inOrder.verify(mLockMock).lock();
        inOrder.verify(mLockMock).unlock();
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

        doNothing().when(mLockMock).lock();
        doNothing().when(mLockMock).unlock();

        mManager.release(lockedPalantir);

        verify(mLockMock).lock();
        assertTrue(
                "Released Palantir should not be locked in HashMap.",
                mPalantiriMap.get(lockedPalantir));
    }

    @Test
    public void testReleaseAllAcquiredPalantiri() {
        lockAllPalantiri();

        doNothing().when(mLockMock).lock();
        doNothing().when(mLockMock).unlock();

        mPalantiri.forEach(palantir -> mManager.release(palantir));

        InOrder inOrder = inOrder(mLockMock);

        verify(mLockMock, times(PALANTIRI_COUNT)).lock();
        verify(mLockMock, times(PALANTIRI_COUNT)).unlock();

        inOrder.verify(mLockMock).lock();
        inOrder.verify(mLockMock).unlock();

        long count = mPalantiriMap.values().stream()
                .filter(b -> b)
                .count();
        assertEquals(
                "All " + PALANTIRI_COUNT + " Palantiri should be unlocked.",
                PALANTIRI_COUNT,
                count);
    }

    private void lockAllPalantiri() {
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
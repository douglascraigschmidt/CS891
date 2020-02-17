package edu.vandy.simulator.managers.palantiri.reentrantLockHashMapSimpleSemaphore;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import admin.AssignmentTestRule;
import admin.AssignmentTests;
import admin.ReflectionHelper;
import edu.vandy.simulator.managers.palantiri.Palantir;
import edu.vandy.simulator.utils.Assignment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class Assignment_2B_ReentrantLockHashMapSimpleSemaphoreMgrTest extends AssignmentTests {
    private final static int PALANTIRI_COUNT = 5;

    @Mock
    Stream<Palantir> mStreamPalantirMock;
    @Mock
    Optional<Palantir> mOptionalPalantirMock;
    @Mock
    Palantir mPalantirMock;
    @Mock
    private SimpleSemaphore mSemaphoreMock;
    @Mock
    private Map<Palantir, Boolean> mPalantiriMapMock;
    @Mock
    private Set<Map.Entry<Palantir, Boolean>> mEntrySetMock;
    @Mock
    private Stream<Map.Entry<Palantir, Boolean>> mStreamEntrySetMock;
    @Mock
    private List<Palantir> mPalantiriListMock;
    @Mock
    private Stream<Palantir> mStreamPalantiriListMock;

    @InjectMocks
    private ReentrantLockHashMapSimpleSemaphoreMgr mManager;
    // In order to put mock entries in this map, it can't be a mock.
    private HashMap<Palantir, Boolean> mPalantiriMap = new HashMap<>(PALANTIRI_COUNT);
    // In order to put mock entries in this list, it can't be a mock.
    private List<Palantir> mPalantiri;

    private ReentrantLock mLockMock;

    @Before
    public void before() throws IllegalAccessException {
        mPalantiri =
                IntStream.rangeClosed(1, PALANTIRI_COUNT)
                        .mapToObj(unused -> mock(Palantir.class))
                        .collect(Collectors.toList());
        mPalantiri.forEach(palantir -> mPalantiriMap.put(palantir, true));

        // mPalantiriMap and mPalantiri can't be mocked themselves,
        // only their contents can be mocked.
        mManager.mPalantiriMap = mPalantiriMap;
        mManager.mPalantiri = mPalantiri;

        // Handles the case where the user has declared the lock field as
        // either Lock or ReentrantLock.
        mLockMock = mock(ReentrantLock.class);
        Field field = ReflectionHelper.findFirstMatchingField(mManager, ReentrantLock.class);
        if (field != null) {
            field.set(mManager, mLockMock);
        } else {
            field = ReflectionHelper.findFirstMatchingField(mManager, Lock.class);
            field.set(mManager, mLockMock);
        }
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
        doCallRealMethod().when(manager).getSemaphore();

        // Call SUT method.
        manager.buildModel();

        assertEquals(PALANTIRI_COUNT, manager.getSemaphore().availablePermits());

        try {
            Lock lock = ReflectionHelper.findFirstMatchingFieldValue(manager, ReentrantLock.class);
            assertNotNull(
                    "Lock field should exist and not be null.",
                    lock);
        } catch (Throwable t) {
            Lock lock = ReflectionHelper.findFirstMatchingFieldValue(manager, Lock.class);
            assertNotNull(
                    "Lock field should exist and not be null.",
                    lock);
        }

        SimpleSemaphore availablePalantiri =
                ReflectionHelper.findFirstMatchingFieldValue(manager, SimpleSemaphore.class);
        assertNotNull(
                "SimpleSemaphore field should exist and not be null",
                availablePalantiri);

        assertNotNull(
                "mPalantiriMap should not be null.",
                manager.mPalantiriMap);

        assertEquals(
                "getPalantiriMap() should contain " + PALANTIRI_COUNT + " entries.",
                PALANTIRI_COUNT,
                manager.mPalantiriMap.size());
    }

    /**
     * Tests GRADUATE students use of Java 8 streams.
     */
    @Test
    public void buildModelGraduate() {
        if (!Assignment.testType(Assignment.GRADUATE)) {
            System.out.println("Skipping buildModelGraduate (graduate only test)");
            return;
        }

        // Note that the buildModel method does not use the
        // managerMock created in the @Before setup
        // method because it needs to test the real Semaphore,
        // ReentrantLock, and HashMap fields for proper initialization.
        ReentrantLockHashMapSimpleSemaphoreMgr managerMock =
                mock(ReentrantLockHashMapSimpleSemaphoreMgr.class);

        InOrder inOrder = inOrder(managerMock, mPalantiriListMock, mStreamPalantiriListMock);

        when(managerMock.getPalantiri()).thenReturn(mPalantiriListMock);
        when(mPalantiriListMock.stream()).thenReturn(mStreamPalantiriListMock);
        when(mPalantiriListMock.size()).thenReturn(PALANTIRI_COUNT);
        when(mStreamPalantiriListMock.collect(any())).thenReturn(mPalantiriMapMock);

        when(managerMock.getPalantiri()).thenReturn(mPalantiriListMock);

        doCallRealMethod().when(managerMock).buildModel();

        // Call SUT method.
        managerMock.buildModel();

        verify(managerMock, times(1)).getPalantiri();
        verify(mPalantiriListMock, times(1)).stream();
        verify(mStreamPalantiriListMock, times(1)).collect(any());

        inOrder.verify(managerMock).getPalantiri();
        inOrder.verify(mPalantiriListMock).stream();
        inOrder.verify(mStreamPalantiriListMock).collect(any());
    }

    @Test
    public void testGetSemaphore() {
        ReentrantLockHashMapSimpleSemaphoreMgr manager =
                mock(ReentrantLockHashMapSimpleSemaphoreMgr.class);
        List<Palantir> mockPalantiri =
                IntStream.rangeClosed(1, PALANTIRI_COUNT)
                        .mapToObj(unused -> mock(Palantir.class))
                        .collect(Collectors.toList());

        when(manager.getPalantiri()).thenReturn(mockPalantiri);

        doCallRealMethod().when(manager).buildModel();

        // Call SUT method.
        try {
            manager.buildModel();
        } catch (Exception e) {
            // Don't care if buildModel fails because it's only
            // called to ensure that a SimpleSemaphore field has
            // be initialized.
        }

        doCallRealMethod().when(manager).getSemaphore();
        SimpleSemaphore simpleSemaphore = manager.getSemaphore();
        assertNotNull("getSemaphore() should return a non-null SimpleSemaphore value.", simpleSemaphore);

        SimpleSemaphore availablePalantiri =
                ReflectionHelper.findFirstMatchingFieldValue(manager, SimpleSemaphore.class);
        assertSame("getSemaphore() should return the class SimpleSemaphore field value.",
                availablePalantiri, simpleSemaphore);
    }

    /**
     * Uses mManager instance created in the @Before setup method.
     */
    @Test
    public void testAcquireWithAllPalantiriAvailable() throws InterruptedException {
        Palantir palantir = mManager.acquire();

        assertNotNull("Acquire should return a non-null Palantir", palantir);
        long locked =
                mPalantiriMap.values()
                        .stream()
                        .filter(b -> !b)
                        .count();
        assertEquals("Only 1 palantir should be locked", 1, locked);

        verify(mSemaphoreMock).acquire();

        try {
            verify(mLockMock).lock();
        } catch (Throwable t) {
            verify(mLockMock).lockInterruptibly();
        }

        verify(mLockMock).unlock();
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
        try {
            verify(mLockMock).lock();
        } catch (Throwable t) {
            verify(mLockMock).lockInterruptibly();
        }
        verify(mLockMock).unlock();

        inOrder.verify(mSemaphoreMock).acquire();
        try {
            inOrder.verify(mLockMock).lock();
        } catch (Throwable t) {
            inOrder.verify(mLockMock).lockInterruptibly();
        }
        inOrder.verify(mLockMock).unlock();
    }

    /**
     * Uses mManager instance created in the @Before setup method.
     */
    @Test
    public void testAcquireAllAvailablePalantiri() throws InterruptedException {
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
        verify(mLockMock, times(PALANTIRI_COUNT)).lockInterruptibly();
        verify(mLockMock, times(PALANTIRI_COUNT)).unlock();

        inOrder.verify(mSemaphoreMock).acquire();
        inOrder.verify(mLockMock).lockInterruptibly();
        inOrder.verify(mLockMock).unlock();
    }

    /**
     * Tests GRADUATE students use of Java 8 streams.
     */
    @Test
    public void testAcquireAllAvailablePalantiriGraduate() throws InterruptedException {
        if (!Assignment.testType(Assignment.GRADUATE)) {
            System.out.println("Skipping testAcquireAllAvailablePalantiriGraduate (graduate only test)");
            return;
        }

        InOrder inOrder =
                inOrder(mPalantiriMapMock,
                        mEntrySetMock,
                        mStreamEntrySetMock,
                        mStreamPalantirMock,
                        mOptionalPalantirMock);

        when(mPalantiriMapMock.entrySet()).thenReturn(mEntrySetMock);
        when(mEntrySetMock.stream()).thenReturn(mStreamEntrySetMock);
        Predicate<Map.Entry<Palantir, Boolean>> anyFunction = any();
        when(mStreamEntrySetMock.filter(anyFunction)).thenReturn(mStreamEntrySetMock);
        Function<Map.Entry<Palantir, Boolean>, Palantir> mapFunction = any();
        when(mStreamEntrySetMock.map(mapFunction)).thenReturn(mStreamPalantirMock);
        when(mStreamPalantirMock.findFirst()).thenReturn(mOptionalPalantirMock);
        when(mStreamPalantirMock.findAny()).thenReturn(mOptionalPalantirMock);
        when(mOptionalPalantirMock.orElse(any())).thenReturn(mPalantirMock);

        mManager.mPalantiriMap = mPalantiriMapMock;

        // Call SUT.
        Palantir palantir = mManager.acquire();

        assertEquals(mPalantirMock, palantir);

        verify(mPalantiriMapMock, times(1)).entrySet();
        verify(mEntrySetMock, times(1)).stream();
        verify(mStreamEntrySetMock, times(1)).filter(any());
        verify(mStreamEntrySetMock, times(1)).map(any());
        try {
            verify(mStreamPalantirMock, times(1)).findFirst();
        } catch (Exception e) {
            verify(mStreamPalantirMock, times(1)).findAny();
        }

        inOrder.verify(mPalantiriMapMock).entrySet();
        inOrder.verify(mEntrySetMock).stream();
        inOrder.verify(mStreamEntrySetMock).filter(any());
        inOrder.verify(mStreamEntrySetMock).map(any());
        try {
            inOrder.verify(mStreamPalantirMock).findFirst();
        } catch (Exception e) {
            inOrder.verify(mStreamPalantirMock).findAny();
        }
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
    public void testReleaseOneAcquiredPalantir() throws InterruptedException {
        Palantir lockedPalantir = mPalantiri.get(PALANTIRI_COUNT - 1);
        lockPalantir(lockedPalantir);

        mManager.release(lockedPalantir);

        try {
            verify(mLockMock).lock();
        } catch (Throwable t) {
            verify(mLockMock).lockInterruptibly();
        }
        assertTrue(
                "Released Palantir should not be locked in HashMap.",
                mPalantiriMap.get(lockedPalantir));
    }

    @Test
    public void testReleaseAllAcquiredPalantiri() throws InterruptedException {
        lockAllPalantiri();

        mPalantiri.forEach(palantir -> mManager.release(palantir));

        InOrder inOrder = inOrder(mLockMock);

        try {
            verify(mLockMock, times(PALANTIRI_COUNT)).lock();
        } catch (Throwable t) {
            verify(mLockMock, times(PALANTIRI_COUNT)).lockInterruptibly();
        }
        verify(mLockMock, times(PALANTIRI_COUNT)).unlock();

        try {
            inOrder.verify(mLockMock).lock();
        } catch (Throwable t) {
            inOrder.verify(mLockMock).lockInterruptibly();
        }
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

    private class LockCounter {
        int lock = 0;
        int lockInterruptibly = 0;
    }
}
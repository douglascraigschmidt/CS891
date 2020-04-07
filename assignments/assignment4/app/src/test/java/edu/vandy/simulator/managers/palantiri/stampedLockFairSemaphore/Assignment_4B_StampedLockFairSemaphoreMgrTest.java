package edu.vandy.simulator.managers.palantiri.stampedLockFairSemaphore;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import admin.AssignmentTests;
import admin.ReflectionHelper;
import edu.vandy.simulator.managers.palantiri.Palantir;
import edu.vandy.simulator.managers.palantiri.concurrentMapFairSemaphore.FairSemaphore;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class Assignment_4B_StampedLockFairSemaphoreMgrTest extends AssignmentTests {
    private final static int PALANTIRI_COUNT = 5;
    // In order to put mock entries in this map, it can't be a mock.
    private HashMap<Palantir, Boolean> mPalantiriMap = new HashMap<>(PALANTIRI_COUNT);
    // In order to put mock entries in this list, it can't be a mock.
    private List<Palantir> mPalantiri;

    @Mock
    private FairSemaphore mSemaphoreMock;
    @Mock
    private StampedLock mLockMock;
    @Mock
    private HashMap<Palantir, Boolean> mPalantiriMapMock;
    @InjectMocks
    private StampedLockFairSemaphoreMgr mManager;
    
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
        StampedLockFairSemaphoreMgr manager =
                mock(StampedLockFairSemaphoreMgr.class);
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

        ReflectionHelper.assertAnonymousFieldNotNull(manager, StampedLock.class);

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
        final long stamp = System.currentTimeMillis();
        lenient().when(mLockMock.readLock()).thenReturn(stamp);
        lenient().doNothing().when(mLockMock).unlock(stamp);
        doNothing().when(mSemaphoreMock).acquire();
        when(mLockMock.tryConvertToWriteLock(anyLong()))
                .thenAnswer(invocation -> {
                    assertEquals("tryConvertToWriteLock called with wrong stamp value",
                            stamp,
                            (long) invocation.getArgument(0));
                    return stamp;
                });

        // There are many possible solutions involving different permutations
        // on calls to unlock, unlockRead, and unlockWrite. To handle all possible
        // permutations, a special UnlockCounter class instance is used to track
        // the count of calls to these 3 methods.
        final UnlockCounter unlockCounter = new UnlockCounter();
        lenient().doAnswer(invocation -> {
            unlockCounter.unlock++;
            return null;
        }).when(mLockMock).unlock(anyLong());
        lenient().doAnswer(invocation -> {
            unlockCounter.unlockWrite++;
            return null;
        }).when(mLockMock).unlockWrite(anyLong());

        InOrder inOrder = inOrder(mLockMock, mSemaphoreMock);

        Palantir palantir = mManager.acquire();

        assertNotNull("Acquire should return a non-null Palantir", palantir);
        long locked =
                mPalantiriMap.values()
                        .stream()
                        .filter(b -> !b)
                        .count();
        assertEquals("Only 1 palantir should be locked", 1, locked);

        verify(mSemaphoreMock).acquire();
        verify(mLockMock).readLock();
        verify(mLockMock).tryConvertToWriteLock(stamp);
        if (unlockCounter.unlock + unlockCounter.unlockWrite != 1) {
            fail("Either unlock or unlockWrite should have been called exactly once.");
        }

        inOrder.verify(mSemaphoreMock).acquire();
        inOrder.verify(mLockMock).readLock();
        inOrder.verify(mLockMock).tryConvertToWriteLock(stamp);
        if (unlockCounter.unlock != 0) {
            inOrder.verify(mLockMock).unlock(stamp);
        }
        if (unlockCounter.unlockWrite != 0) {
            inOrder.verify(mLockMock).unlockWrite(stamp);
        }
    }

    /**
     * Uses mManager instance created in the @Before setup method.
     */
    @Test
    public void testAcquireWithOnePalantiriAvailable() throws InterruptedException {
        // Lock all but the last Palantir.
        lockAllPalantiri();
        Palantir unlockedPalantir = mPalantiri.get(PALANTIRI_COUNT - 1);
        unlockPalantir(unlockedPalantir);

        final long stamp = System.currentTimeMillis();

        when(mLockMock.readLock()).thenReturn(stamp);
        doNothing().when(mSemaphoreMock).acquire();
        when(mLockMock.tryConvertToWriteLock(anyLong()))
                .thenAnswer(invocation -> {
                    assertEquals("tryConvertToWriteLock called with wrong stamp value",
                            stamp,
                            (long) invocation.getArgument(0));
                    return stamp;
                });

        // There are many possible solutions involving different permutations
        // on calls to unlock, unlockRead, and unlockWrite. To handle all possible
        // permutations, a special UnlockCounter class instance is used to track
        // the count of calls to these 3 methods.
        final UnlockCounter unlockCounter = new UnlockCounter();
        lenient().doAnswer(invocation -> {
            unlockCounter.unlock++;
            return null;
        }).when(mLockMock).unlock(anyLong());
        lenient().doAnswer(invocation -> {
            unlockCounter.unlockWrite++;
            return null;
        }).when(mLockMock).unlockWrite(anyLong());

        InOrder inOrder = inOrder(mLockMock, mSemaphoreMock);

        Palantir palantir = mManager.acquire();
        assertNotNull("Acquire should return a non-null Palantir", palantir);
        assertSame(
                "The only available Palantir should be returned",
                unlockedPalantir,
                palantir);

        long locked =
                mPalantiriMap.values()
                        .stream()
                        .filter(b -> !b)
                        .count();
        Assert.assertEquals(
                "All " + PALANTIRI_COUNT + " palantiri should be locked",
                PALANTIRI_COUNT,
                locked);

        verify(mSemaphoreMock).acquire();
        verify(mLockMock).readLock();
        verify(mLockMock).tryConvertToWriteLock(stamp);
        if (unlockCounter.unlock + unlockCounter.unlockWrite != 1) {
            fail("Either unlock or unlockWrite should have been called exactly once.");
        }

        inOrder.verify(mSemaphoreMock).acquire();
        inOrder.verify(mLockMock).readLock();
        inOrder.verify(mLockMock).tryConvertToWriteLock(stamp);
        if (unlockCounter.unlock != 0) {
            inOrder.verify(mLockMock).unlock(stamp);
        }
        if (unlockCounter.unlockWrite != 0) {
            inOrder.verify(mLockMock).unlockWrite(stamp);
        }
    }

    /**
     * Uses mManager instance created in the @Before setup method.
     */
    @Test
    public void testAcquireAllAvailablePalantiri() throws InterruptedException {
        final long stamp = System.currentTimeMillis();
        when(mLockMock.readLock()).thenReturn(stamp);
        doNothing().when(mSemaphoreMock).acquire();
        when(mLockMock.tryConvertToWriteLock(anyLong()))
                .thenAnswer(invocation -> {
                    assertEquals("tryConvertToWriteLock called with wrong stamp value",
                            stamp,
                            (long) invocation.getArgument(0));
                    return stamp;
                });
        // There are many possible solutions involving different permutations
        // on calls to unlock, unlockRead, and unlockWrite. To handle all possible
        // permutations, a special UnlockCounter class instance is used to track
        // the count of calls to these 3 methods.
        final UnlockCounter unlockCounter = new UnlockCounter();
        lenient().doAnswer(invocation -> {
            unlockCounter.unlock++;
            return null;
        }).when(mLockMock).unlock(anyLong());
        lenient().doAnswer(invocation -> {
            unlockCounter.unlockWrite++;
            return null;
        }).when(mLockMock).unlockWrite(anyLong());

        InOrder inOrder = inOrder(mLockMock, mSemaphoreMock);

        for (int i = 1; i <= PALANTIRI_COUNT; i++) {
            Palantir palantir = mManager.acquire();
            assertNotNull("Acquire should return a non-null Palantir", palantir);

            long lockedCount =
                    mPalantiriMap.values()
                            .stream()
                            .filter(b -> !b)
                            .count();
            Assert.assertEquals(
                    i + " palantiri should be acquired (locked).",
                    i,
                    lockedCount);
        }

        verify(mSemaphoreMock, times(PALANTIRI_COUNT)).acquire();
        verify(mLockMock, times(PALANTIRI_COUNT)).readLock();
        verify(mLockMock, times(PALANTIRI_COUNT)).tryConvertToWriteLock(stamp);
        if (unlockCounter.unlock + unlockCounter.unlockWrite != PALANTIRI_COUNT) {
            fail("unlock or unlockWrite should have been called exactly " + PALANTIRI_COUNT + " times.");
        }

        verify(mLockMock, never()).unlockRead(anyLong());
        verify(mLockMock, never()).writeLock();
        verify(mLockMock, never()).writeLockInterruptibly();

        inOrder.verify(mSemaphoreMock).acquire();
        inOrder.verify(mLockMock).readLock();
        inOrder.verify(mLockMock).tryConvertToWriteLock(stamp);
        if (unlockCounter.unlock != 0) {
            inOrder.verify(mLockMock).unlock(stamp);
        }
        if (unlockCounter.unlockWrite != 0) {
            inOrder.verify(mLockMock).unlockWrite(stamp);
        }
    }

    /**
     * Uses mManager instance created in the @Before setup method.
     */
    @Test
    public void testAcquireThatRequiresUpgradeToWriteLock() throws InterruptedException {
        final long stamp = System.currentTimeMillis();
        lenient().when(mLockMock.readLock()).thenReturn(stamp);
        lenient().doNothing().when(mLockMock).unlock(stamp);
        doNothing().when(mSemaphoreMock).acquire();
        when(mLockMock.writeLock()).thenReturn(stamp);
        when(mLockMock.tryConvertToWriteLock(anyLong()))
                // First time: FAIL upgrade to write lock.
                .thenAnswer(invocation -> {
                    assertEquals("tryConvertToWriteLock called with wrong stamp value",
                            stamp,
                            (long) invocation.getArgument(0));
                    return 0L;
                })
                // Second time: SUCCEED upgrade to write lock.
                .thenAnswer(invocation -> {
                    assertEquals("tryConvertToWriteLock called with wrong stamp value",
                            stamp,
                            (long) invocation.getArgument(0));
                    return stamp;
                });


        // There are many possible solutions involving different permutations
        // on calls to unlock, unlockRead, and unlockWrite. To handle all possible
        // permutations, a special UnlockCounter class instance is used to track
        // the count of calls to these 3 methods.
        final UnlockCounter unlockCounter = new UnlockCounter();
        doAnswer(invocation -> {
            unlockCounter.unlock++;
            return null;
        }).when(mLockMock).unlock(anyLong());
        doAnswer(invocation -> {
            unlockCounter.unlockRead++;
            return null;
        }).when(mLockMock).unlockRead(anyLong());
        lenient().doAnswer(invocation -> {
            unlockCounter.unlockWrite++;
            return null;
        }).when(mLockMock).unlockWrite(anyLong());

        InOrder inOrder = inOrder(mLockMock, mSemaphoreMock);

        Palantir palantir = mManager.acquire();

        assertNotNull("Acquire should return a non-null Palantir", palantir);
        long locked =
                mPalantiriMap.values()
                        .stream()
                        .filter(b -> !b)
                        .count();
        assertEquals("Only 1 palantir should be locked", 1, locked);

        verify(mSemaphoreMock, times(1)).acquire();
        verify(mLockMock, times(1)).readLock();
        verify(mLockMock, times(2)).tryConvertToWriteLock(stamp);
        verify(mLockMock, times(1)).writeLock();

        // An optimal solution should include a single call to unlockRead and a
        // single call to unlock, but another viable solution is to 2 calls to unlock.
        // Note that a 3rd possible solution is to call unlockRead followed by unlockWrite
        // but this is considered as being very unlikely and would also require an extra
        // boolean to keep track of whether unlockRead or unlockWrite should be called and
        // this is a sub-optimal solution and is therefore considered an error.
        if (unlockCounter.unlock + unlockCounter.unlockRead != 2) {
            fail("Expected 1 call to StampedLock unlockRead() and 1 call to unlock().");
        } else if (unlockCounter.unlockRead == 2) {
            fail("Expected only a single call to either either StampedLock unlockRead() or unlock().");
        }

        inOrder.verify(mSemaphoreMock).acquire();
        inOrder.verify(mLockMock).readLock();
        inOrder.verify(mLockMock).tryConvertToWriteLock(stamp);
        inOrder.verify(mLockMock).writeLock();

        if (unlockCounter.unlock > 0) {
            inOrder.verify(mLockMock).unlock(stamp);
        } else {
            inOrder.verify(mLockMock).unlockRead(stamp);
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
    public void releaseInUsePalantiri() throws IllegalAccessException {
        Palantir palantir = mock(Palantir.class);

        final long stamp = System.currentTimeMillis();
        when(mLockMock.writeLock()).thenReturn(stamp);
        when(mPalantiriMapMock.put(any(Palantir.class), anyBoolean())).thenReturn(false);
        ReflectionHelper.injectValueIntoFirstMatchingField(mManager, mPalantiriMapMock, Map.class);

        InOrder inOrder = inOrder(mLockMock, mSemaphoreMock, mPalantiriMapMock);
        final UnlockCounter unlockCounter = new UnlockCounter();
        lenient().doAnswer(invocation -> {
            unlockCounter.unlock++;
            return null;
        }).when(mLockMock).unlock(anyLong());
        lenient().doAnswer(invocation -> {
            unlockCounter.unlockWrite++;
            return null;
        }).when(mLockMock).unlockWrite(anyLong());

        mManager.release(palantir);

        verify(mPalantiriMapMock, times(1)).put(palantir, true);
        verify(mSemaphoreMock, times(1)).release();

        if (unlockCounter.unlock + unlockCounter.unlockWrite == 0) {
            fail("Expected call to either StampedLock unlockWrite() or unlock().");
        } else if (unlockCounter.unlock + unlockCounter.unlockWrite > 1) {
            fail("Expected only a single call to either either StampedLock unlockWrite() or unlock().");
        }

        inOrder.verify(mLockMock).writeLock();
        inOrder.verify(mPalantiriMapMock).put(palantir, true);
        if (unlockCounter.unlock > 0) {
            inOrder.verify(mLockMock).unlock(stamp);
        } else {
            inOrder.verify(mLockMock).unlockWrite(stamp);
        }
        inOrder.verify(mSemaphoreMock).release();
    }

    @Test
    public void releaseNotInUsePalantiri() throws IllegalAccessException {
        Palantir palantir = mock(Palantir.class);

        final long stamp = System.currentTimeMillis();
        when(mLockMock.writeLock()).thenReturn(stamp);
        when(mPalantiriMapMock.put(any(Palantir.class), anyBoolean())).thenReturn(true);
        ReflectionHelper.injectValueIntoFirstMatchingField(mManager, mPalantiriMapMock, Map.class);

        InOrder inOrder = inOrder(mLockMock, mSemaphoreMock, mPalantiriMapMock);

        // This is a trick to handle
        final UnlockCounter unlockWriteCounter = new UnlockCounter();
        lenient().doAnswer(invocation -> {
            unlockWriteCounter.unlock++;
            return null;
        }).when(mLockMock).unlock(anyLong());
        lenient().doAnswer(invocation -> {
            unlockWriteCounter.unlockWrite++;
            return null;
        }).when(mLockMock).unlockWrite(anyLong());

        mManager.release(palantir);

        // Trick to handle either unlockWrite() or unlock() as
        // a valid solution.
        verify(mLockMock, times(1)).writeLock();
        verify(mPalantiriMapMock, times(1)).put(palantir, true);
        verify(mSemaphoreMock, never()).release();

        if (unlockWriteCounter.unlock + unlockWriteCounter.unlockWrite == 0) {
            fail("Expected call to either StampedLock unlockWrite() or unlock().");
        } else if (unlockWriteCounter.unlock + unlockWriteCounter.unlockWrite > 1) {
            fail("Expected only a single call to either either StampedLock unlockWrite() or unlock().");
        }

        inOrder.verify(mLockMock).writeLock();
        inOrder.verify(mPalantiriMapMock).put(palantir, true);
        if (unlockWriteCounter.unlock > 0) {
            inOrder.verify(mLockMock).unlock(stamp);
        } else {
            inOrder.verify(mLockMock).unlockWrite(stamp);
        }
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

    private class UnlockCounter {
        int unlock = 0;
        int unlockRead = 0;
        int unlockWrite = 0;
    }
}

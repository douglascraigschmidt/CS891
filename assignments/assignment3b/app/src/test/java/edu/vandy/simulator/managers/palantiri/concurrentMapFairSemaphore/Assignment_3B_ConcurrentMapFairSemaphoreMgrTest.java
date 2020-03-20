package edu.vandy.simulator.managers.palantiri.concurrentMapFairSemaphore;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import admin.AssignmentTestRule;
import admin.AssignmentTests;
import edu.vandy.simulator.managers.palantiri.Palantir;
import edu.vandy.simulator.managers.palantiri.PalantiriManager;
import edu.vandy.simulator.utils.Assignment;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class Assignment_3B_ConcurrentMapFairSemaphoreMgrTest extends AssignmentTests {
    private static final int PALANTIRI_COUNT = 999;

    @Mock
    public Stream<Palantir> mockStreamPalantiri;
    @Mock
    public List<Palantir> mockPalantiri;
    @Mock
    protected ConcurrentMap<Palantir, Boolean> mockPalantiriMap;
    @Mock
    private Set<Palantir> mockKeySet;
    @Mock
    private Iterator<Palantir> mockKeySetIterator;
    @InjectMocks
    private ConcurrentMapFairSemaphoreMgr mockManager = mock(ConcurrentMapFairSemaphoreMgr.class);

    @Test
    public void buildModelTestUndergraduate() {
        undergraduateTest();
        buildModelTestForAssignmentType(Assignment.UNDERGRADUATE);
    }

    @Test
    public void buildModelTestGraduate() {
        graduateTest();
        buildModelTestForAssignmentType(Assignment.GRADUATE);
    }

    private void buildModelTestForAssignmentType(int type) {
        ConcurrentMapFairSemaphoreMgr manager = new ConcurrentMapFairSemaphoreMgr();

        manager.mPalantiri = buildPalantirList(manager);

        // Call SUT
        manager.buildModel();

        // Make sure it wasn't changed.
        assertEquals(PALANTIRI_COUNT, manager.mPalantiri.size());

        assertNotNull(manager.mPalantiriMap);
        assertEquals(manager.mPalantiri.size(), manager.mPalantiriMap.size());
        assertNotNull(manager.mAvailablePalantiri);
        if (type == Assignment.UNDERGRADUATE) {
            assertTrue(manager.mAvailablePalantiri instanceof FairSemaphoreMO);
        } else {
            assertTrue(manager.mAvailablePalantiri instanceof FairSemaphoreCO);
        }
        assertEquals(PALANTIRI_COUNT, manager.mAvailablePalantiri.availablePermits());

        for (Palantir palantir : manager.mPalantiri) {
            Boolean aBoolean = manager.mPalantiriMap.get(palantir);
            assertNotNull(aBoolean);
            assertTrue(aBoolean);
        }
    }

    @Test
    public void acquireTest() throws InterruptedException {
        Random random = new Random();
        for (int i = 1; i <= 50; i++) {
            mockManager.mPalantiri = buildPalantirList(mockManager);
            mockManager.mPalantiriMap = buildPalantirMap(mockManager.mPalantiri);
            ConcurrentMap<Palantir, Boolean> map = buildPalantirMap(mockManager.mPalantiri);
            ConcurrentMap<Palantir, Boolean> spyMap = spy(map);
            mockManager.mPalantiriMap = spyMap;
            mockManager.mAvailablePalantiri = mock(FairSemaphore.class);

            InOrder inOrder = inOrder(mockManager.mAvailablePalantiri, spyMap);

            // Randomly pick a single palantir to be available.
            Palantir availablePalantir = mockManager.mPalantiri.get(random.nextInt(PALANTIRI_COUNT));
            for (Palantir palantir : mockManager.mPalantiriMap.keySet()) {
                mockManager.mPalantiriMap.replace(palantir, palantir == availablePalantir);
            }

            doCallRealMethod().when(mockManager).acquire();

            // Call SUT method.
            Palantir result = mockManager.acquire();

            verify(spyMap).replace(availablePalantir, true, false);
            verify(mockManager.mAvailablePalantiri, times(1)).acquire();
            assertSame(availablePalantir, result);

            inOrder.verify(mockManager.mAvailablePalantiri).acquire();
            inOrder.verify(spyMap).replace(availablePalantir, true, false);
            reset(mockManager);
        }
    }

    @Test
    public void releaseAcquirePalantir() {
        mockManager.mAvailablePalantiri = mock(FairSemaphore.class);
        Palantir mockPalantir = mock(Palantir.class);
        doCallRealMethod().when(mockManager).release(any());
        when(mockPalantiriMap.put(mockPalantir, true)).thenReturn(false);

        mockManager.release(mockPalantir);

        verify(mockPalantiriMap, times(1)).put(mockPalantir, true);
        verify(mockManager.mAvailablePalantiri, times(1)).release();
    }

    @Test
    public void releaseFreePalantir() {
        mockManager.mAvailablePalantiri = mock(FairSemaphore.class);
        Palantir mockPalantir = mock(Palantir.class);
        doCallRealMethod().when(mockManager).release(any());
        when(mockPalantiriMap.put(mockPalantir, true)).thenReturn(true);

        mockManager.release(mockPalantir);

        verify(mockPalantiriMap, times(1)).put(mockPalantir, true);
        verify(mockManager.mAvailablePalantiri, never()).release();
    }

    @Test
    public void releaseNullPalantir() {
        mockManager.mAvailablePalantiri = mock(FairSemaphore.class);
        doCallRealMethod().when(mockManager).release(any());

        try {
            mockManager.release(null);
        } catch (Throwable t) {
            fail("method should gracefully handle null input");
        }

        verify(mockPalantiriMap, never()).put(any(), any());
        verify(mockManager.mAvailablePalantiri, never()).release();
    }

    private List<Palantir> buildPalantirList(PalantiriManager manager) {
        ArrayList<Palantir> palantiri = new ArrayList<>();
        for (int i = 1; i <= PALANTIRI_COUNT; i++) {
            palantiri.add(new Palantir(manager));
        }
        return palantiri;
    }

    private ConcurrentMap<Palantir, Boolean> buildPalantirMap(List<Palantir> palantiri) {
        ConcurrentMap<Palantir, Boolean> map = new ConcurrentHashMap<>();
        for (Palantir palantir : palantiri) {
            map.put(palantir, true);
        }
        return map;
    }

}
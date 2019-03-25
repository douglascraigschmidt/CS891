package edu.vandy.simulator.managers.palantiri.concurrentMapFairSemaphore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class Assignment_3B_ConcurrentMapFairSemaphoreMgrTest {
    // Model parameters.
    private final static int PALANTIRI_COUNT = 999;
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock
    public Stream<Palantir> mockStreamPalantiri;
    @Mock
    public List<Palantir> mockPalantiri;
    @Mock
    protected ConcurrentMap<Palantir, Boolean> mockPalantiriMap;

    @InjectMocks
    private ConcurrentMapFairSemaphoreMgr mockManager = mock(ConcurrentMapFairSemaphoreMgr.class);

    // Used to reset assignment types back to original value before each test is run
    private int assignmentTypes;
    @Mock
    private Set<Palantir> mockKeySet;
    @Mock
    private Iterator<Palantir> mockKeySetIterator;

    @Before
    public void before() {
        assignmentTypes = Assignment.sTypes;
    }

    @After
    public void after() {
        Assignment.sTypes = assignmentTypes;
    }

    @Test
    public void buildModelTest() {
        doCallRealMethod().when(mockManager).getPalantiri();
        doCallRealMethod().when(mockManager).getPalantirCount();
        when(mockPalantiri.size()).thenReturn(PALANTIRI_COUNT);
        when(mockPalantiri.stream()).thenReturn(mockStreamPalantiri);
        when(mockStreamPalantiri.collect(any())).thenReturn(mockPalantiriMap);

        doCallRealMethod().when(mockManager).buildModel();

        // Call SUT method.
        mockManager.buildModel();

        verify(mockPalantiri, times(1)).stream();
        verify(mockStreamPalantiri, times(1)).collect(any());
        assertSame(mockPalantiriMap, mockManager.mPalantiriMap);
        assertNotNull(mockManager.mAvailablePalantiri);
        assertEquals(PALANTIRI_COUNT, mockManager.mAvailablePalantiri.availablePermits());
    }

    @Test
    public void buildModelTestUndergraduate() {
        if (!Assignment.testType(Assignment.UNDERGRADUATE)) {
            System.out.println("Skipping buildModelTestUndergraduate (undergraduate only test)");
            return;
        }

        buildModelTestForAssignmentType(Assignment.UNDERGRADUATE);
    }

    @Test
    public void buildModelTestGraduate() {
        if (!Assignment.testType(Assignment.GRADUATE)) {
            System.out.println("Skipping buildModelTestGraduate (graduate only test)");
            return;
        }

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
    public void acquireTest() {
        Random random = new Random();
        for (int i = 0; i < 50; i++) {
            mockManager.mPalantiri = buildPalantirList(mockManager);
            mockManager.mPalantiriMap = buildPalantirMap(mockManager.mPalantiri);
            mockManager.mAvailablePalantiri = mock(FairSemaphore.class);

            // Randomly pick a single palantir to be available.
            Palantir availablePalantir = mockManager.mPalantiri.get(random.nextInt(PALANTIRI_COUNT));
            for (Palantir palantir : mockManager.mPalantiriMap.keySet()) {
                mockManager.mPalantiriMap.replace(palantir, palantir == availablePalantir);
            }

            doCallRealMethod().when(mockManager).acquire();

            // Call SUT method.
            Palantir result = mockManager.acquire();

            verify(mockManager, times(1)).acquire();
            assertSame(availablePalantir, result);

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


    @Test
    public void availablePermits() {
        int expectedPermits = -293;
        mockManager.mAvailablePalantiri = mock(FairSemaphore.class);
        when(mockManager.mAvailablePalantiri.availablePermits()).thenReturn(expectedPermits);

        doCallRealMethod().when(mockManager).availablePermits();

        int availablePermits = mockManager.availablePermits();
        assertEquals(expectedPermits, availablePermits);

        verify(mockManager.mAvailablePalantiri, times(1)).availablePermits();
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
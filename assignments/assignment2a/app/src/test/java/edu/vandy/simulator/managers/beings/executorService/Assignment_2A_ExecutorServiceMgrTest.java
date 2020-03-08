package edu.vandy.simulator.managers.beings.executorService;

import org.junit.After;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import admin.AssignmentTests;
import admin.ReflectionHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class Assignment_2A_ExecutorServiceMgrTest extends AssignmentTests {
    private final int BEING_COUNT = 5;

    @Mock
    Future<BeingCallable> mFutureMock;
    @Mock
    List<BeingCallable> beingsMock;
    @Mock
    List<Future<BeingCallable>> mListFutureMock;
    @Mock
    private ExecutorService mExecutorMock;
    @Mock
    private Stream<BeingCallable> mStreamBeingCallableMock;
    @Mock
    private Stream<Future<BeingCallable>> mStreamFutureMock;
    @InjectMocks
    private ExecutorServiceMgr mManagerMock = mock(ExecutorServiceMgr.class);

    // Used to count lambda errors caught by try/catch block
    private int mErrorCount = 0;

    @After
    public void after() {
        shutdownExecutor();
    }

    @Test
    public void testNewBeing() {
        doCallRealMethod().when(mManagerMock).newBeing();

        // Call SUT.
        BeingCallable beingCallable = mManagerMock.newBeing();

        assertNotNull("newBeing should not return null.", beingCallable);
    }

    @Test
    public void testRunSimulation() {
        doCallRealMethod().when(mManagerMock).runSimulation();

        // Call SUT.
        mManagerMock.runSimulation();

        InOrder inOrder = inOrder(mManagerMock);

        verify(mManagerMock).beginBeingThreadPool();
        verify(mManagerMock).awaitCompletionOfFutures();
        verify(mManagerMock).shutdownNow();

        inOrder.verify(mManagerMock).beginBeingThreadPool();
        inOrder.verify(mManagerMock).awaitCompletionOfFutures();
        inOrder.verify(mManagerMock).shutdownNow();
    }

    /**
     * Test common to both UNDERGRADUATE And GRADUATE Assignments.
     */
    @Test
    public void testBeginBeingThreadPool() {
        List<BeingCallable> mockBeings = createMockBeingList(BEING_COUNT);
        when(mManagerMock.getBeings()).thenReturn(mockBeings);
        when(mManagerMock.getBeingCount()).thenReturn(mockBeings.size());
        when(mManagerMock.createExecutorService(mockBeings.size())).thenReturn(mExecutorMock);
        when(mManagerMock.getThreadCount()).thenReturn(BEING_COUNT);
        when(mExecutorMock.submit(any(BeingCallable.class))).thenReturn(mFutureMock);
        doCallRealMethod().when(mManagerMock).beginBeingThreadPool();

        // Call SUT.
        mManagerMock.beginBeingThreadPool();

        verify(mExecutorMock, times(mockBeings.size())).submit(any(BeingCallable.class));

        List<Future<BeingCallable>> futureList =
                ReflectionHelper.findFirstMatchingFieldValue(mManagerMock, List.class);
        assertNotNull("Unable to access List<Future<BeingCallable>> field in " +
                "ExecutorServiceMgr class.", futureList);

        assertEquals(
                "Futures list should contain " + BEING_COUNT + " threads.",
                BEING_COUNT,
                futureList.size());

        for (Future<BeingCallable> future : futureList) {
            assertEquals("Unexpected future value in mFutureList", mFutureMock, future);
        }
    }

    /**
     * Test for GRADUATE use of Java 8 Streams.
     */
    @Test
    public void testBeginBeingThreadPoolGraduate() {
        graduateTest();

        when(beingsMock.size()).thenReturn(BEING_COUNT);
        when(beingsMock.stream()).thenReturn(mStreamBeingCallableMock);
        when(mManagerMock.getBeings()).thenReturn(beingsMock);
        when(mManagerMock.createExecutorService(beingsMock.size())).thenReturn(mExecutorMock);
        when(mManagerMock.getThreadCount()).thenReturn(BEING_COUNT);
        Function<BeingCallable, Future<BeingCallable>> anyFunction = any();
        when(mStreamBeingCallableMock.map(anyFunction)).thenReturn(mStreamFutureMock);
        List<Future<BeingCallable>> futureListMock = new ArrayList<>();
        when(mStreamFutureMock.collect(any())).thenReturn(futureListMock);

        doCallRealMethod().when(mManagerMock).beginBeingThreadPool();

        // Call SUT.
        mManagerMock.beginBeingThreadPool();

        verify(mManagerMock, times(1)).createExecutorService(beingsMock.size());
        verify(beingsMock, times(1)).stream();
        verify(mStreamBeingCallableMock, times(1)).map(any());
        verify(mStreamFutureMock, times(1)).collect(any());

        List<Future<BeingCallable>> futureList =
                ReflectionHelper.findFirstMatchingFieldValue(mManagerMock, List.class);
        assertNotNull("Unable to access List<Future<BeingCallable>> field in " +
                "ExecutorServiceMgr class.", futureList);

        assertSame(
                "Futures list should contain " + BEING_COUNT + " threads.",
                futureListMock,
                futureList);
    }

    /**
     * Test common to both UNDERGRADUATE And GRADUATE Assignments.
     */
    @Test
    public void testAwaitCompletionOfFutures() throws IllegalAccessException {
        List<Future<BeingCallable>> futureList = createMockFutureList(BEING_COUNT, true);
        ReflectionHelper.injectValueIntoFirstMatchingField(
                mManagerMock, futureList, List.class);

        doCallRealMethod().when(mManagerMock).awaitCompletionOfFutures();

        // Call SUT.
        long expectedCount = futureList.size();
        long beingCount = mManagerMock.awaitCompletionOfFutures();
        assertEquals(expectedCount, beingCount);

        futureList.forEach(futureMock -> {
            try {
                verify(futureMock).get();
            } catch (Exception e) {
                mErrorCount++;
            }
        });

        if (mErrorCount > 0) {
            throw new IllegalStateException("Call to Future.get() failed "
                    + mErrorCount + (mErrorCount == 1 ? " time" : "times"));
        }
    }

    /**
     * Test for GRADUATE use of Java 8 Streams.
     */
    @Test
    public void testAwaitCompletionOfFuturesGraduate() throws Exception {
        graduateTest();

        long expectedCount = 999;

        ReflectionHelper.injectValueIntoFirstMatchingField(mManagerMock, mListFutureMock, List.class);

        when(mListFutureMock.stream()).thenReturn(mStreamFutureMock);
        Function<Future<BeingCallable>, BeingCallable> anyFunction = any();
        when(mStreamFutureMock.map(anyFunction)).thenReturn(mStreamBeingCallableMock);
        when(mStreamBeingCallableMock.count()).thenReturn(expectedCount);

        doCallRealMethod().when(mManagerMock).awaitCompletionOfFutures();

        // Call SUT.
        long beingCount = mManagerMock.awaitCompletionOfFutures();
        assertEquals(expectedCount, beingCount);

        verify(mListFutureMock, times(1)).stream();
        verify(mStreamFutureMock, times(1)).map(any());
        verify(mStreamBeingCallableMock, times(1)).count();
    }

    @Test
    public void testShutdownNow() throws IllegalAccessException {
        ReflectionHelper.injectValueIntoFirstMatchingField(
                mManagerMock, mExecutorMock, ExecutorService.class);

        List<Future<BeingCallable>> futureList = createMockFutureList(BEING_COUNT, false);
        futureList.forEach(futureMock -> {
            when(futureMock.isCancelled()).thenReturn(false);
            when(futureMock.isDone()).thenReturn(false);
            when(futureMock.cancel(anyBoolean())).thenReturn(true);
        });
        ReflectionHelper.injectValueIntoFirstMatchingField(mManagerMock, futureList, List.class);
        doCallRealMethod().when(mManagerMock).shutdownNow();

        // Call SUT.
        mManagerMock.shutdownNow();

        // Cleanup - ensure that all futures are cancelled
        futureList.forEach(futureMock -> {
            try {
                verify(futureMock, times(1)).cancel(anyBoolean());
            } catch (Exception e) {
            }
        });
    }

    private List<Future<BeingCallable>> createMockFutureList(int count, boolean mockGet) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(unused -> {
                    @SuppressWarnings("unchecked")
                    Future<BeingCallable> futureMock =
                            (Future<BeingCallable>) mock(Future.class);
                    try {
                        if (mockGet) {
                            when(futureMock.get()).thenReturn(mock(BeingCallable.class));
                        }
                    } catch (Exception e) {
                    }
                    return futureMock;
                })
                .collect(Collectors.toList());
    }

    private List<BeingCallable> createMockBeingList(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(unused -> {
                    BeingCallable being = mock(BeingCallable.class);
                    //Mockito says this is never used...
                    //when(being.call()).thenReturn(being);
                    return being;
                })
                .collect(Collectors.toList());
    }

    /**
     * Make sure to shutdown thread pool executor to prevent the autograder
     * from hanging in the case where the threads continue running after the
     * test is over.
     */
    private void shutdownExecutor() {
        try {
            ExecutorService executor =
                    ReflectionHelper.findFirstMatchingFieldValue(
                            mManagerMock,
                            ExecutorService.class);
            assertNotNull("Unable to access ExecutorService " +
                    "field in ExecutorServiceMgr class.", executor);
            executor.shutdownNow();
        } catch (Exception e) {
            fail("Unable to access ExecutorService " +
                    "field in ExecutorServiceMgr class: " + e);
        }
    }
}

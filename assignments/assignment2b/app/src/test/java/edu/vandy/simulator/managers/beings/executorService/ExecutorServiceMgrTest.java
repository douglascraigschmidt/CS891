package edu.vandy.simulator.managers.beings.executorService;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import edu.vandy.simulator.ReflectionHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExecutorServiceMgrTest {
    private final int BEING_COUNT = 5;

    @Mock
    private ExecutorService mExecutor;

    @InjectMocks
    private ExecutorServiceMgr mManagerMock = mock(ExecutorServiceMgr.class);

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test(timeout = 2000)
    public void testNewBeing() {
        doCallRealMethod().when(mManagerMock).newBeing();
        BeingCallable beingCallable = mManagerMock.newBeing();

        assertNotNull("newBeing should not return null.", beingCallable);
    }

    @Test(timeout = 2000)
    public void testRunSimulation() {
        doCallRealMethod().when(mManagerMock).runSimulation();

        mManagerMock.runSimulation();

        InOrder inOrder = inOrder(mManagerMock);

        verify(mManagerMock).beginBeingThreadPool();
        verify(mManagerMock).awaitCompletionOfFutures();
        verify(mManagerMock).shutdownNow();

        inOrder.verify(mManagerMock).beginBeingThreadPool();
        inOrder.verify(mManagerMock).awaitCompletionOfFutures();
        inOrder.verify(mManagerMock).shutdownNow();
    }

    @Test(timeout = 2000)
    public void testShutdownNow() throws IllegalAccessException {
        ReflectionHelper.injectFieldValueIntoFirstFieldOfType(
                mManagerMock, ExecutorService.class, mExecutor);

        List<Future<BeingCallable>> futureList = createMockFutureList(BEING_COUNT, false);
        futureList.forEach(futureMock -> {
            when(futureMock.isCancelled()).thenReturn(false);
            when(futureMock.isDone()).thenReturn(false);
            when(futureMock.cancel(any(Boolean.class))).thenReturn(true);
        });
        ReflectionHelper.injectFieldValueIntoFirstFieldOfType(
                mManagerMock, List.class, futureList);
        doCallRealMethod().when(mManagerMock).shutdownNow();

        mManagerMock.shutdownNow();

        futureList.forEach(futureMock -> {
            try {
                verify(futureMock, times(1)).cancel(any(Boolean.class));
            } catch (Exception e) {
            }
        });
    }

    @Test(timeout = 2000)
    public void testBeginBeingThreadPool() {
        List<BeingCallable> mockBeings = createMockBeingList(BEING_COUNT);
        when(mManagerMock.getBeings()).thenReturn(mockBeings);
        when(mManagerMock.getBeingCount()).thenReturn(mockBeings.size());

        doCallRealMethod().when(mManagerMock).beginBeingThreadPool();

        mManagerMock.beginBeingThreadPool();

        // Make sure to shutdown thread pool executor to prevent the autograder
        // from hanging for while the threads continue running after the test is
        // over.
        try {
            ExecutorService executor =
                    ReflectionHelper.findFirstFieldValueOfType(mManagerMock, ExecutorService.class);
            assertNotNull("Unable to access ExecutorService " +
                    "field in ExecutorServiceMgr class.", executor);
            executor.shutdownNow();
        } catch (Exception e) {
            fail("Unable to access ExecutorService " +
                    "field in ExecutorServiceMgr class: " + e);
        }

        try {
            List<Future<BeingCallable>> futureList =
                    ReflectionHelper.findFirstFieldValueOfType(mManagerMock, List.class);
            assertNotNull("Unable to access List<Future<BeingCallable>> field in " +
                    "ExecutorServiceMgr class.", futureList);

            assertEquals(
                    "Futures list should contain " + BEING_COUNT + " threads.",
                    BEING_COUNT,
                    futureList.size());
        } catch (Exception e) {
            fail("Unable to access List<Future<BeingCallable>> field in " +
                    "ExecutorServiceMgr class: " + e);
        }
    }

    @Test(timeout = 2000)
    public void testAwaitCompletionOfFutures() throws IllegalAccessException {
        List<Future<BeingCallable>> futureList = createMockFutureList(BEING_COUNT, true);
        ReflectionHelper.injectFieldValueIntoFirstFieldOfType(
                mManagerMock, List.class, futureList);

        doCallRealMethod().when(mManagerMock).awaitCompletionOfFutures();
        mManagerMock.awaitCompletionOfFutures();

        futureList.forEach(futureMock -> {
            try {
                verify(futureMock).get();
            } catch (Exception e) {
            }
        });
    }

    private List<Future<BeingCallable>> createMockFutureList(int count, boolean mockGet) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(unused -> {
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
                    when(being.call()).thenReturn(being);
                    return being;
                })
                .collect(Collectors.toList());
    }
}

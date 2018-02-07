package edu.vandy.simulator.managers.beings.executorService;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExecutorServiceMgrTest {
    private final int BEING_COUNT = 5;

    @Mock
    private ExecutorServiceMgr mManagerMock;

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

        verify(mManagerMock).beginBeingThreadPool();
        verify(mManagerMock).awaitCompletionOfFutures();
    }

    @Test(timeout = 2000)
    public void testBeginBeingThreadPool() {
        List<BeingCallable> mockBeings = createMockBeingList(BEING_COUNT);
        when(mManagerMock.getBeings()).thenReturn(mockBeings);
        when(mManagerMock.getBeingCount()).thenReturn(mockBeings.size());        

        doCallRealMethod().when(mManagerMock).beginBeingThreadPool();
        doCallRealMethod().when(mManagerMock).getExecutor();
        doCallRealMethod().when(mManagerMock).getFutureList();

        mManagerMock.beginBeingThreadPool();

        assertNotNull("mExecutor should not be null.", mManagerMock.getExecutor());
        assertTrue(
                "mExecutor should be a ThreadPoolExecutor.",
                mManagerMock.getExecutor() instanceof ThreadPoolExecutor);

        assertNotNull("mFutureList should not be null.", mManagerMock.getFutureList());
        assertEquals(
                "mFutureList should contain " + BEING_COUNT + " threads.",
                BEING_COUNT,
                mManagerMock.getFutureList().size());
    }

    @Test(timeout = 2000)
    public void testAwaitCompletionOfFutures() {
        List<Future<BeingCallable>> futureList = createMockFutureList(BEING_COUNT);
        mManagerMock.mFutureList = futureList;

        doCallRealMethod().when(mManagerMock).awaitCompletionOfFutures();
        mManagerMock.awaitCompletionOfFutures();

        futureList.forEach(futureMock -> {
            try {
                verify(futureMock).get();
            } catch (Exception e) {
            }
        });
    }

    private List<Future<BeingCallable>> createMockFutureList(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(unused -> {
                    Future<BeingCallable> futureMock =
                            (Future<BeingCallable>)mock(Future.class);
                    try {
                        when(futureMock.get()).thenReturn(mock(BeingCallable.class));
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

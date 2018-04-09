package edu.vandy.simulator.managers.beings.asyncTask;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import edu.vandy.simulator.ReflectionHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AsyncTaskMgrTest {
    private final int BEING_COUNT = 5;
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @InjectMocks
    private AsyncTaskMgr mManagerMock = mock(AsyncTaskMgr.class);

    @Test(timeout = 2000)
    public void testNewBeing() {
        doCallRealMethod().when(mManagerMock).newBeing();
        AsyncBeing beingAsyncTask = mManagerMock.newBeing();

        assertNotNull("newBeing should not return null.", beingAsyncTask);
    }

    @Test(timeout = 2000)
    public void testRunSimulation() {
        doCallRealMethod().when(mManagerMock).runSimulation();

        mManagerMock.runSimulation();

        InOrder inOrder = inOrder(mManagerMock);

        verify(mManagerMock).beginAsyncTasksGazing();
        verify(mManagerMock).waitForAsyncTasksToFinishGazing();
        verify(mManagerMock).shutdownNow();

        inOrder.verify(mManagerMock).beginAsyncTasksGazing();
        inOrder.verify(mManagerMock).waitForAsyncTasksToFinishGazing();
        inOrder.verify(mManagerMock).shutdownNow();
    }

    @Test(timeout = 2000)
    public void testThreadFactory() {
        AsyncTaskMgr realManager = new AsyncTaskMgr();
        Runnable runnable = () -> {
        };

        List<Thread> threads = IntStream.rangeClosed(1, BEING_COUNT)
                .mapToObj(unused -> realManager.mThreadFactory.newThread(runnable))
                .collect(Collectors.toList());

        long distinctThreads = threads.stream()
                .distinct()
                .count();
        assertEquals(
                "ThreadFactory should create " + BEING_COUNT + " distinct threads.",
                BEING_COUNT, distinctThreads);

        long distinctNames = threads.stream()
                .map(Thread::getName)
                .distinct()
                .count();
        assertEquals(
                "ThreadFactory should create " + BEING_COUNT + " distinctly named threads.",
                BEING_COUNT, distinctNames);
    }

    @Test(timeout = 2000)
    public void testShutdownNow() throws IllegalAccessException {
        List<AsyncBeing> mockBeings = createMockBeingList(BEING_COUNT);
        when(mManagerMock.getBeings()).thenReturn(mockBeings);
        when(mManagerMock.getBeingCount()).thenReturn(mockBeings.size());
        ThreadPoolExecutor threadPoolExecutorMock = mock(ThreadPoolExecutor.class);
        when(threadPoolExecutorMock.shutdownNow()).thenReturn(null);

        ReflectionHelper.injectFieldValueIntoFirstFieldOfType(
                mManagerMock, ThreadPoolExecutor.class, threadPoolExecutorMock);

        doCallRealMethod().when(mManagerMock).shutdownNow();
        mManagerMock.shutdownNow();

        mockBeings.forEach(
                being -> verify(being, times(1)).cancel(true));

        verify(threadPoolExecutorMock, times(1)).shutdownNow();
    }

    @Test(timeout = 2000)
    public void testBeginAsyncTasksGazing() {
        List<AsyncBeing> mockBeings = createMockBeingList(BEING_COUNT);
        when(mManagerMock.getBeings()).thenReturn(mockBeings);
        when(mManagerMock.getBeingCount()).thenReturn(mockBeings.size());

        // Special handling for mThreadFactory field which will not
        // be initialized when mManagerMock is created. The only way
        // to overcome this limitation is by creating a real instance
        // of AsyncTaskMgr and assigning it's mThreadFactory implementation
        // to the mock instance's mThreadFactory field.
        AsyncTaskMgr realManager = new AsyncTaskMgr();
        mManagerMock.mThreadFactory = realManager.mThreadFactory;

        doCallRealMethod().when(mManagerMock).beginAsyncTasksGazing();

        mManagerMock.beginAsyncTasksGazing();

        // Make sure to shutdown thread pool executor to prevent the autograder
        // from hanging for while the threads continue running after the test is
        // over.
        try {
            ThreadPoolExecutor executor =
                    ReflectionHelper.findFirstFieldValueOfType(
                            mManagerMock, ThreadPoolExecutor.class);
            assertNotNull("Unable to access ThreadPoolExecutor " +
                    "field in " + mManagerMock.getClass().getSimpleName() + " class.",
                    executor);
            executor.shutdownNow();
        } catch (Exception e) {
            fail("Unable to access ExecutorService " +
                    "field in ExecutorServiceMgr class: " + e);
        }

        ReflectionHelper.assertAnonymousFieldNotNull(mManagerMock, CyclicBarrier.class);
        CyclicBarrier cyclicBarrier =
                ReflectionHelper.findFirstFieldValueOfType(mManagerMock, CyclicBarrier.class);
        assert cyclicBarrier != null;

        int parties = cyclicBarrier.getParties();
        assertEquals(
                "Cyclic barrier should should be initialized to " + BEING_COUNT + 1 + " parties.",
                BEING_COUNT + 1, parties);

        ReflectionHelper.assertAnonymousFieldNotNull(mManagerMock, CountDownLatch.class);
        CountDownLatch countDownLatch =
                ReflectionHelper.findFirstFieldValueOfType(mManagerMock, CountDownLatch.class);
        assert countDownLatch != null;

        long count = countDownLatch.getCount();
        assertEquals(
                "Countdown latch should should be initialized to " + BEING_COUNT + " beings.",
                BEING_COUNT, count);

        ReflectionHelper.assertAnonymousFieldNotNull(mManagerMock, ThreadPoolExecutor.class);
        ThreadPoolExecutor threadPoolExecutor =
                ReflectionHelper.findFirstFieldValueOfType(mManagerMock, ThreadPoolExecutor.class);
        assert threadPoolExecutor != null;

        assertEquals(0, threadPoolExecutor.getCorePoolSize());
        assertEquals(BEING_COUNT, threadPoolExecutor.getMaximumPoolSize());
        assertEquals(60L, threadPoolExecutor.getKeepAliveTime(TimeUnit.SECONDS));
        assertNotNull(threadPoolExecutor.getQueue());
        assertSame(mManagerMock.mThreadFactory, threadPoolExecutor.getThreadFactory());

        mockBeings.forEach(
                being -> verify(being, times(1))
                        .executeOnExecutor(cyclicBarrier, countDownLatch, threadPoolExecutor));
    }

    private List<AsyncBeing> createMockBeingList(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(unused -> mock(AsyncBeing.class))
                .peek(mock ->
                        doNothing().when(mock).executeOnExecutor(
                                any(CyclicBarrier.class),
                                any(CountDownLatch.class),
                                any(ThreadPoolExecutor.class)))
                .collect(Collectors.toList());
    }
}

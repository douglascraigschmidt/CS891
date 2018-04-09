package edu.vandy.simulator.managers.beings.asyncTask;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadPoolExecutor;

import edu.vanderbilt.grader.rubric.Rubric;
import edu.vandy.simulator.ReflectionHelper;
import edu.vandy.simulator.managers.beings.BeingManager;
import edu.vandy.simulator.managers.palantiri.Palantir;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AsyncBeingTest {
    private static int GAZING_ITERATIONS = 10;

    @Mock
    private BeingManager mBeingManagerMock = mock(AsyncTaskMgr.class);
    @Mock
    private Palantir palantirMock = mock(Palantir.class);
    @Mock
    private CyclicBarrier mCyclicBarrierMock;
    @Mock
    private CountDownLatch mCountDownLatchMock;
    @Mock
    private ThreadPoolExecutor mThreadPoolExecutorMock;
    @InjectMocks
    private AsyncBeing.AsyncBeingTask mAsyncBeingTaskMock = mock(AsyncBeing.AsyncBeingTask.class);
    @InjectMocks
    private AsyncBeing mAsyncBeingMock = mock(AsyncBeing.class);

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rubric(value = "ExecuteOnExecutor method test.",
            goal = "This test checks for the proper implementation of the " +
                    "AsyncTask's ExecuteOnExecutor method. " +
                    "It checks that all the expected method calls are made " +
                    "with the correct parameters, the correct number of times," +
                    "and in the correct order."
    )
    @Test(timeout = 2000)
    public void testExecuteOnExecutorMethod() {
        when(mAsyncBeingMock.createTask(any(), any())).thenReturn(mAsyncBeingTaskMock);
        when(mAsyncBeingMock.getGazingIterations()).thenReturn(GAZING_ITERATIONS);

        doCallRealMethod()
                .when(mAsyncBeingMock)
                .executeOnExecutor(any(), any(), any());

        mAsyncBeingMock.executeOnExecutor(
                mCyclicBarrierMock,
                mCountDownLatchMock,
                mThreadPoolExecutorMock);

        assertNotNull(mAsyncBeingMock.mAsyncTask);
        verify(mAsyncBeingMock, times(1))
                .createTask(mCyclicBarrierMock, mCountDownLatchMock);
        verify(mAsyncBeingTaskMock, times(1))
                .executeOnExecutor(mThreadPoolExecutorMock, GAZING_ITERATIONS);
    }

    @Rubric(value = "AsyncBeing.AsyncBeingTask class implementation test.",
            goal = "This test checks for the proper implementation of the " +
                    "AsyncBeing.AsyncBeingTask's onPreExecute method."
    )
    @Test(timeout = 2000)
    public void testAsyncTaskOnPreExecute() {
        doCallRealMethod().when(mAsyncBeingTaskMock).onPreExecute();

        mAsyncBeingTaskMock.onPreExecute();

        verify(mAsyncBeingTaskMock, times(1)).log(any(String.class));
    }

    @Rubric(value = "AsyncBeing.AsyncBeingTask class implementation test.",
            goal = "This test checks for the proper implementation of the " +
                    "AsyncBeing.AsyncBeingTask's onPostExecute method."
    )
    @Test(timeout = 2000)
    public void testAsyncTaskOnPostExecute() throws IllegalAccessException {
        final String msg = "Random message";

        doCallRealMethod().when(mAsyncBeingTaskMock).onPostExecute(any());

        mAsyncBeingTaskMock.onPostExecute(msg);

        verify(mAsyncBeingTaskMock, times(1)).log(msg);
    }

    @Rubric(value = "AsyncBeing.AsyncBeingTask class implementation test.",
            goal = "This test checks for the proper implementation of the " +
                    "AsyncBeing.AsyncBeingTask's onPostExecute method when no errors occur."
    )
    @Test(timeout = 2000)
    public void testAsyncTaskDoInBackground() throws Exception {
        ReflectionHelper.injectOuterClass(mAsyncBeingMock, mAsyncBeingTaskMock);

        doNothing().when(mAsyncBeingMock).runGazingSimulation(GAZING_ITERATIONS);

        doCallRealMethod().when(mAsyncBeingTaskMock).doInBackground(GAZING_ITERATIONS);

        String result = mAsyncBeingTaskMock.doInBackground(GAZING_ITERATIONS);

        verify(mCyclicBarrierMock, times(1)).await();
        verify(mAsyncBeingMock, times(1)).runGazingSimulation(GAZING_ITERATIONS);

        assertEquals("being succeeded", result);
    }

    @Rubric(value = "AsyncBeing.AsyncBeingTask class implementation test.",
            goal = "This test checks for the proper implementation of the " +
                    "AsyncBeing.AsyncBeingTask's onPostExecute method when an error occurs."
    )
    @Test(timeout = 2000)
    public void testAsyncTaskDoInBackgroundWithError() throws Exception {
        ReflectionHelper.injectOuterClass(mAsyncBeingMock, mAsyncBeingTaskMock);

        doNothing().when(mAsyncBeingMock).runGazingSimulation(GAZING_ITERATIONS);
        when(mCyclicBarrierMock.await()).thenThrow(new InterruptedException("await exception"));

        doCallRealMethod().when(mAsyncBeingTaskMock).doInBackground(GAZING_ITERATIONS);

        String result = mAsyncBeingTaskMock.doInBackground(GAZING_ITERATIONS);

        verify(mCyclicBarrierMock, times(1)).await();
        verify(mAsyncBeingMock, never()).runGazingSimulation(GAZING_ITERATIONS);

        assertEquals("being failed with exception await exception", result);
    }

    @Rubric(value = "AsyncTask acquirePalantirAndGaze method test.",
            goal = "This test checks for the proper implementation of the " +
                    "AsyncTask's acquirePalantirAndGaze method. " +
                    "It checks that all the expected method calls are made " +
                    "with the correct parameters, the correct number of times," +
                    "and in the correct order."
    )
    @Test(timeout = 2000)
    public void testBeingAcquirePalantirAndGazeMethod() {
        AsyncBeing being = new AsyncBeing(mBeingManagerMock);
        assertNotNull("new AsyncTask should not be null.", being);

        when(mBeingManagerMock.acquirePalantir(same(being))).thenReturn(palantirMock);
        doNothing().when(mBeingManagerMock).releasePalantir(same(being), same(palantirMock));

        InOrder inOrder = inOrder(mBeingManagerMock, palantirMock);

        // Make the SUT call.
        being.acquirePalantirAndGaze();

        verify(mBeingManagerMock, times(1)).acquirePalantir(being);
        verify(mBeingManagerMock, times(1)).releasePalantir(being, palantirMock);
        verify(mBeingManagerMock, never()).error(anyString());

        inOrder.verify(mBeingManagerMock).acquirePalantir(being);
        inOrder.verify(palantirMock).gaze(being);
        inOrder.verify(mBeingManagerMock).releasePalantir(being, palantirMock);
    }

    @Rubric(value = "AsyncTask acquirePalantirAndGaze method error test.",
            goal = "This test checks the acquirePalantirAndGaze method for proper " +
                    "error handling when the method is unable to acquire a Palantir. " +
                    "It also checks that all the expected method calls are made " +
                    "with the correct parameters, the correct number of times," +
                    "and in the correct order."
    )
    @Test(timeout = 2000)
    public void testBeingRunGazingSimulationMethodErrorHandling() {
        BeingManager beingManager = mock(AsyncTaskMgr.class);

        AsyncBeing being = new AsyncBeing(beingManager);
        assertNotNull("new AsyncTask should not be null.", being);

        when(beingManager.acquirePalantir(same(being))).thenReturn(null);
        doNothing().when(beingManager).releasePalantir(any(), any());
        doNothing().when(beingManager).error(anyString());

        InOrder inOrder = inOrder(beingManager);

        // Make the SUT call.
        being.acquirePalantirAndGaze();

        verify(beingManager, times(1)).acquirePalantir(being);
        verify(beingManager, times(1)).error(anyString());
        verify(beingManager, never()).releasePalantir(any(), any());

        inOrder.verify(beingManager).acquirePalantir(being);
        inOrder.verify(beingManager).error(anyString());
    }
}

package edu.vandy.simulator.managers.beings.runnableThreads;

import org.junit.Test;

import java.util.List;

import edu.vanderbilt.grader.rubric.Rubric;
import edu.vandy.simulator.managers.beings.Being;
import edu.vandy.simulator.managers.beings.BeingManager;
import edu.vandy.simulator.managers.palantiri.Palantir;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BeingRunnableTest {
    @Rubric(value = "@@Doug",
            goal = "@@Doug",
            points = 1,
            reference = {"@@Doug", "@@Doug" }
    )
    @Test(timeout = 1000)
    public void testBeingRunGazingSimulationMethod() {
        // Setup mocks.
        BeingManager beingManager = mock(RunnableThreadsMgr.class);
        Palantir palantir = mock(Palantir.class);

        // Instantiate class to test.
        SimpleBeingRunnable being = new SimpleBeingRunnable(beingManager);
        assertNotNull("new SimpleBeingRunnable should not be null.", being);

        // Trap call to return a mock palantir.
        when(beingManager.acquirePalantir(same(being))).thenReturn(palantir);

        // Trap call to ensure that its called once with the correct parameters.
        doNothing().when(beingManager).releasePalantir(same(being), same(palantir));

        // Trap error call to make sure that it's never called.
        doNothing().when(beingManager).error(anyString());

        // Make the call.
        being.acquirePalantirAndGaze();

        // Make sure that the acquirePalantir was called exactly once.
        verify(beingManager, times(1)).acquirePalantir(being);

        // Make sure that the releasePalantir was called exactly once.
        verify(beingManager, times(1)).releasePalantir(being, palantir);

        // Make sure that the releasePalantir was called exactly once.
        verify(beingManager, never()).error(anyString());
    }

    @Rubric(value = "@@Doug",
            goal = "@@Doug",
            points = 1,
            reference = {"@@Doug", "@@Doug" }
    )
    @Test(timeout = 1000)
    public void testBeingRunGazingSimulationMethodErrorHandling() {
        // Setup mocks.
        BeingManager beingManager = mock(RunnableThreadsMgr.class);

        // Instantiate class to test.
        SimpleBeingRunnable being = new SimpleBeingRunnable(beingManager);
        assertNotNull("new SimpleBeingRunnable should not be null.", being);

        // Return a null palantir to test error handling.
        when(beingManager.acquirePalantir(same(being))).thenReturn(null);

        // Trap call to ensure that its never called.
        doNothing().when(beingManager).releasePalantir(any(), any());

        // Trap error call to make sure that it's called once with any error string.
        doNothing().when(beingManager).error(anyString());

        // Make the call.
        being.acquirePalantirAndGaze();

        // Make sure that the acquirePalantir was called exactly once.
        verify(beingManager, times(1)).acquirePalantir(being);

        // Make sure that the error method was called exactly once.
        verify(beingManager, times(1)).error(anyString());

        // Make sure that the releasePalantir was called exactly once.
        verify(beingManager, never()).releasePalantir(any(), any());
    }
}
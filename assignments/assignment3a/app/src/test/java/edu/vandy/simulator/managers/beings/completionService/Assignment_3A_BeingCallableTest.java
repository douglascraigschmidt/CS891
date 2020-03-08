package edu.vandy.simulator.managers.beings.completionService;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.mockito.InOrder;
import org.mockito.Mock;

import admin.AssignmentTests;
import edu.vandy.simulator.managers.beings.BeingManager;
import edu.vandy.simulator.managers.palantiri.Palantir;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class Assignment_3A_BeingCallableTest extends AssignmentTests {
    @Rule
    public final Timeout timeout = new Timeout(10, SECONDS);

    @Mock
    BeingManager mockBeingManager = mock(ExecutorCompletionServiceMgr.class);

    @Mock
    Palantir mockPalantir = mock(Palantir.class);

    @Test
    public void call() {
        BeingCallable beingCallable = new BeingCallable(mockBeingManager);

        // Make the SUT call.
        BeingCallable callResult = beingCallable.call();

        // Check Results.
        assertNotNull("Call should not return null.", callResult);
        assertSame("Call should return the BeingCallable instance.", beingCallable, callResult);
    }

    @Test
    public void testAcquirePalantirAndGazeMethod() {
        BeingCallable being = new BeingCallable(mockBeingManager);
        assertNotNull("new BeingCallable should not be null.", being);

        when(mockBeingManager.acquirePalantir(same(being))).thenReturn(mockPalantir);
        doNothing().when(mockBeingManager).releasePalantir(same(being), same(mockPalantir));

        InOrder inOrder = inOrder(mockBeingManager, mockPalantir);

        // Make the SUT call.
        being.acquirePalantirAndGaze();

        verify(mockBeingManager, times(1)).acquirePalantir(being);
        verify(mockBeingManager, times(1)).releasePalantir(being, mockPalantir);
        verify(mockBeingManager, never()).error(anyString());

        inOrder.verify(mockBeingManager).acquirePalantir(being);
        inOrder.verify(mockPalantir).gaze(being);
        inOrder.verify(mockBeingManager).releasePalantir(being, mockPalantir);
    }

    @Test
    public void testAcquirePalantirAndGazeMethodErrorHandling() {
        BeingManager beingManager = mock(ExecutorCompletionServiceMgr.class);

        BeingCallable being = new BeingCallable(beingManager);
        assertNotNull("new BeingCallable should not be null.", being);

        when(beingManager.acquirePalantir(same(being))).thenReturn(null);
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


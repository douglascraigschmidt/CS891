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
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RunnableThreadsMgrTest {
    // Model parameters.
    private final static int BEING_COUNT = 5;

    @Rubric(value = "BeingManager class: newBeing test.",
            goal = "The goal of this test is to ensure that the newBeing call creates a new being.",
            points = 1,
            reference = {"@@Doug", "@@Doug"}
    )
    @Test(timeout = 100)
    public void newBeingTest() {
        BeingManager beingManager = new RunnableThreadsMgr();
        Being being = beingManager.newBeing();
        assertNotNull("Being should not be null.", being);
        assertEquals("Being mManager should be set.", beingManager, being.mManager);
        assertNull("Being mThread should be null.", being.mThread);
        assertEquals("Being mThread should be null.", 0, being.mCompleted);
    }

    @Rubric(value = "BeingManager class: makeBeings test.",
            goal = "The goal of this test is to ensure that the makeBeings call creates" +
                    "5 new beings.",
            points = 1,
            reference = {"@@Doug", "@@Doug"}
    )
    @Test(timeout = 100)
    public void makeBeingsTest() {
        BeingManager beingManager = new RunnableThreadsMgr();
        List beings = beingManager.makeBeings(BEING_COUNT);
        assertNotNull("Being list should not be null.", beings);
        assertEquals(
                BEING_COUNT + " new beings should be created.",
                BEING_COUNT,
                beings.size());
    }

    @Rubric(value = "@@Doug",
            goal = "@@Doug",
            points = 1,
            reference = {"@@Doug", "@@Doug"}
    )
    @Test(timeout = 1000)
    public void runSimulationTest() {
        // Setup mocks.
        RunnableThreadsMgr beingManager = mock(RunnableThreadsMgr.class);
        Thread thread = new Thread(() -> System.out.println("Waiter thread was run."));

        // Trap call to ensure that its called once.
        doNothing().when(beingManager).beginBeingThreads();

        // Trap call to ensure that its called once and to return a mock Thread.
        when(beingManager.createAndStartWaiterForBeingThreads()).thenReturn(thread);

        // Trap the call to runSimulation so that we can call the real method.
        doCallRealMethod().when(beingManager).runSimulation();

        // Make the call.
        beingManager.runSimulation();

        // Start the thread.
        thread.start();

        // Make sure that the beginBeingThreads was called exactly once.
        verify(beingManager, times(1)).beginBeingThreads();

        // Make sure that the releasePalantir was called exactly once.
        verify(beingManager, times(1)).createAndStartWaiterForBeingThreads();
    }

    // WORKING ON THIS...
//    @Rubric(value = "@@Doug",
//            goal = "@@Doug",
//            points = 1,
//            reference = {"@@Doug", "@@Doug"}
//    )
//    @Test(timeout = 100)
//    public void beginBeingThreadsTest() {
//        // Setup mocks.
//        Being being = mock(SimpleBeingRunnable.class);
//        Thread thread = new Thread(() -> System.out.println("Waiter thread was run."));
//
//        // Trap call to ensure that its called once.
//        doNothing().when(beingManager).beginBeingThreads();
//
//        // Trap call to ensure that its called once and to return a mock Thread.
//        when(beingManager.createAndStartWaiterForBeingThreads()).thenReturn(thread);
//
//        // Trap the call to runSimulation so that we can call the real method.
//        doCallRealMethod().when(beingManager).runSimulation();
//
//        // Make the call.
//        beingManager.runSimulation();
//
//        // Start the thread.
//        thread.start();
//
//        // Make sure that the beginBeingThreads was called exactly once.
//        verify(beingManager, times(1)).beginBeingThreads();
//
//        // Make sure that the releasePalantir was called exactly once.
//        verify(beingManager, times(1)).createAndStartWaiterForBeingThreads();
//    }
}
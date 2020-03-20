package edu.vandy.simulator.managers.beings.runnableThreads;

import org.junit.Test;
import org.mockito.InOrder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import edu.vanderbilt.grader.rubric.Rubric;
import edu.vandy.simulator.managers.beings.Being;
import edu.vandy.simulator.managers.beings.BeingManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class Assignment_1A_RunnableThreadsMgrTest {
    private final static int BEING_COUNT = 5;

    @Rubric(value = "RunnableThreadsManager newBeing method test.",
            goal = "The goal of this test is to ensure that the newBeing call creates a new being.",
            reference = {"@@Unfinished"}
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

    @Rubric(value = "BeingManager makeBeings method test.",
            goal = "The goal of this test is to ensure that when the BeingManager makeBeings method " +
                    "is invoked and repeatedly calls the RunnableThreadsMgr newBeing method, that the " +
                    "correct number of unique Being instances are created." +
                    "5 new beings.",
            reference = {"@@Unfinished"}
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

        assertEquals("All created beings should be unique.",
                BEING_COUNT,
                beings.stream().distinct().count()
        );
    }

    @Rubric(value = "RunnableThreadsMgr beingThreads method test.",
            goal = "This test checks for the proper implementation of the " +
                    "RunnableThreadsMgr's beingThreads method. " +
                    "It checks that all the expected method calls are made " +
                    "with the correct parameters, the correct number of times," +
                    "and in the correct order.",
            reference = {"@@Unfinished"}
    )
    @Test(timeout = 10000)
    public void beginBeingThreadsTest() {
        // Setup mocks.
        RunnableThreadsMgr beingManager = mock(RunnableThreadsMgr.class);

        List<SimpleBeingRunnable> mockBeings =
                IntStream.rangeClosed(1, 1)
                        .mapToObj(unused -> {
                            SimpleBeingRunnable being = mock(SimpleBeingRunnable.class);
                            doNothing().when(being).run();
                            return being;
                        })
                        .collect(Collectors.toList());

        when(beingManager.getBeings()).thenReturn(mockBeings);
        doCallRealMethod().when(beingManager).beginBeingThreads();

        // Make the SUT call.
        beingManager.beginBeingThreads();

        // This test requires waiting for all threads to completed
        // before testing to see if their run methods were called.
        try {
            for (Thread thr : beingManager.mBeingThreads) {
                thr.join();
            }
        } catch (Exception e) {
            fail("Being threads should not be interrupted.");
        }

        verify(beingManager).getBeings();
        mockBeings.forEach(being -> verify(being, times(1)).run());
    }

    @Rubric(value = "RunnableThreadsMgr runSimulation method test.",
            goal = "This test checks for the proper implementation of the " +
                    "RunnableThreadsMgr's runSimulation method. " +
                    "It checks that all the expected method calls are made " +
                    "with the correct parameters, the correct number of times," +
                    "and in the correct order.",
            reference = {"@@Unfinished"}
    )
    @Test(timeout = 10000)
    public void runSimulationTest() throws Exception {
        RunnableThreadsMgr beingManager = mock(RunnableThreadsMgr.class);
        Thread waiterThread = mock(Thread.class);

        doNothing().when(waiterThread).join();
        doNothing().when(beingManager).beginBeingThreads();
        when(beingManager.createAndStartWaiterForBeingThreads()).thenReturn(waiterThread);
        doCallRealMethod().when(beingManager).runSimulation();

        InOrder inOrder = inOrder(beingManager, waiterThread);

        // Call the SUT method.
        beingManager.runSimulation();
        waiterThread.start();

        verify(beingManager).beginBeingThreads();
        verify(beingManager).createAndStartWaiterForBeingThreads();
        verify(waiterThread).join();

        inOrder.verify(beingManager).beginBeingThreads();
        inOrder.verify(beingManager).createAndStartWaiterForBeingThreads();
        inOrder.verify(waiterThread).join();
    }

    @Rubric(value = "RunnableThreadsMgr createAndStartWaiterForBeingThreads method test.",
            goal = "This test checks for the proper implementation of the " +
                    "RunnableThreadsMgr's createAndStartWaiterForBeingThreads method. " +
                    "It checks that all the expected method calls are made " +
                    "with the correct parameters, the correct number of times," +
                    "and in the correct order.",
            reference = {"@@Unfinished"}
    )
    @Test(timeout = 10000)
    public void testCreateAndStartWaiterForBeingThreadsMethod() {
        RunnableThreadsMgr beingManager = new RunnableThreadsMgr();

        beingManager.mBeingThreads =
                IntStream.rangeClosed(1, BEING_COUNT)
                        .mapToObj(unused -> mock(Thread.class))
                        .peek(thread -> {
                            try {
                                doNothing().when(thread).join();
                            } catch (Exception e) {
                            }
                        })
                        .collect(Collectors.toList());

        // Call the SUT method.
        Thread waiterThread = beingManager.createAndStartWaiterForBeingThreads();

        assertNotNull(
                "createAndStartWaiterForBeingThreads " +
                        "should return a non null thread thread",
                waiterThread);

        try {
            waiterThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        beingManager.mBeingThreads.forEach(thread -> {
            try {
                verify(thread, times(1)).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}

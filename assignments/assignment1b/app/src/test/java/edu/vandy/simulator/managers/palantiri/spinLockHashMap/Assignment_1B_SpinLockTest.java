package edu.vandy.simulator.managers.palantiri.spinLockHashMap;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import edu.vanderbilt.grader.rubric.Rubric;
import admin.AssignmentTestRule;
import edu.vandy.simulator.utils.Assignment;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Run with power mock and prepare the Thread
 * class for mocking it's static methods.
 */
public class Assignment_1B_SpinLockTest {
    @Mock
    public Supplier<Boolean> isCancelled;

    @Mock
    public AtomicBoolean owner;

    @InjectMocks
    public SpinLock spinLock;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public AssignmentTestRule assignmentTestRule = new AssignmentTestRule();

    @Rubric(value = "SpinLock tryLock method test.",
            goal = "The goal of this test is to ensure that SpinLock's tryLock method" +
                    "calls the correct method the correct number of times.",
            reference = {"@@Unfinished"}
    )
    @Test(timeout = 1000)
    public void tryLockTest() {
        if (!Assignment.testType(Assignment.UNDERGRADUATE)) {
            System.out.println("Skipping undergraduate test");
            return;
        }

        when(owner.compareAndSet(false, true)).thenReturn(true);

        // Call the SUT method.
        boolean locked = spinLock.tryLock();

        verify(owner, times(1)).compareAndSet(false, true);
        assertEquals("tryLock should return true", true, locked);
    }

    @Test(timeout = 1000)
    public void lockIsUnlockedTest() {
        if (!Assignment.testType(Assignment.UNDERGRADUATE)) {
            System.out.println("Skipping undergraduate test");
            return;
        }

        when(owner.compareAndSet(false, true)).thenReturn(true);

        // Call the SUT method.
        spinLock.lock(isCancelled);

        verify(isCancelled, never()).get();
        verify(owner, times(1)).compareAndSet(false, true);
    }

    @Rubric(value = "SpinLock lock method test (locked).",
            goal = "The goal of this test is to ensure that SpinLock's lock method " +
                    "behaves correctly when the lock is locked. It also ensures that the " +
                    "correct methods are called with the correct parameters the correct " +
                    "number of times.",
            reference = {"@@Unfinished"}
    )
    @Test(timeout = 1000)
    public void lockIsLockedTest() {
        if (!Assignment.testType(Assignment.UNDERGRADUATE)) {
            System.out.println("Skipping undergraduate test");
            return;
        }

        when(owner.compareAndSet(false, true))
                .thenReturn(false)
                .thenReturn(true);
        when(isCancelled.get()).thenReturn(false);

        // Call the SUT method.
        spinLock.lock(isCancelled);

        verify(isCancelled, times(1)).get();
        verify(owner, times(2)).compareAndSet(false, true);
    }

    @Rubric(value = "SpinLock lock method test (locked, cancelled).",
            goal = "The goal of this test is to ensure that SpinLock's lock method " +
                    "behaves correctly when the lock is locked and a cancel is encountered. " +
                    "It also ensures that the correct methods are called with the correct " +
                    "parameters the correct number of times.",
            reference = {"@@Unfinished"}
    )
    @Test(timeout = 1000)
    public void lockIsLockedAndThenCancelledTest() {
        if (!Assignment.testType(Assignment.UNDERGRADUATE)) {
            System.out.println("Skipping undergraduate test");
            return;
        }

        when(owner.compareAndSet(false, true))
                .thenReturn(false)
                .thenReturn(true);
        when(isCancelled.get()).thenReturn(true);

        try {
            // Call the SUT method.
            spinLock.lock(isCancelled);
        } catch (CancellationException e) {
            verify(isCancelled, times(1)).get();
            verify(owner, times(1)).compareAndSet(false, true);
            return;
        }

        fail("lock() should throw a CancellationException when isCancelled() returns true.");
    }

    @Rubric(value = "SpinLock unlock method test.",
            goal = "The goal of this test is to ensure that SpinLock's unlock method" +
                    "calls the correct method the correct number of times.",
            reference = {"@@Unfinished"}
    )
    @Test(timeout = 1000)
    public void unlockTest() {
        if (!Assignment.testType(Assignment.UNDERGRADUATE)) {
            System.out.println("Skipping undergraduate test");
            return;
        }

        doNothing().when(owner).set(false);
        when(owner.compareAndSet(true, false)).thenReturn(true);

        // Call the SUT method.
        spinLock.unlock();

        ArgumentCaptor<Boolean> setCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(owner, atMost(1)).set(setCaptor.capture());

        ArgumentCaptor<Boolean> compareAndSetCaptor1 = ArgumentCaptor.forClass(Boolean.class);
        ArgumentCaptor<Boolean> compareAndSetCaptor2 = ArgumentCaptor.forClass(Boolean.class);

        verify(owner, atMost(1))
                .compareAndSet(
                        compareAndSetCaptor1.capture(),
                        compareAndSetCaptor2.capture());

        assertTrue(
                "Either AtomicBoolean set or compareAndSet should be called.",
                setCaptor.getAllValues().size() > 0 !=
                compareAndSetCaptor1.getAllValues().size() > 0);

        if (setCaptor.getAllValues().size() > 0) {
            assertFalse("set should be called with true", setCaptor.getValue());
        } else {
            assertTrue("compareAndSet should be called with true and false",
                    compareAndSetCaptor1.getValue());
            assertFalse("compareAndSet should be called with true and false",
                    compareAndSetCaptor2.getValue());
        }
    }
}
package edu.vandy.simulator.managers.palantiri.spinLockHashMap;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import edu.vanderbilt.grader.rubric.Rubric;
import admin.AssignmentTestRule;
import edu.vandy.simulator.utils.Assignment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Run with power mock and prepare the Thread
 * class for mocking it's static methods.
 */
public class Assignment_1B_ReentrantSpinLockTest {
    // Mocks.
    @Mock
    public Supplier<Boolean> isCancelled;

    @Mock
    public AtomicReference<Thread> owner;

    @InjectMocks
    public ReentrantSpinLock spinLock;

    // Initializes the above isCancelled mock;
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public AssignmentTestRule assignmentTestRule = new AssignmentTestRule();

    @Rubric(value = "ReentrantSpinLock tryLock method test.",
            goal = "The goal of this test is to ensure that ReentrantSpinLock's tryLock method" +
                    "calls the correct method the correct number of times.",
            reference = {"@@Unfinished"}
    )
    @Test(timeout = 1000)
    public void testTryLock() {
        if (!Assignment.testType(Assignment.GRADUATE)) {
            System.out.println("Skipping graduate test");
            return;
        }

        // Trap call to AtomicReference compareAndSet and return
        // true so that spin lock (should) only call it once.
        when(owner.compareAndSet(null, Thread.currentThread()))
                .thenReturn(true);

        // Call the SUT method.
        boolean locked = spinLock.tryLock();

        verify(owner, times(1)).compareAndSet(null, Thread.currentThread());
        assertEquals("tryLock should return true", true, locked);
        assertEquals("Recursion count should be 0.", 0, spinLock.getRecursionCount());
    }

    @Rubric(value = "ReentrantSpinLock lock method test (unlocked).",
            goal = "The goal of this test is to ensure that ReentrantSpinLock's lock method " +
                    "behaves correctly when the lock is unlocked. It also ensures that the " +
                    "correct methods are called with the correct parameters the correct " +
                    "number of times.",
            reference = {"@@Unfinished"}
    )
    @Test(timeout = 1000)
    public void testLockWhenUnlocked() {
        if (!Assignment.testType(Assignment.GRADUATE)) {
            System.out.println("Skipping graduate test");
            return;
        }

        when(owner.get()).thenReturn(Thread.currentThread());

        // Call the SUT method.
        spinLock.lock(isCancelled);

        verify(owner, times(1)).get();
        verify(isCancelled, never()).get();
        verify(owner, never()).compareAndSet(any(), any());
        assertEquals("Recursion count should be 1.", 1, spinLock.getRecursionCount());
    }

    @Rubric(value = "ReentrantSpinLock lock method test (locked but reentrant).",
            goal = "The goal of this test is to ensure that ReentrantSpinLock's lock method " +
                    "behaves correctly when the lock is unlocked due to it being reentrant. " +
                    "It also ensures that the correct methods are called with the correct " +
                    "parameters the correct number of times.",
            reference = {"@@Unfinished"}
    )
    @Test(timeout = 1000)
    public void testLockWhenAlreadyLockedButIsReentrant() {
        if (!Assignment.testType(Assignment.GRADUATE)) {
            System.out.println("Skipping graduate test");
            return;
        }

        when(owner.get()).thenReturn(Thread.currentThread());

        // Make 3 calls to the method to test.
        spinLock.lock(isCancelled);
        spinLock.lock(isCancelled);
        spinLock.lock(isCancelled);

        verify(owner, times(3)).get();
        verify(isCancelled, never()).get();
        verify(owner, never()).compareAndSet(any(), any());
        assertEquals("Recursion count should be 3.", 3, spinLock.getRecursionCount());
    }

    @Rubric(value = "ReentrantSpinLock lock method test (locked and not reentrant).",
            goal = "The goal of this test is to ensure that ReentrantSpinLock's lock method " +
                    "behaves correctly when the lock is locked and not reentrant. " +
                    "It also ensures that the correct methods are called with the correct " +
                    "parameters the correct number of times.",
            reference = {"@@Unfinished"}
    )
    @Test(timeout = 1000)
    public void testLockWhenAlreadyLockedTest() {
        if (!Assignment.testType(Assignment.GRADUATE)) {
            System.out.println("Skipping graduate test");
            return;
        }

        Thread thread = new Thread();

        when(owner.get()).thenReturn(thread);
        when(owner.compareAndSet(null, Thread.currentThread()))
                .thenReturn(true);

        // Call the SUT method.
        spinLock.lock(isCancelled);

        verify(owner, times(1)).get();
        verify(owner, times(1)).compareAndSet(null, Thread.currentThread());
        assertEquals("Recursion count should be 0.", 0, spinLock.getRecursionCount());
    }

    @Rubric(value = "ReentrantSpinLock lock method test (locked, not reentrant, spin wait).",
            goal = "The goal of this test is to ensure that ReentrantSpinLock's lock method " +
                    "behaves correctly when the lock is locked and not reentrant and needs to" +
                    "be checked more than once. It also ensures that the correct methods are " +
                    "called with the correct parameters the correct number of times.",
            reference = {"@@Unfinished"}
    )
    @Test(timeout = 1000)
    public void testLockWhenAlreadyLockedWithWaitTest() {
        if (!Assignment.testType(Assignment.GRADUATE)) {
            System.out.println("Skipping graduate test");
            return;
        }

        Thread thread = new Thread();

        when(owner.get()).thenReturn(thread);
        when(isCancelled.get()).thenReturn(false);
        when(owner.compareAndSet(null, Thread.currentThread()))
                .thenReturn(false)
                .thenReturn(true);

        // Call the SUT method.
        spinLock.lock(isCancelled);

        verify(owner, times(1)).get();
        verify(isCancelled, times(1)).get();
        verify(owner, times(2)).compareAndSet(null, Thread.currentThread());
        assertEquals("Recursion count should be 0.", 0, spinLock.getRecursionCount());
    }

    @Rubric(value = "ReentrantSpinLock lock method test (locked, not reentrant, spin wait, cancelled).",
            goal = "The goal of this test is to ensure that ReentrantSpinLock's lock method " +
                    "behaves correctly when the lock is locked and not reentrant and a cancel " +
                    "is received during the wait. It also ensures that the correct methods are " +
                    "called with the correct parameters the correct number of times.",
            reference = {"@@Unfinished"}
    )
    @Test(timeout = 1000)
    public void testLockWhenAlreadyLockedWithWaitTestAndThenCancelled() {
        if (!Assignment.testType(Assignment.GRADUATE)) {
            System.out.println("Skipping graduate test");
            return;
        }

        Thread thread = new Thread();

        when(owner.get()).thenReturn(thread);
        when(isCancelled.get()).thenReturn(true);
        when(owner.compareAndSet(null, Thread.currentThread()))
                .thenReturn(false);

        try {
            // Call the SUT method.
            spinLock.lock(isCancelled);
        } catch (CancellationException e) {
            verify(isCancelled, times(1)).get();
            verify(owner, times(1)).compareAndSet(null, Thread.currentThread());
            assertEquals("Recursion count should be 0.", 0, spinLock.getRecursionCount());
            return;
        }

        fail("lock() should throw a CancellationException when isCancelled() returns true.");
    }

    @Rubric(value = "ReentrantSpinLock unlock method test.",
            goal = "The goal of this test is to ensure that ReentrantSpinLock's unlock method" +
                    "calls the correct method the correct number of times.",
            reference = {"@@Unfinished"}
    )
    @Test(timeout = 1000)
    public void testUnlock() {
        if (!Assignment.testType(Assignment.GRADUATE)) {
            System.out.println("Skipping graduate test");
            return;
        }

        when(owner.get()).thenReturn(Thread.currentThread());

        // Call the SUT method.
        spinLock.unlock();

        verify(owner, times(1)).get();
        assertEquals("Recursion count should be 0.", 0, spinLock.getRecursionCount());
    }
}

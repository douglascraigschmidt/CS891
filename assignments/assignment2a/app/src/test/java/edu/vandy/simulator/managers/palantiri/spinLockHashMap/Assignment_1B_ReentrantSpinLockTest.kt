package edu.vandy.simulator.managers.palantiri.spinLockHashMap

import admin.AssignmentTests
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Supplier

/**
 * Run with power mock and prepare the Thread
 * class for mocking it's static methods.
 */
class Assignment_1B_ReentrantSpinLockTest : AssignmentTests() {
    @Mock
    lateinit var isCancelled: Supplier<Boolean>

    @Mock
    lateinit var owner: AtomicReference<Thread?>

    @InjectMocks
    internal lateinit var spinLock: ReentrantSpinLock

    @Before
    fun before() {
        graduateTest()
    }

    @Test
    fun testTryLock() {
        // Trap call to AtomicReference compareAndSet and return
        // true so that spin lock (should) only call it once.
        whenever(owner.compareAndSet(null, Thread.currentThread()))
                .thenReturn(true)

        // SUT
        val locked = spinLock.tryLock()

        verify(owner).compareAndSet(null, Thread.currentThread())
        assertEquals("tryLock should return true", true, locked)
        assertEquals("Recursion count should be 0.", 0, spinLock.recursionCount.toLong())
    }

    @Test
    fun testLockWhenUnlocked() {
        whenever(owner.get()).thenReturn(Thread.currentThread())

        // SUT
        spinLock.lock(isCancelled)

        verify(owner).get()
        verify(isCancelled, never()).get()
        verify(owner, never()).compareAndSet(any(), any())
        assertEquals("Recursion count should be 1.", 1, spinLock.recursionCount.toLong())
    }

    @Test
    fun testLockWhenAlreadyLockedButIsReentrant() {
        whenever(owner.get()).thenReturn(Thread.currentThread())

        // SUT
        spinLock.lock(isCancelled)
        spinLock.lock(isCancelled)
        spinLock.lock(isCancelled)

        verify(owner, times(3)).get()
        verify(isCancelled, never()).get()
        verify(owner, never()).compareAndSet(any(), any())
        assertEquals("Recursion count should be 3.", 3, spinLock.recursionCount.toLong())
    }

    @Test
    fun testLockWhenAlreadyLockedTest() {
        whenever(owner.get()).thenReturn(null)
        whenever(owner.compareAndSet(null, Thread.currentThread())).thenReturn(true)

        // SUT
        spinLock.lock(isCancelled)

        verify(owner, atLeast(1)).get()
        verify(owner).compareAndSet(null, Thread.currentThread())
        assertEquals("Recursion count should be 0.", 0, spinLock.recursionCount.toLong())
    }

    fun testLockWhenAlreadyLockedWithWaitTest() {
        // Handles case where get() == null is used to
        // avoid calling tryLock's compareAndSet.
        whenever(owner.get()).thenReturn(null)
        whenever(isCancelled.get()).thenReturn(false)
        whenever(owner.compareAndSet(null, Thread.currentThread()))
                .thenReturn(false)
                .thenReturn(true)

        // SUT
        spinLock.lock(isCancelled)

        verify(owner, atLeast(1)).get()
        verify(isCancelled).get()
        verify(owner, times(2)).compareAndSet(null, Thread.currentThread())
        assertEquals("Recursion count should be 0.", 0, spinLock.recursionCount.toLong())
    }

    @Test
    fun testLockWhenAlreadyLockedWithWaitTestAndThenCancelled() {
        // Handles case where get() == null is used to
        // avoid calling tryLock's compareAndSet.
        whenever(owner.get()).thenReturn(null)
        whenever(isCancelled.get()).thenReturn(true)
        whenever(owner.compareAndSet(null, Thread.currentThread()))
                .thenReturn(false)
        try {
            // SUT
            spinLock.lock(isCancelled)

            fail("lock() should throw a CancellationException when isCancelled() returns true.")
        } catch (e: CancellationException) {
            verify(isCancelled).get()
            verify(owner).compareAndSet(null, Thread.currentThread())
            assertEquals("Recursion count should be 0.", 0, spinLock.recursionCount.toLong())
        }
    }

    @Test
    fun testUnlock() {
        doReturn(Thread.currentThread()).whenever(owner).get()

        // SUT
        spinLock.unlock()

        verify(owner).get()
        verify(owner).set(null)
        verify(owner, never()).compareAndSet(any(), any())
        assertEquals("Recursion count should be 0.", 0, spinLock.recursionCount.toLong())
    }
}
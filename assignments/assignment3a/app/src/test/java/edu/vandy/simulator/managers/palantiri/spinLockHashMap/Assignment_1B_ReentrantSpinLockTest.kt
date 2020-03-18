package edu.vandy.simulator.managers.palantiri.spinLockHashMap

import admin.AssignmentTests
import admin.injectInto
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Supplier
import kotlin.random.Random
import kotlin.random.nextInt

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
    fun `lock takes ownership of lock when lock is not owned`() {
        // Trap call to AtomicReference compareAndSet and return
        // true so that spin lock (should) only call it once.
        whenever(owner.compareAndSet(null, Thread.currentThread())).thenReturn(true)

        // SUT
        val locked = spinLock.tryLock()

        verify(owner).compareAndSet(null, Thread.currentThread())
        assertEquals("tryLock should return true", true, locked)
        assertEquals("Recursion count should be 0.", 0, spinLock.recursionCount.toLong())
    }

    @Test
    fun `lock increments lock count when already held by calling thread`() {
        whenever(owner.get()).thenReturn(Thread.currentThread())

        // SUT
        spinLock.lock(isCancelled)

        verify(owner).get()
        verify(isCancelled, never()).get()
        verify(owner, never()).compareAndSet(any(), any())
        assertEquals("Recursion count should be 1.", 1, spinLock.recursionCount.toLong())
    }

    @Test
    fun `lock is reentrant when called multiple times by owning thread`() {
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
    fun `lock immediately takes ownership when lock not already held`() {
        whenever(owner.get()).thenReturn(null)
        whenever(owner.compareAndSet(null, Thread.currentThread())).thenReturn(true)

        // SUT
        spinLock.lock(isCancelled)

        verify(owner, atLeast(1)).get()
        verify(owner).compareAndSet(null, Thread.currentThread())
        assertEquals("Recursion count should be 0.", 0, spinLock.recursionCount.toLong())
    }

    @Test
    fun `lock spins when lock already held and then holds like once lock has been released`() {
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
    fun `waiting for a held lock can be cancelled`() {
        // Handles case where get() == null is used to
        // avoid calling tryLock's compareAndSet.
        whenever(owner.get()).thenReturn(null)
        whenever(isCancelled.get()).thenReturn(true)
        whenever(owner.compareAndSet(null, Thread.currentThread()))
                .thenReturn(false)
        // SUT
        assertThrows<CancellationException>("should throw CancellationException") {
            spinLock.lock(isCancelled)
        }

        verify(isCancelled).get()
        verify(owner).compareAndSet(null, Thread.currentThread())
        assertEquals("Recursion count should be 0.", 0, spinLock.recursionCount.toLong())
    }

    @Test
    fun `unlock releases a held lock`() {
        doReturn(Thread.currentThread()).whenever(owner).get()

        // SUT
        spinLock.unlock()

        verify(owner).get()
        verify(owner).set(null)
        verify(owner, never()).compareAndSet(any(), any())
        assertEquals("Recursion count should be 0.", 0, spinLock.recursionCount.toLong())
    }

    @Test
    fun `unlock only release lock after recursion count reaches 0`() {
        doReturn(Thread.currentThread()).whenever(owner).get()
        val count = Random.nextInt(10..20)
        (count - 1).injectInto(spinLock)

        // SUT
        repeat(count) {
            spinLock.unlock()
        }

        verify(owner, times(count)).get()
        verify(owner).set(null)
        verifyNoMoreInteractions(owner)
        assertEquals("Recursion count should be 0.", 0, spinLock.recursionCount.toLong())
    }

    @Test
    fun `unlock should throw an exception when lock is not held`() {
        doReturn(null).whenever(owner).get()

        // SUT
        assertThrows<IllegalMonitorStateException>("Should throw an exception") {
            spinLock.unlock()
        }

        verify(owner).get()
        verifyNoMoreInteractions(owner)
    }
}
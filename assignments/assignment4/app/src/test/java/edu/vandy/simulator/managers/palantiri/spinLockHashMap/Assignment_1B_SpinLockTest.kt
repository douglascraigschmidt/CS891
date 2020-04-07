package edu.vandy.simulator.managers.palantiri.spinLockHashMap

import admin.AssignmentTests
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import java.lang.RuntimeException
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Supplier

/**
 * Run with power mock and prepare the Thread
 * class for mocking it's static methods.
 */
@ExperimentalCoroutinesApi
class Assignment_1B_SpinLockTest : AssignmentTests() {
    @Mock
    lateinit var isCancelled: Supplier<Boolean>

    @Mock
    lateinit var owner: AtomicBoolean

    @InjectMocks
    internal lateinit var spinLock: SpinLock

    @Before
    fun before() {
        undergraduateTest()
    }

    @Test
    fun tryLockTest() {
        whenever(owner.compareAndSet(false, true)).thenReturn(true)
        // Call the SUT method.
        val locked = spinLock.tryLock()
        verify(owner, times(1)).compareAndSet(false, true)
        assertTrue("tryLock should return true", locked)
    }

    @Test
    fun lockIsUnlockedTest() {
        whenever(owner.compareAndSet(false, true)).thenReturn(true)
        // Call the SUT method.
        spinLock.lock(isCancelled)
        verify(isCancelled, never()).get()
        verify(owner, times(1)).compareAndSet(false, true)
    }

    @Test
    fun lockIsLockedTest() {
        whenever(owner.compareAndSet(false, true))
                .thenReturn(false)
                .thenReturn(true)
        whenever(isCancelled.get()).thenReturn(false)
        // Call the SUT method.
        spinLock.lock(isCancelled)
        verify(isCancelled, times(1)).get()
        verify(owner, times(2)).compareAndSet(false, true)
    }

    @Test
    fun lockIsLockedAndThenCancelledTest() {
        whenever(owner.compareAndSet(false, true))
                .thenReturn(false)
                .thenReturn(true)
        whenever(isCancelled.get()).thenReturn(true)

        try {
            // Call the SUT method.
            spinLock.lock(isCancelled)
            fail("lock() should throw a CancellationException when isCancelled() returns true.")
        } catch (e: CancellationException) {
            verify(isCancelled, times(1)).get()
            verify(owner, times(1)).compareAndSet(false, true)
        }
    }

    @Test
    fun `unlock should release a held lock`() {
        lenient().`when`(owner.get()).thenReturn(true)
        lenient().`when`(owner.getAndSet(false)).thenReturn(true)

        spinLock.unlock()

        try {
            verify(owner).get()
            verify(owner).set(false)
        } catch (t: Throwable) {
            verify(owner).getAndSet(false)
        }
    }
}
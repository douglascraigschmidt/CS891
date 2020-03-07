package admin

import edu.vandy.simulator.utils.Assignment
import edu.vandy.simulator.utils.Assignment.GRADUATE
import edu.vandy.simulator.utils.Assignment.UNDERGRADUATE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.rules.TestWatcher
import org.junit.rules.Timeout
import org.junit.rules.Timeout.seconds
import org.junit.runner.Description
import org.mockito.junit.MockitoJUnit


/**
 * Base class used for all assignment test classes
 */
@ExperimentalCoroutinesApi
open class AssignmentTests {
    /** Required for all mockito tests */
    @Rule
    @JvmField
    var mockitoRule = MockitoJUnit.rule()!!

    /** Assignment rule ensures that the Assignment type is reset before each test method runs */
    @get:Rule
    var assignmentTestRule = AssignmentTestRule()

    /** Sets all tests to timeout after 5 seconds. */
    @get:Rule
    open var timeout: Timeout = seconds(10)

    /** Special coroutine test rule that sets up TestCoroutineDispatcher */
    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    /** Can be overridden by derived classes to run or prevent tests from running */
    open val runTest: Boolean = true

    val skipTest: Boolean
        get() = (!runTest).also {
            if (it) println("SKIPPING: ${Thread.currentThread().stackTrace[2].methodName}")
        }

    /** @return true if current project is a graduate assignment */
    fun isGradAssignment() = isAssignmentType(GRADUATE)

    /** @return true if current project is a undergraduate assignment */
    fun isUndergradAssignment() = isAssignmentType(UNDERGRADUATE)

    /** @return true if current project is a [type] assignment */
    fun isAssignmentType(type: Int) = (Assignment.sTypes and type) != 0

    /**
     * Throws [org.junit.AssumptionViolatedException]
     * to force a test to be ignored when the current
     * project is not a graduate assignment.
     */
    fun graduateTest() = assumeTrue("Graduate test ignored", runAs(GRADUATE))

    /**
     * Throws [org.junit.AssumptionViolatedException]
     * to force a test to be ignored when the current
     * project is not an undergraduate assignment.
     */
    fun undergraduateTest() = assumeTrue("Undergraduate test ignored", runAs(UNDERGRADUATE))

    /**
     * This call the a side-effect of setting the Assignment.sTypes
     * member to type iff (Assignment.sTypes & type) == true.
     */
    fun runAs(type: Int) = Assignment.testType(type)

//    class CoroutineTestRule : TestRule, CoroutineScope {
//        override val coroutineContext: CoroutineContext = Job() + Dispatchers.Unconfined
//
//        override fun apply(base: Statement, description: Description?) = object : Statement() {
//            override fun evaluate() {
//                base.evaluate()
//                this@CoroutineTestRule.cancel()
//            }
//        }
//    }

    class CoroutineTestRule(val dispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher())
        : TestWatcher() {
        override fun starting(description: Description?) {
            super.starting(description)
            Dispatchers.setMain(dispatcher)
        }

        override fun finished(description: Description?) {
            super.finished(description)
            Dispatchers.resetMain()
            dispatcher.cleanupTestCoroutines()
        }
    }

    //TODOx: can't get this to work without getting UnfinishedMockingSessionException
    //@Rule
    //@JvmField
    //var mockito = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS)

    /**
     * Example usage:
     * ```kotlin
     * val exception = assertThrows<IllegalArgumentException> {
     *     throw IllegalArgumentException("Talk to a duck")
     * }
     * assertEquals("Talk to a duck", exception.message)
     * ```
     * @see Assertions.assertThrows
     */
    inline fun <reified T : Throwable> assertThrows(executable: () -> Unit): T =
            assertThrows(runCatching(executable))

    /**
     * Example usage:
     * ```kotlin
     * val exception = assertThrows<IllegalArgumentException>("Should throw an Exception") {
     *     throw IllegalArgumentException("Talk to a duck")
     * }
     * assertEquals("Talk to a duck", exception.message)
     * ```
     * @see Assertions.assertThrows
     */
    inline fun <reified T : Throwable> assertThrows(message: String, executable: () -> Unit): T =
            assertThrows(message, runCatching(executable))

    /**
     * Example usage:
     * ```kotlin
     * val exception = assertThrows<IllegalArgumentException>(runCatching {
     *     throw IllegalArgumentException("Talk to a duck")
     * })
     * assertEquals("Talk to a duck", exception.message)
     * ```
     * @see Assertions.assertThrows
     */
    inline fun <reified T : Throwable> assertThrows(result: Result<*>): T =
            Assert.assertThrows(T::class.java) { result.getOrThrow() }

    /**
     * Example usage:
     * ```kotlin
     * val exception = assertThrows<IllegalArgumentException>("Should throw an Exception") {
     *     runCatching {
     *        throw IllegalArgumentException("Talk to a duck")
     *     }
     *  }
     * assertEquals("Talk to a duck", exception.message)
     * ```
     * @see Assertions.assertThrows
     */
    inline fun <reified T : Throwable> assertThrows(message: String, result: Result<*>): T =
            Assert.assertThrows(message, T::class.java) { result.getOrThrow() }
}

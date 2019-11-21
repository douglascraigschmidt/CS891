package admin

import edu.vanderbilt.imagecrawler.utils.Assignment
import edu.vanderbilt.imagecrawler.utils.Assignment.GRADUATE
import edu.vanderbilt.imagecrawler.utils.Assignment.UNDERGRADUATE
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.rules.ExpectedException
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnit
import org.junit.rules.Timeout.seconds
import org.junit.rules.Timeout


/**
 * Base class used for all assignment test classes
 */
open class AssignmentTests {
    /** Can be overridden by derived classes to run or prevent tests from running */
    open val runTest: Boolean = true

    val skipTest: Boolean
        get() = (!runTest).also {
            if (it) println("SKIPPING: ${Thread.currentThread().stackTrace[2].methodName}")
        }

    /** Required for all mockito tests */
    @Rule
    @JvmField
    var mockitoRule = MockitoJUnit.rule()!!

    /** Assignment rule ensures that the Assignment type is reset before each test method runs */
    @Rule
    @JvmField
    var assignmentTestRule = AssignmentTestRule()

    /**
     * An exception rule used by most derived test classes.
     */
    @Rule
    @JvmField
    var exception: ExpectedException = ExpectedException.none()

    /**
     * Sets all tests to timeout after 5 seconds.
     */
    @Rule
    @JvmField
    var timeout: Timeout = seconds(5)

    /**
     * Required to allow any() to work with mockito calls
     * to prevent any() can't be null Kotlin error messages.
     */
    fun <T> any(): T = Mockito.any<T>()

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
}

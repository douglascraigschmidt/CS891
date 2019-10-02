package admin

import edu.vanderbilt.imagecrawler.utils.Assignment
import edu.vanderbilt.imagecrawler.utils.Assignment.GRADUATE
import edu.vanderbilt.imagecrawler.utils.Assignment.UNDERGRADUATE
import org.junit.Rule
import org.junit.rules.ExpectedException

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

    fun isGradAssignment() = Assignment.testType(GRADUATE)

    fun isUndergradAssignment() = Assignment.testType(UNDERGRADUATE)

    fun assignmentType(type: Int) = Assignment.testType(type).also {
        if (!it) println("SKIPPING: ${Thread.currentThread().stackTrace[2].methodName}")
    }
}

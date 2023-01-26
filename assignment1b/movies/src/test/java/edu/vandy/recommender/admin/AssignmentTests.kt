package edu.vandy.recommender.admin

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import org.junit.Rule
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.fail
import org.junit.rules.Timeout
import org.junit.rules.Timeout.seconds


/**
 * Base class used for all assignment test classes
 */

@ExtendWith(AssignmentTests.AssignmentTestExtensions::class)
open class AssignmentTests(
    timeoutSeconds: Int = if (System.getenv()["USER"] == "monte") 0 else 10
) {

    class AssignmentTestExtensions : AfterEachCallback, BeforeEachCallback {
        override fun afterEach(context: ExtensionContext?) {
            clearAllMocks()
        }

        override fun beforeEach(context: ExtensionContext?) {
            MockKAnnotations.init(
                this,
                relaxUnitFun = true,
                overrideRecordPrivateCalls = true
            )
        }
    }

    /**
     * Sets all tests to timeout after 5 seconds.
     */
    @Rule
    @JvmField
    var timeout: Timeout = seconds(timeoutSeconds.toLong())

    fun verifyOneOf(message: String, vararg block: () -> Unit) {
        // List to record which lambdas throw exceptions and
        // which do not.
        val status = mutableListOf<Boolean>()

        // Run each lambda and add result to status list.
        block.forEach {
            status.add(
                try {
                    // Run the lambda.
                    it.invoke()
                    true
                } catch (t: Throwable) {
                    false
                }
            )
        }

        if (status.count { result -> result } != 1) {
            fail(message)
        }
    }
}

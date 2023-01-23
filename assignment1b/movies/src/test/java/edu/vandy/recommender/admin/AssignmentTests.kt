package edu.vandy.recommender.admin

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.jupiter.api.fail
import org.junit.rules.TestRule
import org.junit.rules.Timeout
import org.junit.rules.Timeout.seconds
import org.junit.runners.model.Statement

/**
 * Base class used for all assignment test classes
 */
open class AssignmentTests(
    timeoutSeconds: Int = if (System.getenv()["USER"] == "monte") 0 else 10) {

    @Rule
    @JvmField
    var mockkRule = TestRule { base, _ ->
        object : Statement() {
            override fun evaluate() {
                MockKAnnotations.init(
                    this,
                    relaxUnitFun = true,
                    overrideRecordPrivateCalls = true
                )
                try {
                    base.evaluate()
                } finally {
                }
            }
        }
    }


    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    @After
    fun tearDown() {
        clearAllMocks()
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

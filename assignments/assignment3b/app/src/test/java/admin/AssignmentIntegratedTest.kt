package admin

import edu.vandy.simulator.Controller
import edu.vandy.simulator.managers.beings.BeingManager
import edu.vandy.simulator.managers.palantiri.PalantiriManager

/**
 * Precision of 0 means round percents up to 0 decimal places.
 * No point values are used for each rubric since the default
 * point value of 1 is acceptable for this assignment. No weight
 * has been specified instrumented and unit tests are worth the
 * same value towards the final mark (50% for each class each).
 */
abstract class AssignmentIntegratedTest: AssignmentTests() {
    abstract val beingManager: BeingManager.Factory.Type
    abstract val palantirManager: PalantiriManager.Factory.Type
    open val threadCount: Int = 10

    open fun normalTest() {
        Controller.setLogging(false)

        TestHelper.testStrategy(
                beingManager,
                palantirManager,
                /* beingCount */ 10,
                /* palantirCount */ 6,
                threadCount,
                /* iterations */ 10,
                /* animationSpeed */ 0f,
                /* gazingRangeMin */ 0,
                /* gazingRangeMax */ 100)
    }

    open fun stressTest() {
        Controller.setLogging(false)

        TestHelper.testStrategy(
                beingManager,
                palantirManager,
                /* beingCount */ 50,
                /* palantirCount */ 10,
                threadCount,
                /* iterations */ 100,
                /* animationSpeed */ 0f,
                /* gazingRangeMin */ 0,
                /* gazingRangeMax */ 0)
    }
}

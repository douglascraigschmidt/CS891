package edu.vandy.simulator

import admin.AssignmentIntegratedTest
import edu.vanderbilt.grader.rubric.Rubric
import edu.vandy.simulator.managers.beings.BeingManager
import edu.vandy.simulator.managers.palantiri.PalantiriManager

/**
 * Precision of 0 means round percents up to 0 decimal places.
 * No point values are used for each rubric since the default
 * point value of 1 is acceptable for this assignment. No weight
 * has been specified instrumented and unit tests are worth the
 * same value towards the final mark (50% for each class each).
 */
@Rubric
class Assignment_1B_IntegratedTest : AssignmentIntegratedTest() {
    override val beingManager = BeingManager.Factory.Type.RUNNABLE_THREADS
    override val palantirManager = PalantiriManager.Factory.Type.ARRAY_BLOCKING_QUEUE
}

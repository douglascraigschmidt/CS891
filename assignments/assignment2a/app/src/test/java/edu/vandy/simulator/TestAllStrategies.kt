package edu.vandy.simulator

import edu.vanderbilt.grader.rubric.Rubric
import org.junit.Ignore
import org.junit.Test

@Ignore
@Rubric(precision = 0)
class TestAllStrategies {
    @Rubric(
            value = "normalTestOfAllStrategies",
            goal = "The goal of this evaluation is to ensure that all BeingManager and" +
                   "PalantiriManager combinations run correctly with 10 beings, 6 palantiri, " +
                   "10 iterations and a gazing delay of 0 to 50 milliseconds.",
            reference = [
                "https://www.youtube.com/watch?v=WxpjEXt7J0g&index=6&list=PLZ9NgFYEMxp4p5piHxajQXRRlsyDCLvI3&t=15s",
                "https://www.youtube.com/watch?v=8Ij9Q4AGfgc&list=PLZ9NgFYEMxp4p5piHxajQXRRlsyDCLvI3&index=7",
                "https://www.youtube.com/watch?v=GdrXGs2Ipp4&index=8&list=PLZ9NgFYEMxp4p5piHxajQXRRlsyDCLvI3"
            ]
    )
    @Test
    fun normalTest() {
        Controller.setLogging(false)

        TestHelper.testStrategyPairs(
                TestHelper.buildAllPairs(),
                /* beingCount */ 10,
                /* palantirCount */ 6,
                /* iterations */ 10,
                /* animationSpeed */ 0f,
                /* gazingRangeMin */ 0,
                /* gazingRangeMax */ 50)
    }

    @Rubric(
            value = "stressTestAllStrategies",
            goal = "The goal of this evaluation is to stress test all BeingManager and" +
                   "PalantiriManager combinations 50 beings, 10 palantiri, 100 iterations and" +
                   "no gazing delay and no animations.",
            reference = [
                "https://www.youtube.com/watch?v=WxpjEXt7J0g&index=6&list=PLZ9NgFYEMxp4p5piHxajQXRRlsyDCLvI3&t=15s",
                "https://www.youtube.com/watch?v=8Ij9Q4AGfgc&list=PLZ9NgFYEMxp4p5piHxajQXRRlsyDCLvI3&index=7",
                "https://www.youtube.com/watch?v=GdrXGs2Ipp4&index=8&list=PLZ9NgFYEMxp4p5piHxajQXRRlsyDCLvI3"
            ]
    )
    @Test
    fun stressTestAllStrategies() {
        Controller.setLogging(false)

        TestHelper.testStrategyPairs(
                TestHelper.buildAllPairs(),
                /* beingCount */ 50,
                /* palantirCount */ 10,
                /* iterations */ 100,
                /* animationSpeed */ 0f,
                /* gazingRangeMin */ 0,
                /* gazingRangeMax */ 0)
    }
}

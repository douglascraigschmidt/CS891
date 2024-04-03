package edu.vandy.recommender.microservice.completablefuture

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import edu.vandy.recommender.common.BlackBoxCommon
import edu.vandy.recommender.common.ServerBeans
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ContextConfiguration
import test.admin.AssignmentTests
import kotlin.time.ExperimentalTime

@ExperimentalTime
@WebMvcTest
@ContextConfiguration(classes = [CompletableFutureService::class, ServerBeans::class])
class CompletableFutureBlackBoxTest : AssignmentTests() {
//    @MockkBean(name = "movieMap")
//    lateinit var vectorMap: MutableMap<String, List<Double>>
//
//    @SpykBean
//    lateinit var service: CompletableFutureService
//
//    @Test
//    @Timeout(60)
//    fun `getRecommendations(single input) BlackBox test with random data set`() {
//        BlackBoxCommon.getRecommendationsSingleBlackBox(
//            vectorMap
//        ) { input, max ->
//            requireNotNull(
//                service.getRecommendations(input, max).block()
//            ).toMutableList()
//        }
//    }
//
//    @Test
//    @Timeout(60)
//    fun `getRecommendations(multiple input) BlackBox test with random data set`() {
//        BlackBoxCommon.getRecommendationsMultipleBlackBoxTest(
//            vectorMap
//        ) { input, max ->
//            requireNotNull(
//                service.getRecommendations(input, max).block()
//            ).toMutableList()
//        }
//    }
}
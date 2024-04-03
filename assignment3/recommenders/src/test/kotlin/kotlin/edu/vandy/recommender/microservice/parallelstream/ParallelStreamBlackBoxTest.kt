package edu.vandy.recommender.microservice.parallelstream

//@ExperimentalTime
//@WebMvcTest
//@ContextConfiguration(classes = [ParallelStreamService::class, ServerBeans::class])
//class ParallelStreamBlackBoxTest : AssignmentTests() {
//    @MockkBean(name = "movieMap")
//    lateinit var vectorMap: MutableMap<String, List<Double>>
//
//    @SpykBean
//    lateinit var service: ParallelStreamService
//
//    @Test
//    @Timeout(60)
//    fun `getRecommendations(single input) BlackBox test with random data set`() {
//        BlackBoxCommon.getRecommendationsSingleBlackBox(
//            vectorMap
//        ) { input, max ->
//            service.getRecommendations(input, max)
//        }
//    }
//
//    @Disabled //TODO
//    @Test
//    @Timeout(60)
//    fun `getRecommendations(multiple input) BlackBox test with random data set`() {
//        BlackBoxCommon.getRecommendationsMultipleBlackBoxTest(
//            vectorMap
//        ) { input, max ->
//            service.getRecommendations(input, max)
//        }
//    }
//}
package edu.vandy.recommender.microservice.sequentialstream

//@ExperimentalTime
//@WebMvcTest
//@ContextConfiguration(classes = [SequentialStreamService::class, ServerBeans::class])
//class SequentialStreamBlackBoxTest : AssignmentTests() {
//    @MockkBean(name = "movieMap")
//    lateinit var vectorMap: MutableMap<String, List<Double>>
//
//    @SpykBean
//    lateinit var service: SequentialStreamService
//
//    @Test
//    @Timeout(60)
//    fun `getRecommendations(single input) BlackBox test with random data set`() {
//        BlackBoxCommon.getRecommendationsSingleBlackBox(
//            vectorMap
//        ) { input, max ->
//            requireNotNull(
//                service.getRecommendations(input, max)
//            ).toMutableList()
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
//            requireNotNull(
//                service.getRecommendations(input, max)
//            ).toMutableList()
//        }
//    }
//}
package edu.vandy.recommender.microservice.concurrentflux

//@ExperimentalTime
//@WebMvcTest
//@ContextConfiguration(classes = [ConcurrentFluxService::class, ServerBeans::class])
//class ConcurrentFluxBlackBoxTest : AssignmentTests() {
//    @MockkBean(name = "movieMap")
//    lateinit var vectorMap: MutableMap<String, List<Double>>
//
//    @SpykBean
//    lateinit var service: ConcurrentFluxService
//
//    @Test
//    @Timeout(60)
//    @Disabled //TODO
//    fun `getRecommendations(single input) BlackBox test with random data set`() {
//        BlackBoxCommon.getRecommendationsSingleBlackBox(
//            vectorMap,
//            size = 100_000
//        ) { input, max ->
//            requireNotNull(
//                service.getRecommendations(input, max).collectList().block()
//            ).toMutableList()
//        }
//    }
//
//    @Test
//    @Timeout(60)
//    @Disabled // TODO
//    fun `getRecommendations(multiple input) BlackBox test with random data set`() {
//        BlackBoxCommon.getRecommendationsMultipleBlackBoxTest(
//            vectorMap, size = 1000
//        ) { input, max ->
//            requireNotNull(
//                service.getRecommendations(input, max).collectList().block()
//            ).toMutableList()
//        }
//    }
//}
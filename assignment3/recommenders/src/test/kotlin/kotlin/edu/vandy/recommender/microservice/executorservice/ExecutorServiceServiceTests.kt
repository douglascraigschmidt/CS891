package edu.vandy.recommender.microservice.executorservice

/**
 * These use mocking to isolate and test only the service component.
 */
//@WebMvcTest
//@ContextConfiguration(classes = [ExecutorServiceService::class, ServerBeans::class])
//class ExecutorServiceServiceTests : AssignmentTests() {
//
//    @MockkBean(name = "movieMap")
//    lateinit var vectorMap: MutableMap<String, List<Double>>
//
//    @SpykBean
//    lateinit var service: ExecutorServiceService
//
//    @Test
//    fun `getRecommendations handles unknown watched movie`() {
//        every { vectorMap[any()] } answers { listOf() }
//        assertThat(service.getRecommendations("?", 10)).isEmpty()
//    }
//
//    @Disabled //TODO
//    @Test
//    fun `getRecommendations(multiple) handles unknown watched movies`() {
//        every { vectorMap[any()] } answers { listOf() }
//        assertThat(service.getRecommendations(listOf("?", "?"), 10)).isEmpty()
//    }
//
//    @Test
//    fun `allMovies uses correct implementation and returns expected results`() {
//        val mockKeys = mutableSetOf("1", "2", "3")
//
//        every { vectorMap.keys } answers {
//            mockKeys
//        }
//
//        assertThat(service.allMovies).isEqualTo(mockKeys)
//
//        verify(exactly = 1) { vectorMap.keys }
//        confirmVerified(vectorMap)
//    }
//
//    @Disabled //TODO
//    @Test
//    fun `search uses correct implementation and returns expected results`() {
//        val input = "mock"
//        val output = mockk<List<Ranking>>()
//        val keys = mockk<MutableSet<String>>()
//        val strings = mockk<Stream<String>>()
//        val fs = spyk<Future<String>>()
//        val fr = spyk<Future<Ranking>>()
//        val sfs = mockk<Stream<Future<String>>>()
//        val lfs = mockk<List<Future<String>>>()
//
//        every { vectorMap.keys } answers { keys }
//        every { keys.stream() } answers { strings }
//        every { service.searchAsync(any(), input) } answers { fr }
//        every { strings.map<Future<String>>(any()) } answers {
//            assertThat(firstArg<Function<String, Future<String>>>().apply("mock")).isSameAs(
//                fs
//            )
//            sfs
//        }
//        every { sfs.collect(any<Collector<Any, Any, Any>>()) } answers { lfs }
//        every { lfs.stream() } answers { sfs }
//        every { fs.get() } answers { input }
//        every { sfs.map<String>(any()) } answers {
//            assertThat(firstArg<Function<Future<String>, String>>().apply(fs)).isSameAs(
//                input
//            )
//            strings
//        }
//        every { strings.filter(any()) } answers { strings }
//        every { strings.collect(any<Collector<Any, Any, Any>>()) } answers { output }
//
//        assertThat(service.search(input)).isSameAs(output)
//
//        verify(exactly = 1) {
//            vectorMap.keys
//            keys.stream()
//            service.searchAsync(any(), input)
//            strings.map<Future<String>>(any())
//            sfs.collect(any<Collector<Any, Any, Any>>())
//            lfs.stream()
//            fs.get()
//            sfs.map<String>(any())
//            strings.filter(any())
//            strings.collect(any<Collector<Any, Any, Any>>())
//            service.search(input)
//        }
//        confirmVerified(vectorMap, keys, strings, service, sfs, lfs, fs)
//    }
//
//    @Test
//    fun `getRecommendations uses correct implementation and returns expected results`() {
//        val input = "mock"
//        val count = 99
//        val expected = mockk<List<Ranking>>()
//        val rankings = mockk<Stream<Future<Ranking>>>()
//        val ranking = mockk<Stream<Ranking>>()
//        val rank = mockk<Ranking>()
//        val rf = mockk<Future<Ranking>>()
//        val test = mockk<Ranking>()
//
//        mockkStatic(GetTopRecommendationsStream::class)
//        every { vectorMap.containsKey(any()) } answers { true }
//        every { vectorMap[any()] } answers { mockk() }
//        every { service.computeRecommendationsFutures(any()) } answers { rankings }
//        every { rf.get() } answers { rank }
//        every { rankings.map<Ranking>(any()) } answers {
//            assertThat(
//                firstArg<Function<Future<Ranking>, Ranking>>().apply(
//                    rf
//                )
//            ).isSameAs(
//                rank
//            )
//            ranking
//        }
//        every { test.title } returnsMany listOf("mock", "amock")
//        every { ranking.filter(any()) } answers {
//            assertThat(firstArg<Predicate<Ranking>>().test(test)).isFalse
//            assertThat(firstArg<Predicate<Ranking>>().test(test)).isTrue
//            ranking
//        }
//
//        // Return the top maxCount recommendations.
//        every { getTopRecommendationsHeap(ranking, count) } answers {
//            expected
//        }
//
//        assertThat(service.getRecommendations(input, count)).isSameAs(
//            expected
//        )
//
//        verify(exactly = 1) {
//            vectorMap.containsKey(any())
//            vectorMap[any()]
//            service.computeRecommendationsFutures(any())
//            rf.get()
//            rankings.map<Ranking>(any())
//            ranking.filter(any())
//            getTopRecommendationsHeap(ranking, count)
//            service.getRecommendations(input, count)
//        }
//        verify(exactly = 2) {
//            test.title
//        }
//
//        confirmVerified(test, vectorMap, service, rf, ranking, rankings)
//    }
//
//    @Disabled //TODO
//    @Test
//    fun `getRecommendation(list input) uses correct implementation and returns expected results`() {
//        val input = mockk<List<String>>()
//        val count = 99
//        val strings = mockk<Stream<String>>()
//        val ranking = mockk<Stream<Future<Ranking>>>()
//        val expected = mockk<List<Ranking>>()
//        val futures = mockk<List<Future<Ranking>>>()
//        val rankings = mockk<Stream<Ranking>>()
//        val vector = mockk<List<Double>>()
//
//        mockkStatic(GetTopRecommendationsStream::class)
//        every { input.stream() } answers { strings }
//        every { vectorMap.containsKey(any()) } returnsMany listOf(
//            true,
//            false
//        )
//        every { strings.filter(any()) } answers {
//            assertThat(firstArg<Predicate<String>>().test("a")).isTrue
//            assertThat(firstArg<Predicate<String>>().test("b")).isFalse
//            strings
//        }
//        every { strings.flatMap(any<Function<String, Stream<Future<Ranking>>>>()) } answers {
//            firstArg<Function<String, Stream<Future<Ranking>>>>().apply("mock")
//            ranking
//        }
//        every { service.computeRecommendationsFutures(any()) } answers { ranking }
//        every { vectorMap[any()] } answers { vector }
//        every { ranking.collect(any<Collector<Any, Any, Any>>()) } answers { futures }
//        every { futures.stream() } answers { ranking }
//        every { ranking.map<Ranking>(any()) } answers { rankings }
//        every { input.contains(any()) } returnsMany listOf(true, false)
//        every { rankings.filter(any()) } answers {
//            assertThat(firstArg<Predicate<Ranking>>().test(mockk())).isFalse
//            assertThat(firstArg<Predicate<Ranking>>().test(mockk())).isTrue
//            rankings
//        }
//        every {
//            GetTopRecommendationsStream.getTopRecommendationsSort(
//                rankings,
//                count
//            )
//        } answers { expected }
//
//        assertThat(service.getRecommendations(input, count)).isSameAs(
//            expected
//        )
//
//        verify(exactly = 1) {
//            input.stream()
//            strings.filter(any())
//            strings.flatMap(any<Function<String, Stream<Future<Ranking>>>>())
//            service.getRecommendations(input, any())
//            service.computeRecommendationsFutures(any())
//            vectorMap[any()]
//            ranking.collect(any<Collector<Any, Any, Any>>())
//            futures.stream()
//            ranking.map<Ranking>(any())
//            rankings.filter(any())
//            GetTopRecommendationsStream.getTopRecommendationsSort(
//                rankings,
//                count
//            )
//        }
//        verify(exactly = 2) {
//            vectorMap.containsKey(any())
//            input.contains(any())
//        }
//        confirmVerified(
//            input, strings, service,
//            vectorMap, ranking, futures, rankings
//        )
//    }
//
//    @Test
//    fun `searchAsyncuses correct implementation and returns expected results`() {
//        val input1 = mockk<Ranking>()
//        val input2 = "Some movie"
//        val input3 = "Some move"
//        val output = mockk<Future<String>>()
//        val es = mockk<ExecutorService>()
//
//        es.injectInto(service)
//
//        every { es.submit(any<Callable<String>>()) } answers {
//            assertThat(firstArg<Callable<String>>().call()).isEqualTo(input1)
//            output
//        }
//
//        assertThat(service.searchAsync(input1, input2)).isSameAs(output)
//
//        every { es.submit(any<Callable<String>>()) } answers {
//            assertThat(firstArg<Callable<String>>().call()).isEqualTo(null)
//            output
//        }
//
//        assertThat(service.searchAsync(input1, input3)).isSameAs(output)
//
//        verify {
//            es.submit(any<Callable<String>>())
//        }
//
//        confirmVerified(es)
//    }
//
//    @Test
//    fun `computeRecommendationsFutures uses correct implementation and returns expected results`() {
//        val input = mockk<List<Double>>()
//        val output = mockk<Stream<Future<Ranking>>>()
//
//        val msme = mockk<MutableSet<MutableEntry>>()
//        val m0 = mockk<Future<Ranking>>()
//        val sme = mockk<Stream<MutableEntry>>()
//        val e = mockk<Entry>()
//
//        every { vectorMap.entries } answers { msme }
//        every { msme.stream() } answers { sme }
//        every { sme.map<Future<Ranking>>(any()) } answers {
//            assertThat(firstArg<Function<Entry, Future<Ranking>>>().apply(e)).isSameAs(
//                m0
//            )
//            output
//        }
//        every { service.rankAsync(any(), any()) } answers { m0 }
//
//        assertThat(service.computeRecommendationsFutures(input)).isSameAs(output)
//        verify(exactly = 1) {
//            vectorMap.entries
//            msme.stream()
//            sme.map<Future<Ranking>>(any())
//            service.rankAsync(any(), any())
//            service.computeRecommendationsFutures(any())
//        }
//        confirmVerified(vectorMap, sme, msme, service)
//    }
//
//    @Test
//    fun `rankAsync uses correct implementation and returns expected results`() {
//        val mk = mockk<Future<Ranking>>()
//        val v = mockk<List<Double>>()
//        val v1 = mockk<List<Double>>()
//        val e = mockk<MutableEntry>()
//        val es = mockk<ExecutorService>()
//        val md = 99.99
//        val k = "mock"
//
//        mockkStatic(CosineSimilarityUtils::class)
//        es.injectInto(service)
//
//        every { e.key } answers { k }
//        every { e.value } answers { v1 }
//        every { CosineSimilarityUtils.cosineSimilarity(v, v1, false) } answers { md }
//        every { es.submit<Ranking>(any()) } answers {
//            firstArg<Callable<Ranking>>().call()
//            mk
//        }
//
//        assertThat(service.rankAsync(e, v)).isSameAs(mk)
//
//        verify(exactly = 1) {
//            e.key
//            e.value
//            CosineSimilarityUtils.cosineSimilarity(v, v1, false)
//            es.submit<Ranking>(any())
//        }
//
//        confirmVerified(e, es)
//    }
//}
//
//typealias Entry = Map.Entry<Double, String>
//typealias MutableEntry = MutableMap.MutableEntry<String, List<Double>>

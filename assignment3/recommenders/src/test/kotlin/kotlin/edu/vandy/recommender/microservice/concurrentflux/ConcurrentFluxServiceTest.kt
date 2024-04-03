package edu.vandy.recommender.microservice.concurrentflux

/**
 * These use mocking to isolate and test only the service component.
 */
//@WebMvcTest
//@ContextConfiguration(classes = [ConcurrentFluxService::class, ServerBeans::class])
//internal class ConcurrentFluxServiceTest : AssignmentTests() {
//    @MockkBean(name = "movieMap")
//    lateinit var vectorMap: MutableMap<String, List<Double>>
//
//    @SpykBean
//    lateinit var service: ConcurrentFluxService
//
//    @Test
//    fun `getRecommendations handles unknown watched movie`() {
//        every { vectorMap[any()] } answers { listOf() }
//        assertThat(
//            service.getRecommendations("?", 10).collectList().block()
//        ).isEmpty()
//    }
//
//    @Disabled //TODO
//    @Test
//    fun `getRecommendations(multiple) handles unknown watched movies`() {
//        every { vectorMap[any()] } answers { listOf() }
//        assertThat(
//            service.getRecommendations(listOf("?", "?"), 10).collectList()
//                .block()
//        ).isEmpty()
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
//        assertThat(
//            service.allMovies.collectList().block()
//        ).isEqualTo(mockKeys.toList())
//
//        verify(exactly = 1) { vectorMap.keys }
//    }
//
//    @Test
//    fun `searchResults returns the expected results`() {
//        val input = "mock"
//        val expected = mutableListOf("aMock2", "to kill mocking bird")
//        val set = expected.toMutableSet().apply { add("moc") }
//        every { vectorMap.keys } returns set
//        assertThat(service.search(input).collectList().block()).isEqualTo(
//            expected
//        )
//    }
//
//    @Test
//    fun `search uses correct implementation`() {
//        val input = "mock"
//        val output = mockk<Flux<String>>()
//        val strings = mockk<Flux<String>>()
//        val mono = mockk<Mono<String>>()
//        val scheduler = mockk<Scheduler>()
//        val set = mockk<MutableSet<String>>()
//
//        mockkStatic(Flux::class)
//        mockkStatic(Mono::class)
//        mockkStatic(Schedulers::class)
//        every { vectorMap.keys } answers { set }
//        every { Flux.fromIterable(set) } answers { strings }
//        every { strings.flatMap<String>(any()) } answers {
//            firstArg<Function<String, Publisher<String>>>().apply(input)
//            output
//        }
//        every { Mono.fromCallable<String>(any()) } answers { mono }
//        every { Schedulers.parallel() } answers { scheduler }
//        every { mono.subscribeOn(any()) } answers { mono }
//        every { mono.filter(any()) } answers { mono }
//
//        assertThat(service.search(input)).isSameAs(output)
//
//        verify(exactly = 1) {
//            vectorMap.keys
//            Flux.fromIterable(set)
//            strings.flatMap<String>(any())
//            Mono.fromCallable<String>(any())
//            Schedulers.parallel()
//            mono.subscribeOn(any())
//            mono.filter(any())
//        }
//
//        confirmVerified(vectorMap, set, output, strings, mono, scheduler)
//        clearAllMocks()
//    }
//
//    @Test
//    fun `getRecommendations uses correct implementation and returns expected results`() {
//        val input = "mock"
//        val count = 99
//        val output = mockk<Flux<Ranking>>()
//        val vector = mockk<List<Double>>()
//        val flux = mockk<Flux<Ranking>>()
//        val similarity = mockk<Ranking>()
//
//        every { vectorMap[input] } answers { vector }
//        every { service.computeRecommendationsFlux(vector) } answers { flux }
//        every { similarity.title } returnsMany listOf("Mock", "mock", "mock1")
//        every { vectorMap.containsKey(any()) } answers { true }
//        every { flux.filter(any()) } answers {
//            assertThat(firstArg<Predicate<Ranking>>().test(similarity)).isTrue
//            assertThat(firstArg<Predicate<Ranking>>().test(similarity)).isFalse
//            assertThat(firstArg<Predicate<Ranking>>().test(similarity)).isTrue
//            flux
//        }
//        mockkStatic(GetTopRecommendationsFlux::class)
//        every {
//            getTopRecommendationsHeap(
//                flux,
//                count
//            )
//        } answers { output }
//
//        assertThat(service.getRecommendations(input, count)).isSameAs(output)
//
//        verify(exactly = 1) {
//            service.getRecommendations(input, count)
//            vectorMap[input]
//            service.computeRecommendationsFlux(vector)
//            vectorMap.containsKey(any())
//            flux.filter(any())
//            getTopRecommendationsHeap(flux, count)
//        }
//        verify(exactly = 3) { similarity.title }
//        confirmVerified(service, vectorMap, similarity, flux)
//    }
//
//    @Disabled //TODO
//    @Test
//    private fun `getRecommendation(list input) uses correct implementation and returns expected results`() {
//        val input = listOf("mock1", "mock2", "mock3")
//        val one = "mock"
//        val count = 99
//        val output = mockk<Flux<Ranking>>()
//        val vector = mockk<List<Double>>()
//        val flux = mockk<Flux<Ranking>>()
//        val similarity = mockk<Ranking>()
//        val strings = mockk<Flux<String>>()
//        val vectors = mockk<Flux<List<Double>>>()
//
//        mockkStatic(Flux::class)
//        every { Flux.fromIterable(input) } answers { strings }
//        every { strings.map<List<Double>>(any()) } answers {
//            firstArg<Function<String, List<Double>>>().apply(one)
//            vectors
//        }
//        every { vectorMap[one] } answers { vector }
//        every { vectors.flatMap<Ranking>(any()) } answers {
//            firstArg<Function<List<Double>, Flux<Ranking>>>().apply(vector)
//            flux
//        }
//        every { vectorMap.containsKey(any()) } returnsMany listOf(true, false)
//        every { strings.filter(any()) } answers {
//            assertThat(firstArg<Predicate<String>>().test("mock")).isTrue
//            assertThat(firstArg<Predicate<String>>().test("mock")).isFalse
//            strings
//        }
//        every { service.computeRecommendationsFlux(vector) } answers { flux }
//        every { similarity.title } returnsMany listOf(
//            input[0],
//            "mock4",
//            input[2],
//            "mock5"
//        )
//        every { flux.filter(any()) } answers {
//            assertThat(firstArg<Predicate<Ranking>>().test(similarity)).isFalse
//            assertThat(firstArg<Predicate<Ranking>>().test(similarity)).isTrue
//            assertThat(firstArg<Predicate<Ranking>>().test(similarity)).isFalse
//            assertThat(firstArg<Predicate<Ranking>>().test(similarity)).isTrue
//            flux
//        }
//        mockkStatic(GetTopRecommendationsFlux::class)
//        every {
//            getTopRecommendationsSort(flux, count)
//        } answers { output }
//
//        assertThat(service.getRecommendations(input, count)).isSameAs(output)
//
//        verify(exactly = 1) {
//            Flux.fromIterable(input)
//            strings.map<List<Double>>(any())
//            vectorMap[one]
//            service.getRecommendations(input, count)
//            strings.filter(any())
//            vectors.flatMap<Ranking>(any())
//            service.computeRecommendationsFlux(vector)
//            flux.filter(any())
//            getTopRecommendationsSort(flux, count)
//        }
//        verify(exactly = 4) { similarity.title }
//        verify(exactly = 2) { vectorMap.containsKey(any()) }
//
//        confirmVerified(strings, vectorMap, vectors, service, flux)
//    }
//
//    @Test
//    fun `computeRecommendationsFlux uses correct implementation and returns expected results`() {
//        val input = mockk<List<Double>>()
//        val output = mockk<Flux<Ranking>>()
//        val entries =
//            mockk<MutableSet<MutableMap.MutableEntry<String, List<Double>>>>()
//        val entry = mockk<Entry>()
//        val similarity = 9.9
//        val scheduler = mockk<Scheduler>()
//        val vector = mockk<List<Double>>()
//        val mono = mockk<Mono<Ranking>>()
//        val flux = mockk<Flux<Entry>>()
//        val key = "mock"
//
//        mockkStatic(Flux::class)
//        mockkStatic(Mono::class)
//        mockkStatic(CosineSimilarityUtils::class)
//        mockkStatic(Schedulers::class)
//
//        every { vectorMap.entries } answers { entries }
//        every { entry.value } answers { vector }
//        every { entry.key } answers { key }
//        every { Flux.fromIterable(any<Set<Entry>>()) } answers { flux }
//        every { flux.flatMap<Ranking>(any()) } answers {
//            firstArg<Function<Entry, Publisher<Ranking>>>().apply(entry)
//            output
//        }
//        every { Mono.fromCallable<Ranking>(any()) } answers {
//            firstArg<Callable<Mono<Ranking>>>().call()
//            mono
//        }
//        every { mono.subscribeOn(any()) } answers { mono }
//        every {
//            CosineSimilarityUtils.cosineSimilarity(
//                any(),
//                any(),
//                true
//            )
//        } answers { similarity }
//        every { Schedulers.parallel() } answers { scheduler }
//
//        assertThat(service.computeRecommendationsFlux(input)).isSameAs(output)
//
//        verify(exactly = 1) {
//            service.computeRecommendationsFlux(input)
//            vectorMap.entries
//            entry.value
//            entry.key
//            Flux.fromIterable(any<Set<Entry>>())
//            flux.flatMap<Ranking>(any())
//            Mono.fromCallable<Ranking>(any())
//            mono.subscribeOn(any())
//            CosineSimilarityUtils.cosineSimilarity(any(), any(), true)
//            Schedulers.parallel()
//        }
//
//        confirmVerified(service, vectorMap, entry, mono, flux)
//    }
//}
//
//typealias Entry = Map.Entry<String, List<Double>>

package edu.vandy.recommender.microservice.completablefuture

/**
 * These use mocking to isolate and test only the service component.
 */
//@WebMvcTest
//@ContextConfiguration(classes = [CompletableFutureService::class, ServerBeans::class])
//internal class CompletableFutureServiceTest : AssignmentTests() {
//    @MockkBean(name = "movieMap")
//    lateinit var vectorMap: MutableMap<String, List<Double>>
//
//    @SpykBean
//    lateinit var service: CompletableFutureService
//
//    @MockK
//    lateinit var lr: MutableList<Ranking>
//
//    @MockK
//    lateinit var mlr: Mono<List<Ranking>>
//
//    private val mockKeys = mutableSetOf("1", "2", "3")
//
//    @BeforeEach
//    fun setup() {
//        clearAllMocks()
//        mockkStatic(Converters::class)
//        every { Converters.titles2Rankings(any()) } answers { lr }
//    }
//
//    @Test
//    fun `getRecommendations handles unknown watched movie`() {
//        every { vectorMap[any()] } answers { listOf() }
//        assertThat(service.getRecommendations("?", 10).block()).isEmpty()
//    }
//
//    @Disabled //TODO
//    @Test
//    fun `getRecommendations(multiple) handles unknown watched movies`() {
//        every { vectorMap[any()] } answers { listOf() }
//        assertThat(
//            service.getRecommendations(listOf("?", "?"), 10).block()
//        ).isEmpty()
//    }
//
//    @Test
//    fun `allMovies uses correct implementation and returns expected results`() {
//        mockkStatic(Mono::class)
//
//        every { Converters.titles2Rankings(any()) } answers { lr }
//        every { Mono.just(any<List<Ranking>>()) } answers { mlr }
//
//        every { vectorMap.keys } answers {
//            mockKeys
//        }
//
//        assertThat(service.allMovies.block()).isEqualTo(lr)
//
//        verify(exactly = 1) { vectorMap.keys }
//    }
//
//    @Test
//    fun `search uses correct implementation and returns expected results`() {
//        val input = "mock"
//        val keys = mockk<MutableSet<String>>()
//        val stream = mockk<Stream<String>>()
//        val stream1 = mockk<Stream<CompletableFuture<Optional<String>>>>()
//        val future = mockk<CompletableFuture<List<String>>>()
//        val list = mockk<List<Ranking>>()
//        val sos = mockk<Stream<Optional<String>>>()
//
//        mockkStatic(Mono::class)
//        mockkStatic(CompletableFutureService::class)
//        every { vectorMap.keys } answers { keys }
//        every { Mono.fromFuture(any<CompletableFuture<List<Ranking>>>()) } answers {
//            firstArg<CompletableFuture<List<Ranking>>>()
//            mlr
//        }
//        every { keys.stream() } answers { stream }
//        every { stream.map<CompletableFuture<Optional<String>>>(any()) } answers {
//            firstArg<Function<Any, Any>>().apply("mock")
//            stream1
//        }
//        val r = mockk<Ranking>()
//        every { service.searchAsync(r, input) } answers { mockk() }
//        every { stream1.collect(any<Collector<Any, Any, Any>>()) } answers { future }
//        every { CompletableFutureService.apply(any()) } answers {
//            list
//        }
//        every { future.thenApply<List<String>>(any()) } answers {
//            firstArg<Function<Any, Any>>().apply(sos)
//            future
//        }
//
//        assertThat(service.search("mock")).isSameAs(mlr)
//
//        verify(exactly = 1) {
//            service.search(any())
//            vectorMap.keys
//            Mono.fromFuture(any<CompletableFuture<List<String>>>())
//            keys.stream()
//            stream.map<CompletableFuture<Optional<String>>>(any())
//            service.searchAsync(r, input)
//            stream1.collect(any<Collector<Any, Any, Any>>())
//            future.thenApply<List<String>>(any())
//            CompletableFutureService.apply(any())
//        }
//
//        confirmVerified(
//            vectorMap,
//            keys,
//            stream,
//            stream1,
//            sos,
//            future,
//            service
//        )
//    }
//
//    @Test
//    fun `getRecommendations(single input) uses correct implementation and returns expected results`() {
//        val input = "mock"
//        val count = 99
//        val stream1 = mockk<Stream<CompletableFuture<Ranking>>>()
//        val stream2 = mockk<CompletableFuture<Ranking>>()
//        val stream3 = mockk<CompletableFuture<List<String>>>()
//        val stream4 = mockk<Stream<Ranking>>()
//        val stream5 = mockk<List<Ranking>>()
//        val keys = mockk<List<Double>>()
//
//        mockkStatic(Mono::class)
//        every { Mono.fromFuture(any<CompletableFuture<List<Ranking>>>()) } answers {
//            firstArg<CompletableFuture<List<Ranking>>>()
//            mlr
//        }
//        every { vectorMap[input] } answers { keys }
//        every { service.computeRecommendationsStreamAsync(any()) } answers { stream1 }
//        every { vectorMap.containsKey(any()) } answers { true }
//        every { stream1.collect(any<Collector<Any, Any, Any>>()) } answers { stream2 }
//        every { stream2.thenApply<List<String>>(any()) } answers {
//            firstArg<Function<Stream<Ranking>, Any>>().apply(stream4)
//            stream3
//        }
//        every {
//            service.getTopRecommendations(input, count, stream4)
//        } answers { stream5 }
//        every { vectorMap.containsKey(any()) } answers { true }
//
//        assertThat(service.getRecommendations(input, count)).isSameAs(mlr)
//
//        verify(exactly = 1) {
//            service.getRecommendations(input, count)
//            Mono.fromFuture(any<CompletableFuture<List<String>>>())
//            vectorMap[input]
//            vectorMap.containsKey(any())
//            service.computeRecommendationsStreamAsync(any())
//            vectorMap.containsKey(any())
//            stream1.collect(any<Collector<Any, Any, Any>>())
//            stream2.thenApply<List<String>>(any())
//            service.getTopRecommendations(input, count, stream4)
//        }
//
//        confirmVerified(vectorMap, service, stream1, stream2, stream3)
//    }
//
//    @Disabled //TODO
//    @Test
//    fun `getRecommendations(multiple input) uses correct implementation and returns expected results`() {
//        val input = mockk<List<String>>()
//        val count = 99
//        val stream1 = mockk<Stream<String>>()
//        val stream2 = mockk<Stream<List<Double>>>()
//        val vector = mockk<List<Double>>()
//        val stream3 = mockk<Stream<CompletableFuture<Ranking>>>()
//        val stream4 = mockk<Stream<CompletableFuture<Ranking>>>()
//        val collector =
//            mockk<Collector<CompletableFuture<Ranking>, *, CompletableFuture<Stream<Ranking>>>>()
//        val future = mockk<CompletableFuture<Stream<Ranking>>>()
//        val future2 = mockk<CompletableFuture<List<String>>>()
//        val stream5 = mockk<Stream<Ranking>>()
//        val stream6 = mockk<Stream<Ranking>>()
//        val list = mockk<MutableList<Ranking>>()
//
//        mockkStatic(Mono::class)
//        mockkStatic(StreamOfFuturesCollector::class)
//        every { Mono.fromFuture(any<CompletableFuture<List<Ranking>>>()) } answers {
//            firstArg<CompletableFuture<List<Ranking>>>()
//            mlr
//        }
//        every { input.stream() } answers { stream1 }
//        every { stream1.map<List<Double>>(any()) } answers {
//            firstArg<Function<String, List<Double>>>().apply("mock")
//            stream2
//        }
//        every { vectorMap[any()] } answers { vector }
//        every { stream2.flatMap<CompletableFuture<Ranking>>(any()) } answers {
//            firstArg<Function<List<Double>, CompletableFuture<Ranking>>>().apply(
//                vector
//            )
//            stream3
//        }
//        every { vectorMap.containsKey(any()) } returnsMany listOf(true, false)
//        every { stream1.filter(any()) } answers {
//            assertThat(firstArg<Predicate<String>>().test("aMock")).isTrue
//            assertThat(firstArg<Predicate<String>>().test("aMock")).isFalse
//            stream1
//        }
//        every { service.computeRecommendationsStreamAsync(vector) } answers { stream4 }
//        every { stream3.collect(any<Collector<Any, Any, Any>>()) } answers {
//            future
//        }
//        every { StreamOfFuturesCollector.toFuture<Ranking>() } answers { collector }
//        every { future.thenApply<List<String>>(any()) } answers {
//            firstArg<Function<Stream<Ranking>, List<String>>>().apply(stream5)
//            future2
//        }
//        every { stream5.filter(any()) } answers { stream6 }
//        every { getTopRecommendationsSort(stream6, count) } answers { list }
//        every {
//            service.getTopRecommendations(input, count, stream5)
//        } answers { list }
//
//        assertThat(service.getRecommendations(input, count)).isSameAs(mlr)
//
//        verify(exactly = 1) {
//            service.getRecommendations(input, count)
//            Mono.fromFuture(any<CompletableFuture<List<String>>>())
//            input.stream()
//            stream1.map<List<Double>>(any())
//            vectorMap[any()]
//            stream2.flatMap<CompletableFuture<Ranking>>(any())
//            service.computeRecommendationsStreamAsync(vector)
//            stream3.collect(any<Collector<Any, Any, Any>>())
//            StreamOfFuturesCollector.toFuture<Ranking>()
//            stream1.filter(any())
//            service.getTopRecommendations(input, count, stream5)
//            future.thenApply<List<String>>(any())
//        }
//
//        verify(exactly = 2) {
//            vectorMap.containsKey(any())
//        }
//
//        confirmVerified(
//            service,
//            input,
//            stream1,
//            stream2,
//            stream3,
//            stream4,
//            stream5,
//            stream6,
//            list,
//            future,
//            future2,
//            vectorMap
//        )
//    }
//
//    @Disabled // TODO
//    @Test
//    fun `computeRecommendationsStream uses correct implementation and returns expected results`() {
//        val input = spyk(listOf(1.0, 2.0))
//        val output = mockk<Stream<CompletableFuture<Ranking>>>()
//        val entries = mockk<MutableSet<MutableEntry>>()
//        val stream = mockk<Stream<MutableEntry>>()
//        val stream1 = mockk<Stream<CompletableFuture<Ranking>>>()
//        val entry = mockk<MutableEntry>()
//        val future = mockk<CompletableFuture<Ranking>>()
//        val value = 9.9
//        val list = mockk<List<Double>>()
//        val key = "mock"
//
//        mockkStatic(CompletableFuture::class)
//        mockkStatic(CosineSimilarityUtils::class)
//        every { vectorMap.entries } answers { entries }
//        every { entries.stream() } answers { stream }
//        every { stream.map<CompletableFuture<Ranking>>(any()) } answers {
//            firstArg<Function<MutableEntry, CompletableFuture<Ranking>>>().apply(
//                entry
//            )
//            output
//        }
//        every { CompletableFuture.supplyAsync<Ranking>(any()) } answers {
//            firstArg<Supplier<Ranking>>().get()
//            future
//        }
//        every {
//            CosineSimilarityUtils.cosineSimilarity(
//                input,
//                any(),
//                false
//            )
//        } answers { value }
//        every { entry.key } answers { key }
//        every { entry.value } answers { list }
//
//        assertThat(service.computeRecommendationsStreamAsync(input)).isSameAs(
//            output
//        )
//
//        verify(exactly = 1) {
//            service.computeRecommendationsStreamAsync(input)
//            vectorMap.entries
//            entries.stream()
//            stream.map<CompletableFuture<Ranking>>(any())
//            CompletableFuture.supplyAsync<Ranking>(any())
//            CosineSimilarityUtils.cosineSimilarity(input, any(), false)
//            entry.key
//            entry.value
//        }
//
//        confirmVerified(service, vectorMap, stream, stream1, entry, output)
//    }
//
//    @Test
//    fun `getTopRecommendations uses correct implementation and returns expected results`() {
//        val input = "mock"
//        val count = 99
//        val stream = mockk<Stream<Ranking>>()
//        val ranking = mockk<Ranking>()
//
//        mockkStatic(GetTopRecommendationsStream::class)
//        every { ranking.title } returnsMany listOf("mock", "Mock")
//        every { stream.filter(any()) } answers {
//            assertThat(firstArg<Predicate<Ranking>>().test(ranking)).isFalse
//            assertThat(firstArg<Predicate<Ranking>>().test(ranking)).isTrue
//            stream
//        }
//        every { getTopRecommendationsHeap(stream, count) } answers { lr }
//
//        assertThat(
//            service.getTopRecommendations(input, count, stream)
//        ).isSameAs(lr)
//
//        verify(exactly = 1) {
//            stream.filter(any())
//            getTopRecommendationsHeap(stream, count)
//            service.getTopRecommendations(input, any(), any())
//        }
//        verify(exactly = 2) {
//            ranking.title
//        }
//        confirmVerified(ranking, stream, service)
//    }
//
//    @Test
//    fun `getTopRecommendations(multiple input) uses correct implementation and returns expected results`() {
//        val input = mockk<List<String>>()
//        val count = 99
//        val stream = mockk<Stream<Ranking>>()
//        val ranking = mockk<Ranking>()
//
//        mockkStatic(GetTopRecommendationsStream::class)
//        every { ranking.title } returnsMany listOf("mock", "Mock")
//        every { input.contains(any()) } returnsMany listOf(true, false)
//        every { stream.filter(any()) } answers {
//            assertThat(firstArg<Predicate<Ranking>>().test(ranking)).isFalse
//            assertThat(firstArg<Predicate<Ranking>>().test(ranking)).isTrue
//            stream
//        }
//        every { getTopRecommendationsSort(stream, count) } answers { lr }
//
//        assertThat(
//            service.getTopRecommendations(
//                input,
//                count,
//                stream
//            )
//        ).isSameAs(lr)
//
//        verify(exactly = 1) {
//            stream.filter(any())
//            getTopRecommendationsSort(stream, count)
//            service.getTopRecommendations(any<List<String>>(), any(), any())
//        }
//        verify(exactly = 2) {
//            ranking.title
//            input.contains(any())
//        }
//        confirmVerified(ranking, input, stream, service)
//    }
//
//    @Test
//    fun `searchAsync uses correct implementation and returns expected results`() {
//        val input =
//            listOf(Ranking("Foo", 1.0) to "fo", Ranking("Bar", 2.0) to "aBar")
//        val expected = mockk<CompletableFuture<Optional<Ranking>>>()
//        var p: Pair<Ranking, String>? = null
//
//        mockkStatic(CompletableFuture::class)
//
//        every { CompletableFuture.supplyAsync<Optional<Ranking>>(any()) } answers {
//            p!!.let {
//                if (it.first == input.first()) {
//                    assertThat(firstArg<Supplier<Optional<String>>>().get()).get()
//                        .isEqualTo(it.first)
//                } else {
//                    assertThat(firstArg<Supplier<Optional<String>>>().get()).isEmpty
//                }
//                expected
//            }
//        }
//
//        input.forEach { i ->
//            p = i
//            assertThat(
//                service.searchAsync(
//                    i.first,
//                    i.second
//                )
//            ).isSameAs(expected)
//        }
//
//        verify(exactly = 2) {
//            service.searchAsync(any(), any())
//            CompletableFuture.supplyAsync<Optional<String>>(any())
//        }
//    }
//}
//
//typealias MutableEntry = MutableMap.MutableEntry<String, List<Double>>

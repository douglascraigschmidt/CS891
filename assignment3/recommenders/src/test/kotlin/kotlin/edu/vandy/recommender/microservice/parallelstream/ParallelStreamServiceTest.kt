package edu.vandy.recommender.microservice.parallelstream

/**
 * These use mocking to isolate and test only the service component.
 */
//@WebMvcTest
//@ContextConfiguration(classes = [ParallelStreamService::class, ServerBeans::class])
//internal class ParallelStreamServiceTest : AssignmentTests() {
//
//    @MockkBean(name = "movieMap")
//    lateinit var vectorMap: MutableMap<String, List<Double>>
//
//    @SpykBean
//    lateinit var service: ParallelStreamService
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
//    }
//
//    @Disabled //TODO
//    @Test
//    fun `search uses correct implementation and returns expected results`() {
//        val input = "mock"
//        val mockKeys = mockk<MutableSet<String>>()
//        val mockStream = mockk<Stream<String>>()
//        val expected = listOf("mock1", "mock2", "mock3")
//        val mockSet = spyk(expected)
//
//        every { mockStream.collect(any<Collector<Any, Any, Any>>()) } returns mockSet
//        every { mockStream.filter(any()) } answers {
//            assertThat(firstArg<Predicate<String>>().test("aMock")).isTrue
//            assertThat(firstArg<Predicate<String>>().test("Moc")).isFalse
//            mockStream
//        }
//        every { mockKeys.parallelStream() } returns mockStream
//        every { vectorMap.keys } returns mockKeys
//        assertThat(service.search(input)).isEqualTo(expected)
//
//        verify(exactly = 1) {
//            mockStream.filter(any())
//            mockStream.collect(any<Collector<Any, Any, Any>>())
//            mockKeys.parallelStream()
//            vectorMap.keys
//        }
//
//        confirmVerified(mockKeys, mockStream, vectorMap)
//    }
//
//    @Disabled //TODO
//    @Test
//    fun `getRecommendations uses correct implementation and returns expected results`() {
//        val count = 999
//        val input = "mock"
//        val stream = mockk<Stream<Ranking>>()
//        val vector = List(1) { it.toDouble() }
//        val expected = mockk<MutableList<String>>()
//        val entry = mockk<Ranking>()
//
//        mockkStatic(GetTopRecommendationsStream::class)
//        every { vectorMap[input] } answers { vector }
//        every { stream.filter(any()) } answers {
//            assertThat(firstArg<Predicate<Ranking>>().test(entry)).isTrue
//            assertThat(firstArg<Predicate<Ranking>>().test(entry)).isFalse
//            stream
//        }
//        every { getTopRecommendationsHeap(stream, count) } answers { expected }
//        //TODO every { service.computeRecommendationsStream(vector) } answers { stream }
//        every { entry.title } returnsMany listOf("mock1", "mock")
//        every { vectorMap.containsKey(any()) } answers { true }
//        every { stream.sequential() } answers { stream }
//
//        val result = service.getRecommendations(input, count)
//        assertThat(result).isSameAs(expected)
//
//        verify(exactly = 1) {
//            stream.filter(any())
//            //TODO service.computeRecommendationsStream(vector)
//            getTopRecommendationsHeap(stream, count)
//            vectorMap.containsKey(any())
//            service.getRecommendations(input, count)
//            stream.sequential()
//            vectorMap[input]
//        }
//        verify(exactly = 2) { entry.title }
//
//        confirmVerified(entry, stream, vectorMap, service)
//    }
//
//    @Disabled //TODO
//    @Test
//    fun `getRecommendation(list input) uses correct implementation and returns expected results`() {
//        val vector = mockk<List<Double>>()
//        val sStream = mockk<Stream<Stream<Ranking>>>()
//        val inputStream = mockk<Stream<String>>()
//        val entry = mockk<Ranking>()
//        val stream = mockk<Stream<Ranking>>()
//        val expected = mockk<MutableList<String>>()
//        val input = spyk(listOf("mock1", "mock2"))
//        val oStream = mockk<Optional<Stream<Ranking>>>()
//        val vectorStream = mockk<Stream<List<Double>>>()
//
//        mockkStatic(GetTopRecommendationsStream::class)
//        every { sStream.reduce(any()) } answers { oStream }
//        every { oStream.orElse(any()) } answers { stream }
//        every { getTopRecommendationsSort(any(), any()) } answers { expected }
//        every { stream.filter(any()) } answers {
//            assertThat(firstArg<Predicate<Ranking>>().test(entry)).isFalse
//            assertThat(firstArg<Predicate<Ranking>>().test(entry)).isTrue
//            assertThat(firstArg<Predicate<Ranking>>().test(entry)).isFalse
//            assertThat(firstArg<Predicate<Ranking>>().test(entry)).isTrue
//            stream
//        }
//        //TODO every { service.computeRecommendationsStream(any()) } answers { stream }
//        every { vectorMap.containsKey(any()) } returnsMany listOf(true, false)
//        every { inputStream.filter(any()) } answers {
//            assertThat(firstArg<Predicate<String>>().test("mock")).isTrue
//            assertThat(firstArg<Predicate<String>>().test("mock")).isFalse
//            inputStream
//        }
//        every { input.parallelStream() } answers { inputStream }
//        every { inputStream.map<List<Double>>(any()) } answers {
//            firstArg<Function<String, List<Double>>>().apply("mock")
//            vectorStream
//        }
//        every { vectorMap[any()] } answers { vector }
//        every { entry.title } returnsMany listOf(
//            "mock1",
//            "mock3",
//            "mock2",
//            "mock4"
//        )
//        every { vectorStream.map<Stream<Ranking>>(any()) } answers {
//            firstArg<Function<List<Double>, Stream<Ranking>>>()
//                .apply(listOf(1.0, 2.0))
//            sStream
//        }
//
//        service.getRecommendations(input, 999)
//
//        verify(exactly = 1) {
//            inputStream.map<Stream<Ranking>>(any())
//            getTopRecommendationsSort(any(), any())
//            vectorMap[any()]
//            sStream.reduce(any())
//            oStream.orElse(any())
//            inputStream.filter(any())
//            input.parallelStream()
//            //TODO service.computeRecommendationsStream(any())
//            stream.filter(any())
//            inputStream.map<List<Double>>(any())
//            service.getRecommendations(input, 999)
//        }
//        verify(exactly = 4) {
//            entry.title
//            input.contains(any())
//        }
//        verify(exactly = 2) {
//            vectorMap.containsKey(any())
//        }
//
//        confirmVerified(service, inputStream, sStream, oStream, stream)
//
//    }
//
//    @Test
//    fun `computeRecommendationsStream uses correct implementation and returns expected results`() {
//        val mockValue = listOf(1.0, 2.0)
//        val mockInput = listOf(1.0)
//        val expected = mockk<Stream<Ranking>>()
//        val mockEntryStream = mockk<Stream<MutableEntry>>()
//        val mockCosine = 99.99
//        val mockEntry = mockk<MutableEntry>()
//        val mockEntries = mockk<MutableSet<MutableEntry>>()
//
//        mockkStatic(CosineSimilarityUtils::class)
//        every { mockEntry.key } answers { "key" }
//        every {
//            CosineSimilarityUtils.cosineSimilarity(
//                mockInput,
//                mockValue,
//                false
//            )
//        } answers {
//            mockCosine
//        }
//        every { mockEntries.parallelStream() } answers { mockEntryStream }
//        every { mockEntryStream.map<Ranking>(any()) } answers {
//            firstArg<Function<MutableEntry, Ranking>>().apply(mockEntry)
//            expected
//        }
//        every { mockEntry.value } answers { mockValue }
//        every { vectorMap.entries } answers { mockEntries }
//
//        assertThat(service.computeRecommendationsStream(mockInput)).isSameAs(
//            expected
//        )
//
//        verify(exactly = 1) {
//            CosineSimilarityUtils.cosineSimilarity(mockInput, mockValue, false)
//            mockEntries.parallelStream()
//            mockEntryStream.map<Entry>(any())
//            vectorMap.entries
//        }
//        verify {
//            mockEntry.key
//            mockEntry.value
//        }
//        confirmVerified(mockEntry, mockEntries, mockEntryStream, expected)
//    }
//}
//
//typealias Entry = Map.Entry<Double, String>
//typealias MutableEntry = MutableMap.MutableEntry<String, List<Double>>

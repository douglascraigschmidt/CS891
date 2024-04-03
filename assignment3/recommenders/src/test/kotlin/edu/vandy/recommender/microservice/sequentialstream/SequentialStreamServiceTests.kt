package edu.vandy.recommender.microservice.sequentialstream

/**
 * These use mocking to isolate and test only the service component.
 */
//@WebMvcTest
//@ContextConfiguration(classes = [SequentialStreamService::class, ServerBeans::class])
//internal class SequentialStreamServiceTests : AssignmentTests() {
//
//    @MockkBean(name = "movieMap")
//    lateinit var vectorMap: MutableMap<String, List<Double>>
//
//    @SpykBean
//    lateinit var service: SequentialStreamService
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
//        val result = service.allMovies
//        assertThat(result).isEqualTo(mockKeys)
//
//        verify(exactly = 1) { vectorMap.keys }
//    }
//
//    @Disabled //TODO
//    @Test
//    fun `search uses correct implementation and returns expected results`() {
//        val input = "mock"
//        val mockKeys = mockk<MutableSet<String>>()
//        val expected = listOf("mock1", "mock2", "mock3")
//        val mockSet = spyk(expected)
//        val mockStream = mockk<Stream<String>>()
//
//        every { mockStream.filter(any()) } answers {
//            assertThat(firstArg<Predicate<String>>().test("aMock")).isTrue
//            assertThat(firstArg<Predicate<String>>().test("Moc")).isFalse
//            mockStream
//        }
//        every { mockStream.collect(any<Collector<Any, Any, Any>>()) } returns mockSet
//        every { mockKeys.stream() } returns mockStream
//        every { vectorMap.keys } returns mockKeys
//
//        assertThat(service.search(input)).isEqualTo(expected)
//
//        verify(exactly = 1) {
//            mockStream.filter(any())
//            mockStream.collect(any<Collector<Any, Any, Any>>())
//            mockKeys.stream()
//            vectorMap.keys
//        }
//
//        confirmVerified(mockKeys, mockStream, vectorMap)
//    }
//
//    @Disabled //TODO
//    @Test
//    fun `getRecommendations uses correct implementation and returns expected results`() {
//        val stream = mockk<Stream<Ranking>>()
//        val count = 999
//        val expected = mockk<MutableList<String>>()
//        val entry = mockk<Ranking>()
//        val input = "mock"
//        val vector = List(1) { it.toDouble() }
//
//        mockkStatic(GetTopRecommendationsStream::class)
//        every { entry.title } returnsMany listOf("mock1", "mock")
//        //TODO
////        every { service.getRankingsStream(vector) } answers { stream }
//        every {
//            getTopRecommendationsHeap(
//                stream,
//                count
//            )
//        } answers { expected }
//        every { vectorMap.containsKey(any()) } answers { true }
//        every { stream.filter(any()) } answers {
//            assertThat(firstArg<Predicate<Ranking>>().test(entry)).isTrue
//            assertThat(firstArg<Predicate<Ranking>>().test(entry)).isFalse
//            stream
//        }
//        every { vectorMap[input] } answers { vector }
//
//        val result = service.getRecommendations(input, count)
//        assertThat(result).isSameAs(expected)
//
//        verify(exactly = 1) {
//            getTopRecommendationsHeap(stream, count)
//            //TODO
////            service.getRankingsStream(vector)
//            vectorMap.containsKey(any())
//            service.getRecommendations(input, count)
//            stream.filter(any())
//            vectorMap[input]
//        }
//        verify(exactly = 2) {
//            entry.title
//        }
//
//        confirmVerified(entry, stream, vectorMap, service)
//    }
//
//    @Disabled //TODO
//    @Test
//    fun `getRecommendation(list input) uses correct implementation and returns expected results`() {
//        val expected = mockk<MutableList<String>>()
//        val vectorStream = mockk<Stream<List<Double>>>()
//        val stream = mockk<Stream<Ranking>>()
//        val input = mockk<List<String>>()
//        val inputStream = mockk<Stream<String>>()
//        val entry = mockk<Ranking>()
//        val list = mockk<List<Double>>()
//
//        mockkStatic(GetTopRecommendationsStream::class)
//        every { stream.filter(any()) } answers {
//            assertThat(firstArg<Predicate<Ranking>>().test(entry)).isFalse
//            assertThat(firstArg<Predicate<Ranking>>().test(entry)).isTrue
//            stream
//        }
//        every { input.contains(any()) } returnsMany listOf(true, false)
//        every {
//            getTopRecommendationsSort(
//                any(),
//                any()
//            )
//        } answers { expected }
//        every { entry.title } returnsMany listOf("mock1", "mock")
//        every { vectorStream.flatMap<Ranking>(any()) } answers {
//            firstArg<Function<List<Double>, Ranking>>().apply(list)
//            stream
//        }
//        every { input.stream() } answers { inputStream }
//        every { vectorMap.containsKey(any()) } returnsMany listOf(true, false)
//        every { inputStream.filter(any()) } answers {
//            assertThat(firstArg<Predicate<String>>().test("mock")).isTrue
//            assertThat(firstArg<Predicate<String>>().test("mock")).isFalse
//            inputStream
//        }
////TODO
////        every { service.getRankingsStream(any<List<Double>>()) } answers { stream }
//        every { inputStream.map<List<Double>>(any()) } answers { vectorStream }
//
//        service.getRecommendations(input, 999)
//
//        verify(exactly = 1) {
//            stream.filter(any())
//            vectorStream.flatMap<CosineSimilarityUtils>(any())
//            service.getRecommendations(input, 999)
//            inputStream.map<List<Double>>(any())
//            inputStream.filter(any())
//            //TODO
////            service.getRankingsStream(any<List<Double>>())
//            input.stream()
//            getTopRecommendationsSort(any(), any())
//        }
//        verify(exactly = 2) {
//            entry.title
//            input.contains(any())
//            vectorMap.containsKey(any())
//        }
//
//        confirmVerified(inputStream, vectorStream, service, stream, vectorMap)
//
//    }
//
//    @Disabled //TODO
//    @Test
//    fun `computeRecommendationsStream uses correct implementation and returns expected results`() {
//        val expected = mockk<Stream<Entry>>()
//        val mockEntries = mockk<MutableSet<MutableEntry>>()
//        val mockEntry = mockk<MutableEntry>()
//        val mockEntryStream = mockk<Stream<MutableEntry>>()
//        val mockValue = listOf(1.0, 2.0)
//        val mockCosine = 99.99
//        val mockInput = listOf(1.0)
//
//        mockkStatic(CosineSimilarityUtils::class)
//
//        every { mockEntryStream.map<Entry>(any()) } answers {
//            firstArg<Function<MutableEntry, Entry>>().apply(mockEntry)
//            expected
//        }
//        every { mockEntry.value } answers { mockValue }
//        every { mockEntries.stream() } answers { mockEntryStream }
//        every {
//            CosineSimilarityUtils.cosineSimilarity(mockInput, mockValue, false)
//        } answers { mockCosine }
//        every { mockEntry.key } answers { "key" }
//        every { vectorMap.entries } answers { mockEntries }
//
//        //TODO
////        assertThat(service.getRankingsStream(mockInput)).isSameAs(
////            expected
////        )
//
//        verify(exactly = 1) {
//            vectorMap.entries
//            mockEntryStream.map<Entry>(any())
//            mockEntries.stream()
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

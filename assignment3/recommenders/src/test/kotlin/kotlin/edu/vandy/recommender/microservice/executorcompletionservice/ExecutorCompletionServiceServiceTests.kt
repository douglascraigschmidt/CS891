package edu.vandy.recommender.microservice.executorcompletionservice

/**
 * These use mocking to isolate and test only the service component.
 */
//@WebMvcTest
//@ContextConfiguration(classes = [ExecutorCompletionServiceService::class, ServerBeans::class])
//class ExecutorCompletionServiceServiceTests : AssignmentTests() {
//
//    @MockkBean(name = "movieMap")
//    lateinit var vectorMap: MutableMap<String, List<Double>>
//
//    @SpykBean
//    lateinit var service: ExecutorCompletionServiceService
//
//    @MockkBean
//    lateinit var es: ExecutorService
//
//    @MockkBean
//    lateinit var css: CompletionService<String>
//
//    @MockkBean
//    lateinit var csr: CompletionService<Ranking>
//
//    @MockK
//    lateinit var ls: List<String>
//
//    @MockK
//    lateinit var lr: List<Ranking>
//
//    @BeforeEach
//    fun beforeEach() {
//        es.injectInto(service)
//        css.injectInto(service, "mSearchCompletionService")
//        csr.injectInto(service, "mRankingCompletionService")
//        mockkStatic(Converters::class)
//        every { Converters.titles2Rankings(any()) } answers { lr }
//    }
//
//    @Test
//    fun `getRecommendations handles unknown watched movie`() {
//        every { vectorMap[any()] } answers { listOf() }
//        assertThat(service.getRecommendations("?", 10)).isEmpty()
//    }
//
//    @Disabled //TODO
//    @Test
//    fun `getRecommendations(list input) handles unknown watched movies`() {
//        every { vectorMap[any()] } answers { listOf() }
//        assertThat(service.getRecommendations(listOf("?", "?"), 10)).isEmpty()
//    }
//
//    @Test
//    fun `allMovies uses correct implementation and returns expected results`() {
//        assertThat(service.allMovies).isEqualTo(lr)
//
//        verify(exactly = 1) {
//            vectorMap.keys
//            Converters.titles2Rankings(any())
//        }
//
//        confirmVerified(vectorMap)
//    }
//
//    @Disabled //TODO
//    @Test
//    fun `search uses correct implementation and returns expected results`() {
//        val input = "mock1"
//        val keys = spyk(mutableSetOf("a", "b", "c"))
//        val ss = mockk<Stream<String>>()
//        val fs = spyk<Future<String>>()
//        val sfs = mockk<Stream<Future<String>>>()
//        val t = "mock1"
//        val ms = "mock"
//        val ins = mockk<IntStream>()
//
//        mockkStatic(IntStream::class)
//        mockkStatic(ExceptionUtils::class)
//        every { keys.size } answers { 99 }
//        every { vectorMap.keys } answers { keys }
//        every { service.searchAsync(any(), any(), any()) } answers { }
//        every { keys.forEach(any<Consumer<String>>()) } answers {
//            firstArg<Consumer<String>>().accept(t)
//        }
//        every { IntStream.rangeClosed(any(), any()) } answers { ins }
//        every { css.take() } answers { fs }
//        every { fs.get() } answers { ms }
//        every { ExceptionUtils.supplyOrThrow(any<Supplier_WithExceptions<*>>()) } answers {
//            firstArg<Supplier_WithExceptions<*>>().get()
//        }
//        every { ins.mapToObj<Future<String>>(any()) } answers {
//            val r = firstArg<IntFunction<Future<String>>>().apply(1)
//            assertThat(r).isSameAs(fs)
//            sfs
//        }
//        every { sfs.map<String>(any()) } answers {
//            val r = firstArg<Function<Future<String>, String>>().apply(fs)
//            assertThat(r).isSameAs(ms)
//            ss
//        }
//        every { ss.filter(any()) } answers {
//            assertThat(firstArg<Predicate<String?>>().test("")).isTrue
//            assertThat(firstArg<Predicate<String?>>().test(null)).isFalse
//            ss
//        }
//        every { ss.collect(any<Collector<Any, Any, Any>>()) } answers { ls }
//
//        assertThat(service.search(input)).isSameAs(lr)
//
//        verify {
//            keys.size
//            vectorMap.keys
//            service.searchAsync(any(), any(), any())
//            keys.forEach(any<Consumer<String>>())
//            IntStream.rangeClosed(any(), any())
//            css.take()
//            fs.get()
//            ss.filter(any())
//            ins.mapToObj<Future<String>>(any())
//            sfs.map<String>(any())
//            ss.collect(any<Collector<Any, Any, Any>>())
//            service.search(input)
//        }
//
//        verify(exactly = 2) {
//            ExceptionUtils.supplyOrThrow(any<Supplier_WithExceptions<*>>())
//        }
//
//        confirmVerified(keys, vectorMap, service, css, fs, ss, ins, sfs)
//    }
//
//    @Test
//    fun `getRecommendations uses correct implementation and returns expected results`() {
//        val input = "mock"
//        val c = 99
//        val ranking = mockk<Stream<Ranking>>()
//        val rank = mockk<Ranking>()
//
//        mockkStatic(GetTopRecommendationsStream::class)
//        every { vectorMap.containsKey(any()) } answers { true }
//        every { vectorMap[any()] } answers { mockk() }
//        every { service.computeRecommendations(any()) } answers { ranking }
//        every { rank.title } returnsMany listOf("mock", "mock1")
//        every { ranking.filter(any()) } answers {
//            assertThat(firstArg<Predicate<Ranking>>().test(rank)).isFalse
//            assertThat(firstArg<Predicate<Ranking>>().test(rank)).isTrue
//            ranking
//        }
//        every { getTopRecommendationsHeap(ranking, c) } answers { lr }
//
//        assertThat(service.getRecommendations(input, c)).isSameAs(lr)
//
//        verify(exactly = 1) {
//            vectorMap.containsKey(any())
//            vectorMap[any()]
//            service.computeRecommendations(any())
//            ranking.filter(any())
//            getTopRecommendationsHeap(ranking, c)
//            service.getRecommendations(any<String>(), any())
//        }
//        verify(exactly = 2) {
//            rank.title
//        }
//        confirmVerified(vectorMap, service, rank, ranking)
//    }
//
//    @Disabled //TODO
//    @Test
//    fun `getRecommendation(list input) uses correct implementation and returns expected results`() {
//        val count = 99
//        val strings = mockk<Stream<String>>()
//        val ranking = mockk<Stream<Ranking>>()
//        val lr = mockk<List<Ranking>>()
//        val vector = mockk<List<Double>>()
//        val rank = mockk<Ranking>()
//
//        mockkStatic(GetTopRecommendationsStream::class)
//        every { ls.stream() } answers { strings }
//        every { vectorMap.containsKey(any()) } returnsMany listOf(
//            true,
//            false
//        )
//        every { strings.filter(any()) } answers {
//            assertThat(firstArg<Predicate<String>>().test("a")).isTrue
//            assertThat(firstArg<Predicate<String>>().test("b")).isFalse
//            strings
//        }
//        every { strings.flatMap(any<Function<String, Stream<Ranking>>>()) } answers {
//            firstArg<Function<String, Stream<Future<Ranking>>>>().apply("mock")
//            ranking
//        }
//        every { service.computeRecommendations(any()) } answers { ranking }
//        every { vectorMap[any()] } answers { vector }
//        every { ranking.collect(any<Collector<Any, Any, Any>>()) } answers { lr }
//
//        every { lr.stream() } answers { ranking }
//        every { ls.contains(any()) } returnsMany listOf(true, false)
//        every { rank.title } returnsMany listOf("mock", "mock1")
//        every { ranking.filter(any()) } answers {
//            assertThat(firstArg<Predicate<Ranking>>().test(rank)).isFalse
//            assertThat(firstArg<Predicate<Ranking>>().test(rank)).isTrue
//            ranking
//        }
//        every {
//            GetTopRecommendationsStream.getTopRecommendationsSort(
//                ranking,
//                count
//            )
//        } answers { lr }
//
//        assertThat(service.getRecommendations(ls, count)).isSameAs(
//            lr
//        )
//
//        verify(exactly = 1) {
//            ls.stream()
//            strings.filter(any())
//            strings.flatMap(any<Function<String, Stream<Future<Ranking>>>>())
//            service.computeRecommendations(any())
//            service.getRecommendations(ls, any())
//            vectorMap[any()]
//            ranking.collect(any<Collector<Any, Any, Any>>())
//            ranking.filter(any())
//            lr.stream()
//            GetTopRecommendationsStream.getTopRecommendationsSort(
//                ranking,
//                count
//            )
//        }
//        verify(exactly = 2) {
//            vectorMap.containsKey(any())
//            ls.contains(any())
//            rank.title
//        }
//        confirmVerified(ls, strings, service, vectorMap, ranking, lr, rank)
//    }
//
//    @Test
//    fun `searchAsync uses correct implementation and returns expected results`() {
//        val input0 = mockk<CompletionService<Ranking>>()
//        val i1 = mockk<Ranking>()
//        val i2 = "i2"
//        val i3 = "i3"
//        val output = mockk<Future<Ranking>>()
//
//        every { input0.submit(any<Callable<Ranking>>()) } answers {
//            assertThat(firstArg<Callable<Ranking>>().call()).isEqualTo(i1)
//            output
//        }
//
//        service.searchAsync(input0, i1, i2)
//
//        every { input0.submit(any<Callable<Ranking>>()) } answers {
//            assertThat(firstArg<Callable<String>>().call()).isEqualTo(null)
//            output
//        }
//
//        service.searchAsync(input0, i1, i3)
//
//        verify {
//            input0.submit(any<Callable<Ranking>>())
//        }
//
//        confirmVerified(input0)
//    }
//
//    @Test
//    fun `computeRecommendations uses correct implementation and returns expected results`() {
//        val msme = mockk<MutableSet<MutableEntry>>()
//        val input = mockk<List<Double>>()
//        val me = mockk<MutableEntry>()
//        val ins = mockk<IntStream>()
//        val fr = mockk<Future<Ranking>>()
//        val sfr = mockk<Stream<Future<Ranking>>>()
//        val sr = mockk<Stream<Ranking>>()
//
//        mockkStatic(ExceptionUtils::class)
//        mockkStatic(IntStream::class)
//        every { vectorMap.entries } answers { msme }
//        every { msme.forEach(any<Consumer<MutableEntry>>()) } answers {
//            firstArg<Consumer<MutableEntry>>().accept(me)
//        }
//        every { service.rankAsync(csr, any(), any()) } answers { }
//        every { msme.size } answers { 99 }
//        every { IntStream.rangeClosed(1, 99) } answers { ins }
//        every { ins.mapToObj<Future<Ranking>>(any()) } answers {
//            assertThat(firstArg<IntFunction<Future<Ranking>>>().apply(1)).isSameAs(
//                fr
//            )
//            sfr
//        }
//        every { ExceptionUtils.supplyOrThrow(any<Supplier_WithExceptions<*>>()) } answers {
//            firstArg<Supplier_WithExceptions<*>>().get()
//        }
//        every { sfr.map<Ranking>(any()) } answers { sr }
//        every { sr.filter(any()) } answers { sr }
//        every { csr.take() } answers { fr }
//
//        assertThat(service.computeRecommendations(input)).isSameAs(sr)
//
//        verify(exactly = 1) {
//            msme.forEach(any<Consumer<MutableEntry>>())
//            service.rankAsync(csr, any(), any())
//            msme.size
//            IntStream.rangeClosed(1, 99)
//            ins.mapToObj<Future<Ranking>>(any())
//            ExceptionUtils.supplyOrThrow(any<Supplier_WithExceptions<*>>())
//            sfr.map<Ranking>(any())
//            sr.filter(any())
//            csr.take()
//            service.computeRecommendations(any())
//        }
//        verify(exactly = 2) {
//            vectorMap.entries
//        }
//        confirmVerified(vectorMap, msme, service, ins, sfr, sr, csr)
//    }
//
//    @Test
//    fun `rankAsync uses correct implementation and returns expected results`() {
//        val i0 = mockk<CompletionService<Ranking>>()
//        val mk = mockk<Future<Ranking>>()
//        val v = mockk<List<Double>>()
//        val v1 = mockk<List<Double>>()
//        val e = mockk<MutableEntry>()
//        val es = mockk<ExecutorService>()
//        val md = 99.99
//        val k = "mock"
//
//        mockkStatic(CosineSimilarityUtils::class)
//
//        every { e.key } answers { k }
//        every { e.value } answers { v1 }
//        every {
//            CosineSimilarityUtils.cosineSimilarity(
//                v,
//                v1,
//                false
//            )
//        } answers { md }
//        every { i0.submit(any()) } answers {
//            firstArg<Callable<Ranking>>().call()
//            mk
//        }
//
//        service.rankAsync(i0, e, v)
//
//        verify(exactly = 1) {
//            e.key
//            e.value
//            CosineSimilarityUtils.cosineSimilarity(v, v1, false)
//            i0.submit(any())
//            service.rankAsync(any(), any(), any())
//        }
//
//        confirmVerified(e, es, i0, service)
//    }
//}
//
//typealias MutableEntry = MutableMap.MutableEntry<String, List<Double>>

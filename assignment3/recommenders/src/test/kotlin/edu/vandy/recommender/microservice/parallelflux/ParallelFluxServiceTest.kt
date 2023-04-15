package edu.vandy.recommender.microservice.parallelflux

import edu.vandy.recommender.common.Converters
import edu.vandy.recommender.common.CosineSimilarityUtils
import edu.vandy.recommender.common.GetTopRecommendationsFlux
import edu.vandy.recommender.common.GetTopRecommendationsFlux.getTopRecommendationsHeap
import edu.vandy.recommender.common.GetTopRecommendationsFlux.getTopRecommendationsSort
import edu.vandy.recommender.common.ServerBeans
import edu.vandy.recommender.common.model.Ranking
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ContextConfiguration
import reactor.core.publisher.Flux
import reactor.core.publisher.ParallelFlux
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import test.admin.AssignmentTests
import test.admin.injectInto
import java.util.function.Function
import java.util.function.Predicate

/**
 * These use mocking to isolate and test only the service component.
 */
@WebMvcTest
@ContextConfiguration(classes = [ParallelFluxService::class, ServerBeans::class])
internal class ParallelFluxServiceTest : AssignmentTests() {
    @MockK
    lateinit var vectorMap: MutableMap<String, List<Double>>

    @SpyK
    var service = ParallelFluxService()

    @MockK
    lateinit var lr: List<Ranking>

    @MockK
    lateinit var sch: Scheduler

    private val keys = mutableSetOf("1", "2", "3")

    @BeforeEach
    fun setup() {
        mockkStatic(Schedulers::class)
        every { Schedulers.parallel() } returns Schedulers.immediate()
        every { Schedulers.immediate() } throws Exception("Schedulers.immediate() should not be called.")
        every { Schedulers.newParallel(any()) } throws Exception("Schedulers.newParallel() should not be called.")
        every {
            Schedulers.newParallel(
                any(),
                any<Int>()
            )
        } throws Exception("Schedulers.newParallel() should not be called.")
        every { Schedulers.single() } throws Exception("Schedulers.single() should not be called.")

        mockkStatic(Converters::class)
        every { Converters.titles2Rankings(any()) } answers { lr }
        vectorMap.injectInto(service)
    }

    @Test
    fun `getRecommendations handles unknown watched movie`() {
        val vectorMap = mutableMapOf(
            "1" to listOf(1.0, 2.0, 3.0),
            "2" to listOf(2.0, 3.0, 4.0),
            "3" to listOf(3.0, 4.0, 5.0)
        ).also {
            it.injectInto(service)
        }
        assertThat(
            service.getRecommendations("?", 10).collectList().block()
        ).isEmpty()
    }

    @Test
    fun `getRecommendations(multiple) handles unknown watched movies`() {
        val vectorMap = mutableMapOf(
            "1" to listOf(1.0, 2.0, 3.0),
            "2" to listOf(2.0, 3.0, 4.0),
            "3" to listOf(3.0, 4.0, 5.0)
        ).also {
            it.injectInto(service)
        }
        assertThat(
            service.getRecommendations(mutableListOf("?", "?"), 10)
                .collectList().block()
        ).isEmpty()
    }

    @Test
    fun `allMovies uses correct implementation and returns expected results`() {
        val fr = mockk<Flux<Ranking>>()
        mockkStatic(Flux::class)
        every { Flux.fromIterable<Ranking>(any()) }.answers { fr }
        every { vectorMap.keys } answers { keys }

        assertThat(service.allMovies).isSameAs(fr)

        verify(exactly = 1) {
            vectorMap.keys
            Flux.fromIterable<Ranking>(any())
            service.allMovies
            Converters.titles2Rankings(any())
        }
        confirmVerified(fr, service)
    }

    @Test
    fun `search uses correct implementation and returns expected results`() {
        val input = "mock"
        val fr = mockk<Flux<Ranking>>()
        val pfs = mockk<ParallelFlux<String>>()
        val pfr = mockk<ParallelFlux<Ranking>>()
        val r1 = mockk<Ranking>()
        val r2 = mockk<Ranking>()

        mockkStatic(Comparator::class)
        mockkStatic(Flux::class)
        every { Flux.fromIterable<Ranking>(any()) }.answers { fr }
        every { fr.parallel() }.answers { pfr }
        every { pfr.runOn(any()) }.answers { pfr }
        every { pfr.filter(any()) }.answers {
            assertThat(
                firstArg<Predicate<Ranking>>().test(
                    Ranking(
                        "aMock",
                        0.0
                    )
                )
            ).isTrue
            assertThat(
                firstArg<Predicate<Ranking>>().test(
                    Ranking(
                        "Moc",
                        0.0
                    )
                )
            ).isFalse
            pfr
        }
        every { r1.title } returns "b"
        every { r2.title } returns "a"
        every { pfr.sequential() }.answers { fr }
        every { fr.sort(any<Comparator<Ranking>>()) } answers {
            assertThat(
                firstArg<Comparator<Ranking>>().compare(
                    r1,
                    r2
                )
            ).isEqualTo(1)
            fr
        }

        assertThat(service.search(input)).isSameAs(fr)

        verify(exactly = 1) {
            Flux.fromIterable<Ranking>(any())
            fr.parallel()
            pfr.runOn(any())
            fr.sort(any<Comparator<Ranking>>())
            pfr.filter(any())
            pfr.sequential()
            service.search(input)
            r1.title
            r2.title
            Schedulers.parallel()
            Converters.titles2Rankings(any())
        }

        confirmVerified(r1, r2, fr, pfr, pfs, service)
    }

    @Test
    fun `searchResults returns the expected results`() {
        val input = "mock"
        val expected = mutableListOf("aMock2", "to kill mocking bird")
        val set = expected.toMutableSet().apply { add("moc") }
        every { vectorMap.keys } returns set
        assertThat(
            service.search(input).collectList().block()?.sort()
        ).isEqualTo(
            expected.sort()
        )
    }

    @Test
    fun `getRecommendations uses correct implementation and returns expected results`() {
        val input = "mock"
        val count = 99
        val fr = mockk<Flux<Ranking>>()
        val sequential = mockk<Flux<Ranking>>()
        val vector = mockk<List<Double>>()
        val entry = mockk<Ranking>()
        val flux = mockk<ParallelFlux<Ranking>>()

        mockkStatic(GetTopRecommendationsFlux::class)
        every { vectorMap[input] } answers { vector }
        every { service.computeRecommendationsParallelFlux(vector) } answers { flux }
        every { entry.title } returnsMany listOf("mock1", "mock")
        every { vectorMap.containsKey(any()) } answers { true }
        every { flux.filter(any()) } answers {
            assertThat(firstArg<Predicate<Ranking>>().test(entry)).isTrue
            assertThat(firstArg<Predicate<Ranking>>().test(entry)).isFalse
            flux
        }
        every { flux.sequential() } answers { sequential }
        every { getTopRecommendationsHeap(sequential, count) } answers { fr }

        assertThat(service.getRecommendations(input, count)).isSameAs(fr)

        verify(exactly = 1) {
            service.getRecommendations(input, count)
            vectorMap[input]
            service.computeRecommendationsParallelFlux(vector)
            flux.filter(any())
            flux.sequential()
            vectorMap.containsKey(any())
            getTopRecommendationsHeap(sequential, count)
        }
        verify(exactly = 2) { entry.title }

        confirmVerified(flux, vectorMap, sequential, service, fr, vector, entry)
    }

    @Test
    fun `getRecommendation(list input) uses correct implementation and returns expected results`() {
        val input = spyk(mutableListOf("m1", "?", "m3"))
        val o = mockk<Flux<Ranking>>()
        val count = 99
        val fe = mockk<Flux<Entry>>()
        val fs = mockk<Flux<String>>()
        val pfe = mockk<ParallelFlux<Entry>>()
        val pfr = mockk<ParallelFlux<Ranking>>()
        val fr = mockk<Flux<Ranking>>()
        val r = mockk<Ranking>()
        val map = (1..10).map { "m$it" to listOf(1.0, 2.0, 3.0) }.toMap()
            .toMutableMap().also {
                it.injectInto(service)
            }
        mockkStatic(GetTopRecommendationsFlux::class)
        mockkStatic(Flux::class)

        every { Flux.fromIterable<Entry>(any()) } answers {
            assertThat(firstArg<Iterable<Entry>>()).isSameAs(map.entries)
            fe
        }
        every { fe.parallel() }.answers { pfe }
        every { pfe.runOn(any()) }.answers { pfe }

        every { pfe.filter(any()) } answers {
            val r = map.filter { firstArg<Predicate<Entry>>().test(it) }
            assertThat(r.size).isEqualTo(map.size - 2)
            val x = mapOf("?" to listOf(0.0)).entries.first()
            assertThat(firstArg<Predicate<Entry>>().test(x)).isTrue
            pfe
        }
        every { pfe.map<Ranking>(any()) } answers { pfr }

        every { vectorMap.containsKey(any()) } returnsMany listOf(
            true,
            false,
            true
        )
        every { pfr.sequential() } answers { fr }
        every { getTopRecommendationsSort(fr, count) } answers { o }

        assertThat(service.getRecommendations(input, count)).isSameAs(o)

        verify(exactly = 1) {
            Schedulers.parallel()
            Flux.fromIterable<Entry>(any())
            fe.parallel()
            pfe.runOn(any())
            service.getRecommendations(any<List<String>>(), any())
            pfe.filter(any())
            pfe.map<Ranking>(any())
            pfr.sequential()
            getTopRecommendationsSort(fr, count)
        }
        confirmVerified(r, fe, fs, pfe, pfr, service)
    }

    @Test
    fun `computeRecommendationFlux uses correct implementation and returns expected results`() {
        mockkStatic(Flux::class)
        mockkStatic(CosineSimilarityUtils::class)
        val entry = mockk<Entry>()
        val vector = mockk<List<Double>>()
        val flux = mockk<Flux<Entry>>()
        val parallelFlux = mockk<ParallelFlux<Entry>>()
        val scheduler = mockk<Scheduler>()
        val similarity = mockk<ParallelFlux<Ranking>>()
        val sim = 9.2

        mockkStatic(Schedulers::class)
        every { Schedulers.parallel() } returns sch
        every {
            CosineSimilarityUtils.cosineSimilarity(
                any(), any(), any()
            )
        } answers { sim }
        every { Flux.fromIterable<Entry>(any()) }.answers { flux }
        every { flux.parallel() }.answers { parallelFlux }
        every { parallelFlux.runOn(any()) }.answers { parallelFlux }
        every { entry.value } answers { vector }
        every { entry.key } answers { "mock" }
        every { parallelFlux.map<Ranking>(any()) } answers {
            firstArg<Function<Entry, Ranking>>().apply(entry)
            similarity
        }

        assertThat(service.computeRecommendationsParallelFlux(vector)).isSameAs(
            similarity
        )

        verify {
            Schedulers.parallel()
            service.computeRecommendationsParallelFlux(any())
            CosineSimilarityUtils.cosineSimilarity(any(), any(), any())
            Flux.fromIterable<Entry>(any())
            flux.parallel()
            entry.value
            entry.key
            parallelFlux.runOn(any())
            parallelFlux.map<Ranking>(any())
        }

        confirmVerified(flux, parallelFlux, scheduler, similarity, service)
    }

}

typealias Entry = Map.Entry<String, List<Double>>
package edu.vandy.recommender.common

import edu.vandy.recommender.common.GetTopRecommendationsFlux.getTopRecommendationsHeap
import edu.vandy.recommender.common.GetTopRecommendationsFlux.getTopRecommendationsSort
import edu.vandy.recommender.common.model.Ranking
import edu.vandy.recommender.utils.GetTopK
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import test.admin.AssignmentTests
import java.util.function.Function

class GetTopRecommendationsFluxTest: AssignmentTests() {

    @Test
    fun `getTopRecommendationsHeap uses optimal implementation and lambdas`() {
        val count = 99
        val fri = mockk<Flux<Ranking>>()
        val fro = mockk<Flux<Ranking>>()
        val ranking = mockk<Ranking>()
        val func = mockk<Function<Flux<Function<Flux<Ranking>, Flux<Ranking>>>,
                Flux<Function<Flux<Ranking>, Flux<Ranking>>>>>()
        mockkStatic(GetTopK::class)
        mockkStatic(GetTopRecommendationsFlux::class)

        every { fri.transform(any<Function<in Flux<Ranking>, out Publisher<Ranking>>>()) } answers {
            firstArg<Function<Flux<Ranking>, Publisher<Ranking>>>().apply(fri)
            fro
        }
        every { getTopRecommendationsHeap(any(), any()) } answers {
            callOriginal()
        }
        every { GetTopK.getTopK<Function<Flux<Ranking>, Flux<Ranking>>>(count) } answers { func }

        assertThat(getTopRecommendationsHeap(fri, count)).isSameAs(fro)

        verify(exactly = 1) {
            getTopRecommendationsHeap(any(), any())
            GetTopK.getTopK<Function<Flux<Ranking>, Flux<Ranking>>>(count)
            fri.transform(any<Function<in Flux<Ranking>, out Publisher<Ranking>>>())
        }

        confirmVerified(ranking, fri, fro)
    }

    @Test
    fun `getTopRecommendationsSort uses optimal implementation and lambdas`() {
        val fri = mockk<Flux<Ranking>>()
        val fr = mockk<Flux<Ranking>>()
        val count = 99
        val fro = mockk<Flux<Ranking>>()

        mockkStatic(GetTopRecommendationsFlux::class)
        every { getTopRecommendationsSort(any(), any()) } answers {
            callOriginal()
        }
        every { fri.sort(any()) } answers {
            assertThat(
                firstArg<Comparator<Ranking>>()
                    .compare(
                        Ranking("", 1.0),
                        Ranking("", 2.0)
                    )
            ).isEqualTo(1)
            fr
        }

        every { fr.distinct() } answers { fr }
        every { fr.take(count.toLong()) } answers { fro }

        assertThat(getTopRecommendationsSort(fri, count)).isSameAs(fro)

        verify(exactly = 1) {
            getTopRecommendationsSort(any(), any())
            fri.sort(any())
            fr.distinct()
            fr.take(count.toLong())
        }

        confirmVerified(fri, fro, fr)
    }
}
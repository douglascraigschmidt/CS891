package edu.vandy.recommender.client.proxies;

import edu.vandy.recommender.common.Constants
import edu.vandy.recommender.common.model.Ranking
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import reactor.core.publisher.Flux
import test.admin.AssignmentTests
import test.admin.injectInto

class RecommenderAsyncProxyTest: AssignmentTests() {
    @SpyK
    var proxy: RecommenderAsyncProxy = RecommenderAsyncProxy()

    @MockK
    lateinit var api: ParallelFluxAPI

    @MockK
    lateinit var m1: Flux<Ranking>

    @MockK
    lateinit var m2: Flux<Ranking>

    @BeforeEach
    fun setup() {
        api.injectInto(proxy)
    }

    @ParameterizedTest
    @ValueSource(strings = [Constants.Service.PARALLEL_FLUX, ""])
    fun getMovies(strategy: String) {
        every { api.movies } returns m1
        every { api.moviesTimed } returns m2
        if (!strategy.isBlank()) {
            assertThat(proxy.getMovies(strategy, true)).isSameAs(m2)
            assertThat(proxy.getMovies(strategy, false)).isSameAs(m1)
        } else {
            assertThrows<IllegalArgumentException> {
                proxy.getMovies(strategy, true)
            }
            assertThrows<IllegalArgumentException> {
                proxy.getMovies(strategy, false)
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [Constants.Service.PARALLEL_FLUX, ""])
    fun search(strategy: String) {
        every { api.searchMovies(null) } returns m1
        every { api.searchMoviesTimed(null)} returns m2
        if (!strategy.isBlank()) {
            assertThat(proxy.search(strategy, null, true)).isSameAs(m2)
            assertThat(proxy.search(strategy, null, false)).isSameAs(m1)
        } else {
            assertThrows<IllegalArgumentException> {
                proxy.search(strategy, null, true)
            }
            assertThrows<IllegalArgumentException> {
                proxy.search(strategy, null, false)
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [Constants.Service.PARALLEL_FLUX, ""])
    fun getRecommendations(strategy: String) {
        every { api.recommendations("", 0) } returns m1
        every { api.recommendationsTimed("", any()) } returns m2
        if (!strategy.isBlank()) {
            assertThat(proxy.getRecommendations(strategy, "", 0, true)).isSameAs(m2)
            assertThat(proxy.getRecommendations(strategy, "", 0, false)).isSameAs(m1)
        } else {
            assertThrows<IllegalArgumentException> {
                proxy.getRecommendations(strategy, "", 0, true)
            }
            assertThrows<IllegalArgumentException> {
                proxy.getRecommendations(strategy, "", 0, false)
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [Constants.Service.PARALLEL_FLUX, ""])
    fun getRecommendationsList(strategy: String) {
        val l = mockk<List<String>>()
        every { api.recommendations(any<List<String>>(), 0) } returns m1
        every { api.recommendationsTimed(any<List<String>>(), any()) } returns m2
        if (!strategy.isBlank()) {
            assertThat(proxy.getRecommendations(strategy, l, 0, true)).isSameAs(m2)
            assertThat(proxy.getRecommendations(strategy, l, 0, false)).isSameAs(m1)
        } else {
            assertThrows<IllegalArgumentException> {
                proxy.getRecommendations(strategy, l, 0, true)
            }
            assertThrows<IllegalArgumentException> {
                proxy.getRecommendations(strategy, l, 0, false)
            }
        }
    }
}
package edu.vandy.recommender.databaseex

import edu.vandy.recommender.common.model.Movie
import edu.vandy.recommender.databaseex.client.DatabaseAsyncProxy
import edu.vandy.recommender.utils.WebUtils
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.mockkStatic
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import test.admin.AssignmentTests
import test.admin.injectInto

class DatabaseAsyncProxyTest : AssignmentTests() {
    @SpyK
    var dap = DatabaseAsyncProxy()

    @MockK
    lateinit var wc: WebClient

    @MockK
    lateinit var fm: Flux<Movie>

    @MockK
    lateinit var ls: List<String>

    @BeforeEach
    fun before() {
        wc.injectInto(dap)
        mockkStatic(WebUtils::class)
    }

    @Test
    fun movies() {
        every { WebUtils.buildUriString(any()) } answers {
            assertThat(firstArg<String>().toByteArray().toList()).isEqualTo(x)
            "a"
        }
        every {
            WebUtils.makeGetRequestFlux<Movie, Any>(any(), any(), any())
        } answers {
            fm
        }
        assertThat(dap.movies).isSameAs(fm)
        verify {
            WebUtils.buildUriString(any())
            WebUtils.makeGetRequestFlux<Movie, Any>(any(), any(), any())
            dap.movies
        }
        confirmVerified(dap, wc, fm, ls)
    }

    @Test
    fun searchMovies() {
        every { WebUtils.buildUriString(any()) } answers {
            assertThat(firstArg<String>().toByteArray().toList()).isEqualTo(sm)
            "a"
        }
        every {
            val makeGetRequestFlux =
                WebUtils.makeGetRequestFlux<Movie, Any>(any(), any(), any())
            makeGetRequestFlux
        } answers {
            fm
        }
        assertThat(dap.searchMovies("x")).isSameAs(fm)
        verify {
            WebUtils.buildUriString(any())
            WebUtils.makeGetRequestFlux<Movie, Any>(any(), any(), any())
            dap.searchMovies(any<String>())
        }
        confirmVerified(dap, wc, fm, ls)
    }

    @Test
    fun searchListMovies() {
        every { WebUtils.buildUriString(any()) } answers {
            assertThat(firstArg<String>().toByteArray().toList()).isEqualTo(z)
            "a"
        }
        every {
            WebUtils.makePostRequestFlux<Movie, Any>(any(), any(), any(), any())
        } answers {
            fm
        }
        assertThat(dap.searchMovies(ls)).isSameAs(fm)
        verify {
            WebUtils.buildUriString(any())
            WebUtils.makePostRequestFlux<Movie, Any>(any(), any(), any(), any())
            dap.searchMovies(any<List<String>>())
        }
        confirmVerified(dap, wc, fm, ls)
    }

    private val x = listOf<Byte>(97, 108, 108, 77, 111, 118, 105, 101, 115)
    private val z = listOf<Byte>(115, 101, 97, 114, 99, 104, 101, 115)
    private val sm = listOf<Byte>(115, 101, 97, 114, 99, 104, 47, 120)
}
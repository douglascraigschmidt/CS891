package edu.vandy.recommender.movies

import edu.vandy.recommender.admin.AssignmentTests
import edu.vandy.recommender.movies.client.MoviesSyncProxy
import edu.vandy.recommender.movies.client.WebUtils
import edu.vandy.recommender.movies.common.Constants.EndPoint.*
import edu.vandy.recommender.movies.common.Constants.EndPoint.Params.QUERIES_PARAM
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponents
import org.springframework.web.util.UriComponentsBuilder
import server.injectInto

class MoviesSyncProxyTest: AssignmentTests() {
    @Test
    fun getMovies() {
        val ucb = mockk<UriComponentsBuilder>()
        val uc = mockk<UriComponents>()
        val ml = mockk<MutableList<Any>>()
        val msp = spyk<MoviesSyncProxy>()
        val rt = mockk<RestTemplate>()
        rt.injectInto(msp)
        mockkStatic(UriComponentsBuilder::class)
        mockkStatic(WebUtils::class)

        every { UriComponentsBuilder.fromPath(any()) } answers {
            assertThat(firstArg<String>()).isEqualTo("${GET_ALL_MOVIES}/b")
            ucb
        }
        every { UriComponentsBuilder.fromPath(any()) } returns ucb
        every { ucb.build() } returns uc
        every { uc.toUriString() } returns "a"
        every { WebUtils.makeGetRequestList<Any>(rt, "a", any()) } answers {
            assertThat(thirdArg<Class<*>>().simpleName).isEqualTo("Movie[]")
            ml
        }

        assertThat(msp.movies).isSameAs(ml)

        verify {
            UriComponentsBuilder.fromPath(any())
            ucb.build()
            uc.toUriString()
            WebUtils.makeGetRequestList<Any>(any(), any(), any())
            msp.movies
        }

        confirmVerified(ucb, uc, ml, msp)
    }

    @Test
    fun searchMovies() {
        val rq = "x"
        val ucb = mockk<UriComponentsBuilder>()
        val uc = mockk<UriComponents>()
        val ml = mockk<MutableList<Any>>()
        val msp = spyk<MoviesSyncProxy>()
        val rt = mockk<RestTemplate>()
        rt.injectInto(msp)
        mockkStatic(UriComponentsBuilder::class)
        mockkStatic(WebUtils::class)
        every { UriComponentsBuilder.fromPath(any()) } answers {
            assertThat(firstArg<String>()).isEqualTo("$GET_SEARCH/b")
            ucb
        }
        every { ucb.build() } returns uc
        every { uc.toUriString() } answers {
            "a"
        }
        every { WebUtils.encodeQuery(any()) } answers {
            assertThat(firstArg<String>()).isSameAs(rq)
            "b"
        }
        every { WebUtils.makeGetRequestList<Any>(rt, "a", any()) } answers {
            assertThat(thirdArg<Class<*>>().simpleName).isEqualTo("Movie[]")
            ml
        }

        assertThat(msp.searchMovies(rq)).isSameAs(ml)

        verify {
            UriComponentsBuilder.fromPath(any())
            ucb.build()
            uc.toUriString()
            WebUtils.encodeQuery(any())
            WebUtils.makeGetRequestList<Any>(rt, "a", any())
            msp.searchMovies(rq)
        }

        confirmVerified(ucb, uc, ml, msp, rt)
    }

    @Test
    fun searchMoviesList() {
        val rq = mockk<List<String>>()
        val ucb = mockk<UriComponentsBuilder>()
        val uc = mockk<UriComponents>()
        val ml = mockk<MutableList<Any>>()
        val msp = spyk<MoviesSyncProxy>()
        val rt = mockk<RestTemplate>()
        val ls = mockk<List<String>>()
        rt.injectInto(msp)
        mockkStatic(UriComponentsBuilder::class)
        mockkStatic(WebUtils::class)
        every { UriComponentsBuilder.fromPath(any()) } answers {
            assertThat(firstArg<String>()).isEqualTo(GET_SEARCHES)
            ucb
        }
        every { ucb.queryParam(any(), "b") } answers {
            assertThat(firstArg<String>()).isEqualTo(QUERIES_PARAM)
            ucb
        }
        every { WebUtils.list2String(any()) } answers {
            assertThat(firstArg<List<String>>()).isSameAs(ls)
            "b"
        }
        every { ucb.build() } returns uc
        every { uc.toUriString() } answers {
            "a"
        }
        every { WebUtils.encodeQueries(rq) } answers {
            assertThat(firstArg<List<String>>()).isSameAs(rq)
            ls
        }
        every { WebUtils.makeGetRequestList<Any>(rt, "a", any()) } answers {
            assertThat(thirdArg<Class<*>>().simpleName).isEqualTo("Movie[]")
            ml
        }

        assertThat(msp.searchMovies(rq)).isSameAs(ml)

        verify {
            UriComponentsBuilder.fromPath(any())
            ucb.queryParam(any(), "b")
            WebUtils.list2String(any())
            ucb.build()
            uc.toUriString()
            WebUtils.encodeQueries(rq)
            WebUtils.makeGetRequestList<Any>(rt, "a", any())
            msp.searchMovies(rq)
        }

        confirmVerified(rq, ucb, uc, ml, msp, rt, ls)
    }
}

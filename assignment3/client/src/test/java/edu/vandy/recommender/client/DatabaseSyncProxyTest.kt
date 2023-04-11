package edu.vandy.recommender.client

import edu.vandy.recommender.client.proxies.DatabaseAPI
import edu.vandy.recommender.client.proxies.DatabaseSyncProxy
import edu.vandy.recommender.common.model.Movie
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Call
import retrofit2.Response
import test.admin.AssignmentTests
import test.admin.injectInto

class DatabaseSyncProxyTest : AssignmentTests() {
    @SpyK
    var dap = DatabaseSyncProxy()

    @MockK
    lateinit var clm: Call<List<Movie>>

    @MockK
    lateinit var api: DatabaseAPI

    @MockK
    lateinit var rlm: Response<List<Movie>>

    @MockK
    private lateinit var lm: List<Movie>

    @MockK
    private lateinit var ls: List<String>

    @BeforeEach
    fun before() {
        api.injectInto(dap)
    }

    @Test
    fun movies() {
        every { api.getMovies(any()) } answers { clm }
        every { clm.execute() } answers { rlm }
        every { rlm.isSuccessful } answers { true }
        every { rlm.body() } answers { lm }
        assertThat(dap.getMovies("")).isSameAs(lm)

        verify {
            api.getMovies(any())
            dap.getMovies(any())
            clm.execute()
            rlm.isSuccessful
            rlm.body()
        }
        confirmVerified(dap, lm, clm, rlm)
    }

    @Test
    fun searchMovies() {
        every { api.searchMovies(any(), any<String>()) } answers { clm }
        every { clm.execute() } answers { rlm }
        every { rlm.isSuccessful } answers { true }
        every { rlm.body() } answers { lm }
        assertThat(dap.searchMovies("", "")).isSameAs(lm)

        verify {
            api.searchMovies(any(), any<String>())
            dap.searchMovies(any(), any<String>())
            clm.execute()
            rlm.isSuccessful
            rlm.body()
        }
        confirmVerified(dap, lm, clm, rlm)
    }

    @Test
    fun searchMoviesList() {
        every { api.searchMovies(any(), any<List<String>>()) } answers { clm }
        every { clm.execute() } answers { rlm }
        every { rlm.isSuccessful } answers { true }
        every { rlm.body() } answers { lm }
        assertThat(dap.searchMovies("", ls)).isSameAs(lm)

        verify {
            api.searchMovies(any(), any<List<String>>())
            dap.searchMovies(any(), any<List<String>>())
            clm.execute()
            rlm.isSuccessful
            rlm.body()
        }
        confirmVerified(dap, lm, clm, rlm)
    }


    @Test
    fun moviesTimed() {
        every { api.getMoviesTimed(any()) } answers { clm }
        every { clm.execute() } answers { rlm }
        every { rlm.isSuccessful } answers { true }
        every { rlm.body() } answers { lm }
        assertThat(dap.getMoviesTimed("")).isSameAs(lm)

        verify {
            api.getMoviesTimed(any())
            dap.getMoviesTimed(any())
            clm.execute()
            rlm.isSuccessful
            rlm.body()
        }
        confirmVerified(dap, lm, clm, rlm)
    }

    @Test
    fun searchMoviesTimed() {
        every { api.searchMoviesTimed(any(), any<String>()) } answers { clm }
        every { clm.execute() } answers { rlm }
        every { rlm.isSuccessful } answers { true }
        every { rlm.body() } answers { lm }
        assertThat(dap.searchMoviesTimed("", "")).isSameAs(lm)

        verify {
            api.searchMoviesTimed(any(), any<String>())
            dap.searchMoviesTimed(any(), any<String>())
            clm.execute()
            rlm.isSuccessful
            rlm.body()
        }
        confirmVerified(dap, lm, clm, rlm)
    }

    @Test
    fun searchMoviesListTimed() {
        every { api.searchMoviesTimed(any(), any<List<String>>()) } answers { clm }
        every { clm.execute() } answers { rlm }
        every { rlm.isSuccessful } answers { true }
        every { rlm.body() } answers { lm }
        assertThat(dap.searchMoviesTimed("", ls)).isSameAs(lm)

        verify {
            api.searchMoviesTimed(any(), any<List<String>>())
            dap.searchMoviesTimed(any(), any<List<String>>())
            clm.execute()
            rlm.isSuccessful
            rlm.body()
        }
        confirmVerified(dap, lm, clm, rlm)
    }
}
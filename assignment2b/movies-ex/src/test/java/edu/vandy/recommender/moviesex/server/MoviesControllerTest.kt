package edu.vandy.recommender.moviesex.server

import edu.vandy.recommender.common.model.Movie
import edu.vandy.recommender.moviesex.server.utils.WebUtils
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import test.admin.hasFieldAnnotation
import test.admin.hasMethodAnnotation
import test.admin.hasParameterAnnotation
import test.admin.injectInto

internal class MoviesControllerTest {
    @Test
    fun `Members have correct annotations`() {
        assertThat(
            MoviesController::class.java.hasFieldAnnotation(
                Autowired::class.java,
                "service",
                true
            )
        ).isTrue
    }

    @Test
    fun `getMovie method annotations are correct`() {
        assertThat(
            MoviesController::class.java.hasMethodAnnotation(
                "getMovies",
                annotationClass = GetMapping::class.java,
                validate = { it.value.size == 1 && it.value[0] == "allMovies" },
                onlyAnnotation = true,
            )
        ).isTrue
    }

    @Test
    fun `search method annotations are correct`() {
        assertThat(
            MoviesController::class.java.hasMethodAnnotation(
                "search",
                arrayOf(String::class.java),
                annotationClass = GetMapping::class.java,
                validate = { it.value.size == 1 && it.value[0] == "search/{regexQuery}" },
                onlyAnnotation = true,
            )
        ).isTrue
    }

    @Test
    fun `search method parameter annotations are correct`() {
        assertThat(
            MoviesController::class.java.hasParameterAnnotation(
                "search",
                arrayOf(String::class.java),
                0,
                PathVariable::class.java,
                onlyAnnotation = true
            )
        ).isTrue
    }

    @Test
    fun `search list method annotations are correct`() {
        assertThat(
            MoviesController::class.java.hasMethodAnnotation(
                "search",
                arrayOf(java.util.List::class.java),
                GetMapping::class.java,
                { it.value.size == 1 && it.value[0] == "searches" },
                onlyAnnotation = true
            )
        ).isTrue
    }

    @Test
    fun `search list method parameter annotations are correct`() {
        assertThat(
            MoviesController::class.java.hasParameterAnnotation(
                "search",
                arrayOf(java.util.List::class.java),
                0,
                RequestParam::class.java,
                onlyAnnotation = true
            )
        ).isTrue
    }

    @Test
    fun `getMovies is implemented correctly`() {
        val c = spyk(MoviesController())
        val r = mockk<List<Movie>>()
        val s = mockk<MoviesService>()
        s.injectInto(c)
        every { s.movies } returns r
        assertThat(c.movies).isSameAs(r)
        verify {
            s.movies
            c.movies
        }
        confirmVerified(c, r, s)
    }

    @Test
    fun `search is implemented correctly`() {
        val c = spyk(MoviesController())
        val r = mockk<List<Movie>>()
        val s = spyk<MoviesService>()
        mockkStatic(WebUtils::class)
        s.injectInto(c)
        every { s.search(any<String>()) } returns r
        every { WebUtils.decodeQuery(any()) } returns ""
        assertThat(c.search("")).isSameAs(r)
        verify {
            s.search(any<String>())
            c.search(any<String>())
            WebUtils.decodeQuery(any())
        }
        confirmVerified(c, r, s)
    }

    @Test
    fun `search list is implemented correctly`() {
        val c = spyk(MoviesController())
        val r = mockk<List<Movie>>()
        val s = mockk<MoviesService>()
        val l = mockk<List<String>>()
        mockkStatic(WebUtils::class)
        s.injectInto(c)
        every { s.search(any<List<String>>()) } returns r
        every { WebUtils.decodeQueries(any()) } returns l
        assertThat(c.search(listOf())).isSameAs(r)
        verify {
            WebUtils.decodeQueries(any())
            s.search(any<List<String>>())
            c.search(any<List<String>>())
        }
        confirmVerified(c, r, s, l)
    }
}
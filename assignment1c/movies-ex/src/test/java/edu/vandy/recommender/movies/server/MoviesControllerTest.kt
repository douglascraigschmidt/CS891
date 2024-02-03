package edu.vandy.recommender.movies.server

import edu.vandy.recommender.movies.common.model.Movie
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import server.hasFieldAnnotation
import server.hasMethodAnnotation
import server.hasParameterAnnotation
import server.injectInto

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
        val s = mockk<MoviesService>()
        s.injectInto(c)
        every { s.search(any<String>()) } returns r
        assertThat(c.search("")).isSameAs(r)
        verify {
            s.search(any<String>())
            c.search(any<String>())
        }
        confirmVerified(c, r, s)
    }

    @Test
    fun `search list is implemented correctly`() {
        val c = spyk(MoviesController())
        val r = mockk<List<Movie>>()
        val s = mockk<MoviesService>()
        s.injectInto(c)
        every { s.search(any<List<String>>()) } returns r
        assertThat(c.search(listOf())).isSameAs(r)
        verify {
            s.search(any<List<String>>())
            c.search(any<List<String>>())
        }
        confirmVerified(c, r, s)
    }
}
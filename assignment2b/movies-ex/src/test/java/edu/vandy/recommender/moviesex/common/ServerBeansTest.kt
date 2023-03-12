package edu.vandy.recommender.moviesex.common

import edu.vandy.recommender.common.model.Movie
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Bean
import test.admin.AssignmentTests
import test.admin.hasMethodAnnotation
import java.util.stream.Stream

internal class ServerBeansTest : AssignmentTests() {
    @Test
    fun `movieMap has correct annotation`() {
        assertThat(
            ServerBeans::class.java.hasMethodAnnotation(
                "movieMap",
                annotationClass = Bean::class.java,
                onlyAnnotation = true,
            )
        ).isTrue
    }

    @Test
    fun `movieList has correct annotation`() {
        assertThat(
            ServerBeans::class.java.hasMethodAnnotation(
                "movieList",
                annotationClass = Bean::class.java,
                onlyAnnotation = true,
            )
        ).isTrue
    }

    @Test
    fun `movieList has correct implementation`() {
        val sb = spyk<ServerBeans>()
        val d = "d"
        val lm = mockk<List<Movie>>()
        val sm = mockk<Stream<Movie>>()
        val aa = mockk<MutableMap<String, MutableList<Double>>>()
        val e =
            mockk<MutableSet<MutableMap.MutableEntry<String, MutableList<Double>>>>()
        val xx =
            mockk<Stream<MutableMap.MutableEntry<String, MutableList<Double>>>>()
        mockkStatic(MovieDatasetReader::class)

        every { MovieDatasetReader.loadMovieData(any()) } answers { aa }
        every { aa.entries } answers { e }
        every { e.stream() } answers { xx }
        every { xx.map<Movie>(any()) } answers { sm }
        every { sm.toList() } answers { lm }
        every { sm.sorted(any()) } answers {
            assertThat(
                firstArg<Comparator<Movie>>()
                    .compare(Movie("1", null), Movie("2", null))
            ).isEqualTo(-1)
            assertThat(
                firstArg<Comparator<Movie>>()
                    .compare(Movie("2", null), Movie("1", null))
            ).isEqualTo(1)
            assertThat(
                firstArg<Comparator<Movie>>()
                    .compare(Movie("2", null), Movie("2", null))
            ).isEqualTo(0)
            sm
        }
        assertThat(sb.movieList(d)).isSameAs(lm)
        verify {
            MovieDatasetReader.loadMovieData(any())
            aa.entries
            e.stream()
            xx.map<Movie>(any())
            sm.sorted(any())
            sm.toList()
            sb.movieList(any())
            aa.size
        }

        confirmVerified(aa, e, xx, sm, lm, sb)
    }
}
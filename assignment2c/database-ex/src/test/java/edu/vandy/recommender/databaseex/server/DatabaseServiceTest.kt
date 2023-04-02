package edu.vandy.recommender.databaseex.server

import edu.vandy.recommender.common.model.Movie
import edu.vandy.recommender.databaseex.repository.DatabaseRepository
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.mockkStatic
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.ParallelFlux
import test.admin.AssignmentTests
import test.admin.injectInto
import java.util.function.Function

class DatabaseServiceTest : AssignmentTests() {
    @SpyK
    var s = DatabaseExService()

    @MockK
    lateinit var r: DatabaseRepository

    @MockK
    lateinit var lm: List<Movie>

    @MockK
    lateinit var fm: Flux<Movie>

    @MockK
    lateinit var ls: List<String>

    @MockK
    lateinit var pfm: ParallelFlux<Movie>

    @BeforeEach
    fun before() {
        r.injectInto(s)
    }

    /**
     * @return A [List] of all [Movie] database entries
     * sorted in ascending order by the [Movie] title (id)
     */
    @Test
    fun `getMovies is implemented correctly`() {
        every { r.findAllByOrderByIdAsc() } returns fm
        assertThat(s.movies).isSameAs(fm)
        verify {
            s.movies
            r.findAllByOrderByIdAsc()
        }
        confirmVerified(s, r, lm)
    }

    @Test
    fun `search is implemented correctly`() {
        every { r.findByIdContainingIgnoreCaseOrderByIdAsc(any()) } returns fm
        assertThat(s.search("")).isSameAs(fm)
        verify {
            s.search("")
            r.findByIdContainingIgnoreCaseOrderByIdAsc(any())
        }
        confirmVerified(s, r, lm)
    }

    @Test
    fun `search(list) is implemented correctly`() {
        every { r.findAllByIdContainingAnyInOrderByAsc(any()) } returns fm
        assertThat(s.search(ls)).isSameAs(fm)
        verify {
            r.findAllByIdContainingAnyInOrderByAsc(any())
            s.search(any<List<String>>())
        }
    }
    fun `search(list) is implemented correctly Old`() {
        mockkStatic(Flux::class)
        every { Flux.fromIterable<Movie>(any()) } returns fm
        every { fm.parallel() } returns pfm
        every { pfm.runOn(any()) } returns pfm
        every { pfm.flatMap<Movie>(any()) } answers {
            firstArg<Function<String, Publisher<Movie>>>().apply("")
            pfm
        }
        every { s.search(any<String>()) } returns fm
        every { pfm.sequential() } answers { fm }
        every { fm.sort() } answers { fm }
        every { fm.distinct<String>(any()) } answers { fm }
        assertThat(s.search(ls)).isSameAs(fm)
        verify {
            s.search(ls)
            s.search(any<String>())
            pfm.sequential()
            fm.sort()
            fm.parallel()
            fm.distinct<String>(any())
            pfm.runOn(any())
            pfm.flatMap<Movie>(any())
            Flux.fromIterable<Movie>(any())
        }
        confirmVerified(s, r, lm, fm, pfm)
    }
}
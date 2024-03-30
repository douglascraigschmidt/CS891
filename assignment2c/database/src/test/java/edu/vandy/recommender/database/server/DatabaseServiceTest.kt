package edu.vandy.recommender.database.server

import edu.vandy.recommender.common.model.Movie
import edu.vandy.recommender.database.repository.DatabaseRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import test.admin.AssignmentTests
import test.admin.injectInto
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Stream

class DatabaseServiceTest : AssignmentTests() {
    @SpyK
    var s = DatabaseService()

    @MockK
    lateinit var r: DatabaseRepository

    @MockK
    lateinit var tsm: TreeSet<Movie>

    @MockK
    lateinit var sm: Stream<Movie>

    @MockK
    lateinit var lm: List<Movie>

    @MockK
    lateinit var ls: List<String>

    @MockK
    lateinit var ss: Stream<String>

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
        every { r.findAllByOrderByIdAsc() } returns lm
        assertThat(s.movies).isSameAs(lm)
        verify {
            s.movies
            r.findAllByOrderByIdAsc()
        }
        doConfirmVerified()
    }

    @Test
    fun `search is implemented correctly`() {
        every { r.findByIdContainingIgnoreCaseOrderByIdAsc(any()) } returns lm
        assertThat(s.search("")).isSameAs(lm)
        verify {
            s.search("")
            r.findByIdContainingIgnoreCaseOrderByIdAsc(any())
        }
        doConfirmVerified()
    }

    @Test
    fun `search(list) is implemented correctly`() {
        every { ls.parallelStream() } answers { ss }
        every { ss.mapMulti<Movie>(any()) } answers {
            firstArg<BiConsumer<String, Consumer<Stream<Movie>>>>().accept("", mockk())
            sm
        }
        every { lm.forEach(any<Consumer<Movie>>()) } just Runs
        every { sm.distinct() } answers { sm }
        every { sm.sorted() } answers { sm }
        every { s.search(any<String>()) } answers { lm }
        every { sm.toList() } answers { lm }

        assertThat(s.search(ls)).isSameAs(lm)

        verify(exactly = 1) {
            s.search(ls)
            s.search(any<String>())
            ls.parallelStream()
            lm.forEach(any<Consumer<Movie>>())
            ss.mapMulti<Movie>(any())
            sm.distinct()
            sm.sorted()
            sm.toList()
        }
        doConfirmVerified()
    }

    private fun doConfirmVerified() {
        confirmVerified(s, r, tsm, sm, lm, ls, ss)
    }
}

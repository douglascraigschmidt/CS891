package edu.vandy.recommender.movies.server

import edu.vandy.recommender.admin.AssignmentTests
import edu.vandy.recommender.movies.common.model.Movie
import edu.vandy.recommender.movies.utils.FutureUtils
import io.mockk.*
import jdk.incubator.concurrent.StructuredTaskScope
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import server.hasFieldAnnotation
import server.injectInto
import java.net.URLDecoder
import java.nio.charset.Charset
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.function.Function
import java.util.function.Predicate
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.stream.Stream

class MoviesServiceTest : AssignmentTests() {
    @Test
    fun hasCorrectAnnotations() {
        assertThat(
            MoviesService::class.java.hasFieldAnnotation(
                Autowired::class.java,
                "mMovies",
                true
            )
        ).isTrue
    }

    @Test
    fun movies() {
        val ms = spyk<MoviesService>()
        val lm = mockk<List<Movie>>()
        lm.injectInto(ms)
        assertThat(ms.mMovies).isSameAs(lm)
    }

    @Test
    fun search() {
        val ms = spyk<MoviesService>()
        val lm = mockk<List<Movie>>()
        every { ms.search(any<List<String>>()) } answers { lm }
        assertThat(ms.search("")).isSameAs(lm)
        verify {
            ms.search(any<List<String>>())
            ms.search("")
        }
        confirmVerified(ms)
    }

    @Test
    fun searchMany() {
        val ms = spyk(MoviesService())
        val ls = mockk<List<String>>()
        val lm = mockk<List<Movie>>()
        val lflm = mockk<List<Future<List<Movie>>>>()
        val lp = mockk<List<Pattern>>()
        mockkConstructor(StructuredTaskScope.ShutdownOnFailure::class)
        every { ms.makePatternList(any<List<String>>()) } answers { lp }
        every { ms.concatMatches(any()) } answers { lm }
        every { ms.getMatches(any<List<Pattern>>(), any()) } answers { lflm }
        every { anyConstructed<StructuredTaskScope.ShutdownOnFailure>().throwIfFailed() } answers { }
        every { anyConstructed<StructuredTaskScope.ShutdownOnFailure>().close() } answers { }
        every { anyConstructed<StructuredTaskScope.ShutdownOnFailure>().join() } answers { mockk() }

        assertThat(ms.search(ls)).isSameAs(lm)

        verify {
            ms.makePatternList(any<List<String>>())
            ms.concatMatches(any())
            ms.getMatches(any<List<Pattern>>(), any())
            anyConstructed<StructuredTaskScope.ShutdownOnFailure>().throwIfFailed()
            anyConstructed<StructuredTaskScope.ShutdownOnFailure>().close()
            anyConstructed<StructuredTaskScope.ShutdownOnFailure>().join()
            ms.search(ls)
        }

        confirmVerified(ms, ls, lm, lflm)
    }

    @Test
    fun makePatternList() {
        val ms = spyk(MoviesService())
        val ls = mockk<List<String>>()
        val s1 = mockk<Stream<String>>()
        val s2 = mockk<Stream<String>>()
        val lp = mockk<MutableList<Pattern>>()
        val sp = mockk<Stream<Pattern>>()
        val p = mockk<Pattern>()
        mockkStatic(URLDecoder::class)
        every { ls.stream() } answers { s1 }
        every { s1.map<String>(any()) } answers {
            assertThat(firstArg<Function<String, String>>().apply("")).isEqualTo("decoded")
            s2
        }
        every { URLDecoder.decode("", any<Charset>()) } answers { "decoded" }
        every { s2.map<Pattern>(any<Function<String, Pattern>>()) } answers {
            assertThat(firstArg<Function<String, Pattern>>().apply("")).isEqualTo(p)
            sp
        }
        every { sp.toList() } answers { lp }
        mockkStatic(Pattern::class)
        every { Pattern.compile(any(), any()) } answers {
            assertThat(secondArg<Int>()).isEqualTo(2)
            p
        }

        assertThat(ms.makePatternList(ls)).isSameAs(lp)
        verify {
            ls.stream()
            s1.map<URLDecoder>(any())
            s2.map<Pattern>(any())
            sp.toList()
            URLDecoder.decode(any(), any<Charset>())
            Pattern.compile(any(), any())
            ms.makePatternList(ls)
        }
        confirmVerified(ms, s1, s2, sp, lp)
    }

    @Test
    fun getMatches() {
        val ms = spyk(MoviesService())
        val lp = mockk<List<Pattern>>()
        val sp = mockk<Stream<Pattern>>()
        val sflm = mockk<Stream<Future<List<Movie>>>>()
        val sof = mockk<StructuredTaskScope.ShutdownOnFailure>()
        val lflm = mockk<List<Future<List<Movie>>>>()
        val flm = mockk<Future<List<Movie>>>()
        val p = mockk<Pattern>()
        every { lp.stream() } answers { sp }
        every { sp.map<Future<List<Movie>>>(any()) } answers {
            firstArg<Function<Pattern, Future<List<Movie>>>>().apply(p)
            sflm
        }
        every { sflm.toList() } answers { lflm }
        every { ms.findMatches(any(), any()) } answers { flm }
        assertThat(ms.getMatches(lp, sof)).isSameAs(lflm)
        verify {
            lp.stream()
            sp.map<Future<List<Movie>>>(any())
            sflm.toList()
            ms.findMatches(any(), any())
            ms.getMatches(lp, sof)
        }
        confirmVerified(ms, lp, sp, sflm, sof, lflm, flm, p)
    }

    @Test
    fun getListMatches() {
        val ms = spyk(MoviesService())
        val lp = mockk<List<Pattern>>()
        val sp = mockk<Stream<Pattern>>()
        val sflm = mockk<Stream<Future<List<Movie>>>>()
        val sof = mockk<StructuredTaskScope.ShutdownOnFailure>()
        val lflm = mockk<List<Future<List<Movie>>>>()
        val flm = mockk<Future<List<Movie>>>()
        val p = mockk<Pattern>()
        every { lp.stream() } answers { sp }
        every { sp.map<Future<List<Movie>>>(any()) } answers {
            firstArg<Function<Pattern, Future<List<Movie>>>>().apply(p)
            sflm
        }
        every { sflm.toList() } answers { lflm }
        every { ms.findMatches(any(), any()) } answers { flm }
        assertThat(ms.getMatches(lp, sof)).isSameAs(lflm)
        verify {
            lp.stream()
            sp.map<Future<List<Movie>>>(any())
            sflm.toList()
            ms.findMatches(any(), any())
            ms.getMatches(lp, sof)
        }
        confirmVerified(ms, lp, sp, sflm, sof, lflm, flm, p)
    }

    @Test
    fun findMatchesAsync() {
        val ms = spyk(MoviesService())
        val p = mockk<Pattern>()
        val flm = mockk<Future<List<Movie>>>()
        val sof = mockk<StructuredTaskScope.ShutdownOnFailure>()
        val lm = mockk<List<Movie>>()
        val sm = mockk<Stream<Movie>>()
        val m = mockk<Movie>()
        lm.injectInto(ms)
        every { sof.fork<List<Movie>>(any()) } answers {
            firstArg<Callable<List<Movie>>>().call()
            flm
        }
        every { lm.stream() } answers { sm }
        every { sm.filter(any()) } answers {
            firstArg<Predicate<Movie>>().test(m)
            sm
        }
        every { ms.match(any(), any()) } answers { true }
        every { sm.toList() } answers { lm }
        assertThat(ms.findMatches(p, sof)).isSameAs(flm)
        verify {
            sof.fork<List<Movie>>(any())
            lm.stream()
            sm.filter(any())
            ms.match(any(), any())
            sm.toList()
            ms.findMatches(p, sof)
        }
        confirmVerified(ms, p, flm, sof, lm, sm, m)
    }

    @Test
    fun match() {
        val ms = spyk(MoviesService())
        val p = mockk<Pattern>()
        val m = mockk<Movie>()
        val ma = mockk<Matcher>()
        every { p.matcher(any()) } answers { ma }
        every { ma.find() } answers { true }
        every { m.id() } answers { "x" }
        assertThat(ms.match(p, m)).isTrue
        verify {
            p.matcher(any())
            ma.find()
            m.id()
            ms.match(p, m)
        }
        confirmVerified(ms, p, m, ma)
    }

    @Test
    fun convertMatches() {
        val ms = spyk(MoviesService())
        val lm = mockk<List<Movie>>()
        val lflm = mockk<List<Future<MutableList<Movie>>>>()
        val slm = mockk<Stream<List<Movie>>>()
        val sm = mockk<Stream<Movie>>()
        mockkStatic(FutureUtils::class)
        every { FutureUtils.futures2Stream<List<Movie>>(any()) } answers { slm }
        every { slm.flatMap<Movie>(any()) } answers {
            firstArg<Function<List<Movie>, Stream<Movie>>>().apply(lm)
            sm
        }
        every { lm.stream() } answers { sm }
        every { sm.distinct() } answers { sm }
        every { sm.toList() } answers { lm }
        assertThat(ms.concatMatches(lflm)).isSameAs(lm)
        verify {
            FutureUtils.futures2Stream<List<Movie>>(any())
            slm.flatMap<Movie>(any())
            sm.distinct()
            lm.stream()
            sm.toList()
            ms.concatMatches(lflm)
        }
        confirmVerified(ms, lm, lflm, slm, sm)
    }
}

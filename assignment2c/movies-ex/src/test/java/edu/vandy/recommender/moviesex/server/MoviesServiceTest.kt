package edu.vandy.recommender.moviesex.server

import edu.vandy.recommender.common.model.Movie
import edu.vandy.recommender.utils.FutureUtils
import io.mockk.*
import jdk.incubator.concurrent.StructuredTaskScope
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import test.admin.AssignmentTests
import test.admin.hasFieldAnnotation
import test.admin.injectInto
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.function.Consumer
import java.util.function.Function
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
    fun findMoviesMatchingQuery() {
        val ms = spyk(MoviesService())
        val s = ""
        val lfm = mockk<List<Future<Movie>>>()
        val lm = mockk<List<Movie>>()
        val p = mockk<Pattern>()

        mockkConstructor(StructuredTaskScope.ShutdownOnFailure::class)

        every { ms.makePattern(any()) } answers { p }
        every {
            ms.getMatchesForPatterns(
                any<List<Pattern>>(),
                any()
            )
        } answers { lfm }
        every { anyConstructed<StructuredTaskScope.ShutdownOnFailure>().throwIfFailed() } answers { }
        every { anyConstructed<StructuredTaskScope.ShutdownOnFailure>().join() } answers { mockk() }
        every { anyConstructed<StructuredTaskScope.ShutdownOnFailure>().close() } answers { }
        every {
            ms.getMatchesForPattern(
                any<Pattern>(),
                any()
            )
        } answers { mockk() }
        every { ms.convertMovieMatches(any()) } answers { lm }

        assertThat(ms.search(s)).isSameAs(lm)

        verify {
            ms.makePattern(any())
            ms.getMatchesForPattern(any(), any())
            anyConstructed<StructuredTaskScope.ShutdownOnFailure>().throwIfFailed()
            anyConstructed<StructuredTaskScope.ShutdownOnFailure>().join()
            anyConstructed<StructuredTaskScope.ShutdownOnFailure>().close()
            ms.search(any<String>())
            ms.convertMovieMatches(any())
        }

        confirmVerified(ms, lm, p, lfm)
    }

    @Test
    fun findMoviesMatchingQueries() {
        val ms = spyk(MoviesService())
        val ls = mockk<List<String>>()
        val lm = mockk<List<Movie>>()
        val lfm = mockk<List<Future<Movie>>>()
        val lp = mockk<List<Pattern>>()
        mockkConstructor(StructuredTaskScope.ShutdownOnFailure::class)
        every { ms.makePatterns(any<List<String>>()) } answers { lp }
        every { ms.convertMovieMatches(any()) } answers { lm }
        every {
            ms.getMatchesForPatterns(
                any<List<Pattern>>(),
                any()
            )
        } answers { lfm }
        every { anyConstructed<StructuredTaskScope.ShutdownOnFailure>().throwIfFailed() } answers { }
        every { anyConstructed<StructuredTaskScope.ShutdownOnFailure>().join() } answers { mockk() }
        every { anyConstructed<StructuredTaskScope.ShutdownOnFailure>().close() } answers { }

        assertThat(ms.search(ls)).isSameAs(lm)

        verify {
            ms.makePatterns(any<List<String>>())
            ms.convertMovieMatches(any())
            ms.getMatchesForPatterns(any<List<Pattern>>(), any())
            anyConstructed<StructuredTaskScope.ShutdownOnFailure>().throwIfFailed()
            anyConstructed<StructuredTaskScope.ShutdownOnFailure>().join()
            anyConstructed<StructuredTaskScope.ShutdownOnFailure>().close()
            ms.search(ls)
        }

        confirmVerified(ms, ls, lm, lfm)
    }

    @Test
    fun makePattern() {
        val ms = spyk(MoviesService())
        val p = ms.makePattern("abc")
        assertThat(p).isNotNull()
        assertThat(p.pattern()).isEqualTo("abc")
        assertThat(p.flags()).isEqualTo(2)
    }

    @Test
    fun makePatterns() {
        val ms = spyk(MoviesService())
        val ls = mockk<List<String>>()
        val s = mockk<Stream<String>>()
        val sp = mockk<Stream<Pattern>>()
        val lp = mockk<MutableList<Pattern>>()
        val p = mockk<Pattern>()

        every { ls.stream() } answers { s }
        every { s.map<Pattern>(any()) } answers {
            firstArg<Function<String, Pattern>>().apply("")
            sp
        }
        every { ms.makePattern(any()) } answers { p }
        every { sp.toList() } answers { lp }
        assertThat(ms.makePatterns(ls)).isSameAs(lp)
        verify {
            ls.stream()
            s.map<Pattern>(any())
            sp.toList()
            ms.makePattern(any())
            ms.makePatterns(ls)
        }
        confirmVerified(ms, ls, sp, lp)
    }

    @Test
    fun getMatchesForPattern() {
        val lm = mockk<List<Movie>>()
        val ms = spyk(MoviesService()).also { lm.injectInto(it) }
        val m = mockk<Movie>()
        val sm = mockk<Stream<Movie>>()
        val lp = mockk<List<Pattern>>()
        val sfm = mockk<Stream<Future<Movie>>>()
        val sof = mockk<StructuredTaskScope.ShutdownOnFailure>()
        val fm = mockk<Future<Movie>>()
        val lfm = mockk<List<Future<Movie>>>()

        every { lm.stream() } answers { sm }
        every { sm.map<Future<Movie>>(any()) } answers {
            firstArg<Function<Movie, Future<Movie>>>().apply(m)
            sfm
        }
        every { sof.fork<Movie>(any()) } answers {
            assertThat(firstArg<Callable<Movie>>().call()).isEqualTo(m)
            fm
        }
        every { ms.findMatchesForPatternsTask(any(), any()) } answers { m }
        every { sfm.toList() } answers { lfm }

        assertThat(ms.getMatchesForPatterns(lp, sof)).isSameAs(lfm)

        verify {
            lm.stream()
            sm.map<Future<Movie>>(any())
            sof.fork<Movie>(any())
            ms.findMatchesForPatternsTask(any(), any())
            sfm.toList()
            ms.getMatchesForPatterns(lp, sof)
        }
        confirmVerified(lm, ms, sm, lp, sfm, sof, fm, lfm)
    }

    @Test
    fun getMatchesFromPatterns() {
        val lm = mockk<List<Movie>>()
        val ms = spyk(MoviesService()).also { lm.injectInto(it) }
        val sm = mockk<Stream<Movie>>()
        val lp = mockk<List<Pattern>>()
        val m = mockk<Movie>()
        val fm = mockk<Future<Movie>>()
        val sof = mockk<StructuredTaskScope.ShutdownOnFailure>()
        val sfm = mockk<Stream<Future<Movie>>>()
        val lfm = mockk<List<Future<Movie>>>()

        every { lm.stream() } answers { sm }
        every { sm.map<Future<Movie>>(any()) } answers {
            firstArg<Function<Movie, Future<Movie>>>().apply(m)
            sfm
        }
        every { sof.fork<Movie>(any()) } answers {
            assertThat(firstArg<Callable<Movie>>().call()).isEqualTo(m)
            fm
        }
        every { ms.findMatchesForPatternsTask(any(), any()) } answers { m }
        every { sfm.toList() } answers { lfm }

        assertThat(ms.getMatchesForPatterns(lp, sof)).isSameAs(lfm)

        verify {
            lm.stream()
            sm.map<Future<Movie>>(any())
            sof.fork<Movie>(any())
            ms.findMatchesForPatternsTask(any(), any())
            sfm.toList()
            ms.getMatchesForPatterns(any<List<Pattern>>(), any())
        }

        confirmVerified(lm, ms, sm, lp, sfm, sof, fm, lfm)
    }

    @Test
    fun findMatchAsync() {
        val ms = spyk<MoviesService>()
        val fm = mockk<Future<Movie>>()
        val p = mockk<Pattern>()
        val m = mockk<Movie>()
        val s = mockk<StructuredTaskScope.ShutdownOnFailure>()
        every { ms.match(any(), any()) } returnsMany listOf(true, false)
        every { s.fork<Movie>(any()) } answers {
            assertThat(firstArg<Callable<Movie>>().call()).isEqualTo(m)
            assertThat(firstArg<Callable<Movie>>().call()).isEqualTo(null)
            firstArg<Callable<Movie>>().call()
            fm
        }
        assertThat(ms.findMatchAsync(p, m, s)).isSameAs(fm)
        verify {
            ms.match(any(), any())
            s.fork<Movie>(any())
            ms.findMatchAsync(any(), any(), any())
        }
        confirmVerified(ms, fm, p, s)
    }

    @Test
    fun findMatchesAsync() {
        val lm = mockk<List<Movie>>()
        val lp = mockk<List<Pattern>>()
        val p = mockk<Pattern>()
        val s = mockk<StructuredTaskScope.ShutdownOnSuccess<Movie>>()
        val m = mockk<Movie>()
        val fm = mockk<Future<Movie>>()
        val ms = spyk(MoviesService()).also { lm.injectInto(it) }
        every { lp.forEach(any<Consumer<Pattern>>()) } answers {
            firstArg<Consumer<Pattern>>().accept(p)
        }
        every { s.fork<Movie>(any()) } answers {
            assertThat(firstArg<Callable<Movie>>().call()).isSameAs(m); fm
        }
        every { ms.match(any(), any()) } answers { true }
        ms.findMatchesAsync(lp, m, s)
        verify {
            lp.forEach(any<Consumer<Pattern>>())
            s.fork<Movie>(any())
            ms.match(any(), any())
            ms.findMatchesAsync(any(), any(), any())
        }
        confirmVerified(lm, lp, s, m, fm, ms)
    }

    @Test
    fun findMatchesForPatternsTask() {
        val ms = spyk(MoviesService())
        val lp = mockk<List<Pattern>>()
        val m = mockk<Movie>()
        val sm = mockk<StructuredTaskScope.ShutdownOnSuccess<Movie>>()
        mockkConstructor(StructuredTaskScope.ShutdownOnSuccess::class)
        every { anyConstructed<StructuredTaskScope.ShutdownOnSuccess<Movie>>().join() } answers { sm }
        every { anyConstructed<StructuredTaskScope.ShutdownOnSuccess<Movie>>().result() } answers { m }
        every { anyConstructed<StructuredTaskScope.ShutdownOnSuccess<Movie>>().close() } answers { }
        every { ms.findMatchesAsync(any(), any(), any()) } just Runs
        ms.findMatchesForPatternsTask(lp, m)
        verify {
            ms.findMatchesAsync(any(), any(), any())
            anyConstructed<StructuredTaskScope.ShutdownOnSuccess<Movie>>().join()
            anyConstructed<StructuredTaskScope.ShutdownOnSuccess<Movie>>().result()
            anyConstructed<StructuredTaskScope.ShutdownOnSuccess<Movie>>().close()
            ms.findMatchesForPatternsTask(any(), any())
        }
        confirmVerified(ms, lp, m, sm)
    }

    @Test
    fun match() {
        val ms = spyk(MoviesService())
        val p = mockk<Pattern>()
        val m = mockk<Movie>()
        val ma = mockk<Matcher>()
        every { p.matcher(any()) } answers { ma }
        every { ma.find() } answers { true }
        assertThat(ms.match(p, m)).isTrue
        verify {
            p.matcher(any())
            ma.find()
            ms.match(p, m)
        }
        confirmVerified(ms, p, m, ma)
    }

    @Test
    fun convertMovieMatches() {
        val ms = spyk(MoviesService())
        val lfm = mockk<List<Future<Movie>>>()
        val sm = mockk<Stream<Movie>>()
        val l = mockk<MutableList<Movie>>()

        mockkStatic(FutureUtils::class)
        every { FutureUtils.futures2Stream(any<List<Future<Movie>>>()) } answers { sm }
        every { sm.toList() } answers { l }
        assertThat(ms.convertMovieMatches(lfm)).isSameAs(l)
        verify {
            FutureUtils.futures2Stream<Movie>(any())
            sm.toList()
            ms.convertMovieMatches(lfm)
        }
        confirmVerified(ms, lfm, sm, l)
    }
}

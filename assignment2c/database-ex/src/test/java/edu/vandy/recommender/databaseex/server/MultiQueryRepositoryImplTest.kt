package edu.vandy.recommender.databaseex.server

import edu.vandy.recommender.common.model.Movie
import edu.vandy.recommender.databaseex.repository.MultiQueryRepositoryImpl
import edu.vandy.recommender.utils.ArrayUtils
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.mockkStatic
import io.r2dbc.spi.Readable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.RowsFetchSpec
import reactor.core.publisher.Flux
import test.admin.AssignmentTests
import test.admin.injectInto
import java.util.function.Function

class MultiQueryRepositoryImplTest : AssignmentTests() {
    @SpyK
    var mr = MultiQueryRepositoryImpl()

    @MockK
    lateinit var row: Readable

    @MockK
    lateinit var ld: List<Double>

    @MockK
    lateinit var rfsm: RowsFetchSpec<Movie>

    @MockK
    lateinit var a: Any

    @MockK
    lateinit var dc: DatabaseClient

    @MockK
    lateinit var es: DatabaseClient.GenericExecuteSpec

    @MockK
    lateinit var ls: List<String>

    @MockK
    lateinit var fm: Flux<Movie>

    init {
        mockkStatic(MultiQueryRepositoryImpl::class)
    }
    @Test
    fun findAllByIdContainingAnyInOrderByAsc() {
        every { MultiQueryRepositoryImpl.buildQueryString(any(), any(), ls) } answers {
            assertThat(
                firstArg<String>().lowercase().toByteArray().toList()
            ).isEqualTo(x1)
            assertThat(
                secondArg<String>().lowercase().toByteArray().toList()
            ).isEqualTo(x2)
            ""
        }
        every { mr.getMovieFlux(any()) } returns fm
        assertThat(mr.findAllByIdContainingAnyInOrderByAsc(ls)).isSameAs(fm)
    }

    @Test
    fun findAllByIdContainingAllInOrderByAsc() {
        every { MultiQueryRepositoryImpl.buildQueryString(any(), any(), ls) } answers {
            assertThat(
                firstArg<String>().lowercase().toByteArray().toList()
            ).isEqualTo(x3)
            assertThat(
                secondArg<String>().lowercase().toByteArray().toList()
            ).isEqualTo(x4)
            ""
        }
        every { mr.getMovieFlux(any()) } returns fm
        assertThat(mr.findAllByIdContainingAllInOrderByAsc(ls)).isSameAs(fm)
    }

    @Test
    fun getMovieFlux() {
        dc.injectInto(mr)
        mockkStatic(ArrayUtils::class)
        every { dc.sql(any<String>()) } answers { es }
        every { es.map(any<Function<Readable, Movie>>()) } answers {
            val ex = firstArg<Function<Readable, Movie>>().apply(row)
            assertThat(ex.id).isEqualTo("a")
            assertThat(ex.vector).isSameAs(ld)
            rfsm
        }
        every { rfsm.all() } answers { fm }
        every { row.get(any<String>(), any<Class<String>>()) } answers { "a" }
        every { row.get("vector") } answers { a }
        every {
            ArrayUtils.obj2List(
                any<Object>(),
                any<Class<Double>>()
            )
        } answers { ld }
        assertThat(mr.getMovieFlux("a")).isSameAs(fm)
    }

    val x1 = listOf<Byte>(
        115,
        101,
        108,
        101,
        99,
        116,
        32,
        42,
        32,
        102,
        114,
        111,
        109,
        32,
        109,
        111,
        118,
        105,
        101,
        32,
        119,
        104,
        101,
        114,
        101,
        32,
        108,
        111,
        119,
        101,
        114,
        40,
        105,
        100,
        41,
        32,
        108,
        105,
        107,
        101,
        32,
        58,
        112,
        97,
        114,
        97,
        109,
        115
    )
    val x2 = listOf<Byte>(
        37,
        39,
        32,
        111,
        114,
        32,
        108,
        111,
        119,
        101,
        114,
        40,
        105,
        100,
        41,
        32,
        108,
        105,
        107,
        101,
        32,
        39,
        37
    )
    val x3 = listOf<Byte>(
        115,
        101,
        108,
        101,
        99,
        116,
        32,
        42,
        32,
        102,
        114,
        111,
        109,
        32,
        109,
        111,
        118,
        105,
        101,
        32,
        119,
        104,
        101,
        114,
        101,
        32,
        108,
        111,
        119,
        101,
        114,
        40,
        105,
        100,
        41,
        32,
        108,
        105,
        107,
        101,
        32,
        58,
        112,
        97,
        114,
        97,
        109,
        115
    )
    val x4 = listOf<Byte>(
        37,
        39,
        32,
        97,
        110,
        100,
        32,
        108,
        111,
        119,
        101,
        114,
        40,
        105,
        100,
        41,
        32,
        108,
        105,
        107,
        101,
        32,
        39,
        37
    )
}
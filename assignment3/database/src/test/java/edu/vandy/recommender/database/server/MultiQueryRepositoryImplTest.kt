package edu.vandy.recommender.database.server

import edu.vandy.recommender.common.model.Movie
import edu.vandy.recommender.database.repository.MultiQueryRepositoryImpl
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import jakarta.persistence.EntityManager
import jakarta.persistence.TypedQuery
import jakarta.persistence.criteria.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import test.admin.AssignmentTests
import test.admin.injectInto
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.stream.Stream

class MultiQueryRepositoryImplTest : AssignmentTests() {
    @SpyK
    var mr = MultiQueryRepositoryImpl()

    @MockK
    lateinit var em: EntityManager

    @MockK
    lateinit var cb: CriteriaBuilder

    @MockK
    lateinit var cq: CriteriaQuery<Movie>

    @MockK
    lateinit var rm: Root<Movie>

    @MockK
    lateinit var e: Expression<String>

    @MockK
    lateinit var ls: List<String>

    @MockK
    lateinit var p: Path<Object>

    @MockK
    lateinit var s1: Stream<String>

    @MockK
    lateinit var s2: Stream<String>

    @MockK
    lateinit var pr: Predicate

    @MockK
    lateinit var sp: Stream<Predicate>

    @MockK
    lateinit var tq: TypedQuery<Movie>

    @MockK
    lateinit var cm: CriteriaQuery<Movie>

    @MockK
    lateinit var o: Order

    @MockK
    lateinit var lm: List<Movie>

    @Test
    fun findAllByIdContainingInOrderByAsc() {
        mockkStatic(MultiQueryRepositoryImpl::class)
        em.injectInto(mr)
        every { em.criteriaBuilder } answers { cb }
        every { cb.createQuery<Movie>(any()) } answers { cq }
        every { cq.from(any<Class<Movie>>()) } answers { rm }
        every { rm.get<Object>(any<String>()) } answers { p }
        every { cb.lower(any()) } answers {
            firstArg<Expression<String>>()
            e
        }
        every { mr.getQueryResults(any(), any(), any(), any()) } answers { lm }
        every {
            MultiQueryRepositoryImpl.getPredicate(any(), any(), any())
        } answers { pr }
        mr.findAllByIdContainingInOrderByAsc(ls)

        verify {
            em.criteriaBuilder
            cb.createQuery<Movie>(any())
            cq.from(Movie::class.java)
            rm.get<Object>(any<String>())
            cb.lower(any())
            mr.getQueryResults(any(), any(), any(), any())
            mr.findAllByIdContainingInOrderByAsc(any())
            MultiQueryRepositoryImpl.getPredicate(any(), any(), any())
        }
        doConfirmVerified()
    }

    @Test
    fun getPredicate() {
        verifyOneOf(
            "getPredicate not implemented correctly",
            {
                clearAllMocks()
                getPredicate1()
            },
            {
                clearAllMocks()
                getPredicate2()
            }
        )
    }

    private fun getPredicate1() {
        every { ls.stream() } answers { s1 }
        every { s1.map<String>(any()) } answers {
            assertThat(firstArg<Function<String, String>>().apply("A")).isEqualTo(
                "a"
            )
            s2
        }
        every { cb.like(any(), any<String>()) } answers {
            assertThat(secondArg<String>()).isEqualTo("%A%")
            pr
        }
        every { s2.map(any<Function<String, Predicate>>()) } answers {
            firstArg<Function<String, Predicate>>().apply("A")
            sp
        }
        every { sp.reduce(any(), any()) } answers {
            secondArg<BinaryOperator<Predicate>>().apply(pr, pr)
            pr
        }

        every { cb.conjunction() } answers { pr }
        every {
            cb.and(
                any<Expression<Boolean>>(),
                any<Expression<Boolean>>()
            )
        } answers { pr }

        assertThat(
            MultiQueryRepositoryImpl.getPredicate(
                ls,
                cb,
                e
            )
        ).isSameAs(pr)

        verify {
            ls.stream()
            s1.map(any<Function<String, String>>())
            cb.like(any(), any<String>())
            s2.map(any<Function<String, Predicate>>())
            sp.reduce(any(), any())
            cb.conjunction()
            cb.and(any<Expression<Boolean>>(), any<Expression<Boolean>>())
        }
        doConfirmVerified()
    }

    private fun getPredicate2() {
        every { ls.stream() } answers { s1 }
        every { cb.like(any(), any<String>()) } answers {
            assertThat(secondArg<String>()).isEqualTo("%a%")
            pr
        }
        every { s1.map<Predicate>(any<Function<String, Predicate>>()) } answers {
            firstArg<Function<String, Predicate>>().apply("A")
            sp
        }
        every {
            sp.reduce(
                any<Predicate>(),
                any<BinaryOperator<Predicate>>()
            )
        } answers {
            secondArg<BinaryOperator<Predicate>>().apply(pr, pr)
            pr
        }

        every { cb.conjunction() } answers { pr }
        every {
            cb.and(
                any<Expression<Boolean>>(),
                any<Expression<Boolean>>()
            )
        } answers { pr }

        assertThat(
            MultiQueryRepositoryImpl.getPredicate(
                ls,
                cb,
                e
            )
        ).isSameAs(pr)

        verify {
            ls.stream()
            cb.like(any(), any<String>())
            s1.map(any<Function<String, Predicate>>())
            sp.reduce(any(), any())
            cb.conjunction()
            cb.and(any<Expression<Boolean>>(), any<Expression<Boolean>>())
        }
        doConfirmVerified()
    }

    @Test
    fun getQueryResults() {
        em.injectInto(mr)
        every { em.createQuery<Movie>(any()) } answers { tq }
        every { cq.where(any<Expression<Boolean>>()) } answers { cm }
        every { cm.groupBy(any<Expression<*>>()) } answers { cm }
        every { cm.orderBy(any<Order>()) } answers { cm }
        every { cb.asc(any()) } answers { o }
        every { tq.resultList } answers { lm }
        assertThat(mr.getQueryResults(cq, cb, pr, e)).isSameAs(lm)

        verify {
            em.createQuery<Movie>(any())
            cq.where(any<Expression<Boolean>>())
            cm.groupBy(any<Expression<*>>())
            cm.orderBy(any<Order>())
            cb.asc(any())
            tq.resultList
            mr.getQueryResults(any(), any(), any(), any())
        }

        doConfirmVerified()
    }

    fun doConfirmVerified() {
        confirmVerified(
            mr, em, cb, cq, rm, e, ls, p, s1, s2, pr, sp, tq, cm, o, lm
        )
    }
}
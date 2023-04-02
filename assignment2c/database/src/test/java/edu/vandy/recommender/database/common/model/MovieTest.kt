package edu.vandy.recommender.database.common.model

import edu.vandy.recommender.common.model.Movie
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import test.admin.AssignmentTests

class MovieTest : AssignmentTests() {
    private val m1 = listOf("a".m, "b".m, "c".m, "c".m, "C".m)
    private val m2 = listOf("b".m, "a".m, "c".m, "C".m, "c".m)

    val String.m
        get() = Movie(this, null)

    @Test
    fun compareTo() {
        val expected = listOf(-1, 1, 0, 0, 0)
        repeat(expected.size) {
            assertThat(m1[it].compareTo(m2[it])).isEqualTo(expected[it])
        }
    }

    @Test
    fun testEquals() {
        val expected = listOf(false, false, true, false, false)
        repeat(expected.size) {
            assertThat(m1[it].equals(m2[it])).isEqualTo(expected[it])
        }
    }

    @Test
    fun testHashCode() {
        val r = listOf(128, 129, 130, 130, 98)
        m1.forEachIndexed() { i, m ->
            assertThat(m.hashCode() == r[i])
        }
    }
}

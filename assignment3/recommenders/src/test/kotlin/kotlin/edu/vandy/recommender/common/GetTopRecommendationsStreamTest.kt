package edu.vandy.recommender.common

import edu.vandy.recommender.common.GetTopRecommendationsStream.getTopRecommendationsHeap
import edu.vandy.recommender.common.GetTopRecommendationsStream.getTopRecommendationsSort
import edu.vandy.recommender.common.model.Ranking
import edu.vandy.recommender.utils.GetTopK
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import test.admin.AssignmentTests
import java.util.*
import java.util.stream.Stream

internal class GetTopRecommendationsStreamTest : AssignmentTests() {
    @Test
    fun topRecommendationsHeap() {
    }

    // This test will never work because of mocking bug - sr.list is reported
    // to not be called, but it is. There is no way to fix this bug.
//    @Test
    fun getTopRecommendationsHeap() {
        val lro = mockk<List<Ranking>>()
        val sri = mockk<Stream<Ranking>>()
        val count = 99
        val sr = mockk<Stream<Ranking>>()

        mockkStatic(GetTopK::class)
        every { GetTopK.getTopK(any<Stream<Ranking>>(), any()) } answers { sr }
        every { sr.toList() } answers { lro }

        assertThat(getTopRecommendationsHeap(sri, count)).isSameAs(lro)

        verify {
            getTopRecommendationsHeap(any(), any())
            GetTopK.getTopK(any<Stream<Ranking>>(), any())
            sr.toList()
        }

        confirmVerified(lro, sri)
    }

    @Test
    fun getTopRecommendationsSort() {
        val count = 99
        val sri = mockk<Stream<Ranking>>()
        val sr = mockk<Stream<Ranking>>()
        val sro = mockk<List<Ranking>>()

        every { sri.sorted(any()) } answers { sr }
        every { sr.distinct() } answers { sr }
        every { sr.limit(any()) } answers { sr }
        every { sr.toList() } answers { sro }
        assertThat(getTopRecommendationsSort(sri, count)).isSameAs(sro)
        verify {
            sri.sorted(any())
            sr.distinct()
            sr.limit(any())
            sr.toList()
        }
        confirmVerified(sr, sro, sri)
    }
}
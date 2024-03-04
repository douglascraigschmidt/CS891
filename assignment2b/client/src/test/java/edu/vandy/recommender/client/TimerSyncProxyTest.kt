package edu.vandy.recommender.client

import edu.vandy.recommender.utils.CallUtils
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.mockkStatic
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import retrofit2.Call
import test.admin.AssignmentTests
import test.admin.injectInto

internal class TimerSyncProxyTest : AssignmentTests() {
    @SpyK
    var tsp = TimerSyncProxy()

    @MockK
    lateinit var clm: Call<String>

    @MockK
    lateinit var api: TimerAPI

    @Test
    fun timings() {
        api.injectInto(tsp)
        mockkStatic(CallUtils::class)
        every { api.getTimings(any()) } returns clm
        every { CallUtils.executeCall<String>(any()) } answers {
            firstArg<Call<String>>()
            "x"
        }
        assertThat(tsp.timings).isEqualTo("x")

        verify {
            api.getTimings("timer")
            CallUtils.executeCall<String>(any())
            tsp.timings
        }
    }
}
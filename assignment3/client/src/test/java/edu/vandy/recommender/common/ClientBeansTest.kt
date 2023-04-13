package edu.vandy.recommender.common

import edu.vandy.recommender.client.proxies.DatabaseAPI
import edu.vandy.recommender.client.proxies.TimerAPI
import io.mockk.*
import io.mockk.impl.annotations.SpyK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import retrofit2.Retrofit
import retrofit2.create
import test.admin.AssignmentTests

class ClientBeansTest : AssignmentTests() {
    @SpyK
    var cb = ClientBeans()

    @Test
    fun getDatabaseAPI() {
        mockkConstructor(Retrofit.Builder::class)
        val a = mockk<DatabaseAPI>()
        val r = mockk<Retrofit>()
        every { r.create<DatabaseAPI>() } returns a
        every { anyConstructed<Retrofit.Builder>().build() } returns r
        assertThat(cb.databaseAPI).isSameAs(a)
        verify {
            anyConstructed<Retrofit.Builder>().addConverterFactory(any())
            anyConstructed<Retrofit.Builder>()
            anyConstructed<Retrofit.Builder>().build()
            r.create(any<Class<Any>>())
            anyConstructed<Retrofit.Builder>().baseUrl(any<String>())
            cb.databaseAPI
        }
        confirmVerified(a, r, cb)
    }

    @Test
    fun getTimingAPI() {
        mockkConstructor(Retrofit.Builder::class)
        val a = mockk<TimerAPI>()
        val r = mockk<Retrofit>()
        every { r.create<TimerAPI>() } returns a
        every { anyConstructed<Retrofit.Builder>().build() } returns r
        assertThat(cb.timerAPI).isSameAs(a)
        verify {
            anyConstructed<Retrofit.Builder>().addConverterFactory(any())
            anyConstructed<Retrofit.Builder>()
            anyConstructed<Retrofit.Builder>().build()
            r.create(any<Class<Any>>())
            anyConstructed<Retrofit.Builder>().baseUrl(any<String>())
            cb.timerAPI
        }
        confirmVerified(a, r, cb)
    }

    @Test
    fun movieMap() {
    }

    val x1 = listOf<Byte>(
        104,
        116,
        116,
        112,
        58,
        47,
        47,
        108,
        111,
        99,
        97,
        108,
        104,
        111,
        115,
        116,
        58,
        56,
        48,
        56,
        48
    )
}

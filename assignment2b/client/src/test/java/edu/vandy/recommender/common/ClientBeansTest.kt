package edu.vandy.recommender.common

import edu.vandy.recommender.client.DatabaseAPI
import edu.vandy.recommender.client.TimerAPI
import io.mockk.*
import io.mockk.impl.annotations.SpyK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import retrofit2.Retrofit
import retrofit2.create
import test.admin.AssignmentTests

class ClientBeansTest : AssignmentTests() {
    @SpyK
    var cb = ClientBeans()

    fun getRestTemplate() {
        assertThat(cb.restTemplate).isNotNull
    }

    @Test
    fun getWebClient() {
        mockkStatic(WebClient::class)
        val b = mockk<WebClient.Builder>()
        val wc = mockk<WebClient>()
        every { b.build() } returns wc
        every { WebClient.builder() } returns b
        every { b.baseUrl(any()) } returns b
        assertThat(cb.webClient).isSameAs(wc)
        verify {
            b.build()
            WebClient.builder()
            b.baseUrl(any())
            cb.webClient
        }
        confirmVerified(b, wc, cb)
    }

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
        assertThat(cb.timingAPI).isSameAs(a)
        verify {
            anyConstructed<Retrofit.Builder>().addConverterFactory(any())
            anyConstructed<Retrofit.Builder>()
            anyConstructed<Retrofit.Builder>().build()
            r.create(any<Class<Any>>())
            anyConstructed<Retrofit.Builder>().baseUrl(any<String>())
            cb.timingAPI
        }
        confirmVerified(a, r, cb)
    }

    @Test
    fun movieMap() {
    }
}
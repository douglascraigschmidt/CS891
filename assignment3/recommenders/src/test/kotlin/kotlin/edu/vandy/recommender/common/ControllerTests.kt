package edu.vandy.recommender.common

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import edu.vandy.recommender.common.Constants.EndPoint.*
import edu.vandy.recommender.common.Constants.Params.MAX_COUNT_PARAM
import edu.vandy.recommender.common.Constants.Params.WATCHED_MOVIE_PARAM
import edu.vandy.recommender.common.model.Ranking
import edu.vandy.recommender.microservice.sequentialstream.SequentialStreamApplication
import edu.vandy.recommender.microservice.sequentialstream.SequentialStreamController
import edu.vandy.recommender.microservice.sequentialstream.SequentialStreamService
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.PropertySources
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.function.Supplier

@SpringBootTest(
    classes = [
        SequentialStreamApplication::class,
        SequentialStreamService::class,
        SequentialStreamController::class
    ],
    webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@PropertySources(PropertySource("classpath:/test.yml"))
@AutoConfigureMockMvc
class ControllerTests {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    lateinit var service: SequentialStreamService

    @MockkBean
    lateinit var runTimer: RunTimer

    private val expected = listOf(
        Ranking("mock1", 1.0), Ranking("mock2", 2.0), Ranking("mock2", 3.0)
    )


    @ParameterizedTest
    @ValueSource(strings = ["/", "/timed/"])
    fun `all-movies end-point returns expected results`(prefix: String) {
        val request = MockMvcRequestBuilders.get("$prefix$GET_ALL_MOVIES")

        every { service.allMovies } answers {
            expected
        }

        every {
            runTimer.runAndRecordTime(
                any(),
                any<Supplier<*>>()
            )
        } answers {
            secondArg<Supplier<*>>().get()
        }

        val jsonResult =
            mockMvc.perform(request)
                .andExpect(status().isOk)
                .andReturn()
                .response
                .contentAsString

        val result: List<Ranking> =
            objectMapper.readValue(
                jsonResult,
                object : TypeReference<List<Ranking>>() {}
            )

        verify {
            service.allMovies
        }

        if (prefix.startsWith("/timed")) {
            verify {
                runTimer.runAndRecordTime(any(), any<Supplier<*>>())
            }
        }

        confirmVerified(runTimer, service)

        assertThat(result).isEqualTo(expected)
    }

    @ParameterizedTest
    @ValueSource(strings = ["/", "/timed/"])
    fun `search end-point returns expected results`(prefix: String) {
        val request =
            MockMvcRequestBuilders.get("$prefix$GET_SEARCH/mock-query")

        every { service.search("mock-query") } returns expected

        every {
            runTimer.runAndRecordTime(
                any(),
                any<Supplier<*>>()
            )
        } answers {
            secondArg<Supplier<*>>().get()
        }

        val jsonResult =
            mockMvc.perform(request)
                .andExpect(status().isOk)
                .andReturn()
                .response
                .contentAsString

        val result: List<Ranking> =
            objectMapper.readValue(
                jsonResult,
                object : TypeReference<List<Ranking>>() {}
            )

        verify { service.search("mock-query") }

        assertThat(result).isEqualTo(expected)

        if (prefix.startsWith("/timed")) {
            verify {
                runTimer.runAndRecordTime(any(), any<Supplier<*>>())
            }
        }

        confirmVerified(runTimer, service)
    }

    @ParameterizedTest
    @ValueSource(strings = ["/", "/timed/"])
    fun `recommend end-point returns expected results`(prefix: String) {
        val mockCount = 999
        val mockMovie = "mock"
        val request =
            MockMvcRequestBuilders
                .get("$prefix$GET_RECOMMENDATIONS")
                .queryParam(WATCHED_MOVIE_PARAM, mockMovie)
                .queryParam(MAX_COUNT_PARAM, mockCount.toString())

        every {
            service.getRecommendations(
                mockMovie,
                mockCount
            )
        } returns expected

        every {
            runTimer.runAndRecordTime(
                any(),
                any<Supplier<*>>()
            )
        } answers {
            secondArg<Supplier<*>>().get()
        }

        val jsonResult =
            mockMvc.perform(request)
                .andExpect(status().isOk)
                .andReturn()
                .response
                .contentAsString

        val result: List<Ranking> =
            objectMapper.readValue(
                jsonResult,
                object : TypeReference<List<Ranking>>() {}
            )

        verify { service.getRecommendations(mockMovie, mockCount) }
        assertThat(result).isEqualTo(expected)

        if (prefix.startsWith("/timed")) {
            verify {
                runTimer.runAndRecordTime(any(), any<Supplier<*>>())
            }
        }

        confirmVerified(runTimer, service)
    }

    @ParameterizedTest
    @ValueSource(strings = ["/", "/timed/"])
    fun `recommend-many end-point returns expected results`(prefix: String) {
        val mockCount = 999
        val input = listOf("watched-1", "watched-2", "watched-3")

        val jsonContent = objectMapper.writeValueAsString(input)

        val request =
            MockMvcRequestBuilders
                .multipart("$prefix$POST_RECOMMENDATIONS")
                .queryParam(MAX_COUNT_PARAM, mockCount.toString())
                .content(jsonContent)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)

        every { service.getRecommendations(input, mockCount) } returns expected

        every {
            runTimer.runAndRecordTime(
                any(),
                any<Supplier<*>>()
            )
        } answers {
            secondArg<Supplier<*>>().get()
        }

        val jsonResult =
            mockMvc.perform(request)
                .andExpect(status().isOk)
                .andReturn()
                .response
                .contentAsString

        val result: List<Ranking> =
            objectMapper.readValue(
                jsonResult,
                object : TypeReference<List<Ranking>>() {}
            )

        verify { service.getRecommendations(input, mockCount) }
        assertThat(result).isEqualTo(expected)

        if (prefix.startsWith("/timed")) {
            verify {
                runTimer.runAndRecordTime(any(), any<Supplier<*>>())
            }
        }

        confirmVerified(runTimer, service)
    }
}
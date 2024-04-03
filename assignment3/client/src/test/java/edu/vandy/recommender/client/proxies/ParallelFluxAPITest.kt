package edu.vandy.recommender.client.proxies;

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.PostExchange
import test.admin.hasClassAnnotation
import test.admin.hasMethodAnnotation
import test.admin.hasParameterAnnotation

class ParallelFluxAPITest {

    @Test
    fun parallelFluxApiClass() {
        assertThat(
            hasClassAnnotation(
                clazz = ParallelFluxAPI::class.java,
                annotationClass = GetExchange::class.java,
                validate = { it.value == "parallelFlux/" },
                onlyAnnotation = true
            )
        ).isTrue
    }

    @Test
    fun parallelFluxApiGetMovies() {
        assertThat(
            ParallelFluxAPI::class.java.hasMethodAnnotation(
                "getMovies",
                params = arrayOf(),
                annotationClass = GetExchange::class.java,
                validate = { it.value.toByteArray().toList() == x2 },
                onlyAnnotation = true
            )
        ).isTrue
    }

    @Test
    fun parallelFluxApiSearchMovies() {
        assertThat(
            ParallelFluxAPI::class.java.hasMethodAnnotation(
                "searchMovies",
                params = arrayOf(String::class.java),
                annotationClass = GetExchange::class.java,
                validate = { it.value.toByteArray().toList() == x3 },
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            ParallelFluxAPI::class.java.hasParameterAnnotation(
                "searchMovies",
                params = arrayOf(String::class.java),
                param = 0,
                annotationClass = PathVariable::class.java,
                validate = { it.value.isNullOrBlank() },
                onlyAnnotation = true
            )
        ).isTrue
    }

    @Test
    fun parallelFluxApiRecommendations() {
        assertThat(
            ParallelFluxAPI::class.java.hasMethodAnnotation(
                "recommendations",
                params = arrayOf(String::class.java, Int::class.java),
                annotationClass = GetExchange::class.java,
                validate = { it.value == "getRecommendations" },
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            ParallelFluxAPI::class.java.hasParameterAnnotation(
                "recommendations",
                params = arrayOf(String::class.java, Int::class.java),
                param = 0,
                annotationClass = RequestParam::class.java,
                validate = { it.value.isNullOrBlank() },
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            ParallelFluxAPI::class.java.hasParameterAnnotation(
                "recommendations",
                params = arrayOf(String::class.java, Int::class.java),
                param = 1,
                annotationClass = RequestParam::class.java,
                validate = { it.value.isNullOrBlank() },
                onlyAnnotation = true
            )
        ).isTrue
    }

    @Test
    fun parallelFluxApiRecommendationsList() {
        assertThat(
            ParallelFluxAPI::class.java.hasMethodAnnotation(
                "recommendations",
                params = arrayOf(List::class.java, Int::class.java),
                annotationClass = PostExchange::class.java,
                validate = { it.value == "postRecommendations" },
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            ParallelFluxAPI::class.java.hasParameterAnnotation(
                "recommendations",
                params = arrayOf(List::class.java, Int::class.java),
                param = 0,
                annotationClass = RequestBody::class.java,
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            ParallelFluxAPI::class.java.hasParameterAnnotation(
                "recommendations",
                params = arrayOf(List::class.java, Int::class.java),
                param = 1,
                annotationClass = RequestParam::class.java,
                validate = { it.value.isNullOrBlank() },
                onlyAnnotation = true
            )
        ).isTrue
    }

    @Test
    fun parallelFluxApiGetMoviesTimed() {
        assertThat(
            ParallelFluxAPI::class.java.hasMethodAnnotation(
                "getMoviesTimed",
                params = arrayOf(),
                annotationClass = GetExchange::class.java,
                validate = { it.value.toByteArray().toList() == x6 },
                onlyAnnotation = true
            )
        ).isTrue
    }

    @Test
    fun parallelFluxApiSearchMoviesTimed() {
        assertThat(
            ParallelFluxAPI::class.java.hasMethodAnnotation(
                "searchMoviesTimed",
                params = arrayOf(String::class.java),
                annotationClass = GetExchange::class.java,
                validate = { it.value.toByteArray().toList() == x7 },
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            ParallelFluxAPI::class.java.hasParameterAnnotation(
                "searchMoviesTimed",
                params = arrayOf(String::class.java),
                param = 0,
                annotationClass = PathVariable::class.java,
                validate = { it.value.isNullOrBlank() },
                onlyAnnotation = true
            )
        ).isTrue
    }

    @Test
    fun parallelFluxApiRecommendationsTimed() {
        assertThat(
            ParallelFluxAPI::class.java.hasMethodAnnotation(
                "recommendationsTimed",
                params = arrayOf(String::class.java, Int::class.java),
                annotationClass = GetExchange::class.java,
                validate = { it.value == "timed/getRecommendations" },
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            ParallelFluxAPI::class.java.hasParameterAnnotation(
                "recommendationsTimed",
                params = arrayOf(String::class.java, Int::class.java),
                param = 0,
                annotationClass = RequestParam::class.java,
                validate = { it.value.isNullOrBlank() },
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            ParallelFluxAPI::class.java.hasParameterAnnotation(
                "recommendationsTimed",
                params = arrayOf(String::class.java, Int::class.java),
                param = 1,
                annotationClass = RequestParam::class.java,
                validate = { it.value.isNullOrBlank() },
                onlyAnnotation = true
            )
        ).isTrue
    }

    @Test
    fun parallelFluxApiRecommendationsListTimed() {
        assertThat(
            ParallelFluxAPI::class.java.hasMethodAnnotation(
                "recommendationsTimed",
                params = arrayOf(List::class.java, Int::class.java),
                annotationClass = PostExchange::class.java,
                validate = { it.value == "timed/postRecommendations" },
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            ParallelFluxAPI::class.java.hasParameterAnnotation(
                "recommendationsTimed",
                params = arrayOf(List::class.java, Int::class.java),
                param = 0,
                annotationClass = RequestBody::class.java,
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            ParallelFluxAPI::class.java.hasParameterAnnotation(
                "recommendationsTimed",
                params = arrayOf(List::class.java, Int::class.java),
                param = 1,
                annotationClass = RequestParam::class.java,
                validate = { it.value.isNullOrBlank() },
                onlyAnnotation = true
            )
        ).isTrue
    }

    val x2 = listOf<Byte>(97, 108, 108, 77, 111, 118, 105, 101, 115)
    val x3 = listOf<Byte>(
        115,
        101,
        97,
        114,
        99,
        104,
        47,
        123,
        113,
        117,
        101,
        114,
        121,
        125
    )
    val x6 = listOf<Byte>(
        116,
        105,
        109,
        101,
        100,
        47,
        97,
        108,
        108,
        77,
        111,
        118,
        105,
        101,
        115
    )
    val x7 = listOf<Byte>(
        116,
        105,
        109,
        101,
        100,
        47,
        115,
        101,
        97,
        114,
        99,
        104,
        47,
        123,
        113,
        117,
        101,
        114,
        121,
        125
    )
}
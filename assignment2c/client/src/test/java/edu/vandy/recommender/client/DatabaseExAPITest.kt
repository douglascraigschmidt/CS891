package edu.vandy.recommender.client;

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.PostExchange
import test.admin.hasClassAnnotation
import test.admin.hasMethodAnnotation
import test.admin.hasParameterAnnotation

class DatabaseExAPITest {

    @Test
    fun databaseExApiClassTest() {
        assertThat(
            hasClassAnnotation(
                clazz = DatabaseExAPI::class.java,
                annotationClass = GetExchange::class.java,
                validate = { it.value.toByteArray().toList() == x1 },
                onlyAnnotation = true
            )
        ).isTrue
    }

    @Test
    fun getMovies() {
        assertThat(
            DatabaseExAPI::class.java.hasMethodAnnotation(
                "getMovies",
                params = arrayOf(),
                annotationClass = GetExchange::class.java,
                validate = { it.value.toByteArray().toList() == x2 },
                onlyAnnotation = true
            )
        ).isTrue
    }

    @Test
    fun searchMovies() {
        assertThat(
            DatabaseExAPI::class.java.hasMethodAnnotation(
                "searchMovies",
                params = arrayOf(String::class.java),
                annotationClass = GetExchange::class.java,
                validate = { it.value.toByteArray().toList() == x3 },
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            DatabaseExAPI::class.java.hasParameterAnnotation(
                "searchMovies",
                params = arrayOf(String::class.java),
                param = 0,
                annotationClass = PathVariable::class.java,
                validate = { it.value.toByteArray().toList() == x10 },
                onlyAnnotation = true
            )
        ).isTrue
    }

    @Test
    fun testSearchMovies() {
        assertThat(
            DatabaseExAPI::class.java.hasMethodAnnotation(
                "searchMovies",
                params = arrayOf(List::class.java),
                annotationClass = PostExchange::class.java,
                validate = { it.value.toByteArray().toList() == x4 },
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            DatabaseExAPI::class.java.hasParameterAnnotation(
                "searchMovies",
                params = arrayOf(List::class.java),
                param = 0,
                annotationClass = RequestBody::class.java,
                onlyAnnotation = true
            )
        ).isTrue
    }

    @Test
    fun searchMoviesEx() {
        assertThat(
            DatabaseExAPI::class.java.hasMethodAnnotation(
                "searchMoviesEx",
                params = arrayOf(List::class.java),
                annotationClass = PostExchange::class.java,
                validate = { it.value.toByteArray().toList() == x5 },
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            DatabaseExAPI::class.java.hasParameterAnnotation(
                "searchMoviesEx",
                params = arrayOf(List::class.java),
                param = 0,
                annotationClass = RequestBody::class.java,
                onlyAnnotation = true
            )
        ).isTrue
    }

    @Test
    fun getMoviesTimed() {
        assertThat(
            DatabaseExAPI::class.java.hasMethodAnnotation(
                "getMoviesTimed",
                params = arrayOf(),
                annotationClass = GetExchange::class.java,
                validate = { it.value.toByteArray().toList() == x6 },
                onlyAnnotation = true
            )
        ).isTrue
    }

    @Test
    fun searchMoviesTimed() {
        assertThat(
            DatabaseExAPI::class.java.hasMethodAnnotation(
                "searchMoviesTimed",
                params = arrayOf(String::class.java),
                annotationClass = GetExchange::class.java,
                validate = { it.value.toByteArray().toList() == x7 },
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            DatabaseExAPI::class.java.hasParameterAnnotation(
                "searchMoviesTimed",
                params = arrayOf(String::class.java),
                param = 0,
                annotationClass = PathVariable::class.java,
                validate = { it.value.toByteArray().toList() == x10 },
                onlyAnnotation = true
            )
        ).isTrue
    }

    @Test
    fun testSearchMoviesTimed() {
        assertThat(
            DatabaseExAPI::class.java.hasMethodAnnotation(
                "searchMoviesTimed",
                params = arrayOf(List::class.java),
                annotationClass = PostExchange::class.java,
                validate = { it.value.toByteArray().toList() == x8 },
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            DatabaseExAPI::class.java.hasParameterAnnotation(
                "searchMoviesTimed",
                params = arrayOf(List::class.java),
                param = 0,
                annotationClass = RequestBody::class.java,
                onlyAnnotation = true
            )
        ).isTrue
    }

    @Test
    fun searchMoviesExTimed() {
        assertThat(
            DatabaseExAPI::class.java.hasMethodAnnotation(
                "searchMoviesExTimed",
                params = arrayOf(),
                annotationClass = PostExchange::class.java,
                validate = { it.value.toByteArray().toList() == x9 },
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            DatabaseExAPI::class.java.hasParameterAnnotation(
                "searchMoviesExTimed",
                params = arrayOf(List::class.java),
                param = 0,
                annotationClass = RequestBody::class.java,
                onlyAnnotation = true
            )
        ).isTrue
    }

    val x1 = listOf<Byte>(100, 97, 116, 97, 98, 97, 115, 101, 101, 120, 47)
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
    val x4 = listOf<Byte>(115, 101, 97, 114, 99, 104, 101, 115)
    val x5 = listOf<Byte>(115, 101, 97, 114, 99, 104, 101, 115, 69, 120)
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
    val x8 = listOf<Byte>(
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
        101,
        115
    )
    val x9 = listOf<Byte>(
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
        101,
        115,
        69,
        120
    )
    val x10 = listOf<Byte>(113, 117, 101, 114, 121)
}
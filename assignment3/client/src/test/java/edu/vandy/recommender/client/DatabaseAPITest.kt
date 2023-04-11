package edu.vandy.recommender.client;

import edu.vandy.recommender.client.proxies.DatabaseAPI
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import test.admin.hasMethodAnnotation
import test.admin.hasParameterAnnotation

class DatabaseAPITest {

    @Test
    fun getMovies() {
        assertThat(
            DatabaseAPI::class.java.hasMethodAnnotation(
                "getMovies",
                params = arrayOf(String::class.java),
                annotationClass = GET::class.java,
                validate = { it.value.toByteArray().toList() == x1 },
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            DatabaseAPI::class.java.hasParameterAnnotation(
                "getMovies",
                params = arrayOf(String::class.java),
                param = 0,
                annotationClass = Path::class.java,
                validate = { it.value.toByteArray().toList() == x9 },
                onlyAnnotation = true
            )
        ).isTrue
    }

    @Test
    fun searchMovies() {
        assertThat(
            DatabaseAPI::class.java.hasMethodAnnotation(
                "searchMovies",
                params = arrayOf(String::class.java, String::class.java),
                annotationClass = GET::class.java,
                validate = { it.value.toByteArray().toList() == x2 },
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            DatabaseAPI::class.java.hasParameterAnnotation(
                "searchMovies",
                params = arrayOf(String::class.java, String::class.java),
                param = 0,
                annotationClass = Path::class.java,
                validate = { it.value.toByteArray().toList() == x9 },
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            DatabaseAPI::class.java.hasParameterAnnotation(
                "searchMovies",
                params = arrayOf(String::class.java, String::class.java),
                param = 1,
                annotationClass = Path::class.java,
                validate = { it.value.toByteArray().toList() == x10 },
                onlyAnnotation = true
            )
        ).isTrue
    }

    @Test
    fun testSearchMovies() {
        assertThat(
            DatabaseAPI::class.java.hasMethodAnnotation(
                "searchMovies",
                params = arrayOf(String::class.java, List::class.java),
                annotationClass = POST::class.java,
                validate = { it.value.toByteArray().toList() == x3 },
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            DatabaseAPI::class.java.hasParameterAnnotation(
                "searchMovies",
                params = arrayOf(String::class.java, List::class.java),
                param = 0,
                annotationClass = Path::class.java,
                validate = { it.value.toByteArray().toList() == x9 },
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            DatabaseAPI::class.java.hasParameterAnnotation(
                "searchMovies",
                params = arrayOf(String::class.java, List::class.java),
                param = 1,
                annotationClass = Body::class.java,
                onlyAnnotation = true
            )
        ).isTrue
    }

    @Test
    fun searchMoviesEx() {
        assertThat(
            DatabaseAPI::class.java.hasMethodAnnotation(
                "searchMoviesEx",
                params = arrayOf(String::class.java, List::class.java),
                annotationClass = POST::class.java,
                validate = { it.value.toByteArray().toList() == x4 },
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            DatabaseAPI::class.java.hasParameterAnnotation(
                "searchMoviesEx",
                params = arrayOf(String::class.java, List::class.java),
                param = 0,
                annotationClass = Path::class.java,
                validate = { it.value.toByteArray().toList() == x9 },
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            DatabaseAPI::class.java.hasParameterAnnotation(
                "searchMoviesEx",
                params = arrayOf(String::class.java, List::class.java),
                param = 1,
                annotationClass = Body::class.java,
                onlyAnnotation = true
            )
        ).isTrue
    }

    @Test
    fun getMoviesTimed() {
        assertThat(
            DatabaseAPI::class.java.hasMethodAnnotation(
                "getMoviesTimed",
                params = arrayOf(String::class.java),
                annotationClass = GET::class.java,
                validate = { it.value.toByteArray().toList() == x5 },
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            DatabaseAPI::class.java.hasParameterAnnotation(
                "getMoviesTimed",
                params = arrayOf(String::class.java),
                param = 0,
                annotationClass = Path::class.java,
                validate = { it.value.toByteArray().toList() == x9 },
                onlyAnnotation = true
            )
        ).isTrue
    }

    @Test
    fun searchMoviesTimed() {
        assertThat(
            DatabaseAPI::class.java.hasMethodAnnotation(
                "searchMoviesTimed",
                params = arrayOf(String::class.java, String::class.java),
                annotationClass = GET::class.java,
                validate = { it.value.toByteArray().toList() == x6 },
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            DatabaseAPI::class.java.hasParameterAnnotation(
                "searchMoviesTimed",
                params = arrayOf(String::class.java, String::class.java),
                param = 0,
                annotationClass = Path::class.java,
                validate = { it.value.toByteArray().toList() == x9 },
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            DatabaseAPI::class.java.hasParameterAnnotation(
                "searchMoviesTimed",
                params = arrayOf(String::class.java, String::class.java),
                param = 1,
                annotationClass = Path::class.java,
                validate = { it.value.toByteArray().toList() == x10 },
                onlyAnnotation = true
            )
        ).isTrue
    }

    @Test
    fun testSearchMoviesTimed() {
        assertThat(
            DatabaseAPI::class.java.hasMethodAnnotation(
                "searchMoviesTimed",
                params = arrayOf(String::class.java, List::class.java),
                annotationClass = POST::class.java,
                validate = { it.value.toByteArray().toList() == x7 },
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            DatabaseAPI::class.java.hasParameterAnnotation(
                "searchMoviesTimed",
                params = arrayOf(String::class.java, List::class.java),
                param = 0,
                annotationClass = Path::class.java,
                validate = { it.value.toByteArray().toList() == x9 },
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            DatabaseAPI::class.java.hasParameterAnnotation(
                "searchMoviesTimed",
                params = arrayOf(String::class.java, List::class.java),
                param = 1,
                annotationClass = Body::class.java,
                onlyAnnotation = true
            )
        ).isTrue
    }

    @Test
    fun searchMoviesExTimed() {
        assertThat(
            DatabaseAPI::class.java.hasMethodAnnotation(
                "searchMoviesExTimed",
                params = arrayOf(String::class.java, List::class.java),
                annotationClass = POST::class.java,
                validate = { it.value.toByteArray().toList() == x8 },
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            DatabaseAPI::class.java.hasParameterAnnotation(
                "searchMoviesExTimed",
                params = arrayOf(String::class.java, List::class.java),
                param = 0,
                annotationClass = Path::class.java,
                validate = { it.value.toByteArray().toList() == x9 },
                onlyAnnotation = true
            )
        ).isTrue
        assertThat(
            DatabaseAPI::class.java.hasParameterAnnotation(
                "searchMoviesExTimed",
                params = arrayOf(String::class.java, List::class.java),
                param = 1,
                annotationClass = Body::class.java,
                onlyAnnotation = true
            )
        ).isTrue
    }

    val x1 = listOf<Byte>(
        123,
        114,
        111,
        117,
        116,
        101,
        110,
        97,
        109,
        101,
        125,
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
    val x2 = listOf<Byte>(
        123,
        114,
        111,
        117,
        116,
        101,
        110,
        97,
        109,
        101,
        125,
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
    val x3 = listOf<Byte>(
        123,
        114,
        111,
        117,
        116,
        101,
        110,
        97,
        109,
        101,
        125,
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
    val x4 = listOf<Byte>(
        123,
        114,
        111,
        117,
        116,
        101,
        110,
        97,
        109,
        101,
        125,
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
    val x5 = listOf<Byte>(
        123,
        114,
        111,
        117,
        116,
        101,
        110,
        97,
        109,
        101,
        125,
        47,
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
    val x6 = listOf<Byte>(
        123,
        114,
        111,
        117,
        116,
        101,
        110,
        97,
        109,
        101,
        125,
        47,
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
    val x7 = listOf<Byte>(
        123,
        114,
        111,
        117,
        116,
        101,
        110,
        97,
        109,
        101,
        125,
        47,
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
    val x8 = listOf<Byte>(
        123,
        114,
        111,
        117,
        116,
        101,
        110,
        97,
        109,
        101,
        125,
        47,
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
    val x9 =
        listOf<Byte>(114, 111, 117, 116, 101, 110, 97, 109, 101)
    val x10 = listOf<Byte>(113, 117, 101, 114, 121)
}
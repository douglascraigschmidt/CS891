package edu.vandy.recommender.movies;

import edu.vandy.recommender.movies.client.MoviesSyncProxy;
import edu.vandy.recommender.movies.common.model.Movie;
import edu.vandy.recommender.movies.server.MoviesApplication;
import edu.vandy.recommender.movies.server.MoviesController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

/**
 * This program tests the {@link MoviesSyncProxy} and its ability to
 * communicate with the {@link MoviesController} via Spring WebMVC
 * features.
 *
 * The {@code @SpringBootTest} annotation tells Spring to look for a
 * main configuration class (a {@code @SpringBootApplication}, i.e.,
 * {@link MoviesApplication}) and use that to start a Spring
 * application context to serve as the target of the tests.
 *
 * The {@code @SpringBootConfiguration} annotation indicates that a
 * class provides a Spring Boot application {@code @Configuration}.
 */
@SpringBootConfiguration
@SpringBootTest(classes = MoviesApplication.class,
                webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class MoviesTest {
    @Autowired
    private MoviesSyncProxy mMoviesSyncProxy;

    @Autowired
    Map<String, List<Double>> mMovies;

    @Test
    public void testGetMoviesSize() {
        var movies = mMoviesSyncProxy
            .getMovies();

        assertEquals(4801,
                     movies.size());
    }

    @Test
    public void testGetMoviesContents() {
        var movies = mMoviesSyncProxy
            .getMovies();

        assertEquals(4801, mMovies.size());

        int i = 0;
        for (var movie : mMovies.entrySet()) {
            assertEquals(movie.getKey(),
                         movies.get(i).id());
            assertIterableEquals(movie.getValue(),
                                 movies.get(i++).vector());
        }
    }

    @Test
    public void testSearchMoviesSize() {
        var searchWord = "Night.*Museum";
        var matchingMovies = mMoviesSyncProxy
            .searchMovies(searchWord);

        assertEquals(3,
                     matchingMovies.size());
    }

    @Test
    public void testSearchMoviesContents() {
        var searchWord = "Night.*Museum";
        var matchingMovies = mMoviesSyncProxy
            .searchMovies("Night.*Museum");

        assertEquals(3,
                matchingMovies.size());

        var iterator =
            mMovies.entrySet().iterator();
        for (Movie matchingMovie : matchingMovies) {
            String matchedMovie = "";
            outer:
            while (iterator.hasNext()) {
                var nextMovie = iterator.next().getKey();
                var nextMovieWords = nextMovie
                    .split("\\P{L}+");
                for (int i = 0; i < nextMovieWords.length; i++) {
                    if (nextMovieWords[i].equals("Night"))
                        for (int j = i + 1; j < nextMovieWords.length; j++)
                            if (nextMovieWords[j].equals("Museum")) {
                                matchedMovie = nextMovie;
                                break outer;
                            }
                }
            }

            assertEquals(matchedMovie,
                         matchingMovie.id());
        }
    }

    @Test
    public void testSearchMoviesManySize() {
        var watchedMovies = List
            .of("Iron|Steal", "\\bStar\\b");

        var matchingMovies = mMoviesSyncProxy
            .searchMovies(watchedMovies);

        assertEquals(34,
                     matchingMovies.size());
    }

    @SuppressWarnings("ConstantValue")
    @Test
    public void testSearchMoviesManyContents() {
        var watchedMovies = List
            .of("Iron|Steal", "\\bStar\\b");

        var matchingMovies = mMoviesSyncProxy
            .searchMovies(watchedMovies);

        assertEquals(34,
                     matchingMovies.size());

        var iterator = mMovies
                .entrySet().iterator();
        for (var expected : matchingMovies) {
            String matchingMovie = "";

            outer:
            while (iterator.hasNext()) {
                for (var nextMovieWord : iterator.next().getKey()
                        .split("\\P{L}+")) {
                    if (nextMovieWord.toLowerCase().contains("iron")
                            || nextMovieWord.contains("Steal")
                            || nextMovieWord.equalsIgnoreCase("star")) {
                        matchingMovie = nextMovieWord;
                        break outer;
                    }
                }
                if (!matchingMovie.equals(""))
                    assertEquals(expected.id(),
                            matchingMovie);
            }
        }
    }
}
    

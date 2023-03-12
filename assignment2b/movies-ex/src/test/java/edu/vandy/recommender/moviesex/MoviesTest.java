package edu.vandy.recommender.moviesex;

import edu.vandy.recommender.common.model.Movie;
import edu.vandy.recommender.moviesex.client.MoviesSyncProxy;
import edu.vandy.recommender.moviesex.server.MoviesApplication;
import edu.vandy.recommender.moviesex.server.MoviesController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIterable;

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
@TestPropertySource(locations = "classpath:test-application.properties")
public class MoviesTest {
    @Autowired
    private MoviesSyncProxy mMoviesSyncProxy;

    @Autowired
    Map<String, List<Double>> mMovies;

    /**
     * Returns a list of movies that match one or more search queries.
     *
     * @param movies a list of Movie objects to search through
     * @param queries a list of Strings representing the search queries to match against
     * @return a list of Movie objects that match at least one search query
     */
    public List<Movie> findMatchingMovies(Map<String, List<Double>> movies,
                                          List<String> queries) {
        // Create a new list to store the movies that match the search criteria.
        List<Movie> matchingMovies = new ArrayList<>();

        // Loop over each movie in the Set of movies.
        for (var entry : movies.entrySet()) {
            // Loop over each search query.
            for (String query : queries) {
                // Check if the ID of the current movie matches the
                // current search query (ignoring case).
                if (entry.getKey().toLowerCase()
                        .matches(".*(" + query.toLowerCase() + ").*")) {
                    // If the movie matches the search criteria, add
                    // it to the list of matching movies.
                    matchingMovies.add(new Movie(entry.getKey(),
                                                 entry.getValue()));
                    // Break out of the inner loop since we've found a
                    // match.
                    break;
                }
            }
        }

        // Sort the movies.
        matchingMovies.sort(Comparator.comparing(movie -> movie.id));

        // Return the List of movies that match the search criteria.
        return matchingMovies;
    }

    /**
     * Ensure that the {@code movies} {@link List} returned from the
     * server matches the expected results.
     */
    private void checkResults(List<Movie> movies,
                              List<String> queries) {
        if (!queries.isEmpty()) {
            var it1 = movies.iterator();
            var matchingMovies =
                findMatchingMovies(mMovies,
                                   queries);

            assertThat(matchingMovies.size())
                    .isEqualTo(movies.size());

            if (!matchingMovies.isEmpty()) {
                var it2 = matchingMovies.iterator();

                while (it1.hasNext()) {
                    var next1 = it1.next();
                    var next2 = it2.next();

                    assertThat(next1.id)
                            .isEqualTo(next2.id);
                    assertThatIterable(next1.vector)
                            .isEqualTo(next2.vector);
                }
            }
        }
    }

    @Test
    public void testGetMoviesSize() {
        var movies = mMoviesSyncProxy
            .getMovies();

        assertThat(movies.size())
            .isEqualTo(4801);
    }

    @Test
    public void testGetMoviesContents() {
        var movies = mMoviesSyncProxy
            .getMovies();

        assertThat(movies.size())
            .isEqualTo(4801);

        checkResults(movies, Collections.emptyList());
    }

    @Test
    public void testSearchMoviesSize() {
        var searchWord = "Night.*Museum";
        var matchingMovies = mMoviesSyncProxy
            .searchMovies(searchWord);

        assertThat(matchingMovies.size()).isEqualTo(3);
    }

    @Test
    public void testSearchMoviesContents() {
        var searchWord = "Night.*Museum";
        var matchingMovies = mMoviesSyncProxy
            .searchMovies("Night.*Museum");

        assertThat(matchingMovies.size())
            .isEqualTo(3);

        checkResults(matchingMovies,
                     List.of(searchWord));
    }

    @Test
    public void testSearchMoviesManySize() {
        var watchedMovies = List
            .of("Iron|Steal", "\\bStar\\b", "Trek,", "War");

        var matchingMovies = mMoviesSyncProxy
            .searchMovies(watchedMovies);

        assertThat(matchingMovies.size()).isEqualTo(80);
    }

    @SuppressWarnings("ConstantValue")
    @Test
    public void testSearchMoviesManyContents() {
        var watchedMovies = List
            .of("Iron|Steal", "\\bStar\\b", "Trek,", "War");

        var matchingMovies = mMoviesSyncProxy
            .searchMovies(watchedMovies);

        assertThat(matchingMovies.size()).isEqualTo(80);

        checkResults(matchingMovies,
                     watchedMovies);
    }
}
    

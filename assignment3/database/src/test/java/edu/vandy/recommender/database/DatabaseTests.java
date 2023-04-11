package edu.vandy.recommender.database;

import edu.vandy.recommender.common.model.Movie;
import edu.vandy.recommender.database.client.DatabaseSyncProxy;
import edu.vandy.recommender.database.server.DatabaseApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;

import java.util.*;
import java.util.function.BiFunction;

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This program tests the {@link DatabaseSyncProxy} and its ability to
 * communicate with the {@code DatabaseController} via Spring WebMVC
 * features.
 *
 * The {@code @SpringBootTest} annotation tells Spring to look for a
 * main configuration class (a {@code @SpringBootApplication}, i.e.,
 * {@link DatabaseApplication}) and use that to start a Spring
 * application context to serve as the target of the tests.
 *
 * The {@code @SpringBootConfiguration} annotation indicates that a
 * class provides a Spring Boot application {@code @Configuration}.
 */
@SuppressWarnings("DataFlowIssue")
@SpringBootConfiguration
@ComponentScan("edu.vandy.recommender.database")
@SpringBootTest(classes = DatabaseApplication.class,
                webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:test-application.properties")
public class DatabaseTests {
    /**
     * This auto-wired field connects the {@link DatabaseTests} to
     * the {@link DatabaseSyncProxy} that performs HTTP requests
     * synchronously.
     */
    @Autowired
    private DatabaseSyncProxy mDatabaseProxy;

    /** 
     * This auto-wired field contains all the movies.
     */
    @Autowired
    Map<String, List<Double>> mMovies;

    /**
     * Print out the {@link Movie} titles.
     */
    private void printMovieTitles(List<Movie> movies,
                                  String heading) {
        System.out.println(heading);
        movies
            .forEach(movie ->
                     System.out.println(movie.id));
    }

    /**
     * Returns a {@link List} of {@link Movie} objects that match all
     * search {@code queries}.
     *
     * @param movies a {@link List} of {@link Movie} objects to search
     * @param queries a {@link List} of {@link String} representing
     *                the search queries to match against
     * @return A {@link List} of {@ink Movie} objects that match all
     *         search {@code queries}
     */
    static List<Movie> findMoviesMatchingAllQueries
        (Map<String, List<Double>> movies,
         List<String> queries) {
        // Create a new List to store the movies that match the search
        // criteria.
        List<Movie> matchingMovies = new ArrayList<>();

        // Loop over each movie in the Set of movies.
        for (var entry : movies.entrySet()) {
            boolean matchesAllQueries = true;

            // Loop over each search query.
            for (String query : queries)
                // Check if the ID of the current movie contains the
                // current search query (ignoring case).
                if (!entry
                    .getKey()
                    .toLowerCase()
                    .contains(query.toLowerCase())) {
                    // Break out at the first non-match.
                    matchesAllQueries = false;
                    break;
                }

            // Only add movies that match all the search queries.
            if (matchesAllQueries)
                matchingMovies.add(new Movie(entry));
        }

        // Sort the results by id.
        matchingMovies
            .sort(Comparator
                  .comparing(movie -> movie.id,
                             CASE_INSENSITIVE_ORDER));

        // Return the List of movies that match the search criteria.
        return matchingMovies;
    }

    /**
     * Returns a {@link List} of {@link Movie} objects that
     * match one or more search {@code queries}.
     *
     * @param movies a {@link List} of {@link Movie} objects to search
     * @param queries a {@link List} of {@link String} representing
     *                the search queries to match against
     * @return A {@link List} of {@ink Movie} objects that match at
     *         least one search query
     */
    public static List<Movie> findMoviesMatchingAnyQueries
        (Map<String, List<Double>> movies,
         List<String> queries) {
        // Create a new List to store the movies that match the search
        // criteria.
        List<Movie> matchingMovies = new ArrayList<>();

        // Loop over each movie in the Set of movies.
        for (var entry : movies.entrySet()) {
            // Loop over each search query.
            for (String query : queries) {
                // Check if the ID of the current movie contains the
                // current search query (ignoring case).
                if (entry.getKey()
                    .toLowerCase()
                    .contains(query.toLowerCase())) {
                    // If the movie matches the search criteria, add
                    // it to the list of matching movies.
                    matchingMovies.add(new Movie(entry));
                    // Break out of the inner loop since we've found a
                    // match.
                    break;
                }
            }
        }

        // Sort the results by id.
        matchingMovies
            .sort(Comparator
                  .comparing(movie -> movie.id,
                             CASE_INSENSITIVE_ORDER));

        // Return the List of movies that match the search criteria.
        return matchingMovies;
    }

    /**
     * Ensure that the {@code movies} {@link List} returned from the
     * server matches the expected results.
     */
    private boolean checkResults
        (List<Movie> movies,
         List<String> queries,
         BiFunction<Map<String, List<Double>>,
                                      List<String>,
                                      List<Movie>> filterFunction) {
        if (!queries.isEmpty()) {
            var it1 = movies.iterator();
            var matchingMovies =
                filterFunction.apply(mMovies,
                                     queries);
            assertThat(movies.size())
                .isEqualTo(matchingMovies.size());

            var it2 = matchingMovies
                .iterator();

            while (it1.hasNext()) {
                var next1 = it1.next();
                var next2 = it2.next();

                assertThat(next1.id)
                    .isEqualTo(next2.id);
            }
        }
        return true;
    }

    @Test
    public void testGetMoviesSize() {
        var movies = mDatabaseProxy
            .getMovies();

        assertThat(movies.size())
            .isEqualTo(4801);
    }

    @Test
    public void testGetMoviesContents() {
        var movies = mDatabaseProxy
            .getMovies();

        assertThat(movies.size())
            .isEqualTo(4801);

        checkResults(movies,
                     Collections.emptyList(),
                     DatabaseTests
                     ::findMoviesMatchingAnyQueries);
    }

    @Test
    public void testSearchMoviesSize() {
        var searchWord = "Museum";
        var matchingMovies = mDatabaseProxy
            .searchMovies(searchWord);

        assertThat(matchingMovies.size()).isEqualTo(3);
    }

    @Test
    public void testSearchMoviesContents() {
        var searchWord = "Museum";
        var matchingMovies = mDatabaseProxy
            .searchMovies(searchWord);

        assertThat(matchingMovies.size())
            .isEqualTo(3);

        checkResults(matchingMovies,
                     List.of(searchWord),
                     DatabaseTests
                     ::findMoviesMatchingAnyQueries);
    }

    @Test
    public void testSearchMoviesManySize() {
        var watchedMovies = List
            .of("Iron", "Steal", "Star", "Trek", "War");

        var matchingMovies = mDatabaseProxy
            .searchMovies(watchedMovies);

        assertThat(matchingMovies.size()).isEqualTo(91);
    }

    @Test
    public void testSearchMoviesManyContents() {
        var watchedMovies = List
            .of("Iron", "Steal", "Star", "Trek", "War");

        var matchingMovies = mDatabaseProxy
            .searchMovies(watchedMovies);

        checkResults(matchingMovies,
                     watchedMovies,
                     DatabaseTests
                     ::findMoviesMatchingAnyQueries);
    }

    @Test
    public void testSearchMoviesExManySize() {
        var watchedMovies = List
            .of("Star", "Trek");

        var matchingMovies = mDatabaseProxy
            .searchMoviesEx(watchedMovies);

        assertThat(matchingMovies.size()).isEqualTo(13);
    }

    @Test
    public void testSearchMoviesExManyContents() {
        var watchedMovies = List
            .of("Star", "Trek");

        var matchingMovies = mDatabaseProxy
            .searchMoviesEx(watchedMovies);

        checkResults(matchingMovies,
                     watchedMovies,
                     DatabaseTests
                     ::findMoviesMatchingAllQueries);
    }
}
    

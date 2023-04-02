package edu.vandy.recommender.client;

import edu.vandy.recommender.common.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.BiFunction;

import static edu.vandy.recommender.common.Constants.Service.DATABASE;
import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This client uses Spring WebMVC features to perform synchronous
 * remote method invocations on the {@code GatewayApplication} and the
 * {@code Database} microservice that it encapsulates.
 *
 * The {@code @Component} annotation allows Spring to automatically
 * detect custom beans, i.e., Spring will scan the application for
 * classes annotated with {@code @Component}, instantiate them, and
 * inject the specified dependencies into them without having to write
 * any explicit code.
 *
 * The {@code ComponentScan} annotation instructs Spring to scan for
 * components that are within the "edu.vandy.recommender" package.
 */
@Component
@ComponentScan("edu.vandy.recommender")
public class DatabaseClient {
    /** 
     * This auto-wired field contains all the movies.
     */
    @Autowired
    Map<String, List<Double>> mMovies;

    /**
     * This auto-wired field connects the {@link DatabaseClient} to
     * the {@link DatabaseSyncProxy} that performs HTTP requests
     * synchronously.
     */
    @Autowired
    private DatabaseSyncProxy mDatabaseProxy;

    /**
     * This auto-wired field connects the {@link DatabaseClient} to
     * the {@link TimerSyncProxy} that performs HTTP requests
     * synchronously.
     */
    @Autowired
    private TimerSyncProxy mTimerSyncProxy;

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
            assertThat(matchingMovies.size())
                .isEqualTo(movies.size());

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

    public void testGetMoviesSize() {
        var movies = mDatabaseProxy
            .getMovies(DATABASE);

        assertThat(movies.size())
            .isEqualTo(4801);
    }

    public void testGetMoviesContents() {
        var movies = mDatabaseProxy
            .getMovies(DATABASE);

        assertThat(movies.size())
            .isEqualTo(4801);

        checkResults(movies,
                     Collections.emptyList(),
                     DatabaseClient
                     ::findMoviesMatchingAnyQueries);
    }

    public void testSearchMoviesSize() {
        var searchWord = "Museum";
        var matchingMovies = mDatabaseProxy
            .searchMovies(DATABASE, searchWord);

        assertThat(matchingMovies.size()).isEqualTo(3);
    }

    public void testSearchMoviesContents() {
        var searchWord = "Museum";
        var matchingMovies = mDatabaseProxy
            .searchMovies(DATABASE, searchWord);

        assertThat(matchingMovies.size())
            .isEqualTo(3);

        checkResults(matchingMovies,
                     List.of(searchWord),
                     DatabaseClient
                     ::findMoviesMatchingAnyQueries);
    }

    public void testSearchMoviesManySize() {
        var watchedMovies = List
            .of("Iron", "Steal", "Star", "Trek", "War");

        var matchingMovies = mDatabaseProxy
            .searchMovies(DATABASE, watchedMovies);

        assertThat(matchingMovies.size()).isEqualTo(91);
    }

    public void testSearchMoviesManyContents() {
        var watchedMovies = List
            .of("Iron", "Steal", "Star", "Trek", "War");

        var matchingMovies = mDatabaseProxy
            .searchMovies(DATABASE, watchedMovies);

        checkResults(matchingMovies,
                     watchedMovies,
                     DatabaseClient
                     ::findMoviesMatchingAnyQueries);
    }

    public void testSearchMoviesExManySize() {
        var watchedMovies = List
            .of("Star", "Trek");

        var matchingMovies = mDatabaseProxy
            .searchMoviesEx(DATABASE, watchedMovies);

        assertThat(matchingMovies.size()).isEqualTo(13);
    }

    public void testSearchMoviesExManyContents() {
        var watchedMovies = List
            .of("Star", "Trek");

        var matchingMovies = mDatabaseProxy
            .searchMoviesEx(DATABASE, watchedMovies);

        checkResults(matchingMovies,
                     watchedMovies,
                     DatabaseClient
                     ::findMoviesMatchingAllQueries);
    }

    public void testGetMoviesSizeTimed() {
        var movies = mDatabaseProxy
            .getMoviesTimed(DATABASE);

        assertThat(movies.size())
            .isEqualTo(4801);
    }

    public void testSearchMoviesSizeTimed() {
        var searchWord = "Night at the Museum";
        var matchingMovies = mDatabaseProxy
            .searchMoviesTimed(DATABASE, searchWord);

        assertThat(matchingMovies.size())
            .isEqualTo(3);
    }

    public void testSearchMoviesManySizeTimed() {
        var watchedMovies = List
            .of("Iron", "Steal", "Star", "Trek", "War");

        var matchingMovies = mDatabaseProxy
            .searchMoviesTimed(DATABASE, watchedMovies);

        assertThat(matchingMovies.size())
            .isEqualTo(91);
    }

    public void testSearchMoviesExManySizeTimed() {
        var watchedMovies = List
            .of("Star", "Trek");

        var matchingMovies = mDatabaseProxy
            .searchMoviesExTimed(DATABASE, watchedMovies);

        assertThat(matchingMovies.size())
            .isEqualTo(13);
    }

    /**
     * Test the Movie microservice.
     */
    public void runMoviesTests() {
        // Run all the non-timed Database microservice tests.
        testGetMoviesSize();
        testGetMoviesContents();
        testSearchMoviesSize();
        testSearchMoviesContents();
        testSearchMoviesManySize();
        testSearchMoviesManyContents();
        testSearchMoviesExManySize();
        testSearchMoviesExManyContents();

        // Run all the timed Database microservice tests.
        testGetMoviesSizeTimed();
        testSearchMoviesSizeTimed();
        testSearchMoviesManySizeTimed();
        testSearchMoviesExManySizeTimed();
    }

    /**
     * Print results of the timed microservice calls.
     */
    public void printSyncTestResults() {
        var timings = mTimerSyncProxy
            .getTimings();

        System.out.println(timings);
    }
}

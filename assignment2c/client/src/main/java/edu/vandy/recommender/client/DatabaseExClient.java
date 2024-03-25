package edu.vandy.recommender.client;

import edu.vandy.recommender.common.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import reactor.test.StepVerifier;

import java.util.*;
import java.util.function.BiFunction;

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This client uses Spring WebMVC features to perform synchronous
 * remote method invocations on the {@code GatewayApplication} and the
 * {@code DATABASE_EX} microservice that it encapsulates.
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
public class DatabaseExClient {
    /**
     * This auto-wired field contains all the movies.
     */
    @Autowired
    Map<String, List<Double>> mMovies;

    /**
     * Create an instance of the {@link DatabaseExAPI} HTTP interface client,
     * which is then used to making HTTP requests to the {@code
     * GatewayApplication} RESTful microservice.
     */
    @Autowired
    DatabaseExAPI mDatabaseExAPI;

    /**
     * This auto-wired field connects the {@link RecommenderClient} to
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
     * @return A {@link List} of {@link Movie} objects that match all
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
     * @return A {@link List} of {@link Movie} objects that match at
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
        StepVerifier
            .create(mDatabaseExAPI
                    .getMovies()
                    .collectList())
            .expectNextMatches(matchingMovies ->
                               matchingMovies.size() == 4801)
            .as("The getMovies() count wasn't as expected")
            .verifyComplete();
    }

    public void testGetMoviesContents() {
        StepVerifier
            .create(mDatabaseExAPI
                    .getMovies()
                    .collectList())
            .expectNextMatches(matchingMovies ->
                               matchingMovies.size() == 4801
                               && checkResults
                               (matchingMovies,
                                Collections
                                .emptyList(),
                                DatabaseExClient
                                ::findMoviesMatchingAnyQueries))
            .as("The getMovies() checks weren't as expected")
            .verifyComplete();
    }

    public void testSearchMoviesSize() {
        var searchWord = "Museum";
        StepVerifier
            .create(mDatabaseExAPI
                    .searchMovies(searchWord)
                    .collectList())
            .expectNextMatches(matchingMovies ->
                               matchingMovies.size() == 3)
            .as("The searchMovies() count wasn't as expected")
            .verifyComplete();
    }

    public void testSearchMoviesContents() {
        var searchWord = "Museum";

        StepVerifier
            .create(mDatabaseExAPI
                    .searchMovies(searchWord)
                    .collectList())
            .expectNextMatches(matchingMovies ->
                               matchingMovies.size() == 3
                               && checkResults
                               (matchingMovies,
                                List.of(searchWord),
                                DatabaseExClient
                                ::findMoviesMatchingAnyQueries))
            .as("The searchMovies() checks weren't as expected")
            .verifyComplete();
    }

    public void testSearchMoviesManySize() {
        var watchedMovies = List
            .of("Iron", "Steal", "Star", "Trek", "War");

        StepVerifier
            .create(mDatabaseExAPI
                    .searchMovies(watchedMovies)
                    .collectList())
            .expectNextMatches(matchingMovies ->
                               matchingMovies.size() == 91)
            .as("The searchMoviesMany() count wasn't as expected")
            .verifyComplete();
    }

    public void testSearchMoviesManyContents() {
        var watchedMovies = List
            .of("Iron", "Steal", "Star", "Trek", "War");

        StepVerifier
            .create(mDatabaseExAPI
                    .searchMovies(watchedMovies)
                    .collectList())
            .expectNextMatches(matchingMovies ->
                               matchingMovies.size() == 91
                               && checkResults
                               (matchingMovies,
                                watchedMovies,
                                DatabaseExClient
                                ::findMoviesMatchingAnyQueries))
            .as("The searchMoviesMany() checks weren't as expected")
            .verifyComplete();
    }

    public void testSearchMoviesExManySize() {
        var watchedMovies = List
            .of("Star", "Trek");

        StepVerifier
            .create(mDatabaseExAPI
                    .searchMoviesEx(watchedMovies)
                    .collectList())
            .expectNextMatches(matchingMovies ->
                               matchingMovies.size() == 13)
            .as("The searchMoviesExMany() count wasn't as expected")
            .verifyComplete();
    }

    public void testSearchMoviesExManyContents() {
        var watchedMovies = List
            .of("Star", "Trek");

        StepVerifier
            .create(mDatabaseExAPI
                    .searchMoviesEx(watchedMovies)
                    .collectList())
            .expectNextMatches(matchingMovies ->
                               matchingMovies.size() == 13
                               && checkResults
                               (matchingMovies,
                                watchedMovies,
                                DatabaseExClient
                                ::findMoviesMatchingAllQueries))
            .as("The searchMoviesExMany() checks weren't as expected")
            .verifyComplete();
    }

    public void testGetMoviesSizeTimed() {
        StepVerifier
            .create(mDatabaseExAPI
                    .getMoviesTimed()
                    .collectList())
            .expectNextMatches(matchingMovies ->
                               matchingMovies.size() == 4801)
            .as("The getMoviesTimed() count wasn't as expected")
            .verifyComplete();
    }

    public void testSearchMoviesSizeTimed() {
        var searchWord = "Museum";
        StepVerifier
            .create(mDatabaseExAPI
                    .searchMoviesTimed(searchWord)
                    .collectList())
            .expectNextMatches(matchingMovies ->
                               matchingMovies.size() == 3)
            .as("The getMoviesTimed() count wasn't as expected")
            .verifyComplete();
    }

    public void testSearchMoviesManySizeTimed() {
        var watchedMovies = List
            .of("Iron", "Steal", "Star", "Trek", "War");

        StepVerifier
            .create(mDatabaseExAPI
                    .searchMoviesTimed(watchedMovies)
                    .collectList())
            .expectNextMatches(matchingMovies ->
                               matchingMovies.size() == 91)
            .as("The searchMoviesTimed() count wasn't as expected")
            .verifyComplete();
    }

    public void testSearchMoviesExManySizeTimed() {
        var watchedMovies = List
            .of("Star", "Trek");

        StepVerifier
            .create(mDatabaseExAPI
                    .searchMoviesExTimed(watchedMovies)
                    .collectList())
            .expectNextMatches(matchingMovies ->
                               matchingMovies.size() == 13)
            .as("The searchMoviesExManyTimed() count wasn't as expected")
            .verifyComplete();
    }

    /**
     * Test the Movie microservice.
     */
    public void runMoviesTests() {
        // Run all the non-timed DATABASE_EX microservice tests.
        testGetMoviesSize();
        testGetMoviesContents();
        testSearchMoviesSize();
        testSearchMoviesContents();
        testSearchMoviesManySize();
        testSearchMoviesManyContents();
        testSearchMoviesExManySize();
        testSearchMoviesExManyContents();

        // Run all the timed DATABASE_EX microservice tests.
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

package edu.vandy.recommender.client;

import edu.vandy.recommender.client.proxies.RecommenderAsyncProxy;
import edu.vandy.recommender.common.MovieTestCheckers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static edu.vandy.recommender.common.Converters.rankings2titles;
import static edu.vandy.recommender.common.MovieTestCheckers.*;

/**
 * This client uses Spring WebMVC features to perform synchronous
 * remote method invocations on the {@code GatewayApplication} and
 * various microservices that it encapsulates.
 * <p>
 * The {@code @Component} annotation allows Spring to automatically
 * detect custom beans, i.e., Spring will scan the application for
 * classes annotated with {@code @Component}, instantiate them, and
 * inject the specified dependencies into them without having to write
 * any explicit code.
 * <p>
 * The {@code ComponentScan} annotation instructs Spring to scan for
 * components that are within the "edu.vandy.recommender" package.
 */
@Component
@ComponentScan("edu.vandy.recommender")
public class RecommenderAsyncClient {
    /**
     * This auto-wired field contains a {@link Map} of all the movies.
     */
    @Autowired
    Map<String, List<Double>> mMovies;

    /**
     * This auto-wired field connects the {@link
     * RecommenderAsyncClient} to the {@link RecommenderAsyncProxy}
     * that performs HTTP requests asynchronously.
     */
    @Autowired
    private RecommenderAsyncProxy mRecommenderAsyncProxy;

    public void testGetMoviesSize(String strategy, boolean timed) {
        StepVerifier
            .create(mRecommenderAsyncProxy
                .getMovies(strategy, timed)
                .collectList())
            .expectNextMatches(matchingMovies ->
                matchingMovies.size() == 4801)
            .as("The getMovies() count wasn't as expected")
            .verifyComplete();
    }

    public void testGetMoviesContent(String strategy, boolean timed) {
        StepVerifier
            .create(mRecommenderAsyncProxy
                .getMovies(strategy, timed)
                .collectList())
            .expectNextMatches(matchingMovies ->
                matchingMovies.size() == 4801
                    && checkResults
                    (mMovies.keySet(),
                        rankings2titles(matchingMovies),
                        Collections
                            .emptyList(),
                        MovieTestCheckers
                            ::findMoviesMatchingAnyQueries))
            .as("The getMovies() checks weren't as expected")
            .verifyComplete();
    }

    public void testSearchMoviesSize(String strategy, boolean timed) {
        var searchWord = "Museum";
        StepVerifier
            .create(mRecommenderAsyncProxy
                .search(strategy, searchWord, timed)
                .collectList())
            .expectNextMatches(matchingMovies ->
                matchingMovies.size() == 3)
            .as("The searchMovies() count wasn't as expected")
            .verifyComplete();
    }

    public void testSearchMoviesContents(String strategy, boolean timed) {
        var searchWord = "Museum";

        StepVerifier
            .create(mRecommenderAsyncProxy
                .search(strategy, searchWord, timed)
                .collectList())
            .expectNextMatches(matchingMovies ->
                matchingMovies.size() == 3
                    && checkResults
                    (mMovies.keySet(),
                        rankings2titles(matchingMovies),
                        List.of(searchWord),
                        MovieTestCheckers
                            ::findMoviesMatchingAnyQueries))
            .as("The searchMovies() checks weren't as expected")
            .verifyComplete();
    }

    public void testMovieRecommendationSize(String strategy,
                                            boolean timed) {
        int maxCount = 25;

        var watchedMovie = "Night at the Museum";

        StepVerifier
            .create(mRecommenderAsyncProxy
                .getRecommendations(strategy,
                    watchedMovie,
                    maxCount,
                    timed)
                .collectList())
            .expectNextMatches(matchingMovies ->
                matchingMovies.size() == maxCount)
            .as("The recommendations() count wasn't as expected")
            .verifyComplete();
    }

    public void testMovieRecommendationManySize(String strategy,
                                                boolean timed) {
        int maxCount = 25;

        var watchedMovies = List
            .of("Iron Man", "Star Wars", "Star Trek", "WarGames");

        StepVerifier
            .create(mRecommenderAsyncProxy
                .getRecommendations(strategy,
                    watchedMovies,
                    maxCount,
                    timed)
                .collectList())
            .expectNextMatches(matchingMovies ->
                matchingMovies.size() == maxCount)
            .as("The recommendationsMany() count wasn't as expected")
            .verifyComplete();
    }

    /**
     * Run the asynchronous tests.
     */
    public void runTests(String strategy) {
        // Run all the non-timed 'strategy' microservice tests.
        testGetMoviesSize(strategy, false);
        testGetMoviesContent(strategy, false);
        testSearchMoviesSize(strategy, false);
        testSearchMoviesContents(strategy, false);
        testMovieRecommendationSize(strategy, false);
        testMovieRecommendationManySize(strategy, false);

        // Run all the timed 'strategy' microservice tests.
        testGetMoviesSize(strategy, true);
        testSearchMoviesSize(strategy, true);
        testMovieRecommendationSize(strategy, true);
        testMovieRecommendationManySize(strategy, true);
    }
}

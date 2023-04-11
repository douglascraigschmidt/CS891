package edu.vandy.recommender.client.proxies;

import edu.vandy.recommender.client.proxies.ParallelFluxAPI;
import edu.vandy.recommender.common.model.Ranking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

import static edu.vandy.recommender.common.Constants.Service.PARALLEL_FLUX;

/**
 * This class is a proxy to the {@code Recommender*} microservices and
 * the {@code Timer} microservice.
 */
@Component
public class RecommenderAsyncProxy {
    /**
     * This auto-wired field connects the {@link ParallelFluxAPI} to
     * the {@link RecommenderAsyncProxy} that performs HTTP requests
     * asynchronously.
     */
    @Autowired
    ParallelFluxAPI mParallelFluxAPI;

    /**
     * Return a {@link List} of all movie titles.
     *
     * @param strategy The implementation strategy to invoke (e.g.,
     *                 "structureconcurrency", "parallelstream",
     *                 "sequentialloop", etc.)
     * @param timed True if the implementation strategy should be
     *              timed, else false
     * @return A {@link Flux} that emits {@link Ranking} objects
     *         containing the movie titles
     */
    public Flux<Ranking> getMovies(String strategy,
                                   boolean timed) {
        // TODO -- you fill in here, replacing 'return null' with
        // the proper code.
        return null;
    }

    /**
     * Search for movie titles in the database containing the given
     * query {@link String}.
     *
     * @param strategy The implementation strategy to invoke (e.g.,
     *                 "structureconcurrency", "parallelstream",
     *                 "sequentialloop", etc.)
     * @param query The {@link String} to search for
     * @param timed True if the implementation strategy should be
     *              timed, else false
     * @return A {@link Flux} that emits {@link Ranking} objects
     *         containing movie titles that match the {@code query}
     */
    public Flux<Ranking> search(String strategy,
                                String query,
                                boolean timed) {
        // TODO -- you fill in here, replacing 'return null' with
        // the proper code.
        return null;
    }

    /**
     * Recommend {@code maxCount} movies from the movie database as a
     * function of a single {@code watchedMovie} passed as a request
     * parameter containing the title of the movie that has been
     * watched.
     *
     * @param strategy The implementation strategy to invoke (e.g.,
     *                 "parallelflux", "concurrentflux", etc.)
     * @param watchedMovie A {@link String} indicating the title of
     *                     the movie that has been watched
     * @param maxCount The upper limit for the number of
     *                 recommendations returned
     * @return A {@link Flux} that emits {@link Ranking} objects whose
     *         movie titles are most similar to the {@code
     *         watchedMovie}
     */
    public Flux<Ranking> getRecommendations(String strategy,
                                            String watchedMovie,
                                            int maxCount,
                                            boolean timed) {
        // TODO -- you fill in here, replacing 'return null' with
        // the proper code.
        return null;
    }

    /**
     * Recommend the given number of movies from the database as a
     * function of movies the user has watched previously, indicated
     * by a {@link List} of movie titles in the request body.
     *
     * @param strategy The implementation strategy to invoke (e.g.,
     *                 "parallelflux", "concurrentflux", etc.)
     * @param watchedMovies A {@link List} of titles of movies the
     *                      user has watched
     * @param maxCount The upper limit for the number of
     *                 recommendations returned
     * @return A {@link Flux} that emits {@link Ranking} objects whose
     *         movie titles are most similar to those in {@code
     *         watchedMovies}
     */
    public Flux<Ranking> getRecommendations(String strategy,
                                            List<String> watchedMovies,
                                            int maxCount,
                                            boolean timed) {
        // TODO -- you fill in here, replacing 'return null' with
        // the proper code.
        return null;
    }
}


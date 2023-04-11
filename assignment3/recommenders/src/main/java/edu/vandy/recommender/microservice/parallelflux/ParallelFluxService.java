package edu.vandy.recommender.microservice.parallelflux;

import edu.vandy.recommender.common.BaseService;
import edu.vandy.recommender.common.model.Ranking;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;

import java.util.Comparator;
import java.util.List;

import static edu.vandy.recommender.common.Converters.titles2Rankings;
import static edu.vandy.recommender.common.CosineSimilarityUtils.cosineSimilarity;
import static edu.vandy.recommender.common.CosineSimilarityUtils.sumOfCosines;
import static edu.vandy.recommender.common.GetTopRecommendationsFlux.getTopRecommendationsHeap;
import static edu.vandy.recommender.common.GetTopRecommendationsFlux.getTopRecommendationsSort;
import static java.util.stream.Collectors.toList;

/**
 * This class defines implementation methods that are called by the
 * {@link ParallelFluxController}, which serves as the main
 * "front-end" app gateway entry point for remote clients that want to
 * receive movie recommendations reactively.
 *
 * This class implements the abstract methods in {@link BaseService}
 * using the Project Reactor framework and its support for reactive
 * parallel computing via the {@link ParallelFlux} class.
 *
 * This class is annotated as a Spring {@code @Service}, which enables
 * the automatic detection and wiring of dependent implementation
 * classes via classpath scanning. It also includes its name in the
 * {@code @Service} annotation below so that it can be identified as a
 * service.
 */
@Service
public class ParallelFluxService
       extends BaseService<Flux<Ranking>> {
    /**
     * Get a {@link Flux} that emits all movies represented as {@link
     * String} objects.
     *
     * @return A {@link Flux} that emits movie titles represented as
     *        {@link String} objects sorted in ascending order
     *        (ignoring case)
     */
    @Override
    public Flux<Ranking> getAllMovies() {
        // Convert the mMoviesMap keyset into a Flux via a helper
        // method in the Converters class.

        // TODO -- you fill in here, replacing 'return null'
        // with the proper code.
        return null;
    }

    /**
     * Search for the movie titles in the database containing the
     * given query {@link String} using Project Reactor reactive
     * types.
     *
     * @param query The search query
     * @return A {@link Flux} that emits movie titles containing the
     *         query represented as {@link String} objects in
     *         ascending sorted order (ignoring case)
     */
    @Override
    public Flux<Ranking> search(String query) {
        // Perform the following steps using a Project Reactor
        // ParallelFlux.
        // 
        // 1. Create a Flux of Ranking objects containing movie titles
        //    from mMoviesMap using a helper method in the Converters class.
        // 2. Convert Flux to a ParallelFlux.
        // 3. Run each rail in the parallel thread pool.
        // 4. Only keep titles that contain the search query (ignore
        //    case).
        // 5. Convert ParallelFlux to Flux.
        // 6. Sort Ranking objects by title in ascending order before
        //    returning.

        // TODO -- you fill in here, replacing 'return null' with
        // the proper code.
        return null;
    }

    /**
     * Recommend {@code maxCount} movies from our movie database as a
     * function of a single {@code watchedMovie}, indicated by a
     * request parameter containing the title of the movie that has
     * been watched.
     *
     * @param watchedMovie A {@link String} indicating the title of
     *                     the movie that has been watched
     * @param maxCount The upper limit for the number of
     *                 recommendations returned
     * @return A {@link Flux} that emits movie titles most similar to
     *         the {@code watchedMovie}
     */
    @Override
    public Flux<Ranking> getRecommendations(String watchedMovie,
                                            int maxCount) {
        // Compute the cosine similarities and return the movie titles
        // in order from greatest to least similarity based on the
        // watchedMovie param.

        // Start by checking that the 'watchedMovie' exists in
        // mMoviesMap, returning an empty Flux if it's not there.

        // TODO -- you fill in here, replacing 'return null' with
        // the proper code.

        // Next, create a local Flux variable that is initialized via
        // the following steps:
        //
        // 1. Call a helper method to get the vector portion of the
        //    input movie and create a ParallelFlux stream containing
        //    all the entries in the Map.
        // 2. Filter out the 'watchedMovie' from the Flux stream.
        // 3. Convert the ParallelFlux back to a Flux.

        // TODO -- you fill in here, replacing 'Flux<Ranking> entries
        // = null' with the proper code.
        Flux<Ranking> entries = null;

        // Call a helper method that return the top maxCount
        // recommendations in the Flux of entries.
        return null;
    }

    /**
     * Recommend {@code maxCount} number of movies from our database
     * as a function of films the user has watched previously,
     * indicated by the {@code watchedMovies} {@link List}.
     *
     * @param watchedMovies A {@link List} of titles of movies the
     *                      user has watched
     * @param maxCount The upper limit for the number of
     *                 recommendations returned
     * @return A {@link Flux} of movie titles most similar to those in
     *         {@code watchedMovies}
     */
    @Override
    public Flux<Ranking> getRecommendations(List<String> watchedMovies,
                                            int maxCount) {
        // Remove all movies from the watchedMovies List that do not
        // have a corresponding Movie in the mMovieMap map *without*
        // affecting the original contents of the watchedMovies List.
        // TODO -- you fill in here.

        // Perform the following steps using a Project Reactor
        // ParallelFlux.
        //
        // 1. Convert mMoviesMap into a flux.
        // 2. Convert the Flux to a ParallelFlux.
        // 3. Run the ParallelFlux on the parallel() Scheduler.
        // 4. Filter out 'watchedMovies' from the stream since they
        //    shouldn't be considered as recommendations.
        // 5. Rank movies by sum of cosine similarity functions
        //    to the previously watched movies.
        // 6. Convert the ParallelFlux back to a Flux.

        // TODO -- you fill in here, replacing 'Flux<Ranking> entries
        // = null' with the proper code.
        Flux<Ranking> entries = null;

        // Call a helper method to get/return the top maxCount
        // recommendations.
        // TODO -- you fill in here, replacing 'return null' with the
        // proper code.
        return null;
    }

    /**
     * Compute the cosine similarity between a movie and all other
     * movies in the database using Project Reactor {@link
     * ParallelFlux}.
     *
     * @param vector A {@link List} containing a vector description of
     *               the watched movie
     * @return A {@link ParallelFlux} that emits {@link Ranking}
     *         objects representing the cosine similarity between
     *         movies
     */
    protected ParallelFlux<Ranking> computeRecommendationsParallelFlux
        (List<Double> vector) {
        // Perform the following steps using a Project Reactor
        // ParallelFlux.
        //
        // 1. Convert mMoviesMap to a Flux.
        // 2. Convert the Flux to a ParallelFlux.
        // 3. Run the ParallelFlux on the parallel() Scheduler.
        // 4. Call the cosineSimilarity() helper method to create a
        //    new Ranking object.

        // TODO -- you fill in here, replacing 'return null' with the
        // proper code.
        return null;
    }
}

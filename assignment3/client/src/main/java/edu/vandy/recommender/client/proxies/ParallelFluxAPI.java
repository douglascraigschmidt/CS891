package edu.vandy.recommender.client.proxies;

import edu.vandy.recommender.common.model.Movie;
import edu.vandy.recommender.common.model.Ranking;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Flux;

import java.util.List;

import static edu.vandy.recommender.common.Constants.EndPoint.*;
import static edu.vandy.recommender.common.Constants.Params.MAX_COUNT_PARAM;
import static edu.vandy.recommender.common.Constants.Params.WATCHED_MOVIE_PARAM;
import static edu.vandy.recommender.common.Constants.Service.PARALLEL_FLUX;

/**
 * This interface provides the contract for the RESTful {@code
 * ParallelFluxController} API used in conjunction with the {@code
 * GatewayApplication}.  It defines the HTTP GET and POST methods that
 * can be used to interact with the {@code ParallelFluxController}
 * API, along with the expected request and response parameters for
 * each method.  However, since clients access the {@code
 * ParallelFluxController} API via the {@code GatewayApplication} it's
 * necessary to add a {@code @HttpExchange} prefix to this class.
 *
 * This interface uses Spring HTTP interface annotations that provide
 * metadata about the API, such as the type of HTTP request (i.e.,
 * {@code GET} or {@code POST}), the parameter types (which are
 * annotated with {@code GetExchange}, {@code PostExchange},
 * {@code @RequestPath}, {@code RequestBody}, or {@code RequestParam}
 * tags), and the expected response format.  HTTP interface uses these
 * annotations and method signatures to generate an implementation of
 * the interface that the client uses to make HTTP requests to the
 * API.
 */
@HttpExchange(PARALLEL_FLUX + "/")
public interface ParallelFluxAPI {
    /**
     * Get a {@link Flux} containing the requested movies.
     *
     * @return An {@link Flux} that emits {@link Ranking} objects
     */
    // TODO -- you fill in here.

    /**
     * Get a {@link Flux} containing the requested {@link Ranking}
     * objects.
     *
     * @param query The {@link String} to search for
     * @return A {@link Flux} that emits {@link Ranking} objects on
     *         success
     */
    // TODO -- you fill in here.

    /**
     * Recommend {@code maxCount} movies from our movie database as a
     * function of a {@code watchedMovie} the user has watched,
     * indicated by a request parameter that contains the title of the
     * movie that has been watched.
     *
     * @param watchedMovie A {@link String} indicating the title of
     *                     the movie that has been watched
     * @param maxCount The upper limit for the number of
     *                 recommendations returned
     * @return A {@link Flux} that emits {@link Ranking} objects whose
     *         movie titles are most similar to the {@code
     *         watchedMovie}
     */
    // TODO -- you fill in here.

    /**
     * Recommend the {@code maxCount} movies from our database as a
     * function of films the user has watched, indicated by a {@link
     * List} of movie titles in the request body.
     *
     * @param watchedMovies {@link List<String>} containing titles
     *                      that the user has watched
     * @param maxCount The upper limit for the number of
     *                 recommendations returned
     * @return A {@link Flux} that emits {@link Ranking} objects whose
     *         movie titles are most similar to those in {@code
     *         watchedMovies}
     */
    // TODO -- you fill in here.

    /**
     * Get a {@link Flux} containing the requested movies .
     * 
     * This endpoint also records the execution run time of this call
     * via the {@code Timer} microservice.
     *
     * @return An {@link Flux} that emits {@link Ranking} objects on
     *         success
     */
    // TODO -- you fill in here.
    /**
     * Get a {@link Flux} containing the requested {@link Movie}
     * objects.
     * 
     * This endpoint also records the execution run time of this call
     * via the {@code Timer} microservice.
     *
     * @param query     The {@link String} to search for
     * @return A {@link Flux} that emits {@link Ranking} objects that
     *         match any {@code queries} on success
     */
    // TODO -- you fill in here.

    /**
     * Recommend {@code maxCount} movies from our movie database as a
     * function of a {@code watchedMovie} the user has watched,
     * indicated by a request parameter that contains the title of the
     * movie that has been watched.
     * 
     * This endpoint also records the execution run time of this call
     * via the {@code Timer} microservice.
     *
     * @param watchedMovie A {@link String} indicating the title of
     *                     the movie that has been watched
     * @param maxCount The upper limit for the number of
     *                 recommendations returned
     * @return A {@link Flux} that emits {@link Ranking} objects whose
     *         movie titles are most similar to the {@code
     *         watchedMovie}
     */
    // TODO -- you fill in here.

    /**
     * Recommend the {@code maxCount} movies from our database as a
     * function of films the user has watched, indicated by a {@link
     * List} of movie titles in the request body.
     * 
     * This endpoint also records the execution run time of this call
     * via the {@code Timer} microservice.
     *
     * @param watchedMovies {@link List<String>} containing titles
     *                      that the user has watched.
     * @param maxCount The upper limit for the number of
     *                 recommendations returned
     * @return A {@link Flux} that emits {@link Ranking} objects whose
     *         movie titles are most similar to those in {@code
     *         watchedMovies}
     */
    // TODO -- you fill in here.
}

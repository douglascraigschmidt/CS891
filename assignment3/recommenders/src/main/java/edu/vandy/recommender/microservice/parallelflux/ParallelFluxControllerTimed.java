package edu.vandy.recommender.microservice.parallelflux;

import edu.vandy.recommender.common.BaseController;
import edu.vandy.recommender.common.BaseControllerTimed;
import edu.vandy.recommender.common.RunTimer;
import edu.vandy.recommender.common.model.Movie;
import edu.vandy.recommender.common.model.Ranking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static edu.vandy.recommender.common.Constants.EndPoint.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * The Spring WebFlux controller for the {@link ParallelFluxService}
 * that handles timed method invocations.
 */
@RestController
@RequestMapping(TIMED)
public class ParallelFluxControllerTimed
// @@ Monte, I've not been able to get things to work via the
// BaseControllerTimed, so it's commented out for now.
    /* extends BaseControllerTimed<Flux<String>> */ {
    /**
     * Spring injected {@link ParallelFluxController}.
     */
    @Autowired
    private ParallelFluxController mController;

    /**
     * The {@link RunTimer} bridge that determines the elapsed run
     * time of each end point call and then posts the timing results
     * to the timer service to record for future analysis.
     */
    @Autowired
    private RunTimer mRunTimer;

    /**
     * Returns all movie titles in the database.
     * 
     * This endpoint also records the execution run time of this call.
     *
     * @return A {@link Flux} that emits all movie titles in the
     *         database
     */
    @GetMapping(GET_ALL_MOVIES)
    public Flux<Ranking> allMoviesTimed() {
        return mRunTimer
            // Delegate request to the service.
            .runAndRecordTime(mController.getId()
                              + ":"
                              + GET_ALL_MOVIES,
                              () -> mController.allMovies());
    }

    /**
     * Search for the movie titles in the database containing the
     * given query {@link String}.
     *
     * This endpoint also records the execution run time of this call
     * in the {@code Timer} microservice.
     *
     * @param query The search query
     * @return A {@link Flux} that emits movie titles containing the
     *         query represented as {@link String} objects in
     *         ascending sorted order (ignoring case).
     */
    @GetMapping(GET_SEARCH + "/" + SEARCH_QUERY)
    public Flux<Ranking> searchTimed(@PathVariable String query) {
        return mRunTimer
            // Forward request.
            .runAndRecordTime(mController.getId()
                              + ":"
                              + GET_SEARCH,
                              () -> mController.search(query));
    }

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
     * @return A {@link Flux} that emits movie titles most similar to
     *         the {@code watchedMovie}
     */
    @GetMapping(GET_RECOMMENDATION)
    public Flux<Ranking> recommendationsTimed(@RequestParam String watchedMovie,
                                              @RequestParam int maxCount) {
        return mRunTimer
            // Delegate request to service.
            .runAndRecordTime(mController.getId()
                              + ":"
                              + GET_RECOMMENDATION,
                              () -> mController.recommendations(watchedMovie,
                                                                       maxCount));
    }

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
     * @return A {@link Flux} that emits movie titles most similar to
     *         those in {@code watchedMovies}
     */
    @PostMapping(POST_RECOMMENDATIONS)
    public Flux<Ranking> recommendationsTimed(@RequestBody List<String> watchedMovies,
                                              @RequestParam int maxCount) {
        return mRunTimer
            // Delegate request to service.
            .runAndRecordTime(mController.getId()
                              + ":"
                              + POST_RECOMMENDATIONS,
                              () -> mController.recommendations(watchedMovies,
                                                                maxCount));
    }
}

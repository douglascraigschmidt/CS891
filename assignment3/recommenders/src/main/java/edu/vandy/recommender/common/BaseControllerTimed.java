package edu.vandy.recommender.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static edu.vandy.recommender.common.Constants.EndPoint.*;

/**
 * A common controller implementation that computes the time needed to
 * perform the endpoint handler methods defined below.
 */
public abstract class BaseControllerTimed<T> {
    /**
     * Spring injected {@link BaseController}.
     */
    @Lazy
    @Autowired
    private BaseController<T> mController;

    /**
     * The {@link RunTimer} bridge that determines the elapsed run
     * time of each end point call and then posts the timing results
     * to the timer service to record for future analysis.
     */
    @Autowired
    private RunTimer mRunTimer;

    /**
     * Returns all movie titles in the database.
     * <p>
     * This endpoint also records the execution run time of this call.
     *
     * @return A list of all movie titles in the database
     */
    @GetMapping(GET_ALL_MOVIES)
    public T allMoviesTimed() {
        System.out.println("allMoviesTimed()");
        return mRunTimer
            // Delegate request to the service.
            .runAndRecordTime(mController.getId()
                              + ":"
                              + GET_ALL_MOVIES,
                () -> mController.allMovies());
    }

    /**
     * Search for the movie titles in the database containing the given query
     * {@link String}.
     * <p>
     * This endpoint also records the execution run time of this call
     * in the {@code Timer} microservice.
     *
     * @param query The search query
     * @return A {@link List} of movie titles containing the query represented as
     * {@link String} objects in ascending
     * sorted order (ignoring case).
     */
    @GetMapping(GET_SEARCH + "/" + SEARCH_QUERY)
    public T searchTimed(@PathVariable String query) {
        System.out.println("searchTimed()");
        return mRunTimer
            // Forward request.
            .runAndRecordTime(mController.getId()
                              + ":"
                              + GET_SEARCH,
                () -> mController.search(query));
    }

    /**
     * Recommend {@code maxCount} movies from our movie database as a function
     * of a {@code watchedMovie} the user has watched, indicated by a request
     * parameter that contains the title of the movie that has been watched.
     * <p>
     * This endpoint also records the execution run time of this call
     * via the {@code Timer} microservice.
     *
     * @param watchedMovie A {@link String} indicating the title of the movie
     *                     that has been watched
     * @param maxCount     The upper limit for the number of
     *                     recommendations returned
     * @return A {@link T} of movie titles most similar to the
     * {@code watchedMovie}
     */
    @GetMapping(GET_RECOMMENDATIONS)
    public T recommendationsTimed(@RequestParam String watchedMovie,
                                  @RequestParam int maxCount) {
        System.out.println("recommendationsTimed()");
        return mRunTimer
            // Delegate request to service.
            .runAndRecordTime(mController.getId()
                              + ":"
                              + GET_RECOMMENDATIONS,
                () -> mController.recommendations(watchedMovie,
                    maxCount));
    }

    /**
     * Recommend the {@code maxCount} movies from our database as a function of
     * films the user has watched, indicated by a {@link List} of movie titles
     * in the request body.
     * <p>
     * This endpoint also records the execution run time of this call
     * via the {@code Timer} microservice.
     *
     * @param watchedMovies {@link List<String>} containing titles that the user
     *                      has watched.
     * @param maxCount      The upper limit for the number of recommendations
     *                      returned
     * @return A {@link T} of movie titles most similar to
     * those in {@code watchedMovies}
     */
    @PostMapping(POST_RECOMMENDATIONS)
    public T recommendationsTimed(@RequestBody List<String> watchedMovies,
                                  @RequestParam int maxCount) {
        System.out.println("recommendationsTimedMany()");
        return mRunTimer
            // Delegate request to service.
            .runAndRecordTime(mController.getId()
                              + ":"
                              + POST_RECOMMENDATIONS,
                () -> mController.recommendations(watchedMovies,
                    maxCount));
    }
}

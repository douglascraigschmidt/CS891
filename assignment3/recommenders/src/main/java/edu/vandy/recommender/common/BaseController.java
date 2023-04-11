package edu.vandy.recommender.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import edu.vandy.recommender.common.RunTimer;

import static edu.vandy.recommender.common.Constants.EndPoint.*;
import static edu.vandy.recommender.common.Constants.Params.MAX_COUNT_PARAM;
import static edu.vandy.recommender.common.Constants.Params.WATCHED_MOVIE_PARAM;

/**
 * A common controller implementation that redirects all requests to custom
 * services (e.g., sequential, concurrent, parallel, reactive, asynchronous,
 * etc.) to perform.
 */
// @RestController
public abstract class BaseController<T> {
    /**
     * The central interface to provide configuration for the
     * application.  This field is read-only while the application is
     * running.

    @Autowired
    ApplicationContext mApplicationContext;

    /**
     * The service to delegate requests.
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    BaseService<T> service;

    /**
     * @return The {@link BaseService} encapsulated by the controller

    public BaseService<T> getService() {
        return service;
    }

    /**
     * @return The application id

    public String getId() {
        return mApplicationContext.getId();
    }

    /**
     * A request for testing Eureka connection.
     *
     * @return The application name

    @GetMapping({"/", "actuator/info"})
    public ResponseEntity<String> info() {
        // Indicate the request succeeded and return the
        // application name and thread name.
        return ResponseEntity
            .ok(mApplicationContext.getId()
                + " is alive and running on "
                + Thread.currentThread()
                + "\n");
    }

    /**
     * Returns all movie titles in the database.
     *
     * @return A {@link T} of all movie titles in the database

    @GetMapping(GET_ALL_MOVIES)
    public T allMovies() {
        System.out.println("allMovies()");
        return getService()
            // Delegate request to the service.
            .getAllMovies();
    }

    /**
     * Search for the movie titles in the database containing the given query
     * {@link String}.
     *
     * @param query The search query
     * @return A list of movie titles containing the query represented as
     * {@link String} objects in ascending sorted order (ignoring case).

    @GetMapping(GET_SEARCH + "/" + SEARCH_QUERY)
    public T search(@PathVariable String query) {
        System.out.println("search()");
        return getService()
            // Delegate request to getService().
            .search(query);
    }

    /**
     * Recommend {@code maxCount} movies from our movie database as a function
     * of a {@code watchedMovie} the user has watched, indicated by a request
     * parameter that contains the title of the movie that has been watched.
     *
     * @param watchedMovie A {@link String} indicating the title of the movie
     *                     that has been watched
     * @param maxCount     The upper limit for the number of
     *                     recommendations returned
     * @return A {@link T} of movie titles most similar to the
     * {@code watchedMovie}

    @GetMapping(GET_RECOMMENDATION)
    public T recommendations(@RequestParam(WATCHED_MOVIE_PARAM) String watchedMovie,
                             @RequestParam(MAX_COUNT_PARAM) int maxCount) {
        System.out.println("recommendations()");
        return getService()
            // Delegate request to service.
            .getRecommendations(watchedMovie, maxCount);
    }

    /**
     * Recommend the {@code maxCount} movies from our database as a
     * function of films the user has watched, indicated by a {@link
     * List} of movie titles in the request body.
     *
     * @param watchedMovies {@link List<String>} containing titles that the user
     *                      has watched.
     * @param maxCount      The upper limit for the number of recommendations
     *                      returned
     * @return A {@link T} of movie titles most similar to
     * those in {@code watchedMovies}

    @PostMapping(POST_RECOMMENDATIONS)
    public T recommendations(@RequestBody List<String> watchedMovies,
                             @RequestParam(MAX_COUNT_PARAM) int maxCount) {
        System.out.println("recommendationsMany()");
        return getService()
            // Delegate request to service.
            .getRecommendations(watchedMovies, maxCount);
    }
                             */
}

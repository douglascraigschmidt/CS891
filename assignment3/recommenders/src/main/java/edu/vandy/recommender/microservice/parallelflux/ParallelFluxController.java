package edu.vandy.recommender.microservice.parallelflux;

import edu.vandy.recommender.common.BaseController;
import edu.vandy.recommender.common.model.Movie;
import edu.vandy.recommender.common.model.Ranking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static edu.vandy.recommender.common.Constants.EndPoint.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * The Spring WebFlux controller for the {@link ParallelFluxService}.
 * 
 * {@code @RestController} is a convenience annotation for creating
 * Restful controllers. It is a specialization of {@code @Component}
 * and is automatically detected through classpath scanning. It adds
 * the {@code @Controller} and {@code @ResponseBody} annotations. It
 * also converts responses to JSON.
 */
@RestController
public class ParallelFluxController
// @@ Monte, I've not been able to get things to work via the
// BaseController, so it's commented out for now.
      /* extends BaseController<Flux<String>> */ {
    /**
     * The central interface to provide configuration for the
     * application.  This field is read-only while the application is
     * running.
    */
    @Autowired
    ApplicationContext mApplicationContext;

    // The service to delegate requests.
    @Autowired
    ParallelFluxService mService;

    /**
     * @return The {@link ParallelFluxService} encapsulated by the
     *         controller
    */
    public ParallelFluxService getService() {
        return mService;
    }

    /**
     * @return The application id
    */
    public String getId() {
        return mApplicationContext.getId();
    }

    /**
     * A request for testing Eureka connection.
     *
     * @return The application name
    */
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
     * @return A {@link Flux} that emits all movie titles in the database
    */
    @GetMapping(GET_ALL_MOVIES)
    public Flux<Ranking> allMovies() {
        return getService()
            // Delegate request to the service.
            .getAllMovies();
    }

    /**
     * Search for the movie titles in the database containing the given query
     * {@link String}.
     *
     * @param query The search query
     * @return A {@link Flux} that emits movie titles containing the
     *         query represented as {@link String} objects in
     *         ascending sorted order (ignoring case)
    */
    @GetMapping(GET_SEARCH + "/" + SEARCH_QUERY)
    public Flux<Ranking> search(@PathVariable String query) {
        return getService()
            // Delegate request to getService().
            .search(query);
    }

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
     * @return A {@link Flux} that emits movie titles most similar to
     *         the {@code watchedMovie}
    */
    @GetMapping(GET_RECOMMENDATION)
    public Flux<Ranking> recommendations(@RequestParam String watchedMovie,
                                         @RequestParam int maxCount) {
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
     * @param maxCount The upper limit for the number of recommendations
     *                 returned
     * @return A {@link Flux} that emits movie titles most similar to
     *         those in {@code watchedMovies}
     */
    @PostMapping(POST_RECOMMENDATIONS)
    public Flux<Ranking> recommendations(@RequestBody List<String> watchedMovies,
                                         @RequestParam int maxCount) {
        return getService()
            // Delegate request to service.
            .getRecommendations(watchedMovies, maxCount);
    }
}

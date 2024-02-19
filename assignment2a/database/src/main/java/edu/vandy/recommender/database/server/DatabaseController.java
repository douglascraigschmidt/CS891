package edu.vandy.recommender.database.server;

import edu.vandy.recommender.database.common.model.Movie;
import edu.vandy.recommender.database.common.RunTimer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static edu.vandy.recommender.database.common.Constants.EndPoint.*;

/**
 * The Spring controller for the {@link DatabaseService}, whose
 * endpoint handler methods return a {@link List} of objects
 * containing information about movies.
 *
 * {@code @RestController} is a convenience annotation for creating
 * Restful controllers. It is a specialization of {@code @Component}
 * and is automatically detected through classpath scanning. It adds
 * the {@code @Controller} and {@code @ResponseBody} annotations. It
 * also converts responses to JSON or XML.
 *
 * The {@code ComponentScan} annotation instructs Spring to scan for
 * components that are within the "edu.vandy.recommender.database"
 * package.
 */
@RestController
@ComponentScan("edu.vandy.recommender.database")
public class DatabaseController {
    /**
     * Application context is to return application in "/" endpoint.
     */
    @Autowired
    private ApplicationContext mApplicationContext;

    /**
     * Spring injected {@link DatabaseService}.
     */
    @Autowired
    private DatabaseService mService;

    /**
     * The {@link RunTimer} bridge that determines the elapsed run
     * time of each end point call and then posts the timing results
     * to the timer service to record for future analysis.
     */
    @Autowired
    private RunTimer mRunTimer;

    /**
     * A request for testing Eureka connection.
     *
     * @return The application name.
     */
    @GetMapping({"/", "/actuator/info"})
    ResponseEntity<String> info() {
        // Indicate the request succeeded and return the
        // application name and current thread information.
        return ResponseEntity
            .ok(mApplicationContext.getId()
                + " is alive and running on "
                + Thread.currentThread()
                + "\n");
    }

    /**
     * Returns all movies in the database.
     *
     * @return The {@link List} of all movies in the database
     */
    @GetMapping(GET_ALL_MOVIES)
    public List<Movie> getMovies() {
        return mService
            // Forward to the service.
            .getMovies();
    }

    /**
     * Search for movie titles in the database containing the given
     * query {@link String}.
     *
     * @param query The search query
     * @return A {@link List} of movie titles containing the query
     *         represented as {@link Movie} objects
     */
    @GetMapping(GET_SEARCH + SEARCH_QUERY)
    public List<Movie> search(@PathVariable String query) {
        return mService
            // Forward to the service.
            .search(query);
    }

    /**
     * Search for movie titles in the database containing the given
     * {@link String} queries
     *
     * @param queries The search queries
     * @return A {@link List} of movie titles containing the queries
     *         represented as {@link Movie} objects
     */
    @PostMapping(GET_SEARCHES)
    public List<Movie> search(@RequestBody List<String> queries) {
        return mService
            // Forward to the service.
            .search(queries);
    }

    /**
     * Returns all movies in the database.
     *
     * This endpoint also records the execution run time of this call
     * in the {@code Timer} microservice.
     *
     * @return The {@link List} of all movies in the database
     */
    @GetMapping(TIMED + "/" + GET_ALL_MOVIES)
    public List<Movie> getMoviesTimed() {
        return mRunTimer
            // Forward request.
            .runAndRecordTime(GET_ALL_MOVIES,
                              this::getMovies);
    }

    /**
     * Search for movie titles in the database containing the given
     * query {@link String}.
     *
     * This endpoint also records the execution run time of this call
     * in the {@code Timer} microservice.
     *
     * @param query The search query
     * @return A {@link List} of movie titles containing the query
     *         represented as {@link Movie} objects
     */
    @GetMapping(TIMED + "/" + GET_SEARCH + SEARCH_QUERY)
    public List<Movie> searchTimed(@PathVariable String query) {
        return mRunTimer
            // Forward request.
            .runAndRecordTime(GET_SEARCH,
                              () -> search(query));
    }

    /**
     * Search for movie titles in the database containing the given
     * {@link String} queries
     *
     * This endpoint also records the execution run time of this call
     * in the {@code Timer} microservice.
     *
     * @param queries The search queries
     * @return A {@link List} of movie titles containing the queries
     *         represented as {@link Movie} objects
     */
    @PostMapping(TIMED + "/" + GET_SEARCHES)
    public List<Movie> searchTimed(@RequestBody List<String> queries) {
        return mRunTimer
            // Forward request.
            .runAndRecordTime(GET_SEARCHES,
                              () -> search(queries));
    }
}

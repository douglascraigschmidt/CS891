package edu.vandy.recommender.database.server;

import edu.vandy.recommender.common.model.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static edu.vandy.recommender.common.Constants.EndPoint.*;

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
     * @return The application id
     */
    public String getId() {
        return mApplicationContext.getId();
    }

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
                + " DatabaseController is alive and running on "
                + Thread.currentThread()
                + "\n");
    }

    /**
     * @return A {@link Map} that associates the movie title with
     *         the cosine vector for each movie
     */
    @GetMapping(GET_MOVIES_MAP)
    public Map<String, List<Double>> getMoviesMap() {
        System.out.println("DatabaseController.getMoviesMap()");
        return mService
            // Forward to the service.
            .getMoviesMap();
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
    @GetMapping(GET_SEARCH + "/" + SEARCH_QUERY)
    public List<Movie> search(@PathVariable String query) {
        return mService
            // Forward to the service.
            .search(query);
    }

    /**
     * Search for movie titles in the database containing the given
     * {@link String} queries using Java parallel streams.
     *
     * @param queries The search queries
     * @return A {@link List} of movie titles containing the queries
     *         represented as {@link Movie} objects
     */
    @PostMapping(POST_SEARCHES)
    public List<Movie> search(@RequestBody List<String> queries) {
        return mService
            // Forward to the service.
            .search(queries);
    }

    /**
     * Search for movie titles in the database containing the given
     * {@link String} queries using a custom SQL query.
     *
     * @param queries The search queries
     * @return A {@link List} of movie titles containing the queries
     *         represented as {@link Movie} objects
     */
    @PostMapping(POST_SEARCHES_EX)
    public List<Movie> searchEx(@RequestBody List<String> queries) {
        return mService
            // Forward to the service.
            .searchEx(queries);
    }
}

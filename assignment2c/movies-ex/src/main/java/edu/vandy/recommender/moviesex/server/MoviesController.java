package edu.vandy.recommender.moviesex.server;

import edu.vandy.recommender.common.model.Movie;
import edu.vandy.recommender.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static edu.vandy.recommender.common.Constants.EndPoint.*;

/**
 * The Spring controller for the {@link MoviesService}, whose endpoint
 * handler methods return a {@link List} of objects containing
 * information about movies.
 *
 * {@code @RestController} is a convenience annotation for creating
 * Restful controllers. It is a specialization of {@code @Component}
 * and is automatically detected through classpath scanning. It adds
 * the {@code @Controller} and {@code @ResponseBody} annotations. It
 * also converts responses to JSON or XML.
 */
@RestController
public class MoviesController {
    /**
     * A central interface that provides configuration for this
     * microservice and is read-only while the application is running,
     */
    @Autowired
    ApplicationContext applicationContext;

    /**
     * Spring-injected {@link MoviesService}.
     */
    // TODO -- ensure that 'service' is autowired with the appropriate
    // @Bean factory method.
    // SOLUTION-START
    @Autowired
    // SOLUTION-END
    private MoviesService service;

    /**
     * A request for testing Eureka connection.
     *
     * @return The application name.
     */
    @GetMapping({"/", "/actuator/info"})
    ResponseEntity<String> info() {
        // Indicate the request succeeded and return the application
        // name.
        return ResponseEntity
            .ok(applicationContext.getId() 
                + " is alive and running on "
                + Thread.currentThread()
                + "\n");
    }

    /**
     * @return A {@link List} of all movies
     */
    // TODO -- Create an endpoint with an annotation that maps HTTP
    // GET requests onto a handler method for "all-movies"
    // (GET_ALL_MOVIES) that takes no parameters and forwards to the
    // MoviesService.getMovies() method.
    // SOLUTION-START
    @GetMapping(GET_ALL_MOVIES)
    public List<Movie> getMovies() {
        return service
            // Forward to the service.
            .getMovies();
    }
    // SOLUTION-END

    /**
     * Search for movie titles containing the given query {@link
     * String}.
     *
     * @param query The search query in regular expression form
     * @return A {@link List} of movie titles containing the query
     *         represented as {@link Movie} objects
     */
    // TODO -- Create an endpoint with an annotation that maps HTTP
    // GET requests onto a handler method for "search" (GET_SEARCH)
    // that uses a @PathVariable parameter and forwards to the
    // MoviesService.findMoviesMatchingQuery(String query) method.
    // SOLUTION-START
    @GetMapping(GET_SEARCH + "/" + SEARCH_QUERY)
    public List<Movie> search(@PathVariable String query) {
        return service
            // Forward to the service after decoding regexQuery via a
            // WebUtils helper method.
            .search(WebUtils.decodeQuery(query));
    }
    // SOLUTION-END

    /**
     * Search for movie titles containing the given {@link String}
     * queries
     *
     * @param queries The search queries in regular expression
     *                      form
     * @return A {@link List} of movie titles containing the queries
     *         represented as {@link Movie} objects
     */
    // TODO -- Create an endpoint with an annotation that maps HTTP
    // GET requests onto a handler method for "searches"
    // (GET_SEARCHES) that uses a @RequestParam parameter and forwards
    // to the MoviesService.findMoviesMatchingQueries(List<String> queries) method.
    // SOLUTION-START
    @GetMapping(GET_SEARCHES)
    public List<Movie> search(@RequestParam List<String> queries) {
        return service
            // Forward to the service after first decoding the regular
            // expression queries via a WebUtils helper method.
            .search(WebUtils.decodeQueries(queries));
    }
    // SOLUTION-END
}

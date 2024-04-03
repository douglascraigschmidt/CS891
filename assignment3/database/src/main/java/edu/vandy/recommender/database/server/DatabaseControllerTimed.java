package edu.vandy.recommender.database.server;

import edu.vandy.recommender.common.RunTimer;
import edu.vandy.recommender.common.model.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static edu.vandy.recommender.common.Constants.EndPoint.*;

/**
 * The Spring controller for the {@link DatabaseService}, whose timed
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
@RequestMapping(TIMED)
@ComponentScan("edu.vandy.recommender.database")
public class DatabaseControllerTimed {
    /**
     * Spring injected {@link DatabaseController}.
     */
    @Autowired
    private DatabaseController mController;

    /**
     * The {@link RunTimer} bridge that determines the elapsed run
     * time of each end point call and then posts the timing results
     * to the timer service to record for future analysis.
     */
    @Autowired
    private RunTimer mRunTimer;

    /**
     * Returns all movies in the database.
     *
     * This endpoint also records the execution run time of this call
     * in the {@code Timer} microservice.
     *
     * @return The {@link List} of all movies in the database
     */
    @GetMapping(GET_ALL_MOVIES)
    public List<Movie> getMoviesTimed() {
        return mRunTimer
            // Forward request.
            .runAndRecordTime(mController.getId()
                              + ":"
                              + GET_ALL_MOVIES,
                              () -> mController.getMovies());
    }

    /**
     * @return A {@link Map} that associates the movie title with
     *         the cosine vector for each movie
     */
    @GetMapping(GET_MOVIES_MAP)
    public Map<String, List<Double>> getMoviesMapTimed() {
        System.out.println("DatabaseControllerTimed.getMoviesMapTimed()");
        return mRunTimer
            // Forward request.
            .runAndRecordTime(mController.getId()
                        + ":"
                        + GET_MOVIES_MAP,
                () -> mController.getMoviesMap());
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
    @GetMapping(GET_SEARCH + "/" + SEARCH_QUERY)
    public List<Movie> searchTimed(@PathVariable String query) {
        return mRunTimer
            // Forward request.
            .runAndRecordTime(mController.getId()
                              + ":"
                              + GET_SEARCH,
                              () -> mController.search(query));
    }

    /**
     * Search for movie titles in the database containing any given
     * {@link String} queries using a custom SQL query.
     *
     * This endpoint also records the execution run time of this call
     * in the {@code Timer} microservice.
     *
     * @param queries The search queries
     * @return A {@link List} of movie titles containing any queries
     *         represented as {@link Movie} objects
     */
    @PostMapping(POST_SEARCHES)
    public List<Movie> searchTimed(@RequestBody List<String> queries) {
        return mRunTimer
            // Forward request.
            .runAndRecordTime(mController.getId()
                              + ":"
                              + POST_SEARCHES,
                              () -> mController.search(queries));
    }

    /**
     * Search for movie titles in the database containing all given
     * {@link String} queries using a custom SQL query.
     *
     * This endpoint also records the execution run time of this call
     * in the {@code Timer} microservice.
     *
     * @param queries The search queries
     * @return A {@link List} of movie titles containing all queries
     *         represented as {@link Movie} objects
     */
    @PostMapping(POST_SEARCHES_EX)
    public List<Movie> searchTimedEx(@RequestBody List<String> queries) {
        return mRunTimer
            // Forward request.
            .runAndRecordTime(mController.getId()
                              + ":"
                              + POST_SEARCHES_EX,
                              () -> mController.searchEx(queries));
    }
}

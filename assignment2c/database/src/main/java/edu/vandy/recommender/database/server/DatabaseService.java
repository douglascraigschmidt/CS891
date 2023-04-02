package edu.vandy.recommender.database.server;

import edu.vandy.recommender.common.model.Movie;
import edu.vandy.recommender.database.repository.DatabaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * This class defines implementation methods that are called by the
 * {@link DatabaseController}, which serves as the main "front-end"
 * app gateway entry point for remote clients that want to receive
 * movie recommendations.
 *
 * This class implements the methods forwarded by the {@link
 * DatabaseController} using the Java streams framework and
 * the JPA.
 *
 * This class is annotated as a Spring {@code @Service}, which enables
 * the automatic detection and wiring of dependent implementation
 * classes via classpath scanning.  It also includes its name in the
 * {@code @Service} annotation below so that it can be identified as a
 * service.
 */
@Service
public class DatabaseService {
    /**
     * Spring-injected repository.
     */
    @Autowired
    DatabaseRepository mRepository;

    /**
     * @return A {@link List} of all {@link Movie} database entries
     * sorted in ascending order by the {@link Movie} title (id)
     */
    public List<Movie> getMovies() {
        // Forward to the repository.
        // TODO -- you fill in here, replacing 'return null' with
        // the proper code.
        return null;
    }

    /**
     * Search for movie titles containing the given query {@link
     * String} using JPA.
     *
     * @param query The search query
     * @return A {@link List} of {@link Movie} objects containing
     *         the {@code query} sorted in ascending order by
     *         the {@link Movie} title (id)
     */
    public List<Movie> search(String query) {
        // Forward to the repository.
        // TODO -- you fill in here, replacing 'return null' with
        // the proper code.
        return null;
    }

    /**
     * Search for movie titles in the database containing the given
     * {@link String} queries using Java parallel streams.
     *
     * @param queries The {@link List} of search queries
     * @return A {@link List} of {@link Movie} objects containing the
     *         queries sorted in ascending order by the {@link Movie}
     *         title (id)
     */

    public List<Movie> search(List<String> queries) {
        // TODO -- you fill in here.
        return null;
    }

    /**
     * Search for movie titles in the database containing the given
     * {@link String} queries using a custom SQL query.
     *
     * @param queries The {@link List} of search queries
     * @return A {@link List} of {@link Movie} objects containing the
     *         queries sorted in ascending order by the {@link Movie}
     *         title (id)
     */
    public List<Movie> searchEx(List<String> queries) {
        // Use a custom SQL query to find all movies whose 'id'
        // matches the List of 'queries' and return them as a sorted
        // List of Movie objects that contain no duplicates.

        // TODO -- you fill in here.
        return null;
    }
}

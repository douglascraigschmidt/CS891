package edu.vandy.recommender.database.server;

import edu.vandy.recommender.database.common.model.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This class defines implementation methods that are called by the
 * {@link DatabaseController}, which serves as the main "front-end"
 * app gateway entry point for remote clients that want to receive
 * movie recommendations.
 *
 * This class implements the abstract methods in {@link
 * DatabaseService} using the Java sequential streams framework.
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
     *         sorted in ascending order by the {@link Movie} title
     *         (id)
     */
    public List<Movie> getMovies() {
        // Forward to the repository.
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
     * queries using JPA.
     *
     * @param queries The {@link List} of search queries
     * @return A {@link List} of {@link Movie} objects containing the
     *         queries sorted in ascending order by the {@link Movie}
     *         title (id)
     */
    public List<Movie> search(List<String> queries) {
        // Use Java parallel streams and the JPA to fine all movies
        // whose 'id' matches the List of 'queries' and return them as
        // a sorted List of Movie objects that contain no duplicates.

        // TODO -- you fill in here.
        return null;
    }
}

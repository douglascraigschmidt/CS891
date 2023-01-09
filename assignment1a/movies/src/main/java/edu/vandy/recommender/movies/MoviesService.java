package edu.vandy.recommender.movies;

import edu.vandy.recommender.movies.model.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This class defines implementation methods that are called by the
 * {@link MoviesController} to return a {@link List} of objects
 * containing information about movies.
 *
 * This class is annotated as a Spring {@code @Service}, which enables
 * the automatic detection and wiring of dependent implementation
 * classes via classpath scanning.  It also includes its name in the
 * {@code @Service} annotation below so that it can be identified as a
 * service.
 */
@Service
public class MoviesService {
    /**
     * This auto-wired field connects the {@link MoviesService} to the
     * {@link List} of {@link Movie} objects.
     */
    // TODO -- ensure that mMovies is autowired with the appropriate
    // @Bean factory method.
    List<Movie> mMovies;

    /**
     * @return A {@link List} of all the movies
     */
    public List<Movie> getMovies() {
        // TODO -- you fill in here, replacing 'return null' with
        // the proper code.
        return null;
    }

    /**
     * Search for movie titles containing the given query {@link
     * String} using the Java sequential streams framework.
     *
     * @param query The search query
     * @return A {@link List} of {@link Movie} objects containing the
     *         query
     */
    public List<Movie> search(String query) {
        // Locate all movies whose 'id' matches the 'query' and return
        // them as a List of Movie objects.

        // TODO -- you fill in here, replacing 'return null' with
        // the proper code.
        return null;
    }

    /**
     * Search for movie titles in the database containing the given
     * {@link String} queries using the Java sequential streams
     * framework.
     *
     * @param queries The search queries
     * @return A {@link List} of {@link Movie} objects containing the
     *         queries
     */
    public List<Movie> search(List<String> queries) {
        // Locate all movies whose 'id' matches the List of 'queries'
        // and return them as a List of Movie objects.

        // TODO -- you fill in here, replacing 'return null' with
        // the proper code.
        return null;
   }
}

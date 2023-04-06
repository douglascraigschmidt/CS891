package edu.vandy.recommender.databaseex.server;

import edu.vandy.recommender.common.model.Movie;
import edu.vandy.recommender.databaseex.repository.DatabaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * This class defines implementation methods that are called by the
 * {@link DatabaseExController}, which serves as the main "front-end"
 * app gateway entry point for remote clients that want to receive
 * movie recommendations.
 *
 * This class implements the abstract methods in {@link
 * DatabaseExService} using the Java sequential streams framework.
 *
 * This class is annotated as a Spring {@code @Service}, which enables
 * the automatic detection and wiring of dependent implementation
 * classes via classpath scanning.  It also includes its name in the
 * {@code @Service} annotation below so that it can be identified as a
 * service.
 */
@Service
public class DatabaseExService {
    /**
     * Spring-injected repository.
     */
    @Autowired
    DatabaseRepository mRepository;

    /**
     * @return A {@link Flux} of all {@link Movie} database entries
     * sorted in ascending order by the {@link Movie} title (id)
     */
    public Flux<Movie> getMovies() {
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
     * @return A {@link Flux} of {@link Movie} objects containing
     *         the {@code query} sorted in ascending order by
     *         the {@link Movie} title (id)
     */
    public Flux<Movie> search(String query) {
        // Forward to the repository.
        // TODO -- you fill in here, replacing 'return null' with
        // the proper code.
        return null;
    }

    /**
     * Search for movie titles in the database containing the given
     * {@link String} queries using a custom SQL query.
     *
     * @param queries The {@link List} of search queries
     * @return A {@link Flux} of {@link Movie} objects containing the
     *         queries sorted in ascending order by the {@link Movie}
     *         title (id)
     */
    public Flux<Movie> search(List<String> queries) {
        // Forward to the repository.

        // TODO -- you fill in here, replacing 'return null' with
        // the proper code.
        return null;
    }

    /**
     * Search for movie titles in the database containing all the
     * given {@link String} queries using a custom SQL query.
     *
     * @param queries The {@link List} of search queries
     * @return A {@link Flux} of {@link Movie} objects containing all
     *         the queries sorted in ascending order by the {@link
     *         Movie} title (id)
     */
    public Flux<Movie> searchEx(List<String> queries) {
        // Forward to the repository.

        // TODO -- you fill in here, replacing 'return null' with
        // the proper code.
        return null;
    }
}

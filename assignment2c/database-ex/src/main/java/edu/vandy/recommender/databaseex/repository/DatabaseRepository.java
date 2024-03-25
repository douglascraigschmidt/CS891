package edu.vandy.recommender.databaseex.repository;

import edu.vandy.recommender.common.model.Movie;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * A persistent repository that contains information about {@link
 * Movie} objects and can be queried in various ways.
 *
 * The {@code @Repository} annotation indicates that this class
 * provides the mechanism for storage, retrieval, search, update and
 * delete operation on {@link Movie} objects.
 */
@Repository
public interface DatabaseRepository
       extends ReactiveCrudRepository<Movie, String>,
               MultiQueryRepository {
        /**
         * Find all {@link Movie} rows in the database that
         * contain the {@code query} (ignoring case) sorted
         * in ascending order.
         *
         * @param query The {@link String} query to search for
         * @return A {@link Flux} that emits {@link Movie} objects
         *         containing the {@code query} (ignoring case)
         *         sorted in ascending order
         */
    // TODO -- you fill in here.

    /**
     * @return A {@link Flux} that emits all {@link Movie} objects in
     *         the database sorted in ascending order by Id (movie
     *         title)
     */
    // TODO -- you fill in here.
}

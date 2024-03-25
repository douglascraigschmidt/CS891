package edu.vandy.recommender.databaseex.repository;

import edu.vandy.recommender.common.model.Movie;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * This implementation defines a method that returns a {@link List} of
 * {@link Movie} objects in the database containing at least one of
 * the {@code queries} (ignoring case) sorted in ascending order.
 */
public interface MultiQueryRepository {
    /**
     * Find a {@link Flux} that emits {@link Movie} objects in the database
     * that match at least one of the {@code queries} (ignoring case)
     * sorted in ascending order.
     *
     * @param queries List of queries
     * @return A {@link List} of {@link Movie} objects in the database
     *         containing at least one of the {@code queries}
     *         (ignoring case) sorted in ascending order
     */
    Flux<Movie> findAllByIdContainingAnyInOrderByAsc(List<String> queries);

    /**
     * Find a {@link Flux} that emits {@link Movie} objects in the
     * database containing all of the {@code queries} (ignoring case)
     * sorted in ascending order.
     *
     * @param queries List of queries
     * @return A {@link Flux} that emits {@link Movie} objects in the
     *         database containing all of the {@code queries}
     *         (ignoring case) sorted in ascending order
     */
    public Flux<Movie> findAllByIdContainingAllInOrderByAsc(List<String> queries);
}

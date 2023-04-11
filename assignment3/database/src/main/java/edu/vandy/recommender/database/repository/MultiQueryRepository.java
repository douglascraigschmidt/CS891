package edu.vandy.recommender.database.repository;

import edu.vandy.recommender.common.model.Movie;

import java.util.List;

/**
 * This implementation defines a method that returns a {@link List} of
 * {@link Movie} objects in the database containing at least one of
 * the {@code queries} (ignoring case) sorted in ascending order.
 */
public interface MultiQueryRepository {
    /**
     * Find a {@link List} of {@link Movie} objects in the database
     * containing at least one of the {@code queries} (ignoring case)
     * sorted in ascending order.
     *
     * @param queries List of queries
     * @return A {@link List} of {@link Movie} objects in the database
     *         containing at least one of the {@code queries}
     *         (ignoring case) sorted in ascending order
     */
    List<Movie> findAllByIdContainingInOrderByAsc(List<String> queries);
}

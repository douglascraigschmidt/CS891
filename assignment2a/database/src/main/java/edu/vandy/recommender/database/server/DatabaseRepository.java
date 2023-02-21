package edu.vandy.recommender.database.server;

import edu.vandy.recommender.database.common.model.Movie;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * A persistent repository that contains information about {@link
 * Movie} objects and can be queried in various ways.
 *
 * The {@code @Repository} annotation indicates that this class
 * provides the mechanism for storage, retrieval, search, update and
 * delete operation on {@link Movie} objects.
 */
// @SuppressWarnings("SpringDataRepositoryMethodParametersInspection")
@Repository
public interface DatabaseRepository
       extends JpaRepository<Movie, String> {
    /**
     * Find all {@link Movie} rows in the database that
     * contain the {@code query} (ignoring case).
     *
     * @param query The {@link String} query to search for
     * @return A {@link List} of {@link Movie} objects that
     *         contain the {@code query} (ignoring case)
     */
    // TODO -- you fill in here.

    /**
     * @return A {@link List} of all {@link Movie} objects in
     *         the database sorted in ascending order by Id
     *         (movie title)
     */
    // TODO -- you fill in here.
}

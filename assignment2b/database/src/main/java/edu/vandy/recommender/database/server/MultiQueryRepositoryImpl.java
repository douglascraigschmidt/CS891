package edu.vandy.recommender.database.server;

import edu.vandy.recommender.common.model.Movie;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;

import java.util.List;

/**
 * This implementation defines a method that returns a {@link List} of
 * {@link Movie} objects in the database containing at least one of
 * the {@code queries} (ignoring case) sorted in ascending order.
 */
public class MultiQueryRepositoryImpl
       implements MultiQueryRepository {
    /**
     * This field represents a session with the database, providing
     * the main API for performing CRUD (Create, Read, Update, Delete)
     * operations and querying the database.
     */
    @PersistenceContext
    private EntityManager mEntityManager;

    /**
     * Find a {@link List} of {@link Movie} objects in the database
     * containing all the {@code queries} (ignoring case)
     * sorted in ascending order.
     *
     * @param queries List of queries
     * @return A {@link List} of {@link Movie} objects in the database
     *         containing all the {@code queries}
     *         (ignoring case) sorted in ascending order
     */
    @Override
    public List<Movie> findAllByIdContainingInOrderByAsc(List<String> queries) {
        // TODO -- you fill in here, replacing 'return null' with the
        // proper code that builds the query using various JPA classes
        // like CriteriaBuilder and CriteriaQuery.

        // Get a CriteriaBuilder object from the EntityManager
        // associated with the current JPA transaction and use it to
        // create the criteria query that will be used to search for
        // movies.
        // TODO -- you fill in here.

        // Create a new criteria query of type Movie that's used to
        // specify the search criteria for the movies.
        // TODO -- you fill in here.

        // Create a Root object for the Movie entity that specifies
        // the entity to query.
        // TODO -- you fill in here.

        // Create an Expression object that represents the lower-cased
        // ID (title) field of the Movie entity that's used to create
        // the search predicate that matches the specified queries.
        // TODO -- you fill in here.

        // Call a helper method to get a Predicate that "ands" all the
        // queries together.
        // TODO -- you fill in here.

        // Call a helper method that performs the query and returns
        // the results.
        // TODO -- you fill in here, replacing 'return null' with
        // the proper solution.
        return null;
    }

    /**
     * Get a {@link Predicate} that "ands" all the {@code queries}
     * together.
     * 
     * @param queries The {@link List} of queries
     * @param criteriaBuilder Create the {@link CriteriaQuery} used to
     *                        search for quotes
     * @param idExpression The lower-cased "quote" column of the
     *                        {@link Movie} entity
     * @return A {@link Predicate} that "ands" all the {@code queries}
     *         together
     */
    protected static Predicate getPredicate
        (List<String> queries,
         CriteriaBuilder criteriaBuilder,
         Expression<String> idExpression) {
        // Use a Java sequential stream to build a Predicate that
        // "ands" all the lower-cased queries together.

        // TODO -- you fill in here, replacing 'return null' with the
        // proper solution.
        return null;
    }

    /**
     * Perform the query and return the results.
     * 
     * @param criteriaQuery A {@link CriteriaQuery} of type {@link
     *                       Movie} that specifies the search criteria
     *                       for the quotes
     * @param criteriaBuilder Create the {@link CriteriaQuery} used to
     *                        search for quotes
     * @param andPredicate A {@link Predicate} that "ands" all the
     *                     queries together
     * @param idExpression The lower-cased "quote" column of the
     *                        {@link Movie} entity
     * @return A {@link List} of {@link Movie} objects in the database
     *         containing at all of the {@code queries}
     *         (ignoring case)
     */
    protected List<Movie> getQueryResults
        (CriteriaQuery<Movie> criteriaQuery,
         CriteriaBuilder criteriaBuilder,
         Predicate andPredicate,
         Expression<String> idExpression) {
        // Create and execute a query that returns a List of
        // non-duplicate Movie objects ordered by Movie title (id) in
        // ascending order, where the Movie's id field contains all
        // the given queries.

        // TODO -- you fill in here, replacing 'return null' with the
        // proper solution.
        return null;
    }
}

package edu.vandy.recommender.databaseex.repository;

import edu.vandy.recommender.common.model.Movie;
import edu.vandy.recommender.utils.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * This implementation defines methods that return a {@link Flux} of
 * {@link Movie} objects in an R2DBC database.
 */
public class MultiQueryRepositoryImpl
       implements MultiQueryRepository {
    /**
     * This field is a reactive R2DBC-based client for executing SQL
     * queries against a database using reactive programming
     * constructs, such as {@link Flux} and {@link Mono}.  It provides
     * a fluent API to build and execute SQL queries in a reactive,
     * non-blocking way.
     */
    @Autowired
    private DatabaseClient mDatabaseClient;

    /**
     * Find a {@link Flux} that emits {@link Movie} objects in the
     * database containing at least one of the {@code queries}
     * (ignoring case) sorted in ascending order.
     *
     * @param queries A {@code List} of {@code String} object
     *                containing the queries to search for
     * @return A {@link Flux} that emits {@link Movie} objects in the
     *         database containing at least one of the {@code queries}
     *         (ignoring case) sorted in ascending order
     */
    @Override
    public Flux<Movie> findAllByIdContainingAnyInOrderByAsc
        (List<String> queries) {
        // First use a helper method to build an SQL query String that
        // will match Movie objects in the database containing any of
        // the queries.  Then use a second helper method to perform
        // the SQL query and return a Flux of matching Movie objects.

        // TODO -- you fill in here, replacing 'return null' with the
        // proper solution.
        return null;
    }

    /**
     * Find a {@link Flux} that emits {@link Movie} objects in the
     * database containing all of the {@code queries} (ignoring case)
     * sorted in ascending order.
     *
     * @param queries {@link List} of {@code queries}
     * @return A {@link Flux} that emits {@link Movie} objects in the
     *         database containing all the {@code queries} (ignoring
     *         case) sorted in ascending order
     */
    @Override
    public Flux<Movie> findAllByIdContainingAllInOrderByAsc
        (List<String> queries) {
        // First use a helper method to build an SQL query String that
        // will match Movie objects in the database containing all the
        // queries.  Then use a second helper method to perform the
        // SQL query and return a Flux of matching Movie objects.

        // TODO -- you fill in here, replacing 'return null' with the
        // proper solution.
        return null;
    }

    /**
     * This factory method builds an SQL query string from the
     * provided parameters.
     *
     * @param sqlString The SQL {@link String} to modify by replacing
     *                  the ':params' placeholder with wildcard values
     * @param whereFilter The filter for the {@code sqlString} to use
     *                    when concatenating the {@link List} of {@code
     *                    queries}
     * @param queries The {@link List} of query {@link String} objects
     *                to concatenate and use as wildcard values
     * @return A modified SQL query {@link String} with the ':params'
     *         placeholder replaced by a {@link String} of wildcard
     *         values
     */
    public static String buildQueryString(String sqlString,
                                      String whereFilter,
                                      List<String> queries) {
        // Return a String that replaces the ':params' placeholder in
        // the 'sqlString' param with a String of concatenated
        // wildcard values based on the input List of queries and then
        // adds an 'Order By' clause to sort the result set in
        // ascending order by the 'id' column in a case-insensitive
        // manner.

        // TODO -- you fill in here, replacing 'return null' with the
        // proper solution.
        // SOLUTION-START
        return null;
    }

    /**
     * Execute an R2DBC SQL query against the {@link Movie} database
     * and map the resulting rows to a {@link Flux} that emits objects
     * of the {@link Movie} class.
     *
     * @param sql the SQL query string to execute against the database
     * @return A {@link Flux} that emits {@link Movie} objects, each
     *         representing a row in the query result set that match
     *         the {@code sql} query
     */
    public Flux<Movie> getMovieFlux(String sql) {
        // Use the mDatabaseClient to perform a reactive SQL query and
        // return a Flux that emits matching Movie objects.  This
        // implementation should use the ArrayUtils.obj2List() helper
        // method to extract the 'vector' as a List<Double>.

        // TODO -- You fill in here, replacing 'return null' with the
        // proper solution.

        return null;
    }
}



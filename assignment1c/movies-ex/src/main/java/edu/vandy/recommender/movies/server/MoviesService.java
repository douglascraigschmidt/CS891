package edu.vandy.recommender.movies.server;

import edu.vandy.recommender.movies.common.model.Movie;
import edu.vandy.recommender.movies.server.utils.FutureUtils;
import jdk.incubator.concurrent.StructuredTaskScope;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.concurrent.ExecutionException;

/**
 * This class defines implementation methods that are called by the
 * {@link MoviesController} that to return a {@link List} of objects
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
     * Define a custom {@link Exception} that is thrown
     * if there's no match between a query and a {@link Movie}
     * title.
     */
    private static class NoSuchMatchException
        extends Exception {
    }

    /**
     * This auto-wired field connects the {@link MoviesService} to the
     * {@link List} of {@link Movie} objects.
     */
    // TODO -- ensure that mMovies is autowired with the appropriate
    // @Bean factory method.
    protected List<Movie> mMovies;

    /**
     * @return A {@link List} of all the movies
     */
    public List<Movie> getMovies() {
        // TODO -- you fill in here, replacing 'return null' with
        // the proper code.
        return null;
    }

    /**
     * Search for movie titles containing the given regular expression
     * query using Java structured concurrency.
     *
     * @param regexQuery The search query in regular expression form
     * @return A {@link List} of {@link Movie} objects containing the
     * query
     */
    public List<Movie> findMoviesMatchingQuery(String regexQuery) {
        // Use Java structured concurrency to locate all movies whose
        // 'id' matches the 'regexQuery' and return them as a List of
        // Movie objects.

        // Convert regexQuery to a Pattern.
        // TODO -- you fill in here.

        try (var scope =
             // Create a new StructuredTaskScope that shuts down on
             // failure.
             // TODO -- You fill in here, replacing
             // (StructuredTaskScope<Object>)null with the proper code.
             (StructuredTaskScope<Object>)null
        ) {
            // Call a helper method to concurrently get a List of all
            // Movie objects that match the patternList.
            // TODO -- You fill in here, replacing 'null' with
            // the proper code.

            // Perform a barrier synchronization that waits for all
            // the tasks to complete.
            // TODO -- you fill in here.

            // Throw an Exception upon failure of any tasks.
            // TODO -- you fill in here.

            // Call a helper method that returns a List of Movie
            // objects that matched at least one client query.
            // TODO -- you fill in here, replacing 'return null' with
            // the proper code.
            return null;
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Search for movie titles containing the given {@link List} of
     * regular expression queries using Java structured concurrency.
     *
     * @param regexQueries The search queries in regular expression
     *                     form
     * @return A {@link List} of {@link Movie} objects containing the
     * queries
     */
    public List<Movie> findMoviesMatchingQueries(List<String> regexQueries) {
        // Use Java structured concurrency to locate all movies whose
        // 'id' matches the List of 'regexQueries' and return them as
        // a List of Movie objects.

        // Convert the 'regexQueries' into a List of Pattern objects.
        // TODO -- you fill in here.

        try (var scope =
             // Create a new StructuredTaskScope that shuts down on
             // failure.
             // TODO -- You fill in here, replacing
             // (StructuredTaskScope<Object>)null with the proper code.
             (StructuredTaskScope<Object>)null
        ) {
            // Call a helper method to concurrently get a List of all
            // Movie objects that match the patternList.
            // TODO -- You fill in here, replacing 'null' with
            // the proper code.

            // Perform a barrier synchronization that waits for all
            // the tasks to complete.
            // TODO -- you fill in here.

            // Throw an Exception upon failure of any tasks.
            // TODO -- you fill in here.

            // Call a helper method that concatenates all matches and
            // returns a List of Movie objects that matched at least
            // one client query.
            // TODO -- you fill in here, replacing 'return null' with
            // the proper code.
            return null;
        }
        catch (Exception exception) {
            System.out.println("Exception: " + exception.getMessage());
            throw new RuntimeException(exception);
        }
    }

    /**
     * Convert the {@link List} of {@code regexQueries} into a {@link
     * List} of compiled regular expression {@link Pattern} objects
     *
     * @param regexQueries The {@link List} of regular expression
     *                     queries
     * @return a {@link List} of compiled regular expression {@link
     * Pattern} objects
     */
    protected List<Pattern> makePatterns(List<String> regexQueries) {
        // Use a Java sequential stream to perform the following
        // steps:
        //
        // 1. Convert the List to a Stream.
        // 
        // 2. Call a helper method to compile each regular expression.
        // 
        // 3. Convert the Stream to a List and return that List.

        // TODO -- you fill in here, replacing 'return null' with the
        // proper code.
        return null;
    }

    /**
     * Compile each regular expression.
     *
     * @param regexQuery The regular expression to compile
     * @return A compiled {@link Pattern}
     */
    protected Pattern makePattern(String regexQuery) {
        // Perform the following steps:
        //
        // 1. Compile the regexQuery into a Pattern and return that
        //    Pattern.

        // TODO -- you fill in here, replacing 'return null' with the
        // proper code.
        return null;
    }

    /**
     * Concurrently get a {@link List} of all {@link Movie} objects
     * that match the {@code patternList}.
     *
     * @param patternList A {@link List} of queries in compiled
     *                    regular expression form
     * @param scope       The {@link StructuredTaskScope} used to {@code
     *                    fork()} a virtual thread
     * @return A {@link List} of {@link Future} objects that will emit
     * a {@link List} of {@link Movie} objects that match
     * queries in {@code patternList}
     */
    @NotNull
    protected List<Future<Movie>> getMatchesForPatterns
    (List<Pattern> patternList,
     StructuredTaskScope.ShutdownOnFailure scope) {
        // Use a Java sequential stream to perform the following
        // steps:
        //
        // 1. Convert the List of Movie objects to a Stream.
        // 
        // 2. Use the scope param to concurrently call a helper method
        //    that itself uses a nested Java StructuredTaskScope to
        //    concurrently determine if the movie title matches with
        //    each regex query.
        // 
        // 3. Convert the Stream to a List and return that List.

        // TODO -- you fill in here, replacing 'return null' with the
        // proper code.
        return null;
    }

    /**
     * Concurrently get a {@link List} of {@link Future<Movie>}
     * objects that match the {@link Pattern}.
     *
     * @param pattern A query in compiled regular expression form
     * @param scope   The {@link StructuredTaskScope} used to {@code
     *                fork()} a virtual thread
     * @return A {@link List} of {@link Future} objects that will emit
     * a {@link Movie} object if the {@link Pattern} matches a
     * movie title or a {@code null} if there's no match
     */
    @NotNull
    protected List<Future<Movie>> getMatchesForPattern
    (Pattern pattern,
     StructuredTaskScope.ShutdownOnFailure scope) {
        // Use a Java sequential stream to perform the following
        // steps:
        //
        // 1. Convert the List of Movies into a Stream.
        // 
        // 2. Call a helper method that concurrently determines if the
        //    movie title matches the search query pattern.
        // 
        // 3. Convert the Stream to a List and return that List.

        // TODO -- you fill in here, replacing 'return null' with the
        // proper code.
        return null;
    }

    /**
     * Concurrently determine if any {@link Pattern} objects in
     * {@code patternList} match the {@link Movie}.
     *
     * @param patternList The {@link List} of search queries in
     *                    compiled regular expression form
     * @param movie       The {@link Movie} to match
     * @return A {@link Movie} if there's a match and a {@code null}
     * otherwise
     */
    protected Movie findMatchesForPatternsTask
    (List<Pattern> patternList,
     Movie movie) {
        // Use custom Java structured concurrency to determine if any
        // Pattern objects in patternList match the Movie.

        try (// Create a new ShutdownOnSuccess scope that shuts down
             // on the first match or throws a NoSuchMatchException if
             // there are no matches.
             var scope =
                     // TODO -- You fill in here, replacing
                     // (StructuredTaskScope<Object>)null with the proper code.
                     (StructuredTaskScope<Object>) null
        ) {

            // Call a helper method to concurrently determine if any
            // Pattern objects in patternList match the Movie.
            // TODO -- you fill in here.

            // Perform a barrier synchronization that waits for the
            // first successful computation to find a match or all of
            // them to fail to match.
            // TODO -- you fill in here.

            // Return the result of the concurrent matches.
            // TODO -- you fill in here, replacing 'return null' with
            // the proper code.
            return null;
        } catch (ExecutionException ex) {
            // If there are no matches scope.result() throws an
            // ExecutionException, so catch it and return null.
            return null;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Concurrently determine if any {@link Pattern} objects in
     * {@code patternList} match the {@link Movie}.
     *
     * @param patternList The {@link List} of search queries in
     *                    compiled regular expression form
     * @param movie       The {@link Movie} to match
     * @param scope   The {@link StructuredTaskScope.ShutdownOnSuccess} used to {@code
     *                    fork()} a virtual thread
     */
    protected void findMatchesAsync(List<Pattern> patternList,
                                    Movie movie,
                                    StructuredTaskScope.ShutdownOnSuccess<Movie> scope) {
        // Use the 'scope' determine if any Pattern objects in
        // 'patternList' match the 'movie' as follows:
        // 
        // 1. Functionally iterate through each pattern in
        //    patternList.
        //
        // 2. Create a virtual thread that calls a helper method to
        //    determine if 'movie' matches the search query, yielding
        //    'movie' if there's a match and 'null' otherwise.

        // TODO -- you fill in here.
    }

    /**
     * Use the Java {@link StructuredTaskScope.ShutdownOnFailure} to
     * concurrently determine if the {@code pattern} matches the
     * {@code movie}.
     *
     * @param pattern The search query in compiled regular expression
     *                form
     * @param movie   The {@link Movie} to match with
     * @param scope   The {@link StructuredTaskScope} used to {@code
     *                fork()} a virtual thread
     * @return A {@link Future} that emits a {@link Movie} object if
     * there's a match or {@code null} otherwise
     */
    @NotNull
    protected Future<Movie> findMatchAsync
    (Pattern pattern,
     Movie movie,
     StructuredTaskScope.ShutdownOnFailure scope) {
        // Use the 'scope' parameter to determine if the 'pattern'
        // matches the 'movie' object follows:
        // 
        // 1. Create a virtual thread to perform processing
        //    concurrently.
        // 
        // 2. Call a helper method to check for a match and yield
        //    'movie' if there's a match or else 'null'.

        // TODO -- you fill in here, replacing 'return null' with
        // the proper code.
        return null;
    }

    /**
     * Find a match between the {@code pattern} and the {@code movie}.
     *
     * @param pattern The query in compiled regular expression form
     * @param movie   The {@link Movie} to match with
     * @return True if there's a match, else false
     */
    protected boolean match(@NotNull Pattern pattern,
                            Movie movie) {
        // Find a match between the 'pattern' and the 'movie' by
        // performing the following steps:
        // 
        // 1. Create a Matcher for the movie's title.
        // 
        // 2. Call a Matcher method that returns true if there's a
        //    match, else false.

        // TODO -- you fill in here, replacing 'return false' with
        // the proper code.
        return false;
    }

    /**
     * Convert the {@link List} of {@link Future<Movie>} results
     * into a {@link List} of non-null {@link Movie} objects.
     *
     * @param results A {@link List} of {@link Future} objects
     *                containing a {@link Movie} object if
     *                a client query matched or else {@code null}
     * @return A {@link List} of non-null matching {@link Movie}
     * objects
     */
    @NotNull
    protected List<Movie> convertMovieMatches(List<Future<Movie>> results) {
        // Use a Java sequential stream to perform the following
        // steps:
        //
        // 1. Convert the List of Future<Movie>> objects to a Stream
        //    of Movie objects.
        //
        // 2. Filter out null values.
        //
        // 3. Convert the Stream to a List of Movie objects that are
        //    returned from the method.

        // TODO -- you fill in here, replacing 'return null' with
        // the proper code.
        return null;
    }
}

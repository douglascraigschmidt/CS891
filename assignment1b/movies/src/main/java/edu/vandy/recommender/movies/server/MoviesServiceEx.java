package edu.vandy.recommender.movies.server;

import edu.vandy.recommender.movies.common.model.Movie;
import edu.vandy.recommender.movies.utils.FutureUtils;
import jdk.incubator.concurrent.StructuredTaskScope;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

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
public class MoviesServiceEx {
    /**
     * This auto-wired field connects the {@link MoviesService} to the
     * {@link List} of {@link Movie} objects.
     */
    // @Bean factory method.
    @Autowired
    List<Movie> mMovies;

    /**
     * @return A {@link List} of all the movies
     */
    public List<Movie> getMovies() {
        // TODO -- you fill in here, replacing 'return null' with
        // the proper code.
        return mMovies;
    }

    /**
     * Search for movie titles containing the given query {@link
     * String} using the Java sequential streams framework.
     *
     * @param regex_query The search query in regular expression form
     * @return A {@link List} of {@link Movie} objects containing the
     *         query
     */
    public List<Movie> search(String regex_query) {
        // Locate all movies whose 'id' matches the 'query' and return
        // them as a List of Movie objects.

        // TODO -- you fill in here, replacing 'return null' with
        // the proper code.
        // Call overloaded search(list) variant to do the work.
        return search(List.of(regex_query));
    }

    /**
     * Search for movie titles in the database containing the given
     * query {@link String} using the Java sequential streams
     * framework.
     *
     * @param regex_queries The search queries in regular expression form
     * @return A {@link List} of {@link Movie} objects containing the
     *         queries
     */
    public List<Movie> search(List<String> regex_queries) {
        // Use Java structured concurrency to locate all movies whose
        // 'id' matches the List of 'queries' and return them as a
        // List of Movie objects.

        try (var scope = 
             // Create a new StructuredTaskScope that shutsdown on
             // failure.
             new StructuredTaskScope.ShutdownOnFailure()) {
            // Get a List of Futures to Optional<Movie> objects that
            // are being processed in parallel.
            var results = getFutureList(regex_queries,
                                        scope);

            // Perform a barrier synchronization that waits for all
            // the tasks to complete.

            // TODO -- you fill in here.
            scope.join();

            // Throw an Exception upon failure of any tasks.

            // TODO -- you fill in here.
            scope.throwIfFailed();

            // Return a List of Movie objects.
            return getMovieList(results);
        } catch (Exception exception) {
            System.out.println("Exception: " + exception.getMessage());
            throw new RuntimeException(exception);
        }
    }

    /**
     * Asynchronously determine which {@link Movie} titles match any
     * of the {@code regex_queries}.
     *
     * @param regex_queries The search queries in regular expression form
     * @param scope The {@link StructuredTaskScope} used to {@code
     *              fork()} a virtual thread
     * @return A {@link List} of {@link Future} objects containing an
     *         {@link Optional} with a {@link Movie} if there's a
     *         match or an empty {@link Optional} if there's no match
     */
    @NotNull
    private List<Future<Optional<Movie>>> getFutureList
        (List<String> regex_queries,
         StructuredTaskScope.ShutdownOnFailure scope) {
        // TODO -- you fill in here, replacing 'return null' with
        // the proper code.
        return mMovies
            // Convert the List to a Stream.
            .stream()

            // Asynchronously determine if the movie's title matches
            // any of the search queries.
            .map(movie ->
                 findMatchAsync(movie, regex_queries, scope))

            // Convert the Stream to a List.
            .toList();
    }

    /**
     * Get a {@link List} of {@link Movie} objects that matched at
     * least one regular expression query.
     *
     * @param results A {@link List} of {@link Future} objects
     *                containing an {@link Optional} with a {@link
     *                Movie} if there's a match or an empty {@link
     *                Optional} if there's no match
     * @return A {@link List} of {@link Movie} objects
     */
    @NotNull
    private static List<Movie> getMovieList
        (List<Future<Optional<Movie>>> results) {
        // TODO -- you fill in here, replacing 'return null' with
        // the proper code.
        return FutureUtils
            // Convert the List of Future<Optional<Movie>> objects to
            // a Stream of Optional<Movie> objects.
            .futures2Stream(results)

            // Eliminate empty Optional objects.
            .flatMap(Optional::stream)

            // Convert the Stream to a List.
            .toList();
    }

    /**
     * Asynchronously determine if the {@code movie} title matches
     * with the {@link List} of {@code regex_queries}.
     *
     * @param movie The {@link Movie} to match against
     * @param regex_queries The search queries in regular expression
     *                      form
     * @param scope The {@link StructuredTaskScope} used to {@code
     *              fork()} a virtual thread
     * @return A {@link Future} to an {@link Optional} contain a
     *         {@link Movie} if there's a match or an empty {@link
     *         Optional} if there's no match
     */
    private Future<Optional<Movie>> findMatchAsync
        (Movie movie,
         List<String> regex_queries,
         StructuredTaskScope.ShutdownOnFailure scope) {
        // TODO -- you fill in here, replacing 'return null' with
        // the proper code.
        return scope
            // Create a virtual thread.
            .fork(() -> Optional
                  // Create an empty Optional if there's no match,
                  // else the Optional contains the movie that
                  // matches.
                  .ofNullable(regex_queries
                              // Convert the List to a Stream.
                              .stream()
                              // Determine if there's any match.
                              .anyMatch(regex_query ->
                                        findMatch(regex_query, 
                                                  movie.id()))
                              ? movie
                              : null));
    }

    /**
     * Find a match between the {@code regex_query} and the {@code
     * movieTitle}.
     *
     * @param regex_query The query in regular expression form
     * @param movieTitle The title of the movie to match with
     * @return True if there's a match, else false
     */
    private boolean findMatch(String regex_query,
                              String movieTitle) {
        // TODO -- you fill in here, replacing 'return null' with the
        // proper code.

        // Decode the regular expression.
        String decodedQuery = URLDecoder
            .decode(regex_query,
                    StandardCharsets.UTF_8);

        // Create a regex pattern that ignores case.
        var pattern = Pattern
            .compile(decodedQuery,
                     Pattern.CASE_INSENSITIVE);

        return pattern
            // Create a PatternMatcher for 'movieTitle'.
            .matcher(movieTitle)

            // Return true if there's a match, else false.
            .find();
    }
}

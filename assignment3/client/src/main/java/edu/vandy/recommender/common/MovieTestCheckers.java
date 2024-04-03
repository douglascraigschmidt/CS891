package edu.vandy.recommender.common;

import edu.vandy.recommender.common.model.Movie;

import java.util.*;
import java.util.function.BiFunction;

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This Java utility class contains static methods that
 * help check the results from various microservices.
 */
public final class MovieTestCheckers {
    /**
     * A Java utility class should have a private constructor.
     */
    private MovieTestCheckers() {
    }

    /**
     * Print out the {@link Movie} titles.
     */
    public static void printMovieTitles(List<Movie> movies,
                                        String heading) {
        System.out.println(heading);
        movies
            .forEach(movie ->
                     System.out.println(movie.id));
    }

    /**
     * Returns a {@link List} of {@link Movie} objects that match all
     * search {@code queries}.
     *
     * @param movies  a {@link List} of {@link Movie} objects to search
     * @param queries a {@link List} of {@link String} representing
     *                the search queries to match against
     * @return A {@link List} of {@link Movie} objects that match all
     * search {@code queries}
     */
    public static List<String> findMoviesMatchingAllQueries
        (Set<String> movies,
         List<String> queries) {
        // Create a new List to store the movies that match the search
        // criteria.
        List<String> matchingMovies = new ArrayList<>();

        // Loop over each movie in the Set of movies.
        for (var entry : movies) {
            boolean matchesAllQueries = true;

            // Loop over each search query.
            for (String query : queries)
                // Check if the ID of the current movie contains the
                // current search query (ignoring case).
                if (!entry
                    .toLowerCase()
                    .contains(query.toLowerCase())) {
                    // Break out at the first non-match.
                    matchesAllQueries = false;
                    break;
                }

            // Only add movies that match all the search queries.
            if (matchesAllQueries)
                matchingMovies.add(entry);
        }

        // Sort the results by id.
        matchingMovies
            .sort(Comparator
                  .comparing(movie -> movie,
                             CASE_INSENSITIVE_ORDER));

        // Return the List of movies that match the search criteria.
        return matchingMovies;
    }

    /**
     * Returns a {@link List} of {@link Movie} objects that
     * match one or more search {@code queries}.
     *
     * @param movies  a {@link List} of {@link Movie} objects to search
     * @param queries a {@link List} of {@link String} representing
     *                the search queries to match against
     * @return A {@link List} of {@link Movie} objects that match at
     * least one search query
     */
    public static List<String> findMoviesMatchingAnyQueries
        (Set<String> movies,
         List<String> queries) {
        // Create a new List to store the movies that match the search
        // criteria.
        List<String> matchingMovies = new ArrayList<>();

        // Loop over each movie in the Set of movies.
        for (var entry : movies) {
            // Loop over each search query.
            for (String query : queries) {
                // Check if the ID of the current movie contains the
                // current search query (ignoring case).
                if (entry
                    .toLowerCase()
                    .contains(query.toLowerCase())) {
                    // If the movie matches the search criteria, add
                    // it to the list of matching movies.
                    matchingMovies.add(entry);
                    // Break out of the inner loop since we've found a
                    // match.
                    break;
                }
            }
        }

        // Sort the results by id.
        matchingMovies
            .sort(Comparator
                  .comparing(movie -> movie,
                             CASE_INSENSITIVE_ORDER));

        // Return the List of movies that match the search criteria.
        return matchingMovies;
    }

    /**
     * Ensure that the {@code movies} {@link List} returned from the
     * server matches the expected results.
     */
    public static boolean checkResults
        (Set<String> originalMovies,
         Collection<String> movies,
         List<String> queries,
         BiFunction<Set<String>,
         List<String>,
         List<String>> filterFunction) {
        if (!queries.isEmpty()) {
            var it1 = movies.iterator();
            var matchingMovies =
                filterFunction.apply(originalMovies,
                                     queries);
            assertThat(matchingMovies.size())
                .isEqualTo(movies.size());

            var it2 = matchingMovies
                .iterator();

            while (it1.hasNext()) {
                var next1 = it1.next();
                var next2 = it2.next();

                assertThat(next1)
                    .isEqualTo(next2);
            }
        }
        return true;
    }
}

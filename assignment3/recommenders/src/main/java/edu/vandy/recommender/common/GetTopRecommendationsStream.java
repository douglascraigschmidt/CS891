package edu.vandy.recommender.common;

import edu.vandy.recommender.common.model.Ranking;
import edu.vandy.recommender.utils.GetTopK;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This utility class provides static methods that use the Java Stream
 * framework to get the top {@code maxCount} recommendations via
 * either a Heap or by sorting.
 */
public class GetTopRecommendationsStream {
    /**
     * A Java utility class should have a private constructor.
     */
    private GetTopRecommendationsStream() {
    }

    /**
     * Recommend {@code maxCount} movies from the {@link Stream} of
     * distinct {@link Ranking} objects using a Heap data
     * structure. This implementation is optimized for the case where
     * a single movie is passed as a parameter.
     *
     * @param similarityStream A {@link Stream} of {@link Ranking} objects
     * @param maxCount The upper limit for the number of recommendations
     *                 returned
     * @return A {@link List} of movie titles judged most similar
     */
    public static List<String> getTopRecommendationsHeap
        (Stream<Ranking> similarityStream,
         int maxCount) {
        return GetTopK
            // Get the top maxCount entries.
            .getTopK(similarityStream, maxCount)

            // Extract just the movie titles.
            .map(Ranking::getTitle)

            // Collect and return ordered movie title list.
            .collect(Collectors.toList());
    }

    /**
     * Recommend {@code maxCount} movies from the {@link Stream} of
     * distinct {@link Ranking} objects by sorting the results.  This
     * implementation is intended for situations where multiple movies
     * are passed as a parameter, in which case there may be
     * duplicates.
     *
     * @param similarityStream A {@link Stream} of {@link Ranking} objects
     * @param maxCount The upper limit for the number of recommendations
     *                 returned
     * @return A {@link List} of movie titles judged most similar
     */
    public static List<String> getTopRecommendationsSort
        (Stream<Ranking> similarityStream,
         int maxCount) {
        return similarityStream

            // Sort the stream in reverse order.
            .sorted(Collections.reverseOrder())

            // Remove duplicates (keeps the first one).
            .distinct()

            // Limit the List of movies to just maxCount.
            .limit(maxCount)

            // Extract just the movie titles.
            .map(Ranking::getTitle)

            // Collect and return ordered movie title list.
            .collect(Collectors.toList());
    }
}

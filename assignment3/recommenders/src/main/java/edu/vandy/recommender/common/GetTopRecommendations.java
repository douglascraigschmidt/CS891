package edu.vandy.recommender.common;

import edu.vandy.recommender.common.model.Ranking;
import edu.vandy.recommender.utils.GetTopK;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.TreeSet;

/**
 * This utility class provides static methods that use classic Java
 * control constructs to get a {@link Collection} containing the top
 * {@code maxCount} recommendations via either a Heap or by sorting.
 */
public class GetTopRecommendations {
    /**
     * A Java utility class should have a private constructor.
     */
    private GetTopRecommendations() {}

    /**
     * Recommend {@code maxCount} movies from the {@link Collection} of
     * distinct {@link Ranking} objects using a Heap data structure.
     * This implementation is optimized for the case where a single
     * movie is passed as a parameter.
     *
     * @param similarityCollection A {@link Collection} of {@link Ranking} objects
     * @param maxCount         The upper limit for the number of recommendations
     *                         returned
     * @return A {@link Collection} of movie titles judged most similar
     */
    public static Collection<Ranking> getTopRecommendationsHeap
        (Collection<Ranking> similarityCollection,
         int maxCount) {
        var results = new ArrayList<Ranking>();

        // Iterate through the top maxCount entries.
        for (var entry : GetTopK.getTopK(similarityCollection, maxCount))
            // Add just the movie titles.
            results.add(new Ranking(entry.getTitle(),
                       0.0));

        // Return the Collection of movie titles judged most similar.
        return results;
    }

    /**
     * Recommend {@code maxCount} movies from the {@link Collection} of distinct
     * {@link Ranking} objects by sorting the results.  This implementation is
     * intended for situations where multiple movies are passed as a parameter,
     * in which case there may be duplicates.
     *
     * @param similarityCollection A {@link Collection} of {@link Ranking} objects
     * @param maxCount         The upper limit for the number of recommendations
     *                         returned
     * @return A {@link Collection} of movie titles judged most similar
     */
    public static Collection<Ranking> getTopRecommendationsSort
        (Collection<Ranking> similarityCollection,
         int maxCount) {

        // List to hold the results.
        var results = new ArrayList<Ranking>();

        // Get a List whose unique elements are sorted in reverse order.
        var collection =
            new TreeSet<>(similarityCollection).descendingSet();

        // Limit the results Collection of movies to a total of maxCount.
        for (int i = 0; i < maxCount; i++)
            // Add just the title to the results List.
            results.add(new Ranking(Objects.requireNonNull(collection.pollFirst())
                               .getTitle(), 0.0));

        // Return a Collection of movie titles judged most similar.
        return results;
    }
}

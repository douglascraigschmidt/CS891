package edu.vandy.recommender.common;

import edu.vandy.recommender.common.model.Ranking;
import edu.vandy.recommender.utils.GetTopK;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ParallelFlux;

import java.util.Comparator;

/**
 * This Java utility class provides static methods that use the
 * Project Reactor {@link Flux} class to get the top {@code maxCount}
 * recommendations via either a Heap or by sorting.
 */
public class GetTopRecommendationsFlux {
    /**
     * A Java utility class should have a private constructor.
     */
    private GetTopRecommendationsFlux() {
    }

    /**
     * Recommend {@code maxCount} movies from the {@link Flux} of distinct
     * {@link Ranking} objects using a Heap data structure. This implementation
     * is optimized for the case where a single movie is passed as a parameter.
     *
     * @param rankingFlux A {@link Flux} of {@link Ranking} objects
     * @param maxCount    The upper limit for the number of recommendations
     *                    returned
     * @return A {@link Flux} of movie titles ranked in descending similarity
     * order
     */
    public static Flux<Ranking> getTopRecommendationsHeap
        (Flux<Ranking> rankingFlux,
         int maxCount) {
        return rankingFlux
            // Collect the top maxCount entries into a Flux.
            .transform(GetTopK.getTopK(maxCount));
    }

    /**
     * Recommend {@code maxCount} movies from the {@link ParallelFlux}
     * of distinct {@link Ranking} objects by sorting the results.
     * This implementation is intended for situations where multiple
     * movies are passed as a parameter, in which case there may be
     * duplicates.
     *
     * @param rankingFlux A {@link Flux} of {@link Ranking} objects
     * @param maxCount The upper limit for the number of
     *                 recommendations returned
     * @return A {@link Flux} of movie titles ranked in descending
     *         similarity order
     */

    public static Flux<Ranking> getTopRecommendationsSort
        (Flux<Ranking> rankingFlux,
         int maxCount) {
        return rankingFlux
            // Sort the stream in reverse order.
            .sort(Comparator.reverseOrder())

            // Remove duplicates (but keeps the first one).
            .distinct()

            // Limit the results to just maxCount.
            .take(maxCount);
    }
}

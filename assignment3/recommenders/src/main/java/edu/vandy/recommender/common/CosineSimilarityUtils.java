package edu.vandy.recommender.common;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * This class computes the cosine similarity value for two {@link
 * List}s of {@link Double} objects using Java sequential or parallel
 * streams.
 */
public class CosineSimilarityUtils {
    /**
     * Compute the cosine similarity value for two {@link List}s of
     * {@link Double} objects using Java sequential or parallel
     * streams.
     *
     * @param a The first {@link List} of {@link Double} objects
     * @param b The second {@link List} of {@link Double} objects
     * @param parallel True if parallel streams should be used, else false if
     *                 sequential streams should be used
     * @return The computed cosine similarity index
     */
    public static double cosineSimilarity(List<Double> a,
                                          List<Double> b,
                                          boolean parallel) {
        // The implementation below is based on suggestions at
        // https://stackoverflow.com/a/52702353 and makes sense if the
        // size of the lists is quite large! This program might be
        // faster if we used an iterative cosine similarity algorithm,
        // but this implementation shows off (parallel)stream logic.

        // Sum the values in streamA (in parallel if requested).
        double normA = (parallel
                        ? a.parallelStream()
                        : a.stream())
            .mapToDouble(x -> x * x)
            .sum();

        // Sum the values in streamB (in parallel if requested).
        double normB =
            (parallel
             ? b.parallelStream()
             : b.stream())
            .mapToDouble(x -> x * x)
            .sum();

        // Compute the dot product (in parallel if requested).
        double dotProduct =
            (parallel
             ? IntStream
             .range(0, a.size())
             .parallel()
             : IntStream.range(0, a.size()))
            .mapToDouble(i -> a.get(i) * b.get(i))
            .sum();

        // Return the cosine similarity value.
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * Calculates sum of cosine similarities of the {@code vector}
     * with vectors from the {@link List} of {@code watchedMovies}.
     *
     * @author Serhii Mytsyk
     * @param vector Vector of the movie we want to calculate similarity
     * @param watchedMovies {@link List} of watched movies to compare
     *                      with the vector
     * @param vectorMap {@link Map} from movie to its vector
     * @return A {@link double} containing the sum of cosine
     *         similarities
     */
    public static double sumOfCosines(List<Double> vector,
                                      List<String> watchedMovies,
                                      Map<String, List<Double>> vectorMap,
                                      boolean parallel) {
        return StreamSupport
            // Create either a parallel or sequential Stream.
            .stream(watchedMovies.spliterator(), parallel)

            // Compute the cosine similarity of each watched movie
            // with the vector.
            .map(watchedMovie -> CosineSimilarityUtils
                 .cosineSimilarity(vector,
                                   vectorMap.get(watchedMovie),
                                   parallel))

            // Sum all the cosine similarities together.
            .reduce(0.0, Double::sum);
    }
}

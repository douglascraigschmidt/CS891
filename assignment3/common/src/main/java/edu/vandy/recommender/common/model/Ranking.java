package edu.vandy.recommender.common.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Movie cosine similarity ranking used by all service recommendation end points
 * when calculating recommendations.  This class is {@link Comparable} based on
 * the {@link Ranking#cosineSimilarity} attribute, whereas equality is based
 * on the movie {@link Ranking#movieTitle} attribute.
 */
public class Ranking
       implements Comparable<Ranking> {
    /**
     * The movie title.
     */
    public String movieTitle;

    /**
     * The cosine similarity value.
     */
    public Double cosineSimilarity;

    /**
     * A default constructor is needed.
     */
    public Ranking() {
    }

    /**
     * Creates an instance of this class that contains a movie title and cosine
     * similarity. The cosine similarity is calculated by comparing the
     * attribute vector of this movie with another movie (not included).
     *
     * @param movieTitle the movie title represented by this entry
     * @param cosineSimilarity The cosine similarity represented
     *                         by this entry
     */
    public Ranking(String movieTitle,
                   Double cosineSimilarity) {
        this.movieTitle = movieTitle;
        this.cosineSimilarity = cosineSimilarity;
    }

    /**
     * @return The cosine similarity of this movie with another unspecified
     * movie
     */
    public Double getCosineSimilarity() {
        return cosineSimilarity;
    }

    /**
     * @return The movie title corresponding to this entry
     */
    public String getTitle() {
        return movieTitle;
    }

    /**
     * Compares the specified object with this object for equality.  Returns
     * {@code true} if the given objects have the same title, else
     * {@code false}.
     *
     * @param object Object to compare for equality with this {@link Ranking}
     * @return {@code true} if the given objects have the same title, else
     * {@code false}
     */
    public boolean equals(Object object) {
        return object instanceof Ranking other
            // Only compare the title String values.
            && movieTitle.equals(other.getTitle());
    }

    /**
     * Returns a hash code value for the movie title.  This method is supported
     * for the benefit of hash tables such as those provided by
     * {@link java.util.HashMap}.
     *
     * @return a hash code value for the movie title
     */
    @Override
    public int hashCode() {
        return Objects.hash(movieTitle);
    }

    /**
     * Returns a {@link String} representation of this object where a string
     * representation of the cosine similarity is followed by the equals
     * character ("<tt>=</tt>") followed by the string representation of the
     * movie title.
     *
     * @return A String representation of this {@link Ranking} object
     */
    public String toString() {
        return cosineSimilarity + "=" + movieTitle;
    }

    /**
     * Comparable implementation to support comparing {@link Ranking} objects
     * based on their cosine similarity values.  Returns a @return A negative
     * integer, zero, or a positive integer as the first argument is less than,
     * equal to, or greater than the second.
     *
     * @param ranking An {@link Ranking} object to compare to
     * @return A negative integer, zero, or a positive integer as the first
     * argument is less than, equal to, or greater than the second
     */
    @Override
    public int compareTo(@NotNull Ranking ranking) {
        return Double.compare(cosineSimilarity,
                              ranking.cosineSimilarity);
    }
}

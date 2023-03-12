package edu.vandy.recommender.common.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Movie title and vector as stored and returned from the database
 * microservice.
 */
@Entity
@Table(name = "MOVIE")
public class Movie
// Implement an interface that enables two Movie objects to be
// compared and checked for equality.
// TODO -- you fill in here
{
    /**
     * The movie name.
     */
    @Id
    public String id;

    /**
     * The encoding of the movie properties.
     */
    public List<Double> vector;

    public Movie() {
    }

    /**
     * Initialize columns in a {@link Movie}.
     */
    public Movie(String id, List<Double> vector) {
        this.id = id;
        this.vector = vector;
    }

    /**
     * Create a {@link Movie} from a {@link Map.Entry}.
     *
     * @param movie The {@link Map.Entry}
     */
    public Movie(Map.Entry<String, List<Double>> movie) {
        id = movie.getKey();
        vector = movie.getValue();
    }

    /**
     * Perform a case-insensitive comparison of this {@link Movie}
     * with the {@code other} {@link Movie} based on their IDs.
     *
     * @param other The {@link Movie} to compare to this {@link Movie}
     * @return A negative integer, zero, or a positive integer as this
     * movie's ID is less than, equal to, or greater than the
     * specified movie's ID (ignoring case)
     */
    // TODO -- you fill in here.

    /**
     * Overrides the equals method to compare two {@link Movie}
     * objects based on their id only.
     *
     * @param other The other {@link Object} to compare with this object
     * @return true if the object ids are equal, false otherwise
     */
    // TODO -- you fill in here.

    /**
     * @return A hash of the {@link Movie} id (title)
     */
    // TODO -- you fill in here.
}

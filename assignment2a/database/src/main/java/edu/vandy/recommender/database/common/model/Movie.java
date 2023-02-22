package edu.vandy.recommender.database.common.model;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;
import java.util.Objects;

/**
 * Movie title and vector as stored and returned from the database
 * microservice.
 */
@Value
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
@Entity // For Jpa
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
    @Column(name = "id", nullable = false)
    public String id;

    /**
     * The encoding of the movie properties.  {@link FetchType#EAGER}
     * ensures this code works with parallel stream.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    public List<Double> vector;

    /**
     * Compare this {@link Movie} with the {@code other} {@link Movie}
     * based on their IDs.
     *
     * @param other The {@link Movie} to compare to this {@link Movie}
     * @return A negative integer, zero, or a positive integer as this
     *         movie's ID is less than, equal to, or greater than the
     *         specified movie's ID
     */
    // TODO -- you fill in here.


    /**
     * Overrides the equals method to compare two {@link Movie}
     * objects based on their id and vector.
     *
     * @param object The {@link Object} to compare
     * @return true if the objects are equal, false otherwise.
     */
    // TODO -- you fill in here.

}

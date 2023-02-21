package edu.vandy.recommender.common;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;

/**
 * Movie title and vector as stored and returned from the database
 * microservice.
 */
@Value
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
public class Movie {
    /**
     * The movie name.
     */
    public String id;

    /**
     * The encoding of the movie properties.
     */
    public List<Double> vector;
}
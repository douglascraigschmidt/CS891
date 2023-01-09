package edu.vandy.recommender.movies.model;

import java.util.List;

/**
 * Movie title and vector as stored and returned from the Movies
 * microservice.
 */
public record Movie (
        /*
         * The movie name.
         */
        String id,

        /*
         * The encoding of the movie properties.
         */
        List<Double> vector) {
}

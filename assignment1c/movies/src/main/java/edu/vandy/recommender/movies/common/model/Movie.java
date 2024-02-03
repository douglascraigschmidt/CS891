package edu.vandy.recommender.movies.common.model;

import java.util.List;

/**
 * A "plain old Java object" (POJO") containing a movie's title and
 * properties vector that is stored and returned from the 'movies'
 * microservice.
 */
public record Movie 
    (/*
      * The movie title.
      */
     String id,

     /*
      * The encoding of the movie properties.
      */
     List<Double> vector) {}

package edu.vandy.recommender.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This abstract class defines the methods that are called by the
 * {@link BaseController}, which serves as the main "front-end" app gateway
 * entry point for remote clients that want to receive movie recommendations.
 *
 * This abstract class is used to collate common functionality of all Service
 * implementations and to enable a common interface for the
 * {@link BaseController} to delegate to.
 *
 * Any class that extends this abstract class should be annotated as a Spring
 * {@code @Service}, which enables the automatic detection and wiring of
 * dependent implementation classes via classpath scanning.
 */
public abstract class BaseService<T> {
    /**
     * This auto-wired {@link Map} field contains all movie cosine
     * vectors that are configured automatically by Spring sorted in
     * ascending order (ignoring case).
     */
    @Autowired
    /*
     * This annotation may be used on a field or parameter as a qualifier
     * for candidate beans when autowiring. It may also be used to annotate
     * other custom annotations that can then in turn be used as qualifiers.
     */
    @Qualifier("movieMap")
    protected Map<String, List<Double>> mMovieMap;

    /**
     * Get a {@link Collection} of all movies represented as {@link
     * String} objects.
     *
     * @return A {@link Collection} of movie titles represented as
     *         {@link String} objects
     */
    public abstract T getAllMovies();

    /**
     * Search for the movie titles in the database containing the
     * given query {@link String}.
     *
     * @param query The search query
     * @return A {@link List} of movie titles containing the query
     *         represented as {@link String} objects
     */
    public abstract T search(String query);

    /**
     * Recommend maxCount movies from our movie database as a function
     * of a single watched movie, indicated by a request parameter
     * giving the title of the movie that has been watched.
     *
     * @param watchedMovie A {@link String} indicating the title of the movie
     *                     that has been watched
     * @param maxCount     The upper limit for the number of recommendations
     *                     returned
     * @return A {@link List} of movie titles most similar to the
     *         {@code watchedMovie}
     */
    public abstract T getRecommendations(String watchedMovie,
                                         int maxCount);

    /**
     * Recommend the given number of movies from our database as a
     * function of films the user has watched previously, indicated by
     * a {@link List} of movie titles in the request body.
     *
     * @param watchedMovies A {@link List} of titles of movies the
     *                      user has watched
     * @param maxCount The upper limit for the number of
     *                 recommendations returned
     * @return A {@link List} of movie titles most similar to those in
     *         {@code watchedMovies}
     */
    public abstract T getRecommendations(List<String> watchedMovies,
                                         int maxCount);
}

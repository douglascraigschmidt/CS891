package edu.vandy.recommender.movies.client;

import edu.vandy.recommender.movies.common.model.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static edu.vandy.recommender.movies.common.Constants.EndPoint.*;
import static edu.vandy.recommender.movies.common.Constants.EndPoint.Params.QUERIES_PARAM;

/**
 * This class provides proxies to various endpoints in the 'movies'
 * microservice.
 */
@Component
public class MoviesSyncProxy {
    /**
     * The Spring-injected {@link RestTemplate}.
     */
    @Autowired
    private RestTemplate mMoviesRestTemplate;

    /**
     * @return A {@link List} of {@link Movie} objects
     */
    public List<Movie> getMovies() {
        // Use the UriComponentsBuilder to create a URI to the
        // "all-movies" endpoint of the 'movies' microservice.

        // TODO -- you fill in here, replacing 'String uri = null'
        // with the proper code.
        // SOLUTION-START
        String uri = UriComponentsBuilder
            .fromPath(GET_ALL_MOVIES)
            .build()
            .toUriString();
        // SOLUTION-END String uri = null;

        // Use WebUtils.makeGetRequestList() and mMoviesRestTemplate
        // to get a List of all movies from the 'movie' microservice.

        // TODO -- you fill in here, replacing 'List<Movie> movies =
        // null' with the proper code.
        // SOLUTION-START
        List<Movie> movies = WebUtils
            // Create and send a GET request to the server.
            .makeGetRequestList(
                mMoviesRestTemplate,
                uri,
                // Return type is a String array.
                Movie[].class);
        // SOLUTION-END List<Movie> movies = null;

        if (movies == null) {
            throw new IllegalStateException
                ("Can't retrieve movies from 'movie' microservice.");
        }

        return movies;
    }

    /**
     * Search for movie titles in the database containing the given
     * query {@link String}.
     *
     * @param regex_query The search query in regular expression form
     * @return A {@link List} of {@link Movie} objects that match the
     * query
     */
    public List<Movie> searchMovies(String regex_query) {
        // Use the UriComponentsBuilder to create a URI to the
        // "search" endpoint of the 'movies' microservice.  The
        // 'regex_query' should be encoded via WebUtils.encodeQuery()
        // prior to being to construct the URI.

        // TODO -- you fill in here, replacing 'String uri = null'
        // with the proper code.
        // SOLUTION-START
        String uri = UriComponentsBuilder
            .fromPath(GET_SEARCH
                          + "/"
                          + WebUtils.encodeQuery(regex_query))
            .build()
            .toUriString();
        // SOLUTION-END String uri = null;

        // Use WebUtils.makeGetRequestList() and mMoviesRestTemplate
        // to get a List of all matching movies from the 'movie'
        // microservice.
        // TODO -- you fill in here, replacing 'List<Movie> movies =
        // null' with the proper code.
        // SOLUTION-START
        List<Movie> movies = WebUtils
            // Create and send a GET request to the server.
            .makeGetRequestList(
                mMoviesRestTemplate,
                uri,
                // Return type is a Movie array.
                Movie[].class);
        // SOLUTION-END List<Movie> movies = null;

        if (movies == null) {
            throw new IllegalStateException
                ("Can't retrieve movies from 'movies' microservice.");
        }

        return movies;
    }

    /**
     * Search for movie titles in the database containing the given
     * {@link List} of queries.
     *
     * @param regex_queries The {@link List} queries to search for
     *                      in regular expression form
     * @return A {@link List} of {@link Movie} objects that match the
     * queries
     */
    public List<Movie> searchMovies(List<String> regex_queries) {
        // Use the UriComponentsBuilder to create a URI to the
        // "searches" endpoint of the 'movies' microservice.  You'll
        // need to convert 'regex_queries' into a String after
        // encoding them via WebUtils.encodeQuery().

        // TODO -- you fill in here, replacing 'String uri = null'
        // with the proper code.
        // SOLUTION-START
        String uri = UriComponentsBuilder
            .fromPath(GET_SEARCHES)
            .queryParam(
                QUERIES_PARAM,
                WebUtils
                    // Convert the List to a String.
                    .list2String(WebUtils.encodeQueries(regex_queries)))
            .build()
            .toUriString();
        // SOLUTION-END String uri = null;

        // Use WebUtils.makeGetRequestList() and mMoviesRestTemplate
        // to get a List of all matching movies from the 'movie'
        // microservice.
        // TODO -- you fill in here, replacing 'List<Movie> movies =
        // null' with the proper code.
        // SOLUTION-START
        List<Movie> movies = WebUtils
            // Create and send a GET request to the server.
            .makeGetRequestList(
                mMoviesRestTemplate,
                uri,
                // Return type is a Movie array.
                Movie[].class);
        // SOLUTION-END List<Movie> movies = null;

        if (movies == null) {
            throw new IllegalStateException
                ("Can't retrieve movies from 'movies' microservice.");
        }

        return movies;
    }
}


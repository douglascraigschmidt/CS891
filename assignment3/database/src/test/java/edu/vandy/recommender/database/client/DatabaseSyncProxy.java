package edu.vandy.recommender.database.client;

import edu.vandy.recommender.common.model.Movie;
import edu.vandy.recommender.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static edu.vandy.recommender.common.Constants.EndPoint.*;
import static edu.vandy.recommender.common.Constants.Service.DATABASE;

/**
 * This class provides synchronous proxies to the endpoints in the
 * 'database' microservice.
 */
@Component
public class DatabaseSyncProxy {
    /**
     * The Spring-injected {@link RestTemplate}.
     */
    @Autowired
    @Qualifier(DATABASE)
    private RestTemplate mRestTemplate;

    /**
     * @return A {@link List} of {@link Movie} objects
     */
    public List<Movie> getMovies() {
        // Use a WebUtils helper method to create a URL to the
        // GET_ALL_MOVIES endpoint of the 'database' microservice.
        var uri = WebUtils
            .buildUriString(GET_ALL_MOVIES);

        // Use WebUtils and mRestTemplate to get a List of all movies
        // from the 'database' microservice.
        var movies = WebUtils
            // Create and send a GET request to the server.
            .makeGetRequestList(mRestTemplate,
                                uri,
                                // Return type is a String array.
                                Movie[].class);

        if (movies ==null)
            throw new IllegalStateException
                ("Unable to retrieve movies from database microservice.");

        return movies;
    }

    /**
     * Search for movie titles in the database containing the given
     * query {@link String}.
     *
     * @param query The search query
     * @return A {@link List} of {@link Movie} objects that match the
     *         query
     */
    public List<Movie> searchMovies(String query) {
        // Use a WebUtils helper method to create a URL to the
        // GET_SEARCH endpoint of the 'database' microservice.
        var uri = WebUtils
            .buildUriString(GET_SEARCH + "/" + query);

        // Use WebUtils and mRestTemplate to get a List of all
        // matching movies from the 'database' microservice.
        var matchingMovies = WebUtils
            // Create and send a GET request to the server.
            .makeGetRequestList(mRestTemplate,
                                uri,
                                // Return type is a Movie array.
                                Movie[].class);

        if (matchingMovies ==null)
            throw new IllegalStateException
                ("Unable to retrieve movies from 'database' microservice.");

        return matchingMovies;
    }

    /**
     * Search for movie titles in the database containing the given
     * {@link List} of queries.
     *
     * @param queries The {@link List} queries to search for
     *                      in regular expression form
     * @return A {@link List} of {@link Movie} objects that match any
     *         of the queries
     */
    public List<Movie> searchMovies(List<String> queries) {
        // Use a WebUtils helper method to create a URL to the
        // POST_SEARCHES endpoint of the 'database' microservice.
        var uri = WebUtils
            .buildUriString(POST_SEARCHES);

        // Use WebUtils and mRestTemplate to get a List of all
        // matching movies from the 'database' microservice.
        var matchingMovies = WebUtils
            // Create and send a POST request to the server.
            .makePostRequestList(mRestTemplate,
                                 uri,
                                 queries,
                                 // Return type is a Movie array.
                                 Movie[].class);

        if (matchingMovies ==null)
            throw new IllegalStateException
                ("Unable to retrieve movies from 'database' microservice.");

        return matchingMovies;
    }

    /**
     * Search for movie titles in the database containing the given
     * {@link List} of queries.
     *
     * @param queries The {@link List} queries to search for
     *                      in regular expression form
     * @return A {@link List} of {@link Movie} objects that match any
     *         of the queries
     */
    public List<Movie> searchMoviesEx(List<String> queries) {
        // Use a WebUtils helper method to create a URL to the
        // POST_SEARCHES endpoint of the 'database' microservice.
        var uri = WebUtils
            .buildUriString(POST_SEARCHES_EX);

        // Use WebUtils and mRestTemplate to get a List of all
        // matching movies from the 'database' microservice.
        var matchingMovies = WebUtils
            // Create and send a POST request to the server.
            .makePostRequestList(mRestTemplate,
                                 uri,
                                 queries,
                                 // Return type is a Movie array.
                                 Movie[].class);

        if (matchingMovies ==null)
            throw new IllegalStateException
                ("Unable to retrieve movies from 'database' microservice.");

        return matchingMovies;
    }
}


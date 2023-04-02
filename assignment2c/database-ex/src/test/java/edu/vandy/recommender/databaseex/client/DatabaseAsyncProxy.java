package edu.vandy.recommender.databaseex.client;

import edu.vandy.recommender.common.model.Movie;
import edu.vandy.recommender.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;

import static edu.vandy.recommender.common.Constants.EndPoint.*;

/**
 * This class is a proxy to the {@code Database-Ex} microservice.
 */
@Component
public class DatabaseAsyncProxy {
    /**
     * Create an instance of the {@link WebClient} client, which is
     * then used to make HTTP requests asynchronously to the {@code
     * GatewayApplication} RESTful microservice.
     */
    @Autowired
    WebClient mWebClient;

    /**
     * Get a {@link List} containing the requested {@link Movie}
     * objects on success.
     *
     * @return A {@link Flux} that emits all the {@link Movie} objects
     *         on success
     */
    public Flux<Movie> getMovies() {
        // TODO -- you fill in here by replacing 'return null' with
        // the appropriate helper methods provided by WebUtils.

        // SOLUTION-START
        var uri = WebUtils
            .buildUriString(GET_ALL_MOVIES);

        return WebUtils
            // Use WebUtils and mWebClient to get a Flux of all movies
            // from the 'database' microservice.
            .makeGetRequestFlux(mWebClient,
                                uri,
                                Movie.class);
        // SOLUTION-END return null;
    }

    /**
     * Search for movie titles in the database containing the given
     * query {@link String} on success.
     *
     * @param query The {@link String} to search for
     * @return A {@link Flux} that emits all the {@link Movie} {@code
     *         query} on success
     */
    public Flux<Movie> searchMovies(String query) {
        // TODO -- you fill in here by replacing 'return null' with
        // the appropriate helper methods provided by WebUtils.

        // SOLUTION-START
        var uri = WebUtils
            .buildUriString(GET_SEARCH + "/" + query);

        return WebUtils
            // Use WebUtils and mWebClient to get a Flux of Movie
            // objects that match the query on the 'database'
            // microservice.
            .makeGetRequestFlux(mWebClient,
                                uri,
                                Movie.class);
        // SOLUTION-END return null;
    }

    /**
     * Search for movie titles in the database containing any given
     * {@link List} of queries on success.
     *
     * @param queries The {@link List} queries to search for
     * @return A {@link Flux} that emits the {@link Movie} objects
     *         that match any {@code queries} on success
     */
    public Flux<Movie> searchMovies(List<String> queries) {
        // TODO -- you fill in here by replacing 'return null' with
        // the appropriate helper method provided by WebUtils.

        // SOLUTION-START
        var uri = WebUtils
            .buildUriString(POST_SEARCHES);

        return WebUtils
            // Use WebUtils and mWebClient to get a Flux of all
            // matching queries from the 'database' microservice.
            .makePostRequestFlux(mWebClient,
                                 uri,
                                 queries,
                                 Movie.class);
        // SOLUTION-END return null;
    }

    /**
     * Search for movie titles in the database containing all given
     * {@link List} of queries on success.
     *
     * @param queries The {@link List} queries to search for
     * @return A {@link Flux} that emits the {@link Movie} objects
     *         that match all {@code queries} on success
     */
    public Flux<Movie> searchMoviesEx(List<String> queries) {
        // TODO -- you fill in here by replacing 'return null' with
        // the appropriate helper method provided by WebUtils.

        // SOLUTION-START
        var uri = WebUtils
            .buildUriString(POST_SEARCHES_EX);

        return WebUtils
            // Use WebUtils and mWebClient to get a Flux of all
            // matching queries from the 'database' microservice.
            .makePostRequestFlux(mWebClient,
                uri,
                queries,
                Movie.class);
        // SOLUTION-END return null;
    }
}



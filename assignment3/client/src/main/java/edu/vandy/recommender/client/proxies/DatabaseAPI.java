package edu.vandy.recommender.client.proxies;

import edu.vandy.recommender.common.model.Movie;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

import static edu.vandy.recommender.common.Constants.Params.ROUTE_NAME;
import static edu.vandy.recommender.common.Constants.EndPoint.*;

/**
 * This interface provides the contract for the RESTful {@code
 * DatabaseController} API used in conjunction with the {@code
 * GatewayApplication}.  It defines the HTTP GET and POST methods that
 * can be used to interact with the {@code ROUTE_NAMEController} API,
 * along with the expected request and response parameters for each
 * method.  However, since clients access the {@code
 * DatabaseController} API via the {@code GatewayApplication} it's
 * necessary to add a {@code "{routename}"} prefix to each URL
 * mapping, along with the corresponding {@code @Path("routename")}
 * parameter to each method.
 *
 * This interface uses Retrofit annotations that provide metadata
 * about the API, such as the type of HTTP request (i.e., {@code GET}
 * or {@code POST}), the parameter types (which are annotated with
 * {@code Path}, {@code Body}, or {@code Query} tags), and the
 * expected response format (which are all wrapped in {@link Call}
 * objects).  Retrofit uses these annotations and method signatures to
 * generate an implementation of the interface that the client uses to
 * make HTTP requests to the API.
 */
public interface DatabaseAPI {
    /**
     * Get a {@link List} containing the requested movies.
     *
     * @param routename The microservice that performs the request,
     *                  which is dynamically inserted into the URI via
     *                  the {@code Path} annotation
     * @return An {@link Call} object that yields a {@link List}
     *         containing all the {@link Movie} objects on success and
     *         an error message on failure
     */
    // TODO -- you fill in here.

    /**
     * Get a {@link Map} that associates the movie title with
     * the cosine vector for each movie.
     *
     * @param routename The microservice that performs the request,
     *                  which is dynamically inserted into the URI via
     *                  the {@code Path} annotation
     * @return An {@link Call} object that yields a {@link Map}
     *         containing all the movie titles and cosine vectors
     *         on success
     */
    // TODO -- you fill in here.

    /**
     * Get a {@link List} containing the requested {@link Movie}
     * objects.
     *
     * @param routename The microservice that performs the request,
     *                  which is dynamically inserted into the URI via
     *                  the {@code Path} annotation
     * @param query The {@link String} to search for
     * @return A {@link Call} object that yields a {@link List}
     *         containing all the {@link Movie} objects on success and
     *         an error message on failure
     */
    // TODO -- you fill in here.

    /**
     * Search for movies containing the given {@link List} of {@code
     * queries}.
     *
     * @param routename The microservice that performs the request,
     *                  which is dynamically inserted into the URI via
     *                  the {@code Path} annotation
     * @param queries The {@link List} of {@code queries} to search
     *                for, which is passed in the body of the {@code
     *                POST} request
     * @return A {@link Call} object that yields a {@link List}
     *         containing {@link Movie} objects that match any {@code
     *         queries} on success and an error message on failure
     */
    // TODO -- you fill in here.

    /**
     * Search for movies containing all given {@link List} of {@code
     * queries}.
     *
     * @param routename The microservice that performs the request,
     *                  which is dynamically inserted into the URI via
     *                  the {@code Path} annotation
     * @param queries The {@link List} of {@code queries} to search
     *                for, which is passed in the body of the {@code
     *                POST} request
     * @return A {@link Call} object that yields a {@link List}
     *         containing {@link Movie} objects that match all {@code
     *         queries} on success and an error message on failure
     */
    // TODO -- you fill in here.

    /**
     * Get a {@link List} containing the requested movies .
     *
     * This endpoint also records the execution run time of this call
     * via the {@code Timer} microservice.
     * 
     * @param routename The microservice that performs the request,
     *                  which is dynamically inserted into the URI via
     *                  the {@code Path} annotation
     * @return An {@link Call} object that yields a {@link List}
     *         containing all the {@link Movie} objects on success and
     *         an error message on failure
     */
    // TODO -- you fill in here.

    /**
     * Get a {@link Map} that associates the movie title with
     * the cosine vector for each movie.
     *
     * This endpoint also records the execution run time of this call
     * via the {@code Timer} microservice.
     *
     * @param routename The microservice that performs the request,
     *                  which is dynamically inserted into the URI via
     *                  the {@code Path} annotation
     * @return An {@link Call} object that yields a {@link Map}
     *         containing all the movie titles and cosine vectors
     *         on success
     */
    // TODO -- you fill in here.

    /**
     * Get a {@link List} containing the requested {@link Movie}
     * objects.
     *
     * This endpoint also records the execution run time of this call
     * via the {@code Timer} microservice.
     *
     * @param routename The microservice that performs the request,
     *                  which is dynamically inserted into the URI via
     *                  the {@code Path} annotation
     * @param query The {@link String} to search for
     * @return A {@link Call} object that yields a {@link List}
     *         containing {@link Movie} objects that match any {@code
     *         queries} on success and an error message on failure
     */
    // TODO -- you fill in here.

    /**
     * Search for movies containing any given {@link List} of {@code
     * queries}.
     *
     * This endpoint also records the execution run time of this call
     * via the {@code Timer} microservice.
     *
     * @param routename The microservice that performs the request,
     *                  which is dynamically inserted into the URI via
     *                  the {@code Path} annotation
     * @param queries The {@link List} of {@code queries} to search
     *                for, which is passed in the body of the {@code
     *                POST} request
     * @return A {@link Call} object that yields a {@link List}
     *         containing the {@link Movie} objects that match any
     *         {@code queries} on success and an error message on
     *         failure
     */
    // TODO -- you fill in here.

    /**
     * Search for movies containing all given {@link List} of {@code
     * queries}.
     *
     * This endpoint also records the execution run time of this call
     * via the {@code Timer} microservice.
     *
     * @param routename The microservice that performs the request,
     *                  which is dynamically inserted into the URI via
     *                  the {@code Path} annotation
     * @param queries The {@link List} of {@code queries} to search
     *                for, which is passed in the body of the {@code
     *                POST} request
     * @return A {@link Call} object that yields a {@link List}
     *         containing the {@link Movie} objects that match all
     *         {@code queries} on success and an error message on
     *         failure
     */
    // TODO -- you fill in here.
}

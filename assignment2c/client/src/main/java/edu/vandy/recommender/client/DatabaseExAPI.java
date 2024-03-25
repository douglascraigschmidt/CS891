package edu.vandy.recommender.client;

import edu.vandy.recommender.common.Movie;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Flux;

import java.util.List;

import static edu.vandy.recommender.client.Constants.DATABASE_EX;
import static edu.vandy.recommender.common.Constants.EndPoint.*;

/**
 * This interface provides the contract for the RESTful {@code
 * DatabaseController} API used in conjunction with the {@code
 * GatewayApplication}.  It defines the HTTP GET and POST methods that
 * can be used to interact with the {@code DatabaseController} API,
 * along with the expected request and response parameters for each
 * method.  However, since clients access the {@code
 * DatabaseController} API via the {@code GatewayApplication} it's
 * necessary to add a {@code "{routename}"} prefix to each URL
 * mapping, along with the corresponding {@code @Path("routename")}
 * parameter to each method.
 *
 * This interface uses Spring HTTP interface annotations that provide
 * metadata about the API, such as the type of HTTP request (i.e.,
 * {@code GET} or {@code POST}), the parameter types (which are
 * annotated with {@code GetExchange}, {@code PostExchange},
 * {@code @RequestPath}, {@code RequestBody}, or {@code RequestParam}
 * tags), and the expected response format.  HTTP interface uses these
 * annotations and method signatures to generate an implementation of
 * the interface that the client uses to make HTTP requests to the
 * API.
 */
@HttpExchange(DATABASE_EX + "/")
public interface DatabaseExAPI {
    /**
     * Get a {@link Flux} containing the requested movies.
     *
     * @return An {@link Flux} that emits all {@link Movie} objects on
     *         success and an error message on failure
     */
    // TODO -- you fill in here.

    /**
     * Get a {@link Flux} containing the requested {@link Movie}
     * objects.
     *
     * @param query The {@link String} to search for
     * @return A {@link Flux} that emits all {@link Movie} objects on
     * success and an error message on failure
     */
    // TODO -- you fill in here.

    /**
     * Search for movies containing the given {@link List} of {@code
     * queries}.
     *
     * @param queries   The {@link List} of {@code queries} to search
     *                  for, which is passed in the body of the {@code
     *                  POST} request
     * @return A {@link Flux} that emits {@link Movie} objects that
     * match any {@code queries} on success and an error
     * message on failure
     */
    // TODO -- you fill in here.

    /**
     * Search for movies containing all given {@link List} of {@code
     * queries}.
     *
     * @param queries   The {@link List} of {@code queries} to search
     *                  for, which is passed in the body of the {@code
     *                  POST} request
     * @return A {@link Flux} that emits {@link Movie} objects that
     * match all {@code queries} on success and an error
     * message on failure
     */
    // TODO -- you fill in here.

    /**
     * Get a {@link Flux} containing the requested movies .
     * <p>
     * This endpoint also records the execution run time of this call
     * via the {@code Timer} microservice.
     *
     * @return An {@link Flux} that emits {@link Movie} objects on
     * success and an error message on failure
     */
    // TODO -- you fill in here.

    /**
     * Get a {@link Flux} containing the requested {@link Movie}
     * objects.
     * <p>
     * This endpoint also records the execution run time of this call
     * via the {@code Timer} microservice.
     *
     * @param query     The {@link String} to search for
     * @return A {@link Flux} that emits {@link Movie} objects that
     * match any {@code queries} on success and an error
     * message on failure
     */
    // TODO -- you fill in here.

    /**
     * Search for movies containing any given {@link List} of {@code
     * queries}.
     * <p>
     * This endpoint also records the execution run time of this call
     * via the {@code Timer} microservice.
     *
     * @param queries   The {@link List} of {@code queries} to search
     *                  for, which is passed in the body of the {@code
     *                  POST} request
     * @return A {@link Flux} emits the {@link Movie} objects that
     * match any {@code queries} on success and an error
     * message on failure
     */
    // TODO -- you fill in here.

    /**
     * Search for movies containing all given {@link List} of {@code
     * queries}.
     * <p>
     * This endpoint also records the execution run time of this call
     * via the {@code Timer} microservice.
     *
     * @param queries   The {@link List} of {@code queries} to search
     *                  for, which is passed in the body of the {@code
     *                  POST} request
     * @return A {@link Flux} that emits {@link Movie} objects that
     * match all {@code queries} on success and an error
     * message on failure
     */
    // TODO -- you fill in here.
}

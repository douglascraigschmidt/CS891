package edu.vandy.recommender.client;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import static edu.vandy.recommender.common.Constants.EndPoint.GET_TIMINGS;

/**
 * This interface provides the contract for the RESTful {@code
 * TimerController} API used in conjunction with the {@code
 * GatewayApplication}.  It defines the HTTP GET methods that can be
 * used to interact with the {@code TimerController} API, along with
 * the expected request and response parameters for each method.
 * However, since clients access the {@code DatabaseController} API
 * via the {@code GatewayApplication} it's necessary to add a {@code
 * "{routename}"} prefix to each URL mapping, along with the
 * corresponding {@code @Path("routename")} parameter to each method.
 *
 * This interface uses Retrofit annotations that provide metadata
 * about the API, such as the type of HTTP request (i.e., {@code GET}
 * or {@code PUT}), the parameter types (which are annotated with
 * {@code Path}, {@code Body}, or {@code Query} tags), and the
 * expected response format (which are all wrapped in {@link Call}
 * objects).  Retrofit uses these annotations and method signatures to
 * generate an implementation of the interface that the client uses to
 * make HTTP requests to the API.
 */
public interface TimerAPI {
    /**
     * Get a {@link String} containing the recorded timing summaries.
     *
     * @param routename The microservice that performs the request,
     *                  which is dynamically inserted into the URI via
     *                  the {@code Path} annotation
     * @return An {@link Call} object that yields a {@link String}
     *         containing all the recorded timing summaries on success
     *         and an error message on failure
     */
    // TODO -- you fill in here.
}

package edu.vandy.recommender.common;

import edu.vandy.recommender.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

import static edu.vandy.recommender.common.Constants.*;
import static edu.vandy.recommender.common.Constants.EndPoint.GET_TIMINGS;
import static edu.vandy.recommender.common.Constants.EndPoint.POST_TIMING;
import static edu.vandy.recommender.common.Constants.Service.TIMER;

/**
 * This class provides asynchronous and synchronous computation of
 * method execution times.
 */
public class RunTimer {
    /**
     * The {@link RestTemplate} used to communicate with
     * the {@code Timer} microservice.
     */
    private final RestTemplate mRestTemplate;

    /**
     * The constructor initializes the {@link RestTemplate}.
     */
    public RunTimer(RestTemplate mRestTemplate) {
        this.mRestTemplate = mRestTemplate;
    }

    /**
     * Builds a {@link String} url to post the specified {@code
     * request} to the 'timer' microservice.
     *
     * @param request A request endpoint {@link String}
     * @return A url {@link String}
     */
    private static String makeTimerUrl(String request) {
        // Create a URL that can be used to send GET and POST
        // requests to the 'timer' microservice.
        return UriComponentsBuilder
            // Make a new UriComponentsBuilder.
            .newInstance()
            
            // Use the HTTP protocol.
            .scheme(HTTP_PROTOCOL)

            // Connect to the Gateway.
            .port(GATEWAY_PORT)

            // Connect to the "localhost".
            .host(LOCAL_HOST)

            // Make a path to the TIMER service.
            .path("/"
                  + TIMER
                  + "/"
                  + request)

            // Build the URI as a URI String.
            .build()
            .toUriString();
    }

    /**
     * Call {@code supplier.get()} and time how long it takes to
     * run. This method supports synchronous timings.
     *
     * @param identifier A {@link String} identifying what is being
     *                   timed
     * @param supplier The {@link Supplier} containing the code to run
     * @return The result returned by {@code supplier.get()}
     */
    public <U> U runAndRecordTime(String identifier,
                                  Supplier<U> supplier) {
        // Create a Timer instance and set its start time.
        Timer timer = start(new Timer(identifier));

        // Run the code that's being timed.
        U result = supplier.get();

        // Stop the timer and post the result to the timer service.
        stopAndPost(timer);

        // Return the result from the code that was being timed.
        return result;
    }

    /**
     * Call {@code supplier.get()} and time how long it takes to
     * run. This method supports synchronous timings.
     *
     * @param identifier A {@link String} identifying what is being
     *                   timed
     * @param runnable   The {@link Runnable} containing the code
     *                   to run
     */
    public void runAndRecordTime(String identifier,
                                 Runnable runnable) {
        // Create a TimerInfo instance and set its start time.
        Timer timer = start(new Timer(identifier));

        // Run the code that's being timed.
        runnable.run();

        // Stop the timer and post the result to the timer service.
        stopAndPost(timer);
    }

    /**
     * Adds a run timer to the passed {@code Flux} and post the
     * elapsed execution time to the timer service when the Flux
     * completes.
     *
     * @param identifier A {@link String} identifying what is being
     *                   timed
     * @param flux       The {@link Flux} to run and record
     */
    public <T> Flux<T> runAndRecordTime(String identifier,
                                        Flux<T> flux) {
        return Mono
            // Start with a supplier that will create a Timer
            // instance.
            .fromSupplier(// Generate a new started Timer instance.
                          () -> start(new Timer(identifier)))

            // Map the Timer instance to the output of the pass flux.
            .flatMapMany(timer ->
                         // Lastly, stop the timer and post the timing
                         // result to the timer service.
                         flux.doFinally(result -> stopAndPost(timer)));
    }

    /**
     * @return A {@link String} containing the timing results for all
     * timings ordered from fastest to slowest
     */
    public String getTimings() {
        return WebUtils
            // Make an HTTP GET call to the server passing in the URL
            // containing the GET_TIMINGS request and returning a
            // result of type String.
            .makeGetRequest(mRestTemplate,
                            makeTimerUrl(GET_TIMINGS),
                            String.class);
    }

    /**
     * Starts this {@link Timer} instance and return it.
     *
     * @return A started {@link Timer} instance
     */
    public Timer start(Timer timer) {
        timer.startTime = System.nanoTime();
        return timer;
    }

    /**
     * Stops this {@link Timer} instance and return it.
     *
     * @return A stopped {@link Timer} instance
     */
    public Timer stop(Timer timer) {
        timer.stopTime = System.nanoTime();
        return timer;
    }

    /**
     * Stops this {@link Timer} instance and also posts the result to
     * the 'timer' service.
     */
    public void stopAndPost(Timer timer) {
        // Make a URL for the Timer microservice.
        String url = makeTimerUrl(POST_TIMING);

        System.out.println("Posting to Timer service at URL = " 
                           + url);

        var uri = WebUtils
            // Sent a POST request to the 'timer' microservice.
            .makePostRequestLocation(mRestTemplate,
                                     url,
                                     stop(timer));

        System.out.println("The Timer service returned this URI "
                           + uri);
    }
}

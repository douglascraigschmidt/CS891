package edu.vandy.recommender.database.common;

import edu.vandy.recommender.database.common.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.function.Supplier;

import static edu.vandy.recommender.database.common.Constants.*;
import static edu.vandy.recommender.database.common.Constants.EndPoint.GET_TIMINGS;
import static edu.vandy.recommender.database.common.Constants.EndPoint.POST_TIMING;

import edu.vandy.recommender.database.utils.WebUtils;

/**
 * This class provides asynchronous and synchronous computation of
 * method execution times.
 */
@Component
public class RunTimer {
    /**
     * The central interface to provide configuration for the
     * application.  This field is read-only while the application is
     * running.
     */
    @Autowired
    ApplicationContext mApplicationContext;

    /**
     * Spring injects the field, which makes synchronous calls to the
     * 'timer' microservice.
     */
    @Autowired
    private RestTemplate mRestTemplate;

    /**
     * Builds a {@link String} url to post the specified {@code
     * request} to the 'timer' microservice.
     *
     * @param request A request endpoint {@link String}
     * @return A url {@link String}
     */
    private static String makeTimerUrl(String request) {
        // Create a URL that can be used to send GET and POST requests
        // to the 'timer' microservice.
        return UriComponentsBuilder
            .newInstance()
            .scheme(PROTOCOL)
            .port(GATEWAY_PORT)
            .host(HOST)
            .path("/"
                  + Constants.Service.TIMER
                  + "/"
                  + request)
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
        Timer timer = new Timer(identifier).start();

        // Run the code that's being timed.
        U result = supplier.get();

        // Stop the timer and post the result to the timer service.
        timer.stopAndPost();

        // Return the result from the code that was being timed.
        return result;
    }

    /**
     * Call {@code supplier.get()} and time how long it takes to run. This
     * method supports synchronous timings.
     *
     * @param identifier A {@link String} identifying what is being
     *                   timed
     * @param runnable   The {@link Runnable} containing the code
     *                   to run
     */
    public void runAndRecordTime(String identifier,
                                 Runnable runnable) {
        // Create a TimerInfo instance and set its start time.
        Timer timer = new Timer(identifier).start();

        // Run the code that's being timed.
        runnable.run();

        // Stop the timer and post the result to the timer service.
        timer.stopAndPost();
    }

    /**
     * Adds a run timer to the passed {@code Flux} and post the elapsed
     * execution time to the timer service when the Flux completes.
     *
     * @param identifier A {@link String} identifying what is being timed
     * @param flux       The {@link Flux} to run and record
     */
    public <T> Flux<T> runAndRecordTime(String identifier,
                                        Flux<T> flux) {
        return Mono
            // Start with a supplier that will create a Timer instance.
            .fromSupplier(// Generate a new started Timer instance.
                          () -> new Timer(identifier).start())

            // Map the Timer instance to the output of the pass flux.
            .flatMapMany(timer ->
                         // Lastly, stop the timer and post the timing
                         // result to the timer service.
                         flux.doFinally(result -> timer.stopAndPost()));
    }

    /**
     * @return A {@link String} containing the timing results for all
     *         timings ordered from fastest to slowest
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
     * A Data Transfer Object (DTO) used when communicating with the
     * timer service. This static class must be cloned and used in the
     * Timer service.
     */
    private class Timer {
        /**
         * Id of the request being timed.
         */
        public final String id;

        /**
         * Time when the request processing was initiated.
         */
        public long startTime;

        /**
         * Time when the request processing was finished.
         */
        public long stopTime;

        /**
         * Constructs a {@link Timer} object and automatically sets
         * its globally unique invocation id. Both start and stop
         * times can only be set by explicitly calling the start() and
         * stop() methods.
         *
         * @param id A {@link String} identifying what is being
         *           timed
         */
        public Timer(String id) {
            this.id = mApplicationContext.getId() + ":" + id;

            // Start and stop times must be set explicitly.
            startTime = 0L;
            stopTime = 0L;
        }

        /**
         * Starts this {@link Timer} instance and return it.
         *
         * @return This {@link Timer} instance
         */
        public Timer start() {
            startTime = System.nanoTime();
            return this;
        }

        /**
         * Stops this {@link Timer} instance and return it.
         *
         * @return This {@link Timer} instance
         */
        public Timer stop() {
            stopTime = System.nanoTime();
            return this;
        }

        /**
         * Stops this {@link Timer} instance and also posts the
         * result to the 'timer' service.
         */
        public void stopAndPost() {
            // Make a URL for the Timer microservice.
            String url = makeTimerUrl(POST_TIMING);

            System.out.println("Posting to Timer service at URL = " + url);

            URI uri = WebUtils
                // Sent a POST request to the 'timer' microservice.
                .makePostRequestLocation(mRestTemplate,
                                         url,
                                         stop());

            System.out.println("Timer service returned URI for this microservice = "
                               + uri);
        }
    }
}

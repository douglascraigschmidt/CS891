package edu.vandy.recommender.client;

import edu.vandy.recommender.client.proxies.TimerSyncProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * This client uses Spring WebMVC features to perform synchronous
 * remote method invocations on the {@code GatewayApplication} and
 * various microservices that it encapsulates.
 *
 * The {@code @Component} annotation allows Spring to automatically
 * detect custom beans, i.e., Spring will scan the application for
 * classes annotated with {@code @Component}, instantiate them, and
 * inject the specified dependencies into them without having to write
 * any explicit code.
 *
 * The {@code ComponentScan} annotation instructs Spring to scan for
 * components that are within the "edu.vandy.recommender" package.
 */
@Component
@ComponentScan("edu.vandy.recommender")
public class RecommenderSyncClient {
    /**
     * This auto-wired field contains all the movies.
     */
    @Autowired
    Map<String, List<Double>> mMovies;

    /**
     * This auto-wired field connects the {@link RecommenderAsyncClient} to
     * the {@link TimerSyncProxy} that performs HTTP requests
     * synchronously.
     */
    @Autowired
    private TimerSyncProxy mTimerSyncProxy;

    /**
     * Run the synchronous tests.
     */
    public void runTests(String strategy, boolean timed) {
        // This implementation intentionally left blank.
    }

    /**
     * Print the timing results.
     */
    public void printTestResults() {
        var timings = mTimerSyncProxy
            .getTimings();

        System.out.println("The timing results are:");
        System.out.println(timings);
    }
}

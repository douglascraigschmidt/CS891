package edu.vandy.recommender.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static edu.vandy.recommender.common.Constants.Service.TIMER;
import static edu.vandy.recommender.utils.ExceptionUtils.rethrowSupplier;

/**
 * This class is a proxy to the {@code Timer} microservice.
 */
@Component
public class TimerSyncProxy {
    /**
     * Create an instance of the {@link TimerAPI} Retrofit client,
     * which is then used to making HTTP requests to the {@code
     * GatewayApplication} RESTful microservice.
     */
    @Autowired
    TimerAPI mTimerAPI;

    /**
     * @return A {@link String} containing the timing results for all
     * timings ordered from fastest to slowest
     */
    public String getTimings() {
        // TODO -- you fill in here.

        return null;
    }
}


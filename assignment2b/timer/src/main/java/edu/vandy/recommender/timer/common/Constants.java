package edu.vandy.recommender.timer.common;

/**
 *
 */
public class Constants {
    /**
     * All supported HTTP request endpoints.
     */
    public static class EndPoint {
        public static final String POST_TIMING = "postTiming";
        public static final String CLEAR_TIMINGS = "clearTimings";
        public static final String GET_TIMINGS = "getTimings";
    }

     /**
      * List of microservices that are directly accessed via RestTemplate.
     */
    public static class Service {
        public static final String TIMER = "timer";
    }
}

package edu.vandy.recommender.database.common;

/**
 * This class centralizes all constants used by the movie recommender
 * microservices.
 */
public class Constants {
    public static final String DATABASE_BASE_URL = "http://localhost:9000";
    public static final int GATEWAY_PORT = 8080;
    public static final String HOST = "localhost";
    public static final String PROTOCOL = "http";

    /**
     * All supported HTTP request endpoints.
     */
    public static class EndPoint {
        public static final String TIMED = "timed";
        public static final String GET_ALL_MOVIES = "allMovies";
        public static final String GET_SEARCH = "search";
        public static final String GET_SEARCHES = "searches";
        public static final String SEARCH_QUERY = "/{query}";

        public static final String POST_TIMING = "postTiming";
        public static final String GET_TIMINGS = "getTimings";
    }

    /**
     * List of microservices that are directly accessed via RestTemplate.
     */
    public static class Service {
        public static final String TIMER = "timer";
    }
}

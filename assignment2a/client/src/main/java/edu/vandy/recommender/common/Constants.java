package edu.vandy.recommender.common;

/**
 * This class centralizes all constants used by the movie recommender
 * microservices.
 */
public class Constants {
    public static final int GATEWAY_PORT = 8080;

    public static final String GATEWAY_BASE_URL = "http://localhost:" + GATEWAY_PORT;

    /**
     * All supported HTTP request endpoints.
     */
    public static class EndPoint {
        public static final String TIMED = "timed";
        public static final String GET_ALL_MOVIES = "allMovies";
        public static final String GET_SEARCH = "search";
        public static final String SEARCH_QUERY = "/{query}";
        public static final String POST_SEARCHES = "searches";
        public static final String GET_RECOMMEND = "recommend";
        public static final String POST_RECOMMEND_MANY = "recommendMany";

        public static final String POST_TIMING = "postTiming";
        public static final String GET_TIMINGS = "getTimings";

        /**
         * Supported HTTP request parameters identifiers.
         */
        public static class Params {
            public static final String WATCHED_MOVIE_PARAM = "watchedMovie";
            public static final String MAX_COUNT_PARAM = "maxCount";
        }
    }

    /**
     * List of microservices that are directly accessed via RestTemplate.
     */
    public static class Service {
        public static final String TIMER = "timer";
        public static final String DATABASE = "database";
        public static final String MOVIES = "movies";
        public static final String GATEWAY = "gateway";
    }
}

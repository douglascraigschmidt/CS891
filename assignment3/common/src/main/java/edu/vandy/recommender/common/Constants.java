package edu.vandy.recommender.common;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * This class centralizes all constants used by the movie recommender
 * microservices.
 */
public class Constants {
    public static final int GATEWAY_PORT = 8080;
    public static final String DATABASE_BASE_URL = "http://localhost:" + GATEWAY_PORT;
    public static final String GATEWAY_BASE_URL = "http://localhost:" + GATEWAY_PORT;
    public static final String MOVIES_BASE_URL = "http://localhost:" + GATEWAY_PORT;

    public static final String LOCAL_HOST = "localhost";
    public static final String HTTP_PROTOCOL = "http";

    /**
     * All supported HTTP request endpoints.
     */
    public static class EndPoint {
        public static final String TIMED = "timed";

        /*
         * Database[ex] microservice endpoints.
         */
        public static final String GET_ALL_MOVIES = "allMovies";
        public static final String GET_MOVIES_MAP = "moviesMap";
        public static final String GET_SEARCH = "search";
        public static final String POST_SEARCHES = "searches";
        public static final String GET_SEARCHES = "searches";
        public static final String POST_SEARCHES_EX = "searchesEx";
        public static final String SEARCH_QUERY = "{query}";
        public static final String PATH_QUERY = "query";

        /*
        * Recommender microservice endponts.
         */
        public static final String GET_RECOMMENDATION = "getRecommendation";
        public static final String POST_RECOMMENDATIONS = "postRecommendations";

        /*
        * Timer microservice endpoints.
         */
        public static final String POST_TIMING = "postTiming";
        public static final String CLEAR_TIMINGS = "clearTimings";
        public static final String GET_TIMINGS = "getTimings";
    }

    /**
     * List of microservices that are directly accessed via
     * {@link RestTemplate} or {@link WebClient}.
     */
    public static class Service {
        public static final String TIMER = "timer";
        public static final String DATABASE = "database";
        public static final String MOVIES = "movies";
        public static final String DATABASE_EX = "databaseex";
        public static final String PARALLEL_FLUX = "parallelflux";
    }

    /**
     * Parameters that are sent in GET or POST requests.
     */
    public static class Params {
        public static final String QUERIES_PARAM = "queries";
        public static final String WATCHED_MOVIE_PARAM = "watchedMovie";
        public static final String MAX_COUNT_PARAM = "maxCount";
        public static final String MOVIES_CACHE = "moviesCache";
        public static final String ROUTE_NAME = "{routename}";
    }
}

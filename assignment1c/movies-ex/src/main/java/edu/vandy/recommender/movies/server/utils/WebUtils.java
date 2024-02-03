package edu.vandy.recommender.movies.server.utils;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A Java utility class that provides helper methods for dealing with
 * Spring web programming.
 */
public final class WebUtils {
    /**
     * A Java utility class should have a private constructor.
     */
    private WebUtils() {}

    /**
     * Decode the {@code queries {@link List} so it can be received
     * correctly from HTTP.
     *
     * @param queries The {@link List} to decode
     * @return The encoded {@link List}
     */
    public static List<String> decodeQueries(List<String> queries) {
        return queries
            // Convert List to a Stream.
            .stream()

            // Encode each String in the Stream.
            .map(query -> URLDecoder
                 .decode(query,
                         StandardCharsets.UTF_8))

            // Convert the Stream back to a List.
            .toList();
    }

    /**
     * Decode the {@code query} {@link String} so it can be
     * received correctly from HTTP.
     *
     * @param query The {@link String} to decode
     * @return The encoded {@link String}
     */
    public static String decodeQuery(String query) {
        return URLDecoder
            // Decode the query so it can be received correctly from
            // HTTP.
            .decode(query,
                    StandardCharsets.UTF_8);
    }
}

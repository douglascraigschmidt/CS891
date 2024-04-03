package edu.vandy.recommender.common;

import edu.vandy.recommender.common.model.Movie;
import edu.vandy.recommender.utils.WebUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static edu.vandy.recommender.common.Constants.EndPoint.GET_MOVIES_MAP;
import static edu.vandy.recommender.common.Constants.GATEWAY_PORT;
import static edu.vandy.recommender.common.Constants.LOCAL_HOST;
import static edu.vandy.recommender.common.Constants.Service.DATABASE;
import static java.util.stream.Collectors.toMap;

/**
 * Loads the {@link Movie} titles and cosine vectors from various
 * locations (e.g., a file in the resources folder or the database
 * microservice).
 */
@Component
public class MoviesLoader {
    /**
     * Separator used in each dataset line entry.
     */
    private static final String SPLITTER = " ";

    /**
     * This {@link RestTemplate} is used to connect to the database
     * microservice.
     */
    private static final RestTemplate sRestTemplate =
        new RestTemplate();

    /**
     * Load the movie titles and vectors from the database
     * microservice.
     *
     * @return A {@link Map} that associates the movie title with
     * the cosine vector for each movie
     */
    public static Map<String, List<Double>> loadMoviesFromDatabase() {
        // Use a helper method from WebUtils to get a URL string
        // to database microservice URL accessed via the API
        // gateway on the localhost at the GET_ALL_MOVIES path.
        // TODO -- you fill in here, replacing 'String url = null' with
        // the proper code.
        String url = null;

        // Use a helper method from WebUtils to get the List of all
        // Movie objects from the database microservice.
        // TODO -- you fill in here, replacing 'Map<String,
        // List<Double>> movieMap = null' with the proper code.
        Map<String, List<Double>> movieMap = null;

        if (movieMap == null) {
            throw new IllegalStateException
                ("Unable to retrieve movies from database microservice.");
        }

        System.out.println("Successfully loaded "
                           + movieMap.size()
                           + " movies from the database microservice at "
                           + url);

        // Return the Map.
        return movieMap;
    }

    /**
     * Load the movie titles and vectors from a file in the resources
     * folder.
     *
     * @param dataset The pathname where the dataset is located
     * @return A {@link TreeMap} that associates the movie title with
     * the cosine vector for each movie
     */
    public static TreeMap<String, List<Double>> loadMoviesFromResources
        (final String dataset) {
        // Create an InputStream from the file specified by the
        // `dataset` variable.
        try (InputStream is = MoviesLoader.class
             .getResourceAsStream("/" + dataset)) {

            // Create an InputStreamReader from the InputStream.
            InputStreamReader isr =
                new InputStreamReader(Objects.requireNonNull(is));

            // Create a BufferedReader from the InputStreamReader.
            BufferedReader br = new BufferedReader(isr);

            // Use the loadCSVFile() method to load the CSV data from
            // the BufferedReader and convert it into a Map data
            // structure.
            var map = loadCSVFile(br);

            System.out.println("Successfully loaded "
                               + map.size()
                               + " movies from the resources folder.");

            // Return the TreeMap data structure containing the loaded
            // movie data.
            return map;
        } catch (Exception e) {
            System.out.println("ERROR: resource load failed: "
                               + e);
            // Return an empty TreeMap.
            return new TreeMap<>();
        }
    }

    /**
     * Factory method that builds a cosine vector {@link Map} from a
     * CSV file containing the cosine values for all the movies.
     *
     * @param reader A {@link BufferedReader}.
     * @return A {@link TreeMap} that associates the movie title with
     * the cosine vector for each movie
     */
    private static TreeMap<String, List<Double>> loadCSVFile
        (BufferedReader reader) {
        // Read all lines from filename and convert into a Stream of
        // Strings.
        Stream<String> lines = reader.lines();

        return lines
            // Consume the first line, which gives the format of the
            // CSV file.
            .skip(1)

            // Divide the title from the cosine vector.
            .map(line -> line.split(";"))

            // Put the title and the associated cosine vector in map.
            .map(strings -> new SimpleImmutableEntry<>
                 (strings[0], parseVector(strings[1])))

            // Trigger intermediate processing and collect the results
            // into a Map.
            .collect(toMap(SimpleImmutableEntry::getKey,
                           SimpleImmutableEntry::getValue,
                           (x, y) -> x,
                           TreeMap::new));
    }

    /**
     * Convert the {@link String} representation of movie cosine
     * values into a {@link List<Double>}.
     *
     * @param movieValues The {@link String} vector of cosine values
     *                    representing a movie
     * @return A {@link List<Double>} containing the movie cosine
     * values
     */
    private static List<Double> parseVector(String movieValues) {
        // Access the vector stored in String form.
        return Pattern
            // Compile splitter into a regular expression (regex).
            .compile(SPLITTER)

            // Use regex to split the vector into a stream of strings.
            .splitAsStream(movieValues
                           // Remove brackets and spaces.
                           .substring(3, movieValues.length() - 2))

            // Make the stream a parallel stream.
            .parallel()

            // Convert each cosine value from String to Double.
            .map(Double::valueOf)

            // Collect the Stream<Double> into an List<Double>.
            .toList();
    }
}

package edu.vandy.recommender.database.common;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * Constructs a {@link Map} that loads the cosine vector map from
 * resources.
 */
@Component
public class MoviesDatasetReader {
    /**
     * Character used to split each movie property vector entry.
     */
    private static final String SPLITTER = " ";

    /**
     * Character used to separate a movie title from
     * the movie property vector.
     */
    private static final String SEPARATOR = ";";

    /**
     * Load the {@code dataset} and return a {@link Map} of {@link
     * String} and {@link List<Double>} objects.
     *
     * @param dataset The name of the dataset containing movie-related
     *                data
     * @return A {@link Map} that associates each movie title with the
     *         movie property vector for the movie
     */
    public static Map<String, List<Double>> loadMovieData(final String dataset) {
        try (InputStream is = MoviesDatasetReader.class
             .getResourceAsStream("/" + dataset)) {
            InputStreamReader isr =
                new InputStreamReader(Objects.requireNonNull(is));
            BufferedReader br = new BufferedReader(isr);
            var map = loadCSVFile(br);

            System.out.println("DATABASE: successfully loaded " + map.size() + " vectors.");
            return map;
        } catch (Exception e) {
            System.out.println("ERROR: Database load failed: " + e);
            return Map.of();
        }
    }

    /**
     * Factory method that builds a {@link Map} from a CSV file
     * containing the movie property values for all the movies.
     *
     * @param reader A {@link BufferedReader}.
     * @return A {@link Map} that associates the movie title with the
     * movie property vector for each movie
     */
    private static Map<String, List<Double>> loadCSVFile(BufferedReader reader) {
        // Read all lines from filename and convert into a Stream of
        // Strings.
        try (Stream<String> lines = reader.lines()) {
            return lines
                // Convert the stream to a parallel stream.
                .parallel()

                // Consume the first line, which gives the format of
                // the CSV file.
                .skip(1)

                // Divide the title from the cosine vector.
                .map(line -> line
                     .split(SEPARATOR))

                // Put the title and the associated cosine vector in
                // the map.
                .map(strings -> new SimpleImmutableEntry<>
                     (strings[0], parseVector(strings[1])))

                // Trigger intermediate processing and collect the
                // results into a Map.
                .collect(toMap(SimpleImmutableEntry::getKey,
                               SimpleImmutableEntry::getValue,
                               (x, y) -> x));
        } catch (Exception e) {
            // There is no point in continuing if the underlying
            // database has loading issues.
            System.out.println("Exception Occurred: " + e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert the {@link String} representation of movie cosine
     * values into a {@link List<Double>}.
     *
     * @param movieValues The {@link String} vector of cosine values
     *                    representing a movie
     * @return A {@link List<Double>} containing the movie cosine values
     */
    private static List<Double> parseVector(String movieValues) {
        // Access the vector that's stored in String form.
        return Pattern
            // Compile splitter into a regular expression (regex).
            .compile(SPLITTER)

            // Use the regex to split the vector into a stream of
            // strings.
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

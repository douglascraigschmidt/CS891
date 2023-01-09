package edu.vandy.recommender.movies;

import edu.vandy.recommender.movies.model.Movie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.List;
import java.util.Map;

import static edu.vandy.recommender.movies.Constants.MOVIES_BASE_URL;

/**
 * This class contains a {@code Bean} annotation that can be injected
 * into classes using the Spring {@code @Autowired} annotation.
 */
@Configuration
@PropertySource("classpath:application.yml")
public class Components {
    /**
     * Loads the {@code dataset} and returns a {@link Map} of {@link
     * String} and {@link List<Double>} objects.
     *
     * @param dataset The name of the dataset containing movie-related
     *                data
     * @return A {@link Map} of {@link String} and {@link List<Double>} objects
     */
    // TODO -- Add the appropriate annotation to make this factory
    // method a "Bean".
    public Map<String, List<Double>> movieMap
    // The @Value annotation injects values into fields in
    // Spring-managed beans.
    (@Value("${app.dataset}") final String dataset) {
        return MovieDatasetReader
            .loadMovieData(dataset);
    }

    /**
     * Loads the {@code dataset} and returns a {@link List} of {@link
     * Movie} objects.
     *
     * @param dataset The name of the dataset containing movie-related
     *                data
     * @return A {@link List} of {@link Movie} objects
     */
    // TODO -- Add the appropriate annotation to make this factory
    // method a "Bean".
    public List<Movie> movieList
        // The @Value annotation injects values into fields in
        // Spring-managed beans.
        (@Value("${app.dataset}") final String dataset) {
        // Use the MovieDatasetReader to create a Map that associates
        // the movie title with the cosine vector for each movie and
        // then convert this into a List of Movie objects and return
        // this List.

        // TODO -- You fill in here, replacing 'return null' with the
        // proper code.

        return null;
    }

    /**
     * @return A new {@link RestTemplate} that knows how to connect to
     * the 'movies' microservice.
     */
    // TODO -- Add the appropriate annotation to make this factory
    // method a "Bean".
    public RestTemplate getMoviesRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        restTemplate
            // Set the base URL for the RestTemplate.
            .setUriTemplateHandler
            (new DefaultUriBuilderFactory(MOVIES_BASE_URL));

        // Return restTemplate.
        return restTemplate;
    }
}

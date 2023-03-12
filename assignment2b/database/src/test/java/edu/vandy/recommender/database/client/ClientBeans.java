package edu.vandy.recommender.database.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.List;
import java.util.Map;

import static edu.vandy.recommender.database.common.Constants.DATABASE_BASE_URL;
import static edu.vandy.recommender.database.common.Constants.Service.DATABASE;

/**
 * This class contains a {@code Bean} annotation that can be injected
 * into classes using the Spring {@code @Autowired} annotation.
 */
@Configuration
public class ClientBeans {
    /**
     * @return A new {@link RestTemplate} that knows how to connect to
     * the 'movies' microservice.
     */
    // TODO -- Add the appropriate annotation to make this factory
    // method a "Bean".
    // SOLUTION-START
    @Bean
    // SOLUTION-END
    @Qualifier(DATABASE)
    public RestTemplate getMoviesRestTemplate() {
        var restTemplate = new RestTemplate();

        restTemplate
            // Set the base URL for the RestTemplate.
            .setUriTemplateHandler
                 (new DefaultUriBuilderFactory(DATABASE_BASE_URL));

        // Return restTemplate.
        return restTemplate;
    }

    /**
     * Loads the {@code dataset} and returns a {@link Map} of {@link
     * String} and {@link List <Double>} objects.
     *
     * @param dataset The name of the dataset containing movie-related
     *                data
     * @return A {@link Map} of {@link String} and {@link List<Double>} objects
     */
    // TODO -- Add the appropriate annotation to make this factory
    // method a "Bean".
    // SOLUTION-START
    @Bean
    // SOLUTION-END
    public Map<String, List<Double>> movieMap
    // The @Value annotation injects values into fields in
    // Spring-managed beans.
    (@Value("${app.dataset}") final String dataset) {
        return MoviesDatasetReader
            .loadMovieData(dataset);
    }
}

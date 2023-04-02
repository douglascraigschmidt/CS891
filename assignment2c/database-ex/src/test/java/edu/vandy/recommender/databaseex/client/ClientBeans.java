package edu.vandy.recommender.databaseex.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.List;
import java.util.Map;

import static edu.vandy.recommender.common.Constants.DATABASE_BASE_URL;
import static edu.vandy.recommender.common.Constants.Service.DATABASE;

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
    @Bean
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
     * @return A new {@link WebClient}.
     */
    @Bean
    public WebClient getWebClient() {
        return WebClient
            // Create a new WebClient.
            .builder()

            // Add the base URL for the 'databaseex' microservice.
            .baseUrl(DATABASE_BASE_URL)

            // Finish initializing the WebClient.
            .build();
    }

    /**
     * Loads the {@code dataset} and returns a {@link Map} of {@link
     * String} and {@link List <Double>} objects.
     *
     * @param dataset The name of the dataset containing movie-related
     *                data
     * @return A {@link Map} of {@link String} and {@link List<Double>} objects
     */
    @Bean
    public Map<String, List<Double>> movieMap
    // The @Value annotation injects values into fields in
    // Spring-managed beans.
    (@Value("${app.dataset}") final String dataset) {
        System.out.println("dataset name = " + dataset);
        var results = MoviesDatasetReader
            .loadMovieData(dataset);
        System.out.println("Loaded "
                           + results.size()
                           + " movies");
        return results;
    }
}

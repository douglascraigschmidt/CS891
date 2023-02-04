package edu.vandy.recommender.movies.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import static edu.vandy.recommender.movies.common.Constants.MOVIES_BASE_URL;

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
    public RestTemplate getMoviesRestTemplate() {
        var restTemplate = new RestTemplate();

        restTemplate
                // Set the base URL for the RestTemplate.
                .setUriTemplateHandler
                        (new DefaultUriBuilderFactory(MOVIES_BASE_URL));

        // Return restTemplate.
        return restTemplate;
    }
}

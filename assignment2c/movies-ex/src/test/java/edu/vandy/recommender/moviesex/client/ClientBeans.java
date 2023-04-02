package edu.vandy.recommender.moviesex.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import static edu.vandy.recommender.common.Constants.MOVIES_BASE_URL;
import static edu.vandy.recommender.common.Constants.Service.MOVIES;
import static edu.vandy.recommender.common.Constants.Service.TIMER;

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

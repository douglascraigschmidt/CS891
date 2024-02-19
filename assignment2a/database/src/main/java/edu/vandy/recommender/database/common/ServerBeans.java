package edu.vandy.recommender.database.common;

import edu.vandy.recommender.database.server.DatabaseRepository;
import edu.vandy.recommender.database.common.model.Movie;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestTemplate;

/**
 * This class contains a {@code Bean} annotation that can be injected into
 * classes using the Spring {@code @Autowired} annotation.
 */
@Configuration
@PropertySource("classpath:application.yml")
public class ServerBeans {
    /**
     * Database where movies will be installed.
     */
    @Autowired
    DatabaseRepository repository;

    /**
     * Spring will inject the dataset file path from
     * the property value in the application resources.
     */
    @Value("${app.dataset}")
    private String dataset;

    /**
     * This method will run once the application has been created and
     * will load the repository from the movie dataset file in the
     * application resources.
     */
    @PostConstruct
    public void loadDatabaseFromResources() {
        MoviesDatasetReader
            // Load the vector map from resources.
            .loadMovieData(dataset)

            // Add each vector entry into database table.
            .forEach((key, value) -> repository.save(new Movie(key, value)));
    }

    /**
     * @return A new {@link RestTemplate}.
     */
    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}

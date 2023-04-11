package edu.vandy.recommender.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class contains a {@code Bean} annotation that can be injected into
 * classes using the Spring {@code @Autowired} annotation.
 */
@Component
@PropertySource(
    value = "classpath:/application.yml",
    factory = YamlPropertySourceFactory.class)
public class ServerBeans {
    /**
     * Constructs a {@link TreeMap} Bean that contains the movie titles
     * and cosine vectors.
     *
     * @return A {@link Map} containing all movie titles and associated
     *         cosine vectors.
     */
    @Lazy // Only create this bean lazily (on demand).
    @Bean("movieMap")
    public Map<String, List<Double>> getMovieMap
        (@Value("${app.dataset}") final String dataset) {
        try {
            // return LoadVectors.loadVectors(dataset);
            return MoviesLoader.loadMoviesFromDatabase();
        } catch (Exception e) {
            System.out.println(
                "ERROR! Unable to retrieve movie cosine"
                    + " vectors from database microservice: "
                    + e);
            // Return an empty TreeMap.
            return new TreeMap<>();
        }
    }
}

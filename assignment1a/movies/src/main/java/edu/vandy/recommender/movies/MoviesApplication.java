package edu.vandy.recommender.movies;

import edu.vandy.recommender.movies.model.Movie;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.util.List;

/**
 * This class provides the entry point for the MoviesApplication
 * microservice, whose methods return a {@link List} of objects
 * containing information about movies.
 * 
 * The {@code @SpringBootApplication} annotation enables apps to use
 * automatic configuration, component scan, and to define extra
 * configurations on their "application" class.
 */
@SpringBootApplication
public class MoviesApplication {
    /**
     * The static main() entry point runs this Spring application.
     */
    public static void main(String[] args) {
        // Launch this application.
        SpringApplication.run(MoviesApplication.class, args);
    }
}

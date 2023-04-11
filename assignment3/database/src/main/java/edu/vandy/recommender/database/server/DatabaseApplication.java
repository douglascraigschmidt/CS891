package edu.vandy.recommender.database.server;

import edu.vandy.recommender.common.BaseApplication;
import edu.vandy.recommender.common.model.Movie;
import edu.vandy.recommender.database.repository.DatabaseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;

/**
 * This class provides the entry point for the DataApplication
 * microservice, whose methods return a {@link List} of objects
 * containing information about {@link Movie} objects.
 * 
 * The {@code @SpringBootApplication} annotation enables apps to use
 * automatic configuration, component scan, and to define extra
 * configurations on their "application" class.
 *
 * The {@code @EntityScan} annotation is used when entity classes are
 * not placed in the main application package or its sub-packages.
 *
 * The {@code @ComponentScan} annotation tells Spring the packages to
 * scan for annotated components (i.e., tagged with
 * {@code @Component}).  In this case, this annotation instructs
 * Spring to scan components in the "edu.vandy.recommender.movies"
 * package.
 *
 * The {@code @EnableJpaRepositories} annotation enables the use of
 * JPA repositories by scanning the package of the annotated
 * configuration class for Spring Data repositories.
 *
 * The {@code ComponentScan} annotation instructs Spring to scan for
 * components that are within the "edu.vandy.recommender.database"
 * package.
 */
@SpringBootApplication
@ComponentScan("edu.vandy.recommender.database")
@EntityScan(basePackageClasses = Movie.class)
@EnableJpaRepositories(basePackageClasses = DatabaseRepository.class)
@EnableCaching
public class DatabaseApplication
       extends BaseApplication {
    /**
     * The static main() entry point runs this Spring application.
     */
    public static void main(String[] args) {
        // Call the BaseApplication.run() method to build and run this
        // application.
        run(DatabaseApplication.class, args);
    }

    // @Bean
    public CommandLineRunner demo(DatabaseRepository repository) {
        return args -> {
            var list = repository.findAll();
            list.forEach(movie ->
                         System.out.println(movie.id));
        };
    }
}

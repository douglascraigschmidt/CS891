package edu.vandy.recommender.databaseex.server;

import edu.vandy.recommender.common.BaseApplication;
import edu.vandy.recommender.common.model.Movie;
import edu.vandy.recommender.databaseex.repository.DatabaseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

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
 * The {@code @EnableR2dbcRepositories} annotation enables the use of
 * R2DBC repositories by scanning the package of the annotated
 * configuration class for Spring Data repositories.
 *
 * The {@code ComponentScan} annotation instructs Spring to scan for
 * components that are within the "edu.vandy.recommender.database"
 * package.
 */
@SpringBootApplication
@ComponentScan("edu.vandy.recommender.databaseex")
@EntityScan(basePackageClasses = Movie.class)
@EnableR2dbcRepositories(basePackageClasses = DatabaseRepository.class)
public class DatabaseExApplication
       extends BaseApplication {
    /**
     * The static main() entry point runs this Spring application.
     */
    public static void main(String[] args) {
        // Call the BaseApplication.run() method to build and run this
        // application.
        run(DatabaseExApplication.class, args);
    }

    // @Bean
    public CommandLineRunner demo(DatabaseRepository repository) {
        return args -> {
            repository.findAll().subscribe(movie ->
                System.out.println(movie.id));
        };
    }
}
